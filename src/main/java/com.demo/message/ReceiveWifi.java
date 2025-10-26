package com.demo.message;

import com.demo.serialport.SerialPortData;
import com.demo.serialport.SerialPortError;
import com.demo.serialport.SerialPortException;
import com.demo.tools.ByteUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WiFi scanning message (0xCF command)
 */
public class ReceiveWifi extends SerialPortData {

    private List<String> name;

    public ReceiveWifi(byte[] bytes) throws SerialPortException {
        super(bytes);
        if (super.getCmd() != 0XCF) {
            throw new SerialPortException(SerialPortError.CMD);
        }
        int[] data = getData();
        String wifiStr = new String(ByteUtils.toBytes(data));
        String nameStr = wifiStr.substring(1, wifiStr.length() - 1);
        String[] nameArray = nameStr.split(",");
        name = Arrays.stream(nameArray).filter(element -> element.startsWith("\"") && element.endsWith("\"")).map(element -> element.substring(1, element.length() - 1)).collect(Collectors.toList());
    }

    public ReceiveWifi(int cmd, int[] data) {
        super(cmd, data);
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }
}
