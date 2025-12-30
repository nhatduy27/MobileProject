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
    Column {
        // Hiển thị theo dạng 2x2 giống layout cũ nếu đủ 4 stat
        val rows = stats.chunked(2)

        rows.forEachIndexed { index, rowStats ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                rowStats.forEachIndexed { colIndex, stat ->
                    if (colIndex > 0) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    DashboardStatCard(
                        iconRes = stat.iconRes,
                        value = stat.value,
                        label = stat.label,
                        color = stat.color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
