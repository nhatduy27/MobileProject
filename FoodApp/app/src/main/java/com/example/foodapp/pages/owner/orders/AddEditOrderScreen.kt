package com.example.foodapp.pages.owner.orders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.Order
import com.example.foodapp.data.model.owner.OrderStatus
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderScreen(
    initialOrder: Order? = null,
    onBack: () -> Unit,
    onSave: (Order) -> Unit
) {
    BackHandler { onBack() }
    
    var id by remember(initialOrder) { mutableStateOf(initialOrder?.id ?: "#ORD" + System.currentTimeMillis().toString().takeLast(4)) }
    var customerName by remember(initialOrder) { mutableStateOf(initialOrder?.customerName ?: "") }
    var location by remember(initialOrder) { mutableStateOf(initialOrder?.location ?: "") }
    var items by remember(initialOrder) { mutableStateOf(initialOrder?.items ?: "") }
    var time by remember(initialOrder) { mutableStateOf(initialOrder?.time ?: "") }
    var price by remember(initialOrder) { mutableStateOf((initialOrder?.price ?: 0).toString()) }
    var status by remember(initialOrder) { mutableStateOf(initialOrder?.status ?: OrderStatus.PENDING) }

    val statusOptions = OrderStatus.values().toList()

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Order Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Thông tin đơn hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        // Order ID
                        OutlinedTextField(
                            value = id,
                            onValueChange = { id = it },
                            label = { Text("Mã đơn hàng") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        // Customer Name
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Tên khách hàng") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        // Location
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Địa chỉ giao hàng") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Order Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Chi tiết đơn hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        // Items
                        OutlinedTextField(
                            value = items,
                            onValueChange = { items = it },
                            label = { Text("Danh sách món ăn") },
                            placeholder = { Text("Nhập các món ăn, mỗi món một dòng") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Time
                            OutlinedTextField(
                                value = time,
                                onValueChange = { time = it },
                                label = { Text("Thời gian") },
                                placeholder = { Text("HH:mm") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )

                            // Price
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Giá (đ)") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                        }

                        // Status Dropdown
                        var expandedStatus by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = !expandedStatus }
                        ) {
                            OutlinedTextField(
                                value = status.displayName,
                                onValueChange = {},
                                label = { Text("Trạng thái") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35)
                                    )
                                },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
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

            // Bottom Save Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Button(
                    onClick = {
                        if (customerName.isNotBlank() && location.isNotBlank()) {
                            val order = Order(
                                id = id,
                                customerName = customerName,
                                location = location,
                                items = items,
                                time = time.ifBlank { "--:--" },
                                price = price.toIntOrNull() ?: 0,
                                status = status
                            )
                            onSave(order)
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        imageVector = if (initialOrder == null) Icons.Default.Add else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialOrder == null) "Thêm đơn hàng" else "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
