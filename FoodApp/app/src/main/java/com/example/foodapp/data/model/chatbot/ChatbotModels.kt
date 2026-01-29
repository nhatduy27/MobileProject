package com.example.foodapp.data.model.chatbot

import com.google.gson.annotations.SerializedName

/**
 * Confidence level of chatbot response
 */
enum class ChatConfidence {
    @SerializedName("high")
    HIGH,
    @SerializedName("medium")
    MEDIUM,
    @SerializedName("low")
    LOW
}

/**
 * Single chat message in conversation
 */
data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: ChatConfidence? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

/**
 * Response data from chatbot API
 */
data class ChatResponseData(
    @SerializedName("answer")
    val answer: String = "",
    
    @SerializedName("confidence")
    val confidence: String = "high",
    
    @SerializedName("sources")
    val sources: List<String>? = null,
    
    @SerializedName("rateLimited")
    val rateLimited: Boolean = false,
    
    @SerializedName("waitTime")
    val waitTime: Int? = null
)

/**
 * Wrapped response from chatbot message API
 */
data class ChatbotMessageResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: ChatResponseData? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    // Direct fields (wenn data is null)
    @SerializedName("answer")
    val answer: String? = null,
    
    @SerializedName("confidence")
    val confidence: String? = null
)

/**
 * Quick replies data
 */
data class QuickRepliesData(
    @SerializedName("quickReplies")
    val quickReplies: List<String> = emptyList()
)

/**
 * Wrapped response for quick replies API
 */
data class QuickRepliesResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: QuickRepliesData? = null,
    
    // Direct field (if data is null)
    @SerializedName("quickReplies")
    val quickReplies: List<String>? = null
)

/**
 * Request body for sending message
 */
data class ChatMessageRequest(
    @SerializedName("message")
    val message: String
)
