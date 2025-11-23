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
        try {
            JsonNode event = objectMapper.readTree(payload);
            
            String action = event.get("action").asText();
            String clientId = event.get("clientid").asText();
            
            // Only process device clients (15-digit numbers)
            if (!clientId.matches("\\d{15}")) {
                return;
            }
            
            String deviceName = clientId;
            long now = System.currentTimeMillis();
            
            if ("client.connected".equals(action)) {
                System.out.println("🟢 Device CONNECTED: " + deviceName);
                updateDeviceStatus(deviceName, now);
            }
            else if ("client.disconnected".equals(action)) {
                System.out.println("🔴 Device DISCONNECTED: " + deviceName);
                updateDeviceStatus(deviceName, now);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
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
