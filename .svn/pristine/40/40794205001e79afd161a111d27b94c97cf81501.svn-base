package com.demo.serialport;


public class SerialPortException extends Exception {
    private SerialPortError serialPortError;

    public SerialPortException(SerialPortError serialPortError) {
        super(serialPortError.getMsg());
        this.serialPortError = serialPortError;
    }

    public SerialPortException(SerialPortError serialPortError, String message) {
        super(message);
        this.serialPortError = serialPortError;
    }

    public SerialPortError getSerialPortError(){
        return serialPortError;
    }

}
