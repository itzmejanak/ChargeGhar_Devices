package com.demo.service;

import com.demo.dao.AdminUserDao;
import com.demo.model.AdminUser;
import com.demo.model.LoginRequest;
import com.demo.model.LoginResponse;
import com.demo.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthService {
    private AdminUserDao adminUserDao;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;

    public void setAdminUserDao(AdminUserDao adminUserDao) {
        this.adminUserDao = adminUserDao;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate user and generate JWT token
     */
    public LoginResponse login(LoginRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new LoginResponse(false, "Username is required");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new LoginResponse(false, "Password is required");
        }

        // Find user
        AdminUser user = adminUserDao.findByUsername(request.getUsername());
        
        if (user == null) {
            System.out.println("❌ Login failed: User not found - " + request.getUsername());
            return new LoginResponse(false, "Invalid username or password");
        }

        if (!user.getIsActive()) {
            System.out.println("❌ Login failed: User inactive - " + request.getUsername());
            return new LoginResponse(false, "Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("❌ Login failed: Wrong password - " + request.getUsername());
            return new LoginResponse(false, "Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Update last login
        adminUserDao.updateLastLogin(user.getId());

        // Don't send password in response
        user.setPassword(null);

        System.out.println("✅ Login successful: " + user.getUsername() + " (Role: " + user.getRole() + ")");
        
        return new LoginResponse(true, "Login successful", token, user);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            System.out.println("❌ Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user from token
     */
    public AdminUser getUserFromToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            return adminUserDao.findByUsername(username);
        } catch (Exception e) {
            System.out.println("❌ Failed to get user from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Change password
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        AdminUser user = adminUserDao.findById(userId);
        
        if (user == null) {
            return false;
        }

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // Hash new password
        String hashedPassword = passwordEncoder.encode(newPassword);
        
        // Update password
        adminUserDao.changePassword(userId, hashedPassword);
        
        System.out.println("✅ Password changed for user: " + user.getUsername());
        return true;
    }
}
