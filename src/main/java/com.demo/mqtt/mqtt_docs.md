# Documentation for Directory: mqtt

**Path:** src/main/java/com.demo\mqtt

**Total Java files:** 3

**Output mode:** brief

## Summary

- **Files:** 3
- **Classes:** 3
- **Methods:** 24

## DeviceCommandUtils.java

### Classes

#### DeviceCommandUtils

**Used in:**
- controller\ListenController.java
- controller\ShowController.java

### Methods

- `ReceiveUpload check(String rentboxSN)`
- `ReceiveUpload checkAll(String rentboxSN)`
- `ReceivePopupSN popup(String rentboxSN, String singleSN)`
- `ReceivePopupSN popupByRandom(String rentboxSN, int minPower)`
- `byte sendPopupWait(String key, String rentboxSN, String message, int overSecond)`
- `void checkOnlineStatus(String rentboxSN)`
## MqttPublisher.java

### Classes

#### MqttPublisher

**Used in:**
- controller\ApiController.java
- controller\IndexController.java
- controller\ListenController.java
- controller\ShowController.java
- mqtt\DeviceCommandUtils.java

### Methods

- `void init()`
- `DeviceOnline getDeviceStatus(String productKey, String deviceName)`
- `void sendMsgAsync(String productKey, String topicFullName, String messageContent, int qos)`
- `void sendMsgAsync(String productKey, String topicFullName, byte bytes, int qos)`
## MqttSubscriber.java

### Classes

#### MqttSubscriber

**Used in:**
- controller\ApiController.java
- controller\IndexController.java
- controller\ListenController.java
- controller\ShowController.java
- controller\VersionController.java
- mqtt\MqttPublisher.java

### Methods

- `void startQueue()`
- `void stopQueue()`
- `void connectionLost(Throwable cause)`
- `void messageArrived(String topic, MqttMessage message)`
- `void deliveryComplete(IMqttDeliveryToken token)`
- `void handlerMessage(MessageBody messageBody)`
- `void putMessageBody(MessageBody messageBody)`
- `void clearMessageBody()`
- `Exception getException()`
