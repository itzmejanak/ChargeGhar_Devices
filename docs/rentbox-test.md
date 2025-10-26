#!/bin/bash

echo "========================================="
echo "MANUAL CURL TEST - CORRECTED"
echo "========================================="
echo ""

# Test parameters
RENTBOX_SN="TEST123456"
SIGNAL="100"

# CORRECT hex data (41 bytes - 2-port device upload)
# Fixed: Added missing bytes in powerbank data
HEX_DATA="A800291001FFFF1E0101010101000005123456785028014B0A01010200000000000000000000000014"

echo "1️⃣  Test Parameters:"
echo "   Rentbox SN: $RENTBOX_SN"
echo "   Signal: $SIGNAL"
echo "   Data (hex): $HEX_DATA"
echo "   Data length: ${#HEX_DATA} chars = $((${#HEX_DATA}/2)) bytes"
echo ""

# Calculate signature
SIGN_STRING="rentboxSN=${RENTBOX_SN}|signal=${SIGNAL}"
SIGNATURE=$(echo -n "$SIGN_STRING" | md5sum | awk '{print $1}')

echo "2️⃣  Signature:"
echo "   Sign String: $SIGN_STRING"
echo "   MD5: $SIGNATURE"
echo ""

# Convert hex to binary
echo "$HEX_DATA" | xxd -r -p > /tmp/test_data.bin

echo "3️⃣  Binary Data:"
xxd /tmp/test_data.bin
echo ""

# Build URL
URL="http://localhost:8080/api/rentbox/upload/data?rentboxSN=${RENTBOX_SN}&sign=${SIGNATURE}&signal=${SIGNAL}"

echo "4️⃣  Sending POST Request..."
echo ""

# Send request
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
  -X POST \
  -H "Content-Type: application/octet-stream" \
  --data-binary @/tmp/test_data.bin \
  "$URL")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | grep -v "HTTP_CODE")

echo "========================================="
echo "RESPONSE"
echo "========================================="
echo "HTTP Status: $HTTP_CODE"
echo ""

# Pretty print JSON if possible
if command -v python3 &> /dev/null; then
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
    echo "$BODY"
fi

echo ""

# Check result
if [ "$HTTP_CODE" = "200" ] && echo "$BODY" | grep -q '"code":200'; then
    echo "✅ ✅ ✅ SUCCESS! Manufacturer's updates working perfectly!"
    echo "✅ Endpoint: /api/rentbox/upload/data"
    echo "✅ HTTP Status: 200 OK"
    echo "✅ Parsing: SUCCESS"
else
    echo "⚠️  Check response above for details"
fi

echo ""
echo "========================================="

# Cleanup
rm -f /tmp/test_data.bin
EOF


chmod +x /tmp/correct_curl_test.sh
bash /tmp/correct_curl_test.sh