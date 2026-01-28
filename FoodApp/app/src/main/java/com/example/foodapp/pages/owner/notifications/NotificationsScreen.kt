package com.example.foodapp.pages.owner.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.owner.notification.Notification
import com.example.foodapp.data.model.owner.notification.NotificationType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onMenuClick: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NotificationsTopBar(
                unreadCount = uiState.unreadCount,
                onMenuClick = onMenuClick,
                onRefreshClick = { viewModel.refresh() },
                onMarkAllReadClick = { viewModel.markAllAsRead() },
                onSettingsClick = { viewModel.showPreferencesDialog() },
                isRefreshing = uiState.isRefreshing,
                isActionLoading = uiState.isActionLoading
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Chips
            NotificationFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.onFilterSelected(it) },
                unreadCount = uiState.unreadCount
            )

            // Content
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null && uiState.notifications.isEmpty() -> {
                    ErrorContent(
                        message = uiState.error ?: "Đã xảy ra lỗi",
                        onRetry = { viewModel.loadNotifications() }
                    )
                }
                else -> {
                    val filteredNotifications = viewModel.getFilteredNotifications()
                    
                    if (filteredNotifications.isEmpty()) {
                        EmptyNotificationsContent(selectedFilter = uiState.selectedFilter)
                    } else {
                        NotificationsList(
                            notifications = filteredNotifications,
                            onNotificationClick = { notification ->
                                if (!notification.read) {
                                    viewModel.markAsRead(notification.id)
                                }
                            },
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                }
            }
        }

        // Preferences Dialog
        if (uiState.showPreferencesDialog) {
            NotificationPreferencesDialog(
                preferences = uiState.preferences,
                isLoading = uiState.isActionLoading,
                onDismiss = { viewModel.dismissPreferencesDialog() },
                onSave = { informational, marketing ->
                    viewModel.updatePreferences(informational, marketing)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTopBar(
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onMarkAllReadClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isRefreshing: Boolean,
    isActionLoading: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Thông báo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else "$unreadCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            // Refresh button
            IconButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                }
            }

            // More options menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Thêm")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Đánh dấu tất cả đã đọc") },
                        onClick = {
                            showMenu = false
                            onMarkAllReadClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.DoneAll, contentDescription = null)
                        },
                        enabled = unreadCount > 0 && !isActionLoading
                    )
                    DropdownMenuItem(
                        text = { Text("Cài đặt thông báo") },
                        onClick = {
                            showMenu = false
                            onSettingsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun NotificationFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    unreadCount: Int
) {
    val filters = listOf(
        Triple(NotificationUiState.FILTER_ALL, "Tất cả", Icons.Default.Notifications),
        Triple(NotificationUiState.FILTER_UNREAD, "Chưa đọc ($unreadCount)", Icons.Default.MarkEmailUnread),
        Triple(NotificationUiState.FILTER_ORDER, "Đơn hàng", Icons.Default.ShoppingCart),
        Triple(NotificationUiState.FILTER_SYSTEM, "Hệ thống", Icons.Default.Info),
        Triple(NotificationUiState.FILTER_PROMOTION, "Khuyến mãi", Icons.Default.LocalOffer)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label, icon) ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(label, maxLines = 1) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null,
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

@Composable
private fun NotificationsList(
    notifications: List<Notification>,
    onNotificationClick: (Notification) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = notifications,
                key = { it.id }
            ) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Show loading indicator when refreshing
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = !notification.read

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            NotificationIcon(type = notification.type, isUnread = isUnread)

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Body
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatNotificationTime(notification.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Status badge
                    if (isUnread) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mới",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đã đọc",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Type badge
                notification.type.let { type ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getTypeColor(type).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = getTypeColor(type),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationIcon(type: NotificationType, isUnread: Boolean) {
    val iconInfo = getIconForType(type)
    val icon = iconInfo.first
    val color = iconInfo.second

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.2f),
                        color.copy(alpha = 0.05f)
                    )
                )
            )
            .then(
                if (isUnread) Modifier.shadow(4.dp, CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = type.displayName,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                "Đang tải thông báo...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Lỗi",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Không thể tải thông báo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại")
            }
        }
    }
}

