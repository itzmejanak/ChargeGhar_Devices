package com.demo.controller;

import com.demo.common.HttpResult;
import com.demo.model.AdminUser;
import com.demo.model.LoginRequest;
import com.demo.model.LoginResponse;
import com.demo.service.AdminUserService;
import com.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AdminUserService adminUserService;

    /**
     * Login endpoint
     */
    @ResponseBody
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        
        if (loginResponse.isSuccess()) {
            // Set JWT token in cookie
            Cookie cookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(cookie);
        }
        
        return loginResponse;
    }

    /**
     * Logout endpoint
     */
    @ResponseBody
    @PostMapping("/logout")
    public HttpResult logout(HttpServletResponse response) {
        // Remove JWT token cookie
        Cookie cookie = new Cookie("JWT_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie
        response.addCookie(cookie);
        
        return HttpResult.ok("Logged out successfully");
    }

    /**
     * Get current user info and validate token
     */
    @ResponseBody
    @GetMapping("/me")
    public HttpResult getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        
        if (userId == null || username == null) {
            return HttpResult.error(401, "Not authenticated");
        }
        
        AdminUser user = adminUserService.getAdminById(userId);
        if (user == null) {
            return HttpResult.error(404, "User not found");
        }
        
        // Check if token needs refresh (handled by filter)
        Boolean needsRefresh = (Boolean) request.getAttribute("needsRefresh");
        if (needsRefresh != null && needsRefresh) {
            // Generate new token
            String newToken = authService.refreshToken(user);
            if (newToken != null) {
                // Set new token in cookie
                Cookie cookie = new Cookie("JWT_TOKEN", newToken);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(24 * 60 * 60); // 24 hours
                response.addCookie(cookie);
                
                // Add new token to response
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("user", user);
                responseData.put("newToken", newToken);
                responseData.put("refreshed", true);
                
                return HttpResult.ok(responseData);
            }
        }
        
        return HttpResult.ok(user);
    }

    /**
     * Validate token endpoint
     */
    @ResponseBody
    @PostMapping("/validate")
    public HttpResult validateToken(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        
        if (userId == null) {
            return HttpResult.error(401, "Invalid or expired token");
        }
        
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("valid", true);
        tokenInfo.put("userId", userId);
        tokenInfo.put("username", username);
        tokenInfo.put("role", role);
        
        return HttpResult.ok(tokenInfo);
    }

    /**
     * Refresh token endpoint
     */
    @ResponseBody
    @PostMapping("/refresh")
    public HttpResult refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Integer userId = (Integer) request.getAttribute("userId");
        
        if (userId == null) {
            return HttpResult.error(401, "Not authenticated");
        }
        
        AdminUser user = adminUserService.getAdminById(userId);
        if (user == null) {
            return HttpResult.error(404, "User not found");
        }
        
        // Generate new token
        String newToken = authService.refreshToken(user);
        if (newToken == null) {
            return HttpResult.error(500, "Failed to refresh token");
        }
        
        // Set new token in cookie
        Cookie cookie = new Cookie("JWT_TOKEN", newToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        response.addCookie(cookie);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", newToken);
        responseData.put("user", user);
        
        return HttpResult.ok(responseData);
    }

    /**
     * Get all admins (SUPER_ADMIN only)
     */
    @ResponseBody
    @GetMapping("/admins")
    public HttpResult getAllAdmins(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        
        if (!"SUPER_ADMIN".equals(role)) {
            return HttpResult.error("Access denied. SUPER_ADMIN only.");
        }
        
        List<AdminUser> admins = adminUserService.getAllAdmins();
        return HttpResult.ok(admins);
    }

    /**
     * Create new admin (SUPER_ADMIN only)
     */
    @ResponseBody
    @PostMapping("/admins")
    public HttpResult createAdmin(@RequestBody AdminUser newAdmin, HttpServletRequest request) {
        Integer creatorId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        if (!"SUPER_ADMIN".equals(role)) {
            return HttpResult.error("Access denied. Only SUPER_ADMIN can create admins.");
        }
        
        AdminUser created = adminUserService.createAdmin(newAdmin, creatorId, role);
        
        if (created == null) {
            return HttpResult.error("Failed to create admin. Username or email may already exist.");
        }
        
        return HttpResult.ok(created);
    }
}
