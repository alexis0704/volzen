package com.app.venus.modules.user.domain;

import com.app.venus.shared.auditing.Auditable;
import com.app.venus.shared.domain.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User extends Auditable {
    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;

    private String avatarUrl;

    protected User() {
    }

    public User(String id, String fullName, String email, Role role, String avatarUrl) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void updateProfile(String fullName, String avatarUrl) {
        if (fullName != null) {
            this.fullName = fullName;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }
}
