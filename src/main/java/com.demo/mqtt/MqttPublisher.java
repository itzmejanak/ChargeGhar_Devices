package com.demo.mqtt;

import com.demo.common.DeviceConfig;
import com.demo.common.DeviceOnline;
import com.demo.common.MessageBody;
import com.demo.common.AppConfig;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class MqttPublisher {
    
    @Autowired
    private AppConfig appConfig;

    @Autowired
    private MqttSubscriber mqttSubscriber;

    @Autowired
    RedisTemplate redisTemplate;

    private MqttClient mqttClient;

    @PostConstruct
    public void init() throws Exception {
        String protocol = appConfig.isMqttSsl() ? "ssl://" : "tcp://";
        String broker = protocol + appConfig.getMqttBroker() + ":" + appConfig.getMqttPort();
        
        mqttClient = new MqttClient(broker, appConfig.getMqttClientId() + "-publisher");

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(appConfig.getMqttUsername());
        options.setPassword(appConfig.getMqttPassword().toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);
        options.setConnectionTimeout(30);

        mqttClient.connect(options);
        System.out.println("MQTT Publisher initialized successfully");
    }

    // Enhanced device status check with multiple indicators
    public DeviceOnline getDeviceStatus(String productKey, String deviceName) {
        // Check multiple indicators for device status
        
        // 1. Check last heartbeat in Redis
        String heartbeatKey = "device_heartbeat:" + deviceName;
        BoundValueOperations heartbeatOps = redisTemplate.boundValueOps(heartbeatKey);
        Long lastSeen = (Long) heartbeatOps.get();
        
        // 2. Check if device has active connection config
        String configKey = "clientConect:" + deviceName;
        BoundValueOperations configOps = redisTemplate.boundValueOps(configKey);
        Object deviceConfig = configOps.get();
        
        // 3. Check recent message activity
        String activityKey = "device_activity:" + deviceName;
        BoundValueOperations activityOps = redisTemplate.boundValueOps(activityKey);
        Long lastActivity = (Long) activityOps.get();
        
        long now = System.currentTimeMillis();
        
        // Device is considered ONLINE if:
        // - Has recent heartbeat (within 2 minutes) OR
        // - Has recent activity (within 5 minutes) AND has valid config
        if (lastSeen != null && (now - lastSeen < 120000)) { // 2 minute threshold for heartbeat
            return DeviceOnline.ONLINE;
        }
        
        if (deviceConfig != null && lastActivity != null && (now - lastActivity < 300000)) { // 5 minute threshold for activity
            return DeviceOnline.ONLINE;
        }
        
        // If device has config but no recent activity, it's registered but offline
        if (deviceConfig != null) {
            return DeviceOnline.OFFLINE;
        }
        
        // No device registration found
        return DeviceOnline.NO_DEVICE;
    }

    // EMQX MQTT publish with standardized topic format
    public void sendMsgAsync(String productKey, String topicFullName, String messageContent, int qos) throws Exception {
        String emqxTopic;
        String deviceName;
        
        // Handle both legacy and new topic formats
        if (topicFullName.startsWith("device/")) {
            // Already in EMQX format: device/{deviceName}/command
            emqxTopic = topicFullName;
            String[] parts = topicFullName.split("/");
            deviceName = parts.length > 1 ? parts[1] : "unknown";
        } else {
            // Convert legacy format to EMQX format
            // "/productKey/deviceName/get" → "device/deviceName/command"
            String[] parts = topicFullName.split("/");
            deviceName = parts.length > 2 ? parts[2] : (parts.length > 1 ? parts[1] : "unknown");
            emqxTopic = "device/" + deviceName + "/command";
        }

        MqttMessage message = new MqttMessage(messageContent.getBytes());
        message.setQos(qos);
        message.setRetained(false);

        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.publish(emqxTopic, message);
            System.out.println("Message sent to device " + deviceName + " on topic: " + emqxTopic);
            
            // Track message activity
            String activityKey = "device_activity:" + deviceName;
            BoundValueOperations activityOps = redisTemplate.boundValueOps(activityKey);
            activityOps.set(System.currentTimeMillis(), 10, TimeUnit.MINUTES);
            
            // Keep same logging for compatibility
            MessageBody messageBody = new MessageBody();
            messageBody.setMessageId("send_message");
            messageBody.setMessageType("send");
            messageBody.setTopic(emqxTopic);
            messageBody.setDeviceName(deviceName);
            messageBody.setProductKey(productKey);
            messageBody.setPayload(messageContent);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);
            mqttSubscriber.putMessageBody(messageBody);
        } else {
            throw new Exception("MQTT client not connected to EMQX broker");
        }
    }

    // Overloaded method for byte array
    public void sendMsgAsync(String productKey, String topicFullName, byte[] bytes, int qos) throws Exception {
        String[] parts = topicFullName.split("/");
        String deviceName = parts.length > 2 ? parts[2] : parts[1];
        String topicPrefix = productKey + "/" + deviceName;
        String userPath = appConfig.isTopicType() ? "/user" : "";
        String emqxTopic = topicPrefix + userPath + "/command";

        MqttMessage message = new MqttMessage(bytes);
        message.setQos(qos);
        message.setRetained(false);

        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.publish(emqxTopic, message);
        } else {
            throw new Exception("MQTT client not connected");
        }
    }



    // Compatibility methods for existing code
    public Map<String, DeviceOnline> getDeviceStatusMap(String productKey, String... deviceNames) {
        Map<String, DeviceOnline> statusMap = new HashMap<>();
        for (String deviceName : deviceNames) {
            statusMap.put(deviceName, getDeviceStatus(productKey, deviceName));
        }
        return statusMap;
    }


}