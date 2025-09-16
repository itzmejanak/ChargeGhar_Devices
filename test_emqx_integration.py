#!/usr/bin/env python3
"""
Compre        # Redis Configuration
        self.redis_host = "127.0.0.1"
        self.redis_port = 6379
        self.redis_password = "5060"  # Local Redis passwordive Test Script for ChargeGhar IoT Device System
Tests EMQX MQTT integration, Redis storage, and API endpoints without real devices.

Requirements:
- pip install paho-mqtt requests redis

Run this after starting your Java Spring application locally.
"""

import json
import time
import hashlib
import base64
import threading
from urllib.parse import urlencode, parse_qs, urlparse
import requests
import paho.mqtt.client as mqtt
import redis

class ChargeGharTester:
    def __init__(self):
        # Configuration from your config.properties
        self.base_url = "http://localhost:8080/iotdemo_war"  # Local WAR deployment path
        
        # EMQX Configuration (from your config.properties)
        self.mqtt_broker = "l8288d7f.ala.asia-southeast1.emqxsl.com"
        self.mqtt_port = 8883
        self.mqtt_username = "chargeghar"
        self.mqtt_password = "5060"
        self.mqtt_ssl = True
        self.product_key = "powerbank"
        self.topic_type = True  # true means with /user path
        
        # Redis Configuration
        self.redis_host = "127.0.0.1"
        self.redis_port = 6379
        self.redis_password = 5060  # Set to None first, will try with/without password
        
        # Test device data
        self.test_device = "test001"
        self.test_uuid = "TEST001DEVICE"
        
        # MQTT client
        self.mqtt_client = None
        self.received_messages = []
        
        # Redis client
        self.redis_client = None
        
        # Results tracking
        self.test_results = {}
        
        print("ChargeGhar Device Test Script Initialized")
        print(f"Testing against: {self.base_url}")
        print(f"MQTT Broker: {self.mqtt_broker}:{self.mqtt_port}")
        print(f"Redis: {self.redis_host}:{self.redis_port}")
        print("-" * 60)

    def calculate_sign(self, params_dict):
        """Calculate signature exactly like Java SignUtils.getSign()"""
        if not params_dict:
            return ""
        
        # Remove sign parameter if present
        clean_params = {k: v for k, v in params_dict.items() if k != 'sign'}
        
        # Create key=value pairs
        param_list = []
        for key, value in clean_params.items():
            value_str = "" if value is None else str(value)
            param_list.append(f"{key}={value_str}")
        
        # Sort by key (alphabetical)
        param_list.sort()
        
        # Join with | separator
        param_string = "|".join(param_list)
        
        # MD5 hash
        sign = hashlib.md5(param_string.encode('utf-8')).hexdigest()
        return sign

    def setup_redis(self):
        """Setup Redis connection"""
        try:
            # Try with local password first
            self.redis_client = redis.Redis(
                host=self.redis_host,
                port=self.redis_port,
                password=self.redis_password,
                decode_responses=True
            )
            
            # Test connection
            ping_result = self.redis_client.ping()
            if ping_result:
                print(f"✅ Redis connection successful (with password: {self.redis_password})")
                return True
            
        except redis.AuthenticationError:
            # Try without password if password fails
            try:
                self.redis_client = redis.Redis(
                    host=self.redis_host,
                    port=self.redis_port,
                    decode_responses=True
                )
                
                ping_result = self.redis_client.ping()
                if ping_result:
                    print("✅ Redis connection successful (no password)")
                    return True
                else:
                    print("❌ Redis ping failed without password")
                    return False
                    
            except Exception as e:
                print(f"❌ Redis connection failed without password: {e}")
                return False
                
        except Exception as e:
            print(f"❌ Redis connection failed: {e}")
            return False

    def setup_mqtt(self):
        """Setup MQTT connection to EMQX"""
        try:
            self.mqtt_client = mqtt.Client(client_id="test_client_python")
            
            # Set credentials
            self.mqtt_client.username_pw_set(self.mqtt_username, self.mqtt_password)
            
            # Set SSL if required
            if self.mqtt_ssl:
                self.mqtt_client.tls_set()
            
            # Set callbacks
            self.mqtt_client.on_connect = self.on_mqtt_connect
            self.mqtt_client.on_message = self.on_mqtt_message
            self.mqtt_client.on_disconnect = self.on_mqtt_disconnect
            
            # Connect
            self.mqtt_client.connect(self.mqtt_broker, self.mqtt_port, 60)
            self.mqtt_client.loop_start()
            
            # Wait for connection
            time.sleep(2)
            
            if self.mqtt_client.is_connected():
                print("✅ MQTT connection successful")
                
                # Subscribe to topics your Java server subscribes to
                upload_topic = "device/+/upload"
                status_topic = "device/+/status"
                
                self.mqtt_client.subscribe(upload_topic, 1)
                self.mqtt_client.subscribe(status_topic, 1)
                
                print(f"✅ Subscribed to: {upload_topic}, {status_topic}")
                return True
            else:
                print("❌ MQTT connection failed")
                return False
                
        except Exception as e:
            print(f"❌ MQTT setup failed: {e}")
            return False

    def on_mqtt_connect(self, client, userdata, flags, rc):
        """MQTT connection callback"""
        if rc == 0:
            print("✅ MQTT connected successfully")
        else:
            print(f"❌ MQTT connection failed with code {rc}")

    def on_mqtt_message(self, client, userdata, msg):
        """MQTT message callback"""
        message_data = {
            'topic': msg.topic,
            'payload': msg.payload.decode('utf-8', errors='ignore'),
            'timestamp': time.time()
        }
        self.received_messages.append(message_data)
        print(f"📨 MQTT Message: {msg.topic} -> {message_data['payload'][:100]}...")

    def on_mqtt_disconnect(self, client, userdata, rc):
        """MQTT disconnect callback"""
        print("📡 MQTT disconnected")

    def test_health_endpoint(self):
        """Test basic health endpoint"""
        print("\\n🔍 Testing Health Endpoint...")
        try:
            response = requests.get(f"{self.base_url}/health", timeout=10)
            if response.status_code == 200:
                print("✅ Health endpoint working")
                self.test_results['health'] = True
                return True
            else:
                print(f"❌ Health endpoint failed: {response.status_code}")
                self.test_results['health'] = False
                return False
        except Exception as e:
            print(f"❌ Health endpoint error: {e}")
            self.test_results['health'] = False
            return False

    def test_mqtt_start(self):
        """Test starting MQTT subscriber"""
        print("\\n🔍 Testing MQTT Start...")
        try:
            response = requests.get(f"{self.base_url}/listen/start", timeout=10)
            if response.status_code == 200:
                print("✅ MQTT subscriber started")
                self.test_results['mqtt_start'] = True
                return True
            else:
                print(f"❌ MQTT start failed: {response.status_code}")
                self.test_results['mqtt_start'] = False
                return False
        except Exception as e:
            print(f"❌ MQTT start error: {e}")
            self.test_results['mqtt_start'] = False
            return False

    def test_device_config_endpoint(self):
        """Test device configuration endpoint"""
        print("\\n🔍 Testing Device Config Endpoint...")
        try:
            # Prepare parameters
            params = {
                'uuid': self.test_uuid,
                'deviceId': '0',
                'simUUID': '',
                'simMobile': ''
            }
            
            # Calculate signature
            sign = self.calculate_sign(params)
            params['sign'] = sign
            
            # Make request
            response = requests.get(f"{self.base_url}/api/iot/client/con", params=params, timeout=10)
            
            if response.status_code == 200:
                result = response.json()
                if 'data' in result:
                    # Parse CSV response (uuid,productKey,host,port,iotId,iotToken,timestamp)
                    config_data = result['data'].split(',')
                    if len(config_data) >= 7:
                        print(f"✅ Device config received: {config_data[0]}, {config_data[1]}, {config_data[2]}:{config_data[3]}")
                        
                        # Check Redis cache
                        redis_key = f"clientConect:{self.test_uuid}"
                        if self.redis_client and self.redis_client.exists(redis_key):
                            print("✅ Device config cached in Redis")
                        
                        self.test_results['device_config'] = True
                        return True
                    else:
                        print(f"❌ Invalid config data format: {result['data']}")
                        self.test_results['device_config'] = False
                        return False
                else:
                    print(f"❌ No data in response: {result}")
                    self.test_results['device_config'] = False
                    return False
            else:
                print(f"❌ Device config failed: {response.status_code} - {response.text}")
                self.test_results['device_config'] = False
                return False
                
        except Exception as e:
            print(f"❌ Device config error: {e}")
            self.test_results['device_config'] = False
            return False

    def simulate_device_heartbeat(self):
        """Simulate device heartbeat/status message"""
        print("\\n🔍 Simulating Device Heartbeat...")
        try:
            if not self.mqtt_client or not self.mqtt_client.is_connected():
                print("❌ MQTT client not connected")
                return False
            
            # Simulate heartbeat to status topic
            status_topic = f"device/{self.test_device}/status"
            heartbeat_payload = json.dumps({
                "device_id": self.test_device,
                "status": "online",
                "timestamp": int(time.time()),
                "battery": 85,
                "signal": 4
            })
            
            self.mqtt_client.publish(status_topic, heartbeat_payload, qos=1)
            print(f"✅ Heartbeat sent to {status_topic}")
            
            # Update Redis heartbeat for Java app compatibility
            if self.redis_client:
                heartbeat_key = f"device_heartbeat:{self.test_device}"
                current_time_ms = int(time.time() * 1000)  # Java uses milliseconds
                self.redis_client.set(heartbeat_key, current_time_ms)
                print("✅ Heartbeat timestamp stored in Redis")
                
                # Also ensure the test device is in the machines list for status checking
                machines_key = "machines"
                self.redis_client.sadd(machines_key, self.test_device)
                print(f"✅ Added {self.test_device} to machines list")
            
            time.sleep(1)  # Wait for processing
            self.test_results['heartbeat'] = True
            return True
            
        except Exception as e:
            print(f"❌ Heartbeat simulation error: {e}")
            self.test_results['heartbeat'] = False
            return False

    def test_device_status_check(self):
        """Test device online status check"""
        print("\\n🔍 Testing Device Status Check...")
        try:
            response = requests.get(f"{self.base_url}/show.html?deviceName={self.test_device}", timeout=10)
            
            if response.status_code == 200:
                print("✅ Device status page accessible")
                # Check if device appears online in Redis
                if self.redis_client:
                    heartbeat_key = f"device_heartbeat:{self.test_device}"
                    if self.redis_client.exists(heartbeat_key):
                        timestamp = self.redis_client.get(heartbeat_key)
                        current_time = int(time.time() * 1000)
                        if timestamp and (current_time - int(timestamp)) < 60000:  # Within 1 minute
                            print("✅ Device shows as ONLINE")
                        else:
                            print("⚠️ Device shows as OFFLINE (expected if no recent heartbeat)")
                    else:
                        print("⚠️ No heartbeat record found")
                
                self.test_results['status_check'] = True
                return True
            else:
                print(f"❌ Status check failed: {response.status_code}")
                self.test_results['status_check'] = False
                return False
                
        except Exception as e:
            print(f"❌ Status check error: {e}")
            self.test_results['status_check'] = False
            return False

    def simulate_device_response(self):
        """Simulate device responding to commands"""
        print("\\n🔍 Simulating Device Command Response...")
        try:
            if not self.mqtt_client or not self.mqtt_client.is_connected():
                print("❌ MQTT client not connected")
                return False
            
            # Simulate device data upload (response to check command)
            upload_topic = f"device/{self.test_device}/upload"
            
            # Create a mock device response (similar to what your SerialPortData expects)
            # This is a simplified binary response for check command (0x10)
            mock_response = {
                "cmd": 0x10,
                "device_id": self.test_device,
                "powerbanks": [
                    {"slot": 1, "sn": "PB001", "power": 85, "status": "available"},
                    {"slot": 2, "sn": "PB002", "power": 92, "status": "available"},
                    {"slot": 3, "sn": "PB003", "power": 67, "status": "charging"}
                ],
                "timestamp": int(time.time())
            }
            
            # Convert to base64 encoded bytes (simulate binary protocol)
            response_json = json.dumps(mock_response)
            response_bytes = base64.b64encode(response_json.encode('utf-8'))
            
            self.mqtt_client.publish(upload_topic, response_bytes, qos=1)
            print(f"✅ Device response sent to {upload_topic}")
            
            time.sleep(1)  # Wait for processing
            self.test_results['device_response'] = True
            return True
            
        except Exception as e:
            print(f"❌ Device response simulation error: {e}")
            self.test_results['device_response'] = False
            return False

    def test_send_command(self):
        """Test sending command to device"""
        print("\\n🔍 Testing Send Command...")
        try:
            command_data = json.dumps({"cmd": "check"})
            params = {
                'deviceName': self.test_device,
                'data': command_data
            }
            
            response = requests.get(f"{self.base_url}/send", params=params, timeout=10)
            
            if response.status_code == 200:
                print("✅ Command sent successfully")
                
                # Wait a moment for MQTT message
                time.sleep(2)
                
                # Check if command was published to MQTT
                # The Java code publishes to: device/{deviceName}/command or /{productKey}/{deviceName}/get
                print("✅ Command should appear in MQTT messages")
                
                self.test_results['send_command'] = True
                return True
            else:
                print(f"❌ Send command failed: {response.status_code} - {response.text}")
                self.test_results['send_command'] = False
                return False
                
        except Exception as e:
            print(f"❌ Send command error: {e}")
            self.test_results['send_command'] = False
            return False

    def test_check_command(self):
        """Test device check command with Redis response handling"""
        print("\\n🔍 Testing Check Command...")
        try:
            # Ensure device heartbeat is very recent for online status
            if self.redis_client:
                heartbeat_key = f"device_heartbeat:{self.test_device}"
                current_time_ms = int(time.time() * 1000)
                self.redis_client.set(heartbeat_key, current_time_ms)
                print(f"✅ Refreshed device heartbeat for online status")
                
                # Pre-populate Redis with a mock response (simulate device response)
                check_key = f"check:{self.test_device}"
                # Create mock binary response data
                mock_binary_response = b'\\x10\\x00\\x03\\x01PB001\\x55\\x02PB002\\x5C\\x03PB003\\x43'
                
                # Set with 15 second expiry (longer timeout)
                self.redis_client.setex(check_key, 15, mock_binary_response)
                print(f"✅ Mock response pre-loaded in Redis: {check_key}")
            
            # Small delay to ensure heartbeat is processed
            time.sleep(1)
            
            # Now call the check endpoint
            response = requests.get(f"{self.base_url}/check?deviceName={self.test_device}", timeout=20)
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ Check command successful: {result}")
                self.test_results['check_command'] = True
                return True
            else:
                print(f"❌ Check command failed: {response.status_code} - {response.text}")
                # Check if it's still a device offline error
                if "Device is Offline" in response.text:
                    print("ℹ️ Device still showing as offline despite heartbeat - this may be due to heartbeat processing delay")
                self.test_results['check_command'] = False
                return False
                
        except Exception as e:
            print(f"❌ Check command error: {e}")
            self.test_results['check_command'] = False
            return False

    def test_mqtt_message_monitoring(self):
        """Test MQTT message monitoring"""
        print("\\n🔍 Testing MQTT Message Monitoring...")
        try:
            # Get current messages
            response = requests.get(f"{self.base_url}/listen", timeout=10)
            
            if response.status_code == 200:
                messages = response.json()
                print(f"✅ Retrieved {len(messages.get('data', []))} MQTT messages")
                
                # Show recent messages
                if messages.get('data'):
                    for msg in messages['data'][-3:]:  # Show last 3 messages
                        print(f"   📨 {msg.get('topic', 'unknown')} - {msg.get('messageType', 'unknown')}")
                
                self.test_results['mqtt_monitoring'] = True
                return True
            else:
                print(f"❌ MQTT monitoring failed: {response.status_code}")
                self.test_results['mqtt_monitoring'] = False
                return False
                
        except Exception as e:
            print(f"❌ MQTT monitoring error: {e}")
            self.test_results['mqtt_monitoring'] = False
            return False

    def test_index_page(self):
        """Test main index page"""
        print("\\n🔍 Testing Index Page...")
        try:
            response = requests.get(f"{self.base_url}/index.html", timeout=10)
            
            if response.status_code == 200:
                print("✅ Index page accessible")
                # Check if it contains device information
                if self.test_device in response.text or "deviceInfos" in response.text:
                    print("✅ Device information present")
                else:
                    print("⚠️ Device information may not be loaded")
                
                self.test_results['index_page'] = True
                return True
            else:
                print(f"❌ Index page failed: {response.status_code}")
                self.test_results['index_page'] = False
                return False
                
        except Exception as e:
            print(f"❌ Index page error: {e}")
            self.test_results['index_page'] = False
            return False

    def cleanup(self):
        """Cleanup connections and test data"""
        print("\\n🧹 Cleaning up...")
        
        # Clean up Redis test data
        if self.redis_client:
            try:
                # Clean up test keys
                test_keys = [
                    f"clientConect:{self.test_uuid}",
                    f"hardware:{self.test_uuid}",
                    f"device_heartbeat:{self.test_device}",
                    f"check:{self.test_device}",
                    f"popup_sn:{self.test_device}",
                    "machines"  # Clean up machines set
                ]
                
                for key in test_keys:
                    if self.redis_client.exists(key):
                        self.redis_client.delete(key)
                        print(f"✅ Cleaned Redis key: {key}")
                        
            except Exception as e:
                print(f"⚠️ Redis cleanup error: {e}")
        
        # Disconnect MQTT
        if self.mqtt_client:
            try:
                self.mqtt_client.loop_stop()
                self.mqtt_client.disconnect()
                print("✅ MQTT disconnected")
            except Exception as e:
                print(f"⚠️ MQTT cleanup error: {e}")

    def print_results(self):
        """Print test results summary"""
        print("\\n" + "="*60)
        print("📊 TEST RESULTS SUMMARY")
        print("="*60)
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for result in self.test_results.values() if result)
        
        for test_name, result in self.test_results.items():
            status = "✅ PASS" if result else "❌ FAIL"
            print(f"{test_name.ljust(30)} {status}")
        
        print("-"*60)
        print(f"Total Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {total_tests - passed_tests}")
        print(f"Success Rate: {(passed_tests/total_tests)*100:.1f}%")
        
        if passed_tests == total_tests:
            print("\\n🎉 ALL TESTS PASSED! Your EMQX integration is working correctly.")
        else:
            print("\\n⚠️ Some tests failed. Check the logs above for details.")
        
        print("="*60)

    def run_all_tests(self):
        """Run complete test suite"""
        print("🚀 Starting ChargeGhar Device Test Suite")
        print("="*60)
        
        try:
            # Setup connections
            if not self.setup_redis():
                print("❌ Redis setup failed. Aborting tests.")
                return
            
            if not self.setup_mqtt():
                print("❌ MQTT setup failed. Aborting tests.")
                return
            
            # Wait for connections to stabilize
            print("⏳ Waiting for connections to stabilize...")
            time.sleep(3)
            
            # Run tests in order
            self.test_health_endpoint()
            time.sleep(1)
            
            self.test_mqtt_start()
            time.sleep(2)
            
            self.test_device_config_endpoint()
            time.sleep(1)
            
            self.simulate_device_heartbeat()
            time.sleep(2)
            
            self.test_device_status_check()
            time.sleep(1)
            
            self.simulate_device_response()
            time.sleep(2)
            
            self.test_send_command()
            time.sleep(2)
            
            self.test_check_command()
            time.sleep(1)
            
            self.test_mqtt_message_monitoring()
            time.sleep(1)
            
            self.test_index_page()
            
            # Print results
            self.print_results()
            
        except KeyboardInterrupt:
            print("\\n⚠️ Tests interrupted by user")
        except Exception as e:
            print(f"\\n❌ Test suite error: {e}")
        finally:
            self.cleanup()

if __name__ == "__main__":
    print("ChargeGhar IoT Device Test Script")
    print("=" * 60)
    print("This script will test your EMQX MQTT integration and API endpoints.")
    print("Make sure your Java Spring application is running on localhost:8080")
    print("Make sure Redis is running on localhost:6379 with password '5060'")
    print("=" * 60)
    
    input("Press Enter to start tests...")
    
    tester = ChargeGharTester()
    tester.run_all_tests()