package com.demo.controller;

import com.demo.bean.ApiIotClientConValid;
import com.demo.bean.ApiRentboxOrderReturnValid;
import com.demo.common.AppConfig;
import com.demo.common.DeviceConfig;
import com.demo.common.HttpResult;
import com.demo.common.MessageBody;
import com.demo.connector.ChargeGharConnector;
import com.demo.emqx.EmqxDeviceService;
import com.demo.helper.ControllerHelper;
import com.demo.message.ReceiveUpload;
import com.demo.model.Device;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import com.demo.service.DeviceService;
import com.demo.tools.ByteUtils;
import com.demo.tools.HttpServletUtils;
import com.demo.tools.JsonUtils;
import com.demo.tools.SignUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ResponseBody
@Controller
public class ApiController {

    @Autowired
    AppConfig appConfig;

    @Autowired
    MqttPublisher mqttPublisher;

    @Autowired
    MqttSubscriber mqttSubscriber;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    EmqxDeviceService emqxDeviceService;

    @Autowired
    ChargeGharConnector chargeGharConnector;

    @Autowired
    ControllerHelper controllerHelper;

    @Autowired
    private DeviceService deviceService;

    // ========================================================================================
    // API ENDPOINTS
    // ========================================================================================

    @RequestMapping("/api/iot/client/con")
    public HttpResult iotClientCon(ApiIotClientConValid valid, HttpServletResponse response, HttpServletRequest request) throws Exception {
        HttpResult httpResult = new HttpResult();
        MessageBody messageBody = new MessageBody();

        try {
            String url = HttpServletUtils.getRealUrl(true);
            messageBody.setMessageId("client_con");
            messageBody.setMessageType("http");
            messageBody.setTopic("POST：" + url + "  UUID:" + valid.getUuid());
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);

            this.checkSign(valid, valid.getSign());

            // Get existing device or auto-register if not found
            Device device = controllerHelper.getOrCreateDevice(valid.getUuid());

            // Get or create EMQX configuration using database password
            DeviceConfig config = emqxDeviceService.getOrCreateDeviceConfig(valid.getUuid(), device.getPassword());
            if (config == null) {
                throw new Exception("Failed to get device configuration. EMQX service may be unavailable.");
            }

            // Validate and sync device configuration with EMQX
            config = controllerHelper.validateAndSyncDeviceConfig(valid.getUuid(), device, config);

            // Return device configuration with validated password
            String[] arrStr = new String[]{
                valid.getUuid(),
                config.getProductKey(),
                config.getHost(),
                String.valueOf(config.getPort()),
                config.getIotId(),
                device.getPassword(),
                config.getTimeStamp()
            };

            String data = StringUtils.join(arrStr, ",");
            httpResult.setData(data);
            messageBody.setPayload(JsonUtils.toJson(httpResult));

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
            messageBody.setPayload(e.toString());
            e.printStackTrace();
        } finally {
            mqttSubscriber.putMessageBody(messageBody);
        }

