package com.example.foodapp.data.remote.chat

import com.example.foodapp.data.model.chat.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Chat
 */
interface ChatApiService {
    
    // ==================== CONVERSATIONS ====================
    
    /**
     * POST /chat/conversations
     * Create or get conversation with another user
     */
    @POST("chat/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<Conversation>
    
    /**
     * GET /chat/conversations
     * List my conversations
     */
    @GET("chat/conversations")
    suspend fun listConversations(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<PaginatedConversationsResponse>
    
    /**
     * GET /chat/conversations/{id}
     * Get conversation by ID
     */
    @GET("chat/conversations/{id}")
    suspend fun getConversation(
        @Path("id") conversationId: String
    ): Response<Conversation>
    
    // ==================== MESSAGES ====================
    
    /**
     * GET /chat/conversations/{id}/messages
     * List messages in conversation
     */
    @GET("chat/conversations/{id}/messages")
    suspend fun listMessages(
        @Path("id") conversationId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<PaginatedMessagesResponse>
    
    /**
     * POST /chat/messages
     * Send message
     */
    @POST("chat/messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ChatMessage>
    
    /**
     * PUT /chat/messages/{id}/read
     * Mark message as read
     */
    @PUT("chat/messages/{id}/read")
    suspend fun markAsRead(
        @Path("id") messageId: String,
        @Body request: MarkAsReadRequest
    ): Response<ChatMessage>
}
