package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    val sections = listOf(
        SettingSection(
            title = "TÀI KHOẢN",
            items = listOf(
                SettingItem(
                    title = "Thông tin cá nhân",
                    subtitle = "Chỉnh sửa thông tin tài khoản",
                    icon = "👤",
                    onClick = { /* TODO */ }
                ),
                SettingItem(
                    title = "Đổi mật khẩu",
                    subtitle = "Thay đổi mật khẩu đăng nhập",
                    icon = "🔐",
                    onClick = { /* TODO */ }
                )
            )
        ),
        SettingSection(
            title = "CỬA HÀNG",
            items = listOf(
                SettingItem(
                    title = "Thông tin cửa hàng",
                    subtitle = "Tên, địa chỉ, giờ mở cửa",
                    icon = "🏪",
                    onClick = { /* TODO */ }
                ),
                SettingItem(
                    title = "Phương thức thanh toán",
                    subtitle = "Quản lý tài khoản ngân hàng",
                    icon = "💳",
                    onClick = { /* TODO */ }
                )
            )
        ),
        SettingSection(
            title = "THÔNG BÁO",
            items = listOf(
                SettingItem(
                    title = "Đơn hàng mới",
                    subtitle = "Nhận thông báo khi có đơn mới",
                    icon = "🔔",
                    hasSwitch = true,
                    isEnabled = true
                ),
                SettingItem(
                    title = "Cập nhật đơn hàng",
                    subtitle = "Thông báo trạng thái đơn hàng",
                    icon = "📦",
                    hasSwitch = true,
                    isEnabled = true
                ),
                SettingItem(
                    title = "Khuyến mãi",
                    subtitle = "Nhận thông báo ưu đãi",
                    icon = "🎁",
                    hasSwitch = true,
                    isEnabled = false
                )
            )
        ),
        SettingSection(
            title = "BẢO MẬT",
            items = listOf(
                SettingItem(
                    title = "Xác thực 2 bước",
                    subtitle = "Tăng cường bảo mật tài khoản",
                    icon = "🔒",
                    hasSwitch = true,
                    isEnabled = false
                ),
                SettingItem(
                    title = "Lịch sử đăng nhập",
                    subtitle = "Xem các phiên đăng nhập gần đây",
                    icon = "📱",
                    onClick = { /* TODO */ }
                )
            )
        ),
        SettingSection(
            title = "VỀ ỨNG DỤNG",
            items = listOf(
                SettingItem(
                    title = "Điều khoản sử dụng",
                    subtitle = "Quy định và chính sách",
                    icon = "📋",
                    onClick = { /* TODO */ }
                ),
                SettingItem(
                    title = "Chính sách bảo mật",
                    subtitle = "Cách chúng tôi bảo vệ dữ liệu",
                    icon = "🛡️",
                    onClick = { /* TODO */ }
                ),
                SettingItem(
                    title = "Trợ giúp & Hỗ trợ",
                    subtitle = "Liên hệ với chúng tôi",
                    icon = "💬",
                    onClick = { /* TODO */ }
                )
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        SettingsHeader()

        // Settings List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            sections.forEach { section ->
                SettingSectionCard(
                    section = section,
                    onSwitchChanged = { title, enabled ->
                        // TODO: Handle switch changes
                        println("$title switched to $enabled")
                    }
                )
            }

            // Version Info
            Text(
                text = "KTX Food Store\nVersion 1.0.0",
                fontSize = 12.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
        }
    }
}
