-- PostgreSQL Database Setup for IoT Demo Phase 1
-- Run this script to set up the database

-- Create database (run as postgres user)
CREATE DATABASE iotdemo;

-- Create user (run as postgres user)
CREATE USER iotdemo WITH PASSWORD 'password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE iotdemo TO iotdemo;

-- Connect to iotdemo database and create tables
\c iotdemo;

-- Create admins table
CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Grant table privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO iotdemo;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO iotdemo;

-- The application will automatically create a default admin:
-- Username: admin
-- Password: admin123