def _handle_status(self, data):
    """
    Handle device status change (online/offline/maintenance)
    """
    try:
        service = StationSyncService()
        
        with transaction.atomic():
            result = service.update_station_status(data)
        
        logger.info(f"Status update processed: {result}")
        return result
        
    except ServiceException:
        # Re-raise service exceptions to be handled by parent
        raise
    except Exception as e:
        logger.error(f"Error processing status update: {str(e)}", exc_info=True)
        raise ServiceException(
            detail=f'Status update failed: {str(e)}',
            code="status_update_error"
        )    

@transaction.atomic
def update_station_status(self, data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Update station status (online/offline/maintenance)
    
    Args:
        data: Status update payload from IoT system
        
    Returns:
        Summary of status update
        
    Raises:
        ServiceException: If validation or update fails
    """
    try:
        # Validate input data
        self._validate_status_data(data)
        
        device_data = data.get('device', {})
        serial_number = device_data.get('serial_number')
        imei = device_data.get('imei')
        new_status = device_data.get('status')
        
        # Find station by serial_number (could be IMEI or actual serial)
        identifier = imei or serial_number
        if not identifier:
            raise ServiceException(
                detail="Missing device identifier (imei or serial_number)",
                code="missing_device_identifier"
            )
        
        # Check if identifier matches IMEI or serial_number field
        station = Station.objects.filter(
            Q(imei=identifier) | Q(serial_number=identifier)
        ).first()
        
        if not station:
            raise ServiceException(
                detail=f"Station with identifier {identifier} not found",
                code="station_not_found"
            )
        
        # Validate status value
        if new_status not in self.STATION_STATUS_MAP:
            raise ServiceException(
                detail=f"Invalid status '{new_status}'. Must be one of: {', '.join(self.STATION_STATUS_MAP.keys())}",
                code="invalid_status"
            )
        
        # Update station status and heartbeat
        station.status = self.STATION_STATUS_MAP.get(new_status, 'OFFLINE')
        station.last_heartbeat = timezone.now()
        
        # Update hardware info if provided
        if device_data.get('hardware_info'):
            station.hardware_info.update(device_data['hardware_info'])
        
        station.save(update_fields=['status', 'last_heartbeat', 'hardware_info'])
        
        result = {
            'station_id': str(station.id),
            'serial_number': station.serial_number,
            'status': station.status,
            'last_heartbeat': station.last_heartbeat.isoformat(),
            'updated_at': timezone.now().isoformat()
        }
        
        identifier = imei or serial_number
        self.log_info(f"Station {identifier} status updated to {station.status}")
        return result
    
    except ServiceException:
        # Re-raise service exceptions
        raise
    except Exception as e:
        identifier = data.get('device', {}).get('imei') or data.get('device', {}).get('serial_number', 'unknown')
        self.handle_service_error(e, f"Failed to update station status for {identifier}")

def _validate_status_data(self, data: Dict[str, Any]) -> None:
    """Validate status update data structure"""
    if not isinstance(data, dict):
        raise ServiceException(
            detail="Data must be a dictionary",
            code="invalid_data_format"
        )
    
    device_data = data.get('device', {})
    # Accept either IMEI or serial_number as device identifier
    if not device_data.get('imei') and not device_data.get('serial_number'):
        raise ServiceException(
            detail="Missing device imei or serial_number",
            code="missing_device_identifier"
        )
    
    if not device_data.get('status'):
        raise ServiceException(
            detail="Missing device status",
            code="missing_status"
        )