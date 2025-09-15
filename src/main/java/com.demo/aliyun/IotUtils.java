package com.demo.aliyun;

/**
 * DEPRECATED: This class has been replaced by MqttPublisher
 * Keeping for backward compatibility during migration
 */

import com.demo.common.AppConfig;
import com.demo.common.DeviceConfig;
import com.demo.common.DeviceOnline;
import com.demo.common.MessageBody;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @deprecated Use MqttPublisher instead
 */
@Component
@Deprecated
public class IotUtils {
    
    @Autowired
    private MqttPublisher mqttPublisher;
    
    @Autowired
    private MqttSubscriber mqttSubscriber;
    
    private AppConfig appConfig;

    @Autowired
    public IotUtils(AppConfig appConfig) {
        this.appConfig = appConfig;
    }


    /**
     * @deprecated Use MqttPublisher.getDeviceStatusMap() instead
     */
    @Deprecated
    public Map<String, DeviceOnline> getDeviceStatusMap(String productKey, String... deviceNames) throws Exception {
        return mqttPublisher.getDeviceStatusMap(productKey, deviceNames);
    }

    /**
     * @deprecated Use MqttPublisher.getDeviceStatus() instead
     */
    @Deprecated
    public DeviceOnline getDeviceStatus(String productKey, String deviceName) throws Exception {
        return mqttPublisher.getDeviceStatus(productKey, deviceName);
    }

    /**
     * @deprecated Use MqttPublisher.getDeviceInfo() instead
     */
    @Deprecated
    public Object getDeviceInfo(String productKey, String deviceName) throws Exception {
        return mqttPublisher.getDeviceInfo(productKey, deviceName);
    }

    /**
     * @deprecated Use MqttPublisher.registDevice() instead
     */
    @Deprecated
    public Object registDevice(String productKey, String deviceName) throws Exception {
        return mqttPublisher.registDevice(productKey, deviceName);
    }



    /**
     * @deprecated Use MqttPublisher.getIotDeviceConfig() instead
     */
    @Deprecated
    public DeviceConfig getIotDeviceConfig(String productKey, String uuid) throws Exception {
        return mqttPublisher.getIotDeviceConfig(productKey, uuid);
    }

    /**
     * @deprecated Use MqttPublisher.sendMsgAsync() instead
     */
    @Deprecated
    public void sendMsgAsync(String productKey, String topicFullName, String messageContent, int qos) throws Exception {
        mqttPublisher.sendMsgAsync(productKey, topicFullName, messageContent, qos);
    }

    /**
     * @deprecated Use MqttPublisher.sendMsgAsync() instead
     */
    @Deprecated
    public void sendMsgAsync(String productKey, String topicFullName, byte[] bytes, int qos) throws Exception {
        mqttPublisher.sendMsgAsync(productKey, topicFullName, bytes, qos);
    }

}
