package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast

@Composable
fun VehicleInfoScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var vehicleType by remember { mutableStateOf("Xe máy") }
    var licensePlate by remember { mutableStateOf("59-H1 12345") }
    var color by remember { mutableStateOf("Đen") }
    var brand by remember { mutableStateOf("Honda") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Thông tin phương tiện", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        OutlinedTextField(
            value = vehicleType,
            onValueChange = { vehicleType = it },
            label = { Text("Loại phương tiện") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = licensePlate,
            onValueChange = { licensePlate = it },
            label = { Text("Biển số xe") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Màu xe") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Hãng xe") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                onSave()
            }) { Text("Lưu") }
            OutlinedButton(onClick = onCancel) { Text("Hủy") }
        }
    }
}
