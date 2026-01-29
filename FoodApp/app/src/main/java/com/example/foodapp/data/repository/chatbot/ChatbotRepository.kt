package com.example.foodapp.data.repository.chatbot

import android.util.Log
import com.example.foodapp.data.model.chatbot.*
import com.example.foodapp.data.remote.chatbot.ChatbotApiService
import org.json.JSONObject
import retrofit2.Response

/**
 * Repository for Chatbot operations
 */
class ChatbotRepository(
    private val apiService: ChatbotApiService
) {
    companion object {
        private const val TAG = "ChatbotRepository"
    }
    
    /**
     * Send a message to chatbot and get response
     */
    suspend fun sendMessage(message: String): Result<ChatResponseData> {
        return try {
            Log.d(TAG, "🔄 Sending message: $message")
            
            val request = ChatMessageRequest(message)
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                
                // Try to extract answer from response
                val answerData = when {
                    body?.data != null -> body.data
                    body?.answer != null -> ChatResponseData(
                        answer = body.answer,
                        confidence = body.confidence ?: "high"
                    )
                    else -> null
                }
                
                if (answerData != null) {
                    Log.d(TAG, "✅ Got response: ${answerData.answer.take(50)}...")
                    Result.success(answerData)
                } else {
                    Result.failure(Exception("Empty response from chatbot"))
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
     * Get quick reply suggestions
     */
    suspend fun getQuickReplies(): Result<List<String>> {
        return try {
            Log.d(TAG, "🔄 Fetching quick replies")
            
            val response = apiService.getQuickReplies()
            
            if (response.isSuccessful) {
                val body = response.body()
                
                // Try to extract quick replies
                val replies = body?.data?.quickReplies 
                    ?: body?.quickReplies 
                    ?: emptyList()
                
                Log.d(TAG, "✅ Got ${replies.size} quick replies")
                Result.success(replies)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching quick replies: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching quick replies", e)
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
