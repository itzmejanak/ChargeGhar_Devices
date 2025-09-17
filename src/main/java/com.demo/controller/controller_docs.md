# Documentation for Directory: controller

**Path:** src/main/java/com.demo\controller

**Total Java files:** 8

**Output mode:** detailed

## Summary

- **Files:** 8
- **Classes:** 8
- **Methods:** 37

## Table of Contents

- [ApiController.java](#ApiController-java)
- [EmqxTestController.java](#EmqxTestController-java)
- [IndexController.java](#IndexController-java)
- [ListenController.java](#ListenController-java)
- [ShowController.java](#ShowController-java)
- [TestController.java](#TestController-java)
- [VersionController.java](#VersionController-java)
- [WelcomeController.java](#WelcomeController-java)

## ApiController.java

### Classes

#### ApiController

*No usage found in codebase*

### Methods

#### `HttpResult iotClientCon(ApiIotClientConValid valid, HttpServletResponse response, HttpServletRequest request)`

**Called in:**
- **controller\ApiController.java:**
  - Direct call: iotClientCon()

#### `HttpResult deviceCreate(HttpServletResponse response, String deviceName)`

**Called in:**
- **controller\ApiController.java:**
  - Direct call: deviceCreate()
- **controller\IndexController.java:**
  - Direct call: deviceCreate()

#### `HttpResult powerbankReturn(ApiRentboxOrderReturnValid valid, HttpServletResponse response)`

**Called in:**
- **controller\ApiController.java:**
  - Direct call: powerbankReturn()

#### `HttpResult rentboxOrderReturnEnd(byte bytes, String rentboxSN, String sign, String signal, HttpServletResponse response)`

**Called in:**
- **controller\ApiController.java:**
  - Direct call: rentboxOrderReturnEnd()

#### `void checkSign(Object valid, String sign)`

**Called in:**
- **controller\ApiController.java:**
  - Static call: this.checkSign()
  - Direct call: checkSign()
- **controller\VersionController.java:**
  - Static call: this.checkSign()
  - Direct call: checkSign()

#### `HttpResult rentboxConfigData()`

**Called in:**
- **controller\ApiController.java:**
  - Direct call: rentboxConfigData()

---

## EmqxTestController.java

### Classes

#### EmqxTestController

*No usage found in codebase*

### Properties (Getters/Setters)

**devicecredentials:**
- **getDeviceCredentials()**: Map

### Methods

#### `Map testConnection()`

**Called in:**
- **controller\EmqxTestController.java:**
  - Direct call: testConnection()
  - Static call: emqxApiClient.testConnection()
  - Instance call: emqxApiClient.testConnection()
- **emqx\EmqxApiClient.java:**
  - Direct call: testConnection()

#### `Map testDeviceRegistration(String deviceName)`

**Called in:**
- **controller\EmqxTestController.java:**
  - Direct call: testDeviceRegistration()

#### `Map testPublish(String topic, String message, int qos)`

**Called in:**
- **controller\EmqxTestController.java:**
  - Direct call: testPublish()

#### `Map removeDevice(String deviceName)`

**Called in:**
- **controller\EmqxTestController.java:**
  - Direct call: removeDevice()
  - Instance call: emqxDeviceService.removeDevice()
  - Static call: emqxDeviceService.removeDevice()
- **emqx\EmqxDeviceService.java:**
  - Direct call: removeDevice()

---

## IndexController.java

### Classes

#### IndexController

*No usage found in codebase*

### Methods

#### `ModelAndView indexHtml()`

**Called in:**
- **controller\IndexController.java:**
  - Direct call: indexHtml()

#### `HttpResult deviceCreate(HttpServletResponse response, String deviceName)`

**Called in:**
- **controller\ApiController.java:**
  - Direct call: deviceCreate()
- **controller\IndexController.java:**
  - Direct call: deviceCreate()

#### `void root(HttpServletResponse response)`

**Called in:**
- **controller\IndexController.java:**
  - Direct call: root()

---

## ListenController.java

### Classes

#### ListenController

*No usage found in codebase*

### Methods

#### `ModelAndView listenHtml()`

**Called in:**
- **controller\ListenController.java:**
  - Direct call: listenHtml()

#### `HttpResult listen()`

**Called in:**
- **controller\ListenController.java:**
  - Direct call: listen()

#### `HttpResult listenStart(HttpServletResponse response)`

**Called in:**
- **controller\ListenController.java:**
  - Direct call: listenStart()

#### `HttpResult listenStop(HttpServletResponse response)`

**Called in:**
- **controller\ListenController.java:**
  - Direct call: listenStop()

#### `HttpResult listenClear(HttpServletResponse response)`

**Called in:**
- **controller\ListenController.java:**
  - Direct call: listenClear()

#### `HttpResult listen0x10(String hexs, HttpServletResponse response)`

**Called in:**
- **controller\ListenController.java:**
  - Direct call: listen0x10()

---

## ShowController.java

### Classes

#### ShowController

*No usage found in codebase*

### Methods

#### `ModelAndView showHtml(String deviceName)`

**Called in:**
- **controller\ShowController.java:**
  - Direct call: showHtml()

#### `HttpResult send(String deviceName, String data, HttpServletResponse response)`

**Called in:**
- **controller\ShowController.java:**
  - Direct call: send()

#### `HttpResult check(String deviceName, HttpServletResponse response)`

**Called in:**
- **controller\ShowController.java:**
  - Direct call: check()
  - Static call: deviceCommandUtils.check()
  - Instance call: deviceCommandUtils.check()
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: check()

#### `HttpResult checkAll(String deviceName, HttpServletResponse response)`

**Called in:**
- **controller\ShowController.java:**
  - Direct call: checkAll()
  - Static call: deviceCommandUtils.checkAll()
  - Instance call: deviceCommandUtils.checkAll()
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: checkAll()

#### `HttpResult checkAll(String deviceName, Integer minPower, HttpServletResponse response)`

**Called in:**
- **controller\ShowController.java:**
  - Direct call: checkAll()
  - Static call: deviceCommandUtils.checkAll()
  - Instance call: deviceCommandUtils.checkAll()
- **mqtt\DeviceCommandUtils.java:**
  - Direct call: checkAll()

---

## TestController.java

### Classes

#### TestController

*No usage found in codebase*

### Methods

#### `Map test()`

**Called in:**
- **controller\TestController.java:**
  - Direct call: test()

#### `Map health()`

**Called in:**
- **controller\TestController.java:**
  - Direct call: health()

---

## VersionController.java

### Classes

#### VersionController

*No usage found in codebase*

### Methods

#### `ModelAndView versionHtml()`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: versionHtml()

#### `VersionInfo getVersionInfo()`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: getVersionInfo()

#### `HttpResult versionUpdate(VersionInfo versionInfo, HttpServletResponse response)`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: versionUpdate()

#### `HttpResult versionClear(HttpServletResponse response)`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: versionClear()

#### `HttpResult iotAppVersion(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: iotAppVersion()

#### `HttpResult iotAppVersionTest(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: iotAppVersionTest()

#### `HttpResult iotAppVersionPublichMcu(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: iotAppVersionPublichMcu()

#### `HttpResult iotAppVersionTestMcu(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`

**Called in:**
- **controller\VersionController.java:**
  - Direct call: iotAppVersionTestMcu()

#### `void checkSign(Object valid, String sign)`

**Called in:**
- **controller\ApiController.java:**
  - Static call: this.checkSign()
  - Direct call: checkSign()
- **controller\VersionController.java:**
  - Static call: this.checkSign()
  - Direct call: checkSign()

---

## WelcomeController.java

### Classes

#### WelcomeController

*No usage found in codebase*

### Methods

#### `ModelAndView welcome()`

**Called in:**
- **controller\WelcomeController.java:**
  - Direct call: welcome()

---

