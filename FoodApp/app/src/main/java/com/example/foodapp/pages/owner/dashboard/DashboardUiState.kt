package com.example.foodapp.pages.owner.dashboard

import com.example.foodapp.data.model.owner.DashboardData

/**
 * UI state cho màn hình Dashboard API.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: DashboardData? = null
)
