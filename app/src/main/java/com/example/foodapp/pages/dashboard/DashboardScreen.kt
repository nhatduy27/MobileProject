package com.example.foodapp.pages.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF00FF00)
@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        DashboardHeader()

        Spacer(modifier = Modifier.height(16.dp))

        DashboardStatsSection()

        Spacer(modifier = Modifier.height(24.dp))

        DashboardRevenueChart()

        Spacer(modifier = Modifier.height(24.dp))

        DashboardSummaryLists()
    }
}
