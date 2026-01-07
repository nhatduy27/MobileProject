package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        DashboardHeader(onMenuClick = onMenuClick)

        Spacer(modifier = Modifier.height(20.dp))

        DashboardStatsSection(stats = uiState.stats)

        Spacer(modifier = Modifier.height(20.dp))

        DashboardRevenueChart(weeklyRevenue = uiState.weeklyRevenue)

        Spacer(modifier = Modifier.height(20.dp))

        DashboardSummaryLists(
            recentOrders = uiState.recentOrders,
            topProducts = uiState.topProducts
        )

        // Bottom padding for better scrolling
        Spacer(modifier = Modifier.height(16.dp))
    }
}
