package com.example.foodapp.pages.owner.chatbot

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.chatbot.ChatMessage
import com.example.foodapp.pages.owner.theme.OwnerColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chatbot Screen for Owner
 * AI-powered customer support assistant
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onMenuClick: () -> Unit,
    viewModel: ChatbotViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    
    // Auto scroll when new message added
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    // Snackbar for errors
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatbotTopBar(
                onMenuClick = onMenuClick,
                onClearChat = viewModel::clearConversation
            )
        },
        containerColor = OwnerColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageItem(message = message)
                }
            }
            
            // Quick replies
            if (uiState.quickReplies.isNotEmpty() && uiState.messages.size <= 2) {
                QuickRepliesSection(
                    replies = uiState.quickReplies,
                    onReplyClick = viewModel::selectQuickReply,
                    isLoading = uiState.isLoadingQuickReplies
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatbotTopBar(
    onMenuClick: () -> Unit,
    onClearChat: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bot avatar
                Surface(
                    shape = CircleShape,
                    color = OwnerColors.Primary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Trợ lý AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Luôn sẵn sàng hỗ trợ",
                        fontSize = 12.sp,
                        color = OwnerColors.Success
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onClearChat) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Clear chat",
                    tint = OwnerColors.TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OwnerColors.Surface
        )
    )
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.isFromUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Bot avatar
            Surface(
                shape = CircleShape,
                color = OwnerColors.Primary.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = OwnerColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Message bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = when {
                    isUser -> OwnerColors.Primary
                    message.isError -> OwnerColors.Error.copy(alpha = 0.1f)
                    else -> OwnerColors.Surface
                },
                shadowElevation = if (isUser) 0.dp else 1.dp
            ) {
                if (message.isLoading) {
                    // Simple loading indicator
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = OwnerColors.Primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Đang suy nghĩ...",
                            color = OwnerColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        message.content,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = when {
                            isUser -> Color.White
                            message.isError -> OwnerColors.Error
                            else -> OwnerColors.TextPrimary
                        },
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Timestamp
            if (!message.isLoading) {
                Text(
                    formatTime(message.timestamp),
                    fontSize = 11.sp,
                    color = OwnerColors.TextTertiary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                )
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Surface(
                shape = CircleShape,
                color = OwnerColors.Success.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = OwnerColors.Success,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickRepliesSection(
    replies: List<String>,
    onReplyClick: (String) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OwnerColors.Surface)
            .padding(vertical = 12.dp)
    ) {
        Text(
            "Gợi ý câu hỏi",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = OwnerColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(replies) { reply ->
                AssistChip(
                    onClick = { onReplyClick(reply) },
                    label = { 
                        Text(
                            reply,
                            fontSize = 13.sp,
                            maxLines = 1
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.QuestionAnswer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = OwnerColors.Primary.copy(alpha = 0.08f),
                        labelColor = OwnerColors.Primary,
                        leadingIconContentColor = OwnerColors.Primary
                    ),
                    border = null
                )
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
        shadowElevation = 8.dp,
        color = OwnerColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Nhập câu hỏi...",
                        color = OwnerColors.TextTertiary
                    ) 
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OwnerColors.Primary,
                    unfocusedBorderColor = OwnerColors.Divider,
                    focusedContainerColor = OwnerColors.Background,
                    unfocusedContainerColor = OwnerColors.Background
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 3,
                enabled = !isSending
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSending,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = OwnerColors.Primary,
                    contentColor = Color.White,
                    disabledContainerColor = OwnerColors.Primary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// Helper function
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
