package com.demo.controller;

import com.demo.common.DeviceOnline;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import com.demo.bean.DeviceInfo;
import com.demo.common.AppConfig;
import com.demo.common.HttpResult;
import com.demo.emqx.EmqxDeviceService;
import com.demo.common.DeviceConfig;
import com.demo.model.Device;
import com.demo.service.AdminUserService;
import com.demo.service.DeviceService;
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

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AdminUserService adminUserService;


    @RequestMapping("/index.html")
    public ModelAndView indexHtml() throws Exception {
        ModelAndView mv = new ModelAndView("/web/views/page/index");
        mv.addObject("data", appConfig);

        List<DeviceInfo> deviceInfos = new ArrayList<>();

        // Get devices from database instead of properties file
        List<String> machinesList = deviceService.getDeviceNames();
        String[] machines = machinesList.toArray(new String[0]);

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
    public HttpResult deviceCreate(HttpServletRequest request, HttpServletResponse response, 
                                   @RequestParam String deviceName, 
                                   @RequestParam(required = false) String imei) throws Exception {
        
        if (StringUtils.isBlank(deviceName)) {
            return HttpResult.error("Device name cannot be empty");
        }

        try {
            // Get admin user ID from request (if authenticated)
            Integer adminId = (Integer) request.getAttribute("userId");
            
            System.out.println("========================================");
            System.out.println("DEVICE REGISTRATION REQUEST");
            System.out.println("Device Name: " + deviceName);
            System.out.println("IMEI: " + imei);
            System.out.println("Requested by Admin ID: " + adminId);
            System.out.println("========================================");

            // Step 1: Register device with EMQX platform and get generated password
            DeviceConfig deviceConfig = emqxDeviceService.getOrCreateDeviceConfig(deviceName);
            String generatedPassword = deviceConfig.getIotToken();
            
            System.out.println("✅ Device registered with EMQX: " + deviceName);
            System.out.println("   Username: " + deviceConfig.getIotId());
            System.out.println("   Host: " + deviceConfig.getHost() + ":" + deviceConfig.getPort());

            // Step 2: Check if device exists in database
            Device existingDevice = deviceService.getDeviceByName(deviceName);
            
            if (existingDevice != null) {
                // Device exists in database, update password to match EMQX
                boolean passwordUpdated = deviceService.updateDevicePassword(deviceName, generatedPassword);
                if (passwordUpdated) {
                    System.out.println("✅ Device password updated in database: " + deviceName);
                } else {
                    System.out.println("⚠️ Failed to update device password in database: " + deviceName);
                }
                
                // Update IMEI if provided and different
                if (imei != null && !imei.equals(existingDevice.getImei())) {
                    existingDevice.setImei(imei);
                    deviceService.updateDevice(existingDevice);
                    System.out.println("✅ Device IMEI updated: " + deviceName);
                }
                
            } else {
                // Device doesn't exist in database, create new record
                Device device = new Device();
                device.setDeviceName(deviceName);
                device.setImei(imei);
                device.setPassword(generatedPassword);
                device.setCreatedBy(adminId);

                Device created = deviceService.createDevice(device, adminId);
                if (created == null) {
                    return HttpResult.error("Failed to save device to database");
                }

                System.out.println("✅ Device saved to database: " + deviceName);
            }
            
            System.out.println("========================================");

            // Step 3: Return success with device information
            Map<String, Object> result = new HashMap<>();
            result.put("deviceName", deviceName);
            result.put("imei", imei);
            result.put("password", generatedPassword);
            result.put("emqxRegistered", true);
            result.put("username", deviceConfig.getIotId());
            result.put("host", deviceConfig.getHost());
            result.put("port", deviceConfig.getPort());
            result.put("message", "Device created successfully and registered with EMQX platform");
            
            return HttpResult.ok(result);

        } catch (Exception e) {
            System.out.println("❌ Error during device creation: " + e.getMessage());
            e.printStackTrace();
            return HttpResult.error("Device creation failed: " + e.getMessage());
        }
    }

    @RequestMapping("/device/delete")
    public HttpResult deviceDelete(HttpServletRequest request, @RequestParam String deviceName) throws Exception {
        if (StringUtils.isBlank(deviceName)) {
            return HttpResult.error("Device name cannot be empty");
        }

        try {
            // Get admin user ID from request (if authenticated)
            Integer adminId = (Integer) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");
            
            System.out.println("========================================");
            System.out.println("DEVICE DELETION REQUEST");
            System.out.println("Device Name: " + deviceName);
            System.out.println("Requested by Admin ID: " + adminId + " (Role: " + role + ")");
            System.out.println("========================================");

            // Step 1: Check if device exists in database
            Device device = deviceService.getDeviceByName(deviceName);
            if (device == null) {
                return HttpResult.error("Device not found: " + deviceName);
            }

            // Step 2: Remove device from EMQX platform
            try {
                boolean emqxRemoved = emqxDeviceService.removeDevice(deviceName);
                if (emqxRemoved) {
                    System.out.println("✅ Device removed from EMQX: " + deviceName);
                } else {
                    System.out.println("⚠️ Failed to remove device from EMQX: " + deviceName);
                }
            } catch (Exception emqxError) {
                System.err.println("⚠️ EMQX removal failed (continuing anyway): " + emqxError.getMessage());
            }

            // Step 3: Delete device from database
            boolean deleted = deviceService.deleteDevice(device.getId());
            if (!deleted) {
                return HttpResult.error("Failed to delete device from database");
            }

            System.out.println("✅ Device deleted successfully: " + deviceName);
            System.out.println("========================================");

            return HttpResult.ok("Device deleted successfully");

        } catch (Exception e) {
            System.out.println("❌ Error during device deletion: " + e.getMessage());
            e.printStackTrace();
            return HttpResult.error("Device deletion failed: " + e.getMessage());
        }
    }

    @RequestMapping("/admin/panel")
    public ModelAndView adminPanel() throws Exception {
        ModelAndView mv = new ModelAndView("/web/views/page/admin");
        return mv;
    }

    @RequestMapping("/api/admin/statistics")
    public HttpResult getAdminStatistics(HttpServletRequest request) {
        try {
            // Get admin user ID from request (if authenticated)
            Integer adminId = (Integer) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");
            
            if (adminId == null) {
                return HttpResult.error("Authentication required");
            }

            // Get statistics from services
            Map<String, Object> deviceStats = deviceService.getDeviceStatistics();
            Map<String, Object> adminStats = adminUserService.getAdminStatistics();
            
            // Combine statistics
            Map<String, Object> allStats = new HashMap<>();
            allStats.putAll(deviceStats);
            allStats.putAll(adminStats);
            
            // Add system info
            allStats.put("systemStatus", "Online");
            allStats.put("currentUser", adminId);
            allStats.put("currentUserRole", role);
            
            return HttpResult.ok(allStats);

        } catch (Exception e) {
            System.out.println("❌ Error getting statistics: " + e.getMessage());
            e.printStackTrace();
            return HttpResult.error("Failed to get statistics: " + e.getMessage());
        }
    }

    @RequestMapping("/")
    public void root(HttpServletResponse response) throws IOException {
        String url = HttpServletUtils.getRealContextpath();
        url += "/index.html";
        response.sendRedirect(url);

    }
}
