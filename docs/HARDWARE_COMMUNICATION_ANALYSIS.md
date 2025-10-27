# Hardware Communication Flow Analysis

## 🔌 MQTT Topics Used by Hardware Device

### Device SUBSCRIBES to (Receives Commands from Server):
```
/powerbank/{deviceName}/user/get
```
- **Purpose**: Receive commands from server
- **Commands**: `{"cmd":"check"}`, `{"cmd":"check_all"}`, `{"cmd":"popup_sn","data":"..."}`
- **Triggered by**: Server check endpoint, popup commands

### Device PUBLISHES to (Sends Data to Server):

#### 1. `/powerbank/{deviceName}/user/update`
- **Purpose**: Device RESPONSE to server commands
- **Triggered by**: After receiving command on `/user/get` topic
- **Data**: Binary device status (ReceiveUpload format)
- **Use case**: Response to check commands

#### 2. `/powerbank/{deviceName}/user/heart`
- **Purpose**: Heartbeat/keepalive messages
- **Frequency**: Periodic (keeps connection alive)
- **Data**: Heartbeat payload
- **Use case**: Device online status detection

#### 3. `/powerbank/{deviceName}/user/upload`
- **Purpose**: Scheduled device data uploads via MQTT (if used)
- **Data**: Binary device status
- **Note**: May not be actively used if device uses HTTP upload

---

## 🌐 HTTP Endpoints Called by Hardware Device

### 1. `/api/iot/client/con` - Device Registration
**Method**: GET  
**When**: Device startup/initialization  
**Parameters**:
- `uuid` - Device serial number (rentboxSN)
- `version` - Firmware version
- `sign` - Security signature

**Purpose**: Register device and get configuration  
**Response**: Device credentials, MQTT settings

---

### 2. `/api/rentbox/upload/data` - Scheduled Data Upload ⭐ MAIN ENDPOINT
**Method**: POST  
**When**: **Every 20 minutes automatically** (hardware scheduled)  
**Parameters**:
- `rentboxSN` - Device serial number
- `signal` - WiFi signal strength
- `sign` - Security signature
- `ssid` - WiFi network name (optional)
- `io` - IO status (optional)

**Body**: Binary data (ReceiveUpload format) containing:
- All pinboard statuses
- All powerbank data (SN, battery level, status, etc.)

**Purpose**: Regular device health check and state synchronization  
**THIS IS THE PRIMARY DATA SOURCE!** ✅

**Current Implementation**:
```java
// ApiController.java - Line 269
@RequestMapping("/api/rentbox/upload/data")
public HttpResult rentboxOrderReturnEnd(@RequestBody byte[] bytes, ...) {
    // Parse binary data
    ReceiveUpload receiveUpload = new ReceiveUpload(bytes);
    
    // Cache data for 30 minutes
    uploadCacheOps.set(bytes, 30, TimeUnit.MINUTES);
    
    // Update Redis activity (25 min TTL)
    activityOps.set(now, 25, TimeUnit.MINUTES);
    
    // 🔄 Sync to Django Main App
    chargeGharConnector.sendDeviceData(rentboxSN, receiveUpload, signal, ssid);
}
```

---

### 3. `/api/rentbox/order/return` - PowerBank Return Notification
**Method**: POST  
**When**: External system detects powerbank return  
**Note**: This is **NOT called by hardware device directly**  
**Called by**: Your backend system/third-party service when return detected

**Parameters**:
- `rentboxSN` - Station serial number
- `singleSN` - PowerBank serial number
- `hole` - Slot number where powerbank returned
- `sign` - Security signature

**Purpose**: Notify that a powerbank was returned to station

---

## 🔄 Check Command Flow (Real-time Data Request)

### When Server Sends Check Command:

