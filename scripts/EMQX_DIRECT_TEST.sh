#!/bin/bash

# Direct EMQX Testing Script
# This will help us identify the exact topic device responds on

BROKER="qd081a20.ala.dedicated.aws.emqxcloud.com"
PORT="1883"
USERNAME="chargeghar"
PASSWORD="5060"
DEVICE_NAME="864601069946994"
API_URL="https://qd081a20.ala.dedicated.aws.emqxcloud.com:8443/api/v5"
API_KEY="mb450eff10110460"
API_SECRET="I0HL2xIoXc.3Qzt5H9A.Wg0yWp395-oC"

echo "=========================================="
echo "🔍 EMQX DIRECT TESTING"
echo "=========================================="
echo ""

# Test 1: Check EMQX API - Get device subscriptions
echo "1️⃣ Getting device subscriptions from EMQX API..."
echo ""
curl -s -u "${API_KEY}:${API_SECRET}" \
  "${API_URL}/clients/${DEVICE_NAME}/subscriptions" | jq '.'
echo ""
echo "=========================================="
echo ""

# Test 2: Check all active topics
echo "2️⃣ Getting all active topics with 'powerbank' in name..."
echo ""
curl -s -u "${API_KEY}:${API_SECRET}" \
  "${API_URL}/topics" | jq '.data[] | select(.topic | contains("powerbank"))'
echo ""
echo "=========================================="
echo ""

# Test 3: Get message stats for specific topics
echo "3️⃣ Checking message stats for device topics..."
echo ""
echo "Topic: /powerbank/${DEVICE_NAME}/user/get"
curl -s -u "${API_KEY}:${API_SECRET}" \
  "${API_URL}/topics/%2Fpowerbank%2F${DEVICE_NAME}%2Fuser%2Fget" | jq '.'
echo ""
echo "Topic: /powerbank/${DEVICE_NAME}/user/upload"
curl -s -u "${API_KEY}:${API_SECRET}" \
  "${API_URL}/topics/%2Fpowerbank%2F${DEVICE_NAME}%2Fuser%2Fupload" | jq '.'
echo ""
echo "Topic: /powerbank/${DEVICE_NAME}/user/heart"
curl -s -u "${API_KEY}:${API_SECRET}" \
  "${API_URL}/topics/%2Fpowerbank%2F${DEVICE_NAME}%2Fuser%2Fheart" | jq '.'
echo ""
echo "=========================================="
echo ""

# Test 4: Using mosquitto tools if available
if command -v mosquitto_pub &> /dev/null; then
    echo "4️⃣ Testing with mosquitto_pub (direct MQTT publish)..."
    echo "Publishing test command to /powerbank/${DEVICE_NAME}/user/get"
    echo ""
    mosquitto_pub -h "$BROKER" -p "$PORT" \
      -u "$USERNAME" -P "$PASSWORD" \
      -t "/powerbank/${DEVICE_NAME}/user/get" \
      -m '{"cmd":"check"}' \
      -q 1
    echo "✅ Published! Now subscribe to see response..."
    echo ""
    
    echo "Subscribing to potential response topics for 15 seconds..."
    timeout 15 mosquitto_sub -h "$BROKER" -p "$PORT" \
      -u "$USERNAME" -P "$PASSWORD" \
      -t "/powerbank/${DEVICE_NAME}/#" \
      -v || echo "Timeout - no response received"
    echo ""
else
    echo "4️⃣ mosquitto tools not installed. Install with:"
    echo "   sudo apt-get install mosquitto-clients"
    echo ""
fi

echo "=========================================="
echo ""
echo "📊 ANALYSIS NEEDED:"
echo "1. Check device subscriptions - does it subscribe to /user/get?"
echo "2. Check topic stats - are messages being published/subscribed?"
echo "3. If mosquitto test works, we'll see what topic device responds on"
echo ""
echo "If device doesn't respond, possible reasons:"
echo "  - Device firmware doesn't handle /user/get commands"
echo "  - Device expects different message format"
echo "  - Device publishes to different topic than /user/upload"
echo "  - Device only responds via HTTP, not MQTT"
echo ""
