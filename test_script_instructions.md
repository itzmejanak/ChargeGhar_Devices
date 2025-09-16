# Test Script Setup Instructions

## Prerequisites

1. **Install Python Dependencies:**
```bash
pip install paho-mqtt requests redis
```

2. **Ensure Services are Running:**
   - Java Spring Boot application on `http://localhost:8080`
   - Redis server on `localhost:6379` with password `5060`
   - EMQX Cloud connection configured in your `config.properties`

## How to Run

1. **Start your Java application:**
```bash
cd /path/to/your/project
mvn spring-boot:run
```

2. **Run the test script:**
```bash
python test_emqx_integration.py
```

## What the Script Tests

### ✅ Connection Tests
- Redis connectivity and authentication
- EMQX MQTT broker connection with SSL
- Java application health endpoint

### ✅ API Endpoints
- `/health` - Health check
- `/api/iot/client/con` - Device configuration (with proper signature)
- `/listen/start` - Start MQTT subscriber
- `/send` - Send commands to devices
- `/check` - Device status check with Redis response handling
- `/listen` - MQTT message monitoring
- `/index.html` - Main dashboard
- `/show.html` - Device control page

### ✅ MQTT Message Flow
- Publishes test messages to `device/{deviceName}/upload`
- Simulates device heartbeats to `device/{deviceName}/status`
- Verifies your Java server receives and processes messages
- Tests command publishing to device topics

### ✅ Redis Integration
- Device configuration caching (`clientConect:{uuid}`)
- Hardware version storage (`hardware:{uuid}`)
- Device heartbeat tracking (`device_heartbeat:{deviceName}`)
- Command response handling (`check:{deviceName}`, `popup_sn:{deviceName}`)

### ✅ Signature Validation
- Implements the exact same MD5 signature algorithm as your Java `SignUtils`
- Tests API endpoints that require signature validation

## Expected Output

The script will show real-time progress:
```
🚀 Starting ChargeGhar Device Test Suite
✅ Redis connection successful
✅ MQTT connection successful
✅ Subscribed to: device/+/upload, device/+/status
🔍 Testing Health Endpoint...
✅ Health endpoint working
...
📊 TEST RESULTS SUMMARY
Total Tests: 10
Passed: 10
Failed: 0
Success Rate: 100.0%
🎉 ALL TESTS PASSED! Your EMQX integration is working correctly.
```

## Troubleshooting

### If Redis Connection Fails:
- Check Redis is running: `redis-cli -h 127.0.0.1 -p 6379 -a 5060 ping`
- Verify password in config.properties matches script

### If MQTT Connection Fails:
- Check EMQX credentials in config.properties
- Verify SSL certificates and network connectivity
- Check EMQX Cloud console for connection attempts

### If API Tests Fail:
- Ensure Java application is running on port 8080
- Check application logs for errors
- Verify endpoints are accessible: `curl http://localhost:8080/health`

### If Signature Tests Fail:
- Check the signature calculation algorithm
- Verify parameter ordering and formatting

## Script Features

### Accurate Implementation
- **Exact signature matching** with your Java SignUtils
- **Proper MQTT topic structure** matching your MqttPublisher/Subscriber
- **Correct Redis key patterns** used by your DeviceCommandUtils
- **Real binary data simulation** for device responses

### Comprehensive Coverage
- Tests all critical integration points
- Simulates real device behavior without hardware
- Validates end-to-end data flow: API → MQTT → Redis → Response

### Safe Testing
- Automatically cleans up test data
- Uses non-conflicting test device IDs
- Handles connection failures gracefully

This script will definitively prove your EMQX migration is working correctly!