```
User/System calls: GET /check?deviceName=864601069946994
         ↓
DeviceCommandUtils.check()
         ↓
Publish MQTT: {"cmd":"check"} to /powerbank/{deviceName}/user/get
         ↓
Store null in Redis: "check:{deviceName}" with 10s TTL
         ↓
Wait (poll Redis every 500ms for 10 seconds)
         ↓
Device receives command on /user/get
         ↓
Device sends response on /powerbank/{deviceName}/user/update
         ↓
MqttSubscriber receives and stores in Redis: "check:{deviceName}"
         ↓
DeviceCommandUtils retrieves from Redis
         ↓
Parse and return ReceiveUpload data
```

**Key Point**: Check command requires **MQTT round-trip** (1-2 seconds)

---

## 📊 Data Flow Comparison

### Hardware's Regular Upload (Every 20 minutes)
```
Device (Hardware Timer)
    ↓ HTTP POST (scheduled)
    ↓ /api/rentbox/upload/data
    ↓ Binary data with ALL powerbank info
    ↓ Includes: SN, battery %, status, temp, etc.
    ↓
ApiController receives
    ↓
Parse to ReceiveUpload
    ↓
Cache in Redis (30 min)
    ↓
Sync to Django Main App ✅
```

### Check Command Flow (On-demand)
```
Server/User request
    ↓ GET /check?deviceName=...
    ↓
Send MQTT command to device
    ↓ /powerbank/{deviceName}/user/get
    ↓
Wait for device response (10s timeout)
    ↓ /powerbank/{deviceName}/user/update
    ↓
Parse binary response
    ↓
Return current data
    ❌ Does NOT sync to Django
```

---

## 🎯 Critical Analysis for Return Event Battery Level

### Current Approach: Use Cached Upload Data
```java
// Get battery level from last scheduled upload (within 30 min)
String cacheKey = "upload_data:" + rentboxSN;
byte[] cachedBytes = (byte[]) cacheOps.get();
ReceiveUpload cachedData = new ReceiveUpload(cachedBytes);
// Extract battery level from cached powerbank data
```

**Pros**:
- ✅ Fast (no MQTT call)
- ✅ Works even if device temporarily offline
- ✅ Uses data from regular hardware upload (already synced to Django)
- ✅ **NO DUPLICATE DATA** sent to Django

**Cons**:
- ⚠️ Battery level may be **0-30 minutes old**
- ⚠️ If return happens 25 min after upload, data is stale

---

### Alternative: Use Check Command
```java
// Get real-time data by sending check command
ReceiveUpload realtimeData = deviceCommandUtils.check(rentboxSN);
// Extract current battery level
```

**Pros**:
- ✅ Real-time accurate (0-2 seconds old)
- ✅ Most accurate battery level at return moment

**Cons**:
- ❌ Adds 1-2 second latency to return API
- ❌ Requires device online and responding
- ❌ Can timeout if device offline
- ❌ Blocks return API call
- ⚠️ **CREATES REDUNDANT DATA FLOW**

---

## ⚠️ THE REDUNDANCY PROBLEM

### Scenario: Using Check Command for Return Event

```
Timeline of Events:
─────────────────────────────────────────────────────────────

Time 0:00  → Device uploads via HTTP (/api/rentbox/upload/data)
             ├─► Battery levels: PB1=85%, PB2=90%, PB3=75%
             ├─► Synced to Django Main App ✅
             └─► Cached in Redis for 30 min

Time 0:15  → PowerBank returned (PB1 returned to slot 1)
             └─► System calls /api/rentbox/order/return
                 ├─► If using CACHE approach:
                 │   ├─► Get battery from cached upload data (85%)
                 │   └─► Send return event to Django ✅
                 │       └─► Django already has full station data from 0:00
                 │
                 ├─► If using CHECK command:
                 │   ├─► Send MQTT check command to device
                 │   ├─► Device responds with FULL station data
                 │   │   └─► All powerbanks: PB1=85%, PB2=90%, PB3=75%
                 │   ├─► Extract PB1 battery (85%)
                 │   └─► Send return event to Django ✅
                 │       
                 │   ⚠️ PROBLEM: Django ALREADY has this data from 0:00!
                 │   ⚠️ If we also sync check command data to Django:
                 │       └─► DUPLICATE: Same station state sent twice
                 │       └─► Only 15 minutes apart
                 │       └─► Battery levels probably unchanged

Time 0:20  → Device regular upload again (/api/rentbox/upload/data)
             ├─► Updated battery levels
             └─► Synced to Django ✅ (this is expected)
```

