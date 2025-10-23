package com.demo.mqtt;

import com.demo.common.MessageBody;
import com.demo.common.AppConfig;
import com.demo.serialport.SerialPortData;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class MqttSubscriber implements MqttCallback {
    
    @Autowired
    private AppConfig appConfig;

    @Autowired
    RedisTemplate redisTemplate;

    private MqttClient mqttClient;
    private Exception exception;
    private boolean isRunning = false;
    private List<MessageBody> messageBodys = new ArrayList<>();

    /**
     * Auto-start MQTT Subscriber on application startup
     * This ensures the subscriber is always ready to receive messages from devices
     */
    @PostConstruct
    public void autoStart() {
        try {
            System.out.println("🚀 Auto-starting MQTT Subscriber...");
            startQueue();
            System.out.println("✅ MQTT Subscriber auto-started successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to auto-start MQTT Subscriber: " + e.getMessage());
            e.printStackTrace();
            this.exception = e;
        }
    }

    public void startQueue() throws Exception {
        // Prevent multiple starts
        if (isRunning && mqttClient != null && mqttClient.isConnected()) {
            System.out.println("⚠️ MQTT Subscriber already running");
            return;
        }
        
        String protocol = appConfig.isMqttSsl() ? "ssl://" : "tcp://";
        String broker = protocol + appConfig.getMqttBroker() + ":" + appConfig.getMqttPort();
        
        // Add unique timestamp to prevent clientId conflicts on restart
        String clientId = appConfig.getMqttClientId() + "-subscriber-" + System.currentTimeMillis();
        mqttClient = new MqttClient(broker, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(appConfig.getMqttUsername());
        options.setPassword(appConfig.getMqttPassword().toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);
        options.setConnectionTimeout(30);
        options.setAutomaticReconnect(true);  // Enable automatic reconnection

        mqttClient.setCallback(this);
        
        try {
            mqttClient.connect(options);
            
            // ✅ VERIFY CONNECTION BEFORE PROCEEDING
            if (!mqttClient.isConnected()) {
                throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
            }
            
            System.out.println("✅ MQTT Subscriber connected to: " + broker);
            System.out.println("   Client ID: " + clientId);

            // FIX: Subscribe to exact topics device publishes to (with leading slash)
            // Device publishes to: /powerbank/{deviceName}/user/upload
            // Device publishes to: /powerbank/{deviceName}/user/heart (heartbeat)
            String productKey = appConfig.getProductKey();
            
            // Primary subscriptions (with leading slash to match device topics)
            String uploadTopic = "/" + productKey + "/+/user/upload";
            String heartTopic = "/" + productKey + "/+/user/heart";
            
            mqttClient.subscribe(uploadTopic, 1);
            mqttClient.subscribe(heartTopic, 1);
            System.out.println("   Subscribed to: " + uploadTopic);
            System.out.println("   Subscribed to: " + heartTopic);
            
            // Backward compatibility: Also listen to non-slash topics in case some devices use them
            mqttClient.subscribe(productKey + "/+/user/upload", 1);
            mqttClient.subscribe(productKey + "/+/user/heart", 1);
            System.out.println("   Subscribed to: " + productKey + "/+/user/upload");
            System.out.println("   Subscribed to: " + productKey + "/+/user/heart");
            
            // Legacy device format for backward compatibility
            mqttClient.subscribe("device/+/upload", 1);
            mqttClient.subscribe("device/+/status", 1);
            System.out.println("   Subscribed to: device/+/upload");
            System.out.println("   Subscribed to: device/+/status");
            
            isRunning = true;
            System.out.println("✅ MQTT Subscriber started successfully - Ready to receive messages");
            
        } catch (MqttException e) {
            System.err.println("❌ MQTT Subscriber connection failed!");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Reason Code: " + e.getReasonCode());
            System.err.println("   Broker: " + broker);
            throw e;
        }
    }

    public void stopQueue() throws Exception {
        isRunning = false;
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
            System.out.println("✅ MQTT Subscriber stopped and disconnected");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("❌ MQTT Subscriber connection lost: " + cause.getMessage());
        this.exception = new Exception(cause);
        isRunning = false;
        
        // Note: Automatic reconnect is enabled in MqttConnectOptions
        // Paho client will handle reconnection automatically
        System.out.println("⚡ Automatic reconnection enabled - will retry connection...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            // Extract device name from topic
            // Topic format: /powerbank/{deviceName}/user/upload or /powerbank/{deviceName}/user/heart
            String[] topicParts = topic.split("/");
            String deviceName;
            String messageType;
            
            // Handle topics with leading slash: /powerbank/deviceName/user/upload
            if (topicParts.length >= 4 && topicParts[0].isEmpty()) {
                deviceName = topicParts[2]; // Skip empty first element from leading slash
                messageType = topicParts[4]; // "upload", "heart", "status"
            } 
            // Handle topics without leading slash: powerbank/deviceName/user/upload
            else if (topicParts.length >= 4) {
                deviceName = topicParts[1];
                messageType = topicParts[3];
            }
            // Fallback for other formats
            else if (topicParts.length >= 3) {
                deviceName = topicParts[1];
                messageType = topicParts[2];
            } else {
                deviceName = "unknown";
                messageType = "upload";
            }
            
            // Track device activity for status checking
            String activityKey = "device_activity:" + deviceName;
            BoundValueOperations activityOps = redisTemplate.boundValueOps(activityKey);
            activityOps.set(System.currentTimeMillis(), 10, TimeUnit.MINUTES);
            
            // Handle heartbeat messages specially (both "heart" and "status" indicate heartbeat)
            if ("status".equals(messageType) || "heart".equals(messageType)) {
                String heartbeatKey = "device_heartbeat:" + deviceName;
                BoundValueOperations heartbeatOps = redisTemplate.boundValueOps(heartbeatKey);
                heartbeatOps.set(System.currentTimeMillis(), 5, TimeUnit.MINUTES);
                System.out.println("Heartbeat received from device: " + deviceName + " (type: " + messageType + ")");
            }
            
            // Convert MQTT message to MessageBody format
            MessageBody messageBody = new MessageBody();
            messageBody.setTopic(topic);
            messageBody.setDeviceName(deviceName);
            messageBody.setProductKey(appConfig.getProductKey());
            messageBody.setPayload(Base64.encodeBase64String(message.getPayload()));
            messageBody.setMessageType(messageType);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);
            messageBody.setMessageId(UUID.randomUUID().toString());

            handlerMessage(messageBody);
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for subscriber
    }

    // Keep same handlerMessage logic as original MnsUtils
    public void handlerMessage(MessageBody messageBody) {
        // Same Redis caching logic - no changes needed
        putMessageBody(messageBody);

        String type = messageBody.getMessageType();
        if ("upload".equals(type)) {
            int cmd = SerialPortData.checkCMD(messageBody.getPayloadAsBytes());
            switch (cmd) {
                case 0x10:
                    String key = "check:" + messageBody.getDeviceName();
                    BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);
                    long time = boundValueOps.getExpire();
                    if (time <= 0) break;
                    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
                    break;
                case 0x31:
                    key = "popup_sn:" + messageBody.getDeviceName();
                    boundValueOps = redisTemplate.boundValueOps(key);
                    time = boundValueOps.getExpire();
                    if (time <= 0) break;
                    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
                    break;
                case 0x40:
                    // Power bank return event - process immediately
                    System.out.println("Power bank return detected for device: " + messageBody.getDeviceName());
                    break;
            }
        }
    }

    // Keep compatibility with existing code
    public void putMessageBody(MessageBody messageBody) {
        synchronized (messageBodys) {
            messageBodys.add(messageBody);
            // Keep only last 100 messages
            if (messageBodys.size() > 100) {
                messageBodys.remove(0);
            }
        }
    }

    public List<MessageBody> getMessageBodys() {
        synchronized (messageBodys) {
            return new ArrayList<>(messageBodys);
        }
    }

    public void clearMessageBody() {
        synchronized (messageBodys) {
            messageBodys.clear();
        }
    }

    public boolean isRunning() {
        return isRunning && mqttClient != null && mqttClient.isConnected();
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}