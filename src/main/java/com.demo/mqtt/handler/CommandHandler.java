package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Interface for handling device command responses
 * 
 * Each command (0x10, 0x21, 0x31, etc.) has its own handler implementation.
 * This enables:
 * - Easy addition of new commands
 * - Single responsibility per handler
 * - Testable individual handlers
 * - No modification to MqttSubscriber when adding commands
 */
public interface CommandHandler {
    
    /**
     * Get the command code this handler processes
     * @return Command code (e.g., 0x10, 0x31)
     */
    int getCommandCode();
    
    /**
     * Get human-readable command name for logging
     * @return Command name (e.g., "CHECK", "POPUP_SN")
     */
    String getCommandName();
    
    /**
     * Get the Redis key prefix for caching response
     * @return Key prefix (e.g., "check:", "popup_sn:") or null if no caching
     */
    String getRedisKeyPrefix();
    
    /**
     * Handle the command response
     * 
     * @param messageBody The MQTT message
     * @param bytes Raw payload bytes
     * @param rawHex Hex string representation
     * @param redisTemplate Redis template for caching
     * @param transactionLogger Logger for transaction audit trail
     */
    void handle(
        MessageBody messageBody,
        byte[] bytes,
        String rawHex,
        RedisTemplate redisTemplate,
        TransactionLogger transactionLogger
    );
}
