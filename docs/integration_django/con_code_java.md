# ChargeGhar Integration - Java Implementation

## 📦 Package Structure
```
src/main/java/com.demo/
├── connector/
│   └── ChargeGharConnector.java     [NEW] - Main integration class
├── security/
│   ├── AuthTokenManager.java        [NEW] - Token management singleton
│   └── SignChargeGharMain.java      [NEW] - Signature utilities
├── dto/
│   ├── DeviceDataDTO.java           [NEW] - Device data transfer object
│   ├── StationDataDTO.java          [NEW] - Station sync DTO
│   └── ReturnEventDTO.java          [NEW] - Return event DTO
└── controller/
    └── ApiController.java           [MODIFY] - Add connector calls
```

---

## 🔐 1. Signature Utility Class

**File**: `src/main/java/com.demo/security/SignChargeGharMain.java`

```java
package com.demo.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Signature generation and validation for ChargeGhar Main API integration
 * Uses HMAC-SHA256 algorithm for request signing
 */
public class SignChargeGharMain {
    
    private static final String ALGORITHM = "HmacSHA256";
    private final String secretKey;
    
    public SignChargeGharMain(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        this.secretKey = secretKey;
    }
    
    /**
     * Generate HMAC-SHA256 signature for request
     * 
     * @param payload JSON request body as string
     * @param timestamp Unix timestamp in seconds
     * @return Base64 encoded signature
     */
    public String generateSignature(String payload, long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String message = payload + timestamp;
        
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(keySpec);
        
        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }
    
    /**
     * Validate signature from response (if needed)
     * 
     * @param payload Response body
     * @param timestamp Timestamp from response
     * @param receivedSignature Signature from X-Signature header
     * @return true if valid, false otherwise
     */
    public boolean validateSignature(String payload, long timestamp, String receivedSignature) {
        try {
            String computedSignature = generateSignature(payload, timestamp);
            return computedSignature.equals(receivedSignature);
        } catch (Exception e) {
            System.err.println("Signature validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current timestamp in seconds
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
}
```

---

## 🎫 2. Token Manager Singleton

**File**: `src/main/java/com.demo/security/AuthTokenManager.java`

```java
package com.demo.security;

/**
 * Singleton class to manage JWT tokens for ChargeGhar Main API
 * Thread-safe implementation
 */
public class AuthTokenManager {
    
    private static volatile AuthTokenManager instance;
    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile long tokenExpiresAt;  // Unix timestamp in milliseconds
    
    private static final long TOKEN_EXPIRY_BUFFER = 5 * 60 * 1000;  // 5 minutes buffer
    
    private AuthTokenManager() {}
    
    public static AuthTokenManager getInstance() {
        if (instance == null) {
            synchronized (AuthTokenManager.class) {
                if (instance == null) {
                    instance = new AuthTokenManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Set access token and calculate expiry time
     * JWT tokens typically expire in 60 minutes
     */
    public synchronized void setAccessToken(String token) {
        this.accessToken = token;
        // Assume 60 minute expiry, minus 5 minute buffer
        this.tokenExpiresAt = System.currentTimeMillis() + (55 * 60 * 1000);
    }
    
    public synchronized void setRefreshToken(String token) {
        this.refreshToken = token;
    }
    
    public synchronized String getAccessToken() {
        return accessToken;
    }
    
    public synchronized String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * Check if access token is expired or about to expire
     */
    public synchronized boolean isTokenExpired() {
        if (accessToken == null) {
            return true;
        }
        return System.currentTimeMillis() >= tokenExpiresAt;
    }
    
    /**
     * Clear all tokens (useful for logout or error recovery)
     */
    public synchronized void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiresAt = 0;
    }
}
```

---

## 📡 3. Main Connector Class

**File**: `src/main/java/com.demo/connector/ChargeGharConnector.java`

