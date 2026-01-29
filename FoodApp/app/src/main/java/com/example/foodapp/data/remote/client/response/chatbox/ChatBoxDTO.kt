package com.example.foodapp.data.remote.client.response.chatbox

import com.google.gson.annotations.SerializedName


sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}
/**
 * Request DTO cho API gửi tin nhắn đến Chatbot
 * Format request:
 * POST /api/chatbot/message
 * Headers:
 * - Authorization: Bearer <firebase_id_token>
 * - Content-Type: application/json
 * Body:
 * - message: Nội dung tin nhắn gửi đến chatbot
 */
data class ChatbotMessageRequest(
    @SerializedName("message")
    val message: String
)

/**
 * Response model cho API chatbot message (rate limit case)
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "answer": "Bạn đang gửi tin nhắn quá nhanh. Vui lòng đợi 15 giây trước khi gửi tiếp.",
 *     "confidence": "low",
 *     "rateLimited": true,
 *     "waitTime": 15
 *   }
 * }
 */
data class ChatbotMessageApiModel @JvmOverloads constructor(
    @SerializedName("answer")
    val answer: String = "",

    @SerializedName("confidence")
    val confidence: String = "",

    @SerializedName("rateLimited")
    val rateLimited: Boolean = false,

    @SerializedName("waitTime")
    val waitTime: Int = 0
)

/**
 * Response model wrapper cho API chatbot message
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "answer": "Bạn đang gửi tin nhắn quá nhanh. Vui lòng đợi 15 giây trước khi gửi tiếp.",
 *     "confidence": "low",
 *     "rateLimited": true,
 *     "waitTime": 15
 *   }
 * }
 */
data class ChatbotMessageResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ChatbotMessageApiModel? = null
)


/**
 * Response model cho API lấy quick replies
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "quickReplies": [
 *       "Làm sao để hủy đơn hàng?",
 *       "Thời gian giao hàng là bao lâu?",
 *       "Phí ship được tính như thế nào?",
 *       "Làm sao để theo dõi đơn hàng?",
 *       "Thanh toán online có an toàn không?",
 *       "Tôi muốn đăng ký làm shipper",
 *       "Cách sử dụng mã giảm giá?"
 *     ]
 *   }
 * }
 */
data class QuickRepliesApiModel @JvmOverloads constructor(
    @SerializedName("quickReplies")
    val quickReplies: List<String> = emptyList()
)

/**
 * Response model wrapper cho API quick replies
 */
data class QuickRepliesResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: QuickRepliesApiModel? = null
)