package com.demo.aliyun;

/**
 * DEPRECATED: This class has been replaced by DeviceCommandUtils
 * Keeping for backward compatibility during migration
 */

import com.demo.common.AppConfig;
import com.demo.common.CacheMessage;
import com.demo.common.DeviceOnline;
import com.demo.message.Powerbank;
import com.demo.message.ReceivePopupSN;
import com.demo.message.ReceiveUpload;
import com.demo.mqtt.DeviceCommandUtils;
import com.demo.mqtt.MqttPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated Use DeviceCommandUtils instead
 */
@Component
@Deprecated
public class RentboxUtils {
    public static final String SEND_CHECK ="{\"cmd\":\"check\"}";
    public static final String SEND_CHECK_LOG ="{\"cmd\":\"check_log\"}";
    public static final String SEND_CHECK_ALL ="{\"cmd\":\"check_all\"}";
    public static final String SEND_PUISH_VERSION_PUBLISH ="{\"cmd\":\"push_version_publish\"}";
    public static final String SEND_POPUP = "{\"cmd\":\"popup_sn\",\"data\":\"%s\"}";

    @Autowired
    DeviceCommandUtils deviceCommandUtils;

    @Autowired
    MqttPublisher mqttPublisher;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * @deprecated Use DeviceCommandUtils.getCache() instead
     */
    @Deprecated
    public Map<String, CacheMessage> getCache() {
        return deviceCommandUtils.getCache();
    }

    /**
     * @deprecated Use DeviceCommandUtils.check() instead
     */
    @Deprecated
    public ReceiveUpload check(String rentboxSN) throws Exception {
        return deviceCommandUtils.check(rentboxSN);
    }

    /**
     * @deprecated Use DeviceCommandUtils.checkAll() instead
     */
    @Deprecated
    public ReceiveUpload checkAll(String rentboxSN) throws Exception {
        return deviceCommandUtils.checkAll(rentboxSN);
    }

    /**
     * @deprecated Use DeviceCommandUtils.popup() instead
     */
    @Deprecated
    public ReceivePopupSN popup(String rentboxSN, String singleSN) throws Exception {
        return deviceCommandUtils.popup(rentboxSN, singleSN);
    }

    /**
     * @deprecated Use DeviceCommandUtils.popupByRandom() instead
     */
    @Deprecated
    public ReceivePopupSN popupByRandom(String rentboxSN, int minPower) throws Exception {
        return deviceCommandUtils.popupByRandom(rentboxSN, minPower);
    }

    /**
     * @deprecated Use DeviceCommandUtils.checkOnlineStatus() instead
     */
    @Deprecated
    public void checkOnlineStatus(String rentboxSN) throws Exception {
        deviceCommandUtils.checkOnlineStatus(rentboxSN);
    }





}
