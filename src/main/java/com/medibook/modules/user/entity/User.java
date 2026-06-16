package com.medibook.modules.user.entity;

import java.time.LocalDate;

import com.medibook.common.base.VersionedEntity;
import com.medibook.common.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
                @Index(name = "idx_users_role", columnList = "role_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email_active", columnNames = { "email", "deleted_at" }),
                @UniqueConstraint(name = "uq_users_phone_active", columnNames = { "phone", "deleted_at" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends VersionedEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        private Role role;

        @Column(nullable = false, length = 255)
        private String email;

        @Column(nullable = false, length = 255)
        private String password;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @Column(length = 20)
        private String phone;

        @Enumerated(EnumType.STRING)
        private Gender gender;

        @Column(name = "birth_date")
        private LocalDate birthDate;

        @Column(name = "profile_image", length = 500)
        private String profileImage;

        @Column(name = "is_active")
        private Boolean isActive = true;
}
