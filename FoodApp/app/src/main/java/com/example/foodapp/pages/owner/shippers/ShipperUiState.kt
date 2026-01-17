package com.example.foodapp.pages.owner.shippers

import com.example.foodapp.data.model.owner.shipper.*

/**
 * UI State cho màn hình quản lý shipper
 * Bao gồm 2 tabs: Applications và Active Shippers
 */
data class ShipperUiState(
    // Tab selection
    val selectedTab: Int = 0, // 0 = Applications, 1 = Active Shippers
    
    // Applications data
    val applications: List<ShipperApplication> = emptyList(),
    val selectedApplicationStatus: ApplicationStatus? = null,
    
    // Active shippers data
    val shippers: List<Shipper> = emptyList(),
    
    // Search
    val searchQuery: String = "",
    
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isProcessing: Boolean = false, // For approve/reject/remove actions
    
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
    val busyShippers: Int = 0
)
