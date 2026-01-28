package com.example.foodapp.data.remote.shipper.response

import com.google.gson.annotations.SerializedName

/**
 * Response for shipper go online API
 */
data class GoOnlineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: OnlineData
)

data class OnlineData(
    @SerializedName("subscribedCount") val subscribedCount: Int,
    @SerializedName("topic") val topic: String
)

/**
 * Response for shipper go offline API
 */
data class GoOfflineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: OfflineData
)

data class OfflineData(
    @SerializedName("unsubscribedCount") val unsubscribedCount: Int,
    @SerializedName("topic") val topic: String
)
