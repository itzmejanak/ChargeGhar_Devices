# Testing Commands Flow

## 🔧 Docker Build & Deploy
```bash
cd /home/revdev/Desktop/Daily/Devalaya/PowerBank/Emqx/ChargeGhar_Devices
docker-compose down
docker-compose build --no-cache
docker-compose up -d
sleep 10  # Wait for startup
```

## 📡 EMQX Testing

### Device Command Test
```bash
# Send command to device
curl -X POST "http://localhost:8080/send?deviceName=860588041468359&data=TEST_COMMAND"

# Monitor device topic (simulate device listening)
mosquitto_sub -h qd081a20.ala.dedicated.aws.emqxcloud.com -p 1883 -u "chargeghar" -P "5060" -t "powerbank/860588041468359/user/command" -v
```

### Production Endpoints Test
```bash
# Device connection validation
curl -X POST "http://localhost:8080/api/iot/client/con" \
  -H "Content-Type: application/json" \
  -d '{"productKey":"powerbank","deviceName":"860588041468359","clientId":"test_device_001"}'

# Device configuration
curl -X POST "http://localhost:8080/api/rentbox/config/data" \
  -H "Content-Type: application/json" \
  -d '{"sn":"860588041468359","version":"1.0"}'

# Clear device
curl -X POST "http://localhost:8080/api/iot/client/clear" \
  -H "Content-Type: application/json" \
  -d '{"productKey":"powerbank","deviceName":"860588041468359"}'
```

## 📊 EMQX Cloud Monitoring

### Check Connections
```bash
curl -X GET "https://qd081a20.ala.dedicated.aws.emqxcloud.com:8443/api/v5/clients" \
  -u "mb450eff10110460:I0HL2xIoXc.3Qzt5H9A.Wg0yWp395-oC" | jq '.meta'
```

### Check Metrics
```bash
curl -X GET "https://qd081a20.ala.dedicated.aws.emqxcloud.com:8443/api/v5/metrics" \
  -u "mb450eff10110460:I0HL2xIoXc.3Qzt5H9A.Wg0yWp395-oC" | jq '.[0] | {
  "messages_published": ."messages.publish",
  "messages_received": ."messages.received", 
  "authorization_allow": ."authorization.allow",
  "authorization_deny": ."authorization.deny"
}'
```

### Check Subscriptions
```bash
# Get subscriber client ID first from connections, then:
curl -X GET "https://qd081a20.ala.dedicated.aws.emqxcloud.com:8443/api/v5/clients/{SUBSCRIBER_CLIENT_ID}/subscriptions" \
  -u "mb450eff10110460:I0HL2xIoXc.3Qzt5H9A.Wg0yWp395-oC" | jq '.'
```

## 🔍 Application Logs
```bash
# Real-time logs
docker logs -f iotdemo-app

# Last 20 lines
docker logs iotdemo-app 2>&1 | tail -20
```

## ✅ Quick Health Check
```bash
# Full system test sequence
curl -X POST "http://localhost:8080/send?deviceName=860588041468359&data=HEALTH_CHECK" && \
docker logs iotdemo-app 2>&1 | tail -5 | grep "Message sent"
```

## 📝 Expected Results

### Successful Command Send:
- **HTTP Response:** `{"code":200,"type":0,"data":null,"msg":"ok"}`
- **App Log:** `Message sent to device 860588041468359 on topic: powerbank/860588041468359/user/command`
- **EMQX Log:** `✅ Message delivered successfully to topic: powerbank/860588041468359/user/command`

### EMQX Metrics:
- **messages.publish:** Incrementing count
- **authorization.allow:** Successful operations
- **authorization.deny:** Should be 1 (old wildcard test)

### Connection Status:
- **Publisher:** `iotdemo-server-publisher-{timestamp}`
- **Subscriber:** `iotdemo-server-subscriber-{timestamp}`
- **Count:** 2 active connections