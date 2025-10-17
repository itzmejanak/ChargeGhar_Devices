package com.demo.emqx;

import com.demo.common.AppConfig;
import com.demo.tools.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmqxApiClient {
    
    @Autowired
    private AppConfig appConfig;
    
    private CloseableHttpClient httpClient;
    private boolean connectionValidated = false;
    
    public EmqxApiClient() {
        this.httpClient = HttpClients.createDefault();
    }
    
    @javax.annotation.PostConstruct
    public void validateConnection() {
        try {
            // Validate EMQX API credentials on startup
            if (appConfig.getEmqxApiKey() == null || appConfig.getEmqxApiKey().equals("YOUR_API_KEY_HERE")) {
                System.err.println("WARNING: EMQX API key not configured. Please update config.properties");
                return;
            }
            
            boolean connected = testConnection();
            if (connected) {
                connectionValidated = true;
                System.out.println("✅ EMQX API connection validated successfully");
            } else {
                System.err.println("❌ EMQX API connection validation failed. Check credentials and network connectivity.");
            }
        } catch (Exception e) {
            System.err.println("❌ EMQX API validation error: " + e.getMessage());
        }
    }
    
    public boolean isConnectionValidated() {
        return connectionValidated;
    }
    
    /**
     * Register a new device with EMQX platform
     */
    public boolean registerDevice(String deviceId, String password) throws Exception {
        String url = appConfig.getEmqxApiUrl() + "/authentication/password_based:built_in_database/users";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", deviceId);
        requestBody.put("password", password);
        
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthHeader());
        request.setEntity(new StringEntity(JsonUtils.toJson(requestBody), StandardCharsets.UTF_8));
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 201) {
                System.out.println("Device registered successfully: " + deviceId);
                return true;
            } else if (statusCode == 409) {
                System.out.println("Device already exists, updating password: " + deviceId);
                // Device exists, update password to ensure sync
                return updateDevicePassword(deviceId, password);
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.err.println("Failed to register device: " + statusCode + " - " + responseBody);
                return false;
            }
        }
    }
    
    /**
     * Check if device exists in EMQX platform
     */
    public boolean deviceExists(String deviceId) throws Exception {
        String url = appConfig.getEmqxApiUrl() + "/api/v5/authentication/password_based:built_in_database/users/" + deviceId;
        
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", getAuthHeader());
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == 200;
        }
    }
    
    /**
     * Update device password
     */
    public boolean updateDevicePassword(String deviceId, String newPassword) throws Exception {
        String url = appConfig.getEmqxApiUrl() + "/authentication/password_based:built_in_database/users/" + deviceId;
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("password", newPassword);
        
        HttpPut request = new HttpPut(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthHeader());
        request.setEntity(new StringEntity(JsonUtils.toJson(requestBody), StandardCharsets.UTF_8));
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                System.out.println("Device password updated successfully: " + deviceId);
                return true;
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.err.println("Failed to update device password: " + statusCode + " - " + responseBody);
                return false;
            }
        }
    }
    
    /**
     * Delete device from EMQX platform
     */
    public boolean deleteDevice(String deviceId) throws Exception {
        String url = appConfig.getEmqxApiUrl() + "/authentication/password_based:built_in_database/users/" + deviceId;
        
        HttpDelete request = new HttpDelete(url);
        request.setHeader("Authorization", getAuthHeader());
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 204) {
                System.out.println("Device deleted successfully: " + deviceId);
                return true;
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.err.println("Failed to delete device: " + statusCode + " - " + responseBody);
                return false;
            }
        }
    }
    
    /**
     * Publish message to EMQX via REST API
     */
    public boolean publishMessage(String topic, String payload, int qos) throws Exception {
        String url = appConfig.getEmqxApiUrl() + "/publish";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("topic", topic);
        requestBody.put("payload", payload);
        requestBody.put("qos", qos);
        requestBody.put("retain", false);
        
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthHeader());
        request.setEntity(new StringEntity(JsonUtils.toJson(requestBody), StandardCharsets.UTF_8));
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                System.out.println("Message published successfully to topic: " + topic);
                return true;
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.err.println("Failed to publish message: " + statusCode + " - " + responseBody);
                return false;
            }
        }
    }
    
    /**
     * Generate Basic Auth header for EMQX API
     */
    private String getAuthHeader() {
        String credentials = appConfig.getEmqxApiKey() + ":" + appConfig.getEmqxApiSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }
    
    /**
     * Test API connectivity
     */
    public boolean testConnection() {
        try {
            // Use /clients endpoint instead of /status as it's accessible with our API key
            String url = appConfig.getEmqxApiUrl() + "/clients";
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", getAuthHeader());
            
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpResponse response = client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                
                if (statusCode == 200) {
                    System.out.println("EMQX API connection successful");
                    return true;
                } else {
                    System.err.println("EMQX API connection failed: " + statusCode);
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("EMQX API connection error: " + e.getMessage());
            return false;
        }
    }
}