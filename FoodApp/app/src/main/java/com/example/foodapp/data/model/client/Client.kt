package com.example.foodapp.data.model

import com.example.foodapp.data.model.client.profile.UserProfileData
import com.example.foodapp.data.model.client.DeliveryAddress
import java.text.SimpleDateFormat
import java.util.Locale

data class Client(
    var id: String = "",
    var fullName: String = "",
    var email: String = "",
    var isVerify: Boolean = false,
    var phone: String = "",
    var role: String = "user",
    var imageAvatar: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var addresses: List<DeliveryAddress> = emptyList()
) {

    companion object {
        private val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        /**
         * Convert từ API response sang Client
         * Trả về Client với giá trị mặc định nếu có lỗi
         */
        fun fromApiResponse(apiData: UserProfileData?): Client {
            return try {
                if (apiData == null || apiData.id.isNullOrEmpty()) {
                    return getDefaultClient()
                }

                Client(
                    id = apiData.id,
                    fullName = apiData.displayName ?: "",
                    email = apiData.email ?: "",
                    phone = apiData.phone ?: "",
                    role = apiData.role ?: "user",
                    isVerify = when (apiData.status?.uppercase()) {
                        "ACTIVE", "VERIFIED" -> true
                        else -> false
                    },
                    imageAvatar = apiData.avatarUrl ?: "",
                    // SỬA Ở ĐÂY: Parse createdAt từ API string sang timestamp
                    createdAt = parseDateStringToTimestamp(apiData.createdAt),
                    // Xử lý addresses an toàn

                    addresses = apiData.addresses?.mapNotNull { address ->
                        address?.let {
                            DeliveryAddress(
                                id = it.id ?: "",
                                name = it.label ?: "Địa chỉ",
                                address = it.fullAddress ?: "",
                                isDefault = it.isDefault ?: false,
                                clientId = apiData.id,
                                phone = apiData.phone ?: "",
                                note = ""
                            )
                        }
                    } ?: emptyList()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                getDefaultClient()
            }
        }



        /**
         * Chuyển đổi date string từ API sang timestamp
         * Ví dụ: "2026-01-05T10:00:00Z" -> 1641362400000
         */
        private fun parseDateStringToTimestamp(dateString: String?): Long {
            return try {
                dateString?.let {
                    dateParser.parse(it)?.time ?: System.currentTimeMillis()
                } ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }

        private fun getDefaultClient(): Client {
            return Client(
                id = "",
                fullName = "",
                email = "",
                phone = "",
                role = "user",
                imageAvatar = "",
                createdAt = System.currentTimeMillis(),
                addresses = emptyList()
            )
        }
    }

    /**
     * Hàm tiện ích: format createdAt thành string hiển thị
     */
    fun getFormattedCreatedAt(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(createdAt)
    }
}