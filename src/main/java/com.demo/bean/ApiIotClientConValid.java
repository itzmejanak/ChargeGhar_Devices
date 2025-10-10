package com.demo.bean;

public class ApiIotClientConValid {
    private String uuid;
    private int deviceId;
    private String simUUID;
    private String simMobile;
    private String sign;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getSimUUID() {
        return simUUID;
    }

    public void setSimUUID(String simUUID) {
        this.simUUID = simUUID;
    }

    public String getSimMobile() {
        return simMobile;
    }

    public void setSimMobile(String simMobile) {
        this.simMobile = simMobile;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
