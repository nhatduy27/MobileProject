package com.example.foodapp.data.remote.shipper.response

import com.example.foodapp.data.model.shipper.DeliveryHistory
import com.example.foodapp.data.model.shipper.HistoryStatus

/**
 * Response DTO cho API History của Shipper
 * Mapping từ JSON response sang domain model
 * 
 * Backend sẽ trả về JSON format này từ endpoint /api/shipper/history
 */
data class HistoryResponse(
    val history: List<HistoryDto>
) {
    /**
     * DTO cho delivery history
     */
    data class HistoryDto(
        val orderId: String,
        val customerName: String,
        val date: String,           // "12/12/2024"
        val time: String,           // "14:30"
        val pickupAddress: String,
        val deliveryAddress: String,
        val distance: String,
        val fee: Int,
        val status: String,         // "COMPLETED", "CANCELLED"
        val rating: Double?         // null nếu chưa có rating
    )
    
    /**
     * Extension function để convert Response DTO sang Domain Model
     */
    fun toDeliveryHistoryList(): List<DeliveryHistory> = history.map { dto ->
        DeliveryHistory(
            orderId = dto.orderId,
            customerName = dto.customerName,
            date = dto.date,
            time = dto.time,
            pickupAddress = dto.pickupAddress,
            deliveryAddress = dto.deliveryAddress,
            distance = dto.distance,
            earnings = dto.fee,
            status = when (dto.status) {
                "COMPLETED" -> HistoryStatus.COMPLETED
                "CANCELLED" -> HistoryStatus.CANCELLED
                else -> HistoryStatus.COMPLETED
            },
            rating = dto.rating
        )
    }
}