        return httpResult;
    }

    @RequestMapping("/api/iot/client/clear")
    public HttpResult deviceCreate(HttpServletResponse response, @RequestParam String deviceName) throws Exception {
        HttpResult httpResult = new HttpResult();

        try {
            // Clear API cache
            String apiKey = "clientConect:" + deviceName;
            BoundValueOperations apiBoundValueOps = redisTemplate.boundValueOps(apiKey);
            apiBoundValueOps.expire(-2, TimeUnit.SECONDS);

            // Clear EMQX cache
            String emqxKey = "device_credentials:" + deviceName;
            BoundValueOperations emqxBoundValueOps = redisTemplate.boundValueOps(emqxKey);
            emqxBoundValueOps.expire(-2, TimeUnit.SECONDS);

            System.out.println("Cleared both API and EMQX cache for device: " + deviceName);

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
        }

        return httpResult;
    }

    @RequestMapping("/api/rentbox/order/return")
    public HttpResult powerbankReturn(ApiRentboxOrderReturnValid valid, HttpServletResponse response) throws Exception {
        HttpResult httpResult = new HttpResult();
        MessageBody messageBody = new MessageBody();

        try {
            String url = HttpServletUtils.getRealUrl(true);
            messageBody.setMessageId("return powerbank");
            messageBody.setMessageType("http");
            messageBody.setTopic("GET：" + url);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);

            this.checkSign(valid, valid.getSign());

            // Send return event to ChargeGhar Main
            controllerHelper.syncReturnEventToMain(valid.getRentboxSN(), valid.getSingleSN(), valid.getHole());

            messageBody.setPayload(JsonUtils.toJson(httpResult));

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
            messageBody.setPayload(e.toString());
        } finally {
            mqttSubscriber.putMessageBody(messageBody);
        }

        return httpResult;
    }

    @RequestMapping("/api/rentbox/upload/data")
    public HttpResult rentboxOrderReturnEnd(@RequestBody byte[] bytes,
        @RequestParam String rentboxSN,
        @RequestParam String sign,
        @RequestParam(defaultValue = "0") String signal,
        @RequestParam(required = false) Integer io,
        @RequestParam(required = false) String ssid,
        HttpServletResponse response) throws Exception {
        HttpResult httpResult = new HttpResult();
        MessageBody messageBody = new MessageBody();

        try {
            String data = ByteUtils.to16Hexs(bytes);
            String url = HttpServletUtils.getRealUrl(true);
            messageBody.setMessageId("upload data");
            messageBody.setMessageType("http");
            messageBody.setTopic("POST：" + url);
            messageBody.setPayload(data);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);

            Map params = new HashMap<>();
            params.put("rentboxSN", rentboxSN);
            params.put("signal", signal);
            if (io != null) {
                params.put("io", io.toString());
            }
            if (StringUtils.isNotEmpty(ssid)) {
                params.put("ssid", ssid);
            }
            this.checkSign(params, sign);

            // Parse the upload data
            ReceiveUpload receiveUpload = new ReceiveUpload(bytes);

            // Update device activity and heartbeat timestamps in Redis
            long now = System.currentTimeMillis();

            // Update device activity (checked by getDeviceStatus)
            String activityKey = "device_activity:" + rentboxSN;
            BoundValueOperations activityOps = redisTemplate.boundValueOps(activityKey);
            activityOps.set(now, 25, TimeUnit.MINUTES);

            // Update device heartbeat (checked by getDeviceStatus)
            String heartbeatKey = "device_heartbeat:" + rentboxSN;
            BoundValueOperations heartbeatOps = redisTemplate.boundValueOps(heartbeatKey);
            heartbeatOps.set(now, 5, TimeUnit.MINUTES);

            // Log parsed data
            controllerHelper.logDeviceUploadData(rentboxSN, signal, sign, io, ssid, bytes.length, data, receiveUpload);

            // Cache the raw upload data for return endpoint to access battery level
            String uploadCacheKey = "upload_data:" + rentboxSN;
            BoundValueOperations uploadCacheOps = redisTemplate.boundValueOps(uploadCacheKey);
            uploadCacheOps.set(bytes, 30, TimeUnit.MINUTES);

            // Sync device data to ChargeGhar Main
            controllerHelper.syncDeviceUploadToMain(rentboxSN, receiveUpload, signal, ssid);

            messageBody.setPayload(JsonUtils.toJson(httpResult));

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
            messageBody.setPayload(e.toString());

            System.out.println("❌ ERROR: " + e.toString());
            e.printStackTrace();
        } finally {
            mqttSubscriber.putMessageBody(messageBody);
        }

        return httpResult;
    }

    @RequestMapping("/api/rentbox/config/data")
    public HttpResult rentboxConfigData() {
        HttpResult httpResult = new HttpResult();
        // Updated dAreaConfig to use your region code 0x165 (357 decimal)
        httpResult.setData("{\"dRotationRefer\":\"15\",\"dReturnLocked\":\"0\",\"dHeadConfig\":\"43\",\"dRotationNumber\":\"5\",\"dRotationEnable\":\"1\",\"dMotorEnable\":\"1\",\"dAreaConfig\":\"357\"}");
        return httpResult;
    }

    /**
     * Check the signature
     *
     * @param valid Object to validate
     * @param sign  Signature to check against
     * @throws Exception if signature doesn't match
     */
    protected void checkSign(Object valid, String sign) throws Exception {
        if (!SignUtils.getSign(valid).equals(sign)) {
            throw new Exception("ERROR SIGN");
        }
    }

}