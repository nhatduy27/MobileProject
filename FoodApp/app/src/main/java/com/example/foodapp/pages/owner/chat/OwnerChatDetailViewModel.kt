package com.example.foodapp.pages.owner.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.chat.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình chat detail - Owner
 */
class OwnerChatDetailViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "OwnerChatDetailVM"
    }
    
    private val repository = RepositoryProvider.getChatRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    private val _uiState = MutableStateFlow(OwnerChatDetailUiState(currentUserId = currentUserId))
    val uiState: StateFlow<OwnerChatDetailUiState> = _uiState.asStateFlow()
    
    private var conversationId: String = ""
    
    fun setConversationId(id: String) {
        conversationId = id
        loadConversation()
        loadMessages()
    }
    
    private fun loadConversation() {
        viewModelScope.launch {
            val result = repository.getConversation(conversationId)
            result.onSuccess { conversation ->
                _uiState.update { it.copy(conversation = conversation) }
            }
        }
    }
    
    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = repository.listMessages(conversationId, limit = 30)
            
            result.onSuccess { response ->
                Log.d(TAG, "✅ Loaded ${response.items.size} messages")
                // Reverse to show oldest first at top
                _uiState.update {
                    it.copy(
                        messages = response.items.reversed(),
                        isLoading = false,
                        hasMore = response.hasMore,
                        nextCursor = response.nextCursor
                    )
                }
                
                // Mark unread messages as read
                markUnreadMessagesAsRead(response.items)
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load messages", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }
    
    fun loadMoreMessages() {
        val cursor = _uiState.value.nextCursor ?: return
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            val result = repository.listMessages(conversationId, limit = 30, cursor = cursor)
            
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        // Prepend older messages at the beginning
                        messages = response.items.reversed() + it.messages,
                        isLoadingMore = false,
                        hasMore = response.hasMore,
                        nextCursor = response.nextCursor
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }
    
    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSending) return
        
        _uiState.update { it.copy(inputText = "", isSending = true) }
        
        // Add optimistic message
        val optimisticMessage = ChatMessage(
            id = "temp_${System.currentTimeMillis()}",
            senderId = currentUserId,
            text = text,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date())
        )
        
        _uiState.update {
            it.copy(messages = it.messages + optimisticMessage)
        }
        
        viewModelScope.launch {
            val result = repository.sendMessage(conversationId, text)
            
            result.onSuccess { sentMessage ->
                Log.d(TAG, "✅ Message sent: ${sentMessage.id}")
                // Replace optimistic message with real one
                _uiState.update {
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == optimisticMessage.id) sentMessage else msg
                        },
                        isSending = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to send message", error)
                // Remove optimistic message on failure
                _uiState.update {
                    it.copy(
                        messages = it.messages.filter { msg -> msg.id != optimisticMessage.id },
                        isSending = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }
    
    private fun markUnreadMessagesAsRead(messages: List<ChatMessage>) {
        viewModelScope.launch {
            messages
                .filter { it.senderId != currentUserId && it.status != com.example.foodapp.data.model.chat.MessageStatus.READ }
                .forEach { message ->
                    repository.markAsRead(message.id, conversationId)
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
