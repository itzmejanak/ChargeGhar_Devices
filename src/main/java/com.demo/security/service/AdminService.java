package com.demo.security.service;

import com.demo.security.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String SELECT_ALL = "SELECT id, username, password, created_at FROM admins ORDER BY created_at DESC";
    private static final String SELECT_BY_USERNAME = "SELECT id, username, password, created_at FROM admins WHERE username = ?";
    private static final String SELECT_BY_ID = "SELECT id, username, password, created_at FROM admins WHERE id = ?";
    private static final String INSERT_ADMIN = "INSERT INTO admins (username, password, created_at) VALUES (?, ?, ?)";
    private static final String UPDATE_ADMIN_PASSWORD = "UPDATE admins SET password = ? WHERE id = ?";
    private static final String UPDATE_ADMIN_USERNAME = "UPDATE admins SET username = ? WHERE id = ?";
    private static final String DELETE_ADMIN = "DELETE FROM admins WHERE id = ?";
    private static final String COUNT_ADMINS = "SELECT COUNT(*) FROM admins";

    public List<Admin> findAllAdmins() {
        return jdbcTemplate.query(SELECT_ALL, new AdminRowMapper());
    }

    public Admin findByUsername(String username) {
        List<Admin> admins = jdbcTemplate.query(SELECT_BY_USERNAME, new AdminRowMapper(), username);
        return admins.isEmpty() ? null : admins.get(0);
    }

    public void createAdmin(String username, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        jdbcTemplate.update(INSERT_ADMIN, username, encodedPassword, LocalDateTime.now());
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public long getAdminCount() {
        return jdbcTemplate.queryForObject(COUNT_ADMINS, Long.class);
    }

    public Admin findById(Long id) {
        List<Admin> admins = jdbcTemplate.query(SELECT_BY_ID, new AdminRowMapper(), id);
        return admins.isEmpty() ? null : admins.get(0);
    }

    public void updateAdminPassword(Long id, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        jdbcTemplate.update(UPDATE_ADMIN_PASSWORD, encodedPassword, id);
    }

    public void updateAdminUsername(Long id, String newUsername) {
        jdbcTemplate.update(UPDATE_ADMIN_USERNAME, newUsername, id);
    }

    public void deleteAdmin(Long id) {
        jdbcTemplate.update(DELETE_ADMIN, id);
    }

    public boolean isUsernameAvailable(String username, Long excludeId) {
        String query = "SELECT COUNT(*) FROM admins WHERE username = ? AND id != ?";
        Long count = jdbcTemplate.queryForObject(query, Long.class, username, excludeId);
        return count == 0;
    }

    public void initializeDefaultAdmin() {
        if (getAdminCount() == 0) {
            createAdmin("admin", "admin123");
            System.out.println("✅ Default admin created: username=admin, password=admin123");
        }
    }

    private static class AdminRowMapper implements RowMapper<Admin> {
        @Override
        public Admin mapRow(ResultSet rs, int rowNum) throws SQLException {
            Admin admin = new Admin();
            admin.setId(rs.getLong("id"));
            admin.setUsername(rs.getString("username"));
            admin.setPassword(rs.getString("password"));
            admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return admin;
        }
    }
}