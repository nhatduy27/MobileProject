package com.example.foodapp.pages.shipper.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

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
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VehicleField("Loại phương tiện", vehicleType, { vehicleType = it }, Icons.Outlined.DirectionsBike)
                HorizontalDivider(color = ShipperColors.Divider)
                VehicleField("Biển số xe", licensePlate, { licensePlate = it }, Icons.Outlined.Badge)
                HorizontalDivider(color = ShipperColors.Divider)
                VehicleField("Màu xe", color, { color = it }, Icons.Outlined.ColorLens)
                HorizontalDivider(color = ShipperColors.Divider)
                VehicleField("Hãng xe", brand, { brand = it }, Icons.Outlined.BrandingWatermark)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ShipperColors.TextSecondary
                )
            ) {
                Text("Hủy")
            }
            Button(
                onClick = {
                    Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
            ) {
                Text("Lưu", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun VehicleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShipperColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = ShipperColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ShipperColors.Primary,
                unfocusedBorderColor = ShipperColors.Divider
            ),
            singleLine = true
        )
    }
}
