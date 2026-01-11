package com.demo.common;

import com.demo.tools.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Transaction Logger - Redis-based logging for device transactions
 * 
 * Stores parsed device responses for audit trail and Django integration.
 * Each device maintains last 50 transactions with 30-minute TTL.
 */
@Component
public class TransactionLogger {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final int MAX_LOGS_PER_DEVICE = 50;
    private static final int LOG_TTL_MINUTES = 30;
    
    /**
     * Log a transaction with parsed data
     * 
     * @param deviceName Device serial number
     * @param messageId  Unique message ID (UUID)
     * @param cmd        Command code (e.g., "0x10", "0x31")
     * @param rawHex     Raw hex string of the response
     * @param parsedData Parsed data object (will be converted to Map/List)
     */
    public void log(String deviceName, String messageId, String cmd, 
                    String rawHex, Object parsedData) {
        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("messageId", messageId);
            logEntry.put("deviceName", deviceName);
            logEntry.put("cmd", cmd);
            logEntry.put("raw", rawHex);
            logEntry.put("parsed", parsedData);
            logEntry.put("timestamp", System.currentTimeMillis() / 1000);
            
            // Store individual transaction for direct lookup
            String txnKey = "txn:" + deviceName + ":" + messageId;
            redisTemplate.opsForValue().set(txnKey, logEntry, LOG_TTL_MINUTES, TimeUnit.MINUTES);
            
            // Add to device's log list (newest first)
            String listKey = "txn_list:" + deviceName;
            redisTemplate.opsForList().leftPush(listKey, logEntry);
            redisTemplate.opsForList().trim(listKey, 0, MAX_LOGS_PER_DEVICE - 1);
            redisTemplate.expire(listKey, LOG_TTL_MINUTES, TimeUnit.MINUTES);
            
            System.out.println("📝 Transaction logged: device=" + deviceName + ", cmd=" + cmd + ", messageId=" + messageId);
        } catch (Exception e) {
            System.err.println("❌ Failed to log transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get transaction by messageId
     * 
     * @param deviceName Device serial number
     * @param messageId  Message ID to look up
     * @return Transaction map or null if not found
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTransaction(String deviceName, String messageId) {
        try {
            String txnKey = "txn:" + deviceName + ":" + messageId;
            Object result = redisTemplate.opsForValue().get(txnKey);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to get transaction: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get device logs with optional CMD filter
     * 
     * @param deviceName Device serial number
     * @param limit      Max number of logs to return
     * @param cmdFilter  Optional CMD filter (e.g., "0x31")
     * @return List of transaction maps
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDeviceLogs(String deviceName, int limit, String cmdFilter) {
        try {
            String listKey = "txn_list:" + deviceName;
            
            // Get more than requested if filtering
            int fetchLimit = cmdFilter != null ? Math.min(limit * 3, MAX_LOGS_PER_DEVICE) : limit;
            List<Object> logs = redisTemplate.opsForList().range(listKey, 0, fetchLimit - 1);
            
            if (logs == null || logs.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object log : logs) {
                if (log instanceof Map) {
                    Map<String, Object> entry = (Map<String, Object>) log;
                    if (cmdFilter == null || cmdFilter.equals(entry.get("cmd"))) {
                        result.add(entry);
                        if (result.size() >= limit) {
                            break;
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("❌ Failed to get device logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all device logs without filtering
     */
    public List<Map<String, Object>> getDeviceLogs(String deviceName, int limit) {
        return getDeviceLogs(deviceName, limit, null);
    }
}
