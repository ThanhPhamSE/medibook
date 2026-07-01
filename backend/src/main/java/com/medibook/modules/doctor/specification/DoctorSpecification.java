package com.medibook.modules.doctor.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.user.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

public final class DoctorSpecification {

    private DoctorSpecification() {
    }

    public static Specification<Doctor> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Doctor> hasKeyword(String keyword) {

        return (root, query, cb) -> {

            Join<Doctor, User> user = getUserJoin(root);

            return cb.like(
                    cb.lower(user.get("fullName")),
                    "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Doctor> hasSpecialty(Long specialtyId) {

        return (root, query, cb) -> {

            Join<Doctor, Specialty> specialty = getSpecialtyJoin(root);

            return cb.equal(
                    specialty.get("id"),
                    specialtyId);
        };
    }

    public static Specification<Doctor> hasMinExperience(Integer min) {
        return (root, query, cb) -> min == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("experienceYears"), min);
    }

    public static Specification<Doctor> hasMaxExperience(Integer max) {
        return (root, query, cb) -> max == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("experienceYears"), max);
    }

    public static Specification<Doctor> hasMinFee(BigDecimal min) {

        return (root, query, cb) -> cb.greaterThanOrEqualTo(
                root.get("consultationFee"),
                min);
    }

    public static Specification<Doctor> hasMaxFee(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("consultationFee"), max);
    }

    public static Specification<Doctor> hasMinRating(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("averageRating"), min);
    }

    public static Specification<Doctor> hasActiveStatus(Boolean active) {

        return (root, query, cb) -> {

            boolean status = (active == null) ? true : active;

            Join<Doctor, User> user = root.join("user");

            return cb.equal(user.get("isActive"), status);
        };
    }

    @SuppressWarnings("unchecked")
    private static Join<Doctor, User> getUserJoin(Root<Doctor> root) {

        for (Join<Doctor, ?> join : root.getJoins()) {

            if ("user".equals(join.getAttribute().getName())) {
                return (Join<Doctor, User>) join;
            }
        }

        return root.join("user");
    }

    @SuppressWarnings("unchecked")
    private static Join<Doctor, Specialty> getSpecialtyJoin(Root<Doctor> root) {

        for (Join<Doctor, ?> join : root.getJoins()) {

            if ("specialty".equals(join.getAttribute().getName())) {
                return (Join<Doctor, Specialty>) join;
            }
        }

        return root.join("specialty");
    }
}