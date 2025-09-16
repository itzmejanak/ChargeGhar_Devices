# API Endpoints Analysis & Test Plan

## Step 1: Complete API Endpoints List

### API Controller Endpoints (Device Communication)
1. **GET/POST `/api/iot/client/con`** 
   - Purpose: Device connection configuration endpoint
   - Input: ApiIotClientConValid (uuid, sign), Optional POST data (hardware version)
   - Output: CSV string with device config (uuid, productKey, host, port, iotId, iotToken, timestamp)
   - Redis: Reads/writes `clientConect:{uuid}`, `hardware:{uuid}`
   - MQTT: None directly

2. **GET `/api/iot/client/clear`**
   - Purpose: Clear cached device connection config
   - Input: deviceName parameter
   - Output: HttpResult (success/error)
   - Redis: Expires `clientConect:{deviceName}` key
   - MQTT: None

3. **GET `/api/rentbox/order/return`**
   - Purpose: Power bank return notification
   - Input: ApiRentboxOrderReturnValid (with sign)
   - Output: HttpResult
   - Redis: None directly
   - MQTT: Logs to MqttSubscriber messageBody list

4. **POST `/api/rentbox/upload/data`**
   - Purpose: Receive raw device data upload
   - Input: byte[] data, rentboxSN, sign, signal
   - Output: HttpResult
   - Redis: None directly (processed via ReceiveUpload)
   - MQTT: Logs to MqttSubscriber messageBody list

5. **GET `/api/rentbox/config/data`**
   - Purpose: Return device configuration JSON
   - Input: None
   - Output: Static config JSON
   - Redis: None
   - MQTT: None

### Show Controller Endpoints (Device Commands)
6. **GET `/show.html`**
   - Purpose: Device control web interface
   - Input: deviceName parameter
   - Output: ModelAndView with device status and controls
   - Redis: Reads `hardware:{deviceName}`, device heartbeat
   - MQTT: Gets device online status via MqttPublisher

7. **GET `/send`**
   - Purpose: Send raw command to device
   - Input: deviceName, data
   - Output: HttpResult
   - Redis: None directly
   - MQTT: Publishes to `/{productKey}/{deviceName}/get` topic

8. **GET `/check`**
   - Purpose: Check device/powerbank status
   - Input: deviceName
   - Output: Powerbank list
   - Redis: Uses `check:{deviceName}` for response waiting
   - MQTT: Sends check command, waits for device response

9. **GET `/check_all`**
   - Purpose: Check all powerbanks in device
   - Input: deviceName
   - Output: Complete powerbank status
   - Redis: Uses `check:{deviceName}` for response waiting
   - MQTT: Sends check_all command

10. **GET `/popup_random`**
    - Purpose: Pop out a random powerbank with minimum power
    - Input: deviceName, minPower
    - Output: Powerbank serial number
    - Redis: Uses `popup_sn:{deviceName}` for response waiting
    - MQTT: Sends popup command

### Index Controller Endpoints (Web Interface)
11. **GET `/index.html`**
    - Purpose: Main dashboard
    - Input: None
    - Output: ModelAndView with device list and status
    - Redis: Reads `hardware:*` keys
    - MQTT: Gets device status via MqttPublisher

12. **GET `/device/create`**
    - Purpose: Add new device to machines.properties
    - Input: deviceName
    - Output: HttpResult
    - Redis: None
    - MQTT: None

13. **GET `/`**
    - Purpose: Root redirect to index
    - Input: None
    - Output: Redirect
    - Redis: None
    - MQTT: None

### Listen Controller Endpoints (MQTT Monitoring)
14. **GET `/listen.html`**
    - Purpose: MQTT message monitoring interface
    - Input: None
    - Output: ModelAndView with MQTT status
    - Redis: None
    - MQTT: Shows MqttSubscriber status

15. **GET `/listen`**
    - Purpose: Get recent MQTT messages
    - Input: None
    - Output: List of MessageBody objects
    - Redis: None
    - MQTT: Returns MqttSubscriber.getMessageBodys()

16. **GET `/listen/start`**
    - Purpose: Start MQTT subscription
    - Input: None
    - Output: HttpResult
    - Redis: None
    - MQTT: Calls MqttSubscriber.startQueue()

