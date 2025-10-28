-- ============================================
-- ChargeGhar IoT Management System
-- Database Schema for Authentication & Device Management
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS chargeghar_iot 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE chargeghar_iot;

-- ============================================
-- Table: admin_users
-- Description: Stores admin user credentials and roles
-- ============================================
CREATE TABLE IF NOT EXISTS admin_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100),
    role ENUM('ADMIN', 'SUPER_ADMIN') NOT NULL DEFAULT 'ADMIN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    created_by INT,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Admin users with authentication credentials';

-- ============================================
-- Table: devices
-- Description: Device registry with essential fields
-- ============================================
DROP TABLE IF EXISTS devices;

CREATE TABLE devices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Device identifier (machine name)',
    imei VARCHAR(50) UNIQUE COMMENT 'Device IMEI number',
    password VARCHAR(255) NOT NULL COMMENT 'Device password for authentication',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_name (device_name),
    INDEX idx_imei (imei),
    FOREIGN KEY (created_by) REFERENCES admin_users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Device registry with essential authentication fields';

-- ============================================
-- Insert Default Admin User
-- Username: admin
-- Password: Admin@123 (BCrypt hashed)
-- Role: SUPER_ADMIN
-- ============================================
INSERT INTO admin_users (username, password, email, full_name, role, is_active) 
VALUES (
    'admin', 
    '$2b$10$KLLfQOjxFcaJ5RqZrq1j5ubyLDYsbbK9/wCn2syJIcAeLJYIuErOi',
    'admin@chargeghar.com',
    'System Administrator',
    'SUPER_ADMIN',
    TRUE
) ON DUPLICATE KEY UPDATE username=username;

-- ============================================
-- Insert Test Admin User (Optional)
-- Username: testadmin
-- Password: Test@123 (BCrypt hashed)
-- Role: ADMIN
-- ============================================
INSERT INTO admin_users (username, password, email, full_name, role, is_active, created_by) 
VALUES (
    'testadmin',
    '$2b$10$KLLfQOjxFcaJ5RqZrq1j5uDwfVXIhIcEItXdOlxLjElgdxezsV3Tq',
    'testadmin@chargeghar.com',
    'Test Administrator',
    'ADMIN',
    TRUE,
    1
) ON DUPLICATE KEY UPDATE username=username;

-- ============================================
-- Verification Queries
-- ============================================

-- Verify tables created
-- SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'chargeghar_iot';

-- Verify admin users
-- SELECT id, username, email, role, is_active, created_at FROM admin_users;

-- Verify devices
-- SELECT id, device_name, device_type, status, is_online, created_at FROM devices;
