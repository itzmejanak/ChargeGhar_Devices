package com.demo.message;

import com.demo.serialport.SerialPortData;
import org.apache.commons.lang3.ArrayUtils;

public class SendPopupSN extends SerialPortData {
    public SendPopupSN(int pinboardIndex, int[] powerbankSN) {
        super(0X30, ArrayUtils.addAll(new int[]{pinboardIndex}, powerbankSN));
    }
}
