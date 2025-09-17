package com.demo.controller;

import com.demo.common.DeviceOnline;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import com.demo.bean.DeviceInfo;
import com.demo.common.AppConfig;
import com.demo.common.HttpResult;
import com.demo.emqx.EmqxDeviceService;
import com.demo.common.DeviceConfig;
import com.demo.tools.HttpServletUtils;
import com.demo.tools.SignUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@ResponseBody
@Controller
public class IndexController {
    @Autowired
    AppConfig appConfig;

    @Autowired
    MqttSubscriber mqttSubscriber;

    @Autowired
    MqttPublisher mqttPublisher;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    EmqxDeviceService emqxDeviceService;


    @RequestMapping("/index.html")
    public ModelAndView indexHtml() throws Exception {
        ModelAndView mv = new ModelAndView("/web/views/page/index");
        mv.addObject("data", appConfig);

        List<DeviceInfo> deviceInfos = new ArrayList<>();

        String[] machines = appConfig.getMachines();

        //online status
        Map<String, DeviceOnline> onlineMap = new HashMap<>();
        try {
            onlineMap = mqttPublisher.getDeviceStatusMap(appConfig.getProductKey(), machines);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //hardware version
        Set<String> hardwareKeys = redisTemplate.keys("hardware:*");
        List<String> hardwareValues = redisTemplate.opsForValue().multiGet(hardwareKeys);

        for(String machine : machines){
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceName(machine);
            deviceInfo.setDeviceOnline(onlineMap.get(machine));

            //hardware version
            int i = 0;
            for(String hardwareKey : hardwareKeys){
                if(hardwareKey.equals("hardware:" + machine)){
                    deviceInfo.setHardware(hardwareValues.get(i));
                    break;
                }
                i++;
            }

            //MQTT CONNECT API
            String contextPath = HttpServletUtils.getRealContextpath();
            String url = (contextPath + "/api/iot/client/con?simUUID=&simMobile=&uuid=" + machine + "&deviceId=" + 0 + "&sign=");
            url += SignUtils.getSign(url);
            deviceInfo.setConnectUrl(url);

            deviceInfos.add(deviceInfo);
        }


        mv.addObject("deviceInfos", deviceInfos);
        mv.addObject("mqtt", "mqtt://" + appConfig.getMqttBroker() + ":" + appConfig.getMqttPort());

        //MQTT BROKER INFO
        String mqttBroker = appConfig.getMqttBroker();
        mqttBroker = mqttBroker.substring(0, Math.min(5, mqttBroker.length()));
        mqttBroker = StringUtils.rightPad(mqttBroker, 30, "*");
        mv.addObject("mqttBroker", mqttBroker);

        //MQTT USERNAME INFO
        String mqttUsername = appConfig.getMqttUsername();
        mqttUsername = mqttUsername.substring(0, Math.min(5, mqttUsername.length()));
        mqttUsername = StringUtils.rightPad(mqttUsername, 30, "*");
        mv.addObject("mqttUsername", mqttUsername);
        return mv;
    }

    @RequestMapping("/device/create")
    public HttpResult deviceCreate(HttpServletResponse response,  @RequestParam String deviceName) throws Exception {
        HttpResult httpResult = new HttpResult();
        try {
            // Step 1: Register device with EMQX platform first
            DeviceConfig deviceConfig = null;
            try {
                deviceConfig = emqxDeviceService.getOrCreateDeviceConfig(deviceName);
                System.out.println("✅ Device registered with EMQX: " + deviceName);
                System.out.println("   Username: " + deviceConfig.getIotId());
                System.out.println("   Host: " + deviceConfig.getHost() + ":" + deviceConfig.getPort());
            } catch (Exception emqxError) {
                System.err.println("❌ EMQX device registration failed for " + deviceName + ": " + emqxError.getMessage());
                throw new Exception("Failed to register device with EMQX platform: " + emqxError.getMessage());
            }

            // Step 2: Add device to machines.properties file (only if EMQX registration succeeded)
            String path = HttpServletUtils.getHttpServletRequest().getServletContext().
                    getRealPath("/WEB-INF/classes/machines.properties");

            // Check if device already exists in file
            boolean deviceExists = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals(deviceName)) {
                        deviceExists = true;
                        break;
                    }
                }
            }

            if (!deviceExists) {
                FileOutputStream fos = null;
                OutputStreamWriter osw = null;
                BufferedWriter bw = null;
                try {
                    fos = new FileOutputStream(new File(path), true);
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    bw = new BufferedWriter(osw);
                    bw.write(System.lineSeparator() + deviceName);
                    System.out.println("✅ Device added to machines.properties: " + deviceName);
                } finally {
                    if (bw != null) bw.close();
                    if (osw != null) osw.close();
                    if (fos != null) fos.close();
                }
            } else {
                System.out.println("ℹ️ Device already exists in machines.properties: " + deviceName);
            }

            // Step 3: Return success with device information
            Map<String, Object> result = new HashMap<>();
            result.put("deviceName", deviceName);
            result.put("emqxRegistered", true);
            result.put("username", deviceConfig.getIotId());
            result.put("host", deviceConfig.getHost());
            result.put("port", deviceConfig.getPort());
            result.put("message", "Device created successfully and registered with EMQX platform");
            
            httpResult.setData(result);

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg("Device creation failed: " + e.getMessage());
            e.printStackTrace();
        }
        return httpResult;
    }

    @RequestMapping("/")
    public void root(HttpServletResponse response) throws IOException {
        String url = HttpServletUtils.getRealContextpath();
        url += "/index.html";
        response.sendRedirect(url);

    }
}
