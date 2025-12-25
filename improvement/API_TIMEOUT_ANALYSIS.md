# API Timeout Analysis & Troubleshooting Guide

**Analysis Date:** December 25, 2025  
**Issue:** Request Timeout Errors  
**Status:** ✅ Verified against actual codebase

---

## 🔴 Executive Summary

After **verifying the actual source code**, I've identified the **exact timeout points** and their **verified values** in your codebase. The primary causes of your "Request Time Out" errors are:

1. **Blocking MQTT command polling** - 10 second blocking loop in `DeviceCommandUtils.java`
2. **Missing HTTP timeouts** on `EmqxApiClient.java` (verified: no `RequestConfig` used)
3. **Synchronous Django sync** blocking device response in `ApiController.java`

---

## 📋 Verified Timeout Configuration

### ✅ VERIFIED: Timeouts Found in Code

| Component | File | Line | Timeout Value | Verified |
|-----------|------|------|---------------|----------|
| **MQTT Check** | `DeviceCommandUtils.java` | L47 | `10` seconds | ✅ Yes |
| **MQTT Check All** | `DeviceCommandUtils.java` | L53 | `10` seconds | ✅ Yes |
| **MQTT Popup** | `DeviceCommandUtils.java` | L60 | `15` seconds | ✅ Yes |
| **MQTT Connect** | `MqttPublisher.java` | L47 | `30` seconds | ✅ Yes |
| **MQTT Connect** | `MqttSubscriber.java` | L69 | `30` seconds | ✅ Yes |
| **Django Connect** | `config.properties` | L46 | `10000` ms (10s) | ✅ Yes |
| **Django Read** | `config.properties` | L47 | `15000` ms (15s) | ✅ Yes |
| **Django Retries** | `config.properties` | L50 | `3` retries | ✅ Yes |
| **Poll Interval** | `DeviceCommandUtils.java` | L95 | `500` ms | ✅ Yes |

### ❌ VERIFIED: Missing Timeouts

| Component | File | Issue | Verified |
|-----------|------|-------|----------|
| **EMQX API Client** | `EmqxApiClient.java` | No `RequestConfig` - uses `HttpClients.createDefault()` | ✅ Confirmed L73, L101, L122, L146, L178, L212 |
| **Redis Operations** | `spring-redis.xml` | No `timeout` property on `JedisConnectionFactory` | ✅ Confirmed L13-19 |
| **Redis Pool** | `spring-redis.xml` | No `maxWaitMillis` in `JedisPoolConfig` | ✅ Confirmed L22-25 |

---

## 🔍 Detailed Code Analysis

### Issue #1: MQTT Command Blocking Loop (PRIMARY CAUSE)

**File:** `DeviceCommandUtils.java`

**Verified Code (Lines 78-114):**
```java
private byte[] sendPopupWait(String key, String rentboxSN, String message, int overSecond) throws Exception {
    this.checkOnlineStatus(rentboxSN);  // Line 79 - throws "Device is Offline"

    // PUT REDIS - same logic as original
    BoundValueOperations operations = redisTemplate.boundValueOps(key);
    operations.set(null, overSecond, TimeUnit.SECONDS);  // Line 83

    String emqxTopic = "/" + appConfig.getProductKey() + "/" + rentboxSN + "/user/get";  // Line 88
    mqttPublisher.sendMsgAsync(appConfig.getProductKey(), emqxTopic, message, 1);  // Line 90

    // ⚠️ BLOCKING LOOP - Line 93-107
    byte[] bytes = null;
    for (int i = 0; i < overSecond * 2; i++) {  // 10*2 = 20 iterations
        Thread.sleep(500);  // 500ms per iteration = 10 seconds total
        Object data = null;
        try {
            data = operations.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data != null && data instanceof byte[]) {
            bytes = (byte[]) data;
            redisTemplate.boundValueOps(key).expire(-1, TimeUnit.MILLISECONDS);
            break;
        }
    }

    if (bytes == null) {
        throw new Exception("Request Time Out");  // Line 110 - YOUR ERROR!
    }

    return bytes;
}
```