17. **GET `/listen/stop`**
    - Purpose: Stop MQTT subscription
    - Input: None
    - Output: HttpResult
    - Redis: None
    - MQTT: Calls MqttSubscriber.stopQueue()

18. **GET `/listen/clear`**
    - Purpose: Clear message history
    - Input: None
    - Output: HttpResult
    - Redis: None
    - MQTT: Calls MqttSubscriber.clearMessageBody()

### Test Controller Endpoints
19. **GET `/test`**
    - Purpose: Basic test endpoint
    - Input: None
    - Output: Simple response
    - Redis: None
    - MQTT: None

20. **GET `/health`**
    - Purpose: Health check
    - Input: None
    - Output: System status
    - Redis: None
    - MQTT: None

## Step 2: MQTT Topic & Redis Key Mapping

### MQTT Topics Used:
- **Subscription Topics:**
  - `device/+/upload` - Device data uploads
  - `device/+/status` - Device status updates

- **Publish Topics:**
  - `device/{deviceName}/command` - Commands to devices
  - `/{productKey}/{deviceName}/get` - Legacy topic format
  - `/{productKey}/{deviceName}/user/get` - Legacy with user path

### Redis Keys Used:
- `clientConect:{uuid}` - Device connection config cache
- `hardware:{uuid}` - Device hardware version
- `device_heartbeat:{deviceName}` - Device online status
- `check:{deviceName}` - Command response waiting
- `popup_sn:{deviceName}` - Popup command response waiting

## Step 3: Test Scenarios

### Scenario 1: Device Connection Flow
1. Device calls `/api/iot/client/con` with uuid
2. Server returns EMQX connection config
3. Device connects to EMQX using returned config
4. Device starts sending heartbeats to `device/{deviceName}/status`

### Scenario 2: Device Command Flow
1. User calls `/check?deviceName=test001`
2. Server publishes check command to MQTT
3. Device receives command and responds
4. Server processes response and returns to user

### Scenario 3: MQTT Message Monitoring
1. Start MQTT subscriber via `/listen/start`
2. Send test messages to subscribed topics
3. Monitor received messages via `/listen`
4. Verify message processing and Redis updates

### Scenario 4: Real-time Device Status
1. Simulate device heartbeats to `device/test001/status`
2. Check device status via `/show.html?deviceName=test001`
3. Verify online/offline status updates

## Step 4: Testing Tools & Setup

### Required Tools:
1. **MQTTX** or **Mosquitto** - MQTT client for simulating devices
2. **Postman** or **curl** - HTTP API testing
3. **redis-cli** - Redis data verification
4. **Browser** - Web interface testing

### Test Environment Setup:
1. Start Redis server (localhost:6379, password: 5060)
2. Configure EMQX connection in config.properties
3. Start Java application
4. Connect MQTT test client to EMQX

## Step 5: Step-by-Step Test Execution Plan

### Phase 1: Basic Connectivity
1. Start application and verify startup logs
2. Test health endpoint: `GET /health`
3. Test MQTT connection: `GET /listen/start`
4. Verify EMQX console shows connection

### Phase 2: Device Configuration
1. Test device config endpoint: `GET /api/iot/client/con?uuid=test001&sign={calculated}`
2. Verify Redis key `clientConect:test001` is created
3. Test config clearing: `GET /api/iot/client/clear?deviceName=test001`

### Phase 3: MQTT Message Flow
1. Use MQTTX to publish to `device/test001/upload` with test payload
2. Verify message appears in `/listen` endpoint
3. Check Redis for any stored data
4. Test command sending via `/send?deviceName=test001&data=test`

### Phase 4: Device Commands
1. Simulate device heartbeat to `device/test001/status`
2. Test device status check: `GET /check?deviceName=test001`
3. Simulate device response to `device/test001/upload`
4. Verify command response processing

### Phase 5: Web Interface
1. Access main dashboard: `GET /index.html`
2. Access device control: `GET /show.html?deviceName=test001`
3. Test message monitoring: `GET /listen.html`
4. Verify all UI components load correctly

### Expected Results:
- All endpoints return appropriate responses
- MQTT messages flow correctly between test client and server
- Redis keys are created/updated as expected
- Web interfaces display real-time data
- Error handling works for invalid requests

This comprehensive test plan will verify your EMQX integration works correctly without requiring real devices.