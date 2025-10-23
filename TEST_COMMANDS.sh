#!/bin/bash

# MQTT Topic Fix - Production Testing Commands
# Server: https://api.chargeghar.com
# Device: 864601069946994
# Date: October 23, 2025

BASE_URL="https://api.chargeghar.com"
DEVICE_NAME="864601069946994"

echo "=========================================="
echo "🧪 TESTING PRODUCTION DEPLOYMENT"
echo "Server: $BASE_URL"
echo "Device: $DEVICE_NAME"
echo "=========================================="
echo ""

# Test 1: Check Command (Synchronous - waits for device response)
echo "📋 Test 1: CHECK Command (Get device status)"
echo "Expected: Returns powerbank data within 10 seconds"
echo "Command:"
echo "curl -X GET '$BASE_URL/check?deviceName=$DEVICE_NAME'"
echo ""
echo "Executing..."
curl -X GET "$BASE_URL/check?deviceName=$DEVICE_NAME" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n" \
  -s | jq '.' || cat
echo ""
echo "=========================================="
echo ""

# Test 2: Check All Command
echo "📋 Test 2: CHECK ALL Command (Get all powerbank slots)"
echo "Expected: Returns all 25+ slots data"
echo "Command:"
echo "curl -X GET '$BASE_URL/check_all?deviceName=$DEVICE_NAME'"
echo ""
echo "Executing..."
curl -X GET "$BASE_URL/check_all?deviceName=$DEVICE_NAME" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n" \
  -s | jq '.' || cat
echo ""
echo "=========================================="
echo ""

# Test 3: Random Popup Command (Min Power 0%)
echo "🔓 Test 3: POPUP RANDOM Command (minPower=0%)"
echo "Expected: Pops out a powerbank with any power level"
echo "Command:"
echo "curl -X GET '$BASE_URL/popup_random?deviceName=$DEVICE_NAME&minPower=0'"
echo ""
echo "Executing..."
curl -X GET "$BASE_URL/popup_random?deviceName=$DEVICE_NAME&minPower=0" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n" \
  -s | jq '.' || cat
echo ""
echo "=========================================="
echo ""

# Test 4: Random Popup Command (Min Power 50%)
echo "🔓 Test 4: POPUP RANDOM Command (minPower=50%)"
echo "Expected: Pops out a powerbank with at least 50% battery"
echo "Command:"
echo "curl -X GET '$BASE_URL/popup_random?deviceName=$DEVICE_NAME&minPower=50'"
echo ""
echo "Executing..."
curl -X GET "$BASE_URL/popup_random?deviceName=$DEVICE_NAME&minPower=50" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n" \
  -s | jq '.' || cat
echo ""
echo "=========================================="
echo ""

# Test 5: Manual Send Command (Async)
echo "📤 Test 5: SEND Command (Manual async command)"
echo "Expected: Returns 200 OK immediately (async)"
echo "Command:"
echo "curl -X POST '$BASE_URL/send' -d 'deviceName=$DEVICE_NAME&data={\"cmd\":\"check\"}'"
echo ""
echo "Executing..."
curl -X POST "$BASE_URL/send" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "deviceName=$DEVICE_NAME&data={\"cmd\":\"check\"}" \
  -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n" \
  -s | jq '.' || cat
echo ""
echo "=========================================="
echo ""

# Test 6: Show Page (Check topic display)
echo "🌐 Test 6: Show Page (Verify topic display)"
echo "Expected: HTML page showing /powerbank/864601069946994/user/get"
echo "Command:"
echo "curl -s '$BASE_URL/show.html?deviceName=$DEVICE_NAME' | grep -E 'Topic|getTopic|updateTopic|user/get|user/upload'"
echo ""
echo "Executing..."
curl -s "$BASE_URL/show.html?deviceName=$DEVICE_NAME" | grep -oP '(Topic|/powerbank/[^"<]+)' | head -20
echo ""
echo "=========================================="
echo ""

# Test 7: Index Page (Check device online status)
echo "🏠 Test 7: Index Page (Check device online status)"
echo "Expected: Device shows as ONLINE"
echo "Command:"
echo "curl -s '$BASE_URL/index.html' | grep -A5 '$DEVICE_NAME'"
echo ""
echo "Executing..."
curl -s "$BASE_URL/index.html" | grep -B2 -A5 "$DEVICE_NAME" | head -20
echo ""
echo "=========================================="
echo ""

echo "✅ All test commands executed!"
echo ""
echo "📊 Next Steps:"
echo "1. Share the output with me"
echo "2. Check Docker logs: docker logs iotdemo-app --tail 100"
echo "3. Check EMQX Cloud for message delivery"
echo "4. Verify Redis keys for device activity"
echo ""
echo "🔍 Expected Results:"
echo "  - Check commands: Should return powerbank data (not timeout)"
echo "  - Popup commands: Should return powerbank SN (hardware executes)"
echo "  - Send command: Should return 200 OK immediately"
echo "  - Topics should show: /powerbank/.../user/get"
echo ""
