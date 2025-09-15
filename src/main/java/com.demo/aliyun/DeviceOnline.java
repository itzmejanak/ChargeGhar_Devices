package com.demo.aliyun;

public enum DeviceOnline {
    UNACTIVE, ONLINE, OFFLINE, NO_DEVICE;

    public static DeviceOnline getByName(String name){
        DeviceOnline status = NO_DEVICE;
        try{
            status = DeviceOnline.valueOf(name);
        }catch (Exception e){
            status  = NO_DEVICE;
        }
        return status;
    }
}
