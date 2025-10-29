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
import java.util.List;

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
     * Get current user info
     */
    @ResponseBody
    @GetMapping("/me")
    public HttpResult getCurrentUser(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        
        if (userId == null) {
            return HttpResult.error("Not authenticated");
        }
        
        AdminUser user = adminUserService.getAdminById(userId);
        if (user == null) {
            return HttpResult.error("User not found");
        }
        
        return HttpResult.ok(user);
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
