package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Base class for command handlers with common functionality
 */
public abstract class BaseCommandHandler implements CommandHandler {
    
    /**
     * Cache response bytes in Redis if key exists and has TTL
     * 
     * @param deviceName Device name for key construction
     * @param bytes Response bytes to cache
     * @param redisTemplate Redis template
     * @return true if cached, false if key expired/not set
     */
    protected boolean cacheResponse(String deviceName, byte[] bytes, RedisTemplate redisTemplate) {
        String keyPrefix = getRedisKeyPrefix();
        if (keyPrefix == null) {
            return false;
        }
        
        String key = keyPrefix + deviceName;
        BoundValueOperations ops = redisTemplate.boundValueOps(key);
        long ttl = ops.getExpire();
        
        if (ttl <= 0) {
            System.out.println("⚠️ " + getCommandName() + " response ignored - Redis key expired or not set");
            return false;
        }
        
        ops.set(bytes, ttl, TimeUnit.SECONDS);
        System.out.println("✅ " + getCommandName() + " response cached for device: " + deviceName);
        return true;
    }
    
    /**
     * Cache response bytes in Redis regardless of existing TTL
     * Used for commands that should always be cached when received
     * 
     * @param deviceName Device name for key construction
     * @param bytes Response bytes to cache
     * @param redisTemplate Redis template
     * @param defaultTtlSeconds Default TTL if key doesn't exist
     */
    protected void cacheResponseAlways(String deviceName, byte[] bytes, RedisTemplate redisTemplate, long defaultTtlSeconds) {
        String keyPrefix = getRedisKeyPrefix();
        if (keyPrefix == null) {
            return;
        }
        
        String key = keyPrefix + deviceName;
        BoundValueOperations ops = redisTemplate.boundValueOps(key);
        long ttl = ops.getExpire();
        
        if (ttl <= 0) {
            ttl = defaultTtlSeconds;
        }
        
        ops.set(bytes, ttl, TimeUnit.SECONDS);
        System.out.println("✅ " + getCommandName() + " response cached for device: " + deviceName);
    }
    
    /**
     * Log transaction to Redis audit trail
     * 
     * @param messageBody Message details
     * @param rawHex Raw hex string
     * @param parsed Parsed data object (must be serializable - use Map/List)
     * @param transactionLogger Transaction logger
     */
    protected void logTransaction(
        MessageBody messageBody,
        String rawHex,
        Object parsed,
        TransactionLogger transactionLogger
    ) {
        try {
            transactionLogger.log(
                messageBody.getDeviceName(),
                messageBody.getMessageId(),
                "0x" + Integer.toHexString(getCommandCode()).toUpperCase(),
                rawHex,
                parsed
            );
        } catch (Exception e) {
            System.err.println("❌ Failed to log transaction for " + getCommandName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Get hex string representation of command code
     */
    protected String getCommandHex() {
        return "0x" + Integer.toHexString(getCommandCode()).toUpperCase();
    }
}
