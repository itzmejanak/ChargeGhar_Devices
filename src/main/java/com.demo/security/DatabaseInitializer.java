package com.demo.security;

import com.demo.security.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminService adminService;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void initialize() {
        try {
            createTables();
            adminService.initializeDefaultAdmin();
            System.out.println("✅ Database initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        // Create admins table
        String createAdminsTable = "CREATE TABLE IF NOT EXISTS admins (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        jdbcTemplate.execute(createAdminsTable);
        System.out.println("✅ Admins table created/verified");
    }
}