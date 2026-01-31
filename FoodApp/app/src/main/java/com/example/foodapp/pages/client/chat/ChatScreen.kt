package com.example.foodapp.pages.client.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.chat.MessageApiModel
import java.text.SimpleDateFormat
import java.util.*

// Màu sắc theme được cải thiện
private val PrimaryYellow = Color(0xFFFBBB00)
private val PrimaryYellowLight = Color(0xFFFFD54F)
private val PrimaryYellowDark = Color(0xFFF9A825)
private val BackgroundGray = Color(0xFFF5F7FA)
private val MessageBubbleMe = Color(0xFFFBBB00)
private val MessageBubbleOther = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B7280)
private val ErrorRed = Color(0xFFEF4444)
private val ErrorBackground = Color(0xFFFEE2E2)
private val SuccessGreen = Color(0xFF10B981)
private val BorderColor = Color(0xFFE5E7EB)

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
            Surface(
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.chat_screen_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Text(
                                    text = stringResource(R.string.chat_screen_support),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp
                                    ),
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        Surface(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp),
                            shape = CircleShape,
                            color = BackgroundGray,
                            onClick = onBackClick
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.back_button),
                                    tint = TextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 12.dp,
                tonalElevation = 0.dp,
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
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

            // Refresh indicator với animation đẹp hơn
            AnimatedVisibility(
                visible = uiState.isLoadingMore,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = PrimaryYellow
                        )
                        Text(
                            text = stringResource(R.string.loading_messages),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary
                        )
                    }
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        reverseLayout = false,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
    // Animation cho message bubble
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
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
            else -> Color(0xFF1A1A1A)
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
                    shadowElevation = if (isCurrentUser) 2.dp else 3.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatMessageTime(message.createdAt),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (isCurrentUser) {
                                    Color(0xFF1A1A1A).copy(alpha = 0.5f)
                                } else {
                                    TextSecondary
                                }
                            )

                            // Icon trạng thái cho tin nhắn của người dùng hiện tại
                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.width(5.dp))
                                when (message.status) {
                                    "SENDING" -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = Color(0xFF1A1A1A).copy(alpha = 0.4f)
                                        )
                                    }
                                    "SENT" -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = stringResource(R.string.message_status_sent),
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF1A1A1A).copy(alpha = 0.5f)
                                        )
                                    }
                                    "FAILED" -> {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.message_status_failed),
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
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    isSending: Boolean
) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Divider line
        Divider(
            color = BorderColor.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Input field với design hiện đại
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = BackgroundGray,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = BorderColor.copy(alpha = 0.6f)
                )
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.message_input_placeholder),
                            color = TextSecondary.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp
                            )
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
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    minLines = 1,
                    maxLines = 4
                )
            }

            // Send button với gradient và animation
            val buttonScale by animateFloatAsState(
                targetValue = if (messageText.isNotBlank() && !isSending) 1f else 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "buttonScale"
            )

            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .scale(buttonScale),
                shape = CircleShape,
                color = if (messageText.isNotBlank() && !isSending) {
                    PrimaryYellow
                } else {
                    BorderColor.copy(alpha = 0.4f)
                },
                shadowElevation = if (messageText.isNotBlank() && !isSending) 4.dp else 0.dp,
                onClick = {
                    if (messageText.isNotBlank() && !isSending) {
                        onSendMessage(messageText.trim())
                        messageText = ""
                    }
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (messageText.isNotBlank() && !isSending) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryYellowLight.copy(alpha = 0.3f),
                                        PrimaryYellow
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                            }
                        )
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
                            contentDescription = stringResource(R.string.send_message_button),
                            tint = if (messageText.isNotBlank()) Color.White else TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated loading indicator
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        color = PrimaryYellow,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.loading_messages_view),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            // Icon lỗi với animation
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = ErrorBackground,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.error_messages_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .height(52.dp)
                    .widthIn(min = 140.dp)
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
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