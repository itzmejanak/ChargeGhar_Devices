package com.demo.controller;


import com.demo.common.DeviceConfig;
import com.demo.common.MessageBody;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import com.demo.emqx.EmqxDeviceService;
import com.demo.bean.ApiIotClientConValid;
import com.demo.bean.ApiRentboxOrderReturnValid;
import com.demo.common.AppConfig;
import com.demo.common.HttpResult;
import com.demo.message.ReceiveUpload;
import com.demo.message.Pinboard;
import com.demo.message.Powerbank;
import com.demo.tools.ByteUtils;
import com.demo.tools.HttpServletUtils;
import com.demo.tools.JsonUtils;
import com.demo.tools.SignUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ResponseBody
@Controller
public class ApiController {
    @Autowired
    AppConfig appConfig;

    @Autowired
    MqttPublisher mqttPublisher;    // Instead of IotUtils

    @Autowired
    MqttSubscriber mqttSubscriber;  // Instead of MnsUtils

    @Autowired
    RedisTemplate redisTemplate;
    
    @Autowired
    EmqxDeviceService emqxDeviceService;

    @RequestMapping("/api/iot/client/con")
    public HttpResult iotClientCon(ApiIotClientConValid valid, HttpServletResponse response, HttpServletRequest request) throws Exception {
        HttpResult httpResult = new HttpResult();
        MessageBody messageBody = new MessageBody();
        try {
            //TEST LOG
            String url = HttpServletUtils.getRealUrl(true);
            messageBody.setMessageId("client_con");
            messageBody.setMessageType("http");
            messageBody.setTopic("GET：" + url);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);

            // Log the request parameters
            messageBody.setTopic("POST：" + url + "  UUID:" + valid.getUuid());



            this.checkSign(valid,valid.getSign());

            //EMQX DEVICE REGISTRATION AND CONFIG
            String key = "clientConect:" + valid.getUuid();
            BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);
            DeviceConfig config = (DeviceConfig) boundValueOps.get();
            if(config == null || StringUtils.isBlank(config.getTimeStamp())){
                try {
                    // *** NEW: Register device with EMQX platform and get unique credentials ***
                    config = emqxDeviceService.getOrCreateDeviceConfig(valid.getUuid());
                    if(config != null) {
                        boundValueOps.set(config, 1, TimeUnit.DAYS);
                        System.out.println("Device registered successfully: " + valid.getUuid());
                    }
                } catch (Exception emqxError) {
                    System.err.println("EMQX device registration failed for " + valid.getUuid() + ": " + emqxError.getMessage());
                    throw new Exception("Device registration failed. Please try again later. Error: " + emqxError.getMessage());
                }
            }
            if(config == null){
                throw new Exception("Failed to get device configuration. EMQX service may be unavailable.");
            }

