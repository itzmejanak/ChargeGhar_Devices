package com.demo.tools;



import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ByteUtils {
    /**
     * 无符号byte
     *
     * @param data
     * @return
     */
    public static int toUnsignedInt(String data) {
        data = StringUtils.deleteWhitespace(data);
        return Integer.parseInt(data, 16);
    }

    /**
     * 无符号byte
     *
     * @param data
     * @return
     */
    public static int toUnsignedInt(byte data) {
        return data & 0xff;
    }

    /**
     * 无符号byte
     *
     * @param datas
     * @return
     */
    public static int[] toUnsignedInts(byte... datas) {
        int[] result = new int[datas.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = toUnsignedInt(datas[i]);
        }
        return result;
    }

    /**
     * 无符号byte
     *
     * @param datas
     * @return
     */
    public static int[] toUnsignedInts(String... datas) {
        int[] result = new int[datas.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = toUnsignedInt(datas[i]);
        }
        return result;
    }

    /**
     * 有符号byte
     *
     * @param data
     * @return
     */
    public static byte toByte(int data) {
        return (byte) data;
    }

    public static byte toByte(String hex){
        int data = toUnsignedInt(hex);
        return toByte(data);
    }

    /**
     * 有符号byte
     *
     * @param datas
     * @return
     */
    public static byte[] toBytes(int... datas) {
        byte[] result = new byte[datas.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) datas[i];
        }
        return result;
    }

    public static byte[] toBytes(String hexs){
        String[] arr = hexs.split(" ");
        int[] data = toUnsignedInts(arr);
        return toBytes(data);
    }

    public static String to16Hex(Byte data){
        String hex = Integer.toHexString(toUnsignedInt(data));
        hex = StringUtils.leftPad(hex,2,"0").toUpperCase();
        return hex;
    }

    public static String to16Hex(Integer data){
        String hex = Integer.toHexString(data);
        hex = StringUtils.leftPad(hex,2,"0").toUpperCase();
        return hex;
    }

    public static String to16Hexs(Byte... datas){
        if(datas.length == 0){
            return null;
        }

        List<String> list = new ArrayList<String>();
        for(Byte b : datas){
            list.add(to16Hex(b));
        }
        return StringUtils.join(list, " ");
    }

    public static String to16Hexs(byte... datas){
        return to16Hexs(ArrayUtils.toObject(datas));
    }



    public static String to16Hexs(Integer... datas){
        if(datas.length == 0){
            return null;
        }

        List<String> list = new ArrayList<String>();
        for(Integer i : datas){
            list.add(to16Hex(i));
        }

        return StringUtils.join(list, " ");
    }

    public static String to16Hexs(int... datas){
        return to16Hexs(ArrayUtils.toObject(datas));
    }

    /**
     * MCU长度为2个字节
     * @param data
     * @return
     */
    public static int[] getMcuInt(int data, int length){
        String hex = ByteUtils.to16Hex(data);
        hex = StringUtils.leftPad(hex, length * 2, "0");

        int[] value = new int[length];
        for(int i = 0; i < length; i ++){
            value[i] = ByteUtils.toUnsignedInt(hex.substring(i * 2, (i + 1) * 2));
        }
        return value;
    }

    public static int getJavaInt(int[] data){
        String hexs = ByteUtils.to16Hexs(data);
        int value = ByteUtils.toUnsignedInt(hexs);
        return value;
    }

    public static int getJavaInt(byte[] data){
        return getJavaInt(toUnsignedInts(data));
    }

}
