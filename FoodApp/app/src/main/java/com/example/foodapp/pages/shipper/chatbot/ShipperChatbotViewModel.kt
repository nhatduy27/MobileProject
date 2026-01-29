package com.example.foodapp.pages.shipper.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.chatbot.ChatConfidence
import com.example.foodapp.data.model.chatbot.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Chatbot Shipper
 */
class ShipperChatbotViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "ShipperChatbotVM"
    }
    
    private val repository = RepositoryProvider.getChatbotRepository()
    
    private val _uiState = MutableStateFlow(ShipperChatbotUiState())
    val uiState: StateFlow<ShipperChatbotUiState> = _uiState.asStateFlow()
    
    init {
        loadQuickReplies()
        // Add welcome message
        addBotMessage("Xin chào! Tôi là trợ lý AI của KTX Delivery. Tôi có thể giúp gì cho bạn về công việc giao hàng?")
    }
    
    // ==================== QUICK REPLIES ====================
    
    private fun loadQuickReplies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingQuickReplies = true) }
            
            val result = repository.getQuickReplies()
            
            result.onSuccess { replies ->
                Log.d(TAG, "✅ Loaded ${replies.size} quick replies")
                _uiState.update {
                    it.copy(
                        quickReplies = replies,
                        isLoadingQuickReplies = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load quick replies", error)
                _uiState.update { it.copy(isLoadingQuickReplies = false) }
            }
        }
    }
    
    // ==================== MESSAGING ====================
    
    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage(message: String? = null) {
        val textToSend = message ?: _uiState.value.inputText.trim()
        
        if (textToSend.isBlank()) return
        
        // Clear input if using from text field
        if (message == null) {
            _uiState.update { it.copy(inputText = "") }
        }
        
        // Add user message
        val userMessage = ChatMessage(
            content = textToSend,
            isFromUser = true
        )
        
        // Add loading message from bot
        val loadingMessage = ChatMessage(
            id = "loading_${System.currentTimeMillis()}",
            content = "",
            isFromUser = false,
            isLoading = true
        )
        
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + loadingMessage,
                isSending = true
            )
        }
        
        // Send to API
        viewModelScope.launch {
            val result = repository.sendMessage(textToSend)
            
            result.onSuccess { response ->
                Log.d(TAG, "✅ Got response: ${response.answer.take(50)}...")
                
                // Remove loading message and add actual response
                val botMessage = ChatMessage(
                    content = response.answer,
                    isFromUser = false,
                    confidence = when (response.confidence.lowercase()) {
                        "high" -> ChatConfidence.HIGH
                        "medium" -> ChatConfidence.MEDIUM
                        else -> ChatConfidence.LOW
                    }
                )
                
                _uiState.update {
                    it.copy(
                        messages = it.messages.filter { msg -> !msg.isLoading } + botMessage,
                        isSending = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to send message", error)
                
                // Remove loading message and add error message
                val errorMessage = ChatMessage(
                    content = "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại.",
                    isFromUser = false,
                    isError = true
                )
                
                _uiState.update {
                    it.copy(
                        messages = it.messages.filter { msg -> !msg.isLoading } + errorMessage,
                        isSending = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }
    
    fun selectQuickReply(reply: String) {
        sendMessage(reply)
    }
    
    private fun addBotMessage(content: String) {
        val message = ChatMessage(
            content = content,
            isFromUser = false
        )
        _uiState.update {
            it.copy(messages = it.messages + message)
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearConversation() {
        _uiState.update {
            it.copy(
                messages = emptyList(),
                inputText = ""
            )
        }
        // Add welcome message again
        addBotMessage("Xin chào! Tôi là trợ lý AI của KTX Delivery. Tôi có thể giúp gì cho bạn về công việc giao hàng?")
    }
}
