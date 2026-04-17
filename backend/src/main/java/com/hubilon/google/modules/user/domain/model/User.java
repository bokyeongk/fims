package com.hubilon.google.modules.user.domain.model;

import com.hubilon.google.common.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 255)
    private String email;

    @Column(nullable = true, length = 100)
    private String name;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true, length = 30)
    private String phone;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String signatureData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    public enum Role {
        USER, ADMIN
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    public static User ofLocal(String email, String name, String password) {
        return User.builder()
                .email(email)
                .name(name)
                .password(password)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

    public static User ofSocial(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .password(null)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    public void updateProfile(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public void updateSignature(String signatureData) {
        this.signatureData = signatureData;
    }

    public void clearSignature() {
        this.signatureData = null;
    }
}
