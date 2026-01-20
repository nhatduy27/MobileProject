package com.example.foodapp.pages.client.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodapp.pages.client.components.home.UserBottomNav

data class UserNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType(val emoji: String, val color: Color) {
    ORDER("📦", Color(0xFF4CAF50)),
    DELIVERY("🚗", Color(0xFF2196F3)),
    PROMOTION("🎉", Color(0xFFFFC107)),
    SYSTEM("ℹ️", Color(0xFF9E9E9E))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotificationsScreen(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                UserNotification(
                    "1",
                    "Đơn hàng được xác nhận",
                    "Đơn hàng #12345 của bạn đã được xác nhận. Cảm ơn đã mua hàng!",
                    "Hôm nay 10:30",
                    NotificationType.ORDER,
                    false
                ),
                UserNotification(
                    "2",
                    "Đơn hàng đang giao",
                    "Shipper đang trên đường giao hàng đến bạn. Vui lòng chờ đợi.",
                    "Hôm nay 09:15",
                    NotificationType.DELIVERY,
                    false
                ),
                UserNotification(
                    "3",
                    "Khuyến mãi mới",
                    "Giảm 30% cho tất cả đồ uống hôm nay!",
                    "Hôm nay 08:00",
                    NotificationType.PROMOTION
                ),
                UserNotification(
                    "4",
                    "Giao hàng thành công",
                    "Đơn hàng #12340 của bạn đã được giao thành công.",
                    "Hôm qua 15:45",
                    NotificationType.ORDER
                ),
                UserNotification(
                    "5",
                    "Đánh giá đơn hàng",
                    "Vui lòng đánh giá đơn hàng #12340 của bạn.",
                    "Hôm qua 16:00",
                    NotificationType.SYSTEM
                ),
                UserNotification(
                    "6",
                    "Khuyến mãi đặc biệt",
                    "Được tặng voucher 50.000đ cho lần mua tiếp theo!",
                    "2 ngày trước",
                    NotificationType.PROMOTION
                ),
                UserNotification(
                    "7",
                    "Cập nhật hệ thống",
                    "Ứng dụng đã được cập nhật phiên bản mới.",
                    "3 ngày trước",
                    NotificationType.SYSTEM
                ),
            )
        )
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Thông báo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount thông báo chưa đọc",
                                fontSize = 12.sp,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800)
                ),
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            notifications = notifications.map { it.copy(isRead = true) }
                        }) {
                            Text("Đánh dấu đã đọc", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        bottomBar = {
            UserBottomNav(navController = navController, onProfileClick = { })
        },
        containerColor = Color.White
    ) { padding ->
        if (notifications.isEmpty()) {
            EmptyNotificationsContent(modifier = Modifier.padding(padding))
        } else {
            NotificationsContent(
                notifications = notifications,
                onRemoveNotification = { notificationId ->
                    notifications = notifications.filter { it.id != notificationId }
                },
                onMarkAsRead = { notificationId ->
                    notifications = notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else notification
                    }
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun NotificationsContent(
    notifications: List<UserNotification>,
    onRemoveNotification: (String) -> Unit,
    onMarkAsRead: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(notifications) { notification ->
            NotificationItemCard(
                notification = notification,
                onRemove = { onRemoveNotification(notification.id) },
                onMarkAsRead = { onMarkAsRead(notification.id) }
            )
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: UserNotification,
    onRemove: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFFFF8F3)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        notification.type.color.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(notification.type.emoji, fontSize = 24.sp)
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title with unread indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF9800), RoundedCornerShape(50))
                        )
                    }
                }

                // Message
                Text(
                    notification.message,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )

                // Timestamp
                Text(
                    notification.timestamp,
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )

                // Actions
                if (!notification.isRead) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Đánh dấu đã đọc", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Delete button
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color(0xFF999999),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🔔",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "Không có thông báo",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            "Bạn sẽ nhận được thông báo về đơn hàng của mình",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}
