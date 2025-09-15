package com.demo.message;


import com.demo.tools.ByteUtils;

/**
 * 充电宝
 */
public class Powerbank {
    private int[] data;
    private int pinboardIndex;
    private int index;
    private int status;
    private int undefined1;
    private int undefined2;
    private int area;
    private int[] sn;
    private int power;
    private int temp;
    private int voltage;
    private int current;
    private int softVersion;
    private int hardVersion;
    private int snAsInt;
    private String snAsString;
    private boolean putaway;               //系统入库
    private String message = "OK";         //故障说明

    public Powerbank(int[] data, int pinboardIndex) {
        this.data = data;
        this.pinboardIndex = pinboardIndex;
        this.index = data[0];
        this.status = data[1];
        this.undefined1 = data[2];
        this.undefined2 = data[3];
        this.area = data[4];
        this.sn = new int[]{data[5], data[6], data[7], data[8]};
        this.power = data[9];
        this.temp = data[10];
        this.voltage = data[11];
        this.current = data[12];
        this.softVersion = data[13];
        this.hardVersion = data[14];
        this.snAsInt = ByteUtils.getJavaInt(this.sn);
        this.snAsString = String.valueOf(this.snAsInt);

        if (status > 0X01) {
            message = "孔位异常：0X0" + status;
        }
        else if(snAsInt == 0){
            message = "NONE";
        }
        else if ((voltage < 45 || voltage > 55) && voltage != 255) {
            message = "电压异常，正常：4.5-5.5V";
        } else if ((temp < 20 || temp > 60) && temp != 255) {
            message = "温度异常，正常：20—60";
        }
        else if(!(snAsString.length() == 8 || snAsString.length() == 9 || snAsString.equals("0"))){
            message = "SN序列号错误";
        }
        else if((power < 0 || power > 100) && power != 255){
            message = "电量异常，正常：0—100";
        }
        else if(power >= 0 && power <= 80){
            if(current < 3 || current > 20){
                message = "电流异常，正常：0.0—0.3A";
            }
        }
        else if(power > 80 && power <= 10){
            if(current < 0 || current > 3){
                message = "电流异常，正常:0.3—2.0A";
            }
        }
    }

    public int[] getData() {
        return data;
    }

    public int getPinboardIndex() {
        return pinboardIndex;
    }

    /**
     * 0x01：第一孔位的移动电源信息
     * 0x02：第二孔位的移动电源信息
     * 0x0A：第十孔位的移动电源信息
     * 0x0F：第十五孔位的移动电源信息
     * 0x14：第二十孔位的移动电源信息
     * 0x00  &&  0x15~0xFF：当前孔位没有移动电源
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * 0x00：该孔位没有移动电源
     * 0x01：该移动电源正常工作
     * 0x02：该移动电源充电异常
     * 0x04：该孔位弹簧无法正常弹出移动电源
     * 0x05：该孔位弹簧被强制释放
     * 0x06：该孔位弹簧被强制释放且无通讯
     * 0x07~0x0FF：保留
     * @return
     */
    public int getStatus() {
        return status;
    }

    public int getUndefined1() {
        return undefined1;
    }

    public int getUndefined2() {
        return undefined2;
    }

    public int getArea() {
        return area;
    }

    /**
     * 柜机SN
     * @return
     */
    public int[] getSn() {
        return sn;
    }

    /**
     * 电量
     * @return
     */
    public int getPower() {
        if(power == 255){
            power = 0;
        }
        return power;
    }

    /**
     * 温度
     * @return
     */
    public int getTemp() {
        if(temp == 255){
            temp = 0;
        }
        return temp;
    }

    /**
     * 电压
     * @return
     */
    public int getVoltage() {
        if(voltage == 255){
            voltage = 0;
        }
        return voltage;
    }

    /**
     * 电流
     * @return
     */
    public int getCurrent() {
        if(current == 255){
            current = 0;
        }
        return current;
    }

    public int getSoftVersion() {
        return softVersion;
    }

    public int getHardVersion() {
        return hardVersion;
    }

    /**
     * 柜机SN
     * @return
     */
    public int getSnAsInt() {
        return snAsInt;
    }

    /**
     * 柜机SN
     * @return
     */
    public String getSnAsString() {
        return snAsString;
    }

    /**
     * 入库状态
     * @return
     */
    public boolean isPutaway() {
        return putaway;
    }

    public void setPutaway(boolean putaway) {
        this.putaway = putaway;
    }

    public String getMessage() {
        return message;
    }
}
