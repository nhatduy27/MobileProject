package com.example.foodapp.pages.client.chat

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.client.chat.ChatRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.remote.client.response.chat.MessageApiModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Success(val messages: List<MessageApiModel>) : ChatState()
    data class Error(val message: String) : ChatState()
}

data class ChatUiState(
    val messages: List<MessageApiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val errorMessage: String? = null,
    val isSending: Boolean = false
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val context: Context,
    val conversationId: String
) : ViewModel() {

    private val authManager = AuthManager(context)

    // LiveData cho state
    private val _messagesState = MutableLiveData<ChatState>(ChatState.Idle)
    val messagesState: LiveData<ChatState> = _messagesState

    // LiveData cho UI state
    private val _uiState = MutableLiveData<ChatUiState>(ChatUiState())
    val uiState: LiveData<ChatUiState> = _uiState

    private var currentMessages = mutableListOf<MessageApiModel>()
    private var currentCursor: String? = null
    private var currentHasMore = false

    init {
        loadMessages()
    }

    fun loadMessages(refresh: Boolean = false) {
        if (refresh) {
            currentMessages.clear()
            currentCursor = null
            currentHasMore = false
        }

        viewModelScope.launch {
            if (currentMessages.isEmpty()) {
                _messagesState.value = ChatState.Loading
                _uiState.value = _uiState.value?.copy(isLoading = true)
            }

            val result = chatRepository.getMessages(
                token = getAuthToken(),
                conversationId = conversationId,
                limit = 50,
                startAfter = currentCursor
            )

            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    data?.let {
                        currentMessages.addAll(it.items)
                        currentHasMore = it.hasMore
                        currentCursor = it.nextCursor

                        _messagesState.value = ChatState.Success(
                            messages = currentMessages.toList()
                        )

                        _uiState.value = ChatUiState(
                            messages = currentMessages.toList(),
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = currentHasMore,
                            nextCursor = currentCursor,
                            errorMessage = null,
                            isSending = false
                        )
                    }
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Đã xảy ra lỗi"
                    _messagesState.value = ChatState.Error(errorMessage)
                    _uiState.value = ChatUiState(
                        messages = currentMessages.toList(),
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = currentHasMore,
                        nextCursor = currentCursor,
                        errorMessage = errorMessage,
                        isSending = false
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isSending = true)

            // 1. Tạo message tạm thời để hiển thị ngay lập tức
            val tempMessage = MessageApiModel(
                id = "temp_${System.currentTimeMillis()}",
                senderId = getCurrentUserId(),
                text = text,
                status = "SENDING", // Trạng thái đang gửi
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date())
            )

            // Thêm message tạm vào danh sách
            currentMessages.add(tempMessage)
            _uiState.value = _uiState.value?.copy(
                messages = currentMessages.toList(),
                isSending = false
            )

            // 2. Gửi message thực tế đến server
            val result = chatRepository.sendMessage(
                token = getAuthToken(),
                conversationId = conversationId,
                text = text
            )

            when {
                result.isSuccess -> {
                    val realMessage = result.getOrNull()
                    realMessage?.let {
                        // Tìm và thay thế message tạm bằng message thực từ server
                        val tempIndex = currentMessages.indexOfFirst { msg -> msg.id == tempMessage.id }
                        if (tempIndex != -1) {
                            currentMessages[tempIndex] = it.copy(status = "SENT")
                            _uiState.value = _uiState.value?.copy(
                                messages = currentMessages.toList()
                            )
                        }
                    }
                }
                result.isFailure -> {
                    // Nếu gửi thất bại, cập nhật trạng thái message tạm
                    val errorIndex = currentMessages.indexOfFirst { msg -> msg.id == tempMessage.id }
                    if (errorIndex != -1) {
                        currentMessages[errorIndex] = tempMessage.copy(status = "FAILED")
                        _uiState.value = _uiState.value?.copy(
                            messages = currentMessages.toList(),
                            errorMessage = "Gửi tin nhắn thất bại"
                        )
                    }
                }
            }
        }
    }

    fun getCurrentUserId(): String {
        return authManager.getCurrentUserId()!!
    }

    private fun getAuthToken(): String {
        val token = authManager.getCurrentToken()
        return token!!
    }

    companion object {
        fun factory(
            conversationId: String,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                        val chatRepository = ChatRepository()
                        return ChatViewModel(chatRepository, context, conversationId) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}