package com.example.foodapp.data.repository.shipper.profile

import com.example.foodapp.data.model.shipper.ProfileAction
import com.example.foodapp.data.model.shipper.ProfileMenuItem
import com.example.foodapp.data.model.shipper.ShipperProfile

/**
 * Repository mock cho màn Hồ sơ Shipper.
 */
class MockShipperProfileRepository {

    fun getProfile(): ShipperProfile = ShipperProfile(
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

    fun getAccountItems(profile: ShipperProfile): List<ProfileMenuItem> = listOf(
        ProfileMenuItem("👤", "Chỉnh sửa thông tin", "Tên, số điện thoại, email", ProfileAction.EDIT_PROFILE),
        ProfileMenuItem("🔒", "Đổi mật khẩu", "Cập nhật mật khẩu của bạn", ProfileAction.CHANGE_PASSWORD),
        ProfileMenuItem("🏍️", "Phương tiện", profile.licensePlate, ProfileAction.VEHICLE_INFO),
        ProfileMenuItem("💳", "Phương thức thanh toán", "Tài khoản ngân hàng", ProfileAction.PAYMENT_METHOD)
    )

    fun getSettingsItems(): List<ProfileMenuItem> = listOf(
        ProfileMenuItem("🔔", "Thông báo", "Cài đặt thông báo đơn hàng", ProfileAction.NOTIFICATIONS),
        ProfileMenuItem("🌐", "Ngôn ngữ", "Tiếng Việt", ProfileAction.LANGUAGE),
        ProfileMenuItem("🔐", "Bảo mật & Quyền riêng tư", null, ProfileAction.PRIVACY),
        ProfileMenuItem("📄", "Điều khoản & Chính sách", null, ProfileAction.TERMS)
    )

    fun getOtherItems(): List<ProfileMenuItem> = listOf(
        ProfileMenuItem("❓", "Trợ giúp & Hỗ trợ", null, ProfileAction.HELP),
        ProfileMenuItem("🚪", "Đăng xuất", null, ProfileAction.LOGOUT)
    )
}
