package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import com.demo.tools.ByteUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for 0x40 (RETURN) command response
 * 
 * Device sends this automatically when a powerbank is returned (inserted).
 * This is an unsolicited message - not a response to a command.
 * 
 * Response format: [0xA8][len][len][0x40][room][slot][SN_4bytes][status][...][power][checksum]
 * - room at byte index 4
 * - slot at byte index 5
 * - SN at bytes 6-9 (4 bytes, little-endian integer)
 * - power at byte index 13
 */
@Component
public class ReturnCommandHandler extends BaseCommandHandler {
    
    public static final int CMD_CODE = 0x40;
    public static final String CMD_NAME = "RETURN";
    
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
        return null; // Return events are not cached for polling
    }
    
    @Override
    public void handle(
        MessageBody messageBody,
        byte[] bytes,
        String rawHex,
        RedisTemplate redisTemplate,
        TransactionLogger transactionLogger
    ) {
        // Parse return event
        Map<String, Object> parsed = parseResponse(bytes);
        
        System.out.println("🔋 " + CMD_NAME + " detected: device=" + messageBody.getDeviceName() + 
            ", slot=" + parsed.get("slot") + 
            ", powerbank=" + parsed.get("powerbankSN") + 
            ", power=" + parsed.get("power") + "%");
        
        // Log transaction for audit trail
        logTransaction(messageBody, rawHex, parsed, transactionLogger);
        
        // Note: The actual return processing is handled by the API endpoint
        // /api/rentbox/order/return which is called by the device firmware
    }
    
    /**
     * Parse return event response
     */
    private Map<String, Object> parseResponse(byte[] bytes) {
        Map<String, Object> parsed = new HashMap<>();
        
        if (bytes.length >= 15) {
            // Format: A8 00 0F 40 [room] [slot] [SN_4bytes] [status] [xx] [xx] [power] [checksum]
            int room = bytes[4] & 0xFF;
            int slot = bytes[5] & 0xFF;
            
            // Parse 4-byte SN (little-endian)
            int[] snBytes = new int[]{
                bytes[6] & 0xFF,
                bytes[7] & 0xFF,
                bytes[8] & 0xFF,
                bytes[9] & 0xFF
            };
            int snAsInt = ByteUtils.getJavaInt(snBytes);
            
            int status = bytes[10] & 0xFF;
            int power = bytes[13] & 0xFF;
            
            parsed.put("room", room);
            parsed.put("slot", slot);
            parsed.put("powerbankSN", String.valueOf(snAsInt));
            parsed.put("snAsInt", snAsInt);
            parsed.put("status", status);
            parsed.put("power", power);
            parsed.put("success", status == 0x01);
        }
        
        return parsed;
    }
}
