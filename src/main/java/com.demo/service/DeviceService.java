package com.demo.service;

import com.demo.dao.DeviceDao;
import com.demo.model.Device;

import java.util.List;
import java.util.Map;

public class DeviceService {
    private DeviceDao deviceDao;

    public void setDeviceDao(DeviceDao deviceDao) {
        this.deviceDao = deviceDao;
    }

    /**
     * Get all devices
     */
    public List<Device> getAllDevices() {
        return deviceDao.findAll();
    }

    /**
     * Get all active devices
     */
    public List<Device> getAllActiveDevices() {
        return deviceDao.findAllActive();
    }

    /**
     * Get device names (for backwards compatibility)
     */
    public List<String> getDeviceNames() {
        return deviceDao.getAllDeviceNames();
    }

    /**
     * Get device by name
     */
    public Device getDeviceByName(String deviceName) {
        return deviceDao.findByDeviceName(deviceName);
    }

    /**
     * Verify device password
     */
    public boolean verifyDevicePassword(String deviceName, String password) {
        Device device = deviceDao.findByDeviceName(deviceName);
        if (device == null) {
            return false;
        }
        return device.getPassword() != null && device.getPassword().equals(password);
    }

    /**
     * Update device password
     */
    public boolean updateDevicePassword(String deviceName, String newPassword) {
        try {
            deviceDao.updatePassword(deviceName, newPassword);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to update device password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get device statistics
     */
    public Map<String, Object> getDeviceStatistics() {
        return deviceDao.getDeviceStatistics();
    }

    /**
     * Create new device with essential validation
     */
    public Device createDevice(Device device, Integer creatorId) {
        // Check if device already exists
        if (deviceDao.existsByDeviceName(device.getDeviceName())) {
            System.out.println("❌ Device already exists: " + device.getDeviceName());
            return null;
        }

        // Check if IMEI already exists (if provided)
        if (device.getImei() != null && deviceDao.existsByImei(device.getImei())) {
            System.out.println("❌ IMEI already exists: " + device.getImei());
            return null;
        }

        device.setCreatedBy(creatorId);
        Integer deviceId = deviceDao.create(device);
        device.setId(deviceId);

        System.out.println("✅ Device created: " + device.getDeviceName());
        return device;
    }

    /**
     * Update device
     */
    public boolean updateDevice(Device device) {
        try {
            deviceDao.update(device);
            System.out.println("✅ Device updated: " + device.getDeviceName());
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to update device: " + e.getMessage());
            return false;
        }
    }



    /**
     * Delete device
     */
    public boolean deleteDevice(Integer deviceId) {
        try {
            deviceDao.delete(deviceId);
            System.out.println("✅ Device deleted: ID " + deviceId);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to delete device: " + e.getMessage());
            return false;
        }
    }


}
