package com.demo.controller;

import com.demo.common.DeviceOnline;
import com.demo.mqtt.MqttPublisher;
import com.demo.mqtt.MqttSubscriber;
import com.demo.bean.DeviceInfo;
import com.demo.common.AppConfig;
import com.demo.common.HttpResult;
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
            String path = HttpServletUtils.getHttpServletRequest().getServletContext().
                    getRealPath("/WEB-INF/classes/machines.properties");

            FileOutputStream fos = null;
            OutputStreamWriter osw = null;
            BufferedWriter  bw = null;
            try {
                fos=new FileOutputStream(new File(path), true);
                osw=new OutputStreamWriter(fos, "UTF-8");
                bw=new BufferedWriter(osw);
                bw.write("\n\r" + deviceName);

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(bw != null) bw.close();
                if(osw != null) osw.close();
                if(fos != null) fos.close();;
            }

        }
        catch (Exception e){
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResult.setCode(response.getStatus());
            httpResult.setMsg(e.toString());
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