            // *** CHANGED: Return EMQX format instead of Alibaba format ***
            String[] arrStr = new String[]{
                valid.getUuid(),                    // Hardware IMEI (unchanged)
                config.getProductKey(),             // Product identifier
                config.getHost(),                   // EMQX broker URL
                String.valueOf(config.getPort()),   // EMQX port (8883 or 1883)
                config.getIotId(),                  // EMQX username (device-based)
                config.getIotToken(),               // EMQX password
                config.getTimeStamp()               // Timestamp
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
        }
        finally {
            // *** CHANGED: Use MQTT subscriber instead of MNS ***
            mqttSubscriber.putMessageBody(messageBody);
        }
        return httpResult;
    }

    @RequestMapping("/api/iot/client/clear")
    public HttpResult deviceCreate(HttpServletResponse response,  @RequestParam String deviceName) throws Exception {
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

        }
        catch (Exception e){
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
            //TEST LOG
            String url = HttpServletUtils.getRealUrl(true);
            messageBody.setMessageId("return powerbank");
            messageBody.setMessageType("http");
            messageBody.setTopic("GET：" + url);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);

            this.checkSign(valid,valid.getSign());
            messageBody.setPayload(JsonUtils.toJson(httpResult));
        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
            messageBody.setPayload(e.toString());
        }
        finally {
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
            // Log request parameters
            System.out.println("========================================");
            System.out.println("REQUEST PARAMETERS:");
            System.out.println("  rentboxSN: " + rentboxSN);
            System.out.println("  signal: " + signal);
            System.out.println("  sign: " + sign);
            System.out.println("  io: " + (io != null ? io.toString() : "null"));
            System.out.println("  ssid: " + (StringUtils.isNotEmpty(ssid) ? ssid : "null"));
            System.out.println("  data length: " + bytes.length + " bytes");
            
            //TEST LOG
            String data = ByteUtils.to16Hexs(bytes);
            String url = HttpServletUtils.getRealUrl(true);
            messageBody.setMessageId("upload data");
            messageBody.setMessageType("http");
            messageBody.setTopic("POST：" + url);
            messageBody.setPayload(data);
            messageBody.setTimestamp(System.currentTimeMillis() / 1000);

            System.out.println("  hex data: " + data);

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
            
            // ✅ FIX: Update device activity and heartbeat timestamps in Redis
            // This makes the device show as ONLINE in the UI
            long now = System.currentTimeMillis();
            
            // Update device activity (checked by getDeviceStatus)
            String activityKey = "device_activity:" + rentboxSN;
            BoundValueOperations activityOps = redisTemplate.boundValueOps(activityKey);
            activityOps.set(now, 25, TimeUnit.MINUTES);  // Expire after 10 minutes
            
            // Update device heartbeat (checked by getDeviceStatus)
            String heartbeatKey = "device_heartbeat:" + rentboxSN;
            BoundValueOperations heartbeatOps = redisTemplate.boundValueOps(heartbeatKey);
            heartbeatOps.set(now, 5, TimeUnit.MINUTES);  // Expire after 5 minutes
            
            // Log parsed data
            System.out.println("PARSED DATA:");
            System.out.println("  Pinboard count: " + receiveUpload.getPinboards().size());
            System.out.println("  Powerbank count: " + receiveUpload.getPowerbanks().size());
            
            // Log each pinboard
            for (int i = 0; i < receiveUpload.getPinboards().size(); i++) {
                Pinboard pinboard = receiveUpload.getPinboards().get(i);
                System.out.println("  Pinboard[" + i + "]: index=" + pinboard.getIndex() + ", io=" + pinboard.getIo());
            }
            
            // Log each powerbank
            for (int i = 0; i < receiveUpload.getPowerbanks().size(); i++) {
                Powerbank pb = receiveUpload.getPowerbanks().get(i);
                System.out.println("  Powerbank[" + i + "]: " +
                    "index=" + pb.getIndex() + ", " +
                    "pinboardIndex=" + pb.getPinboardIndex() + ", " +
                    "SN=" + pb.getSnAsString() + ", " +
                    "status=" + pb.getStatus() + ", " +
                    "power=" + pb.getPower() + "%, " +
                    "area=" + pb.getArea() + ", " +
                    "temp=" + pb.getTemp() + "°C, " +
                    "microSwitch=" + pb.getMicroSwitch() + ", " +
                    "solenoidValve=" + pb.getSolenoidValveSwitch());
            }
            
            System.out.println("RESPONSE:");
            System.out.println("  code: " + httpResult.getCode());
            System.out.println("  msg: " + httpResult.getMsg());
            System.out.println("========================================");
            
            messageBody.setPayload(JsonUtils.toJson(httpResult));
        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
            messageBody.setPayload(e.toString());
            
            System.out.println("ERROR: " + e.toString());
            e.printStackTrace();
            System.out.println("========================================");
        }
        finally {
            mqttSubscriber.putMessageBody(messageBody);
        }
        return httpResult;
    }

    /**
     * Check the signature
     * @param valid
     * @param sign
     */
    protected void checkSign(Object valid, String sign) throws Exception {
        if(!SignUtils.getSign(valid).equals(sign)){
            throw new Exception("ERROR SIGN");
        }
    }
    @RequestMapping("/api/rentbox/config/data")
    public HttpResult rentboxConfigData() {
        HttpResult httpResult = new HttpResult();
        httpResult.setData("{\"dRotationRefer\":\"15\",\"dReturnLocked\":\"0\",\"dHeadConfig\":\"43\",\"dRotationNumber\":\"5\",\"dRotationEnable\":\"1\",\"dMotorEnable\":\"1\",\"dAreaConfig\":\"07\"}");
        return httpResult;
    }

}
