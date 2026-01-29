package com.example.foodapp.data.model.shipper.gps

import com.google.gson.annotations.SerializedName

/**
 * Firestore Timestamp format
 */
data class FirestoreTimestamp(
    @SerializedName("_seconds")
    val seconds: Long = 0,
    @SerializedName("_nanoseconds")
    val nanoseconds: Long = 0
)

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
    val createdAt: FirestoreTimestamp? = null,
    val updatedAt: FirestoreTimestamp? = null
)

