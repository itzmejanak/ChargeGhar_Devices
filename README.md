# IoT Demo Application

A Spring MVC web application for IoT device management with EMQX Cloud integration.

## Features

- **Real-time MQTT messaging** with EMQX Cloud
- **Device management** and monitoring
- **Redis caching** for performance
- **RESTful APIs** for device interaction
- **Web dashboard** for monitoring
- **Docker containerization** for easy deployment

## Technology Stack

- **Backend**: Java 8, Spring MVC, Maven
- **Frontend**: HTML, JavaScript, Layui
- **Database**: Redis
- **MQTT**: EMQX Cloud
- **Containerization**: Docker, Docker Compose
- **Application Server**: Apache Tomcat 8.5

## Quick Start

### Prerequisites

- Docker Desktop
- Git

### Run Application

```bash
# Clone repository
git clone <repository-url>
cd iotdemo

# Start application
make run

# Or using Docker Compose directly
docker-compose up -d
```

### Access Application

- **Main Application**: http://localhost:8080/home/
- **Dashboard**: http://localhost:8080/home/index.html
- **MQTT Listener**: http://localhost:8080/home/listen.html
- **API Health Check**: http://localhost:8080/home/health

## Development

### Local Development with Maven

```bash
# Build WAR file
make package

# Run tests
make test

# Or using Maven directly
mvn clean package
mvn test
```

### Docker Development

```bash
# Build and run
make run

# View logs
make logs

# Stop application
make stop

# Clean up everything
make clean
```

## Configuration

### EMQX Cloud Settings

Update `src/main/resources/config.properties`:

```properties
# EMQX Cloud Configuration
mqtt.broker=your-broker.emqxsl.com
mqtt.port=8883
mqtt.username=your-username
mqtt.password=your-password
mqtt.ssl=true
```

### Redis Settings

Redis is automatically configured in Docker environment. For local development:

```properties
redis.host=localhost
redis.port=6379
redis.pass=your-password
```

## API Endpoints

### Device Management
- `GET /api/iot/client/con` - Device connection
- `GET /health` - Health check
- `GET /test` - API test endpoint

### Version Management
- `GET /api/iot/app/version/publish` - Get latest version
- `GET /api/iot/app/version/test` - Get test version

### MQTT Operations
- `GET /listen.html` - MQTT message listener
- `POST /send` - Send MQTT message

## Project Structure

```
iotdemo/
├── src/main/
│   ├── java/com.demo/
│   │   ├── controller/     # REST controllers
│   │   ├── mqtt/          # MQTT integration
│   │   ├── common/        # Common utilities
│   │   └── bean/          # Data models
│   ├── resources/         # Configuration files
│   └── webapp/           # Web assets
├── Dockerfile            # Container definition
├── docker-compose.yml    # Multi-service setup
├── Makefile             # Build automation
└── pom.xml              # Maven configuration
```

## Deployment

### Production Deployment

```bash
# Deploy to production
make deploy

# Or manually
docker-compose -f docker-compose.yml up -d
```

### Environment Variables

Set these in production:

```bash
REDIS_HOST=redis-server
REDIS_PORT=6379
REDIS_PASSWORD=secure-password
SPRING_PROFILES_ACTIVE=production
```

## Monitoring

### Health Checks

- Application: http://localhost:8080/iotdemo/health
- Redis: `docker exec iotdemo-redis redis-cli ping`

### Logs

```bash
# Application logs
make logs

# All service logs
docker-compose logs -f

# Redis logs
docker-compose logs redis
```

## Troubleshooting

### Common Issues

1. **Port 8080 in use**
   ```bash
   # Change port in docker-compose.yml
   ports:
     - "8081:8080"
   ```

2. **Redis connection failed**
   ```bash
   # Check Redis status
   docker-compose ps redis
   ```

3. **Build failures**
   ```bash
   # Clean and rebuild
   make clean
   make build
   ```

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes
4. Test locally with `make run`
5. Submit pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository.