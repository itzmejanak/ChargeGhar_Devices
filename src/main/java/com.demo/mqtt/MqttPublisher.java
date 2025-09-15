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

    // Replace Alibaba device status check with MQTT heartbeat
    public DeviceOnline getDeviceStatus(String productKey, String deviceName) {
        // Check last heartbeat in Redis
        String key = "device_heartbeat:" + deviceName;
        BoundValueOperations ops = redisTemplate.boundValueOps(key);
        Long lastSeen = (Long) ops.get();

        if (lastSeen == null) return DeviceOnline.NO_DEVICE;

        long now = System.currentTimeMillis();
        if (now - lastSeen < 60000) { // 1 minute threshold
            return DeviceOnline.ONLINE;
        } else {
            return DeviceOnline.OFFLINE;
        }
    }

    // Replace Alibaba MQTT publish with direct MQTT
    public void sendMsgAsync(String productKey, String topicFullName, String messageContent, int qos) throws Exception {
        // Convert Alibaba topic format to EMQX format
        // "/productKey/deviceName/get" → "device/deviceName/command"
        String[] parts = topicFullName.split("/");
        String deviceName = parts.length > 2 ? parts[2] : parts[1];
        String emqxTopic = "device/" + deviceName + "/command";

        MqttMessage message = new MqttMessage(messageContent.getBytes());
        message.setQos(qos);
        message.setRetained(false);

        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.publish(emqxTopic, message);
            
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
            throw new Exception("MQTT client not connected");
        }
    }

    // Overloaded method for byte array
    public void sendMsgAsync(String productKey, String topicFullName, byte[] bytes, int qos) throws Exception {
        String[] parts = topicFullName.split("/");
        String deviceName = parts.length > 2 ? parts[2] : parts[1];
        String emqxTopic = "device/" + deviceName + "/command";

        MqttMessage message = new MqttMessage(bytes);
        message.setQos(qos);
        message.setRetained(false);

        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.publish(emqxTopic, message);
        } else {
            throw new Exception("MQTT client not connected");
        }
    }

    // Simplified device config - no complex Alibaba authentication
    public DeviceConfig getIotDeviceConfig(String productKey, String deviceName) {
        DeviceConfig config = new DeviceConfig();
        config.setDeviceName(deviceName);
        config.setProductKey(productKey);
        config.setHost(appConfig.getMqttBroker());
        config.setPort(appConfig.getMqttPort());
        config.setCreatedTime(new Date());
        config.setTimeStamp(String.valueOf(System.currentTimeMillis()));

        // Simple authentication for EMQX
        config.setIotId("device_" + deviceName);
        config.setIotToken(appConfig.getMqttPassword());
        config.setDeviceSecret(""); // Not used in EMQX

        return config;
    }

    // Compatibility methods for existing code
    public Map<String, DeviceOnline> getDeviceStatusMap(String productKey, String... deviceNames) {
        Map<String, DeviceOnline> statusMap = new HashMap<>();
        for (String deviceName : deviceNames) {
            statusMap.put(deviceName, getDeviceStatus(productKey, deviceName));
        }
        return statusMap;
    }

    // Mock methods for compatibility (not used in EMQX)
    public Object getDeviceInfo(String productKey, String deviceName) {
        // Return mock response for compatibility
        return new Object();
    }

    public Object registDevice(String productKey, String deviceName) {
        // Auto-registration in EMQX - return success
        return new Object();
    }
}