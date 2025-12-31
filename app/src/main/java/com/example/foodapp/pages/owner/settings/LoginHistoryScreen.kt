package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.*

data class LoginSession(
    val id: String,
    val deviceName: String,
    val deviceType: String,
    val location: String,
    val ipAddress: String,
    val loginTime: Date,
    val isCurrentSession: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginHistoryScreen(navController: NavHostController) {
    // Mock data - TODO: Load from Firebase
    val loginSessions = remember {
        listOf(
            LoginSession(
                "1",
                "Samsung Galaxy S21",
                "Android",
                "Hồ Chí Minh, Việt Nam",
                "192.168.1.100",
                Date(),
                true
            ),
            LoginSession(
                "2",
                "iPhone 13 Pro",
                "iOS",
                "Hà Nội, Việt Nam",
                "192.168.1.101",
                Date(System.currentTimeMillis() - 86400000)
            ),
            LoginSession(
                "3",
                "Chrome on Windows",
                "Web",
                "Đà Nẵng, Việt Nam",
                "192.168.1.102",
                Date(System.currentTimeMillis() - 172800000)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Lịch sử đăng nhập", fontWeight = FontWeight.Bold) },
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🔐", fontSize = 24.sp)
                        Text(
                            text = "Đây là danh sách các thiết bị đã đăng nhập vào tài khoản của bạn. Nếu bạn không nhận ra thiết bị nào, hãy đổi mật khẩu ngay lập tức.",
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            items(loginSessions) { session ->
                LoginSessionCard(session = session, onLogout = {
                    // TODO: Implement logout from specific device
                })
            }
        }
    }
}

@Composable
fun LoginSessionCard(
    session: LoginSession,
    onLogout: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val deviceIcon = when (session.deviceType) {
        "Android" -> "📱"
        "iOS" -> "📱"
        "Web" -> "💻"
        else -> "📱"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isCurrentSession) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFF6B35), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = deviceIcon, fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = session.deviceName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = session.deviceType,
                            fontSize = 13.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                if (session.isCurrentSession) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50)
                    ) {
                        Text(
                            text = "Hiện tại",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoRow(icon = "📍", text = session.location)
                InfoRow(icon = "🌐", text = session.ipAddress)
                InfoRow(icon = "🕐", text = dateFormat.format(session.loginTime))
            }

            if (!session.isCurrentSession) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Đăng xuất khỏi thiết bị này", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
    }
}
