package com.example.foodapp.pages.shipper.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.model.chat.Conversation
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Conversations List Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onConversationClick: (String) -> Unit,
    viewModel: ConversationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
        when {
            uiState.isLoading && uiState.conversations.isEmpty() -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ShipperColors.Primary)
                }
            }
            uiState.conversations.isEmpty() -> {
                // Empty state
                EmptyConversationsState()
            }
            else -> {
                // Conversations list
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::refresh
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.conversations, key = { it.id }) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.id) }
                            )
                        }
                        
                        // Load more
                        if (uiState.hasMore) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMore()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = ShipperColors.Primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val otherParticipant = conversation.otherParticipant
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = ShipperColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherParticipant?.displayName ?: "Unknown",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ShipperColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = formatTime(conversation.lastMessageAt),
                        fontSize = 12.sp,
                        color = ShipperColors.TextTertiary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Role badge
                    if (!otherParticipant?.role.isNullOrEmpty()) {
                        val roleText = when (otherParticipant?.role) {
                            "OWNER" -> otherParticipant.shopName ?: "Chủ shop"
                            "CUSTOMER" -> "Khách hàng"
                            "SHIPPER" -> "Shipper"
                            else -> otherParticipant?.role ?: ""
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (otherParticipant?.role) {
                                "OWNER" -> ShipperColors.Primary.copy(alpha = 0.1f)
                                "CUSTOMER" -> ShipperColors.Success.copy(alpha = 0.1f)
                                else -> ShipperColors.TextSecondary.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                text = roleText,
                                fontSize = 10.sp,
                                color = when (otherParticipant?.role) {
                                    "OWNER" -> ShipperColors.Primary
                                    "CUSTOMER" -> ShipperColors.Success
                                    else -> ShipperColors.TextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = conversation.lastMessage.ifEmpty { "Bắt đầu cuộc trò chuyện" },
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    HorizontalDivider(
        color = ShipperColors.Divider,
        modifier = Modifier.padding(start = 80.dp)
    )
}

@Composable
private fun EmptyConversationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = ShipperColors.PrimaryLight,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ShipperColors.Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Chưa có cuộc trò chuyện nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShipperColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Các cuộc trò chuyện với khách hàng và chủ shop sẽ xuất hiện ở đây",
            fontSize = 14.sp,
            color = ShipperColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// Helper function
private fun formatTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString.replace("Z", "").substringBefore("."))
        
        val now = Date()
        val diffMs = now.time - (date?.time ?: now.time)
        val diffMinutes = diffMs / (1000 * 60)
        val diffHours = diffMs / (1000 * 60 * 60)
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        
        when {
            diffMinutes < 1 -> "Vừa xong"
            diffMinutes < 60 -> "${diffMinutes}p"
            diffHours < 24 -> "${diffHours}h"
            diffDays < 7 -> "${diffDays}d"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date!!)
        }
    } catch (e: Exception) {
        ""
    }
}
