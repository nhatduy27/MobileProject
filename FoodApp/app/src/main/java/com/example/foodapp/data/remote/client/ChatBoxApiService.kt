package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.chatbox.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ChatBoxApiService {

    /**
     * Gửi tin nhắn đến Chatbot
     * POST /api/chatbot/message
     * Headers:
     * - Authorization: Bearer <firebase_id_token>
     * - Content-Type: application/json
     * Body: ChatbotMessageRequest
     * Response: ChatbotMessageResponse
     */
    @POST("chatbot/message")
    suspend fun sendMessageToChatbot(
        @Header("Authorization") authorization: String,
        @Body request: ChatbotMessageRequest
    ): Response<ChatbotMessageResponse>


    @POST("api/chatbot/quick-replies")
    suspend fun getQuickReplies(): Response<QuickRepliesResponse>

}