**Call Flow Verified:**
- `check()` Line 47: `sendPopupWait(key, rentboxSN, SEND_CHECK, 10)` → 10 seconds max
- `checkAll()` Line 53: `sendPopupWait(key, rentboxSN, SEND_CHECK_ALL, 10)` → 10 seconds max  
- `popup()` Line 60: `sendPopupWait(key, rentboxSN, message, 15)` → 15 seconds max
- `popupByRandom()` Line 67: Calls `check()` + `popup()` → **25 seconds max** total!

**Root Causes for "Request Time Out":**
1. Device is offline (caught at Line 79)
2. Device connected but doesn't respond to MQTT command
3. MqttSubscriber missed the response message
4. Response stored in Redis but key expired
5. Network latency between EMQX Cloud and device

---

### Issue #2: No HTTP Timeouts in EmqxApiClient

**File:** `EmqxApiClient.java`

**Verified Code - No RequestConfig used:**
```java
// Line 29-31
public EmqxApiClient() {
    this.httpClient = HttpClients.createDefault();  // ❌ No timeout!
}

// Line 73 - registerDevice method  
try (CloseableHttpClient client = HttpClients.createDefault()) {  // ❌ No timeout!
    HttpResponse response = client.execute(request);
    // ...
}

// Line 101 - deviceExists method
try (CloseableHttpClient client = HttpClients.createDefault()) {  // ❌ No timeout!
    HttpResponse response = client.execute(request);
    // ...
}

// Line 122, 146, 178, 212 - Same pattern repeats
```

**Impact:** If EMQX Cloud API is slow or unresponsive:
- Device registration (`/api/iot/client/con`) hangs
- No timeout = request can wait indefinitely
- Tomcat thread gets blocked

---

### Issue #3: Synchronous Django Sync in ApiController

**File:** `ApiController.java`

**Verified Code (Lines 240-241):**
```java
// Line 240-241 in rentboxOrderReturnEnd()
// Sync device data to ChargeGhar Main
controllerHelper.syncDeviceUploadToMain(rentboxSN, receiveUpload, signal, ssid);  // BLOCKING!
```

**File:** `ControllerHelper.java`

**Verified Code (Lines 86-99):**
```java
public void syncDeviceUploadToMain(String rentboxSN, ReceiveUpload receiveUpload, String signal, String ssid) {
    try {
        // This calls ChargeGharConnector which has 10s connect + 15s read timeout
        // With 3 retries and exponential backoff
        boolean syncSuccess = chargeGharConnector.sendDeviceData(rentboxSN, receiveUpload, signal, ssid);
        // ...
    } catch (Exception syncException) {
        // ...
        // Don't fail the request - continue processing
    }
}
```

**File:** `ChargeGharConnector.java`

**Verified Code (Lines 69-72, 289-326):**
```java
// Line 69-72 - Timeout configuration
this.requestConfig = RequestConfig.custom()
        .setConnectTimeout(connectTimeout)   // 10000ms from config
        .setSocketTimeout(readTimeout)       // 15000ms from config
        .build();

// Line 289-326 - sendWithRetry method
private boolean sendWithRetry(String endpoint, String jsonPayload) {
    int attempts = 0;
    
    while (attempts < maxRetries) {  // maxRetries = 3
        attempts++;
        try {
            boolean success = sendHttpPost(endpoint, jsonPayload);
            if (success) return true;
        } catch (Exception e) {
            // ...
        }
        
        // Wait before retry (exponential backoff)
        if (attempts < maxRetries) {
            try {
                int waitTime = (int) Math.pow(2, attempts) * 1000;  // 2s, 4s, 8s
                Thread.sleep(waitTime);
            } catch (InterruptedException ie) {
                // ...
            }
        }
    }
    return false;
}
```

**Worst-case timing for `/api/rentbox/upload/data`:**
- Attempt 1: 10s connect + 15s read = 25s
- Wait: 2s
- Attempt 2: 10s connect + 15s read = 25s
- Wait: 4s  
- Attempt 3: 10s connect + 15s read = 25s
- **Total: 81 seconds blocking!**

---

### Issue #4: Redis Connection Without Timeout

**File:** `spring-redis.xml`