---

## 🚨 Key Insight

### Hardware Upload Frequency: Every 20 minutes
### Cache TTL: 30 minutes
### Activity window: 25 minutes

**Analysis**:
- Hardware uploads happen **automatically every 20 minutes**
- Check command is **on-demand** (only when we request it)
- Check command gets **same data structure** as upload endpoint
- If we sync check command data to Django → **DUPLICATE DATA**

---

## ✅ RECOMMENDED SOLUTION: Keep Cache Approach

### Why Cache is Better:

1. **Uses Hardware's Natural Flow**
   - Device uploads every 20 minutes automatically
   - We already sync this to Django
   - Return event uses this existing data
   - **NO ADDITIONAL HARDWARE CALLS**

2. **Prevents Duplicate Syncs**
   - Only sync on scheduled uploads (every 20 min)
   - Return event just notifies Django (type=returned)
   - Django already has current station state
   - **NO REDUNDANT DATA**

3. **Battery Level Accuracy is Acceptable**
   - Worst case: 20 minutes old (if return happens right before next upload)
   - Average case: 10 minutes old
   - Battery level doesn't change dramatically in 20 minutes while powerbank is in station
   - **GOOD ENOUGH FOR BUSINESS LOGIC**

4. **Performance**
   - No MQTT round-trip delay
   - No blocking on return API
   - Works even if device temporarily offline
   - **FASTER RESPONSE**

---

## 🔧 Optimized Implementation (CURRENT)

```java
// ApiController.java - /api/rentbox/order/return endpoint

// STEP 1: Try to get battery from cached upload data (FAST)
String cacheKey = "upload_data:" + rentboxSN;
byte[] cachedBytes = (byte[]) redisTemplate.boundValueOps(cacheKey).get();

int batteryLevel = 0;
if (cachedBytes != null) {
    ReceiveUpload cachedData = new ReceiveUpload(cachedBytes);
    for (Powerbank pb : cachedData.getPowerbanks()) {
        if (pb.getSnAsString().equals(powerbankSN)) {
            batteryLevel = pb.getPower();  // From last scheduled upload
            break;
        }
    }
}

// STEP 2: Send return event to Django (with battery level from cache)
chargeGharConnector.sendReturnedData(
    rentboxSN, 
    powerbankSN, 
    slotNumber, 
    batteryLevel  // 0-30 minutes old, but acceptable
);
```

**Data flow to Django**:
```
Hardware upload (every 20 min) → sync full station data to Django
Return event → send notification with cached battery level
Next upload (within 20 min) → sync updated station data to Django

Result: Django gets regular updates + return notifications
        NO duplicate data syncs
        Battery level accuracy: ~10-15 min average staleness
```

---

## 📋 Summary

### Hardware Communication Pattern:
1. **Regular uploads**: HTTP POST every 20 minutes to `/api/rentbox/upload/data`
2. **Command responses**: MQTT responses on `/user/update` when server sends check
3. **Heartbeats**: MQTT heartbeats on `/user/heart`

### Best Practice for Return Events:
✅ **USE CACHED DATA** (current implementation)
- Uses hardware's natural upload cycle
- Prevents redundant MQTT commands
- Prevents duplicate Django syncs
- Battery level accuracy: ~10-15 min average (acceptable)
- Fast, reliable, no blocking

❌ **AVOID CHECK COMMAND** for routine return events
- Creates additional MQTT traffic
- Adds latency (1-2 seconds)
- Risk of timeout if device offline
- Would create duplicate data if synced
- Only use check for manual troubleshooting/admin panel

### Current Implementation: ✅ CORRECT
The cache-based approach is the right choice for production.
