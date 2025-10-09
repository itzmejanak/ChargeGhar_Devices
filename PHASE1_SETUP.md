# Phase 1 Setup Guide - Admin Authentication & Access Control

## Overview
Phase 1 implements secure admin authentication using Spring Security with PostgreSQL database backend. Only authenticated admins can access the system.

## Features Implemented
- ✅ PostgreSQL database integration
- ✅ Spring Security configuration
- ✅ Admin login/logout functionality
- ✅ Admin management (view/add/edit/delete admins)
- ✅ Password encryption (BCrypt)
- ✅ Session-based authentication
- ✅ Responsive Freemarker templates
- ✅ Default admin account creation
- ✅ Admin self-protection (cannot delete own account)
- ✅ Last admin protection (cannot delete last admin)

## Quick Start

### 1. Database Setup (Choose one option)

#### Option A: Using Docker (Recommended)
```bash
# Start PostgreSQL and Redis
docker-compose up postgres redis -d

# Wait for services to be ready
docker-compose logs postgres
```

#### Option B: Local PostgreSQL
```bash
# Install PostgreSQL and run the setup script
psql -U postgres -f database-setup.sql
```

### 2. Build and Run Application
```bash
# Build the application
mvn clean package

# Run with Maven (development)
mvn tomcat7:run

# Or deploy the WAR file to your Tomcat server
cp target/ROOT.war /path/to/tomcat/webapps/
```

### 3. Access the Application
- **URL**: http://localhost:8080
- **Default Admin Credentials**:
  - Username: `admin`
  - Password: `admin123`

## Configuration

### Database Configuration
Update `src/main/resources/config.properties`:
```properties
# PostgreSQL Configuration
database.url=jdbc:postgresql://localhost:5432/iotdemo
database.username=iotdemo
database.password=password
```

### Environment Variables (Docker)
```bash
DATABASE_URL=jdbc:postgresql://postgres:5432/iotdemo
DATABASE_USERNAME=iotdemo
DATABASE_PASSWORD=password
```

## Admin Management

### Default Admin
- The system automatically creates a default admin on first startup
- **Username**: admin
- **Password**: admin123
- **⚠️ Change this password immediately in production!**

### Managing Admins

#### Adding New Admins
1. Login as an existing admin
2. Navigate to "Admin Management" → "View Admins"
3. Click "Add New Admin"
4. Fill in username and password (min 6 characters)
5. Confirm password and submit

#### Editing Admins
1. Go to "Admin Management" → "View Admins"
2. Click "Edit" button next to the admin you want to modify
3. Update username and/or password as needed
4. Leave password fields empty to keep current password
5. Click "Update Admin" to save changes

#### Deleting Admins
1. Go to "Admin Management" → "View Admins"
2. Click "Delete" button next to the admin (not available for your own account)
3. Confirm deletion in the popup dialog
4. Note: Cannot delete the last remaining admin

## Security Features

### Authentication
- Session-based authentication using Spring Security
- BCrypt password hashing
- Automatic session timeout
- Secure logout functionality

### Access Control
- All pages require authentication except login page
- Static resources (CSS, JS, images) are publicly accessible
- Automatic redirect to login for unauthenticated users

### Password Requirements
- Minimum 6 characters
- Stored using BCrypt encryption
- Password confirmation required

## Database Schema

### Admins Table
```sql
CREATE TABLE admins (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials in config.properties
   - Ensure database `iotdemo` exists

2. **Login Page Not Loading**
   - Check Spring Security configuration
   - Verify JSP files are in correct location
   - Check Tomcat logs for errors

3. **Default Admin Not Created**
   - Check database connection
   - Verify admins table exists
   - Check application logs for initialization errors

### Logs
- Application logs: Check Tomcat catalina.out
- Database logs: Check PostgreSQL logs
- Spring Security: Enable debug logging if needed

## Next Steps (Phase 2)
Phase 1 is now complete and ready for Phase 2 implementation:
- API key generation and management
- API key validation middleware
- Session + API key coexistence
- External API access control

## File Structure
```
src/main/java/com.demo/security/
├── SecurityConfig.java              # Spring Security configuration
├── DatabaseInitializer.java         # Database setup
├── controller/
│   ├── AuthController.java         # Login/logout handling
│   └── AdminController.java        # Admin management
├── model/
│   └── Admin.java                  # Admin entity
└── service/
    ├── AdminService.java           # Admin business logic
    └── CustomUserDetailsService.java # Spring Security integration

src/main/webapp/web/views/
├── auth/
│   └── login.html                  # Login page
└── admin/
    ├── dashboard.html              # Main dashboard
    ├── admins.html                 # Admin list with edit/delete
    ├── add_admin.html              # Add admin form
    └── edit_admin.html             # Edit admin form
```