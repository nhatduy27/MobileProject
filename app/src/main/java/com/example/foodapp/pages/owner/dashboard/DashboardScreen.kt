package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        DashboardHeader()

        Spacer(modifier = Modifier.height(16.dp))

        DashboardStatsSection(stats = uiState.stats)

        Spacer(modifier = Modifier.height(24.dp))

        DashboardRevenueChart(weeklyRevenue = uiState.weeklyRevenue)

        Spacer(modifier = Modifier.height(24.dp))

        DashboardSummaryLists(
            recentOrders = uiState.recentOrders,
            topProducts = uiState.topProducts
        )
    }
}
