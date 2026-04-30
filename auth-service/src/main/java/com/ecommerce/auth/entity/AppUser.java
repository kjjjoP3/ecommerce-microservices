package com.ecommerce.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "APP_USER")
public class AppUser {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "PASSWORD_HASH")
    private String passwordHash;
    @Column(name = "ROLE_NAME")
    private String roleName;

    public AppUser() {}

    public AppUser(Long id, String username, String passwordHash, String roleName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roleName = roleName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
