NOw our plan are
1. Make 4 methods with file name  src/main/java/com.demo/connector/ChargeGharConnector.java

which will have 2 methods to send data to our https://main.chargeghar.com django project (used by end users) but to update the databse used on that project we will use this endpoints (
    GET
 /api/internal/stations/data (which accepet data with type = full or returned) in python we can make methods to update camed data as usal
)

here is the djoango app ststion related databse tables
class Station(BaseModel):
    """
    Station - PowerBank Charging Station
    """
    STATION_STATUS_CHOICES = [
        ('ONLINE', 'Online'),
        ('OFFLINE', 'Offline'),
        ('MAINTENANCE', 'Maintenance'),
    ]

    station_name = models.CharField(max_length=100)
    serial_number = models.CharField(max_length=255, unique=True)
    imei = models.CharField(max_length=255, unique=True)
    latitude = models.DecimalField(max_digits=10, decimal_places=6)
    longitude = models.DecimalField(max_digits=10, decimal_places=6)
    address = models.CharField(max_length=255)
    landmark = models.CharField(max_length=255, null=True, blank=True)
    total_slots = models.IntegerField()
    status = models.CharField(max_length=50, choices=STATION_STATUS_CHOICES, default='OFFLINE')
    is_maintenance = models.BooleanField(default=False)
    hardware_info = models.JSONField(default=dict)
    last_heartbeat = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "stations"
        verbose_name = "Station"
        verbose_name_plural = "Stations"

    def __str__(self):
        return str(self.station_name)


class StationSlot(BaseModel):
    """
    StationSlot - Individual slot in a charging station
    """
    SLOT_STATUS_CHOICES = [
        ('AVAILABLE', 'Available'),
        ('OCCUPIED', 'Occupied'),
        ('MAINTENANCE', 'Maintenance'),
        ('ERROR', 'Error'),
    ]

    station = models.ForeignKey(Station, on_delete=models.CASCADE, related_name='slots')
    slot_number = models.IntegerField()
    status = models.CharField(max_length=50, choices=SLOT_STATUS_CHOICES, default='AVAILABLE')
    battery_level = models.IntegerField(default=0)
    slot_metadata = models.JSONField(default=dict)
    last_updated = models.DateTimeField(auto_now=True)
    current_rental = models.ForeignKey('rentals.Rental', on_delete=models.SET_NULL, null=True, blank=True)

    class Meta:
        db_table = "station_slots"
        verbose_name = "Station Slot"
        verbose_name_plural = "Station Slots"
        unique_together = ['station', 'slot_number']

    def __str__(self):
        return f"{self.station.station_name} - Slot {self.slot_number}"

class PowerBank(BaseModel):
    """
    PowerBank - Physical power bank device
    """
    POWERBANK_STATUS_CHOICES = [
        ('AVAILABLE', 'Available'),
        ('RENTED', 'Rented'),
        ('MAINTENANCE', 'Maintenance'),
        ('DAMAGED', 'Damaged'),
    ]

    serial_number = models.CharField(max_length=255, unique=True)
    model = models.CharField(max_length=255)
    capacity_mah = models.IntegerField()
    status = models.CharField(max_length=50, choices=POWERBANK_STATUS_CHOICES, default='AVAILABLE')
    battery_level = models.IntegerField(default=100)
    hardware_info = models.JSONField(default=dict)
    last_updated = models.DateTimeField(auto_now=True)
    
    current_station = models.ForeignKey(Station, on_delete=models.SET_NULL, null=True, blank=True)
    current_slot = models.ForeignKey(StationSlot, on_delete=models.SET_NULL, null=True, blank=True)

    class Meta:
        db_table = "power_banks"
        verbose_name = "Power Bank"
        verbose_name_plural = "Power Banks"

    def __str__(self):
        return f"PowerBank {self.serial_number}"


