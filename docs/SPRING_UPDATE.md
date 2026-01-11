# Spring Server Update Plan - Powerbank Popup Sync + Transaction Logs

## Overview

Add production-grade sync popup endpoints and Redis-based transaction logging for Django integration.

---

## Current State

| Feature | Status | Issue |
|---------|--------|-------|
| `/popup_random` | ✅ Working | Sync with 15s timeout |
| `/popup_sn` | ❌ Missing | Method exists, no endpoint |
| `/listen` | ⚠️ Limited | In-memory, max 100 msgs, no filtering |
| Transaction logs | ❌ None | No audit trail in Redis |

---

## Changes Required

### 1. Add `/popup_sn` Endpoint

**File:** `src/main/java/com.demo/controller/ShowController.java`

```java
@RequestMapping("/popup_sn")
public HttpResult popupBySn(
    @RequestParam String rentboxSN,
    @RequestParam String singleSN,
    HttpServletResponse response
) {
    HttpResult httpResult = new HttpResult();
    try {
        ReceivePopupSN result = deviceCommandUtils.popup(rentboxSN, singleSN);
        
        Map<String, Object> data = new HashMap<>();
        data.put("slot", result.getPinboardIndex());
        data.put("powerbankSN", result.getSnAsString());
        data.put("status", result.getStatus());
        data.put("success", result.getStatus() == 1);
        
        httpResult.setData(data);
    } catch (Exception e) {
        response.setStatus(HttpStatus.SC_REQUEST_TIMEOUT);
        httpResult.setCode(408);
        httpResult.setMsg(e.getMessage());
    }
    return httpResult;
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "slot": 2,
    "powerbankSN": "40818048",
    "status": 1,
    "success": true
  },
  "msg": "ok"
}
```

---

### 2. Add Transaction Logger Service

**File:** `src/main/java/com.demo/common/TransactionLogger.java` (NEW)

```java
package com.demo.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionLogger {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final int MAX_LOGS_PER_DEVICE = 50;
    private static final int LOG_TTL_MINUTES = 30;
    
    /**
     * Log a transaction with parsed data
     */
    public void log(String deviceName, String messageId, String cmd, 
                    String rawHex, Object parsedData) {
        
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("messageId", messageId);
        logEntry.put("deviceName", deviceName);
        logEntry.put("cmd", cmd);
        logEntry.put("raw", rawHex);
        logEntry.put("parsed", parsedData);
        logEntry.put("timestamp", System.currentTimeMillis() / 1000);
        
        // Store individual transaction
        String txnKey = "txn:" + deviceName + ":" + messageId;
        redisTemplate.opsForValue().set(txnKey, logEntry, LOG_TTL_MINUTES, TimeUnit.MINUTES);
        
        // Add to device's log list (newest first)
        String listKey = "txn_list:" + deviceName;
        redisTemplate.opsForList().leftPush(listKey, logEntry);
        redisTemplate.opsForList().trim(listKey, 0, MAX_LOGS_PER_DEVICE - 1);
        redisTemplate.expire(listKey, LOG_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * Get transaction by messageId
     */
    public Map<String, Object> getTransaction(String deviceName, String messageId) {
        String txnKey = "txn:" + deviceName + ":" + messageId;
        return (Map<String, Object>) redisTemplate.opsForValue().get(txnKey);
    }
    
    /**
     * Get device logs with optional CMD filter
     */
    public List<Map<String, Object>> getDeviceLogs(String deviceName, int limit, String cmdFilter) {
        String listKey = "txn_list:" + deviceName;
        List<Object> logs = redisTemplate.opsForList().range(listKey, 0, limit - 1);
        
        if (logs == null) return new ArrayList<>();
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object log : logs) {
            Map<String, Object> entry = (Map<String, Object>) log;
            if (cmdFilter == null || cmdFilter.equals(entry.get("cmd"))) {
                result.add(entry);
            }
        }
        return result;
    }
}
```

---

### 3. Update MqttSubscriber to Log Transactions

**File:** `src/main/java/com.demo/mqtt/MqttSubscriber.java`

**Add autowired:**
```java
@Autowired
TransactionLogger transactionLogger;
```

**Update `handlerMessage()` method - add logging for each CMD:**

