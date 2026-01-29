package com.example.foodapp.data.model.shipper.removal

import com.google.gson.annotations.SerializedName

/**
 * Status of removal request
 */
enum class RemovalRequestStatus {
    @SerializedName("PENDING")
    PENDING,
    @SerializedName("APPROVED")
    APPROVED,
    @SerializedName("REJECTED")
    REJECTED
}

/**
 * Type of removal request
 * - QUIT: Shipper wants to quit completely (become CUSTOMER)
 * - TRANSFER: Shipper wants to leave this shop to work at another shop (stay SHIPPER)
 */
enum class RemovalRequestType {
    @SerializedName("QUIT")
    QUIT,
    @SerializedName("TRANSFER")
    TRANSFER
}

/**
 * Shipper Removal Request Entity
 */
data class RemovalRequest(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("shipperId")
    val shipperId: String = "",
    
    @SerializedName("shipperName")
    val shipperName: String = "",
    
    @SerializedName("shipperPhone")
    val shipperPhone: String? = null,
    
    @SerializedName("shopId")
    val shopId: String = "",
    
    @SerializedName("shopName")
    val shopName: String = "",
    
    @SerializedName("ownerId")
    val ownerId: String = "",
    
    @SerializedName("type")
    val type: RemovalRequestType = RemovalRequestType.TRANSFER,
    
    @SerializedName("reason")
    val reason: String? = null,
    
    @SerializedName("status")
    val status: RemovalRequestStatus = RemovalRequestStatus.PENDING,
    
    @SerializedName("rejectionReason")
    val rejectionReason: String? = null,
    
    @SerializedName("processedAt")
    val processedAt: String? = null,
    
    @SerializedName("processedBy")
    val processedBy: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
) {
    fun getStatusDisplayName(): String = when (status) {
        RemovalRequestStatus.PENDING -> "Đang chờ xử lý"
        RemovalRequestStatus.APPROVED -> "Đã chấp nhận"
        RemovalRequestStatus.REJECTED -> "Đã từ chối"
    }
    
    fun getTypeDisplayName(): String = when (type) {
        RemovalRequestType.QUIT -> "Nghỉ việc"
        RemovalRequestType.TRANSFER -> "Chuyển shop"
    }
}

/**
 * DTO for creating a removal request
 */
data class CreateRemovalRequestDto(
    @SerializedName("shopId")
    val shopId: String,
    
    @SerializedName("type")
    val type: String = "TRANSFER", // "QUIT" or "TRANSFER"
    
    @SerializedName("reason")
    val reason: String? = null
)

/**
 * Wrapped response for single removal request
 */
data class WrappedRemovalRequestResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: RemovalRequest? = null,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Wrapped response for list of removal requests
 */
data class WrappedRemovalRequestListResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: List<RemovalRequest>? = null,
    
    @SerializedName("message")
    val message: String? = null
)
