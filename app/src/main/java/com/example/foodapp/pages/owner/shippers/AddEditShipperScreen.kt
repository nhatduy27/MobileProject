package com.example.foodapp.pages.owner.shippers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.Shipper
import com.example.foodapp.data.model.owner.ShipperStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditShipperScreen(
    initialShipper: Shipper? = null,
    onBack: () -> Unit,
    onSave: (Shipper) -> Unit
) {
    var name by remember(initialShipper) { mutableStateOf(initialShipper?.name ?: "") }
    var phone by remember(initialShipper) { mutableStateOf(initialShipper?.phone ?: "") }
    var rating by remember(initialShipper) { mutableStateOf((initialShipper?.rating ?: 4.5).toString()) }
    var totalDeliveries by remember(initialShipper) { mutableStateOf((initialShipper?.totalDeliveries ?: 0).toString()) }
    var todayDeliveries by remember(initialShipper) { mutableStateOf((initialShipper?.todayDeliveries ?: 0).toString()) }
    var status by remember(initialShipper) { mutableStateOf(initialShipper?.status ?: ShipperStatus.AVAILABLE) }
    var avatarUrl by remember(initialShipper) { mutableStateOf(initialShipper?.avatarUrl ?: "") }

    val statusOptions = ShipperStatus.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (initialShipper == null) "Thêm shipper" else "Chỉnh sửa shipper",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9),
        bottomBar = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val shipper = Shipper(
                            id = initialShipper?.id ?: "SH" + System.currentTimeMillis().toString().takeLast(4),
                            name = name,
                            phone = phone,
                            rating = rating.toDoubleOrNull() ?: 4.5,
                            totalDeliveries = totalDeliveries.toIntOrNull() ?: 0,
                            todayDeliveries = todayDeliveries.toIntOrNull() ?: 0,
                            status = status,
                            avatarUrl = avatarUrl.ifBlank { "https://images.pexels.com/photos/4391470/pexels-photo-4391470.jpeg?auto=compress&cs=tinysrgb&w=400" }
                        )
                        onSave(shipper)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
            ) {
                Text(
                    text = if (initialShipper == null) "Thêm shipper" else "Lưu thay đổi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar shipper",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                        Text("Thêm link ảnh avatar", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            OutlinedTextField(
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                label = { Text("Link ảnh avatar (tùy chọn)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên shipper") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Đánh giá (sao)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = totalDeliveries,
                onValueChange = { totalDeliveries = it },
                label = { Text("Tổng đơn đã giao") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = todayDeliveries,
                onValueChange = { todayDeliveries = it },
                label = { Text("Đơn hôm nay") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            var expandedStatus by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = status.displayName,
                    onValueChange = {},
                    label = { Text("Trạng thái") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = {
                                status = option
                                expandedStatus = false
                            }
                        )
                    }
                }
            }
        }
    }
}
