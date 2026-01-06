package com.example.foodapp.data.remote.shipper.response

import com.example.foodapp.data.model.shipper.ShipperProfile

/**
 * Response DTO cho API Profile của Shipper
 * Mapping từ JSON response sang domain model
 * 
 * Backend sẽ trả về JSON format này từ endpoint /api/shipper/profile
 */
data class ProfileResponse(
    val name: String,
    val phone: String,
    val email: String,
    val vehicleType: String,
    val licensePlate: String,
    val rating: Double,
    val totalDeliveries: Int,
    val joinDate: String,       // "01/2024"
    val isVerified: Boolean
) {
    /**
     * Extension function để convert Response DTO sang Domain Model
     */
    fun toShipperProfile(): ShipperProfile = ShipperProfile(
        name = name,
        phone = phone,
        email = email,
        vehicleType = vehicleType,
        licensePlate = licensePlate,
        rating = rating,
        totalDeliveries = totalDeliveries,
        joinDate = joinDate,
        isVerified = isVerified
    )
}
