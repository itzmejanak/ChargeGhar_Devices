package com.demo.helper;

import com.demo.connector.ChargeGharConnector;
import com.demo.message.ReceiveUpload;
import com.demo.message.Pinboard;
import com.demo.message.Powerbank;
import com.demo.common.DeviceConfig;
import com.demo.model.Device;
import com.demo.emqx.EmqxApiClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for API Controller
 * Handles logging and ChargeGhar Main integration
 */
@Component
public class ControllerHelper {
    
    @Autowired
    private ChargeGharConnector chargeGharConnector;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private EmqxApiClient emqxApiClient;
    
    /**
     * Sync device upload data to ChargeGhar Main Django app
     * Called when hardware uploads data every 20 minutes
     * 
     * @param rentboxSN Device serial number
     * @param receiveUpload Parsed device data
     * @param signal WiFi signal strength
     * @param ssid WiFi network name
     */
    public void syncDeviceUploadToMain(String rentboxSN, ReceiveUpload receiveUpload, String signal, String ssid) {
        try {
            boolean syncSuccess = chargeGharConnector.sendDeviceData(rentboxSN, receiveUpload, signal, ssid);
            if (syncSuccess) {
                System.out.println("✅ Data synced to ChargeGhar Main successfully");
            } else {
                System.err.println("⚠️ Failed to sync data to ChargeGhar Main (will retry on next upload)");
            }
        } catch (Exception syncException) {
            System.err.println("⚠️ Error syncing to ChargeGhar Main: " + syncException.getMessage());
            syncException.printStackTrace();
            // Don't fail the request - continue processing
        }
    }
    
