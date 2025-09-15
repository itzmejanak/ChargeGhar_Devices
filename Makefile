# IoT Demo Application - Build Automation
.PHONY: help build run stop clean logs test package deploy

# Default target
help:
	@echo "IoT Demo Application - Available Commands:"
	@echo ""
	@echo "Development:"
	@echo "  make build     - Build Docker image"
	@echo "  make run       - Start application (build + run)"
	@echo "  make stop      - Stop application"
	@echo "  make logs      - View application logs"
	@echo "  make clean     - Clean up containers and images"
	@echo ""
	@echo "Maven:"
	@echo "  make package   - Build WAR file with Maven"
	@echo "  make test      - Run tests"
	@echo ""
	@echo "Production:"
	@echo "  make deploy    - Deploy to production"
	@echo ""
	@echo "Application will be available at: http://localhost:8080/iotdemo/"

# Build Docker image
build:
	@echo "Building IoT Demo application..."
	docker-compose build --no-cache

# Start application
run:
	@echo "Starting IoT Demo application..."
	docker-compose up -d
	@echo ""
	@echo "✅ Application started successfully!"
	@echo "🌐 Access: http://localhost:8080/iotdemo/"
	@echo "📊 Health: http://localhost:8080/iotdemo/health"
	@echo ""
	@echo "Use 'make logs' to view logs"
	@echo "Use 'make stop' to stop the application"

# Stop application
stop:
	@echo "Stopping IoT Demo application..."
	docker-compose down

# View logs
logs:
	docker-compose logs -f app

# Clean up
clean:
	@echo "Cleaning up containers, images, and volumes..."
	docker-compose down -v --rmi all --remove-orphans
	docker system prune -f

# Build WAR with Maven (local development)
package:
	@echo "Building WAR file with Maven..."
	mvn clean package -DskipTests

# Run tests
test:
	@echo "Running tests..."
	mvn test

# Deploy (production)
deploy: build
	@echo "Deploying to production..."
	docker-compose -f docker-compose.yml up -d
	@echo "Deployment complete!"

# Quick development cycle
dev: stop build run logs