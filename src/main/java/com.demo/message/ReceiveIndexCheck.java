package com.demo.message;

import com.demo.serialport.SerialPortData;
import com.demo.serialport.SerialPortError;
import com.demo.serialport.SerialPortException;
import org.apache.commons.lang3.ArrayUtils;

public class ReceiveIndexCheck extends SerialPortData {

    private Powerbank powerbank;

    public ReceiveIndexCheck(byte[] bytes) throws SerialPortException {
        super(bytes);
        if (super.getCmd() != 0X28) {
            throw new SerialPortException(SerialPortError.CMD);
        }
        int[] data = getData();
        int[] powerBankData = ArrayUtils.subarray(data, 1, data.length);
        powerbank = new Powerbank(powerBankData, data[0], false, 0);
    }

    public Powerbank getPowerbank() {
        return powerbank;
    }
}
