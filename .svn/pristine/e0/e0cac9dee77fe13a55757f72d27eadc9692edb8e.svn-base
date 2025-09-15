package com.demo.serialport;

import com.demo.tools.ByteUtils;
import org.apache.commons.lang3.ArrayUtils;

public class SerialPortData {
    private byte[] bytes;
    private int[] ints;
    private int head = 0XA8;
    private int[] length;
    private int cmd;
    private int[] data;
    private int not1;

    public static int checkCMD(byte[] bytes) {
        if(bytes == null || bytes.length < 5 ){
            return -1;
        }

        int cmd = ByteUtils.toUnsignedInt(bytes[3]);
        return cmd;
    }


    public SerialPortData(byte[] bytes) throws SerialPortException {
        this.bytes = bytes;
        this.ints = ByteUtils.toUnsignedInts(bytes);

        if(bytes.length < 5 ){
            throw new SerialPortException(SerialPortError.PACKAGE_SIZE);
        }

        if(ints[0] != head){
            throw new SerialPortException(SerialPortError.NOT1, (ByteUtils.to16Hex(head) + "!=" + ByteUtils.to16Hex(ints[0])));
        }

        this.length = new int[]{ints[1], ints[2]};
        if(ByteUtils.getJavaInt(this.length) != bytes.length){
            throw new SerialPortException(SerialPortError.PACKAGE_SIZE);
        }

        this.cmd = this.ints[3];
        this.data = ArrayUtils.subarray(this.ints, 4, bytes.length - 1);
        this.not1 = getNOT1Value(ArrayUtils.subarray(this.ints,0, bytes.length - 1));
        if(this.not1 != this.ints[bytes.length - 1]){
            //throw new SerialPortException(SerialPortError.NOT1);
        }

    }

    public SerialPortData(int cmd, int[] data) {
        this.cmd = cmd;
        this.data = data;
        this.length = ByteUtils.getMcuInt((data.length + 5), 2);   //头码+长度+补码=5

        this.ints = new int[data.length + 5];
        this.ints[0] = head;
        this.ints[1] = length[0];
        this.ints[2] = length[1];
        this.ints[3] = cmd;
        for(int i = 0; i< data.length; i ++){
            ints[i + 4] = data[i];
        }
        this.ints[ints.length - 1] = getNOT1Value(ints);
        this.bytes = ByteUtils.toBytes(ints);
    }

    /**
     * 取反+1
     *
     * @return
     */
    public static int getNOT1Value(int... datas) {
        int result = 0;
        for (int data : datas) {
            result += data;
        }
        result = ~result + 1;

        String hex = Integer.toHexString(result);
        result = ByteUtils.toUnsignedInt(hex.substring(hex.length() - 2));
        return result;
    }


    public byte[] getBytes() {
        return bytes;
    }

    public int getHead() {
        return head;
    }

    public int getCmd() {
        return cmd;
    }

    public int[] getData() {
        return data;
    }

    public int getNot1() {
        return not1;
    }

    public int[] getInts() {
        return ints;
    }

    public int[] getLength() {
        return length;
    }
}
