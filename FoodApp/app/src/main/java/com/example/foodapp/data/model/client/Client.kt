package com.example.foodapp.data.model.client

import com.example.foodapp.data.remote.client.response.profile.UserProfileData
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
         * Convert từ GoogleAuthResponse sang Client
         * Dùng ngay sau khi đăng nhập Google
         */
        fun fromGoogleAuth(
            userId: String,
            email: String,
            displayName: String?,
            photoUrl: String?,
            role: String?,
            emailVerified: Boolean
        ): Client {
            return Client(
                id = userId,
                fullName = displayName ?: "",
                email = email,
                isVerify = emailVerified,
                role = role ?: "user",
                imageAvatar = photoUrl ?: "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                addresses = emptyList()
            )
        }


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
                                // ID và thông tin cơ bản
                                id = it.id ?: "",
                                label = it.label ?: "Địa chỉ",
                                fullAddress = it.fullAddress ?: "",
                                clientId = apiData.id,
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