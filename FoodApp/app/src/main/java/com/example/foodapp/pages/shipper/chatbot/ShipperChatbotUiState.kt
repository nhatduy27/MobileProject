package com.example.foodapp.pages.shipper.chatbot

import com.example.foodapp.data.model.chatbot.ChatMessage

/**
 * UI State cho màn hình Chatbot Shipper
 */
data class ShipperChatbotUiState(
    // Conversation messages
    val messages: List<ChatMessage> = emptyList(),
    
    // Quick replies
    val quickReplies: List<String> = emptyList(),
    
    // Current input
    val inputText: String = "",
    
    // Loading states
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isLoadingQuickReplies: Boolean = false,
    
    // Error message
    val errorMessage: String? = null
)
