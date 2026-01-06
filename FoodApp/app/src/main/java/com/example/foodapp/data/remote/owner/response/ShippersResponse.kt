package com.example.foodapp.data.remote.owner.response

// Minimal ShippersResponse DTO to satisfy OwnerApiService import.
// Extend fields and add mappers to domain models as needed.

data class ShippersResponse(
    val shippers: List<ShipperDto>
) {
    data class ShipperDto(
        val id: String,
        val name: String,
        val phone: String?,
        val vehicle: String?,
        val rating: Float,
        val active: Boolean
    )
}
