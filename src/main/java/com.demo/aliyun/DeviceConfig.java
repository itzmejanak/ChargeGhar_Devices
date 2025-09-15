package com.demo.aliyun;

import java.io.Serializable;
import java.util.Date;

public class DeviceConfig implements Serializable {
    private static final long serialVersionUID = 4359709211352400086L;

    private String sn;
    private String productKey;
    private String deviceName;
    private String deviceSecret;
    public String host;
    public int port;
    private String iotId;
    private String iotToken;
    private Date createdTime;
    private Date overdueTime;
    private String timeStamp;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIotId() {
        return iotId;
    }

    public void setIotId(String iotId) {
        this.iotId = iotId;
    }

    public String getIotToken() {
        return iotToken;
    }

    public void setIotToken(String iotToken) {
        this.iotToken = iotToken;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getOverdueTime() {
        return overdueTime;
    }

    public void setOverdueTime(Date overdueTime) {
        this.overdueTime = overdueTime;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