```java
package com.demo.connector;

import com.demo.security.AuthTokenManager;
import com.demo.security.SignChargeGharMain;
import com.demo.message.ReceiveUpload;
import com.demo.message.Powerbank;
import com.demo.message.Pinboard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Connector for ChargeGhar Main Django API
 * Handles authentication, data synchronization, and signature generation
 */
@Component
public class ChargeGharConnector {
    
    @Value("${chargeghar.main.baseUrl}")
    private String baseUrl;
    
    @Value("${chargeghar.main.email}")
    private String email;
    
    @Value("${chargeghar.main.password}")
    private String password;
    
    @Value("${chargeghar.main.signatureSecret}")
    private String signatureSecret;
    
    @Value("${chargeghar.main.loginEndpoint:/api/admin/login}")
    private String loginEndpoint;
    
    @Value("${chargeghar.main.stationDataEndpoint:/api/internal/stations/data}")
    private String stationDataEndpoint;
    
    @Value("${chargeghar.main.connectTimeout:10000}")
    private int connectTimeout;
    
    @Value("${chargeghar.main.readTimeout:15000}")
    private int readTimeout;
    
    @Value("${chargeghar.main.maxRetries:3}")
    private int maxRetries;
    
    private SignChargeGharMain signatureUtil;
    private ObjectMapper objectMapper;
    private RequestConfig requestConfig;
    
    @PostConstruct
    public void init() {
        this.signatureUtil = new SignChargeGharMain(signatureSecret);
        this.objectMapper = new ObjectMapper();
        this.requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();
        
        System.out.println("ChargeGharConnector initialized:");
        System.out.println("  Base URL: " + baseUrl);
        System.out.println("  Login Email: " + email);
        System.out.println("  Connect Timeout: " + connectTimeout + "ms");
        System.out.println("  Read Timeout: " + readTimeout + "ms");
    }
    
    /**
     * Authenticate with Django API and obtain JWT tokens
     * @return true if authentication successful, false otherwise
     */
    public boolean connectChargeGharMain() {
        System.out.println("========================================");
        System.out.println("AUTHENTICATING WITH CHARGEGHAR MAIN API");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String loginUrl = baseUrl + loginEndpoint;
            HttpPost httpPost = new HttpPost(loginUrl);
            httpPost.setConfig(requestConfig);
            
            // Build JSON request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("email", email);
            requestBody.put("password", password);
            
            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "application/json");
            
            System.out.println("Login URL: " + loginUrl);
            System.out.println("Login Email: " + email);
            
            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            System.out.println("Response Status: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            
            if (statusCode == 200) {
                // Parse response and extract tokens
                ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);
                
                if (jsonResponse.has("data")) {
                    ObjectNode data = (ObjectNode) jsonResponse.get("data");
                    String accessToken = data.get("access_token").asText();
                    String refreshToken = data.get("refresh_token").asText();
                    
                    // Store tokens in singleton
                    AuthTokenManager tokenManager = AuthTokenManager.getInstance();
                    tokenManager.setAccessToken(accessToken);
                    tokenManager.setRefreshToken(refreshToken);
                    
                    System.out.println("✅ Authentication successful!");
                    System.out.println("Access Token: " + accessToken.substring(0, 20) + "...");
                    System.out.println("========================================");
                    return true;
                } else {
                    System.err.println("❌ Response missing 'data' field");
                    return false;
                }
            } else if (statusCode == 401) {
                System.err.println("❌ Authentication failed: Invalid credentials");
                return false;
            } else {
                System.err.println("❌ Authentication failed: HTTP " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Authentication error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send device data to Django API (full station sync)
     * Called after device uploads data via HTTP or MQTT
     * 
     * @param rentboxSN Station serial number
     * @param receiveUpload Parsed device data
     * @param signal Signal strength
     * @param ssid WiFi SSID
     * @return true if sync successful
     */
    public boolean sendDeviceData(String rentboxSN, ReceiveUpload receiveUpload, 
                                   String signal, String ssid) {
        System.out.println("========================================");
        System.out.println("SENDING DEVICE DATA TO CHARGEGHAR MAIN");
        System.out.println("Station SN: " + rentboxSN);
        
        // Ensure authentication
        if (!ensureAuthenticated()) {
            System.err.println("❌ Failed to authenticate");
            return false;
        }
        
        // Build JSON payload
        try {
            ObjectNode payload = buildDeviceDataPayload(rentboxSN, receiveUpload, signal, ssid);
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            System.out.println("Payload: " + jsonPayload);
            
            // Send with retry logic
            return sendWithRetry(stationDataEndpoint, jsonPayload);
            
        } catch (Exception e) {
            System.err.println("❌ Error building device data payload: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send powerbank return event to Django API
     * Called when device detects powerbank insertion
     * 
     * @param rentboxSN Station serial number
     * @param powerbankSN PowerBank serial number
     * @param slotNumber Slot number where powerbank was returned
     * @param batteryLevel Battery level at return
     * @return true if notification successful
     */
    public boolean sendReturnedData(String rentboxSN, String powerbankSN, 
                                     int slotNumber, int batteryLevel) {
        System.out.println("========================================");
        System.out.println("SENDING RETURN EVENT TO CHARGEGHAR MAIN");
        System.out.println("Station SN: " + rentboxSN);
        System.out.println("PowerBank SN: " + powerbankSN);
        System.out.println("Slot: " + slotNumber);
        
        // Ensure authentication
        if (!ensureAuthenticated()) {
            System.err.println("❌ Failed to authenticate");
            return false;
        }
        
        // Build JSON payload
        try {
            ObjectNode payload = buildReturnEventPayload(rentboxSN, powerbankSN, 
                                                          slotNumber, batteryLevel);
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            System.out.println("Payload: " + jsonPayload);
            
            // Send with retry logic
            return sendWithRetry(stationDataEndpoint, jsonPayload);
            
        } catch (Exception e) {
            System.err.println("❌ Error building return event payload: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ensure we have valid authentication token
     * Auto-refresh if expired
     */
    private boolean ensureAuthenticated() {
        AuthTokenManager tokenManager = AuthTokenManager.getInstance();
        
        if (tokenManager.getAccessToken() == null || tokenManager.isTokenExpired()) {
            System.out.println("Token expired or missing, re-authenticating...");
            return connectChargeGharMain();
        }
        
        return true;
    }
    
    /**
     * Send HTTP POST request with retry logic
     */
    private boolean sendWithRetry(String endpoint, String jsonPayload) {
        int attempts = 0;
        
        while (attempts < maxRetries) {
            attempts++;
            System.out.println("Attempt " + attempts + "/" + maxRetries);
            
            try {
                boolean success = sendHttpPost(endpoint, jsonPayload);
                if (success) {
                    System.out.println("✅ Data sent successfully");
                    System.out.println("========================================");
                    return true;
                }
                
                // If 401, try to re-authenticate once
                // This is handled in sendHttpPost by checking status code
                
            } catch (Exception e) {
                System.err.println("Attempt " + attempts + " failed: " + e.getMessage());
            }
            
            // Wait before retry (exponential backoff)
            if (attempts < maxRetries) {
                try {
                    int waitTime = (int) Math.pow(2, attempts) * 1000;  // 2s, 4s, 8s
                    System.out.println("Waiting " + (waitTime/1000) + "s before retry...");
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        System.err.println("❌ Failed after " + maxRetries + " attempts");
        System.out.println("========================================");
        return false;
    }
    
    /**
     * Send HTTP POST request with signature
     */
    private boolean sendHttpPost(String endpoint, String jsonPayload) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = baseUrl + endpoint;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            
            // Get current timestamp
            long timestamp = SignChargeGharMain.getCurrentTimestamp();
            
            // Generate signature
            String signature = signatureUtil.generateSignature(jsonPayload, timestamp);
            
            // Set headers
            String accessToken = AuthTokenManager.getInstance().getAccessToken();
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader("X-Signature", signature);
            httpPost.setHeader("X-Timestamp", String.valueOf(timestamp));
            
            // Set body
            httpPost.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
            
            System.out.println("POST URL: " + url);
            System.out.println("Signature: " + signature);
            System.out.println("Timestamp: " + timestamp);
            
            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            System.out.println("Response Status: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            
            if (statusCode == 200) {
                return true;
            } else if (statusCode == 401) {
                System.err.println("Token expired, re-authenticating...");
                if (connectChargeGharMain()) {
                    // Retry this request once with new token
                    return sendHttpPost(endpoint, jsonPayload);
                }
                return false;
            } else if (statusCode == 403) {
                System.err.println("❌ Signature validation failed!");
                return false;
            } else {
                System.err.println("❌ HTTP Error: " + statusCode);
                return false;
            }
        }
    }
    
    /**
     * Build JSON payload for device data sync
     */
    private ObjectNode buildDeviceDataPayload(String rentboxSN, ReceiveUpload receiveUpload,
                                               String signal, String ssid) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "full");
        root.put("timestamp", SignChargeGharMain.getCurrentTimestamp());
        
        // Device info
        ObjectNode device = objectMapper.createObjectNode();
        device.put("serial_number", rentboxSN);
        device.put("imei", rentboxSN);
        device.put("signal_strength", signal != null ? signal : "0");
        device.put("wifi_ssid", ssid != null ? ssid : "");
        device.put("last_heartbeat", getCurrentISOTimestamp());
        device.put("status", "ONLINE");
        
        ObjectNode hardwareInfo = objectMapper.createObjectNode();
        hardwareInfo.put("firmware_version", "2.1.5");  // Could extract from device if available
        hardwareInfo.put("protocol_version", "0xA8");
        device.set("hardware_info", hardwareInfo);
        
        root.set("device", device);
        
        // Station info
        ObjectNode station = objectMapper.createObjectNode();
        station.put("serial_number", rentboxSN);
        station.put("total_slots", receiveUpload.getPowerbanks().size());
        
        // Pinboards array
        ArrayNode pinboardsArray = objectMapper.createArrayNode();
        for (Pinboard pinboard : receiveUpload.getPinboards()) {
            ObjectNode pinboardNode = objectMapper.createObjectNode();
            pinboardNode.put("index", pinboard.getIndex());
            pinboardNode.put("io", pinboard.getIo());
            pinboardNode.put("temperature", pinboard.getTemp());
            pinboardNode.put("soft_version", pinboard.getSoftVersion());
            pinboardNode.put("hard_version", pinboard.getHardVersion());
            pinboardsArray.add(pinboardNode);
        }
        station.set("pinboards", pinboardsArray);
        root.set("station", station);
        
        // Slots array
        ArrayNode slotsArray = objectMapper.createArrayNode();
        for (Powerbank pb : receiveUpload.getPowerbanks()) {
            ObjectNode slotNode = objectMapper.createObjectNode();
            slotNode.put("slot_number", pb.getIndex());
            slotNode.put("status", mapPowerbankStatus(pb.getStatus()));
            slotNode.put("battery_level", pb.getPower());
            
            if (pb.getSnAsInt() > 0) {
                slotNode.put("power_bank_serial", pb.getSnAsString());
            }
            
            ObjectNode slotMetadata = objectMapper.createObjectNode();
            slotMetadata.put("micro_switch", pb.getMicroSwitch());
            slotMetadata.put("solenoid_valve", pb.getSolenoidValveSwitch());
            slotMetadata.put("lock_count", pb.getLockCount());
            slotMetadata.put("last_updated", getCurrentISOTimestamp());
            
            if (pb.getStatus() > 0x01) {
                slotMetadata.put("error_code", "0x0" + Integer.toHexString(pb.getStatus()));
                slotMetadata.put("error_message", pb.getMessage());
            }
            
            slotNode.set("slot_metadata", slotMetadata);
            slotsArray.add(slotNode);
        }
        root.set("slots", slotsArray);
        
        // PowerBanks array (only occupied slots)
        ArrayNode powerbanksArray = objectMapper.createArrayNode();
        for (Powerbank pb : receiveUpload.getPowerbanks()) {
            if (pb.getSnAsInt() > 0) {  // Has powerbank
                ObjectNode pbNode = objectMapper.createObjectNode();
                pbNode.put("serial_number", pb.getSnAsString());
                pbNode.put("status", mapPowerbankStatusForPowerBank(pb.getStatus()));
                pbNode.put("battery_level", pb.getPower());
                pbNode.put("current_slot", pb.getIndex());
                
                ObjectNode pbHardwareInfo = objectMapper.createObjectNode();
                pbHardwareInfo.put("temperature", pb.getTemp());
                pbHardwareInfo.put("voltage", pb.getVoltage());
                pbHardwareInfo.put("current", pb.getCurrent());
                pbHardwareInfo.put("soft_version", pb.getSoftVersion());
                pbHardwareInfo.put("hard_version", pb.getHardVersion());
                pbHardwareInfo.put("micro_switch", pb.getMicroSwitch());
                pbHardwareInfo.put("solenoid_valve", pb.getSolenoidValveSwitch());
                pbHardwareInfo.put("area_code", pb.getArea());
                
                pbNode.set("hardware_info", pbHardwareInfo);
                powerbanksArray.add(pbNode);
            }
        }
        root.set("power_banks", powerbanksArray);
        
        return root;
    }
    
    /**
     * Build JSON payload for return event
     */
    private ObjectNode buildReturnEventPayload(String rentboxSN, String powerbankSN,
                                                int slotNumber, int batteryLevel) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "returned");
        root.put("timestamp", SignChargeGharMain.getCurrentTimestamp());
        
        // Device info
        ObjectNode device = objectMapper.createObjectNode();
        device.put("serial_number", rentboxSN);
        device.put("last_heartbeat", getCurrentISOTimestamp());
        root.set("device", device);
        
        // Return event
        ObjectNode returnEvent = objectMapper.createObjectNode();
        returnEvent.put("power_bank_serial", powerbankSN);
        returnEvent.put("slot_number", slotNumber);
        returnEvent.put("battery_level", batteryLevel);
        returnEvent.put("returned_at", getCurrentISOTimestamp());
        returnEvent.put("condition", "NORMAL");
        
        // Hardware info at return (would need actual values from device)
        ObjectNode hardwareInfo = objectMapper.createObjectNode();
        hardwareInfo.put("temperature", 28);  // Placeholder
        hardwareInfo.put("voltage", 4800);    // Placeholder
        hardwareInfo.put("current", 0);       // Placeholder
        returnEvent.set("hardware_info", hardwareInfo);
        
        root.set("return_event", returnEvent);
        
        return root;
    }
    
    /**
     * Map device status code to slot status string
     */
    private String mapPowerbankStatus(int status) {
        switch (status) {
            case 0x00: return "AVAILABLE";
            case 0x01: return "OCCUPIED";
            case 0x02:
            case 0x04:
            case 0x05:
            case 0x06: return "ERROR";
            default: return "AVAILABLE";
        }
    }
    
    /**
     * Map device status code to powerbank status string
     */
    private String mapPowerbankStatusForPowerBank(int status) {
        switch (status) {
            case 0x01: return "AVAILABLE";
            case 0x02:
            case 0x04: return "MAINTENANCE";
            case 0x05:
            case 0x06: return "DAMAGED";
            default: return "AVAILABLE";
        }
    }
    
    /**
     * Get current timestamp in ISO 8601 format
     */
    private String getCurrentISOTimestamp() {
        return Instant.now()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }
}
```

