package com.example.foodapp.data.model.owner.removal

import com.google.gson.annotations.SerializedName

/**
 * Status of removal request
 */
enum class OwnerRemovalRequestStatus {
    @SerializedName("PENDING")
    PENDING,
    @SerializedName("APPROVED")
    APPROVED,
    @SerializedName("REJECTED")
    REJECTED
}

/**
 * Type of removal request
 */
enum class OwnerRemovalRequestType {
    @SerializedName("QUIT")
    QUIT,
    @SerializedName("TRANSFER")
    TRANSFER
}

/**
 * Shipper Removal Request Entity for Owner view
 */
data class OwnerRemovalRequest(
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
    val type: OwnerRemovalRequestType = OwnerRemovalRequestType.TRANSFER,
    
    @SerializedName("reason")
    val reason: String? = null,
    
    @SerializedName("status")
    val status: OwnerRemovalRequestStatus = OwnerRemovalRequestStatus.PENDING,
    
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
        OwnerRemovalRequestStatus.PENDING -> "Đang chờ xử lý"
        OwnerRemovalRequestStatus.APPROVED -> "Đã chấp nhận"
        OwnerRemovalRequestStatus.REJECTED -> "Đã từ chối"
    }
    
    fun getTypeDisplayName(): String = when (type) {
        OwnerRemovalRequestType.QUIT -> "Nghỉ việc"
        OwnerRemovalRequestType.TRANSFER -> "Chuyển shop"
    }
}

/**
 * DTO for processing (approve/reject) a removal request
 */
data class ProcessRemovalRequestDto(
    @SerializedName("action")
    val action: String, // "APPROVE" or "REJECT"
    
    @SerializedName("rejectionReason")
    val rejectionReason: String? = null
)

/**
 * Wrapped response for single removal request
 */
data class WrappedOwnerRemovalRequestResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: OwnerRemovalRequest? = null,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Wrapped response for list of removal requests
 */
data class WrappedOwnerRemovalRequestListResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: List<OwnerRemovalRequest>? = null,
    
    @SerializedName("message")
    val message: String? = null
)
