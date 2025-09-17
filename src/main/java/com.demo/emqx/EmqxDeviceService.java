package com.demo.emqx;

import com.demo.common.DeviceConfig;
import com.demo.common.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class EmqxDeviceService {
    
    @Autowired
    private EmqxApiClient emqxApiClient;
    
    @Autowired
    private AppConfig appConfig;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    private static final String DEVICE_CREDENTIALS_PREFIX = "device_credentials:";
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 16;
    
    /**
     * Register or get existing device configuration
     */
    public DeviceConfig getOrCreateDeviceConfig(String deviceName) throws Exception {
        // Check if EMQX API connection is validated
        if (!emqxApiClient.isConnectionValidated()) {
            System.err.println("EMQX API connection not validated. Attempting to validate...");
            emqxApiClient.validateConnection();
            if (!emqxApiClient.isConnectionValidated()) {
                throw new Exception("EMQX API connection not available. Please check configuration and network connectivity.");
            }
        }
        // Check if device credentials exist in cache
        String credentialsKey = DEVICE_CREDENTIALS_PREFIX + deviceName;
        BoundValueOperations credentialsOps = redisTemplate.boundValueOps(credentialsKey);
        DeviceCredentials credentials = (DeviceCredentials) credentialsOps.get();
        
        if (credentials == null) {
            // Create new device credentials
            credentials = createDeviceCredentials(deviceName);
            
            // Register device with EMQX platform
            boolean registered = emqxApiClient.registerDevice(credentials.getUsername(), credentials.getPassword());
            if (!registered) {
                throw new Exception("Failed to register device with EMQX platform: " + deviceName);
            }
            
            // Cache credentials for 7 days
            credentialsOps.set(credentials, 7, TimeUnit.DAYS);
            System.out.println("New device registered: " + deviceName);
        } else {
            // Verify device still exists in EMQX
            if (!emqxApiClient.deviceExists(credentials.getUsername())) {
                // Re-register device if it doesn't exist
                boolean registered = emqxApiClient.registerDevice(credentials.getUsername(), credentials.getPassword());
                if (!registered) {
                    throw new Exception("Failed to re-register device with EMQX platform: " + deviceName);
                }
                System.out.println("Device re-registered: " + deviceName);
            }
        }
        
        // Create DeviceConfig for compatibility with existing code
        return createDeviceConfig(deviceName, credentials);
    }
    
    /**
     * Create new device credentials
     */
    private DeviceCredentials createDeviceCredentials(String deviceName) {
        DeviceCredentials credentials = new DeviceCredentials();
        credentials.setDeviceName(deviceName);
        credentials.setUsername("device_" + deviceName);
        credentials.setPassword(generateSecurePassword());
        credentials.setCreatedTime(new Date());
        return credentials;
    }
    
    /**
     * Generate secure random password
     */
    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Create DeviceConfig from credentials
     */
    private DeviceConfig createDeviceConfig(String deviceName, DeviceCredentials credentials) {
        DeviceConfig config = new DeviceConfig();
        config.setDeviceName(deviceName);
        config.setProductKey(appConfig.getProductKey());
        config.setHost(appConfig.getMqttBroker());
        config.setPort(appConfig.getMqttPort());
        config.setCreatedTime(credentials.getCreatedTime());
        config.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        
        // EMQX specific configuration
        config.setIotId(credentials.getUsername());
        config.setIotToken(credentials.getPassword());
        config.setDeviceSecret(""); // Not used in EMQX
        
        return config;
    }
    
    /**
     * Rotate device password
     */
    public boolean rotateDevicePassword(String deviceName) throws Exception {
        String credentialsKey = DEVICE_CREDENTIALS_PREFIX + deviceName;
        BoundValueOperations credentialsOps = redisTemplate.boundValueOps(credentialsKey);
        DeviceCredentials credentials = (DeviceCredentials) credentialsOps.get();
        
        if (credentials == null) {
            throw new Exception("Device credentials not found: " + deviceName);
        }
        
        // Generate new password
        String newPassword = generateSecurePassword();
        
        // Update password in EMQX
        boolean updated = emqxApiClient.updateDevicePassword(credentials.getUsername(), newPassword);
        if (!updated) {
            throw new Exception("Failed to update device password in EMQX: " + deviceName);
        }
        
        // Update cached credentials
        credentials.setPassword(newPassword);
        credentialsOps.set(credentials, 7, TimeUnit.DAYS);
        
        System.out.println("Device password rotated: " + deviceName);
        return true;
    }
    
    /**
     * Remove device from EMQX platform
     */
    public boolean removeDevice(String deviceName) throws Exception {
        String credentialsKey = DEVICE_CREDENTIALS_PREFIX + deviceName;
        BoundValueOperations credentialsOps = redisTemplate.boundValueOps(credentialsKey);
        DeviceCredentials credentials = (DeviceCredentials) credentialsOps.get();
        
        if (credentials != null) {
            // Delete from EMQX platform
            boolean deleted = emqxApiClient.deleteDevice(credentials.getUsername());
            if (deleted) {
                // Remove from cache
                credentialsOps.expire(-1, TimeUnit.SECONDS);
                System.out.println("Device removed: " + deviceName);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get device credentials for debugging
     */
    public DeviceCredentials getDeviceCredentials(String deviceName) {
        String credentialsKey = DEVICE_CREDENTIALS_PREFIX + deviceName;
        BoundValueOperations credentialsOps = redisTemplate.boundValueOps(credentialsKey);
        return (DeviceCredentials) credentialsOps.get();
    }
}