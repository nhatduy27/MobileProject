package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodapp.data.model.owner.DashboardStat

@Composable
fun DashboardStatsSection(
    stats: List<DashboardStat>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Display stats in 2x2 grid
        val rows = stats.chunked(2)
        
        rows.forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowStats.forEach { stat ->
                    DashboardStatCard(
                        iconRes = stat.iconRes,
                        value = stat.value,
                        label = stat.label,
                        color = stat.color,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Add empty space if odd number of items in row
                if (rowStats.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
