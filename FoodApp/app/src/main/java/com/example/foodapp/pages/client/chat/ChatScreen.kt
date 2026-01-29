package com.example.foodapp.pages.client.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.remote.client.response.chat.MessageApiModel
import java.text.SimpleDateFormat
import java.util.*

// Màu sắc theme
private val PrimaryYellow = Color(0xFFFBBB00)
private val BackgroundGray = Color(0xFFF8F9FA)
private val MessageBubbleMe = Color(0xFFFBBB00)
private val MessageBubbleOther = Color.White
private val TextPrimary = Color(0xFF212121)
private val TextSecondary = Color(0xFF757575)
private val ErrorRed = Color(0xFFE53935)
private val ErrorBackground = Color(0xFFFFEBEE)

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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Trò chuyện",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Hỗ trợ khách hàng",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(
                    elevation = 2.dp,
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                MessageInput(
                    onSendMessage = { message ->
                        viewModel.sendMessage(message)
                    },
                    isSending = uiState.isSending
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = ErrorRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                is ChatState.Idle, null -> {
                    LoadingMessagesView()
                }
            }

            // Refresh indicator khi đang tải thêm
            if (uiState.isLoadingMore) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = PrimaryYellow
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

    // Sắp xếp messages theo thời gian tăng dần (cũ -> mới)
    val sortedMessages = remember(messages) {
        messages.sortedBy { it.createdAt }
    }

    // Tự động scroll xuống cuối khi có tin nhắn mới
    LaunchedEffect(sortedMessages.size) {
        if (sortedMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(sortedMessages.size - 1)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        reverseLayout = false, // ĐỔI THÀNH false
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = sortedMessages,
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
    // Xác định màu sắc dựa trên status
    val bubbleColor = when {
        !isCurrentUser -> MessageBubbleOther
        message.status == "FAILED" -> ErrorBackground
        else -> MessageBubbleMe
    }

    val textColor = when {
        !isCurrentUser -> TextPrimary
        message.status == "FAILED" -> ErrorRed
        else -> Color.Black
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = when {
                    isCurrentUser -> RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    )
                    else -> RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                },
                color = bubbleColor,
                shadowElevation = if (isCurrentUser) 0.dp else 1.dp,
                tonalElevation = if (isCurrentUser) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatMessageTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp
                            ),
                            color = if (isCurrentUser) {
                                Color.Black.copy(alpha = 0.6f)
                            } else {
                                TextSecondary
                            }
                        )

                        // Icon trạng thái cho tin nhắn của người dùng hiện tại
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
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
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                                "FAILED" -> {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Gửi thất bại",
                                        modifier = Modifier.size(14.dp),
                                        tint = ErrorRed
                                    )
                                }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Input field với background đẹp hơn
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = BackgroundGray,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.1f)
            )
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Nhập tin nhắn...",
                        color = TextSecondary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryYellow,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp
                ),
                minLines = 1,
                maxLines = 4
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Send button với thiết kế đẹp hơn
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (messageText.isNotBlank() && !isSending) {
                PrimaryYellow
            } else {
                Color.LightGray.copy(alpha = 0.3f)
            },
            onClick = {
                if (messageText.isNotBlank() && !isSending) {
                    onSendMessage(messageText.trim())
                    messageText = ""
                }
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = if (messageText.isNotBlank()) Color.White else TextSecondary,
                        modifier = Modifier.size(22.dp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = PrimaryYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Đang tải tin nhắn...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon lỗi
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = ErrorBackground
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 120.dp)
            ) {
                Text(
                    text = "Thử lại",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Extension function để thêm shadow cho TopAppBar
fun Modifier.shadow(
    elevation: androidx.compose.ui.unit.Dp,
    spotColor: Color = Color.Black
): Modifier = this

fun formatMessageTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)

        if (date != null) {
            val now = Calendar.getInstance()
            val messageDate = Calendar.getInstance().apply {
                time = date
            }

            // Kiểm tra nếu là hôm nay
            val isToday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)

            if (isToday) {
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                outputFormat.format(date)
            } else {
                // Nếu không phải hôm nay, hiển thị ngày và giờ
                val outputFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                outputFormat.format(date)
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}