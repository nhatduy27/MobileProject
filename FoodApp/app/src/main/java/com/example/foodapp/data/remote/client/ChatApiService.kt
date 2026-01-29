package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.chat.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {

    @POST("chat/conversations")
    suspend fun createOrGetConversation(
        @Header("Authorization") token: String,
        @Body request: CreateConversationRequest
    ): Response<ConversationResponse>


    @GET("chat/conversations")
    suspend fun listConversations(
        @Header("Authorization") token: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ListConversationsResponse>


    @GET("chat/conversations/{conversationId}/messages")
    suspend fun listMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("startAfter") startAfter: String? = null
    ): Response<ListMessagesResponse>

    @POST("chat/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

}