package com.example.foodapp.data.repository.client.chat

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.chat.*
import retrofit2.Response

class ChatRepository() {

    private val chatApiService = ApiClient.chatApiService

    suspend fun createOrGetConversation(
        token: String,
        participantId: String
    ): Response<ConversationResponse> {
        val request = CreateConversationRequest(participantId = participantId)
        return chatApiService.createOrGetConversation("Bearer $token", request)
    }

    suspend fun getConversations(
        token : String,
        cursor: String? = null,
        limit: Int = 20
    ): Result<ListConversationsApiModel> {
        return try {
            val response = chatApiService.listConversations(
                token = "Bearer $token",
                cursor = cursor,
                limit = limit
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Data is null"))
                }
            } else {
                Result.failure(Exception("Failed to get conversations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getMessages(
        token: String,
        conversationId: String,
        limit: Int = 50,
        startAfter: String? = null
    ): Result<ListMessagesApiModel> {
        return try {
            val response = chatApiService.listMessages(
                token = "Bearer $token",
                conversationId = conversationId,
                limit = limit,
                startAfter = startAfter
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Data is null"))
                }
            } else {
                Result.failure(Exception("Failed to get messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        token: String,
        conversationId: String,
        text: String
    ): Result<MessageApiModel> {
        return try {
            val request = SendMessageRequest(
                conversationId = conversationId,
                text = text
            )

            val response = chatApiService.sendMessage(
                token = "Bearer $token",
                request = request
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Data is null"))
                }
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}