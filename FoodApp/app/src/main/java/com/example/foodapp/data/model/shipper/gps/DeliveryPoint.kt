package com.example.foodapp.data.model.shipper.gps

import com.google.gson.annotations.SerializedName

/**
 * Delivery Point - Điểm giao hàng (tòa nhà KTX)
 */
data class DeliveryPoint(
    val id: String = "",
    val buildingCode: String = "",
    val name: String = "",
    val location: TripLocation = TripLocation(),
    val note: String? = null,
    val active: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