    /**
     * Sync powerbank return event to ChargeGhar Main Django app
     * Called when a powerbank is returned to station
     * 
     * @param rentboxSN Station serial number
     * @param powerbankSN PowerBank serial number
     * @param slotNumber Slot number where powerbank was returned
     */
    public void syncReturnEventToMain(String rentboxSN, String powerbankSN, int slotNumber) {
        try {
            int batteryLevel = 0;  // Default if not found
            
            // Get battery level from cached upload data (from hardware's regular 20-min upload)
            // This avoids unnecessary MQTT commands and prevents duplicate data syncs to Django
            // Battery level will be 0-30 minutes old (average ~10-15 min), which is acceptable
            String cacheKey = "upload_data:" + rentboxSN;
            BoundValueOperations cacheOps = redisTemplate.boundValueOps(cacheKey);
            byte[] cachedBytes = (byte[]) cacheOps.get();
            
            if (cachedBytes != null) {
                try {
                    ReceiveUpload cachedData = new ReceiveUpload(cachedBytes);
                    // Find the powerbank in the cached data
                    for (Powerbank pb : cachedData.getPowerbanks()) {
                        if (pb.getSnAsString().equals(powerbankSN)) {
                            batteryLevel = pb.getPower();
                            System.out.println("📊 Battery level from last hardware upload: " + batteryLevel + "%");
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Could not parse cached device data: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ No cached upload data found for device: " + rentboxSN);
            }
            
            System.out.println("========================================");
            System.out.println("POWERBANK RETURN DETECTED");
            System.out.println("Station: " + rentboxSN);
            System.out.println("PowerBank: " + powerbankSN);
            System.out.println("Slot: " + slotNumber);
            System.out.println("Battery Level: " + batteryLevel + "%");
            
            boolean syncSuccess = chargeGharConnector.sendReturnedData(
                rentboxSN, 
                powerbankSN, 
                slotNumber, 
                batteryLevel
            );
            
            if (syncSuccess) {
                System.out.println("✅ Return event sent to ChargeGhar Main successfully");
            } else {
                System.err.println("⚠️ Failed to send return event to ChargeGhar Main");
            }
            System.out.println("========================================");
            
        } catch (Exception syncException) {
            System.err.println("⚠️ Error sending return event to ChargeGhar Main: " + syncException.getMessage());
            syncException.printStackTrace();
            // Don't fail the request - continue processing
        }
    }
    
    /**
     * Log device upload data in a readable format
     * 
     * @param rentboxSN Device serial number
     * @param signal WiFi signal strength
     * @param sign Security signature
     * @param io IO status
     * @param ssid WiFi network name
     * @param dataLength Data length in bytes
     * @param hexData Hexadecimal representation of data
     * @param receiveUpload Parsed device data
     */
    public void logDeviceUploadData(String rentboxSN, String signal, String sign, Integer io, 
                                     String ssid, int dataLength, String hexData, 
                                     ReceiveUpload receiveUpload) {
        System.out.println("========================================");
        System.out.println("DEVICE UPLOAD DATA RECEIVED");
        System.out.println("REQUEST PARAMETERS:");
        System.out.println("  rentboxSN: " + rentboxSN);
        System.out.println("  signal: " + signal);
        System.out.println("  sign: " + sign);
        System.out.println("  io: " + (io != null ? io.toString() : "null"));
        System.out.println("  ssid: " + (StringUtils.isNotEmpty(ssid) ? ssid : "null"));
        System.out.println("  data length: " + dataLength + " bytes");
        System.out.println("  hex data: " + hexData);
        
        System.out.println("PARSED DATA:");
        System.out.println("  Pinboard count: " + receiveUpload.getPinboards().size());
        System.out.println("  Powerbank count: " + receiveUpload.getPowerbanks().size());
        
        // Log each pinboard
        for (int i = 0; i < receiveUpload.getPinboards().size(); i++) {
            Pinboard pinboard = receiveUpload.getPinboards().get(i);
            System.out.println("  Pinboard[" + i + "]: index=" + pinboard.getIndex() + ", io=" + pinboard.getIo());
        }
        
        // Log each powerbank
        for (int i = 0; i < receiveUpload.getPowerbanks().size(); i++) {
            Powerbank pb = receiveUpload.getPowerbanks().get(i);
            System.out.println("  Powerbank[" + i + "]: " +
                "index=" + pb.getIndex() + ", " +
                "pinboardIndex=" + pb.getPinboardIndex() + ", " +
                "SN=" + pb.getSnAsString() + ", " +
                "status=" + pb.getStatus() + ", " +
                "power=" + pb.getPower() + "%, " +
                "area=" + pb.getArea() + ", " +
                "temp=" + pb.getTemp() + "°C, " +
                "microSwitch=" + pb.getMicroSwitch() + ", " +
                "solenoidValve=" + pb.getSolenoidValveSwitch());
        }
        System.out.println("========================================");
    }
    
    /**
     * Validate and sync device configuration with EMQX
     * Ensures password synchronization between database and EMQX platform
     * 
     * @param deviceName Device name/serial number
     * @param device Device entity from database
     * @param config DeviceConfig from EMQX
     * @return Validated DeviceConfig with synced password
     */
    public DeviceConfig validateAndSyncDeviceConfig(String deviceName, Device device, DeviceConfig config) {
        // CRITICAL: Ensure password synchronization between database and EMQX
        String dbPassword = device.getPassword();
        String emqxPassword = config.getIotToken();
        
        // If passwords don't match, sync them using database as source of truth
        if (!dbPassword.equals(emqxPassword)) {
            System.out.println("⚠️ Password mismatch for device: " + deviceName);
            System.out.println("   DB: " + dbPassword + " | EMQX: " + emqxPassword);
            
            // Update EMQX password to match database
            try {
                boolean updated = emqxApiClient.updateDevicePassword(config.getIotId(), dbPassword);
                if (updated) {
                    System.out.println("✅ EMQX password updated to match database");
                    config.setIotToken(dbPassword);
                } else {
                    System.err.println("❌ Failed to update EMQX password - device may fail to connect");
                }
            } catch (Exception e) {
                System.err.println("❌ Error updating EMQX password: " + e.getMessage());
            }
        }

        // Cache validated configuration for 1 hour
        String key = "clientConect:" + deviceName;
        BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);
        boundValueOps.set(config, 1, TimeUnit.HOURS);
        
        return config;
    }
}
