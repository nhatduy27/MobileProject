package com.example.foodapp.data.remote.owner.response

// Minimal CustomersResponse DTO to satisfy OwnerApiService import.
// Extend fields and add mappers to domain models as needed.

data class CustomersResponse(
    val customers: List<CustomerDto>
) {
    data class CustomerDto(
        val id: String,
        val name: String,
        val phone: String?,
        val email: String?,
        val ordersCount: Int,
        val totalSpent: Int
    )
}
