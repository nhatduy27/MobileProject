package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onVehicleInfo: () -> Unit = {},
    onPaymentMethod: () -> Unit = {},
    onNotificationSettings: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onTerms: () -> Unit = {},
    onHelp: () -> Unit = {}
) {
    val profile = ShipperProfile(
        name = "Nguyễn Văn A",
        phone = "0901234567",
        email = "nguyenvana@email.com",
        vehicleType = "Xe máy",
        licensePlate = "59-H1 12345",
        rating = 4.8,
        totalDeliveries = 1248,
        joinDate = "01/2024",
        isVerified = true
    )

    val accountItems = listOf(
        ProfileMenuItem("👤", "Chỉnh sửa thông tin", "Tên, số điện thoại, email", ProfileAction.EDIT_PROFILE),
        ProfileMenuItem("🔒", "Đổi mật khẩu", "Cập nhật mật khẩu của bạn", ProfileAction.CHANGE_PASSWORD),
        ProfileMenuItem("🏍️", "Phương tiện", profile.licensePlate, ProfileAction.VEHICLE_INFO),
        ProfileMenuItem("💳", "Phương thức thanh toán", "Tài khoản ngân hàng", ProfileAction.PAYMENT_METHOD)
    )

    val settingsItems = listOf(
        ProfileMenuItem("🔔", "Thông báo", "Cài đặt thông báo đơn hàng", ProfileAction.NOTIFICATIONS),
        ProfileMenuItem("🌐", "Ngôn ngữ", "Tiếng Việt", ProfileAction.LANGUAGE),
        ProfileMenuItem("🔐", "Bảo mật & Quyền riêng tư", null, ProfileAction.PRIVACY),
        ProfileMenuItem("📄", "Điều khoản & Chính sách", null, ProfileAction.TERMS)
    )

    val otherItems = listOf(
        ProfileMenuItem("❓", "Trợ giúp & Hỗ trợ", null, ProfileAction.HELP),
        ProfileMenuItem("🚪", "Đăng xuất", null, ProfileAction.LOGOUT)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileMenuCard(
                title = "TÀI KHOẢN",
                items = accountItems,
                onItemClick = {
                    when (it) {
                        ProfileAction.EDIT_PROFILE -> onEditProfile()
                        ProfileAction.CHANGE_PASSWORD -> onChangePassword()
                        ProfileAction.VEHICLE_INFO -> onVehicleInfo()
                        ProfileAction.PAYMENT_METHOD -> onPaymentMethod()
                        ProfileAction.NOTIFICATIONS -> onNotificationSettings()
                        else -> {}
                    }
                }
            )
            ProfileMenuCard(
                title = "CÀI ĐẶT",
                items = settingsItems,
                onItemClick = {
                    when (it) {
                        ProfileAction.NOTIFICATIONS -> onNotificationSettings()
                        ProfileAction.LANGUAGE -> onLanguage()
                        ProfileAction.PRIVACY -> onPrivacy()
                        ProfileAction.TERMS -> onTerms()
                        else -> {}
                    }
                }
            )
            ProfileMenuCard(
                title = "KHÁC",
                items = otherItems,
                onItemClick = {
                    if (it == ProfileAction.HELP) onHelp()
                }
            )
        }
    }
}
