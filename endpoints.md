# Spring Boot API Endpoints

**Generated on:** 2025-10-28 15:17:57  
**Project Directory:** `/home/revdev/Desktop/Daily/Devalaya/PowerBank/Emqx/ChargeGhar_Devices`  
**Total Endpoints:** 37

---

## Table of Contents
- [ApiController](#apicontroller)
- [EmqxTestController](#emqxtestcontroller)
- [IndexController](#indexcontroller)
- [ListenController](#listencontroller)
- [ShowController](#showcontroller)
- [TestController](#testcontroller)
- [VersionController](#versioncontroller)
- [WelcomeController](#welcomecontroller)

---

## ApiController (Dont secure use by hardware)

**File:** `src/main/java/com.demo/controller/ApiController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/api/iot/client/clear` | `deviceCreate()` |
| **GET** | `/api/iot/client/con` | `iotClientCon()` |
| **GET** | `/api/rentbox/config/data` | `rentboxConfigData()` |
| **GET** | `/api/rentbox/order/return` | `powerbankReturn()` |
| **GET** | `/api/rentbox/upload/data` | `rentboxOrderReturnEnd()` |

## EmqxTestController (dont focus for now)

**File:** `src/main/java/com.demo/controller/EmqxTestController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/emqx/test/connection` | `testConnection()` |
| **GET** | `/emqx/test/credentials` | `getDeviceCredentials()` |
| **GET** | `/emqx/test/password` | `getDevicePassword()` |
| **GET** | `/emqx/test/publish` | `testPublish()` |
| **GET** | `/emqx/test/register` | `testDeviceRegistration()` |
| **GET** | `/emqx/test/remove` | `removeDevice()` |

## IndexController (Need to secure)

**File:** `src/main/java/com.demo/controller/IndexController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/` | `root()` |
| **GET** | `/device/create` | `deviceCreate()` |
| **GET** | `/index.html` | `indexHtml()` |

## ListenController (Need to secure)

**File:** `src/main/java/com.demo/controller/ListenController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/listen` | `listen()` |
| **GET** | `/listen.html` | `listenHtml()` |
| **GET** | `/listen/0x10` | `listen0x10()` |
| **GET** | `/listen/clear` | `listenClear()` |
| **GET** | `/listen/start` | `listenStart()` |
| **GET** | `/listen/stop` | `listenStop()` |

## ShowController (Need to secure)

**File:** `src/main/java/com.demo/controller/ShowController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/check` | `check()` |
| **GET** | `/check_all` | `checkAll()` |
| **GET** | `/popup_random` | `checkAll()` |
| **GET** | `/send` | `send()` |
| **GET** | `/show.html` | `showHtml()` |

## TestController (Need to secure)

**File:** `src/main/java/com.demo/controller/TestController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/health` | `health()` |
| **GET** | `/test` | `test()` |

## VersionController (Dont secure use by hardware)

**File:** `src/main/java/com.demo/controller/VersionController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/api/iot/app/version/publish` | `iotAppVersion()` |
| **GET** | `/api/iot/app/version/publish/chip` | `iotAppVersionPublishChip()` |
| **GET** | `/api/iot/app/version/publish/mcu` | `iotAppVersionPublichMcu()` |
| **GET** | `/api/iot/app/version/test` | `iotAppVersionTest()` |
| **GET** | `/api/iot/app/version/test/chip` | `iotAppVersionTestChip()` |
| **GET** | `/api/iot/app/version/test/mcu` | `iotAppVersionTestMcu()` |
| **GET** | `/version.html` | `versionHtml()` |
| **GET** | `/version/clear` | `versionClear()` |
| **GET** | `/version/update` | `versionUpdate()` |

## WelcomeController (Remove this comoletely witgpout lefting any code related to it)

**File:** `src/main/java/com.demo/controller/WelcomeController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/welcome` | `welcome()` |

---

## Complete Endpoint List

| Method | Endpoint | Controller | Handler |
|--------|----------|------------|----------|
| **GET** | `/` | IndexController | `root()` |
| **GET** | `/api/iot/app/version/publish` | VersionController | `iotAppVersion()` |
| **GET** | `/api/iot/app/version/publish/chip` | VersionController | `iotAppVersionPublishChip()` |
| **GET** | `/api/iot/app/version/publish/mcu` | VersionController | `iotAppVersionPublichMcu()` |
| **GET** | `/api/iot/app/version/test` | VersionController | `iotAppVersionTest()` |
| **GET** | `/api/iot/app/version/test/chip` | VersionController | `iotAppVersionTestChip()` |
| **GET** | `/api/iot/app/version/test/mcu` | VersionController | `iotAppVersionTestMcu()` |
| **GET** | `/api/iot/client/clear` | ApiController | `deviceCreate()` |
| **GET** | `/api/iot/client/con` | ApiController | `iotClientCon()` |
| **GET** | `/api/rentbox/config/data` | ApiController | `rentboxConfigData()` |
| **GET** | `/api/rentbox/order/return` | ApiController | `powerbankReturn()` |
| **GET** | `/api/rentbox/upload/data` | ApiController | `rentboxOrderReturnEnd()` |
| **GET** | `/check` | ShowController | `check()` |
| **GET** | `/check_all` | ShowController | `checkAll()` |
| **GET** | `/device/create` | IndexController | `deviceCreate()` |
| **GET** | `/emqx/test/connection` | EmqxTestController | `testConnection()` |
| **GET** | `/emqx/test/credentials` | EmqxTestController | `getDeviceCredentials()` |
| **GET** | `/emqx/test/password` | EmqxTestController | `getDevicePassword()` |
| **GET** | `/emqx/test/publish` | EmqxTestController | `testPublish()` |
| **GET** | `/emqx/test/register` | EmqxTestController | `testDeviceRegistration()` |
| **GET** | `/emqx/test/remove` | EmqxTestController | `removeDevice()` |
| **GET** | `/health` | TestController | `health()` |
| **GET** | `/index.html` | IndexController | `indexHtml()` |
| **GET** | `/listen` | ListenController | `listen()` |
| **GET** | `/listen.html` | ListenController | `listenHtml()` |
| **GET** | `/listen/0x10` | ListenController | `listen0x10()` |
| **GET** | `/listen/clear` | ListenController | `listenClear()` |
| **GET** | `/listen/start` | ListenController | `listenStart()` |
| **GET** | `/listen/stop` | ListenController | `listenStop()` |
| **GET** | `/popup_random` | ShowController | `checkAll()` |
| **GET** | `/send` | ShowController | `send()` |
| **GET** | `/show.html` | ShowController | `showHtml()` |
| **GET** | `/test` | TestController | `test()` |
| **GET** | `/version.html` | VersionController | `versionHtml()` |
| **GET** | `/version/clear` | VersionController | `versionClear()` |
| **GET** | `/version/update` | VersionController | `versionUpdate()` |
| **GET** | `/welcome` | WelcomeController | `welcome()` |


Note: We have entry file index.jsp find its usages and do accuare flow anaysis and make sure it will open after only login successful