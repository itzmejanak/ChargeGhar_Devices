package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import com.demo.message.Powerbank;
import com.demo.message.ReceiveUpload;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for 0x10 (CHECK) command response
 * 
 * Device sends slot/powerbank status in response to:
 * - {"cmd":"check"} - occupied slots only
 * - {"cmd":"check_all"} - all slots
 * 
 * Response contains array of powerbank info (SN, power, status, etc.)
 */
@Component
public class CheckCommandHandler extends BaseCommandHandler {
    
    public static final int CMD_CODE = 0x10;
    public static final String CMD_NAME = "CHECK";
    public static final String REDIS_KEY_PREFIX = "check:";
    
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
        
        // Parse and log transaction
        try {
            ReceiveUpload upload = new ReceiveUpload(bytes);
            List<Map<String, Object>> powerbanksData = parsePowerbanks(upload);
            logTransaction(messageBody, rawHex, powerbanksData, transactionLogger);
        } catch (Exception e) {
            System.err.println("❌ Failed to parse " + CMD_NAME + ": " + e.getMessage());
        }
    }
    
    /**
     * Convert Powerbank objects to serializable Maps
     */
    private List<Map<String, Object>> parsePowerbanks(ReceiveUpload upload) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Object pb : upload.getPowerbanks()) {
            if (pb instanceof Powerbank) {
                Powerbank powerbank = (Powerbank) pb;
                Map<String, Object> pbMap = new HashMap<>();
                pbMap.put("index", powerbank.getIndex());
                pbMap.put("status", powerbank.getStatus());
                pbMap.put("power", powerbank.getPower());
                pbMap.put("snAsString", powerbank.getSnAsString());
                pbMap.put("snAsInt", powerbank.getSnAsInt());
                pbMap.put("message", powerbank.getMessage());
                result.add(pbMap);
            }
        }
        
        return result;
    }
}