**Verified Code (Lines 13-25):**
```xml
<!-- Line 13-19 - JedisConnectionFactory -->
<bean id="singleFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
    <property name="hostName" value="${redis.host}" />
    <property name="port" value="${redis.port}" />
    <property name="password" value="${redis.pass}" />
    <property name="poolConfig" ref="jedisPoolConfig" />
    <property name="database" value="5" />
    <!-- ❌ NO timeout property! -->
</bean>

<!-- Line 22-25 - JedisPoolConfig -->
<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxIdle" value="100" />
    <property name="maxTotal" value="600" />
    <!-- ❌ NO maxWaitMillis property! -->
</bean>
```

**Missing Properties:**
- `JedisConnectionFactory.timeout` - Controls socket timeout for Redis operations
- `JedisPoolConfig.maxWaitMillis` - Controls how long to wait for a connection from pool

---

### Issue #5: Response Handling in MqttSubscriber

**File:** `MqttSubscriber.java`

**Verified Code (Lines 200-218):**
```java
// Line 200-218 - handlerMessage switch cases
case 0x10:  // Check response
    String key = "check:" + messageBody.getDeviceName();
    BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);
    long time = boundValueOps.getExpire();
    if (time <= 0) break;  // ⚠️ If key expired, response is DROPPED!
    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
    break;
case 0x31:  // Popup response
    key = "popup_sn:" + messageBody.getDeviceName();
    boundValueOps = redisTemplate.boundValueOps(key);
    time = boundValueOps.getExpire();
    if (time <= 0) break;  // ⚠️ If key expired, response is DROPPED!
    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
    break;
```

**Race Condition Found:**
1. `DeviceCommandUtils.sendPopupWait()` sets `check:{sn}` with 10-second TTL
2. Device receives command, processes, sends response
3. If response takes > 10 seconds to arrive, key expires
4. `MqttSubscriber.handlerMessage()` checks `getExpire()` → returns -2 (key expired)
5. Response is **silently dropped** (`if (time <= 0) break;`)
6. Polling loop times out → "Request Time Out" error

---

## 📊 Timeout Flow Diagram (Verified)

```
User calls /check?deviceName=X
    │
    ├── DeviceCommandUtils.check(X)
    │       │
    │       ├── checkOnlineStatus(X)                    [Redis: device_activity:{X}]
    │       │       └── If not ONLINE → throw "Device is Offline"
    │       │
    │       ├── redisTemplate.set("check:X", null, 10s) [Set placeholder]
    │       │
    │       ├── mqttPublisher.sendMsgAsync(topic, cmd)  [Publish to EMQX]
    │       │       │
    │       │       └── EMQX Cloud ──────────────────► ESP32 Device
    │       │                                           │
    │       │       ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
    │       │                                           │
    │       │       MqttSubscriber.messageArrived()     │
    │       │           │                               │
    │       │           └── case 0x10:                  │
    │       │               if (getExpire() > 0)        │
    │       │                   set("check:X", bytes)   │
    │       │               else                        │
    │       │                   /* DROPPED! */          │
    │       │
    │       └── BLOCKING LOOP (10 seconds max)
    │               for (i = 0; i < 20; i++) {
    │                   Thread.sleep(500);
    │                   data = redis.get("check:X");
    │                   if (data != null) break;
    │               }
    │               if (data == null) throw "Request Time Out"  ← YOUR ERROR
    │
    └── Return response or error
```

---

## 🛠️ Verified Fixes

### Fix #1: Reduce MQTT Timeout (Quick Win)

**File:** `DeviceCommandUtils.java`

**Change Line 47:**
```java
// Before
byte[] data = sendPopupWait(key, rentboxSN, SEND_CHECK, 10);

// After
byte[] data = sendPopupWait(key, rentboxSN, SEND_CHECK, 5);  // 5 seconds
```

**Change Line 53:**
```java
// Before  
byte[] data = sendPopupWait(key, rentboxSN, SEND_CHECK_ALL, 10);

// After
byte[] data = sendPopupWait(key, rentboxSN, SEND_CHECK_ALL, 5);  // 5 seconds
```

**Change Line 60:**
```java
// Before
byte[] data = sendPopupWait(key, rentboxSN, message, 15);

// After
byte[] data = sendPopupWait(key, rentboxSN, message, 8);  // 8 seconds
```

---

### Fix #2: Add HTTP Timeouts to EmqxApiClient

**File:** `EmqxApiClient.java`

