package com.example.foodapp.pages.client.chatbox

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.client.chatbox.ChatBoxRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// ============== CHAT MESSAGE STATES ==============

sealed class ChatMessageState {
    object Idle : ChatMessageState()
    object Loading : ChatMessageState()
    data class Success(val message: String) : ChatMessageState()
    data class Error(val message: String) : ChatMessageState()
}

// ============== QUICK REPLIES STATES ==============

sealed class QuickRepliesState {
    object Idle : QuickRepliesState()
    object Loading : QuickRepliesState()
    data class Success(val quickReplies: List<String>) : QuickRepliesState()
    data class Error(val message: String) : QuickRepliesState()
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// ============== CHAT BOT VIEW MODEL ==============

class ChatBotViewModel(
    private val chatBoxRepository: ChatBoxRepository,
    private val userRepository: UserFirebaseRepository
) : ViewModel() {

    private val _messageState = MutableLiveData<ChatMessageState>(ChatMessageState.Idle)
    val messageState: LiveData<ChatMessageState> = _messageState

    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isRateLimited = MutableLiveData<Boolean>(false)
    val isRateLimited: LiveData<Boolean> = _isRateLimited

    private val _waitTime = MutableLiveData<Int>(0)
    val waitTime: LiveData<Int> = _waitTime

    private val _isSending = MutableLiveData<Boolean>(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _quickRepliesState = MutableLiveData<QuickRepliesState>(QuickRepliesState.Idle)
    val quickRepliesState: LiveData<QuickRepliesState> = _quickRepliesState

    private val _quickReplies = MutableLiveData<List<String>>(emptyList())
    val quickReplies: LiveData<List<String>> = _quickReplies

    // ============== MESSAGE FUNCTIONS ==============

    fun sendMessage(messageText: String) {
        if (messageText.isBlank() || _isSending.value == true) return

        _isSending.value = true

        // Thêm tin nhắn của user vào danh sách
        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            text = messageText,
            isFromUser = true
        )
        val currentMessages = _chatMessages.value ?: emptyList()
        _chatMessages.value = currentMessages + userMessage

        _messageState.value = ChatMessageState.Loading

        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _messageState.value = ChatMessageState.Error("Vui lòng đăng nhập lại")
                    _isSending.value = false
                    return@launch
                }

                currentUser.getIdToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result?.token
                        if (token != null) {
                            viewModelScope.launch {
                                sendMessageToApi(messageText, token)
                            }
                        } else {
                            _messageState.value = ChatMessageState.Error("Không thể lấy token")
                            _isSending.value = false
                        }
                    } else {
                        _messageState.value = ChatMessageState.Error("Lỗi xác thực")
                        _isSending.value = false
                    }
                }
            } catch (e: Exception) {
                _messageState.value = ChatMessageState.Error(e.message ?: "Lỗi không xác định")
                _isSending.value = false
            }
        }
    }

    private suspend fun sendMessageToApi(messageText: String, firebaseToken: String) {
        try {
            val result = chatBoxRepository.sendMessageToChatbot(messageText, firebaseToken)

            when (result) {
                is com.example.foodapp.data.remote.client.response.chatbox.ApiResult.Success -> {
                    val response = result.data
                    if (response.success && response.data != null) {
                        val botResponse = response.data!!

                        if (botResponse.rateLimited) {
                            // Xử lý rate limit
                            _isRateLimited.value = true
                            _waitTime.value = botResponse.waitTime

                            val rateLimitMessage = ChatMessage(
                                id = "bot_rate_${System.currentTimeMillis()}",
                                text = botResponse.answer,
                                isFromUser = false
                            )
                            val currentMessages = _chatMessages.value ?: emptyList()
                            _chatMessages.value = currentMessages + rateLimitMessage

                            _messageState.value = ChatMessageState.Success("Rate limited")
                        } else {
                            // Thêm tin nhắn bot vào danh sách
                            val botMessage = ChatMessage(
                                id = "bot_${System.currentTimeMillis()}",
                                text = botResponse.answer,
                                isFromUser = false
                            )
                            val currentMessages = _chatMessages.value ?: emptyList()
                            _chatMessages.value = currentMessages + botMessage

                            _isRateLimited.value = false
                            _messageState.value = ChatMessageState.Success("Gửi thành công")
                        }
                    } else {
                        _messageState.value = ChatMessageState.Error("Không nhận được phản hồi từ bot")
                    }
                }
                is com.example.foodapp.data.remote.client.response.chatbox.ApiResult.Failure -> {
                    _messageState.value = ChatMessageState.Error(
                        result.exception.message ?: "Lỗi kết nối"
                    )
                }
            }
        } catch (e: Exception) {
            _messageState.value = ChatMessageState.Error(
                e.message ?: "Lỗi không xác định"
            )
        } finally {
            _isSending.value = false
        }
    }

    // ============== QUICK REPLIES FUNCTIONS ==============

    fun fetchQuickReplies() {
        if (_quickReplies.value?.isNotEmpty() == true) return

        _quickRepliesState.value = QuickRepliesState.Loading

        viewModelScope.launch {
            try {
                val result = chatBoxRepository.getQuickReplies()

                when (result) {
                    is com.example.foodapp.data.remote.client.response.chatbox.ApiResult.Success -> {
                        val response = result.data
                        if (response.success && response.data != null) {
                            val replies = response.data!!.quickReplies
                            _quickReplies.value = replies
                            _quickRepliesState.value = QuickRepliesState.Success(replies)
                        } else {
                            _quickRepliesState.value = QuickRepliesState.Error("Không lấy được dữ liệu")
                            // Nếu API lỗi, dùng default replies
                            loadDefaultQuickReplies()
                        }
                    }
                    is com.example.foodapp.data.remote.client.response.chatbox.ApiResult.Failure -> {
                        _quickRepliesState.value = QuickRepliesState.Error(
                            result.exception.message ?: "Lỗi kết nối"
                        )
                        // Nếu API lỗi, dùng default replies
                        loadDefaultQuickReplies()
                    }
                }
            } catch (e: Exception) {
                _quickRepliesState.value = QuickRepliesState.Error(
                    e.message ?: "Lỗi không xác định"
                )
                // Nếu có exception, dùng default replies
                loadDefaultQuickReplies()
            }
        }
    }

    private fun loadDefaultQuickReplies() {
        val defaultReplies = listOf(
            "Làm sao để hủy đơn hàng?",
            "Thời gian giao hàng là bao lâu?",
            "Phí ship được tính như thế nào?",
            "Làm sao để theo dõi đơn hàng?",
            "Thanh toán online có an toàn không?",
            "Tôi muốn đăng ký làm shipper",
            "Cách sử dụng mã giảm giá?"
        )
        _quickReplies.value = defaultReplies
        _quickRepliesState.value = QuickRepliesState.Success(defaultReplies)
    }

    fun useQuickReply(replyText: String) {
        // Sử dụng quick reply như tin nhắn thường
        sendMessage(replyText)
    }

    // ============== UTILITY FUNCTIONS ==============

    fun clearChat() {
        _chatMessages.value = emptyList()
        _messageState.value = ChatMessageState.Idle
        _isRateLimited.value = false
        _waitTime.value = 0
    }

    fun retryLastMessage() {
        val messages = _chatMessages.value ?: emptyList()
        if (messages.isNotEmpty()) {
            val lastUserMessage = messages.lastOrNull { it.isFromUser }
            lastUserMessage?.let {
                sendMessage(it.text)
            }
        }
    }

    fun reset() {
        clearChat()
        _quickReplies.value = emptyList()
        _quickRepliesState.value = QuickRepliesState.Idle
        _isSending.value = false
    }

    // ============== COMPANION OBJECT ==============

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val chatBoxRepository = ChatBoxRepository()
                val userRepository = UserFirebaseRepository(context)
                ChatBotViewModel(chatBoxRepository, userRepository)
            }
        }
    }
}