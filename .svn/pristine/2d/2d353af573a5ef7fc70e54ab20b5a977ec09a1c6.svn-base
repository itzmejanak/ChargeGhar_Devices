package com.demo.message;


import com.demo.serialport.SerialPortData;
import com.demo.serialport.SerialPortError;
import com.demo.serialport.SerialPortException;
import com.demo.tools.ByteUtils;

public class ReceiveReturn extends SerialPortData {
    private int pinboardIndex;
    private int hole;
    private int area;
    private int[] sn;
    private int status;
    private int snAsInt;
    private String snAsString;
    private int version;


    public ReceiveReturn(byte[] bytes) throws SerialPortException {
        super(bytes);
        if(super.getCmd() != 0X40){
            throw new SerialPortException(SerialPortError.CMD);
        }
        int[] data = getData();

        pinboardIndex = data[0];
        hole = data[1];
        area = data[2];

        sn = new int[]{data[3], data[4], data[5], data[6]};
        snAsInt = ByteUtils.getJavaInt(sn);
        snAsString = String.valueOf(snAsInt);
        status = data[7];

        if(bytes.length == 14){
            version = data[8];
        }
    }
}
