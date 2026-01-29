package com.example.foodapp.pages.shipper.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.chat.Conversation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho danh sách conversations
 */
class ConversationsViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "ConversationsVM"
    }
    
    private val repository = RepositoryProvider.getChatRepository()
    
    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()
    
    init {
        loadConversations()
    }
    
    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = repository.listConversations(limit = 20)
            
            result.onSuccess { response ->
                Log.d(TAG, "✅ Loaded ${response.items.size} conversations")
                _uiState.update {
                    it.copy(
                        conversations = response.items,
                        isLoading = false,
                        hasMore = response.hasMore,
                        nextCursor = response.nextCursor
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load conversations", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            val result = repository.listConversations(limit = 20)
            
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        conversations = response.items,
                        isRefreshing = false,
                        hasMore = response.hasMore,
                        nextCursor = response.nextCursor
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }
    
    fun loadMore() {
        val cursor = _uiState.value.nextCursor ?: return
        if (!_uiState.value.hasMore) return
        
        viewModelScope.launch {
            val result = repository.listConversations(limit = 20, cursor = cursor)
            
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        conversations = it.conversations + response.items,
                        hasMore = response.hasMore,
                        nextCursor = response.nextCursor
                    )
                }
            }
        }
    }
    
    suspend fun createConversation(participantId: String): Result<Conversation> {
        return repository.createConversation(participantId)
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
