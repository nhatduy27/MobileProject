package com.example.foodapp.data.model.chat

import com.google.gson.annotations.SerializedName

/**
 * Message status enum
 */
enum class MessageStatus {
    @SerializedName("PENDING")
    PENDING,
    @SerializedName("SENT")
    SENT,
    @SerializedName("READ")
    READ
}

/**
 * Chat message entity
 */
data class ChatMessage(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("senderId")
    val senderId: String = "",
    
    @SerializedName("text")
    val text: String = "",
    
    @SerializedName("status")
    val status: MessageStatus = MessageStatus.SENT,
    
    @SerializedName("readAt")
    val readAt: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Other participant info
 */
data class OtherParticipant(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("displayName")
    val displayName: String = "",
    
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    
    @SerializedName("role")
    val role: String = "",
    
    @SerializedName("shopName")
    val shopName: String? = null,
    
    @SerializedName("shopId")
    val shopId: String? = null
)

/**
 * Conversation entity
 */
data class Conversation(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("participants")
    val participants: List<String> = emptyList(),
    
    @SerializedName("lastMessage")
    val lastMessage: String = "",
    
    @SerializedName("lastMessageAt")
    val lastMessageAt: String = "",
    
    @SerializedName("lastSenderId")
    val lastSenderId: String = "",
    
    @SerializedName("createdAt")
    val createdAt: String = "",
    
    @SerializedName("updatedAt")
    val updatedAt: String = "",
    
    @SerializedName("otherParticipant")
    val otherParticipant: OtherParticipant? = null
)

// ==================== REQUEST DTOs ====================

/**
 * Create conversation request
 */
data class CreateConversationRequest(
    @SerializedName("participantId")
    val participantId: String
)

/**
 * Send message request
 */
data class SendMessageRequest(
    @SerializedName("conversationId")
    val conversationId: String,
    
    @SerializedName("text")
    val text: String
)

/**
 * Mark as read request
 */
data class MarkAsReadRequest(
    @SerializedName("conversationId")
    val conversationId: String
)

// ==================== RESPONSE DTOs ====================

/**
 * Paginated list response
 */
data class PaginatedConversationsResponse(
    @SerializedName("items")
    val items: List<Conversation> = emptyList(),
    
    @SerializedName("hasMore")
    val hasMore: Boolean = false,
    
    @SerializedName("nextCursor")
    val nextCursor: String? = null
)

data class PaginatedMessagesResponse(
    @SerializedName("items")
    val items: List<ChatMessage> = emptyList(),
    
    @SerializedName("hasMore")
    val hasMore: Boolean = false,
    
    @SerializedName("nextCursor")
    val nextCursor: String? = null
)

/**
 * Wrapped responses for API
 */
data class WrappedConversationResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: Conversation? = null
)

data class WrappedConversationsListResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: PaginatedConversationsResponse? = null
)

data class WrappedMessagesListResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: PaginatedMessagesResponse? = null
)

data class WrappedMessageResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: ChatMessage? = null
)