class Rental(BaseModel):
    """
    Rental - Power bank rental session
    """
    RENTAL_STATUS_CHOICES = [
        ('PENDING', 'Pending'),
        ('ACTIVE', 'Active'),
        ('COMPLETED', 'Completed'),
        ('CANCELLED', 'Cancelled'),
        ('OVERDUE', 'Overdue'),
    ]

    PAYMENT_STATUS_CHOICES = [
        ('PENDING', 'Pending'),
        ('PAID', 'Paid'),
        ('FAILED', 'Failed'),
        ('REFUNDED', 'Refunded'),
    ]

    user = models.ForeignKey('users.User', on_delete=models.CASCADE, related_name='rentals')
    station = models.ForeignKey('stations.Station', on_delete=models.CASCADE, related_name='rentals')
    return_station = models.ForeignKey('stations.Station', on_delete=models.CASCADE, null=True, blank=True, related_name='returned_rentals')
    slot = models.ForeignKey('stations.StationSlot', on_delete=models.CASCADE)
    package = models.ForeignKey('RentalPackage', on_delete=models.CASCADE)
    power_bank = models.ForeignKey('stations.PowerBank', on_delete=models.CASCADE, null=True, blank=True)
    
    rental_code = models.CharField(max_length=10, unique=True)
    status = models.CharField(max_length=50, choices=RENTAL_STATUS_CHOICES, default='PENDING')
    payment_status = models.CharField(max_length=50, choices=PAYMENT_STATUS_CHOICES, default='PENDING')
    
    started_at = models.DateTimeField(null=True, blank=True)
    ended_at = models.DateTimeField(null=True, blank=True)
    due_at = models.DateTimeField()
    
    amount_paid = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    overdue_amount = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    
    is_returned_on_time = models.BooleanField(default=False)
    timely_return_bonus_awarded = models.BooleanField(default=False)
    rental_metadata = models.JSONField(default=dict)

    class Meta:
        db_table = "rentals"
        verbose_name = "Rental"
        verbose_name_plural = "Rentals"

    def __str__(self):
        return f"{self.rental_code} - {self.user.username}"


class RentalExtension(BaseModel):
    """
    RentalExtension - Extension of rental duration
    """
    rental = models.ForeignKey(Rental, on_delete=models.CASCADE, related_name='extensions')
    package = models.ForeignKey('RentalPackage', on_delete=models.CASCADE)
    created_by = models.ForeignKey('users.User', on_delete=models.CASCADE)
    
    extended_minutes = models.IntegerField()
    extension_cost = models.DecimalField(max_digits=10, decimal_places=2)
    extended_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "rental_extensions"
        verbose_name = "Rental Extension"
        verbose_name_plural = "Rental Extensions"

    def __str__(self):
        return f"{self.rental.rental_code} - {self.extended_minutes}min"


by looking at the tables we have and data we wll get from the device responses also mention what is excellent and what is missing either into our databse tables or that data we havennt't into the response format and what might missed to include also see APP_STRUCTURE.txt that i have given you of my django app structure of station so you can clearly make alter paln on django app alter plan also

parsed_data = all the data in formatted way without leavind any fields get from the device that is get into the (/api/rentbox/upload/data) and we will called sendDeviceData(parsed_data) this method into our apicontriller inside rentboxOrderReturnEnd() method and sendReturnedData into our /api/rentbox/order/return

a. sendDeviceData(parsed_data)
b. sendReturnedData(parsed_data) 

also we need to consider security
for this part we will make 1 method 
c. connectChargeGharMain
curl -X POST "BaseUrl/api/admin/login" -H "Content-Type: application/json" -d '{"email": "janak@powerbank.com", "password": "password123"}' -s -w "\n%{http_code}"
{"success":true,"message":"Admin login successful","data":{"user_id":"1","access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI....","refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.....yjll0OlGYNA67WLxnIE_zhSGBtzpb3CURGnVlCZkeds","user":{"id":"1","email":"janak@powerbank.com","username":"janak","is_staff":true,"is_superuser":true},"message":"Admin login successful"}}

after we get token we will use it to send data to our python main.chargeghar.com
also we will make 2 methods generateSignatureMainApp() and validateSignatureApp() into our both project in django as well as into our java
in our current porject the signatue methods will locate into src/main/java/com.demo/tools/signChargeGharMain.java
and in our 
api/stations/services/utils/signChargeGharMain.py
which lets us help to access eachother endpoints

add all (endpoinst, baseurl, logindetails, etc...) inside my this file for consistency 
src/main/resources/config.properties