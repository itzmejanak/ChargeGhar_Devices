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

# Create production directory
DEPLOY_DIR="/opt/iotdemo"
mkdir -p $DEPLOY_DIR

echo "📁 Deployment directory: $DEPLOY_DIR"

# Stop existing deployment if running
if [ -f "$DEPLOY_DIR/docker-compose.prod.yml" ]; then
    echo "🛑 Stopping existing deployment..."
    cd $DEPLOY_DIR
    docker-compose -f docker-compose.prod.yml down
fi

echo "✅ Server preparation completed!"
echo "📋 Next steps:"
echo "1. Upload your project files to $DEPLOY_DIR"
echo "2. Run: cd $DEPLOY_DIR && docker-compose -f docker-compose.prod.yml up -d --build"