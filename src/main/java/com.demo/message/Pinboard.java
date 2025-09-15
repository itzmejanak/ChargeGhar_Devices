package com.demo.message;

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
}
