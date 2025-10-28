package com.demo.service;

import com.demo.dao.AdminUserDao;
import com.demo.model.AdminUser;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

public class AdminUserService {
    private AdminUserDao adminUserDao;
    private PasswordEncoder passwordEncoder;

    public void setAdminUserDao(AdminUserDao adminUserDao) {
        this.adminUserDao = adminUserDao;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all admin users
     */
    public List<AdminUser> getAllAdmins() {
        List<AdminUser> admins = adminUserDao.findAll();
        // Remove passwords from response
        admins.forEach(admin -> admin.setPassword(null));
        return admins;
    }

    /**
     * Get admin by ID
     */
    public AdminUser getAdminById(Integer id) {
        AdminUser admin = adminUserDao.findById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    /**
     * Create new admin (only SUPER_ADMIN can do this)
     */
    public AdminUser createAdmin(AdminUser newAdmin, Integer creatorId, String creatorRole) {
        // Check if creator is SUPER_ADMIN
        if (!"SUPER_ADMIN".equals(creatorRole)) {
            System.out.println("❌ Only SUPER_ADMIN can create new admins");
            return null;
        }

        // Validate username
        if (adminUserDao.existsByUsername(newAdmin.getUsername())) {
            System.out.println("❌ Username already exists: " + newAdmin.getUsername());
            return null;
        }

        // Validate email
        if (newAdmin.getEmail() != null && adminUserDao.existsByEmail(newAdmin.getEmail())) {
            System.out.println("❌ Email already exists: " + newAdmin.getEmail());
            return null;
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(newAdmin.getPassword());
        newAdmin.setPassword(hashedPassword);
        newAdmin.setCreatedBy(creatorId);

        // Create admin
        Integer adminId = adminUserDao.create(newAdmin);
        newAdmin.setId(adminId);

        System.out.println("✅ Admin created: " + newAdmin.getUsername() + " by user ID: " + creatorId);

        // Remove password before returning
        newAdmin.setPassword(null);
        return newAdmin;
    }

    /**
     * Update admin details
     */
    public boolean updateAdmin(AdminUser admin) {
        try {
            adminUserDao.update(admin);
            System.out.println("✅ Admin updated: " + admin.getUsername());
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to update admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deactivate admin
     */
    public boolean deactivateAdmin(Integer adminId, String requesterRole) {
        // Only SUPER_ADMIN can deactivate
        if (!"SUPER_ADMIN".equals(requesterRole)) {
            System.out.println("❌ Only SUPER_ADMIN can deactivate admins");
            return false;
        }

        try {
            adminUserDao.deactivate(adminId);
            System.out.println("✅ Admin deactivated: ID " + adminId);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to deactivate admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get admin statistics
     */
    public Map<String, Object> getAdminStatistics() {
        return adminUserDao.getAdminStatistics();
    }
}
