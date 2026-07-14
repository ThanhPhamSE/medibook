package com.medibook.modules.user.specification;

import org.springframework.data.jpa.domain.Specification;

import com.medibook.modules.user.entity.User;

public class UserSpecification {

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank())
                return null;

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("phone")), like));
        };
    }

    public static Specification<User> roleId(Long roleId) {
        return (root, query, cb) -> roleId == null ? null : cb.equal(root.get("role").get("id"), roleId);
    }

    public static Specification<User> roleName(String roleName) {
        return (root, query, cb) -> (roleName == null || roleName.isBlank()) ? null : cb.equal(root.get("role").get("name"), roleName.toUpperCase());
    }

    public static Specification<User> isActive(Boolean isActive) {
        return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
    }
}