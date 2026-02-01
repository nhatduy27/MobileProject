package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.pages.owner.theme.OwnerColors

@Composable
fun RevenueScreen(
    revenueViewModel: RevenueViewModel = viewModel()
) {
    val uiState by revenueViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OwnerColors.Background)
    ) {
        // Period Filter
        PeriodFilter(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = { revenueViewModel.onPeriodSelected(it) }
        )
        
        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OwnerColors.Primary)
            }
        }
        // Error state
        else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: stringResource(R.string.error_unknown),
                    color = OwnerColors.Error,
                    textAlign = TextAlign.Center
                )
            }
        }
        // Content
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.stats.forEach { stat ->
                            RevenueStatCard(stat = stat)
                        }
                    }
                }

                // Chart
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ChartSection()
                    }
                }

                // Time Slots Section
                if (uiState.timeSlots.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.revenue_time_slots_detail),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OwnerColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    items(uiState.timeSlots) { timeSlot ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            TimeSlotCard(timeSlot = timeSlot)
                        }
                    }
                }

                // Top Products Section
                if (uiState.topProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.revenue_top_products_period, getPeriodDisplayName(uiState.selectedPeriod).lowercase()),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OwnerColors.TextPrimary,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        )
                    }

                    items(uiState.topProducts) { product ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            TopProductCard(product = product)
                        }
                    }
                }
                
                // Empty state
                if (uiState.stats.isEmpty() && uiState.timeSlots.isEmpty() && uiState.topProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.revenue_no_data),
                                color = OwnerColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
