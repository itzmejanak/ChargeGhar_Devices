package com.demo.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

public class MessageBody implements Serializable {
    private static final long serialVersionUID = 4359709211352400087L;

    private String payload;
    private String messageType;
    private String messageId;
    private String topic;
    private long timestamp;
    private String productKey;
    private String deviceName;

    public String getPayload() {
        return payload;
    }

    public String getPayloadAsString(){
        String data = new String(Base64.decodeBase64(getPayload()));
        return data;
    }

    public byte[] getPayloadAsBytes(){
        byte[] data = null;
        data = Base64.decodeBase64(getPayload());
        return data;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMessageType() {
        return messageType;
    }

    @JsonProperty(value="messagetype")
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    @JsonProperty(value="messageid")
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;

        String[] arr = topic.split("/");
        if(arr.length < 3){
            return;
        }
        productKey = arr[1];
        deviceName = arr[2];
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}