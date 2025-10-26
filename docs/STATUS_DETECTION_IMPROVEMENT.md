# Device Status Detection Improvement

## Problem Analysis

### Original Issue
Devices were showing **OFFLINE** status intermittently even when they were actively uploading data and responding to commands.

### Root Cause
**Timeline Mismatch:**
- Device uploads data every **20 minutes** via HTTP
- Original `device_activity` Redis key had TTL of **10 minutes**
- Original status check threshold was **5 minutes**

**The Gap:**
```
Time 0:00  → Device uploads via HTTP → activity_key set (expires at 10:00)
Time 10:00 → activity_key EXPIRES → Device shows OFFLINE ❌
Time 20:00 → Device uploads again → Back to ONLINE ✅
          ↑
    10 minute gap where device appears OFFLINE incorrectly!
```

## Solution Implemented

### Changes Made

#### 1. Increased Redis TTL for `device_activity`
**Changed from: 10 minutes → 25 minutes**

Files updated:
- `MqttSubscriber.java` (line 179): TTL increased to 25 minutes
- `ApiController.java` (line 238): TTL increased to 25 minutes  
- `MqttPublisher.java` (line 189): TTL increased to 25 minutes

**Reasoning:** 25 minutes = 20-minute upload interval + 5-minute grace period

#### 2. Updated Status Check Threshold
**Changed from: 5 minutes (300000ms) → 25 minutes (1500000ms)**

File: `MqttPublisher.java` (line 147)

```java
// OLD (5 minutes)
if (deviceConfig != null && lastActivity != null && (now - lastActivity < 300000)) {
    return DeviceOnline.ONLINE;
}

// NEW (25 minutes)
if (deviceConfig != null && lastActivity != null && (now - lastActivity < 1500000)) {
    return DeviceOnline.ONLINE;
}
```

### Current Status Detection Logic

The system now uses a **multi-tier approach** with appropriate time windows:

```java
public DeviceOnline getDeviceStatus(String productKey, String deviceName) {
    Long lastSeen = (Long) heartbeatOps.get();      // device_heartbeat key (TTL: 5 min)
    Object deviceConfig = configOps.get();           // clientConect key (TTL: 1 day)
    Long lastActivity = (Long) activityOps.get();    // device_activity key (TTL: 25 min)
    
    long now = System.currentTimeMillis();
    
    // Tier 1: Recent heartbeat (within 2 minutes) → ONLINE
    if (lastSeen != null && (now - lastSeen < 120000)) {
        return DeviceOnline.ONLINE;
    }
    
    // Tier 2: Recent activity (within 25 minutes) AND registered → ONLINE
    if (deviceConfig != null && lastActivity != null && (now - lastActivity < 1500000)) {
        return DeviceOnline.ONLINE;
    }
    
    // Tier 3: Registered but stale → OFFLINE
    if (deviceConfig != null) {
        return DeviceOnline.OFFLINE;
    }
    
    // Tier 4: Not registered → NO_DEVICE
    return DeviceOnline.NO_DEVICE;
}
```

## Redis Key Updates

### When `device_activity` is Updated:

1. **MQTT Messages Received** (MqttSubscriber.java):
   - On every message from `/powerbank/+/user/update` (command responses)
   - On every message from `/powerbank/+/user/upload` (data uploads)
   - On every message from `/powerbank/+/user/heart` (heartbeats)
   - TTL: **25 minutes**

2. **HTTP Data Uploads** (ApiController.java):
   - When device POSTs to `/api/iot/client/order`
   - TTL: **25 minutes**

3. **MQTT Commands Sent** (MqttPublisher.java):
   - When server sends commands to device
   - TTL: **25 minutes**

### When `device_heartbeat` is Updated:

Only when specific heartbeat messages are received:
- From `/powerbank/+/user/heart` topic
- From `/powerbank/+/user/status` topic (legacy)
- TTL: **5 minutes** (unchanged, more sensitive for real-time status)

## Benefits

### ✅ Eliminated False Negatives
- Devices uploading every 20 minutes now stay ONLINE throughout the cycle
- 5-minute grace period prevents edge cases

### ✅ Maintains Responsiveness
- Still detects truly offline devices within reasonable time
- Heartbeat check (2 min) provides real-time status for active devices

### ✅ Backward Compatible
- Works with both HTTP and MQTT upload paths
- Supports devices with varying upload frequencies

## Testing Recommendations

### 1. Monitor Device 864601069946994
```bash
# Check status over time (should stay ONLINE)
watch -n 60 'curl -s "http://localhost:8080/check?deviceName=864601069946994" | jq ".code"'
```

### 2. Check Redis Keys
```bash
# Monitor activity timestamp
docker exec iotdemo-redis redis-cli -a chargegharpb GET "device_activity:864601069946994"

# Check TTL (should be ~1500 seconds = 25 minutes)
docker exec iotdemo-redis redis-cli -a chargegharpb TTL "device_activity:864601069946994"
```

### 3. Verify Status Detection
```bash
# Device should show ONLINE even 15-20 minutes after last upload
curl "http://localhost:8080/index.html" | grep -A5 "864601069946994"
```

## Configuration for Different Device Types

If you have devices with different upload intervals:

### Fast Devices (upload every 5-10 min)
- Current settings work well (25 min window has plenty of buffer)

### Slow Devices (upload every 30+ min)
- Consider increasing to 35-40 minutes
- Modify the three locations:
  ```java
  activityOps.set(now, 40, TimeUnit.MINUTES);  // TTL
  (now - lastActivity < 2400000)  // 40 minutes in milliseconds
  ```

### Critical Devices (need immediate offline detection)
- Use heartbeat-only detection (already at 2 min threshold)
- Ensure devices send frequent heartbeats

## Production Deployment Notes

### Files Changed:
1. `src/main/java/com.demo/mqtt/MqttSubscriber.java`
2. `src/main/java/com.demo/mqtt/MqttPublisher.java`
3. `src/main/java/com.demo/controller/ApiController.java`

### Deployment Steps:
1. Build new Docker image with updated code
2. Deploy to production server
3. Restart containers
4. Monitor Redis keys for 30 minutes
5. Verify devices stay ONLINE throughout upload cycles

### Rollback Plan:
If issues arise, revert changes:
```bash
# Revert to 10 minute TTL
activityOps.set(now, 10, TimeUnit.MINUTES);
(now - lastActivity < 300000)  // 5 minutes
```

## Summary

**Before:**
- Activity TTL: 10 minutes
- Status threshold: 5 minutes
- **Problem:** 10-minute gap between uploads showed devices as OFFLINE

**After:**
- Activity TTL: 25 minutes
- Status threshold: 25 minutes
- **Solution:** Full coverage of 20-minute upload cycle + 5-minute buffer

This fix ensures devices are correctly shown as ONLINE as long as they communicate within their expected interval.
