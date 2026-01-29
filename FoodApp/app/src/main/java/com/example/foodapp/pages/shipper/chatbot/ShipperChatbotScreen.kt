package com.example.foodapp.pages.shipper.chatbot

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.chatbot.ChatMessage
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chatbot Screen for Shipper
 * AI-powered assistant
 */
@Composable
fun ShipperChatbotScreen(
    viewModel: ShipperChatbotViewModel = viewModel()
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Refresh button header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShipperColors.Surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ShipperColors.Primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "AI Assistant",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = ShipperColors.TextPrimary
                        )
                        Text(
                            "Luôn sẵn sàng hỗ trợ",
                            fontSize = 11.sp,
                            color = ShipperColors.Success
                        )
                    }
                }
                
                IconButton(onClick = viewModel::clearConversation) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "Clear chat",
                        tint = ShipperColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            HorizontalDivider(color = ShipperColors.Divider, thickness = 1.dp)
            
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
                    ShipperChatMessageItem(message = message)
                }
            }
            
            // Quick replies
            if (uiState.quickReplies.isNotEmpty() && uiState.messages.size <= 2) {
                ShipperQuickRepliesSection(
                    replies = uiState.quickReplies,
                    onReplyClick = viewModel::selectQuickReply,
                    isLoading = uiState.isLoadingQuickReplies
                )
            }
            
            // Input area
            ShipperChatInputArea(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChanged,
                onSend = {
                    viewModel.sendMessage()
                    focusManager.clearFocus()
                },
                isSending = uiState.isSending
            )
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ShipperChatMessageItem(message: ChatMessage) {
    val isUser = message.isFromUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Bot avatar
            Surface(
                shape = CircleShape,
                color = ShipperColors.Primary.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(16.dp)
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
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isUser) 14.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 14.dp
                ),
                color = when {
                    isUser -> ShipperColors.Primary
                    message.isError -> ShipperColors.Error.copy(alpha = 0.1f)
                    else -> ShipperColors.Surface
                },
                shadowElevation = if (isUser) 0.dp else 1.dp
            ) {
                if (message.isLoading) {
                    // Simple loading indicator
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = ShipperColors.Primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Đang suy nghĩ...",
                            color = ShipperColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        message.content,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = when {
                            isUser -> Color.White
                            message.isError -> ShipperColors.Error
                            else -> ShipperColors.TextPrimary
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Timestamp
            if (!message.isLoading) {
                Text(
                    formatTime(message.timestamp),
                    fontSize = 10.sp,
                    color = ShipperColors.TextTertiary,
                    modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
                )
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Surface(
                shape = CircleShape,
                color = ShipperColors.Success.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = ShipperColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShipperQuickRepliesSection(
    replies: List<String>,
    onReplyClick: (String) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShipperColors.Surface)
            .padding(vertical = 10.dp)
    ) {
        Text(
            "Gợi ý câu hỏi",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ShipperColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(replies) { reply ->
                SuggestionChip(
                    onClick = { onReplyClick(reply) },
                    label = { 
                        Text(
                            reply,
                            fontSize = 12.sp,
                            maxLines = 1
                        ) 
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = ShipperColors.Primary.copy(alpha = 0.08f),
                        labelColor = ShipperColors.Primary
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
private fun ShipperChatInputArea(
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
            // Text field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Nhập câu hỏi...",
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
                maxLines = 3,
                enabled = !isSending,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
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

// Helper function
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
