package com.example.foodapp.data.model.owner.shipper

import com.google.gson.annotations.SerializedName

/**
 * Enums
 */
enum class ApplicationStatus {
    @SerializedName("PENDING") PENDING,
    @SerializedName("APPROVED") APPROVED,
    @SerializedName("REJECTED") REJECTED
}

enum class VehicleType {
    @SerializedName("MOTORBIKE") MOTORBIKE,
    @SerializedName("CAR") CAR,
    @SerializedName("BICYCLE") BICYCLE
}

enum class ShipperStatus {
    @SerializedName("AVAILABLE") AVAILABLE,
    @SerializedName("BUSY") BUSY,
    @SerializedName("OFFLINE") OFFLINE
}

/**
 * Shipper Application Entity
 */
data class ShipperApplication(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("userPhone") val userPhone: String,
    @SerializedName("userAvatar") val userAvatar: String,
    @SerializedName("shopId") val shopId: String,
    @SerializedName("shopName") val shopName: String,
    @SerializedName("vehicleType") val vehicleType: VehicleType,
    @SerializedName("vehicleNumber") val vehicleNumber: String,
    @SerializedName("idCardNumber") val idCardNumber: String,
    @SerializedName("idCardFrontUrl") val idCardFrontUrl: String,
    @SerializedName("idCardBackUrl") val idCardBackUrl: String,
    @SerializedName("driverLicenseUrl") val driverLicenseUrl: String,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: ApplicationStatus,
    @SerializedName("reviewedBy") val reviewedBy: String? = null,
    @SerializedName("reviewedAt") val reviewedAt: String? = null,
    @SerializedName("rejectReason") val rejectReason: String? = null,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Shipper Info nested object
 */
data class ShipperInfo(
    @SerializedName("shopId") val shopId: String,
    @SerializedName("shopName") val shopName: String,
    @SerializedName("vehicleType") val vehicleType: VehicleType,
    @SerializedName("vehicleNumber") val vehicleNumber: String,
    @SerializedName("status") val status: ShipperStatus,
    @SerializedName("rating") val rating: Double,
    @SerializedName("totalDeliveries") val totalDeliveries: Int,
    @SerializedName("currentOrders") val currentOrders: List<String>,
    @SerializedName("joinedAt") val joinedAt: String
)

/**
 * Shipper Entity
 */
data class Shipper(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("shipperInfo") val shipperInfo: ShipperInfo? = null
)

/**
 * API Response Wrappers
 */
data class ApplicationsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ShipperApplication>
)

data class ShippersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Shipper>
)

data class MessageResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String
)

/**
 * Request DTOs
 */
data class RejectApplicationRequest(
    @SerializedName("reason") val reason: String
)
