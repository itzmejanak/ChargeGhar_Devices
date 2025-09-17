# Documentation for Directory: mqtt

**Path:** src/main/java/com.demo\mqtt

**Total Java files:** 3

**Output mode:** detailed

## Summary

- **Files:** 3
- **Classes:** 3
- **Methods:** 24

## Table of Contents

- [DeviceCommandUtils.java](#DeviceCommandUtils-java)
- [MqttPublisher.java](#MqttPublisher-java)
- [MqttSubscriber.java](#MqttSubscriber-java)

## DeviceCommandUtils.java

### Classes

#### DeviceCommandUtils

**Used in:**
- **controller\ListenController.java:**
  - Used in declaration: DeviceCommandUtils
- **controller\ShowController.java:**
  - Used in declaration: DeviceCommandUtils

### Properties (Getters/Setters)

**cache:**
- **getCache()**: Map

### Methods

#### `ReceiveUpload check(String rentboxSN)`

**Called in:**
- **controller\ShowController.java:**
  - Static call: deviceCommandUtils.check()
  - Instance call: deviceCommandUtils.check()
  - Direct call: check()
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: check()

#### `ReceiveUpload checkAll(String rentboxSN)`

**Called in:**
- **controller\ShowController.java:**
  - Instance call: deviceCommandUtils.checkAll()
  - Static call: deviceCommandUtils.checkAll()
  - Direct call: checkAll()
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: checkAll()

#### `ReceivePopupSN popup(String rentboxSN, String singleSN)`

**Called in:**
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: popup()

#### `ReceivePopupSN popupByRandom(String rentboxSN, int minPower)`

**Called in:**
- **controller\ShowController.java:**
  - Static call: deviceCommandUtils.popupByRandom()
  - Instance call: deviceCommandUtils.popupByRandom()
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: popupByRandom()

#### `byte sendPopupWait(String key, String rentboxSN, String message, int overSecond)`

**Called in:**
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: sendPopupWait()

#### `void checkOnlineStatus(String rentboxSN)`

**Called in:**
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: checkOnlineStatus()
  - Static call: this.checkOnlineStatus()

---

## MqttPublisher.java

### Classes

#### MqttPublisher

**Used in:**
- **controller\ApiController.java:**
  - Used in declaration: MqttPublisher
- **controller\IndexController.java:**
  - Used in declaration: MqttPublisher
- **controller\ListenController.java:**
  - Used in declaration: MqttPublisher
- **controller\ShowController.java:**
  - Used in declaration: MqttPublisher
- **mqtt\DeviceCommandUtils.java:**
  - Used in declaration: MqttPublisher

### Properties (Getters/Setters)

**devicestatusmap:**
- **getDeviceStatusMap()**: Map

### Methods

#### `void init()`

**Called in:**
- **mqtt\MqttPublisher.java:**
  - Direct call: init()
- **tools\HmacCoder.java:**
  - Static call: mac.init()
  - Instance call: mac.init()

#### `DeviceOnline getDeviceStatus(String productKey, String deviceName)`

**Called in:**
- **controller\ShowController.java:**
  - Instance call: mqttPublisher.getDeviceStatus()
  - Static call: mqttPublisher.getDeviceStatus()
- **mqtt\DeviceCommandUtils.java:**
  - Instance call: mqttPublisher.getDeviceStatus()
  - Static call: mqttPublisher.getDeviceStatus()
- **mqtt\MqttPublisher.java:**
  - Direct call: getDeviceStatus()

#### `void sendMsgAsync(String productKey, String topicFullName, String messageContent, int qos)`

**Called in:**
- **controller\ShowController.java:**
  - Instance call: mqttPublisher.sendMsgAsync()
  - Static call: mqttPublisher.sendMsgAsync()
- **mqtt\DeviceCommandUtils.java:**
  - Instance call: mqttPublisher.sendMsgAsync()
  - Static call: mqttPublisher.sendMsgAsync()
- **mqtt\MqttPublisher.java:**
  - Direct call: sendMsgAsync()

#### `void sendMsgAsync(String productKey, String topicFullName, byte bytes, int qos)`

**Called in:**
- **controller\ShowController.java:**
  - Instance call: mqttPublisher.sendMsgAsync()
  - Static call: mqttPublisher.sendMsgAsync()
- **mqtt\DeviceCommandUtils.java:**
  - Instance call: mqttPublisher.sendMsgAsync()
  - Static call: mqttPublisher.sendMsgAsync()
- **mqtt\MqttPublisher.java:**
  - Direct call: sendMsgAsync()

---

## MqttSubscriber.java

### Classes

#### MqttSubscriber

**Used in:**
- **controller\ApiController.java:**
  - Used in declaration: MqttSubscriber
- **controller\IndexController.java:**
  - Used in declaration: MqttSubscriber
- **controller\ListenController.java:**
  - Used in declaration: MqttSubscriber
- **controller\ShowController.java:**
  - Used in declaration: MqttSubscriber
- **controller\VersionController.java:**
  - Used in declaration: MqttSubscriber
- **mqtt\MqttPublisher.java:**
  - Used in declaration: MqttSubscriber

### Properties (Getters/Setters)

**exception:**
- **setException(Exception exception)**: void

**messagebodys:**
- **getMessageBodys()**: List

**running:**


### Methods

#### `void startQueue()`

**Called in:**
- **controller\ListenController.java:**
  - Static call: mnsUtils.startQueue()
  - Instance call: mnsUtils.startQueue()
  - Static call: mqttSubscriber.startQueue()
  - Instance call: mqttSubscriber.startQueue()
- **mqtt\MqttSubscriber.java:**
  - Direct call: startQueue()

#### `void stopQueue()`

**Called in:**
- **controller\ListenController.java:**
  - Instance call: mqttSubscriber.stopQueue()
  - Instance call: mnsUtils.stopQueue()
  - Static call: mqttSubscriber.stopQueue()
  - Static call: mnsUtils.stopQueue()
- **mqtt\MqttSubscriber.java:**
  - Direct call: stopQueue()

#### `void connectionLost(Throwable cause)`

**Called in:**
- **mqtt\MqttSubscriber.java:**
  - Direct call: connectionLost()

#### `void messageArrived(String topic, MqttMessage message)`

**Called in:**
- **mqtt\MqttSubscriber.java:**
  - Direct call: messageArrived()

#### `void deliveryComplete(IMqttDeliveryToken token)`

**Called in:**
- **mqtt\MqttSubscriber.java:**
  - Direct call: deliveryComplete()

#### `void handlerMessage(MessageBody messageBody)`

**Called in:**
- **mqtt\MqttSubscriber.java:**
  - Direct call: handlerMessage()

#### `void putMessageBody(MessageBody messageBody)`

**Called in:**
- **controller\ApiController.java:**
  - Instance call: mqttSubscriber.putMessageBody()
  - Static call: mqttSubscriber.putMessageBody()
- **controller\VersionController.java:**
  - Instance call: mqttSubscriber.putMessageBody()
  - Static call: mqttSubscriber.putMessageBody()
- **mqtt\MqttPublisher.java:**
  - Instance call: mqttSubscriber.putMessageBody()
  - Static call: mqttSubscriber.putMessageBody()
- **mqtt\MqttSubscriber.java:**
  - Direct call: putMessageBody()

#### `void clearMessageBody()`

**Called in:**
- **controller\ListenController.java:**
  - Instance call: mqttSubscriber.clearMessageBody()
  - Static call: mqttSubscriber.clearMessageBody()
- **mqtt\MqttSubscriber.java:**
  - Direct call: clearMessageBody()

#### `Exception getException()`

**Called in:**
- **controller\ListenController.java:**
  - Static call: mqttSubscriber.getException()
  - Instance call: mqttSubscriber.getException()
- **mqtt\MqttSubscriber.java:**
  - Direct call: getException()

---

