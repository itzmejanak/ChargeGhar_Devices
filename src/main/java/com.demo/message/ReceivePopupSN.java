package com.demo.message;


import com.demo.serialport.SerialPortData;
import com.demo.serialport.SerialPortError;
import com.demo.serialport.SerialPortException;
import com.demo.tools.ByteUtils;

public class ReceivePopupSN extends SerialPortData {
    private int pinboardIndex;
    private int[] sn;
    private int status;
    private int snAsInt;
    private String snAsString;

    public ReceivePopupSN(byte[] bytes) throws SerialPortException {
        super(bytes);
        if(super.getCmd() != 0X31){
            throw new SerialPortException(SerialPortError.CMD);
        }

        int[] data = getData();

        pinboardIndex = data[0];
        sn = new int[]{data[1], data[2], data[3], data[4]};
        snAsInt = ByteUtils.getJavaInt(sn);
        snAsString = String.valueOf(snAsInt);
        status = data[5];
    }

    public int getSnAsInt() {
        return snAsInt;
    }

    public String getSnAsString() {
        return snAsString;
    }

    public int getStatus() {
        return status;
    }

    public int getPinboardIndex() {
        return pinboardIndex;
    }
}
