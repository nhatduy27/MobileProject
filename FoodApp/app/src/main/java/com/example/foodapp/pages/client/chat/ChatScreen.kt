package com.example.foodapp.pages.client.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.remote.client.response.chat.MessageApiModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.factory(
            conversationId = conversationId,
            context = context
        )
    )

    // Observe state
    val uiState by viewModel.uiState.observeAsState(ChatUiState())
    val messagesState by viewModel.messagesState.observeAsState()

    // Hiển thị snackbar khi có lỗi
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            // Xóa error message sau khi hiển thị
            // viewModel.clearErrorMessage() // Bạn có thể thêm hàm này trong ViewModel
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tin nhắn",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            MessageInput(
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                isSending = uiState.isSending
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = messagesState) {
                is ChatState.Loading -> {
                    LoadingMessagesView()
                }
                is ChatState.Error -> {
                    ErrorMessagesView(
                        errorMessage = state.message,
                        onRetryClick = { viewModel.loadMessages(true) }
                    )
                }
                is ChatState.Success -> {
                    MessagesList(
                        messages = uiState.messages,
                        currentUserId = viewModel.getCurrentUserId()
                    )
                }
                is ChatState.Idle -> {
                    LoadingMessagesView()
                }
                null -> {
                    LoadingMessagesView()
                }
            }

            // Refresh indicator khi đang tải thêm
            if (uiState.isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
fun MessagesList(
    messages: List<MessageApiModel>,
    currentUserId: String
) {
    val lazyListState = rememberLazyListState()

    // Tự động scroll xuống cuối khi có tin nhắn mới
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(0) // Vì đang dùng reverseLayout = true
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        reverseLayout = true, // Tin nhắn mới nhất ở dưới cùng
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = messages, // Không cần reversed vì đã có reverseLayout = true
            key = { it.id }
        ) { message ->
            MessageBubble(
                message = message,
                isCurrentUser = message.senderId == currentUserId
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageApiModel,
    isCurrentUser: Boolean
) {
    // Xác định màu sắc và trạng thái dựa trên status
    val bubbleColor = when {
        !isCurrentUser -> Color(0xFFFFFFFF)
        message.status == "FAILED" -> Color(0xFFFFCDD2) // Màu đỏ nhạt khi thất bại
        else -> Color(0xFFFBBB00)
    }

    val textColor = if (isCurrentUser) Color.Black else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = when {
                isCurrentUser -> RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                else -> RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentUser) Color.Black.copy(alpha = 0.7f) else Color.Gray,
                    )

                    // Hiển thị icon trạng thái cho tin nhắn của người dùng hiện tại
                    if (isCurrentUser) {
                        when (message.status) {
                            "SENDING" -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = Color.Black.copy(alpha = 0.5f)
                                )
                            }
                            "SENT" -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Đã gửi",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Black.copy(alpha = 0.5f)
                                )
                            }
                            "FAILED" -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Gửi thất bại",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Red.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    isSending: Boolean
) {
    var messageText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập tin nhắn...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFFFBBB00),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = false,
                maxLines = 3
            )

            // Send button với spacing tốt hơn
            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    if (messageText.isNotBlank() && !isSending) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank() && !isSending,
                modifier = Modifier.size(48.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFFBBB00)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = if (messageText.isNotBlank()) Color(0xFFFBBB00) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingMessagesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFBBB00)
            )
            Text(
                text = "Đang tải tin nhắn...",
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun ErrorMessagesView(
    errorMessage: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFBBB00)
                )
            ) {
                Text("Thử lại")
            }
        }
    }
}

fun formatMessageTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)

        if (date != null) {
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}