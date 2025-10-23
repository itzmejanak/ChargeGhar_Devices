#!/bin/bash

# Script to verify MQTT topic fix is deployed correctly
# Run this to check logs and confirm topic configuration

echo "=========================================="
echo "🔍 MQTT TOPIC VERIFICATION"
echo "=========================================="
echo ""

# Check 1: Verify MQTT Subscriber subscriptions
echo "1️⃣ Checking MQTT Subscriber Subscriptions..."
echo "Expected: Should subscribe to /powerbank/+/user/upload and /powerbank/+/user/heart"
echo ""
docker logs iotdemo-app 2>&1 | grep -A 10 "MQTT Subscriber connected" | tail -15
echo ""
echo "✅ Should see:"
echo "   - Subscribed to: /powerbank/+/user/upload"
echo "   - Subscribed to: /powerbank/+/user/heart"
echo ""
echo "=========================================="
echo ""

# Check 2: Verify MQTT Publisher connection
echo "2️⃣ Checking MQTT Publisher Connection..."
echo "Expected: Should be connected"
echo ""
docker logs iotdemo-app 2>&1 | grep "MQTT Publisher connected"
echo ""
echo "=========================================="
echo ""

# Check 3: Check for topic-related messages
echo "3️⃣ Checking Message Delivery Logs..."
echo "Expected: Should see messages sent to /powerbank/.../user/get"
echo ""
docker logs iotdemo-app --tail 50 2>&1 | grep -E "Message sent to device|on topic:" | tail -10
echo ""
echo "✅ Should see topics like: /powerbank/864601069946994/user/get"
echo ""
echo "=========================================="
echo ""

# Check 4: Check for any errors
echo "4️⃣ Checking for Errors..."
echo ""
docker logs iotdemo-app --tail 100 2>&1 | grep -i -E "error|exception|failed" | grep -v "SerialPortException" | tail -10
echo ""
echo "=========================================="
echo ""

# Check 5: Recent device messages
echo "5️⃣ Recent Device Upload Messages..."
echo "Expected: Should see device data uploads"
echo ""
docker logs iotdemo-app --tail 50 2>&1 | grep -A 5 "REQUEST PARAMETERS" | tail -20
echo ""
echo "=========================================="
echo ""

# Check 6: Device heartbeats
echo "6️⃣ Recent Heartbeat Messages..."
echo "Expected: Should see heartbeat received from device"
echo ""
docker logs iotdemo-app --tail 100 2>&1 | grep -i "heartbeat" | tail -5
echo ""
echo "=========================================="
echo ""

echo "📊 SUMMARY"
echo "=========================================="
echo ""
echo "If all checks pass, the fix is deployed correctly:"
echo "✅ MQTT Subscriber listening to /powerbank/+/user/upload"
echo "✅ MQTT Subscriber listening to /powerbank/+/user/heart"
echo "✅ MQTT Publisher connected"
echo "✅ Commands sent to /powerbank/.../user/get"
echo "✅ Device messages being received"
echo ""
echo "Now run: bash TEST_COMMANDS.sh to test actual commands!"
echo ""
