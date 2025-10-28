package com.demo.model;

/**
 * Login Response Model
 * Sent to client after authentication
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private AdminUser user;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, String token, AdminUser user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AdminUser getUser() {
        return user;
    }

    public void setUser(AdminUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", hasToken=" + (token != null) +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}
