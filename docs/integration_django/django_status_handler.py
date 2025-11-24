# Add this to your existing StationDataInternalView in Django

def post(self, request):
    """
    Process incoming station data from IoT system
    """
    try:
        data = request.data
        data_type = data.get('type')
        
        logger.info(f"Received IoT data sync request: type={data_type}")
        
        if data_type == 'full':
            return self._handle_full_sync(data)
        elif data_type == 'returned':
            return self._handle_return_event(data)
        elif data_type == 'status':  # <-- ADD THIS
            return self._handle_status(data)  # <-- ADD THIS
        else:
            return Response({
                'success': False,
                'message': f'Invalid data type: {data_type}',
                'error': 'INVALID_TYPE'
            }, status=status.HTTP_400_BAD_REQUEST)
    
    except Exception as e:
        logger.error(f"Error processing IoT data: {str(e)}", exc_info=True)
        return Response({
            'success': False,
            'message': f'Internal server error: {str(e)}',
            'error': 'INTERNAL_ERROR'
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# Add this new method to handle status updates
def _handle_status(self, data):
    """
    Handle device status change (online/offline)
    """
    try:
        device_data = data.get('device', {})
        serial_number = device_data.get('serial_number')
        new_status = device_data.get('status')
        
        if not serial_number or not new_status:
            return Response({
                'success': False,
                'message': 'Missing serial_number or status',
                'error': 'MISSING_DATA'
            }, status=status.HTTP_400_BAD_REQUEST)
        
        # Update station status in database
        try:
            station = Station.objects.get(serial_number=serial_number)
            station.status = new_status  # 'ONLINE' or 'OFFLINE'
            station.last_heartbeat = timezone.now()
            station.save()
            
            logger.info(f"Station {serial_number} status updated to {new_status}")
            
            return Response({
                'success': True,
                'message': f'Station status updated to {new_status}',
                'data': {
                    'station_id': str(station.id),
                    'serial_number': station.serial_number,
                    'status': station.status
                }
            }, status=status.HTTP_200_OK)
            
        except Station.DoesNotExist:
            return Response({
                'success': False,
                'message': f'Station {serial_number} not found',
                'error': 'STATION_NOT_FOUND'
            }, status=status.HTTP_404_NOT_FOUND)
    
    except Exception as e:
        logger.error(f"Error processing status update: {str(e)}", exc_info=True)
        return Response({
            'success': False,
            'message': f'Status update failed: {str(e)}',
            'error': 'STATUS_UPDATE_ERROR'
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)