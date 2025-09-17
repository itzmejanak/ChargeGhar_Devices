# EMQX Integration Action Plan

## Current Issues Analysis

### Problems Identified:
1. **No EMQX API Integration** - Using basic MQTT without platform management
2. **Hardcoded Authentication** - All devices use same credentials (chargeghar/5060)
3. **No Device Registration** - Devices not properly registered with EMQX platform
4. **Missing Device Management** - No lifecycle management through EMQX APIs

### Manufacturer Feedback:
- "No EMQX related interface integrated"
- "Device not registered with IoT platform"
- "Device account and password not obtained from IoT"

## Implementation Plan

### Phase 1: EMQX API Integration (Priority: HIGH)

#### Step 1.1: Create EMQX Management API Client
```java
// New class: src/main/java/com.demo/emqx/EmqxApiClient.java
- HTTP client for EMQX REST APIs
- Authentication handling
- Error handling and retry logic
```

#### Step 1.2: Device Registration Service
```java
// New class: src/main/java/com.demo/emqx/EmqxDeviceService.java
- Register devices with EMQX platform
- Generate unique credentials per device
- Manage device lifecycle
```

#### Step 1.3: Update Configuration
```properties
# Add to config.properties
emqx.api.url=https://l8288d7f.ala.asia-southeast1.emqxsl.com:18083
emqx.api.key=your-api-key
emqx.api.secret=your-api-secret
```

### Phase 2: Authentication System (Priority: HIGH)

#### Step 2.1: Per-Device Credentials
- Generate unique username/password for each device
- Store credentials in Redis with expiration
- Update `/api/iot/client/con` to return device-specific credentials

#### Step 2.2: Device Registration Flow
```
1. Device calls /api/iot/client/con with UUID
2. Server checks if device exists in EMQX
3. If not exists: Register device via EMQX API
4. Generate/retrieve device credentials
5. Return EMQX connection config with device credentials
```

### Phase 3: Implementation Details

#### Step 3.1: EMQX API Endpoints to Implement
1. **Device Registration**: `POST /api/v5/authentication/password_based:built_in_database/users`
2. **Device Management**: `GET/PUT/DELETE /api/v5/authentication/password_based:built_in_database/users/{username}`
3. **Message Publishing**: `POST /api/v5/publish`

#### Step 3.2: Updated Device Connection Flow
```java
@RequestMapping("/api/iot/client/con")
public HttpResult iotClientCon(ApiIotClientConValid valid) {
    // 1. Validate device signature
    // 2. Check if device registered in EMQX
    // 3. If not registered: Register via EMQX API
    // 4. Generate/retrieve device credentials
    // 5. Return device-specific MQTT config
}
```

## Technical Implementation

### New Classes to Create:

1. **EmqxApiClient.java** - EMQX REST API client
2. **EmqxDeviceService.java** - Device management service
3. **DeviceCredentials.java** - Device credential model
4. **EmqxConfig.java** - EMQX API configuration

### Modified Classes:

1. **ApiController.java** - Update device connection endpoint
2. **MqttPublisher.java** - Use device-specific credentials
3. **MqttSubscriber.java** - Handle device-specific topics
4. **AppConfig.java** - Add EMQX API configuration

## Testing Strategy

### Phase 1 Testing:
1. Test EMQX API connectivity
2. Test device registration via API
3. Verify device credentials generation

### Phase 2 Testing:
1. Test device connection with new credentials
2. Verify MQTT message flow
3. Test device lifecycle management

### Phase 3 Testing:
1. End-to-end device flow testing
2. Load testing with multiple devices
3. Error handling and recovery testing

## Next Immediate Actions:

1. **Get EMQX API Credentials** - Request API key/secret from EMQX console
2. **Implement EmqxApiClient** - Create HTTP client for EMQX APIs
3. **Update Device Registration** - Modify `/api/iot/client/con` endpoint
4. **Test with Single Device** - Verify registration and connection flow

## Success Criteria:

- ✅ Devices register automatically with EMQX platform
- ✅ Each device gets unique credentials
- ✅ MQTT connection works with device-specific auth
- ✅ Manufacturer can see registered devices in EMQX console
- ✅ Message flow works end-to-end

## Timeline:
- **Phase 1**: 2-3 days (API integration)
- **Phase 2**: 1-2 days (Authentication system)
- **Phase 3**: 1 day (Testing and refinement)

**Total Estimated Time**: 4-6 days