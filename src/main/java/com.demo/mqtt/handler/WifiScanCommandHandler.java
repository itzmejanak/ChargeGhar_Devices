package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Handler for 0xCF (WIFI_SCAN) command response
 * 
 * Device sends this in response to:
 * - {"cmd":"getWifi"}
 * 
 * Response contains list of available WiFi networks.
 * Parsing is handled by ReceiveWifi class.
 */
@Component
public class WifiScanCommandHandler extends BaseCommandHandler {
    
    public static final int CMD_CODE = 0xCF;
    public static final String CMD_NAME = "WIFI_SCAN";
    public static final String REDIS_KEY_PREFIX = "getwifi:";
    
    @Override
    public int getCommandCode() {
        return CMD_CODE;
    }
    
    @Override
    public String getCommandName() {
        return CMD_NAME;
    }
    
    @Override
    public String getRedisKeyPrefix() {
        return REDIS_KEY_PREFIX;
    }
    
    @Override
    public void handle(
        MessageBody messageBody,
        byte[] bytes,
        String rawHex,
        RedisTemplate redisTemplate,
        TransactionLogger transactionLogger
    ) {
        // Cache response for sync polling
        if (!cacheResponse(messageBody.getDeviceName(), bytes, redisTemplate)) {
            return; // Key not set, no one waiting for response
        }
        
        System.out.println("📶 " + CMD_NAME + " result cached for device: " + messageBody.getDeviceName());
        
        // Log transaction (raw only - WiFi parsing is complex and done by caller)
        logTransaction(messageBody, rawHex, null, transactionLogger);
    }
}
