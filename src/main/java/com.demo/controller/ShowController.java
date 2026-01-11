package com.demo.controller;


import com.demo.common.DeviceOnline;
import com.demo.mqtt.DeviceCommandUtils;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import com.demo.common.AppConfig;
import com.demo.common.HttpResult;
import com.demo.message.ReceivePopupSN;
import com.demo.message.ReceiveUpload;
import com.demo.tools.ByteUtils;
import com.demo.tools.HttpServletUtils;
import com.demo.tools.SignUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@ResponseBody
@Controller
public class ShowController {
    @Autowired
    AppConfig appConfig;

    @Autowired
    MqttSubscriber mqttSubscriber;

    @Autowired
    MqttPublisher mqttPublisher;

    @Autowired
    DeviceCommandUtils deviceCommandUtils;  // Instead of RentboxUtils

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/show.html")
    public ModelAndView showHtml(@RequestParam String deviceName) throws Exception {
        ModelAndView mv = new ModelAndView("/web/views/page/show");

        DeviceOnline deviceOnline = mqttPublisher.getDeviceStatus(appConfig.getProductKey(), deviceName);
        mv.addObject("deviceOnline", deviceOnline);
        // Display actual topics device uses
        // Device subscribes to: /{productKey}/{deviceName}/user/get
        // Device publishes to: /{productKey}/{deviceName}/user/update
        mv.addObject("getTopic", "/" + appConfig.getProductKey() + "/" + deviceName + "/user/get");
        mv.addObject("updateTopic", "/" + appConfig.getProductKey() + "/" + deviceName + "/user/update");

        //MQTT CONNECT API
        String contextPath = HttpServletUtils.getRealContextpath();
        String url = (contextPath + "/api/iot/client/con?simUUID=&simMobile=&uuid=" + deviceName + "&deviceId=" + 0 + "&sign=");
        url += SignUtils.getSign(url);
        mv.addObject("connectUrl", url);

        //hardware version
        String key = "hardware:" + deviceName;
        BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);
        String hardware = (String) boundValueOps.get();
        mv.addObject("hardware", hardware);

        return mv;
    }

    @RequestMapping("/send")
    public HttpResult send(@RequestParam String deviceName, @RequestParam String data, HttpServletResponse response) throws Exception {
        HttpResult httpResult = new HttpResult();
        try {
            // FIX: Use the exact topic device is subscribed to
            // Device subscribes to: /powerbank/{deviceName}/user/get (with leading slash)
            String topic = "/" + appConfig.getProductKey() + "/" + deviceName + "/user/get";
            mqttPublisher.sendMsgAsync(appConfig.getProductKey(), topic, data, 1);
        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
        }
        return httpResult;
    }

    @RequestMapping("/check")
    public HttpResult check(@RequestParam String deviceName, HttpServletResponse response) throws Exception {
        HttpResult httpResult = new HttpResult();
        try {
            ReceiveUpload receiveUpload = deviceCommandUtils.check(deviceName);
            httpResult.setData(receiveUpload.getPowerbanks());
        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
        }
        return httpResult;
    }

    @RequestMapping("/check_all")
    public HttpResult checkAll(@RequestParam String deviceName, HttpServletResponse response) throws Exception {
        HttpResult httpResult = new HttpResult();
        try {
            ReceiveUpload receiveUpload = deviceCommandUtils.checkAll(deviceName);
            httpResult.setData(receiveUpload.getPowerbanks());
        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
        }
        return httpResult;
    }

    @RequestMapping("/popup_random")
    public HttpResult popupRandom(@RequestParam String deviceName, @RequestParam Integer minPower, HttpServletResponse response) throws Exception {
        HttpResult httpResult = new HttpResult();
        try {
            ReceivePopupSN receivePopupSN = deviceCommandUtils.popupByRandom(deviceName, minPower);
            if(receivePopupSN.getStatus() != 0x01){
                throw new Exception("Popup Error:" + ByteUtils.to16Hexs(receivePopupSN.getBytes()));
            }
            httpResult.setData(receivePopupSN.getSnAsString());
        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
        }
        return httpResult;
    }

    /**
     * Popup specific powerbank by SN (SYNC - 15s timeout)
     * 
     * @param rentboxSN  Station serial number
     * @param singleSN   Powerbank serial number to eject
     * @return Popup result with slot, powerbankSN, status, success
     */
    @RequestMapping("/popup_sn")
    public HttpResult popupBySn(
        @RequestParam String rentboxSN,
        @RequestParam String singleSN,
        HttpServletResponse response
    ) {
        HttpResult httpResult = new HttpResult();
        try {
            ReceivePopupSN result = deviceCommandUtils.popup(rentboxSN, singleSN);
            
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("slot", result.getPinboardIndex());
            data.put("powerbankSN", result.getSnAsString());
            data.put("status", result.getStatus());
            data.put("success", result.getStatus() == 0x01);
            
            httpResult.setData(data);
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_REQUEST_TIMEOUT);
            httpResult.setCode(408);
            httpResult.setMsg(e.getMessage());
        }
        return httpResult;
    }
}
