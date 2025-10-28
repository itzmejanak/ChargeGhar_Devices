package com.demo.dao;

import com.demo.model.AdminUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin User Data Access Object
 * Handles all database operations for admin_users table
 */
public class AdminUserDao {
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper for AdminUser
     */
    private final RowMapper<AdminUser> rowMapper = new RowMapper<AdminUser>() {
        @Override
        public AdminUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdminUser user = new AdminUser();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            user.setFullName(rs.getString("full_name"));
            user.setRole(rs.getString("role"));
            user.setIsActive(rs.getBoolean("is_active"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
            user.setUpdatedAt(rs.getTimestamp("updated_at"));
            user.setLastLogin(rs.getTimestamp("last_login"));
            user.setCreatedBy((Integer) rs.getObject("created_by"));
            return user;
        }
    };

    /**
     * Find admin user by username
     */
    public AdminUser findByUsername(String username) {
        String sql = "SELECT * FROM admin_users WHERE username = ?";
        List<AdminUser> users = jdbcTemplate.query(sql, rowMapper, username);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Find admin user by ID
     */
    public AdminUser findById(Integer id) {
        String sql = "SELECT * FROM admin_users WHERE id = ?";
        List<AdminUser> users = jdbcTemplate.query(sql, rowMapper, id);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Find all admin users
     */
    public List<AdminUser> findAll() {
        String sql = "SELECT * FROM admin_users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Create new admin user
     */
    public Integer create(AdminUser user) {
        String sql = "INSERT INTO admin_users (username, password, email, full_name, role, is_active, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole());
            ps.setBoolean(6, user.getIsActive() != null ? user.getIsActive() : true);
            if (user.getCreatedBy() != null) {
                ps.setInt(7, user.getCreatedBy());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        
        return keyHolder.getKey().intValue();
    }

    /**
     * Update admin user
     */
    public void update(AdminUser user) {
        String sql = "UPDATE admin_users SET email = ?, full_name = ?, role = ?, is_active = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getFullName(), user.getRole(), user.getIsActive(), user.getId());
    }

    /**
     * Change password
     */
    public void changePassword(Integer userId, String newPasswordHash) {
        String sql = "UPDATE admin_users SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, newPasswordHash, userId);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin(Integer userId) {
        String sql = "UPDATE admin_users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    /**
     * Deactivate admin user
     */
    public void deactivate(Integer userId) {
        String sql = "UPDATE admin_users SET is_active = FALSE WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM admin_users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM admin_users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Get total admin count
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM admin_users";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Get active admin count
     */
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM admin_users WHERE is_active = TRUE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Get admin user statistics
     */
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total active admins
        String totalSql = "SELECT COUNT(*) FROM admin_users WHERE is_active = TRUE";
        Integer total = jdbcTemplate.queryForObject(totalSql, Integer.class);
        stats.put("totalAdmins", total != null ? total : 0);
        
        // Super admins count
        String superAdminSql = "SELECT COUNT(*) FROM admin_users WHERE role = 'SUPER_ADMIN' AND is_active = TRUE";
        Integer superAdmins = jdbcTemplate.queryForObject(superAdminSql, Integer.class);
        stats.put("superAdmins", superAdmins != null ? superAdmins : 0);
        
        // Regular admins count
        String adminSql = "SELECT COUNT(*) FROM admin_users WHERE role = 'ADMIN' AND is_active = TRUE";
        Integer admins = jdbcTemplate.queryForObject(adminSql, Integer.class);
        stats.put("regularAdmins", admins != null ? admins : 0);
        
        // Admins with recent login (last 7 days)
        String recentLoginSql = "SELECT COUNT(*) FROM admin_users WHERE last_login >= DATE_SUB(NOW(), INTERVAL 7 DAY) AND is_active = TRUE";
        Integer recentLogins = jdbcTemplate.queryForObject(recentLoginSql, Integer.class);
        stats.put("recentActiveAdmins", recentLogins != null ? recentLogins : 0);
        
        return stats;
    }
}
