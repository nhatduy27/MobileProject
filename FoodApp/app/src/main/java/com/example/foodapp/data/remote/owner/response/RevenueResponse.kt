package com.example.foodapp.data.remote.owner.response

// Minimal RevenueResponse DTO to satisfy OwnerApiService import.
// Extend fields and add mappers to domain models as needed.

data class RevenueResponse(
    val totalRevenue: Int,
    val daily: List<DayRevenueDto>
) {
    data class DayRevenueDto(
        val date: String,
        val amount: Int
    )
}
