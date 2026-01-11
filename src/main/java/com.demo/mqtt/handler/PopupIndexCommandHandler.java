package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for 0x21 (POPUP_INDEX) command response
 * 
 * Device sends this in response to:
 * - {"cmd":"popup","data":"<slot_index>","io":"0"}
 * 
 * Response format: [0xA8][len][len][0x21][slot][...][status][checksum]
 * - slot at byte index 4
 * - status at byte index 7 (0x01 = success, 0x02 = error)
 */
@Component
public class PopupIndexCommandHandler extends BaseCommandHandler {
    
    public static final int CMD_CODE = 0x21;
    public static final String CMD_NAME = "POPUP_INDEX";
    public static final String REDIS_KEY_PREFIX = "popup_index:";
    
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
        // Cache response (may not have waiting request, but cache anyway if TTL exists)
        cacheResponse(messageBody.getDeviceName(), bytes, redisTemplate);
        
        // Parse and log transaction
        Map<String, Object> parsed = parseResponse(bytes);
        logTransaction(messageBody, rawHex, parsed, transactionLogger);
        
        int status = (int) parsed.getOrDefault("status", 0);
        if (status == 0x01) {
            System.out.println("✅ " + CMD_NAME + " success: slot=" + parsed.get("slot"));
        } else {
            System.out.println("⚠️ " + CMD_NAME + " failed: status=0x" + Integer.toHexString(status));
        }
    }
    
    /**
     * Parse popup index response
     */
    private Map<String, Object> parseResponse(byte[] bytes) {
        Map<String, Object> parsed = new HashMap<>();
        
        if (bytes.length >= 9) {
            // Format: A8 00 09 21 [slot] [xx] [xx] [status] [checksum]
            int slot = bytes[4] & 0xFF;
            int status = bytes[7] & 0xFF;
            
            parsed.put("slot", slot);
            parsed.put("status", status);
            parsed.put("success", status == 0x01);
        }
        
        return parsed;
    }
}
