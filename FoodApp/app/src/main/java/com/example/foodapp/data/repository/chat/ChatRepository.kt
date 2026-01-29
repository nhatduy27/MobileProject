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
            Log.d(TAG, "🔄 Creating conversation with: $participantId")
            
            val request = CreateConversationRequest(participantId)
            val response = apiService.createConversation(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                val conversation = body?.data
                if (conversation != null) {
                    Log.d(TAG, "✅ Created conversation: ${conversation.id}")
                    Result.success(conversation)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error creating conversation: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception creating conversation", e)
            Result.failure(e)
        }
    }
    
    /**
     * List my conversations
     */
    suspend fun listConversations(limit: Int? = null, cursor: String? = null): Result<PaginatedConversationsResponse> {
        return try {
            Log.d(TAG, "🔄 Loading conversations... limit=$limit, cursor=$cursor")
            Log.d(TAG, "🔄 Calling API: GET chat/conversations")
            
            val response = apiService.listConversations(limit, cursor)
            
            Log.d(TAG, "🔄 API Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (data != null) {
                    Log.d(TAG, "✅ Loaded ${data.items.size} conversations")
                    Result.success(data)
                } else {
                    Log.d(TAG, "⚠️ Response body is null, returning empty list")
                    Result.success(PaginatedConversationsResponse())
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error loading conversations: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading conversations", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get conversation by ID
     */
    suspend fun getConversation(conversationId: String): Result<Conversation> {
        return try {
            Log.d(TAG, "🔄 Loading conversation: $conversationId")
            
            val response = apiService.getConversation(conversationId)
            
            if (response.isSuccessful) {
                val body = response.body()
                val conversation = body?.data
                if (conversation != null) {
                    Log.d(TAG, "✅ Loaded conversation: ${conversation.id}")
                    Result.success(conversation)
                } else {
                    Result.failure(Exception("Conversation not found"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error loading conversation: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading conversation", e)
            Result.failure(e)
        }
    }
    
    // ==================== MESSAGES ====================
    
    /**
     * List messages in conversation
     */
    suspend fun listMessages(conversationId: String, limit: Int? = null, cursor: String? = null): Result<PaginatedMessagesResponse> {
        return try {
            Log.d(TAG, "🔄 Loading messages for: $conversationId")
            
            val response = apiService.listMessages(conversationId, limit, cursor)
            
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (data != null) {
                    Log.d(TAG, "✅ Loaded ${data.items.size} messages")
                    Result.success(data)
                } else {
                    Result.success(PaginatedMessagesResponse())
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error loading messages: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading messages", e)
            Result.failure(e)
        }
    }
    
    /**
     * Send message
     */
    suspend fun sendMessage(conversationId: String, text: String): Result<ChatMessage> {
        return try {
            Log.d(TAG, "🔄 Sending message to: $conversationId")
            
            val request = SendMessageRequest(conversationId, text)
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.data
                if (message != null) {
                    Log.d(TAG, "✅ Sent message: ${message.id}")
                    Result.success(message)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error sending message: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception sending message", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark message as read
     */
    suspend fun markAsRead(messageId: String, conversationId: String): Result<ChatMessage> {
        return try {
            Log.d(TAG, "🔄 Marking message as read: $messageId")
            
            val request = MarkAsReadRequest(conversationId)
            val response = apiService.markAsRead(messageId, request)
            
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.data
                if (message != null) {
                    Log.d(TAG, "✅ Marked as read: ${message.id}")
                    Result.success(message)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error marking as read: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception marking as read", e)
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
