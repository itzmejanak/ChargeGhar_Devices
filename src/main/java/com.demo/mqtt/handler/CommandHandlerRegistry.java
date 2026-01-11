package com.demo.mqtt.handler;

import com.demo.common.MessageBody;
import com.demo.common.TransactionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for command handlers
 * 
 * Auto-discovers all CommandHandler beans and registers them by command code.
 * Provides dispatch method to route messages to appropriate handlers.
 * 
 * Usage:
 *   commandHandlerRegistry.dispatch(messageBody, bytes, rawHex);
 */
@Component
public class CommandHandlerRegistry {
    
    @Autowired
    private List<CommandHandler> handlers;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private TransactionLogger transactionLogger;
    
    private final Map<Integer, CommandHandler> handlerMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        for (CommandHandler handler : handlers) {
            handlerMap.put(handler.getCommandCode(), handler);
            System.out.println("📋 Registered handler: " + handler.getCommandName() + 
                " (0x" + Integer.toHexString(handler.getCommandCode()).toUpperCase() + ")");
        }
        System.out.println("✅ CommandHandlerRegistry initialized with " + handlerMap.size() + " handlers");
    }
    
    /**
     * Dispatch message to appropriate handler based on command code
     * 
     * @param cmd Command code from message
     * @param messageBody MQTT message body
     * @param bytes Raw payload bytes
     * @param rawHex Hex string representation
     * @return true if handler was found and executed, false otherwise
     */
    public boolean dispatch(int cmd, MessageBody messageBody, byte[] bytes, String rawHex) {
        CommandHandler handler = handlerMap.get(cmd);
        
        if (handler == null) {
            System.out.println("⚠️ Unknown CMD: 0x" + Integer.toHexString(cmd).toUpperCase() + " - no handler registered");
            return false;
        }
        
        System.out.println("📨 Received CMD: 0x" + Integer.toHexString(cmd).toUpperCase() + 
            " (" + handler.getCommandName() + ") from device: " + messageBody.getDeviceName());
        
        try {
            handler.handle(messageBody, bytes, rawHex, redisTemplate, transactionLogger);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Handler error for " + handler.getCommandName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a handler exists for given command code
     */
    public boolean hasHandler(int cmd) {
        return handlerMap.containsKey(cmd);
    }
    
    /**
     * Get handler for command code (for testing)
     */
    public CommandHandler getHandler(int cmd) {
        return handlerMap.get(cmd);
    }
    
    /**
     * Get all registered command codes
     */
    public java.util.Set<Integer> getRegisteredCommands() {
        return handlerMap.keySet();
    }
}
