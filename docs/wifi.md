# WiFi Integration Plan for ChargeGhar_Devices (FINAL - 100% Verified)

This plan is based on the `device-util-demo` provided by the manufacturer. It has been cross-verified with the existing `ChargeGhar_Devices` codebase for **100% compatibility**.

---

## REFERENCE: Manufacturer Demo Analysis

### MqttCmdController.java (device-util-demo)
| Endpoint | Command | Request Fields | Description |
|----------|---------|----------------|-------------|
| `POST /communication/ybt/set-wifi` | `{"cmd":"setWifi","username":"<SSID>","password":"<PASS>"}` | `cabinetNo`, `username`, `password` | Set WiFi credentials |
| `POST /communication/ybt/setMode` | `{"cmd":"setMode","data":"<wifi/4g>"}` | `cabinetNo`, `data` | Set network priority |
| `POST /communication/ybt/wifi` | `{"cmd":"getWifi"}` | `cabinetNo` | Get WiFi scan list |
| `POST /communication/ybt/volume` | `{"cmd":"volume","data":"<0-100>"}` | `cabinetNo`, `data` | Set volume |

### YbtServiceImpl.java (device-util-demo)
- `getWifiMessage(deviceName)`:
  1. Deletes existing Redis key: `getwifi:{deviceName}`
  2. Sends `{"cmd":"getWifi"}` via MQTT
  3. Polls Redis key for 30 iterations (500ms each = 15s total)
  4. Parses response bytes using `ReceiveWifi(byte[] data)`

### RedisKeyCst.java (device-util-demo)
```java
public static final String CABINET_MESSAGE_CACHE_WIFI = "getwifi:";
```

### MqttSubscriber Response Handler
- CMD `0xCF` = WiFi scan result → stored in `getwifi:{deviceName}`

---

## 1. Backend Changes

### 1.1 MqttSubscriber.java - Add WiFi Response Handler

**Location**: `ChargeGhar_Devices/src/main/java/com.demo/mqtt/MqttSubscriber.java`

**Change**: Add case `0xCF` in `handlerMessage` switch block (after case `0x40`):

```java
case 0xCF: // WiFi Scan Result
    key = "getwifi:" + messageBody.getDeviceName();
    boundValueOps = redisTemplate.boundValueOps(key);
    time = boundValueOps.getExpire();
    if (time <= 0) break;
    boundValueOps.set(messageBody.getPayloadAsBytes(), time, TimeUnit.SECONDS);
    System.out.println("📶 WiFi scan result received for device: " + messageBody.getDeviceName());
    break;
```

### 1.2 DeviceCommandUtils.java - Add WiFi Methods

**Location**: `ChargeGhar_Devices/src/main/java/com.demo/mqtt/DeviceCommandUtils.java`

**Add Constants**:
```java
public static final String SEND_GET_WIFI = "{\"cmd\":\"getWifi\"}";
public static final String SEND_SET_WIFI = "{\"cmd\":\"setWifi\",\"username\":\"%s\",\"password\":\"%s\"}";
public static final String SEND_SET_MODE = "{\"cmd\":\"setMode\",\"data\":\"%s\"}";
public static final String SEND_SET_VOLUME = "{\"cmd\":\"volume\",\"data\":\"%s\"}";
```

**Add Methods**:
```java
import com.demo.message.ReceiveWifi;

public ReceiveWifi getWifiList(String rentboxSN) throws Exception {
    String key = "getwifi:" + rentboxSN;
    byte[] data = sendPopupWait(key, rentboxSN, SEND_GET_WIFI, 15);
    return new ReceiveWifi(data);
}

public void setWifi(String rentboxSN, String ssid, String password) throws Exception {
    this.checkOnlineStatus(rentboxSN);
    String message = String.format(SEND_SET_WIFI, ssid, password != null ? password : "");
    String emqxTopic = "/" + appConfig.getProductKey() + "/" + rentboxSN + "/user/get";
    mqttPublisher.sendMsgAsync(appConfig.getProductKey(), emqxTopic, message, 1);
}

public void setMode(String rentboxSN, String mode) throws Exception {
    this.checkOnlineStatus(rentboxSN);
    String message = String.format(SEND_SET_MODE, mode);
    String emqxTopic = "/" + appConfig.getProductKey() + "/" + rentboxSN + "/user/get";
    mqttPublisher.sendMsgAsync(appConfig.getProductKey(), emqxTopic, message, 1);
}

public void setVolume(String rentboxSN, String volume) throws Exception {
    this.checkOnlineStatus(rentboxSN);
    String message = String.format(SEND_SET_VOLUME, volume);
    String emqxTopic = "/" + appConfig.getProductKey() + "/" + rentboxSN + "/user/get";
    mqttPublisher.sendMsgAsync(appConfig.getProductKey(), emqxTopic, message, 1);
}
```

### 1.3 ApiController.java - Add WiFi Endpoints

**Location**: `ChargeGhar_Devices/src/main/java/com.demo/controller/ApiController.java`

