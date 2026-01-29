package com.example.foodapp.pages.owner.shippers

import com.example.foodapp.data.model.owner.shipper.*
import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequest
import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequestStatus

/**
 * UI State cho màn hình quản lý shipper
 * Bao gồm 3 tabs: Applications, Active Shippers, và Removal Requests
 */
data class ShipperUiState(
    // Tab selection
    val selectedTab: Int = 0, // 0 = Applications, 1 = Active Shippers, 2 = Removal Requests
    
    // Applications data
    val applications: List<ShipperApplication> = emptyList(),
    val selectedApplicationStatus: ApplicationStatus? = null,
    
    // Active shippers data
    val shippers: List<Shipper> = emptyList(),
    
    // Removal requests data
    val removalRequests: List<OwnerRemovalRequest> = emptyList(),
    val selectedRemovalStatus: OwnerRemovalRequestStatus? = null,
    
    // Search
    val searchQuery: String = "",
    
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isProcessing: Boolean = false, // For approve/reject/remove actions
    
    // Dialog state
    val showRejectDialog: Boolean = false,
    val rejectingRequestId: String? = null,
    val rejectionReason: String = "",
    
    // Messages
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Statistics data
 */
data class ShipperStats(
    val totalApplications: Int = 0,
    val pendingApplications: Int = 0,
    val totalShippers: Int = 0,
    val availableShippers: Int = 0,
    val busyShippers: Int = 0,
    // Removal requests stats
    val totalRemovalRequests: Int = 0,
    val pendingRemovalRequests: Int = 0
)