@Composable
private fun EmptyNotificationsContent(selectedFilter: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = "Không có thông báo",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = when (selectedFilter) {
                    NotificationUiState.FILTER_UNREAD -> "Không có thông báo chưa đọc"
                    NotificationUiState.FILTER_ORDER -> "Không có thông báo đơn hàng"
                    NotificationUiState.FILTER_SYSTEM -> "Không có thông báo hệ thống"
                    NotificationUiState.FILTER_PROMOTION -> "Không có thông báo khuyến mãi"
                    else -> "Không có thông báo nào"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Chúng tôi sẽ thông báo cho bạn khi có tin mới",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotificationPreferencesDialog(
    preferences: com.example.foodapp.data.model.owner.notification.NotificationPreferences?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (informational: Boolean?, marketing: Boolean?) -> Unit
) {
    var informational by remember(preferences) { mutableStateOf(preferences?.informational ?: true) }
    var marketing by remember(preferences) { mutableStateOf(preferences?.marketing ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Cài đặt thông báo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (preferences == null && !isLoading) {
                    Text("Không thể tải cài đặt")
                } else {
                    // Transactional (always on)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Đơn hàng & Thanh toán",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Thông báo quan trọng, không thể tắt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = null,
                            enabled = false
                        )
                    }

                    HorizontalDivider()

                    // Informational
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Cập nhật & Tổng kết",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Tổng kết doanh thu, cập nhật hệ thống",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = informational,
                            onCheckedChange = { informational = it }
                        )
                    }

                    HorizontalDivider()

                    // Marketing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Khuyến mãi",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Voucher mới, chương trình khuyến mãi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = marketing,
                            onCheckedChange = { marketing = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(informational, marketing) },
                enabled = !isLoading && preferences != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

// ==================== Helper Functions ====================

private fun getIconForType(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.NEW_ORDER,
        NotificationType.ORDER_CONFIRMED,
        NotificationType.ORDER_PREPARING,
        NotificationType.ORDER_READY,
        NotificationType.ORDER_SHIPPING,
        NotificationType.ORDER_DELIVERED -> Pair(Icons.Default.ShoppingCart, Color(0xFF4CAF50))

        NotificationType.ORDER_CANCELLED -> Pair(Icons.Default.Cancel, Color(0xFFF44336))

        NotificationType.PAYMENT_SUCCESS -> Pair(Icons.Default.Paid, Color(0xFF4CAF50))
        NotificationType.PAYMENT_FAILED -> Pair(Icons.Default.MoneyOff, Color(0xFFF44336))
        NotificationType.PAYMENT_REFOUNDED -> Pair(Icons.Default.CurrencyExchange, Color(0xFFFF9800))

        NotificationType.SHIPPER_ASSIGNED,
        NotificationType.SHIPPER_APPLIED -> Pair(Icons.Default.DeliveryDining, Color(0xFF2196F3))

        NotificationType.SHIPPER_APPLICATION_APPROVED -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
        NotificationType.SHIPPER_APPLICATION_REJECTED -> Pair(Icons.Default.Block, Color(0xFFF44336))

        NotificationType.DAILY_SUMMARY -> Pair(Icons.Default.BarChart, Color(0xFF9C27B0))
        NotificationType.SUBSCRIPTION_EXPIRING -> Pair(Icons.Default.Warning, Color(0xFFFF9800))

        NotificationType.PROMOTION,
        NotificationType.VOUCHER_AVAILABLE -> Pair(Icons.Default.LocalOffer, Color(0xFFE91E63))

        NotificationType.UNKNOWN -> Pair(Icons.Default.Notifications, Color(0xFF607D8B))
    }
}

private fun getTypeColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_ORDER,
        NotificationType.ORDER_CONFIRMED,
        NotificationType.ORDER_PREPARING,
        NotificationType.ORDER_READY,
        NotificationType.ORDER_SHIPPING,
        NotificationType.ORDER_DELIVERED,
        NotificationType.PAYMENT_SUCCESS,
        NotificationType.SHIPPER_APPLICATION_APPROVED -> Color(0xFF4CAF50)

        NotificationType.ORDER_CANCELLED,
        NotificationType.PAYMENT_FAILED,
        NotificationType.SHIPPER_APPLICATION_REJECTED -> Color(0xFFF44336)

        NotificationType.PAYMENT_REFOUNDED,
        NotificationType.SUBSCRIPTION_EXPIRING -> Color(0xFFFF9800)

        NotificationType.SHIPPER_ASSIGNED,
        NotificationType.SHIPPER_APPLIED -> Color(0xFF2196F3)

        NotificationType.DAILY_SUMMARY -> Color(0xFF9C27B0)

        NotificationType.PROMOTION,
        NotificationType.VOUCHER_AVAILABLE -> Color(0xFFE91E63)

        NotificationType.UNKNOWN -> Color(0xFF607D8B)
    }
}

private fun formatNotificationTime(timestamp: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        when {
            diff < 60000 -> "Vừa xong"
            diff < 3600000 -> "${diff / 60000} phút trước"
            diff < 86400000 -> "${diff / 3600000} giờ trước"
            diff < 604800000 -> "${diff / 86400000} ngày trước"
            else -> {
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        "Không xác định"
    }
}