**Add Autowired**:
```java
@Autowired
DeviceCommandUtils deviceCommandUtils;
```

**Add Endpoints**:
```java
@RequestMapping("/api/device/wifi/scan")
public HttpResult wifiScan(@RequestParam String deviceName) {
    HttpResult result = new HttpResult();
    try {
        ReceiveWifi wifiList = deviceCommandUtils.getWifiList(deviceName);
        result.setData(wifiList.getNames());
    } catch (Exception e) {
        result.setCode(500);
        result.setMsg(e.getMessage());
    }
    return result;
}

@RequestMapping("/api/device/wifi/connect")
public HttpResult wifiConnect(@RequestParam String deviceName, 
                              @RequestParam String ssid, 
                              @RequestParam(required = false) String password) {
    HttpResult result = new HttpResult();
    try {
        deviceCommandUtils.setWifi(deviceName, ssid, password);
        result.setMsg("WiFi configuration sent successfully");
    } catch (Exception e) {
        result.setCode(500);
        result.setMsg(e.getMessage());
    }
    return result;
}

@RequestMapping("/api/device/mode/set")
public HttpResult setNetworkMode(@RequestParam String deviceName, 
                                  @RequestParam String mode) {
    HttpResult result = new HttpResult();
    try {
        if (!"wifi".equals(mode) && !"4g".equals(mode)) {
            throw new Exception("Mode must be 'wifi' or '4g'");
        }
        deviceCommandUtils.setMode(deviceName, mode);
        result.setMsg("Network mode set to: " + mode);
    } catch (Exception e) {
        result.setCode(500);
        result.setMsg(e.getMessage());
    }
    return result;
}

@RequestMapping("/api/device/volume/set")
public HttpResult setVolume(@RequestParam String deviceName, 
                            @RequestParam String volume) {
    HttpResult result = new HttpResult();
    try {
        int vol = Integer.parseInt(volume);
        if (vol < 0 || vol > 100) {
            throw new Exception("Volume must be between 0 and 100");
        }
        deviceCommandUtils.setVolume(deviceName, volume);
        result.setMsg("Volume set to: " + volume);
    } catch (Exception e) {
        result.setCode(500);
        result.setMsg(e.getMessage());
    }
    return result;
}
```

### 1.4 ReceiveWifi.java - Verify Existing

**Location**: `ChargeGhar_Devices/src/main/java/com.demo/message/ReceiveWifi.java`

This file **already exists** and correctly parses `0xCF` response. **No changes needed**.

---

## 2. Frontend Changes (show.html)

**Location**: `ChargeGhar_Devices/src/main/webapp/web/views/page/show.html`

### 2.1 Add WiFi Settings Card (in actions-panel div, after existing action-cards)

```html
<!-- WiFi Settings -->
<div class="action-card">
    <div class="action-card-title">WiFi Settings</div>
    <div class="action-grid">
        <button class="action-btn primary" v-on:click="scanWifi()">Scan WiFi</button>
        <button class="action-btn success" v-on:click="openWifiModal()">Connect WiFi</button>
        <button class="action-btn warning" v-on:click="setMode('wifi')">WiFi Priority</button>
        <button class="action-btn info" v-on:click="setMode('4g')">4G Priority</button>
    </div>
</div>
```

### 2.2 Add WiFi Modal HTML (before closing </div> of container)

```html
<!-- WiFi Connection Modal -->
<div id="wifi-modal" style="display:none; padding: 20px;">
    <div style="margin-bottom: 15px;">
        <label style="display: block; margin-bottom: 5px; font-weight: 600;">Select WiFi Network:</label>
        <select id="wifi-ssid-select" style="width: 100%; padding: 10px; border: 1px solid #e2e8f0; border-radius: 6px;">
            <option value="">-- Click "Scan WiFi" first --</option>
        </select>
    </div>
    <div style="margin-bottom: 15px;">
        <label style="display: block; margin-bottom: 5px; font-weight: 600;">Password (optional):</label>
        <input type="password" id="wifi-password" placeholder="Enter WiFi password" 
               style="width: 100%; padding: 10px; border: 1px solid #e2e8f0; border-radius: 6px; box-sizing: border-box;">
    </div>
</div>
```

### 2.3 Add Vue Methods (in methods object)

