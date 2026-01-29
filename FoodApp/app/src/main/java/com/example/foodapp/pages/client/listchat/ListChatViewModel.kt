package com.example.foodapp.pages.client.listchat


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.client.chat.ChatRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.remote.client.response.chat.ConversationApiModel
import kotlinx.coroutines.launch

sealed class ConversationsState {
    object Idle : ConversationsState()
    object Loading : ConversationsState()
    data class Success(
        val conversations: List<ConversationApiModel>,
        val hasMore: Boolean,
        val nextCursor: String?
    ) : ConversationsState()
    data class Error(val message: String) : ConversationsState()
}

data class ConversationsUiState(
    val conversations: List<ConversationApiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

class ConversationsViewModel(
    private val chatRepository: ChatRepository,
    private val context: Context
) : ViewModel() {

    private val authManager = AuthManager(context)

    // LiveData cho state
    private val _conversationsState = MutableLiveData<ConversationsState>(ConversationsState.Idle)
    val conversationsState: LiveData<ConversationsState> = _conversationsState

    // LiveData cho UI state
    private val _uiState = MutableLiveData<ConversationsUiState>(ConversationsUiState())
    val uiState: LiveData<ConversationsUiState> = _uiState

    // Data cache
    private var currentConversations = mutableListOf<ConversationApiModel>()
    private var currentCursor: String? = null
    private var currentHasMore = false

    init {
        loadConversations()
    }

    fun loadConversations(refresh: Boolean = false) {
        if (refresh) {
            currentConversations.clear()
            currentCursor = null
            currentHasMore = false
            _uiState.value = _uiState.value?.copy(isRefreshing = true)
        }

        viewModelScope.launch {
            // Chỉ hiển thị loading nếu là lần đầu load
            if (currentConversations.isEmpty()) {
                _conversationsState.value = ConversationsState.Loading
                _uiState.value = _uiState.value?.copy(isLoading = true)
            }

            val result = chatRepository.getConversations(
                token = getAuthToken(),
                cursor = currentCursor,
                limit = 20
            )

            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    data?.let {
                        // Thêm conversations mới vào cache
                        currentConversations.addAll(it.items)
                        currentHasMore = it.hasMore
                        currentCursor = it.nextCursor

                        // Update state
                        _conversationsState.value = ConversationsState.Success(
                            conversations = currentConversations.toList(),
                            hasMore = currentHasMore,
                            nextCursor = currentCursor
                        )

                        // Update UI state
                        _uiState.value = ConversationsUiState(
                            conversations = currentConversations.toList(),
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = currentHasMore,
                            nextCursor = currentCursor,
                            errorMessage = null,
                            isRefreshing = false
                        )
                    }
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Đã xảy ra lỗi"
                    _conversationsState.value = ConversationsState.Error(errorMessage)
                    _uiState.value = ConversationsUiState(
                        conversations = currentConversations.toList(),
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = currentHasMore,
                        nextCursor = currentCursor,
                        errorMessage = errorMessage,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun loadMoreConversations() {
        if (!currentHasMore || currentCursor == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoadingMore = true)

            val result = chatRepository.getConversations(
                token = getAuthToken(),
                cursor = currentCursor,
                limit = 20
            )

            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    data?.let {
                        // Thêm conversations mới vào cache
                        currentConversations.addAll(it.items)
                        currentHasMore = it.hasMore
                        currentCursor = it.nextCursor

                        // Update UI state
                        _uiState.value = ConversationsUiState(
                            conversations = currentConversations.toList(),
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = currentHasMore,
                            nextCursor = currentCursor,
                            errorMessage = null,
                            isRefreshing = false
                        )
                    }
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Đã xảy ra lỗi"
                    _uiState.value = _uiState.value?.copy(
                        isLoadingMore = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }

    fun refreshConversations() {
        loadConversations(refresh = true)
    }

    fun getConversationById(conversationId: String): ConversationApiModel? {
        return currentConversations.find { it.id == conversationId }
    }

    private fun getAuthToken(): String {
        val token = authManager.getCurrentToken()
        return token!!
    }

    companion object {
        fun factory(chatRepository: ChatRepository, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ConversationsViewModel::class.java)) {
                        return ConversationsViewModel(chatRepository,context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}