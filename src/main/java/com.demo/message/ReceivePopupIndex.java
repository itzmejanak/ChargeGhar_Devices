package com.demo.message;


import com.demo.serialport.SerialPortData;
import com.demo.serialport.SerialPortError;
import com.demo.serialport.SerialPortException;

public class ReceivePopupIndex extends SerialPortData {
    private int pinboardIndex;
    private int powerbankIndex;
    private int status;

    public ReceivePopupIndex(byte[] bytes) throws SerialPortException {
        super(bytes);
        if(super.getCmd() != 0X21){
            throw new SerialPortException(SerialPortError.CMD);
        }

        int[] data = getData();

        this.pinboardIndex = data[0];
        this.powerbankIndex = data[1];
        this.status = data[2];
    }





}
