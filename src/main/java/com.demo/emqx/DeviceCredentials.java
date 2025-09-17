package com.demo.emqx;

import java.io.Serializable;
import java.util.Date;

/**
 * Device credentials for EMQX authentication
 */
public class DeviceCredentials implements Serializable {
    
    private String deviceName;
    private String username;
    private String password;
    private Date createdTime;
    
    public DeviceCredentials() {
    }
    
    public DeviceCredentials(String deviceName, String username, String password) {
        this.deviceName = deviceName;
        this.username = username;
        this.password = password;
        this.createdTime = new Date();
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Date getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    @Override
    public String toString() {
        return "DeviceCredentials{" +
                "deviceName='" + deviceName + '\'' +
                ", username='" + username + '\'' +
                ", password='[HIDDEN]'" +
                ", createdTime=" + createdTime +
                '}';
    }
}