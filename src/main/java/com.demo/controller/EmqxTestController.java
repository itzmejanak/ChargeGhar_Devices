package com.demo.controller;

import com.demo.emqx.EmqxApiClient;
import com.demo.emqx.EmqxDeviceService;
import com.demo.emqx.DeviceCredentials;
import com.demo.common.DeviceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@ResponseBody
public class EmqxTestController {
    
    @Autowired
    private EmqxApiClient emqxApiClient;
    
    @Autowired
    private EmqxDeviceService emqxDeviceService;
    
    /**
     * Test EMQX API connectivity
     */
    @RequestMapping("/emqx/test/connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean connected = emqxApiClient.testConnection();
            result.put("status", connected ? "SUCCESS" : "FAILED");
            result.put("message", connected ? "EMQX API connection successful" : "EMQX API connection failed");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Connection test failed: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }
    
    /**
     * Test device registration
     */
    @RequestMapping("/emqx/test/register")
    public Map<String, Object> testDeviceRegistration(@RequestParam String deviceName) {
        Map<String, Object> result = new HashMap<>();
        try {
            DeviceConfig config = emqxDeviceService.getOrCreateDeviceConfig(deviceName);
            result.put("status", "SUCCESS");
            result.put("message", "Device registered successfully");
            result.put("deviceName", config.getDeviceName());
            result.put("username", config.getIotId());
            result.put("host", config.getHost());
            result.put("port", config.getPort());
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Device registration failed: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }
    
    /**
     * Get device credentials
     */
    @RequestMapping("/emqx/test/credentials")
    public Map<String, Object> getDeviceCredentials(@RequestParam String deviceName) {
        Map<String, Object> result = new HashMap<>();
        try {
            DeviceCredentials credentials = emqxDeviceService.getDeviceCredentials(deviceName);
            if (credentials != null) {
                result.put("status", "SUCCESS");
                result.put("deviceName", credentials.getDeviceName());
                result.put("username", credentials.getUsername());
                result.put("createdTime", credentials.getCreatedTime());
                result.put("message", "Device credentials found");
            } else {
                result.put("status", "NOT_FOUND");
                result.put("message", "Device credentials not found");
            }
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Failed to get credentials: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }
    
    /**
     * Test message publishing via EMQX API
     */
    @RequestMapping("/emqx/test/publish")
    public Map<String, Object> testPublish(@RequestParam String topic, 
                                          @RequestParam String message,
                                          @RequestParam(defaultValue = "1") int qos) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean published = emqxApiClient.publishMessage(topic, message, qos);
            result.put("status", published ? "SUCCESS" : "FAILED");
            result.put("message", published ? "Message published successfully" : "Failed to publish message");
            result.put("topic", topic);
            result.put("payload", message);
            result.put("qos", qos);
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Publish failed: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }
    
    /**
     * Remove test device
     */
    @RequestMapping("/emqx/test/remove")
    public Map<String, Object> removeDevice(@RequestParam String deviceName) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean removed = emqxDeviceService.removeDevice(deviceName);
            result.put("status", removed ? "SUCCESS" : "FAILED");
            result.put("message", removed ? "Device removed successfully" : "Failed to remove device");
            result.put("deviceName", deviceName);
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Remove failed: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }
}