package com.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/emqx")
public class EmqxWebhookController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private com.demo.connector.ChargeGharConnector chargeGharConnector;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody String payload) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📥 EMQX WEBHOOK RECEIVED");
        System.out.println("Raw Payload: " + payload);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            JsonNode event = objectMapper.readTree(payload);
            
            String action = event.get("action").asText();
            String clientId = event.get("clientid").asText();
            
            System.out.println("Action: " + action);
            System.out.println("Client ID: " + clientId);
            
            // Extract device ID (remove device_ prefix if present)
            String deviceId = clientId;
            if (clientId.startsWith("device_")) {
                deviceId = clientId.substring(7); // Remove "device_" prefix
            }
            
            // Only process device clients (numeric, up to 20 digits)
            if (!deviceId.matches("\\d{1,20}")) {
                System.out.println("⚠️  SKIPPED: Not a device client (not numeric)");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                return;
            }
            
            long now = System.currentTimeMillis();
            
            if ("client.connected".equals(action)) {
                System.out.println("🟢 Device CONNECTED: " + deviceId);
                updateDeviceStatus(deviceId, now);
                System.out.println("✅ Redis updated for device: " + deviceId);
            }
            else if ("client.disconnected".equals(action)) {
                System.out.println("🔴 Device DISCONNECTED: " + deviceId);
                deleteDeviceStatus(deviceId);
                System.out.println("✅ Redis keys deleted for device: " + deviceId);
                
                // Send offline status to Django
                chargeGharConnector.sendDeviceStatus(deviceId, "OFFLINE");
                System.out.println("✅ Status sent to Django for device: " + deviceId);
            }
            else {
                System.out.println("⚠️  Unknown action: " + action);
            }
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR processing webhook: " + e.getMessage());
            e.printStackTrace();
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }
    
    private void updateDeviceStatus(String deviceName, long timestamp) {
        String heartbeatKey = "device_heartbeat:" + deviceName;
        BoundValueOperations heartbeatOps = redisTemplate.boundValueOps(heartbeatKey);
        heartbeatOps.set(timestamp, 5, TimeUnit.MINUTES);
        
        String activityKey = "device_activity:" + deviceName;
        BoundValueOperations activityOps = redisTemplate.boundValueOps(activityKey);
        activityOps.set(timestamp, 25, TimeUnit.MINUTES);
    }
    
    private void deleteDeviceStatus(String deviceName) {
        String heartbeatKey = "device_heartbeat:" + deviceName;
        String activityKey = "device_activity:" + deviceName;
        redisTemplate.delete(heartbeatKey);
        redisTemplate.delete(activityKey);
    }
}
