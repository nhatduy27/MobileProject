package com.example.foodapp.pages.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardSummaryLists() {

    Row(modifier = Modifier.fillMaxWidth()) {

        // Đơn gần đây
        Card(
            modifier = Modifier
                .weight(1f)
                .height(400.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Đơn hàng gần đây", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                Text("[Danh sách đơn sẽ render bằng LazyColumn]")
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Món bán chạy
        Card(
            modifier = Modifier
                .weight(1f)
                .height(400.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Món bán chạy", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                Text("[Danh sách món sẽ render bằng LazyColumn]")
            }
        }
    }
}
