package com.medibook.modules.doctor.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.medibook.modules.doctor.entity.Doctor;

import jakarta.persistence.criteria.JoinType;

public final class DoctorSpecification {

    public DoctorSpecification() {
    }

    public static Specification<Doctor> withRelations() {

        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {

                root.fetch("user", JoinType.LEFT);
                root.fetch("specialty", JoinType.LEFT);

                query.distinct(true);
            }
            return cb.conjunction();
        };

    }

    public static Specification<Doctor> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Doctor> hasKeyword(String keyword) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(cb.lower(root.get("user").get("fullName")),
                    "%" + keyword.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Doctor> hasSpecialty(Long specialtyId) {

        return (root, query, cb) -> {

            if (specialtyId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("specialty").get("id"), specialtyId);
        };
    }

    public static Specification<Doctor> hasMinExperience(Integer minExperience) {

        return (root, query, cb) -> {

            if (minExperience == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("experienceYears"), minExperience);
        };
    }

    public static Specification<Doctor> hasMaxExperience(Integer maxExperience) {

        return (root, query, cb) -> {

            if (maxExperience == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("experienceYears"), maxExperience);
        };
    }

    public static Specification<Doctor> hasMinFee(BigDecimal minFee) {

        return (root, query, cb) -> {

            if (minFee == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("consultationFee"), minFee);
        };
    }

    public static Specification<Doctor> hasMaxFee(BigDecimal maxFee) {
        return (root, query, cb) -> {

            if (maxFee == null) {
                cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("consultationFee"), maxFee);
        };

    }

    public static Specification<Doctor> hasMinRating(BigDecimal minRating) {

        return (root, query, cb) -> {

            if (minRating == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
        };

    }

    public static Specification<Doctor> hasMaxRating(Integer maxRaitng) {

        return (root, query, cb) -> {

            if (maxRaitng == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("averageRating"), maxRaitng);
        };

    }

    public static Specification<Doctor> hasActiveStatus(Boolean active) {

        return (root, query, cb) -> {

            if (active == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("user").get("isActive"), active);
        };

    }
}