---

## 🔧 4. Update ApiController

**File**: `src/main/java/com.demo/controller/ApiController.java`

**Add autowired connector**:
```java
@Autowired
ChargeGharConnector chargeGharConnector;
```

**Modify `/api/rentbox/upload/data` endpoint (around line 245)**:
```java
// After successful parsing and Redis updates
System.out.println("========================================");

// ✅ NEW: Send data to ChargeGhar Main Django API
try {
    boolean syncSuccess = chargeGharConnector.sendDeviceData(
        rentboxSN, 
        receiveUpload, 
        signal, 
        ssid
    );
    
    if (syncSuccess) {
        System.out.println("✅ Data synced to ChargeGhar Main successfully");
    } else {
        System.err.println("⚠️ Failed to sync data to ChargeGhar Main (will retry on next upload)");
    }
} catch (Exception e) {
    System.err.println("⚠️ Error syncing to ChargeGhar Main: " + e.getMessage());
    // Don't fail the entire request, just log the error
}

httpResult.setCode(HttpStatus.SC_OK);
httpResult.setMsg("ok");
```

**Modify `/api/rentbox/order/return` endpoint**:
```java
// After detecting powerbank return
System.out.println("========================================");
System.out.println("POWERBANK RETURN DETECTED");

// ✅ NEW: Notify ChargeGhar Main of return event
try {
    boolean syncSuccess = chargeGharConnector.sendReturnedData(
        rentboxSN,
        powerbankSN,
        slotNumber,
        batteryLevel
    );
    
    if (syncSuccess) {
        System.out.println("✅ Return event sent to ChargeGhar Main successfully");
    } else {
        System.err.println("⚠️ Failed to notify ChargeGhar Main of return");
    }
} catch (Exception e) {
    System.err.println("⚠️ Error notifying ChargeGhar Main: " + e.getMessage());
}

httpResult.setCode(HttpStatus.SC_OK);
httpResult.setMsg("return processed");
```