**Add imports after Line 10:**
```java
import org.apache.http.client.config.RequestConfig;
```

**Replace constructor (Line 29-31):**
```java
private static final int CONNECT_TIMEOUT = 5000;  // 5 seconds
private static final int READ_TIMEOUT = 10000;    // 10 seconds

private RequestConfig requestConfig;

public EmqxApiClient() {
    this.requestConfig = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setSocketTimeout(READ_TIMEOUT)
            .setConnectionRequestTimeout(CONNECT_TIMEOUT)
            .build();
    this.httpClient = HttpClients.createDefault();
}
```

**Update each method that creates HttpClient to use the config:**
```java
// Example for registerDevice (Line 68-89)
HttpPost request = new HttpPost(url);
request.setConfig(requestConfig);  // ADD THIS LINE
request.setHeader("Content-Type", "application/json");
// ...
```

---

### Fix #3: Add Redis Timeouts

**File:** `spring-redis.xml`

**Replace Lines 13-25:**
```xml
<!-- Redis Connection with Timeout -->
<bean id="singleFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
    <property name="hostName" value="${redis.host}" />
    <property name="port" value="${redis.port}" />
    <property name="password" value="${redis.pass}" />
    <property name="poolConfig" ref="jedisPoolConfig" />
    <property name="database" value="5" />
    <property name="timeout" value="2000" />  <!-- ADD: 2 second timeout -->
</bean>

<!-- Pool with Wait Timeout -->
<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxIdle" value="100" />
    <property name="maxTotal" value="600" />
    <property name="maxWaitMillis" value="2000" />  <!-- ADD: 2 second wait -->
    <property name="testOnBorrow" value="true" />   <!-- ADD: validate on borrow -->
</bean>
```

---

### Fix #4: Make Django Sync Non-Blocking

**File:** `ApiController.java`

**Change Line 241:**
```java
// Before (Line 241)
controllerHelper.syncDeviceUploadToMain(rentboxSN, receiveUpload, signal, ssid);

// After - non-blocking
final String sn = rentboxSN;
final ReceiveUpload data = receiveUpload;
final String sig = signal;
final String wifi = ssid;
new Thread(() -> {
    try {
        controllerHelper.syncDeviceUploadToMain(sn, data, sig, wifi);
    } catch (Exception e) {
        System.err.println("Async sync failed: " + e.getMessage());
    }
}).start();
```

---

### Fix #5: Reduce Django Retry Configuration

**File:** `config.properties`

**Change Lines 46-50:**
```properties
# Before
chargeghar.main.connectTimeout=10000
chargeghar.main.readTimeout=15000
chargeghar.main.maxRetries=3

# After - faster timeouts, fewer retries
chargeghar.main.connectTimeout=5000
chargeghar.main.readTimeout=8000
chargeghar.main.maxRetries=2
```

---

## 📈 Priority Implementation

| Priority | Fix | Effort | Impact | Files to Change |
|----------|-----|--------|--------|-----------------|
| 🔴 1 | Reduce MQTT timeout | 2 min | High | `DeviceCommandUtils.java` |
| 🔴 2 | Make Django sync async | 5 min | High | `ApiController.java` |
| 🟡 3 | Add EMQX API timeouts | 15 min | Medium | `EmqxApiClient.java` |
| 🟡 4 | Add Redis timeouts | 5 min | Medium | `spring-redis.xml` |
| 🟡 5 | Reduce Django timeouts | 1 min | Medium | `config.properties` |

---

## ✅ Verification Checklist

All timeout values verified against source code:
- [x] `DeviceCommandUtils.java` - Lines 47, 53, 60, 78-114
- [x] `MqttPublisher.java` - Line 47
- [x] `MqttSubscriber.java` - Lines 69, 200-218
- [x] `EmqxApiClient.java` - Lines 29-31, 73, 101, 122, 146, 178, 212
- [x] `ChargeGharConnector.java` - Lines 53-59, 69-72, 289-326
- [x] `ApiController.java` - Lines 240-241
- [x] `ControllerHelper.java` - Lines 86-99
- [x] `spring-redis.xml` - Lines 13-25
- [x] `config.properties` - Lines 46-50

---

*Last Updated: December 25, 2025*  
*All values verified against actual source code*
