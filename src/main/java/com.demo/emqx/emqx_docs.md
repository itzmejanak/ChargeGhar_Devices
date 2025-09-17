# Documentation for Directory: emqx

**Path:** src/main/java/com.demo\emqx

**Total Java files:** 3

**Output mode:** detailed

## Summary

- **Files:** 3
- **Classes:** 3
- **Methods:** 23

## Table of Contents

- [DeviceCredentials.java](#DeviceCredentials-java)
- [EmqxApiClient.java](#EmqxApiClient-java)
- [EmqxDeviceService.java](#EmqxDeviceService-java)

## DeviceCredentials.java

### Classes

#### DeviceCredentials

**Used in:**
- **controller\EmqxTestController.java:**
  - Used in declaration: DeviceCredentials
- **emqx\EmqxDeviceService.java:**
  - Used in declaration: DeviceCredentials
  - Instantiated as: new DeviceCredentials()

### Properties (Getters/Setters)

**createdtime:**
- **getCreatedTime()**: Date
- **setCreatedTime(Date createdTime)**: void

**devicename:**
- **getDeviceName()**: String
- **setDeviceName(String deviceName)**: void

**password:**
- **getPassword()**: String
- **setPassword(String password)**: void

**username:**
- **getUsername()**: String
- **setUsername(String username)**: void

### Methods

#### `String toString()`

**Called in:**
- **common\FreemarkerExceptionHandler.java:**
  - Static call: e.toString()
  - Instance call: e.toString()
- **controller\ApiController.java:**
  - Static call: e.toString()
  - Instance call: e.toString()
- **controller\IndexController.java:**
  - Static call: e.toString()
  - Instance call: e.toString()
- **controller\ListenController.java:**
  - Static call: e.toString()
  - Instance call: e.toString()
- **controller\ShowController.java:**
  - Static call: e.toString()
  - Instance call: e.toString()
- **controller\VersionController.java:**
  - Static call: e.toString()
  - Instance call: e.toString()
- **emqx\DeviceCredentials.java:**
  - Direct call: toString()
- **emqx\EmqxApiClient.java:**
  - Static call: EntityUtils.toString()
  - Instance call: EntityUtils.toString()
- **emqx\EmqxDeviceService.java:**
  - Static call: password.toString()
  - Instance call: password.toString()
- **tools\HmacCoder.java:**
  - Static call: sb.toString()
  - Instance call: sb.toString()
- **tools\HttpServletUtils.java:**
  - Static call: sb.toString()
  - Instance call: sb.toString()

---

## EmqxApiClient.java

### Classes

#### EmqxApiClient

**Used in:**
- **controller\EmqxTestController.java:**
  - Used in declaration: EmqxApiClient
- **emqx\EmqxDeviceService.java:**
  - Used in declaration: EmqxApiClient

### Properties (Getters/Setters)

**authheader:**
- **getAuthHeader()**: String

### Methods

#### `boolean registerDevice(String deviceId, String password)`

**Called in:**
- **emqx\EmqxApiClient.java:**
  - Direct call: registerDevice()
- **emqx\EmqxDeviceService.java:**
  - Instance call: emqxApiClient.registerDevice()
  - Static call: emqxApiClient.registerDevice()

#### `boolean deviceExists(String deviceId)`

**Called in:**
- **emqx\EmqxApiClient.java:**
  - Direct call: deviceExists()
- **emqx\EmqxDeviceService.java:**
  - Instance call: emqxApiClient.deviceExists()
  - Static call: emqxApiClient.deviceExists()

#### `boolean updateDevicePassword(String deviceId, String newPassword)`

**Called in:**
- **emqx\EmqxApiClient.java:**
  - Direct call: updateDevicePassword()
- **emqx\EmqxDeviceService.java:**
  - Instance call: emqxApiClient.updateDevicePassword()
  - Static call: emqxApiClient.updateDevicePassword()

#### `boolean deleteDevice(String deviceId)`

**Called in:**
- **emqx\EmqxApiClient.java:**
  - Direct call: deleteDevice()
- **emqx\EmqxDeviceService.java:**
  - Static call: emqxApiClient.deleteDevice()
  - Instance call: emqxApiClient.deleteDevice()

#### `boolean publishMessage(String topic, String payload, int qos)`

**Called in:**
- **controller\EmqxTestController.java:**
  - Instance call: emqxApiClient.publishMessage()
  - Static call: emqxApiClient.publishMessage()
- **emqx\EmqxApiClient.java:**
  - Direct call: publishMessage()

#### `boolean testConnection()`

**Called in:**
- **controller\EmqxTestController.java:**
  - Direct call: testConnection()
  - Static call: emqxApiClient.testConnection()
  - Instance call: emqxApiClient.testConnection()
- **emqx\EmqxApiClient.java:**
  - Direct call: testConnection()

---

## EmqxDeviceService.java

### Classes

#### EmqxDeviceService

**Used in:**
- **controller\ApiController.java:**
  - Used in declaration: EmqxDeviceService
- **controller\EmqxTestController.java:**
  - Used in declaration: EmqxDeviceService

### Methods

#### `DeviceConfig getOrCreateDeviceConfig(String deviceName)`

**Called in:**
- **controller\ApiController.java:**
  - Static call: emqxDeviceService.getOrCreateDeviceConfig()
  - Instance call: emqxDeviceService.getOrCreateDeviceConfig()
- **controller\EmqxTestController.java:**
  - Static call: emqxDeviceService.getOrCreateDeviceConfig()
  - Instance call: emqxDeviceService.getOrCreateDeviceConfig()
- **emqx\EmqxDeviceService.java:**
  - Direct call: getOrCreateDeviceConfig()

#### `DeviceCredentials createDeviceCredentials(String deviceName)`

**Called in:**
- **emqx\EmqxDeviceService.java:**
  - Direct call: createDeviceCredentials()

#### `String generateSecurePassword()`

**Called in:**
- **emqx\EmqxDeviceService.java:**
  - Direct call: generateSecurePassword()

#### `DeviceConfig createDeviceConfig(String deviceName, DeviceCredentials credentials)`

**Called in:**
- **emqx\EmqxDeviceService.java:**
  - Direct call: createDeviceConfig()

#### `boolean rotateDevicePassword(String deviceName)`

**Called in:**
- **emqx\EmqxDeviceService.java:**
  - Direct call: rotateDevicePassword()

#### `boolean removeDevice(String deviceName)`

**Called in:**
- **controller\EmqxTestController.java:**
  - Direct call: removeDevice()
  - Instance call: emqxDeviceService.removeDevice()
  - Static call: emqxDeviceService.removeDevice()
- **emqx\EmqxDeviceService.java:**
  - Direct call: removeDevice()

#### `DeviceCredentials getDeviceCredentials(String deviceName)`

**Called in:**
- **controller\EmqxTestController.java:**
  - Static call: emqxDeviceService.getDeviceCredentials()
  - Direct call: getDeviceCredentials()
  - Instance call: emqxDeviceService.getDeviceCredentials()
- **emqx\EmqxDeviceService.java:**
  - Direct call: getDeviceCredentials()

---

