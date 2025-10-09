#!/bin/bash
# Production deployment script for hostinger server

set -e

echo "🚀 Starting IoT Demo Production Deployment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "📦 Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    
    # Add current user to docker group (if not root)
    if [ "$USER" != "root" ]; then
        sudo usermod -aG docker $USER
        echo "⚠️ Please logout and login again to use Docker without sudo"
    fi
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "📦 Installing Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

# Install PostgreSQL client tools for backups
echo "📦 Installing PostgreSQL client tools..."
if command -v apt-get &> /dev/null; then
    apt-get update
    apt-get install -y postgresql-client-13 curl
elif command -v yum &> /dev/null; then
    yum install -y postgresql13 curl
fi

# Create production directories
DEPLOY_DIR="/opt/iotdemo"
mkdir -p $DEPLOY_DIR
mkdir -p $DEPLOY_DIR/postgres-backups
mkdir -p /var/log/iotdemo

echo "📁 Deployment directory: $DEPLOY_DIR"
echo "💾 Backup directory: $DEPLOY_DIR/postgres-backups"

# Stop existing deployment if running
if [ -f "$DEPLOY_DIR/docker-compose.prod.yml" ]; then
    echo "🛑 Stopping existing deployment..."
    cd $DEPLOY_DIR
    docker-compose -f docker-compose.prod.yml down
fi

# Set up log rotation for PostgreSQL backups
cat > /etc/logrotate.d/iotdemo << EOF
/var/log/iotdemo/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
}
EOF

echo "✅ Server preparation completed!"
echo "📋 Next steps:"
echo "1. Upload your project files to $DEPLOY_DIR"
echo "2. Set DB_PASSWORD environment variable: export DB_PASSWORD=your_secure_password"
echo "3. Run: cd $DEPLOY_DIR && ./deploy-production.sh"
echo ""
echo "🔧 Useful commands:"
echo "   - View logs: docker-compose -f docker-compose.prod.yml logs -f"
echo "   - Backup database: docker exec iotdemo-postgres-prod pg_dump -U iotdemo iotdemo > backup.sql"
echo "   - Monitor containers: docker-compose -f docker-compose.prod.yml ps"