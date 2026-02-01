package com.example.foodapp.data.repository.chat

import android.util.Log
import com.example.foodapp.data.model.chat.*
import com.example.foodapp.data.remote.chat.ChatApiService
import org.json.JSONObject
import retrofit2.Response

/**
 * Repository for Chat operations
 */
class ChatRepository(
    private val apiService: ChatApiService
) {
    companion object {
        private const val TAG = "ChatRepository"
    }
    
    // ==================== CONVERSATIONS ====================
    
    /**
     * Create or get conversation with another user
     */
    suspend fun createConversation(participantId: String): Result<Conversation> {
        return try {
            val request = CreateConversationRequest(participantId)
            val response = apiService.createConversation(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                val conversation = body?.data
                if (conversation != null) {
                    Result.success(conversation)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * List my conversations
     */
    suspend fun listConversations(limit: Int? = null, cursor: String? = null): Result<PaginatedConversationsResponse> {
        return try {
            val response = apiService.listConversations(limit, cursor)

            
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.success(PaginatedConversationsResponse())
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get conversation by ID
     */
    suspend fun getConversation(conversationId: String): Result<Conversation> {
        return try {

            val response = apiService.getConversation(conversationId)
            
            if (response.isSuccessful) {
                val body = response.body()
                val conversation = body?.data
                if (conversation != null) {
                    Result.success(conversation)
                } else {
                    Result.failure(Exception("Conversation not found"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== MESSAGES ====================
    
    /**
     * List messages in conversation
     */
    suspend fun listMessages(conversationId: String, limit: Int? = null, cursor: String? = null): Result<PaginatedMessagesResponse> {
        return try {
            
            val response = apiService.listMessages(conversationId, limit, cursor)
            
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.success(PaginatedMessagesResponse())
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send message
     */
    suspend fun sendMessage(conversationId: String, text: String): Result<ChatMessage> {
        return try {
            
            val request = SendMessageRequest(conversationId, text)
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.data
                if (message != null) {
                    Result.success(message)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark message as read
     */
    suspend fun markAsRead(messageId: String, conversationId: String): Result<ChatMessage> {
        return try {
            
            val request = MarkAsReadRequest(conversationId)
            val response = apiService.markAsRead(messageId, request)
            
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.data
                if (message != null) {
                    Result.success(message)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun <T> parseErrorBody(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "Error: ${response.code()}")
            } else {
                "Error: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            "Error: ${response.code()} ${response.message()}"
        }
    }
}
