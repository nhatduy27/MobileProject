package com.example.foodapp.pages.client.listchat

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.repository.client.chat.ChatRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onBackClick: () -> Unit,
    onConversationClick: (conversationId: String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: ConversationsViewModel = viewModel(
        factory = ConversationsViewModel.factory(
            chatRepository = ChatRepository(),
            context = context
        )
    )

    // Observe state
    val uiState by viewModel.uiState.observeAsState(ConversationsUiState())
    val conversationsState by viewModel.conversationsState.observeAsState()

    // Lazy list state cho load more
    val lazyListState = rememberLazyListState()

    // Kiểm tra khi scroll đến cuối
    LaunchedEffect(lazyListState.layoutInfo) {
        val layoutInfo = lazyListState.layoutInfo
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

        if (lastVisibleItem != null &&
            lastVisibleItem.index >= layoutInfo.totalItemsCount - 3 &&
            uiState.hasMore &&
            !uiState.isLoadingMore &&
            !uiState.isLoading) {

            viewModel.loadMoreConversations()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.conversations_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFFBBB00),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.refreshConversations() },
                            enabled = !uiState.isRefreshing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh_button),
                                tint = Color.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = conversationsState) {
                is ConversationsState.Loading -> {
                    LoadingView()
                }
                is ConversationsState.Error -> {
                    ErrorView(
                        errorMessage = state.message,
                        onRetryClick = { viewModel.refreshConversations() }
                    )
                }
                is ConversationsState.Success -> {
                    if (uiState.conversations.isEmpty()) {
                        EmptyConversationsView()
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = uiState.conversations,
                                key = { it.id }
                            ) { conversation ->
                                ConversationItem(
                                    conversation = conversation,
                                    onClick = { onConversationClick(conversation.id) },
                                    context
                                )
                            }

                            // Footer cho load more
                            if (uiState.hasMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uiState.isLoadingMore) {
                                            CircularProgressIndicator(
                                                color = Color(0xFFFBBB00),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is ConversationsState.Idle -> {
                    LoadingView()
                }
                null -> {
                    LoadingView()
                }
            }

            // Loading more indicator
            if (uiState.isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFBBB00),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: com.example.foodapp.data.remote.client.response.chat.ConversationApiModel,
    onClick: () -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.avatar_content_description),
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Hiển thị display name từ otherParticipant
                val displayName = conversation.otherParticipant?.displayName?.ifBlank {
                    // Nếu không có displayName, fallback về participants
                    conversation.participants
                        .take(2)
                        .joinToString(", ") { it.take(8) + "..." }
                } ?: run {
                    conversation.participants
                        .take(2)
                        .joinToString(", ") { it.take(8) + "..." }
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Last message (tin nhắn mới nhất)
                if (conversation.lastMessage.isNotBlank()) {
                    Text(
                        text = conversation.lastMessage.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = stringResource(R.string.no_messages),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF999999),
                        maxLines = 1
                    )
                }

                // Time của tin nhắn mới nhất
                if (conversation.lastMessageAt.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(conversation.lastMessageAt, context),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888888)
                        )

                        // Hiển thị role nếu có
                        conversation.otherParticipant?.role?.let { role ->
                            if (role.isNotBlank()) {
                                Text(
                                    text = stringResource(R.string.role_display, role),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFBBB00),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Unread indicator (nếu có)
            // TODO: Bổ sung logic kiểm tra tin nhắn chưa đọc
            val hasUnread = false // Tạm thời để false
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBBB00))
                )
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFBBB00)
            )
            Text(
                text = stringResource(R.string.loading_conversations),
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun ErrorView(
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
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = stringResource(R.string.error_content_description),
                tint = Color(0xFF888888),
                modifier = Modifier.size(64.dp)
            )

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
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
fun EmptyConversationsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = stringResource(R.string.empty_conversations_content_description),
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(96.dp)
            )

            Text(
                text = stringResource(R.string.empty_conversations_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF666666)
            )

            Text(
                text = stringResource(R.string.empty_conversations_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF888888),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Helper function để format thời gian - Compatible với API < 26
fun formatTime(timestamp: String, context: Context): String { // 1. Thêm context vào tham số
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)

        if (date == null) return timestamp

        val now = Date()
        val diffInMillis = now.time - date.time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        // 2. Thay stringResource bằng context.getString
        when {
            minutes < 1 -> context.getString(R.string.just_now)
            minutes < 60 -> context.getString(R.string.minutes_ago, minutes.toInt())
            hours < 24 -> context.getString(R.string.hours_ago, hours.toInt())
            days < 7 -> context.getString(R.string.days_ago, days.toInt())
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        timestamp
    }
}