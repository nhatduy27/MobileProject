package com.example.foodapp.data.model.client

import com.google.gson.annotations.SerializedName
import okhttp3.Address

data class DeliveryAddress(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("label")
    val label: String = "",

    @SerializedName("fullAddress")
    val fullAddress: String = "",

    @SerializedName("isDefault")
    val isDefault: Boolean = false,

    var clientId: String = "",
    var name: String = "",
    var phone: String = "",
    var note: String = "",
    var address: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    // Helper property để lấy address
    val getAddress: String
        get() = fullAddress
}