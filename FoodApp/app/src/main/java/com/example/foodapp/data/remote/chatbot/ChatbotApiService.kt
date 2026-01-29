package com.example.foodapp.data.remote.chatbot

import com.example.foodapp.data.model.chatbot.ChatMessageRequest
import com.example.foodapp.data.model.chatbot.ChatbotMessageResponse
import com.example.foodapp.data.model.chatbot.QuickRepliesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Service for Chatbot
 */
interface ChatbotApiService {
    
    /**
     * POST /chatbot/message
     * Send a message to the AI chatbot
     */
    @POST("chatbot/message")
    suspend fun sendMessage(
        @Body request: ChatMessageRequest
    ): Response<ChatbotMessageResponse>
    
    /**
     * POST /chatbot/quick-replies
     * Get quick reply suggestions
     */
    @POST("chatbot/quick-replies")
    suspend fun getQuickReplies(): Response<QuickRepliesResponse>
}
