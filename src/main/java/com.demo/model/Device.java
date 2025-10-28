package com.demo.model;

import java.util.Date;

/**
 * Device Model - Essential fields for device authentication
 */
public class Device {
    private Integer id;
    private String deviceName;
    private String imei;
    private String password;
    private Date createdAt;
    private Integer createdBy;
    private Date updatedAt;

    // Constructors
    public Device() {
    }

    public Device(String deviceName, String imei, String password) {
        this.deviceName = deviceName;
        this.imei = imei;
        this.password = password;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", deviceName='" + deviceName + '\'' +
                ", imei='" + imei + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
