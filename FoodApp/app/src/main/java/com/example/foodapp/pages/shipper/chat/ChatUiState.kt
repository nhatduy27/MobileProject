package com.example.foodapp.pages.shipper.chat

import com.example.foodapp.data.model.chat.ChatMessage
import com.example.foodapp.data.model.chat.Conversation

/**
 * UI State cho danh sách conversation
 */
data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hasMore: Boolean = false,
    val nextCursor: String? = null
)

/**
 * UI State cho màn hình chat detail
 */
data class ChatDetailUiState(
    val conversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val currentUserId: String = ""
)
