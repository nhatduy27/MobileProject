package com.example.foodapp.authentication.intro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IntroScreen(
    onCustomerClicked: () -> Unit,
    onShipperClicked: () -> Unit,
    onOwnerClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Chọn vai trò để demo", modifier = Modifier.padding(bottom = 32.dp))
            Button(onClick = onCustomerClicked, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Customer")
            }
            Button(onClick = onShipperClicked, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Shipper")
            }
            Button(onClick = onOwnerClicked, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Owner")
            }
        }
    }
}
