package com.example.foodapp.pages.owner.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.model.chat.ChatMessage
import com.example.foodapp.data.model.chat.MessageStatus
import java.text.SimpleDateFormat
import java.util.*

// Owner theme colors
private val OwnerPrimary = Color(0xFFFF5722)
private val OwnerPrimaryLight = Color(0xFFFFE0B2)
private val OwnerBackground = Color(0xFFF5F5F5)
private val OwnerSurface = Color.White
private val OwnerTextPrimary = Color(0xFF212121)
private val OwnerTextSecondary = Color(0xFF757575)
private val OwnerTextTertiary = Color(0xFF9E9E9E)
private val OwnerDivider = Color(0xFFE0E0E0)

/**
 * Chat Detail Screen - Owner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerChatDetailScreen(
    conversationId: String,
    onBack: () -> Unit,
    viewModel: OwnerChatDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Initialize with conversation ID
    LaunchedEffect(conversationId) {
        viewModel.setConversationId(conversationId)
    }
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }
    
    val otherParticipant = uiState.conversation?.otherParticipant
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OwnerPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!otherParticipant?.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = otherParticipant?.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = otherParticipant?.displayName?.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OwnerPrimary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            Text(
                                text = otherParticipant?.displayName ?: "Chat",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = OwnerTextPrimary
                            )
                            if (!otherParticipant?.role.isNullOrEmpty()) {
                                val roleText = when (otherParticipant?.role) {
                                    "CUSTOMER" -> "Khách hàng"
                                    "SHIPPER" -> "Shipper"
                                    "OWNER" -> "Chủ shop"
                                    else -> ""
                                }
                                Text(
                                    text = roleText,
                                    fontSize = 12.sp,
                                    color = OwnerTextSecondary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OwnerTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwnerSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = OwnerBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Load more indicator
                if (uiState.hasMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = viewModel::loadMoreMessages) {
                                if (uiState.isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = OwnerPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Tải thêm", color = OwnerPrimary)
                                }
                            }
                        }
                    }
                }
                
                // Loading state
                if (uiState.isLoading && uiState.messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OwnerPrimary)
                        }
                    }
                }
                
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isMe = message.senderId == uiState.currentUserId
                    )
                }
            }
            
            // Input area
            ChatInputArea(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChanged,
                onSend = {
                    viewModel.sendMessage()
                    focusManager.clearFocus()
                },
                isSending = uiState.isSending
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe) OwnerPrimary else OwnerSurface,
                shadowElevation = if (isMe) 0.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isMe) Color.White else OwnerTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            
            // Time and status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 10.sp,
                    color = OwnerTextTertiary
                )
                
                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (message.status) {
                            MessageStatus.PENDING -> "⏳"
                            MessageStatus.SENT -> "✓"
                            MessageStatus.READ -> "✓✓"
                        },
                        fontSize = 10.sp,
                        color = if (message.status == MessageStatus.READ) 
                            OwnerPrimary 
                        else 
                            OwnerTextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        shadowElevation = 4.dp,
        color = OwnerSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Nhập tin nhắn...",
                        color = OwnerTextTertiary,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OwnerPrimary,
                    unfocusedBorderColor = OwnerDivider,
                    focusedContainerColor = OwnerBackground,
                    unfocusedContainerColor = OwnerBackground
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4,
                enabled = !isSending,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSending,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = OwnerPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = OwnerPrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.size(44.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatMessageTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString.replace("Z", "").substringBefore("."))
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
    } catch (e: Exception) {
        ""
    }
}
