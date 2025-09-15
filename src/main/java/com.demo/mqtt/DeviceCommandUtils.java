package com.demo.mqtt;

import com.demo.common.CacheMessage;
import com.demo.common.DeviceOnline;
import com.demo.common.AppConfig;
import com.demo.message.Powerbank;
import com.demo.message.ReceivePopupSN;
import com.demo.message.ReceiveUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DeviceCommandUtils {
    
    // Keep all same constants and methods
    public static final String SEND_CHECK = "{\"cmd\":\"check\"}";
    public static final String SEND_CHECK_LOG = "{\"cmd\":\"check_log\"}";
    public static final String SEND_CHECK_ALL = "{\"cmd\":\"check_all\"}";
    public static final String SEND_PUISH_VERSION_PUBLISH = "{\"cmd\":\"push_version_publish\"}";
    public static final String SEND_POPUP = "{\"cmd\":\"popup_sn\",\"data\":\"%s\"}";

    @Autowired
    MqttPublisher mqttPublisher;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RedisTemplate redisTemplate;

    // Keep compatibility with existing cache
    public Map<String, CacheMessage> cache = new Hashtable<>();
    
    public Map<String, CacheMessage> getCache() {
        return cache;
    }

    // Same business logic, just replace iotUtils with mqttPublisher
    public ReceiveUpload check(String rentboxSN) throws Exception {
        String key = "check:" + rentboxSN;
        byte[] data = sendPopupWait(key, rentboxSN, SEND_CHECK, 10);
        return new ReceiveUpload(data);
    }

    public ReceiveUpload checkAll(String rentboxSN) throws Exception {
        String key = "check:" + rentboxSN;
        byte[] data = sendPopupWait(key, rentboxSN, SEND_CHECK_ALL, 10);
        return new ReceiveUpload(data);
    }

    public ReceivePopupSN popup(String rentboxSN, String singleSN) throws Exception {
        String key = "popup_sn:" + rentboxSN;
        String message = String.format(SEND_POPUP, singleSN);
        byte[] data = sendPopupWait(key, rentboxSN, message, 15); // 15 second timeout

        return new ReceivePopupSN(data);
    }

    public ReceivePopupSN popupByRandom(String rentboxSN, int minPower) throws Exception {
        // Check station status first
        ReceiveUpload receiveUpload = check(rentboxSN);
        Powerbank powerbank = receiveUpload.getPowerbankByRandom(minPower);
        if (powerbank == null) {
            throw new Exception("NO Powerbank");
        }

        // Popup the selected powerbank
        ReceivePopupSN receivePopupSN = popup(rentboxSN, powerbank.getSnAsString());
        return receivePopupSN;
    }

    private byte[] sendPopupWait(String key, String rentboxSN, String message, int overSecond) throws Exception {
        this.checkOnlineStatus(rentboxSN);

        // PUT REDIS - same logic as original
        BoundValueOperations operations = redisTemplate.boundValueOps(key);
        operations.set(null, overSecond, TimeUnit.SECONDS);

        // Use MQTT instead of Alibaba IoT
        String topicUrl = appConfig.isTopicType() ? "/user" : "";
        String topic = "/" + appConfig.getProductKey() + "/" + rentboxSN + topicUrl + "/get";
        
        mqttPublisher.sendMsgAsync(appConfig.getProductKey(), topic, message, 1);

        // Same waiting logic - no changes needed
        byte[] bytes = null;
        for (int i = 0; i < overSecond * 2; i++) {
            Thread.sleep(500);
            Object data = null;
            try {
                data = operations.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (data != null && data instanceof byte[]) {
                bytes = (byte[]) data;
                redisTemplate.boundValueOps(key).expire(-1, TimeUnit.MILLISECONDS);
                break;
            }
        }

        if (bytes == null) {
            throw new Exception("Request Time Out");
        }

        return bytes;
    }

    public void checkOnlineStatus(String rentboxSN) throws Exception {
        DeviceOnline onlineStatus = mqttPublisher.getDeviceStatus(appConfig.getProductKey(), rentboxSN);
        if (!onlineStatus.name().equals("ONLINE")) {
            throw new Exception("Device is Offline");
        }
    }
}