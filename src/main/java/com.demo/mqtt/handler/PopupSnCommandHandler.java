package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import com.demo.message.ReceivePopupSN;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for 0x31 (POPUP_SN) command response
 * 
 * Device sends this in response to:
 * - {"cmd":"popup_sn","data":"<powerbank_sn>"}
 * 
 * Response format: [0xA8][len][len][0x31][slot][SN_4bytes][status][checksum]
 * - slot at byte index 4
 * - SN at bytes 5-8 (4 bytes, little-endian integer)
 * - status at byte index 9 (0x01 = success, 0x02 = error)
 */
@Component
public class PopupSnCommandHandler extends BaseCommandHandler {
    
    public static final int CMD_CODE = 0x31;
    public static final String CMD_NAME = "POPUP_SN";
    public static final String REDIS_KEY_PREFIX = "popup_sn:";
    
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
            ReceivePopupSN popup = new ReceivePopupSN(bytes);
            Map<String, Object> parsed = new HashMap<>();
            parsed.put("slot", popup.getPinboardIndex());
            parsed.put("powerbankSN", popup.getSnAsString());
            parsed.put("status", popup.getStatus());
            parsed.put("success", popup.getStatus() == 0x01);
            
            logTransaction(messageBody, rawHex, parsed, transactionLogger);
            
            if (popup.getStatus() == 0x01) {
                System.out.println("✅ " + CMD_NAME + " success: powerbank=" + popup.getSnAsString() + ", slot=" + popup.getPinboardIndex());
            } else {
                System.out.println("⚠️ " + CMD_NAME + " failed: status=0x" + Integer.toHexString(popup.getStatus()));
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to parse " + CMD_NAME + ": " + e.getMessage());
        }
    }
}
