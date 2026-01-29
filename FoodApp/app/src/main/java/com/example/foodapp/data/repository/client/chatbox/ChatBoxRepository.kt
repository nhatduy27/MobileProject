package com.example.foodapp.data.repository.client.chatbox

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.chatbox.*

class ChatBoxRepository() {

    private val chatBoxApiService = ApiClient.chatBoxApiService

    /**
     * Gửi tin nhắn đến Chatbot
     * @param message Nội dung tin nhắn gửi đến chatbot
     * @param firebaseIdToken Firebase ID Token để xác thực
     * @return ApiResult<ChatbotMessageResponse> Kết quả trả về
     */
    suspend fun sendMessageToChatbot(
        message: String,
        firebaseIdToken: String
    ): ApiResult<ChatbotMessageResponse> {
        return try {
            // Tạo request DTO
            val request = ChatbotMessageRequest(message = message)

            // Gọi API
            val response = chatBoxApiService.sendMessageToChatbot(
                authorization = "Bearer $firebaseIdToken",
                request = request
            )

            // Kiểm tra response
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                ApiResult.Failure(Exception("API call failed: ${response.code()} - ${response.message()}\n$errorBody"))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }


    /**
     * Lấy danh sách quick replies (câu hỏi gợi ý)
     * @return ApiResult<QuickRepliesResponse> Kết quả trả về
     */
    suspend fun getQuickReplies(): ApiResult<QuickRepliesResponse> {
        return try {
            // Gọi API (public endpoint, không cần authorization)
            val response = chatBoxApiService.getQuickReplies()

            // Kiểm tra response
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                ApiResult.Failure(
                    Exception("API call failed: ${response.code()} - ${response.message()}\n$errorBody")
                )
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

}