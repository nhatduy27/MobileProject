package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
            Text("Thứ Tư, 11 Tháng 12, 2025", style = MaterialTheme.typography.bodyMedium)
        }

        Row(
            modifier = Modifier.clickable { }
        ) {

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Admin", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.width(8.dp))

            // Thay arrow_down_float bằng icon vector Compose
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
