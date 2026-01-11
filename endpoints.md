# Spring Boot API Endpoints

**Generated on:** 2026-01-10 19:37:12  
**Project Directory:** `E:\Companies\DEVALAYA\Deva_ChargeGhar\Emqx\ChargeGhar_Devices`  
**Total Endpoints:** 54

---

## Table of Contents
- [ApiController](#apicontroller)
- [AuthController](#authcontroller)
- [EmqxTestController](#emqxtestcontroller)
- [EmqxWebhookController](#emqxwebhookcontroller)
- [IndexController](#indexcontroller)
- [ListenController](#listencontroller)
- [LoginController](#logincontroller)
- [ShowController](#showcontroller)
- [TestController](#testcontroller)
- [VersionController](#versioncontroller)

---

## ApiController

**File:** `src\main\java\com.demo\controller\ApiController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/api/device/mode/set` | `setNetworkMode()` |
| **GET** | `/api/device/volume/set` | `setVolume()` |
| **GET** | `/api/device/wifi/connect` | `wifiConnect()` |
| **GET** | `/api/device/wifi/scan` | `wifiScan()` |
| **GET** | `/api/iot/client/clear` | `deviceCreate()` |
| **GET** | `/api/iot/client/con` | `iotClientCon()` |
| **GET** | `/api/rentbox/config/data` | `rentboxConfigData()` |
| **GET** | `/api/rentbox/order/return` | `powerbankReturn()` |
| **GET** | `/api/rentbox/upload/data` | `rentboxOrderReturnEnd()` |

## AuthController

**File:** `src\main\java\com.demo\controller\AuthController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/api/auth/admins` | `getAllAdmins()` |
| **POST** | `/api/auth/admins` | `createAdmin()` |
| **GET** | `/api/auth/api/auth` | `()` |
| **POST** | `/api/auth/login` | `login()` |
| **POST** | `/api/auth/logout` | `logout()` |
| **GET** | `/api/auth/me` | `getCurrentUser()` |
| **POST** | `/api/auth/refresh` | `refreshToken()` |
| **POST** | `/api/auth/validate` | `validateToken()` |

## EmqxTestController

**File:** `src\main\java\com.demo\controller\EmqxTestController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/emqx/test/connection` | `testConnection()` |
| **GET** | `/emqx/test/credentials` | `getDeviceCredentials()` |
| **GET** | `/emqx/test/password` | `getDevicePassword()` |
| **GET** | `/emqx/test/publish` | `testPublish()` |
| **GET** | `/emqx/test/register` | `testDeviceRegistration()` |
| **GET** | `/emqx/test/remove` | `removeDevice()` |

## EmqxWebhookController

**File:** `src\main\java\com.demo\controller\EmqxWebhookController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/api/emqx/api/emqx` | `()` |
| **POST** | `/api/emqx/webhook` | `handleWebhook()` |

## IndexController

**File:** `src\main\java\com.demo\controller\IndexController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/` | `root()` |
| **GET** | `/admin/panel` | `adminPanel()` |
| **GET** | `/api/admin/statistics` | `getAdminStatistics()` |
| **GET** | `/device/create` | `deviceCreate()` |
| **GET** | `/device/delete` | `deviceDelete()` |
| **GET** | `/index.html` | `indexHtml()` |

## ListenController

**File:** `src\main\java\com.demo\controller\ListenController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/listen` | `listen()` |
| **GET** | `/listen.html` | `listenHtml()` |
| **GET** | `/listen/0x10` | `listen0x10()` |
| **GET** | `/listen/clear` | `listenClear()` |
| **GET** | `/listen/start` | `listenStart()` |
| **GET** | `/listen/stop` | `listenStop()` |

## LoginController

**File:** `src\main\java\com.demo\controller\LoginController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/login` | `loginPage()` |

## ShowController

**File:** `src\main\java\com.demo\controller\ShowController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/check` | `check()` |
| **GET** | `/check_all` | `checkAll()` |
| **GET** | `/popup_random` | `checkAll()` |
| **GET** | `/send` | `send()` |
| **GET** | `/show.html` | `showHtml()` |

## TestController

**File:** `src\main\java\com.demo\controller\TestController.java`

| Method | Endpoint | Handler |
|--------|----------|----------|
| **GET** | `/health` | `health()` |
| **GET** | `/test` | `test()` |

## VersionController

**File:** `src\main\java\com.demo\controller\VersionController.java`

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

---

## Complete Endpoint List

| Method | Endpoint | Controller | Handler |
|--------|----------|------------|----------|
| **GET** | `/` | IndexController | `root()` |
| **GET** | `/admin/panel` | IndexController | `adminPanel()` |
| **GET** | `/api/admin/statistics` | IndexController | `getAdminStatistics()` |
| **GET** | `/api/auth/admins` | AuthController | `getAllAdmins()` |
| **POST** | `/api/auth/admins` | AuthController | `createAdmin()` |
| **GET** | `/api/auth/api/auth` | AuthController | `()` |
| **POST** | `/api/auth/login` | AuthController | `login()` |
| **POST** | `/api/auth/logout` | AuthController | `logout()` |
| **GET** | `/api/auth/me` | AuthController | `getCurrentUser()` |
| **POST** | `/api/auth/refresh` | AuthController | `refreshToken()` |
| **POST** | `/api/auth/validate` | AuthController | `validateToken()` |
| **GET** | `/api/device/mode/set` | ApiController | `setNetworkMode()` |
| **GET** | `/api/device/volume/set` | ApiController | `setVolume()` |
| **GET** | `/api/device/wifi/connect` | ApiController | `wifiConnect()` |
| **GET** | `/api/device/wifi/scan` | ApiController | `wifiScan()` |
| **GET** | `/api/emqx/api/emqx` | EmqxWebhookController | `()` |
| **POST** | `/api/emqx/webhook` | EmqxWebhookController | `handleWebhook()` |
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
| **GET** | `/device/delete` | IndexController | `deviceDelete()` |
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
| **GET** | `/login` | LoginController | `loginPage()` |
| **GET** | `/popup_random` | ShowController | `checkAll()` |
| **GET** | `/send` | ShowController | `send()` |
| **GET** | `/show.html` | ShowController | `showHtml()` |
| **GET** | `/test` | TestController | `test()` |
| **GET** | `/version.html` | VersionController | `versionHtml()` |
| **GET** | `/version/clear` | VersionController | `versionClear()` |
| **GET** | `/version/update` | VersionController | `versionUpdate()` |