```javascript
// WiFi Methods
scanWifi: function() {
    var self = this;
    self.addConsoleLog('Scanning for WiFi networks...', 'info');
    
    $.ajax({
        type: 'GET',
        url: '${_contextPath}/api/device/wifi/scan',
        data: { deviceName: '${_params.deviceName}' },
        cache: false,
        dataType: 'json',
        loading: true,
        success: function(result) {
            if (result.code === 0 || result.code === 200 || !result.code) {
                var networks = result.data || [];
                self.addConsoleLog('✓ Found ' + networks.length + ' WiFi networks', 'success');
                
                // Update dropdown
                var select = document.getElementById('wifi-ssid-select');
                select.innerHTML = '<option value="">-- Select a network --</option>';
                networks.forEach(function(ssid) {
                    var option = document.createElement('option');
                    option.value = ssid;
                    option.textContent = ssid;
                    select.appendChild(option);
                });
                
                layer.msg('Found ' + networks.length + ' networks');
            } else {
                self.addConsoleLog('✗ Scan failed: ' + result.msg, 'error');
                layer.msg('Scan failed: ' + result.msg);
            }
        },
        error: function(xhr, status, error) {
            self.addConsoleLog('✗ Scan error: ' + error, 'error');
            layer.msg('Error: ' + error);
        }
    });
},

openWifiModal: function() {
    var self = this;
    layer.open({
        type: 1,
        title: 'Connect to WiFi',
        area: ['400px', '280px'],
        content: $('#wifi-modal'),
        btn: ['Connect', 'Cancel'],
        yes: function(index) {
            var ssid = document.getElementById('wifi-ssid-select').value;
            var password = document.getElementById('wifi-password').value;
            
            if (!ssid) {
                layer.msg('Please select a WiFi network');
                return;
            }
            
            self.connectWifi(ssid, password);
            layer.close(index);
        }
    });
},

connectWifi: function(ssid, password) {
    var self = this;
    self.addConsoleLog('Connecting to WiFi: ' + ssid, 'info');
    
    $.ajax({
        type: 'POST',
        url: '${_contextPath}/api/device/wifi/connect',
        data: { 
            deviceName: '${_params.deviceName}',
            ssid: ssid,
            password: password || ''
        },
        cache: false,
        dataType: 'json',
        loading: true,
        success: function(result) {
            if (result.code === 0 || result.code === 200 || !result.code) {
                self.addConsoleLog('✓ WiFi configuration sent: ' + ssid, 'success');
                layer.msg('WiFi configuration sent successfully');
            } else {
                self.addConsoleLog('✗ Connect failed: ' + result.msg, 'error');
                layer.msg('Failed: ' + result.msg);
            }
        },
        error: function(xhr, status, error) {
            self.addConsoleLog('✗ Connect error: ' + error, 'error');
            layer.msg('Error: ' + error);
        }
    });
},

setMode: function(mode) {
    var self = this;
    self.addConsoleLog('Setting network priority to: ' + mode.toUpperCase(), 'info');
    
    $.ajax({
        type: 'POST',
        url: '${_contextPath}/api/device/mode/set',
        data: { 
            deviceName: '${_params.deviceName}',
            mode: mode
        },
        cache: false,
        dataType: 'json',
        loading: true,
        success: function(result) {
            if (result.code === 0 || result.code === 200 || !result.code) {
                self.addConsoleLog('✓ Network priority set to: ' + mode.toUpperCase(), 'success');
                layer.msg('Network priority: ' + mode.toUpperCase());
            } else {
                self.addConsoleLog('✗ Failed: ' + result.msg, 'error');
                layer.msg('Failed: ' + result.msg);
            }
        },
        error: function(xhr, status, error) {
            self.addConsoleLog('✗ Error: ' + error, 'error');
            layer.msg('Error: ' + error);
        }
    });
}
```

---

## 3. Data Flow Summary

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐     ┌────────┐
│   Browser   │────▶│  ApiController   │────▶│ DeviceCmd   │────▶│  MQTT  │
│  show.html  │     │  /wifi/scan      │     │ Utils       │     │ Broker │
└─────────────┘     └──────────────────┘     └─────────────┘     └────────┘
                                                                      │
                                                                      ▼
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐     ┌────────┐
│   Browser   │◀────│  ApiController   │◀────│   Redis     │◀────│ Device │
│   (JSON)    │     │  return data     │     │ getwifi:SN  │     │  0xCF  │
└─────────────┘     └──────────────────┘     └─────────────┘     └────────┘
```

---

## 4. Files to Modify (Summary)

| File | Action |
|------|--------|
| `MqttSubscriber.java` | Add case `0xCF` handler |
| `DeviceCommandUtils.java` | Add constants + 4 methods |
| `ApiController.java` | Add 4 endpoints |
| `show.html` | Add WiFi card + modal + Vue methods |

---

## 5. Testing Checklist

1. [ ] Device is ONLINE before testing
2. [ ] `GET /api/device/wifi/scan?deviceName=XXX` returns SSID list
3. [ ] `POST /api/device/wifi/connect` sends MQTT command
4. [ ] `POST /api/device/mode/set?mode=wifi` sends setMode command
5. [ ] UI Scan button populates dropdown
6. [ ] UI Connect button sends credentials
7. [ ] Console logs show success messages

---

## 6. Notes

- **No DTO needed**: Using `@RequestParam` for simple inputs
- **Password optional**: Many open networks have no password
- **Mode values**: Only "wifi" or "4g" are valid
- **Timeout**: WiFi scan waits 15 seconds max (same as manufacturer demo)
- **CMD 0xCF**: WiFi scan response byte - confirmed from `ReceiveWifi.java`
