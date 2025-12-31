package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreInfoScreen(navController: NavHostController) {
    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var storeEmail by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("08:00") }
    var closeTime by remember { mutableStateOf("22:00") }
    var isEditing by remember { mutableStateOf(false) }

    // TODO: Load store info from Firebase
    LaunchedEffect(Unit) {
        storeName = "KTX Food Store"
        storeAddress = "Khu Ký túc xá ĐHQG, Phường Đông Hòa, TP. Dĩ An, Bình Dương"
        storePhone = "0123456789"
        storeEmail = "ktxfood@example.com"
        description = "Cửa hàng đồ ăn chuyên phục vụ sinh viên KTX"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Thông tin cửa hàng", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF333333)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Store Logo/Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFFF6B35), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏪",
                            fontSize = 56.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { /* TODO: Change logo */ }) {
                        Text("Thay đổi logo cửa hàng", color = Color(0xFFFF6B35))
                    }
                }
            }

            // Basic Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thông tin cơ bản",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333)
                    )

                    StoreInfoField(
                        label = "Tên cửa hàng",
                        value = storeName,
                        onValueChange = { storeName = it },
                        enabled = isEditing,
                        icon = "🏪"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    StoreInfoField(
                        label = "Số điện thoại",
                        value = storePhone,
                        onValueChange = { storePhone = it },
                        enabled = isEditing,
                        icon = "📞"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    StoreInfoField(
                        label = "Email",
                        value = storeEmail,
                        onValueChange = { storeEmail = it },
                        enabled = isEditing,
                        icon = "📧"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    StoreInfoField(
                        label = "Địa chỉ",
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        enabled = isEditing,
                        icon = "📍",
                        singleLine = false
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    StoreInfoField(
                        label = "Mô tả",
                        value = description,
                        onValueChange = { description = it },
                        enabled = isEditing,
                        icon = "📝",
                        singleLine = false
                    )
                }
            }

            // Business Hours
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Giờ hoạt động",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StoreInfoField(
                            label = "Giờ mở cửa",
                            value = openTime,
                            onValueChange = { openTime = it },
                            enabled = isEditing,
                            icon = "🌅",
                            modifier = Modifier.weight(1f)
                        )

                        StoreInfoField(
                            label = "Giờ đóng cửa",
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            enabled = isEditing,
                            icon = "🌙",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Thời gian hiển thị: $openTime - $closeTime",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy", modifier = Modifier.padding(vertical = 4.dp))
                    }
                    Button(
                        onClick = {
                            // TODO: Save to Firebase
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                    ) {
                        Text("Lưu", modifier = Modifier.padding(vertical = 4.dp))
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                    ) {
                        Text("Chỉnh sửa thông tin", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StoreInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: String,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color(0xFF333333),
                disabledBorderColor = Color(0xFFEEEEEE),
                disabledContainerColor = Color(0xFFFAFAFA),
                focusedBorderColor = Color(0xFFFF6B35),
                unfocusedBorderColor = Color(0xFFEEEEEE)
            ),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 3
        )
    }
}
