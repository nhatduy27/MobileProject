package com.example.foodapp.pages.shipper.gps

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.gps.TripLocation
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.data.repository.shipper.base.GpsRepository
import com.example.foodapp.data.repository.shipper.base.ShipperOrderRepository
import com.example.foodapp.utils.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for GPS/Trip functionality
 */
class GpsViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "GpsViewModel"
        private const val MAX_ORDERS_PER_TRIP = 15
        
        // Default origin (KTX Gate - can be updated with real location)
        val DEFAULT_ORIGIN = TripLocation(
            lat = 10.880952,
            lng = 106.782150,
            name = "Cổng chính KTX Khu B"
        )
    }
    
    private val gpsRepository: GpsRepository = RepositoryProvider.getGpsRepository()
    private val orderRepository: ShipperOrderRepository = RepositoryProvider.getShipperOrderRepository()
    
    private val _uiState = MutableStateFlow(GpsUiState())
    val uiState: StateFlow<GpsUiState> = _uiState.asStateFlow()
    
    // Location tracking
    private val _currentLocationState = MutableStateFlow<TripLocation?>(null)
    val currentLocationState: StateFlow<TripLocation?> = _currentLocationState.asStateFlow()
    private var locationJob: Job? = null
    private var locationHelper: LocationHelper? = null
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        loadAvailableOrders()
        loadShippingOrders()
        loadDeliveryPoints()
        loadActiveTrip()
    }
    
    /**
     * Refresh all data - call when entering GPS screen
     */
    fun refreshData() {
        Log.d(TAG, "Refreshing GPS data...")
        // Clear old trip first to ensure fresh state
        _uiState.update { it.copy(currentTrip = null) }
        loadInitialData()
    }
    
    /**
     * Load available READY orders that can be added to a trip
     */
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingOrders = true) }
            
            orderRepository.getAvailableOrders(page = 1, limit = 50)
                .onSuccess { data ->
                    // Show all available orders - backend will validate buildingCode when creating trip
                    Log.d(TAG, "Loaded ${data.orders.size} available orders")
                    _uiState.update { 
                        it.copy(
                            availableOrders = data.orders,
                            isLoadingOrders = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load orders", e)
                    _uiState.update { 
                        it.copy(
                            isLoadingOrders = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    /**
     * Load orders that have been accepted but not yet delivered (status = SHIPPING)
     */
    fun loadShippingOrders() {
        viewModelScope.launch {
            orderRepository.getMyOrders(status = "SHIPPING", page = 1, limit = 50)
                .onSuccess { data ->
                    Log.d(TAG, "Loaded ${data.orders.size} shipping orders")
                    _uiState.update { 
                        it.copy(shippingOrders = data.orders)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load shipping orders", e)
                }
        }
    }
    
    /**
     * Load KTX delivery points
     */
    fun loadDeliveryPoints() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDeliveryPoints = true) }
            
            gpsRepository.getDeliveryPoints()
                .onSuccess { points ->
                    Log.d(TAG, "Loaded ${points.size} delivery points")
                    _uiState.update { 
                        it.copy(
                            deliveryPoints = points,
                            isLoadingDeliveryPoints = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load delivery points", e)
                    _uiState.update { 
                        it.copy(
                            isLoadingDeliveryPoints = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    /**
     * Load active trip (PENDING or STARTED only)
     * Clears currentTrip if no active trip found
     */
    fun loadActiveTrip() {
        viewModelScope.launch {
            // First check for STARTED trips
            gpsRepository.getMyTrips(status = TripStatus.STARTED, page = 1, limit = 1)
                .onSuccess { data ->
                    if (data.items.isNotEmpty()) {
                        val trip = data.items.first()
                        // Only set if trip is truly active
                        if (trip.status == TripStatus.STARTED) {
                            _uiState.update { it.copy(currentTrip = trip) }
                            Log.d(TAG, "Found active STARTED trip: ${trip.id}")
                            return@launch
                        }
                    }
                    
                    // Then check for PENDING trips
                    gpsRepository.getMyTrips(status = TripStatus.PENDING, page = 1, limit = 1)
                        .onSuccess { pendingData ->
                            if (pendingData.items.isNotEmpty()) {
                                val trip = pendingData.items.first()
                                if (trip.status == TripStatus.PENDING) {
                                    _uiState.update { it.copy(currentTrip = trip) }
                                    Log.d(TAG, "Found active PENDING trip: ${trip.id}")
                                }
                            } else {
                                // No active trip found - clear currentTrip
                                Log.d(TAG, "No active trip found, clearing currentTrip")
                                _uiState.update { it.copy(currentTrip = null) }
                            }
                        }
                        .onFailure {
                            // On error, still clear to avoid stale data
                            _uiState.update { it.copy(currentTrip = null) }
                        }
                }
                .onFailure {
                    // On error, clear to avoid stale data
                    _uiState.update { it.copy(currentTrip = null) }
                }
        }
    }
    
    /**
     * Toggle order selection
     */
    fun toggleOrderSelection(orderId: String) {
        val currentSelected = _uiState.value.selectedOrderIds
        val newSelected = if (currentSelected.contains(orderId)) {
            currentSelected - orderId
        } else {
            if (currentSelected.size < MAX_ORDERS_PER_TRIP) {
                currentSelected + orderId
            } else {
                _uiState.update { it.copy(errorMessage = "Tối đa $MAX_ORDERS_PER_TRIP đơn hàng mỗi chuyến") }
                currentSelected
            }
        }
        _uiState.update { it.copy(selectedOrderIds = newSelected) }
    }
    
    /**
     * Select all visible orders (max 15)
     */
    fun selectAllOrders() {
        val orders = _uiState.value.availableOrders.take(MAX_ORDERS_PER_TRIP)
        _uiState.update { 
            it.copy(selectedOrderIds = orders.map { order -> order.id }.toSet())
        }
    }
    
    /**
     * Clear all selections
     */
    fun clearSelection() {
        _uiState.update { it.copy(selectedOrderIds = emptySet()) }
    }
    
    /**
     * Create optimized trip from selected orders
     * First accepts all selected orders, then creates the trip
     */
    fun createTrip(origin: TripLocation = DEFAULT_ORIGIN) {
        val selectedIds = _uiState.value.selectedOrderIds.toList()
        
        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ít nhất 1 đơn hàng") }
            return
        }
        
        if (selectedIds.size > MAX_ORDERS_PER_TRIP) {
            _uiState.update { it.copy(errorMessage = "Tối đa $MAX_ORDERS_PER_TRIP đơn hàng mỗi chuyến") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTrip = true, errorMessage = null) }
            
            // Step 1: Accept all selected orders first
            Log.d(TAG, "Accepting ${selectedIds.size} orders before creating trip...")
            val acceptedOrderIds = mutableListOf<String>()
            val failedOrders = mutableListOf<Pair<String, String>>() // orderId to error message
            
            for (orderId in selectedIds) {
                orderRepository.acceptOrder(orderId)
                    .onSuccess { order ->
                        Log.d(TAG, "Order accepted: ${order.id}")
                        acceptedOrderIds.add(order.id)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to accept order $orderId: ${e.message}")
                        failedOrders.add(orderId to (e.message ?: "Unknown error"))
                    }
            }
            
            // Check if any orders were accepted
            if (acceptedOrderIds.isEmpty()) {
                val errorMsg = if (failedOrders.isNotEmpty()) {
                    "Không thể nhận đơn: ${failedOrders.first().second}"
                } else {
                    "Không thể nhận các đơn hàng đã chọn"
                }
                _uiState.update { 
                    it.copy(isCreatingTrip = false, errorMessage = errorMsg)
                }
                return@launch
            }
            
            // Log partial success if some failed
            if (failedOrders.isNotEmpty()) {
                Log.w(TAG, "Some orders failed to accept: ${failedOrders.map { it.first }}")
            }
            
            // Step 2: Create trip with successfully accepted orders
            Log.d(TAG, "Creating trip with ${acceptedOrderIds.size} accepted orders...")
            gpsRepository.createOptimizedTrip(
                orderIds = acceptedOrderIds,
                origin = origin
            ).onSuccess { trip ->
                Log.d(TAG, "Trip created: ${trip.id}")
                val successMsg = if (failedOrders.isNotEmpty()) {
                    "Đã tạo lộ trình với ${trip.totalBuildings} điểm (${failedOrders.size} đơn không nhận được)"
                } else {
                    "Đã nhận ${acceptedOrderIds.size} đơn và tạo lộ trình với ${trip.totalBuildings} điểm dừng"
                }
                _uiState.update { 
                    it.copy(
                        currentTrip = trip,
                        isCreatingTrip = false,
                        selectedOrderIds = emptySet(),
                        successMessage = successMsg,
                        navigateToTripDetail = trip.id
                    )
                }
                // Reload available orders to reflect accepted ones
                loadAvailableOrders()
            }.onFailure { e ->
                Log.e(TAG, "Failed to create trip", e)
                _uiState.update { 
                    it.copy(
                        isCreatingTrip = false,
                        errorMessage = "Đã nhận đơn nhưng tạo lộ trình thất bại: ${e.message}"
                    )
                }
                // Reload to show current state
                loadAvailableOrders()
                loadShippingOrders()
            }
        }
    }
    
    /**
     * Start the current trip
     */
    fun startTrip() {
        val trip = _uiState.value.currentTrip ?: return
        
        if (trip.status != TripStatus.PENDING) {
            _uiState.update { it.copy(errorMessage = "Chỉ có thể bắt đầu chuyến đang chờ") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isStartingTrip = true, errorMessage = null) }
            
            gpsRepository.startTrip(trip.id)
                .onSuccess { updatedTrip ->
                    Log.d(TAG, "Trip started: ${updatedTrip.id}")
                    _uiState.update { 
                        it.copy(
                            currentTrip = updatedTrip,
                            isStartingTrip = false,
                            successMessage = "Đã bắt đầu giao hàng!",
                            navigateToMap = updatedTrip.id // Navigate to map screen
                        )
                    }
                    // Reload orders as they're now SHIPPING
                    loadAvailableOrders()
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to start trip", e)
                    _uiState.update { 
                        it.copy(
                            isStartingTrip = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    /**
     * Finish the current trip
     */
    fun finishTrip() {
        val trip = _uiState.value.currentTrip ?: return
        
        if (trip.status != TripStatus.STARTED) {
            _uiState.update { it.copy(errorMessage = "Chỉ có thể hoàn thành chuyến đang giao") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isFinishingTrip = true, errorMessage = null) }
            
            gpsRepository.finishTrip(trip.id)
                .onSuccess { (updatedTrip, ordersDelivered) ->
                    Log.d(TAG, "Trip finished: ${updatedTrip.id}, orders delivered: $ordersDelivered")
                    _uiState.update { 
                        it.copy(
                            // Clear currentTrip after finishing to avoid showing completed trip
                            currentTrip = null,
                            isFinishingTrip = false,
                            ordersDeliveredCount = ordersDelivered,
                            successMessage = "Hoàn thành! Đã giao $ordersDelivered đơn hàng"
                        )
                    }
                    // Reload orders and shipping orders
                    loadAvailableOrders()
                    loadShippingOrders()
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to finish trip", e)
                    _uiState.update { 
                        it.copy(
                            isFinishingTrip = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    /**
     * Cancel the current trip
     */
    fun cancelTrip(reason: String? = null) {
        val trip = _uiState.value.currentTrip ?: return
        
        if (trip.status != TripStatus.PENDING) {
            _uiState.update { it.copy(errorMessage = "Chỉ có thể hủy chuyến đang chờ") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingTrip = true, errorMessage = null) }
            
            gpsRepository.cancelTrip(trip.id, reason)
                .onSuccess { updatedTrip ->
                    Log.d(TAG, "Trip cancelled: ${updatedTrip.id}")
                    _uiState.update { 
                        it.copy(
                            currentTrip = null,
                            isCancellingTrip = false,
                            successMessage = "Đã hủy chuyến đi"
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to cancel trip", e)
                    _uiState.update { 
                        it.copy(
                            isCancellingTrip = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    /**
     * Load trip history
     */
    fun loadTrips(status: TripStatus? = null, page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoadingTrips = true,
                    tripStatusFilter = status,
                    tripsPage = page
                )
            }
            
            gpsRepository.getMyTrips(status = status, page = page)
                .onSuccess { data ->
                    Log.d(TAG, "Loaded ${data.items.size} trips")
                    val currentTrips = if (page == 1) emptyList() else _uiState.value.trips
                    _uiState.update { 
                        it.copy(
                            trips = currentTrips + data.items,
                            isLoadingTrips = false,
                            hasMoreTrips = data.hasNext,
                            totalTrips = data.total
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load trips", e)
                    _uiState.update { 
                        it.copy(
                            isLoadingTrips = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    /**
     * Load more trips
     */
    fun loadMoreTrips() {
        if (_uiState.value.hasMoreTrips && !_uiState.value.isLoadingTrips) {
            loadTrips(
                status = _uiState.value.tripStatusFilter,
                page = _uiState.value.tripsPage + 1
            )
        }
    }
    
    /**
     * Load trip by ID
     */
    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            gpsRepository.getTrip(tripId)
                .onSuccess { trip ->
                    _uiState.update { it.copy(currentTrip = trip) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }
    
    /**
     * Load trip containing a specific order
     */
    fun loadTripByOrderId(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrips = true) }
            
            gpsRepository.getTripByOrderId(orderId)
                .onSuccess { trip ->
                    if (trip != null) {
                        _uiState.update { it.copy(
                            currentTrip = trip,
                            isLoadingTrips = false
                        )}
                    } else {
                        _uiState.update { it.copy(
                            errorMessage = "Chưa có lộ trình cho đơn hàng này. Vui lòng tạo lộ trình mới trong mục 'Lộ trình giao hàng'.",
                            isLoadingTrips = false
                        )}
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        errorMessage = e.message,
                        isLoadingTrips = false
                    )}
                }
        }
    }
    
    /**
     * Clear messages
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearNavigation() {
        _uiState.update { it.copy(navigateToTripDetail = null, navigateToMap = null) }
    }
    
    fun clearMapNavigation() {
        _uiState.update { it.copy(navigateToMap = null) }
    }
    
    /**
     * Clear current trip (after viewing completed trip)
     */
    fun clearCurrentTrip() {
        _uiState.update { it.copy(currentTrip = null) }
    }
    
    /**
     * Start location tracking for real-time map updates
     */
    fun startLocationTracking(context: Context) {
        if (locationHelper == null) {
            locationHelper = LocationHelper(context)
        }
        
        if (!locationHelper!!.hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return
        }
        
        // Cancel any existing job
        locationJob?.cancel()
        
        locationJob = viewModelScope.launch {
            locationHelper!!.getLocationUpdates(intervalMillis = 5000L)
                .catch { e ->
                    Log.e(TAG, "Location update error", e)
                }
                .collect { location ->
                    val tripLocation = TripLocation(
                        lat = location.latitude,
                        lng = location.longitude,
                        name = "Vị trí hiện tại"
                    )
                    _currentLocationState.value = tripLocation
                    Log.d(TAG, "Location updated: ${tripLocation.lat}, ${tripLocation.lng}")
                }
        }
    }
    
    /**
     * Stop location tracking
     */
    fun stopLocationTracking() {
        locationJob?.cancel()
        locationJob = null
        _currentLocationState.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}
