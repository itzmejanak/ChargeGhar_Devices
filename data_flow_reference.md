# ChargeGhar Devices: Data Flow & Integration Reference

## System Overview
- **Devices** send data via MQTT to **EMQX Cloud**.
- **Java Server** (Spring) subscribes to EMQX topics, processes device data, and stores relevant state in **Redis**.
- **Django Server** (or any other service) can:
  - Subscribe to EMQX topics for real-time device data.
  - Read from Redis for latest device state/history.

## Data Flow
1. **Device → EMQX Cloud (MQTT Broker)**
   - Devices publish messages to topics (e.g., `device/{deviceName}/upload`).
2. **Java Server (MqttSubscriber)**
   - Subscribes to relevant topics.
   - Processes incoming messages.
   - Stores device status, heartbeats, and responses in Redis.
3. **Redis**
   - Acts as a real-time cache for device state and command responses.
4. **Django Server**
   - Can subscribe to EMQX for real-time data (using paho-mqtt or similar).
   - Can read device state/history from Redis.

## Key Points
- **EMQX** is a message broker, not a database. It routes messages but does not persist them.
- **Redis** is used for stateful data and quick lookups.
- Both Java and Django can access the same Redis and EMQX instance for seamless integration.
- Device status and command responses are stored in Redis with keys like `device_heartbeat:{deviceName}` or `check:{deviceName}`.

## Example Use Cases
- Real-time dashboards: Subscribe to EMQX topics.
- Device status/history: Query Redis.
- Multi-language support: Any service (Java, Python, etc.) can use MQTT and Redis.

## References
- Java MQTT: `com.demo.mqtt.MqttSubscriber`, `MqttPublisher`
- Redis usage: `RedisTemplate` in Java code
- Django MQTT: Use `paho-mqtt` library
- Django Redis: Use `django-redis` or `redis-py`

---

**Tip:** For new integrations, always check both EMQX topic subscriptions and Redis key structure for the data you need.
