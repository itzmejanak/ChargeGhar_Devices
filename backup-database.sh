#!/bin/bash
# PostgreSQL Database Backup Script

set -e

BACKUP_DIR="/opt/iotdemo/postgres-backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="iotdemo_backup_$TIMESTAMP.sql"

echo "🗄️ Creating PostgreSQL backup..."

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Create database backup
docker exec iotdemo-postgres-prod pg_dump -U iotdemo -h localhost iotdemo > "$BACKUP_DIR/$BACKUP_FILE"

# Compress the backup
gzip "$BACKUP_DIR/$BACKUP_FILE"

echo "✅ Backup created: $BACKUP_DIR/$BACKUP_FILE.gz"

# Clean up old backups (keep last 7 days)
find $BACKUP_DIR -name "iotdemo_backup_*.sql.gz" -mtime +7 -delete

echo "🧹 Old backups cleaned up"
echo "📊 Current backups:"
ls -lh $BACKUP_DIR/iotdemo_backup_*.sql.gz 2>/dev/null || echo "No backups found"