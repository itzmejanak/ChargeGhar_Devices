# Documentation for Directory: controller

**Path:** src/main/java/com.demo\controller

**Total Java files:** 8

**Output mode:** brief

## Summary

- **Files:** 8
- **Classes:** 8
- **Methods:** 37

## ApiController.java

### Classes

#### ApiController

### Methods

- `HttpResult iotClientCon(ApiIotClientConValid valid, HttpServletResponse response, HttpServletRequest request)`
- `HttpResult deviceCreate(HttpServletResponse response, String deviceName)`
- `HttpResult powerbankReturn(ApiRentboxOrderReturnValid valid, HttpServletResponse response)`
- `HttpResult rentboxOrderReturnEnd(byte bytes, String rentboxSN, String sign, String signal, HttpServletResponse response)`
- `void checkSign(Object valid, String sign)`
- `HttpResult rentboxConfigData()`
## EmqxTestController.java

### Classes

#### EmqxTestController

### Methods

- `Map testConnection()`
- `Map testDeviceRegistration(String deviceName)`
- `Map testPublish(String topic, String message, int qos)`
- `Map removeDevice(String deviceName)`
## IndexController.java

### Classes

#### IndexController

### Methods

- `ModelAndView indexHtml()`
- `HttpResult deviceCreate(HttpServletResponse response, String deviceName)`
- `void root(HttpServletResponse response)`
## ListenController.java

### Classes

#### ListenController

### Methods

- `ModelAndView listenHtml()`
- `HttpResult listen()`
- `HttpResult listenStart(HttpServletResponse response)`
- `HttpResult listenStop(HttpServletResponse response)`
- `HttpResult listenClear(HttpServletResponse response)`
- `HttpResult listen0x10(String hexs, HttpServletResponse response)`
## ShowController.java

### Classes

#### ShowController

### Methods

- `ModelAndView showHtml(String deviceName)`
- `HttpResult send(String deviceName, String data, HttpServletResponse response)`
- `HttpResult check(String deviceName, HttpServletResponse response)`
- `HttpResult checkAll(String deviceName, HttpServletResponse response)`
- `HttpResult checkAll(String deviceName, Integer minPower, HttpServletResponse response)`
## TestController.java

### Classes

#### TestController

### Methods

- `Map test()`
- `Map health()`
## VersionController.java

### Classes

#### VersionController

### Methods

- `ModelAndView versionHtml()`
- `VersionInfo getVersionInfo()`
- `HttpResult versionUpdate(VersionInfo versionInfo, HttpServletResponse response)`
- `HttpResult versionClear(HttpServletResponse response)`
- `HttpResult iotAppVersion(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`
- `HttpResult iotAppVersionTest(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`
- `HttpResult iotAppVersionPublichMcu(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`
- `HttpResult iotAppVersionTestMcu(String appUuid, String deviceUuid, String sign, HttpServletResponse response)`
- `void checkSign(Object valid, String sign)`
## WelcomeController.java

### Classes

#### WelcomeController

### Methods

- `ModelAndView welcome()`