```java
case 0x10:
    key = "check:" + messageBody.getDeviceName();
    boundValueOps = redisTemplate.boundValueOps(key);
    time = boundValueOps.getExpire();
    if (time <= 0) break;
    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
    
    // LOG TRANSACTION
    try {
        ReceiveUpload upload = new ReceiveUpload(messageBody.getPayloadAsBytes());
        transactionLogger.log(
            messageBody.getDeviceName(),
            messageBody.getMessageId(),
            "0x10",
            ByteUtils.to16Hexs(messageBody.getPayloadAsBytes()),
            upload.getPowerbanks()
        );
    } catch (Exception e) { /* log error */ }
    break;

case 0x31:
    key = "popup_sn:" + messageBody.getDeviceName();
    boundValueOps = redisTemplate.boundValueOps(key);
    time = boundValueOps.getExpire();
    if (time <= 0) break;
    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
    
    // LOG TRANSACTION
    try {
        ReceivePopupSN popup = new ReceivePopupSN(messageBody.getPayloadAsBytes());
        Map<String, Object> parsed = new HashMap<>();
        parsed.put("slot", popup.getPinboardIndex());
        parsed.put("powerbankSN", popup.getSnAsString());
        parsed.put("status", popup.getStatus());
        transactionLogger.log(
            messageBody.getDeviceName(),
            messageBody.getMessageId(),
            "0x31",
            ByteUtils.to16Hexs(messageBody.getPayloadAsBytes()),
            parsed
        );
    } catch (Exception e) { /* log error */ }
    break;
```

---

### 4. Add Log API Endpoint

**File:** `src/main/java/com.demo/controller/ApiController.java`

```java
@Autowired
TransactionLogger transactionLogger;

/**
 * Get device transaction logs
 * GET /api/device/{sn}/logs?limit=20&cmd=0x10
 */
@RequestMapping("/api/device/{sn}/logs")
public HttpResult getDeviceLogs(
    @PathVariable String sn,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(required = false) String cmd
) {
    HttpResult result = new HttpResult();
    result.setData(transactionLogger.getDeviceLogs(sn, limit, cmd));
    return result;
}

/**
 * Get specific transaction
 * GET /api/device/{sn}/logs/{messageId}
 */
@RequestMapping("/api/device/{sn}/logs/{messageId}")
public HttpResult getTransaction(
    @PathVariable String sn,
    @PathVariable String messageId
) {
    HttpResult result = new HttpResult();
    Map<String, Object> txn = transactionLogger.getTransaction(sn, messageId);
    if (txn == null) {
        result.setCode(404);
        result.setMsg("Transaction not found");
    } else {
        result.setData(txn);
    }
    return result;
}
```

---

## Redis Key Structure

| Key Pattern | TTL | Content |
|-------------|-----|---------|
| `txn:{deviceName}:{messageId}` | 30 min | Single transaction JSON |
| `txn_list:{deviceName}` | 30 min | List of last 50 transactions |

**Transaction JSON:**
```json
{
  "messageId": "uuid",
  "deviceName": "864601069946994",
  "cmd": "0x31",
  "raw": "A8 00 0C 31 01 02 6E D5 80 01 02 52",
  "parsed": {
    "slot": 1,
    "powerbankSN": "40818048",
    "status": 1
  },
  "timestamp": 1736580000
}
```

---

## New Endpoints Summary

| Method | Endpoint | Description | Timeout |
|--------|----------|-------------|---------|
| GET | `/popup_sn?rentboxSN=X&singleSN=Y` | Popup specific powerbank | 15s |
| GET | `/api/device/{sn}/logs` | Device transaction logs | - |
| GET | `/api/device/{sn}/logs/{messageId}` | Single transaction | - |

---

## Testing

```bash
# Test popup_sn
curl "https://api.chargeghar.com/popup_sn?rentboxSN=864601069946994&singleSN=40818048"

# Get device logs
curl "https://api.chargeghar.com/api/device/864601069946994/logs?limit=10"

# Get logs filtered by CMD
curl "https://api.chargeghar.com/api/device/864601069946994/logs?cmd=0x31"

# Get specific transaction
curl "https://api.chargeghar.com/api/device/864601069946994/logs/uuid-here"
```

---

## Deployment

1. Add `TransactionLogger.java` to `com.demo.common`
2. Update `MqttSubscriber.java` with transaction logging
3. Add endpoints to `ShowController.java` and `ApiController.java`
4. Rebuild: `mvn clean package`
5. Deploy: `docker-compose up -d --build`