---

## ⚙️ 5. Configuration Properties

**File**: `src/main/resources/config.properties`

**Add these properties**:
```properties
#------------------------- ChargeGhar Main Django API ----------------------------
# Base URL for Django application
chargeghar.main.baseUrl=https://main.chargeghar.com

# Admin credentials for API authentication
chargeghar.main.email=janak@powerbank.com
chargeghar.main.password=password123

# Shared secret for request signature (KEEP SECRET!)
chargeghar.main.signatureSecret=your-strong-secret-key-min-32-chars-here-change-me

# API endpoints
chargeghar.main.loginEndpoint=/api/admin/login
chargeghar.main.stationDataEndpoint=/api/internal/stations/data

# HTTP client timeouts (milliseconds)
chargeghar.main.connectTimeout=10000
chargeghar.main.readTimeout=15000

# Retry configuration
chargeghar.main.maxRetries=3
```

---

## 🏗️ 6. Maven Dependencies

**File**: `pom.xml`

**Add these dependencies if not already present**:
```xml
<!-- Jackson for JSON processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<!-- Apache HttpClient for HTTP requests -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.14</version>
</dependency>

<!-- Apache Commons Codec for Base64 -->
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.15</version>
</dependency>
```

---

## 🧪 7. Testing Code

**File**: `src/test/java/com/demo/connector/ChargeGharConnectorTest.java`

