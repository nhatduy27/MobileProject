package com.example.foodapp.data.remote.shipper.response

import com.google.gson.annotations.SerializedName

/**
 * Response for GET shipper online status API
 */
data class GetOnlineStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: OnlineStatusData
)

data class OnlineStatusData(
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("topic") val topic: String?
)

/**
 * Response for shipper go online API
 */
data class GoOnlineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: OnlineData
)

data class OnlineData(
    @SerializedName("subscribedCount") val subscribedCount: Int,
    @SerializedName("topic") val topic: String,
    @SerializedName("isOnline") val isOnline: Boolean
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
    @SerializedName("topic") val topic: String,
    @SerializedName("isOnline") val isOnline: Boolean
)
