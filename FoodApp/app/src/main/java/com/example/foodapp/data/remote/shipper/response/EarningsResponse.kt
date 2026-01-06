package com.example.foodapp.data.remote.shipper.response

import com.example.foodapp.data.model.shipper.EarningsData

/**
 * Response DTO cho API Earnings của Shipper
 * Mapping từ JSON response sang domain model
 * 
 * Backend sẽ trả về JSON format này từ endpoint /api/shipper/earnings
 */
data class EarningsResponse(
    val earnings: List<EarningsDto>
) {
    /**
     * DTO cho earnings data
     */
    data class EarningsDto(
        val date: String,           // "25/12/2025"
        val totalOrders: Int,
        val totalEarnings: Int,
        val bonus: Int
    )
    
    /**
     * Extension function để convert Response DTO sang Domain Model
     */
    fun toEarningsDataList(): List<EarningsData> = earnings.map { dto ->
        EarningsData(
            date = dto.date,
            totalOrders = dto.totalOrders,
            totalEarnings = dto.totalEarnings,
            bonusEarnings = dto.bonus
        )
    }
}
