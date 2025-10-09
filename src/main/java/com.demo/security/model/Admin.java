package com.demo.security.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Admin {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;

    public Admin() {}

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method for Freemarker date formatting
    public Date getCreatedAtAsDate() {
        return createdAt != null ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}