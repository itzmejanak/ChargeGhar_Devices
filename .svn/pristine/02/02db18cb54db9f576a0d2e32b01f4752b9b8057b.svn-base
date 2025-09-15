package com.demo.serialport;


public enum SerialPortError {
    UNKNOW(-1, "未知错误"),

    HEAD_CODE(100,"头码校验失败"),

    PACKAGE_SIZE(101,"包长度校验失败"),

    NOT1(102,"补码校验失败"),

    CMD(103, "命令不匹配");



    private final int code;
    private final String msg;

    SerialPortError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
