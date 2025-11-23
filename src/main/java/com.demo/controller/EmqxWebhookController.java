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
            
            // Only process device clients (15-digit numbers)
            if (!clientId.matches("\\d{15}")) {
                System.out.println("⚠️  SKIPPED: Not a device client (not 15 digits)");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                return;
            }
            
            String deviceName = clientId;
            long now = System.currentTimeMillis();
            
            if ("client.connected".equals(action)) {
                System.out.println("🟢 Device CONNECTED: " + deviceName);
                updateDeviceStatus(deviceName, now);
                System.out.println("✅ Redis updated for device: " + deviceName);
            }
            else if ("client.disconnected".equals(action)) {
                System.out.println("🔴 Device DISCONNECTED: " + deviceName);
                updateDeviceStatus(deviceName, now);
                System.out.println("✅ Redis updated for device: " + deviceName);
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
}
