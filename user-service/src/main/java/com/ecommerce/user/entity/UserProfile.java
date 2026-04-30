package com.ecommerce.user.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity @Table(name = "USER_PROFILE")
public class UserProfile {
    @Id @Column(name = "ID") private Long id;
    @Column(name = "EMAIL") private String email;
    @Column(name = "FULL_NAME") private String fullName;
    public UserProfile() {}
    public UserProfile(Long id, String email, String fullName) { this.id = id; this.email = email; this.fullName = fullName; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; } public void setFullName(String fullName) { this.fullName = fullName; }
}
