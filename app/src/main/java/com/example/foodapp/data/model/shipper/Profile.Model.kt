package com.example.foodapp.data.model.shipper

// Model cho hồ sơ và menu cấu hình của Shipper

data class ShipperProfile(
    val name: String,
    val phone: String,
    val email: String,
    val vehicleType: String,
    val licensePlate: String,
    val rating: Double,
    val totalDeliveries: Int,
    val joinDate: String,
    val isVerified: Boolean
)

data class ProfileMenuItem(
    val icon: String,
    val title: String,
    val subtitle: String? = null,
    val action: ProfileAction
)

enum class ProfileAction {
    EDIT_PROFILE,
    CHANGE_PASSWORD,
    VEHICLE_INFO,
    PAYMENT_METHOD,
    NOTIFICATIONS,
    LANGUAGE,
    PRIVACY,
    TERMS,
    HELP,
    LOGOUT
}