```java
package com.demo.connector;

import com.demo.security.SignChargeGharMain;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChargeGharConnectorTest {
    
    @Test
    public void testSignatureGeneration() throws Exception {
        String secretKey = "test-secret-key";
        SignChargeGharMain signUtil = new SignChargeGharMain(secretKey);
        
        String payload = "{\"test\":\"data\"}";
        long timestamp = 1698345600;
        
        String signature1 = signUtil.generateSignature(payload, timestamp);
        String signature2 = signUtil.generateSignature(payload, timestamp);
        
        // Same input should produce same signature
        assertEquals(signature1, signature2);
        
        // Signature should not be empty
        assertNotNull(signature1);
        assertTrue(signature1.length() > 0);
        
        System.out.println("Generated signature: " + signature1);
    }
    
    @Test
    public void testSignatureValidation() throws Exception {
        String secretKey = "test-secret-key";
        SignChargeGharMain signUtil = new SignChargeGharMain(secretKey);
        
        String payload = "{\"test\":\"data\"}";
        long timestamp = 1698345600;
        
        String signature = signUtil.generateSignature(payload, timestamp);
        
        // Valid signature should pass
        assertTrue(signUtil.validateSignature(payload, timestamp, signature));
        
        // Invalid signature should fail
        assertFalse(signUtil.validateSignature(payload, timestamp, "invalid-signature"));
        
        // Different payload should fail
        assertFalse(signUtil.validateSignature("{\"different\":\"data\"}", timestamp, signature));
    }
}
```

