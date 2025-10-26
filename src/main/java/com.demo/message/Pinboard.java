package com.demo.message;

import com.demo.tools.ByteUtils;

/**
 * 机芯数据
 */
public class Pinboard {
    private int[] data;

    private int index;

    private int undefined1;

    private int undefined2;

    private int temp;

    private int softVersion;

    private int hardVersion;

    public Pinboard(int[] data) {
        this.data = data;

        index = data[0];
        undefined1 = data[1];
        undefined2 = data[2];
        temp = data[3];
        softVersion = data[4];
        hardVersion = data[5];
    }

    public int[] getData() {
        return data;
    }

    public int getIndex() {
        return index;
    }

    public int getUndefined1() {
        return undefined1;
    }

    public int getUndefined2() {
        return undefined2;
    }

    public int getTemp() {
        return temp;
    }

    public int getSoftVersion() {
        return softVersion;
    }

    public int getHardVersion() {
        return hardVersion;
    }

    public int getIo() {
        //每个串口上只有一个转接板，每个转接板上都只有5个机芯
        int ioHex = Integer.parseInt(ByteUtils.to16Hex(data[0]));
        return ioHex / 50;
    }
}
