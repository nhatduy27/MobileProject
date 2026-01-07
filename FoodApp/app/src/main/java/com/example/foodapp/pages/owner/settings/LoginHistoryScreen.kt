package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.vector.ImageVector
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đăng nhập", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Đây là danh sách các thiết bị đã đăng nhập vào tài khoản của bạn. Nếu bạn không nhận ra thiết bị nào, hãy đổi mật khẩu ngay lập tức.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        "Android" -> Icons.Outlined.Smartphone
        "iOS" -> Icons.Outlined.Smartphone
        "Web" -> Icons.Outlined.Laptop
        else -> Icons.Outlined.Smartphone
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isCurrentSession) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.1f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if(session.isCurrentSession) MaterialTheme.colorScheme.primary.copy(alpha=0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
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
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = deviceIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = session.deviceName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = session.deviceType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (session.isCurrentSession) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50) // Keep green for "Active"
                    ) {
                        Text(
                            text = "Hiện tại",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoRow(icon = Icons.Outlined.LocationOn, text = session.location)
                InfoRow(icon = Icons.Outlined.Language, text = session.ipAddress)
                InfoRow(icon = Icons.Outlined.AccessTime, text = dateFormat.format(session.loginTime))
            }

            if (!session.isCurrentSession) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Đăng xuất khỏi thiết bị này", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
