package com.demo.dao;

import com.demo.model.Device;
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
 * Device Data Access Object
 * Handles all database operations for devices table
 */
public class DeviceDao {
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper for Device - Essential fields only
     */
    private final RowMapper<Device> rowMapper = new RowMapper<Device>() {
        @Override
        public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
            Device device = new Device();
            device.setId(rs.getInt("id"));
            device.setDeviceName(rs.getString("device_name"));
            device.setImei(rs.getString("imei"));
            device.setPassword(rs.getString("password"));
            device.setCreatedAt(rs.getTimestamp("created_at"));
            device.setCreatedBy((Integer) rs.getObject("created_by"));
            device.setUpdatedAt(rs.getTimestamp("updated_at"));
            return device;
        }
    };

    /**
     * Find all devices
     */
    public List<Device> findAll() {
        String sql = "SELECT * FROM devices ORDER BY device_name";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Find all active devices
     */
    public List<Device> findAllActive() {
        String sql = "SELECT * FROM devices WHERE status = 'ACTIVE' ORDER BY device_name";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Get all device names (for backwards compatibility with machines.properties)
     */
    public List<String> getAllDeviceNames() {
        String sql = "SELECT device_name FROM devices ORDER BY device_name";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Find device by device name
     */
    public Device findByDeviceName(String deviceName) {
        String sql = "SELECT * FROM devices WHERE device_name = ?";
        List<Device> devices = jdbcTemplate.query(sql, rowMapper, deviceName);
        return devices.isEmpty() ? null : devices.get(0);
    }

    /**
     * Find device by ID
     */
    public Device findById(Integer id) {
        String sql = "SELECT * FROM devices WHERE id = ?";
        List<Device> devices = jdbcTemplate.query(sql, rowMapper, id);
        return devices.isEmpty() ? null : devices.get(0);
    }

    /**
     * Create new device - Essential fields only
     */
    public Integer create(Device device) {
        String sql = "INSERT INTO devices (device_name, imei, password, created_by) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, device.getDeviceName());
            ps.setString(2, device.getImei());
            ps.setString(3, device.getPassword());
            if (device.getCreatedBy() != null) {
                ps.setInt(4, device.getCreatedBy());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        
        return keyHolder.getKey().intValue();
    }

    /**
     * Update device - Essential fields only
     */
    public void update(Device device) {
        String sql = "UPDATE devices SET imei = ? WHERE id = ?";
        jdbcTemplate.update(sql, device.getImei(), device.getId());
    }

    /**
     * Check if IMEI exists
     */
    public boolean existsByImei(String imei) {
        if (imei == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM devices WHERE imei = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, imei);
        return count != null && count > 0;
    }

    /**
     * Update device password
     */
    public void updatePassword(String deviceName, String password) {
        String sql = "UPDATE devices SET password = ? WHERE device_name = ?";
        jdbcTemplate.update(sql, password, deviceName);
    }

    /**
     * Delete device
     */
    public void delete(Integer id) {
        String sql = "DELETE FROM devices WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Check if device name exists
     */
    public boolean existsByDeviceName(String deviceName) {
        String sql = "SELECT COUNT(*) FROM devices WHERE device_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, deviceName);
        return count != null && count > 0;
    }

    /**
     * Get total device count
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM devices";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Get device statistics
     */
    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total devices
        String totalSql = "SELECT COUNT(*) FROM devices";
        Integer total = jdbcTemplate.queryForObject(totalSql, Integer.class);
        stats.put("totalDevices", total != null ? total : 0);
        
        // Devices created today
        String todaySql = "SELECT COUNT(*) FROM devices WHERE DATE(created_at) = CURDATE()";
        Integer today = jdbcTemplate.queryForObject(todaySql, Integer.class);
        stats.put("devicesCreatedToday", today != null ? today : 0);
        
        // Devices created this week
        String weekSql = "SELECT COUNT(*) FROM devices WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        Integer week = jdbcTemplate.queryForObject(weekSql, Integer.class);
        stats.put("devicesCreatedThisWeek", week != null ? week : 0);
        
        return stats;
    }
}
