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
            return new LoginResponse(false, "Invalid username or password");
        }

        if (!user.getIsActive()) {
            return new LoginResponse(false, "Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new LoginResponse(false, "Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Update last login
        adminUserDao.updateLastLogin(user.getId());

        // Don't send password in response
        user.setPassword(null);
        
        return new LoginResponse(true, "Login successful", token, user);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
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
            return null;
        }
    }

    /**
     * Refresh JWT token
     */
    public String refreshToken(AdminUser user) {
        try {
            // Verify user is still active
            if (!user.getIsActive()) {
                return null;
            }
            
            // Generate new token
            String newToken = jwtUtil.generateToken(user);
            
            // Update last login time
            adminUserDao.updateLastLogin(user.getId());
            
            return newToken;
        } catch (Exception e) {
            System.err.println("Error refreshing token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate token and check if refresh is needed
     */
    public TokenValidationResult validateTokenWithRefresh(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return new TokenValidationResult(false, false, null);
            }
            
            boolean needsRefresh = jwtUtil.needsRefresh(token);
            String username = jwtUtil.extractUsername(token);
            AdminUser user = adminUserDao.findByUsername(username);
            
            if (user == null || !user.getIsActive()) {
                return new TokenValidationResult(false, false, null);
            }
            
            return new TokenValidationResult(true, needsRefresh, user);
        } catch (Exception e) {
            return new TokenValidationResult(false, false, null);
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
        
        return true;
    }

    /**
     * Token validation result class
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final boolean needsRefresh;
        private final AdminUser user;
        
        public TokenValidationResult(boolean valid, boolean needsRefresh, AdminUser user) {
            this.valid = valid;
            this.needsRefresh = needsRefresh;
            this.user = user;
        }
        
        public boolean isValid() { return valid; }
        public boolean needsRefresh() { return needsRefresh; }
        public AdminUser getUser() { return user; }
    }
}
