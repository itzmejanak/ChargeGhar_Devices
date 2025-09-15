//package com.demo.aliyun;
//
//
//import com.demo.common.AppConfig;
//import com.demo.serialport.SerialPortData;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.*;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//
//@Component
//public class MnsUtils {
//    public static final int MAX_VECHOR_LENGTH = 100;
//    @Autowired
//    private AppConfig appConfig;
//
//    @Autowired
//    RentboxUtils rentboxUtils;
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    private MnsThread mnsThread;
//
//    private Exception exception;
//
//    public void startQueue(){
//        if(mnsThread != null) {
//            return;
//        }
//
//        mnsThread = new MnsThread(appConfig, this);
//        mnsThread.setDaemon(true);
//        mnsThread.start();
//    }
//
//    public void stopQueue(){
//        if(mnsThread == null){
//            return;
//        }
//        mnsThread.interrupt();
//        mnsThread = null;
//    }
//
//    //以后需要用户自己实现接收逻辑
//    public void handlerMessage(MessageBody messageBody) {
//        //PUT REDIS
//        putMessageBody(messageBody);
//
//        String type = messageBody.getMessageType();
//        if ("upload".equals(type)) {
//            int cmd = SerialPortData.checkCMD(messageBody.getPayloadAsBytes());
//            switch (cmd) {
//                case 0x10:  //check  check_all
//                    String key = "check:" + messageBody.getDeviceName();
//                    BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);
//                    long time = boundValueOps.getExpire();
//                    if (time <= 0) {
//                        break;
//                    }
//                    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
//                    break;
//                case 0x31: //popup by sn
//                    key = "popup_sn:" + messageBody.getDeviceName();
//                    boundValueOps = redisTemplate.boundValueOps(key);
//                    time = boundValueOps.getExpire();
//                    if (time <= 0) {
//                        break;
//                    }
//                    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
//                    break;
//            }
//        }
//    }
//
//    public Exception getException() {
//        return exception;
//    }
//
//    public void setException(Exception exception) {
//        this.exception = exception;
//    }
//
//    public boolean isRunning(){
//        if(mnsThread == null){
//            return false;
//        }
//        return true;
//    }
//
//    public void putMessageBody(MessageBody messageBody){
//        String key = "messageBody:" + System.currentTimeMillis();
//        BoundListOperations boundListOps = redisTemplate.boundListOps("messageBody");
//        boundListOps.leftPush(messageBody);
//        boundListOps.expire(1, TimeUnit.HOURS);
//    }
//
//    public void clearMessageBody(){
//        String key = "messageBody:" + System.currentTimeMillis();
//        BoundListOperations boundListOps = redisTemplate.boundListOps("messageBody");
//        boundListOps.expire(-2, TimeUnit.HOURS);
//    }
//
//    public List<MessageBody> getMessageBodys(){
//        List<MessageBody> messages = redisTemplate.opsForList().range("messageBody", 0 , 1000);
//        return messages;
//    }
//}
