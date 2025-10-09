#!/bin/bash
# PostgreSQL Database Restore Script

set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <backup_file.sql.gz>"
    echo "Available backups:"
    ls -1 /opt/iotdemo/postgres-backups/iotdemo_backup_*.sql.gz 2>/dev/null || echo "No backups found"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "❌ Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "🔄 Restoring PostgreSQL database from: $BACKUP_FILE"
echo "⚠️ This will overwrite the current database!"
read -p "Are you sure? (y/N): " -n 1 -r
echo

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Restore cancelled"
    exit 1
fi

# Stop the application to prevent connections
echo "🛑 Stopping application..."
docker-compose -f /opt/iotdemo/docker-compose.prod.yml stop app

# Restore database
echo "📥 Restoring database..."
if [[ $BACKUP_FILE == *.gz ]]; then
    gunzip -c "$BACKUP_FILE" | docker exec -i iotdemo-postgres-prod psql -U iotdemo -d iotdemo
else
    cat "$BACKUP_FILE" | docker exec -i iotdemo-postgres-prod psql -U iotdemo -d iotdemo
fi

# Start the application
echo "🚀 Starting application..."
docker-compose -f /opt/iotdemo/docker-compose.prod.yml start app

echo "✅ Database restore completed!"