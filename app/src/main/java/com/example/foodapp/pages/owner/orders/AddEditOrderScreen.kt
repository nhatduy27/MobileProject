package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    var id by remember(initialOrder) { mutableStateOf(initialOrder?.id ?: "#ORD" + System.currentTimeMillis().toString().takeLast(4)) }
    var customerName by remember(initialOrder) { mutableStateOf(initialOrder?.customerName ?: "") }
    var location by remember(initialOrder) { mutableStateOf(initialOrder?.location ?: "") }
    var items by remember(initialOrder) { mutableStateOf(initialOrder?.items ?: "") }
    var time by remember(initialOrder) { mutableStateOf(initialOrder?.time ?: "") }
    var price by remember(initialOrder) { mutableStateOf((initialOrder?.price ?: 0).toString()) }
    var status by remember(initialOrder) { mutableStateOf(initialOrder?.status ?: OrderStatus.PENDING) }

    val statusOptions = OrderStatus.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (initialOrder == null) "Thêm đơn hàng" else "Chỉnh sửa đơn hàng",
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
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
            ) {
                Text(
                    text = if (initialOrder == null) "Thêm đơn" else "Lưu thay đổi",
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
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("Mã đơn hàng") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Tên khách hàng") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Địa chỉ giao") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = items,
                onValueChange = { items = it },
                label = { Text("Chi tiết món (gạch đầu dòng)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Thời gian") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá (đ)") },
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
