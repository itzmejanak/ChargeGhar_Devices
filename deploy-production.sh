#!/bin/bash
# Complete production deployment script

set -e

echo "🚀 IoT Demo - Production Deployment"
echo "=================================="

# Configuration
DEPLOY_DIR="/opt/iotdemo"
REPO_URL="https://github.com/itzmejanak/ChargeGhar_Devices.git"
BACKUP_DIR="/opt/iotdemo-backup"

# Create backup of existing deployment
if [ -d "$DEPLOY_DIR" ]; then
    echo "📦 Creating backup..."
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    cp -r $DEPLOY_DIR $BACKUP_DIR-$TIMESTAMP
fi

# Clone or update repository
if [ ! -d "$DEPLOY_DIR/.git" ]; then
    echo "📥 Cloning repository..."
    rm -rf $DEPLOY_DIR
    git clone $REPO_URL $DEPLOY_DIR
else
    echo "🔄 Updating repository..."
    cd $DEPLOY_DIR
    git pull origin main
fi

cd $DEPLOY_DIR

# Create production environment file
echo "⚙️ Setting up production environment..."
cat > .env.prod << EOF
# Production Environment Variables
REDIS_PASSWORD=iotdemo123_prod
SPRING_PROFILES_ACTIVE=production
COMPOSE_PROJECT_NAME=iotdemo-prod
EOF

# Create database backup before deployment
if docker ps -q -f name=iotdemo-mysql-prod | grep -q .; then
    echo "💾 Creating database backup..."
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    mkdir -p /opt/iotdemo-backups
    docker exec iotdemo-mysql-prod mysqldump -u root -pzaan5060 chargeghar_iot > /opt/iotdemo-backups/db-backup-$TIMESTAMP.sql
    echo "✅ Database backup saved to /opt/iotdemo-backups/db-backup-$TIMESTAMP.sql"
fi

# Stop existing containers (but keep volumes)
echo "🛑 Stopping existing deployment..."
docker-compose -f docker-compose.prod.yml down || true

# Clean up old images (optional, but preserve volumes)
echo "🧹 Cleaning up old Docker images..."
docker image prune -f

# Build and start services (preserve existing volumes)
echo "🏗️ Building and starting services..."
docker-compose -f docker-compose.prod.yml up -d --build

# Wait for services to be healthy
echo "⏳ Waiting for services to start..."
sleep 30

# Health check
echo "🏥 Checking service health..."
if curl -f http://localhost:8080/health; then
    echo "✅ Deployment successful!"
    echo "🌐 Application is running at http://$(hostname -I | awk '{print $1}'):8080"
    echo "📊 Check logs with: docker-compose -f $DEPLOY_DIR/docker-compose.prod.yml logs -f"
else
    echo "❌ Health check failed!"
    echo "📋 Check logs with: docker-compose -f $DEPLOY_DIR/docker-compose.prod.yml logs"
    exit 1
fi

# Display running containers
echo "📋 Running containers:"
docker-compose -f docker-compose.prod.yml ps

echo "🎉 Deployment completed successfully!"