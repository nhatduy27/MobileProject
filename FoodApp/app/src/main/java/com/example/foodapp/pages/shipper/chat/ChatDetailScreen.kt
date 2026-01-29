package com.example.foodapp.pages.shipper.chat

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
import androidx.compose.material.icons.filled.Person
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
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat Detail Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = viewModel()
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
                                .background(ShipperColors.PrimaryLight),
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
                                    color = ShipperColors.Primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            Text(
                                text = otherParticipant?.displayName ?: "Chat",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = ShipperColors.TextPrimary
                            )
                            if (!otherParticipant?.role.isNullOrEmpty()) {
                                val roleText = when (otherParticipant?.role) {
                                    "OWNER" -> otherParticipant.shopName ?: "Chủ shop"
                                    "CUSTOMER" -> "Khách hàng"
                                    "SHIPPER" -> "Shipper"
                                    else -> ""
                                }
                                Text(
                                    text = roleText,
                                    fontSize = 12.sp,
                                    color = ShipperColors.TextSecondary
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
                            tint = ShipperColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShipperColors.Surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ShipperColors.Background
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
                                        color = ShipperColors.Primary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Tải thêm", color = ShipperColors.Primary)
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
                            CircularProgressIndicator(color = ShipperColors.Primary)
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
                color = if (isMe) ShipperColors.Primary else ShipperColors.Surface,
                shadowElevation = if (isMe) 0.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isMe) Color.White else ShipperColors.TextPrimary,
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
                    color = ShipperColors.TextTertiary
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
                            ShipperColors.Primary 
                        else 
                            ShipperColors.TextTertiary
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
        color = ShipperColors.Surface
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
                        color = ShipperColors.TextTertiary,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShipperColors.Primary,
                    unfocusedBorderColor = ShipperColors.Divider,
                    focusedContainerColor = ShipperColors.Background,
                    unfocusedContainerColor = ShipperColors.Background
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
                    containerColor = ShipperColors.Primary,
                    contentColor = Color.White,
                    disabledContainerColor = ShipperColors.Primary.copy(alpha = 0.5f),
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
