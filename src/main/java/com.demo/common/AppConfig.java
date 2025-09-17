package com.demo.common;

import com.demo.tools.HttpServletUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@Component
public class AppConfig {
    // EMQX Cloud Configuration
    @Value("#{appconfig['mqtt.broker']}")
    private String mqttBroker;

    @Value("#{appconfig['mqtt.port']}")
    private int mqttPort;

    @Value("#{appconfig['mqtt.username']}")
    private String mqttUsername;

    @Value("#{appconfig['mqtt.password']}")
    private String mqttPassword;

    @Value("#{appconfig['mqtt.clientId']}")
    private String mqttClientId;

    @Value("#{appconfig['mqtt.ssl']}")
    private boolean mqttSsl;

    // Keep productKey for device identification
    @Value("#{appconfig['productKey']}")
    private String productKey;

    @Value("#{appconfig['topicType']}")
    private boolean topicType;

    // EMQX API Configuration
    @Value("#{appconfig['emqx.api.url']}")
    private String emqxApiUrl;

    @Value("#{appconfig['emqx.api.key']}")
    private String emqxApiKey;

    @Value("#{appconfig['emqx.api.secret']}")
    private String emqxApiSecret;

    // EMQX Cloud Getters
    public String getMqttBroker() {
        return mqttBroker;
    }

    public int getMqttPort() {
        return mqttPort;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public boolean isMqttSsl() {
        return mqttSsl;
    }

    public String getProductKey() {
        return productKey;
    }

    public boolean isTopicType() {
        return topicType;
    }

    public String getEmqxApiUrl() {
        return emqxApiUrl;
    }

    public String getEmqxApiKey() {
        return emqxApiKey;
    }

    public String getEmqxApiSecret() {
        return emqxApiSecret;
    }

    public String[] getMachines(){
        String path = HttpServletUtils.getHttpServletRequest().getServletContext().
                getRealPath("/WEB-INF/classes/machines.properties");

        List<String> machines = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String str = null;
            while((str = bufferedReader.readLine()) != null){
                if(StringUtils.isNotBlank(str)){
                    machines.add(str);
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String[] data = machines.toArray(new String[machines.size()]);
        ArrayUtils.reverse(data);
        return data;
    }
}
