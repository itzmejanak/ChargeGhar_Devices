# Project Run & Deployment Guide

## Prerequisites
- Java 8 or higher (JDK)
- Maven
- Redis server (running, with password set to `5060`)
- EMQX Cloud MQTT broker (configured in `config.properties`)

---

## 1. Running Locally

### 1.1. Clone the Project
```
git clone <your-repo-url>
cd <project-folder>
```

### 1.2. Configure Properties
- Edit `src/main/resources/config.properties`:
  - Set `mqtt.broker`, `mqtt.username`, `mqtt.password`, etc.
  - Set `redis.host=127.0.0.1`, `redis.port=6379`, `redis.pass=5060`

### 1.3. Start Redis (if not already running)
```
redis-server.exe
```

### 1.4. Build the Project
```
mvn clean package
```

### 1.5. Run the Application
```
mvn spring-boot:run
```
- Or run the generated JAR from `target/`:
```
java -jar target/<your-app>.jar
```

### 1.6. Access the Application
- Open your browser and go to: `http://localhost:8080/`

---

## 2. Deploying on Hostinger VPS

### 2.1. Prepare VPS
- Install Java (JDK), Maven, and Redis on your VPS.
- Open required ports (e.g., 8080 for web, 6379 for Redis, 8883 for MQTT if needed).

### 2.2. Upload Project
- Use `scp` or SFTP to upload your project folder to the VPS.

### 2.3. Configure Properties
- Edit `config.properties`:
  - Set `redis.host=127.0.0.1` (if Redis is local on VPS)
  - Set correct `mqtt.broker`, `mqtt.username`, `mqtt.password`

### 2.4. Start Redis on VPS
```
redis-server
```

### 2.5. Build and Run
```
cd <project-folder>
mvn clean package
java -jar target/<your-app>.jar
```

### 2.6. Access Remotely
- Open your browser to `http://<your-vps-ip>:8080/`
- Ensure firewall allows incoming traffic on port 8080.

---

## 3. Troubleshooting
- Check logs for errors: `logs/` or console output.
- Ensure Redis and EMQX credentials are correct.
- Make sure required ports are open and not blocked by firewall.

---

## 4. Security Tips
- Change default Redis password for production.
- Use strong passwords for MQTT and Redis.
- Restrict access to Redis and application ports.
- Regularly update your server and dependencies.

---

## 5. Useful Commands
- Restart app: `pkill -f <your-app>.jar && java -jar target/<your-app>.jar &`
- Check Redis status: `redis-cli -a 5060 ping`
- Check Java version: `java -version`

---

For further help, contact your developer or system administrator.
