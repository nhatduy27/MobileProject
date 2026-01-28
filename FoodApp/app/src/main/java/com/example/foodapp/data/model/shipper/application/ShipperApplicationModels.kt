package com.example.foodapp.data.model.shipper.application

import com.google.gson.annotations.SerializedName

// Shop model for shipper to browse
data class ShopForApplication(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("rating")
    val rating: Double? = null,
    
    @SerializedName("totalRatings")
    val totalRatings: Int? = null,
    
    @SerializedName("isOpen")
    val isOpen: Boolean = false,
    
    @SerializedName("openTime")
    val openTime: String? = null,
    
    @SerializedName("closeTime")
    val closeTime: String? = null,
    
    @SerializedName("logoUrl")
    val logoUrl: String? = null,
    
    @SerializedName("coverImageUrl")
    val coverImageUrl: String? = null
)

// Inner paginated data
data class PaginatedShopsData(
    @SerializedName("shops")
    val shops: List<ShopForApplication> = emptyList(),
    
    @SerializedName("total")
    val total: Int = 0,
    
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("limit")
    val limit: Int = 20
)

// API Response wrapper: { success: true, data: {...} }
data class ShopsApiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: PaginatedShopsData? = null
)

// API Response wrapper for applications: { success: true, data: [...] }
data class ApplicationsApiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: List<ShipperApplication>? = null
)

// API Response wrapper for single application
data class ApplicationApiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: ShipperApplication? = null
)

// Cancel application response
data class CancelApplicationResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String? = null
)

// Shipper Application model - matches backend ShipperApplicationEntity
data class ShipperApplication(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("userName")
    val userName: String? = null,
    
    @SerializedName("userPhone")
    val userPhone: String? = null,
    
    @SerializedName("userAvatar")
    val userAvatar: String? = null,
    
    @SerializedName("shopId")
    val shopId: String,
    
    @SerializedName("shopName")
    val shopName: String? = null,
    
    @SerializedName("vehicleType")
    val vehicleType: String? = null, // MOTORBIKE, CAR, BICYCLE
    
    @SerializedName("vehicleNumber")
    val vehicleNumber: String? = null,
    
    @SerializedName("idCardNumber")
    val idCardNumber: String? = null,
    
    @SerializedName("idCardFrontUrl")
    val idCardFrontUrl: String? = null,
    
    @SerializedName("idCardBackUrl")
    val idCardBackUrl: String? = null,
    
    @SerializedName("driverLicenseUrl")
    val driverLicenseUrl: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("status")
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    
    @SerializedName("reviewedBy")
    val reviewedBy: String? = null,
    
    @SerializedName("reviewedAt")
    val reviewedAt: String? = null,
    
    @SerializedName("rejectReason")
    val rejectReason: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

// Apply request DTO
data class ApplyShipperRequest(
    val shopId: String,
    val vehicleType: String, // MOTORBIKE, CAR, BICYCLE
    val vehicleNumber: String,
    val idCardNumber: String,
    val message: String? = null
)

// Vehicle type enum for display
enum class VehicleType(val displayName: String, val icon: String) {
    MOTORBIKE("Xe máy", "🏍️"),
    CAR("Ô tô", "🚗"),
    BICYCLE("Xe đạp", "🚲")
}

// Status enum for display
enum class ApplicationStatus(val displayName: String, val color: Long) {
    PENDING("Đang chờ duyệt", 0xFFFF9800),
    APPROVED("Đã duyệt", 0xFF4CAF50),
    REJECTED("Bị từ chối", 0xFFF44336)
}

