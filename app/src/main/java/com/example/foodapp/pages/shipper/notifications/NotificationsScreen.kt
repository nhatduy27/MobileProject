package com.example.foodapp.pages.shipper.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationsScreen() {
    val notifications = listOf(
        Notification(
            "1",
            "Đơn hàng mới #ORD10250",
            "Bạn có đơn hàng mới cần giao tại KTX Khu B. Khoảng cách: 1.2km, phí ship: 20.000đ",
            "5 phút trước",
            NotificationType.NEW_ORDER,
            false
        ),
        Notification(
            "2",
            "Đơn hàng #ORD10248 đã hoàn thành",
            "Bạn đã giao thành công đơn hàng. +25.000đ đã được cộng vào tài khoản.",
            "30 phút trước",
            NotificationType.PAYMENT,
            false
        ),
        Notification(
            "3",
            "Cập nhật đơn hàng #ORD10247",
            "Khách hàng đã xác nhận nhận hàng. Vui lòng đánh giá khách hàng.",
            "1 giờ trước",
            NotificationType.ORDER_UPDATE,
            true
        ),
        Notification(
            "4",
            "Khuyến mãi đặc biệt",
            "Tăng 20% phí ship cho tất cả đơn hàng từ 18h-20h hôm nay!",
            "2 giờ trước",
            NotificationType.PROMOTION,
            true
        ),
        Notification(
            "5",
            "Thanh toán tuần này",
            "Bạn đã kiếm được 850.000đ tuần này. Tiền sẽ được chuyển vào tài khoản trong 1-2 ngày.",
            "1 ngày trước",
            NotificationType.PAYMENT,
            true
        ),
        Notification(
            "6",
            "Cập nhật hệ thống",
            "Ứng dụng đã được cập nhật lên phiên bản mới với nhiều tính năng cải tiến.",
            "2 ngày trước",
            NotificationType.SYSTEM,
            true
        )
    )

    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (unreadCount > 0) {
            Text(
                text = "$unreadCount thông báo chưa đọc",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF6B35),
                modifier = Modifier.padding(16.dp).padding(bottom = 0.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            notifications.forEach { notification ->
                NotificationCard(notification)
            }
        }
    }
}