---

## 📝 8. Logging Configuration

**File**: `logging.properties`

**Add logger for connector**:
```properties
# ChargeGhar Connector logging
com.demo.connector.level = INFO
com.demo.security.level = INFO
```

---

## 🚀 9. Deployment Steps

### Local Testing
```bash
# 1. Update config.properties with test credentials
vim src/main/resources/config.properties

# 2. Build project
mvn clean package -DskipTests

# 3. Run with Docker
docker-compose down
docker-compose up -d --build

# 4. Monitor logs
docker logs -f iotdemo-app

# 5. Trigger device upload
curl -X POST "http://localhost:8080/api/rentbox/upload/data?rentboxSN=864601069946994&sign=test&signal=85" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @test_data.bin

# 6. Check for sync messages in logs
docker logs iotdemo-app | grep "CHARGEGHAR MAIN"
```

### Production Deployment
```bash
# 1. SSH to production server
ssh user@api.chargeghar.com

# 2. Navigate to project directory
cd /path/to/ChargeGhar_Devices

# 3. Pull latest code
git pull origin main

# 4. Update production config
vim src/main/resources/config.properties
# Set production baseUrl, credentials, and secret

# 5. Rebuild Docker container
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d --build

# 6. Verify service is running
docker-compose -f docker-compose.prod.yml ps

# 7. Monitor logs for first sync
docker-compose -f docker-compose.prod.yml logs -f iotdemo-app | grep "CHARGEGHAR"
```

---

## 🔍 10. Monitoring & Debugging

### Check Authentication Status
```bash
# Look for authentication messages
docker logs iotdemo-app | grep "AUTHENTICATING WITH CHARGEGHAR"
docker logs iotdemo-app | grep "Authentication successful"
```

### Check Data Sync
```bash
# Look for data sync messages
docker logs iotdemo-app | grep "SENDING DEVICE DATA"
docker logs iotdemo-app | grep "Data sent successfully"
```

### Check Signature Generation
```bash
# Look for signature in logs
docker logs iotdemo-app | grep "Signature:"
```

### Check Errors
```bash
# Filter for errors
docker logs iotdemo-app | grep "❌"
docker logs iotdemo-app | grep "ERROR"
```

### Manual Test Authentication
```bash
# Test from Java container
docker exec -it iotdemo-app bash
curl -X POST "https://main.chargeghar.com/api/admin/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"janak@powerbank.com","password":"password123"}'
```

---

## 📚 Related Files
- **Plan**: `con_plan.md` - System architecture
- **Python Code**: `con_code_python.md` - Django implementation
- **API Specs**: `con_req_res.md` - Request/response formats
