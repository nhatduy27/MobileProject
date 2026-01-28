package com.example.foodapp.pages.owner.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.owner.notification.Notification
import com.example.foodapp.data.model.owner.notification.NotificationType
import java.text.SimpleDateFormat
import java.util.*

/**
 * NotificationBell - Icon chuông thông báo với badge hiển thị số chưa đọc
 * Sử dụng trong TopBar của toàn app owner
 */
@Composable
fun NotificationBell(
    viewModel: NotificationsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPanel by remember { mutableStateOf(false) }
    
    // State cho toast notification mới
    var showNewNotificationToast by remember { mutableStateOf(false) }
    var latestNotification by remember { mutableStateOf<Notification?>(null) }
    
    // Theo dõi các notification ID đã biết để phát hiện thông báo MỚI THẬT SỰ
    var knownNotificationIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isInitialLoad by remember { mutableStateOf(true) }
    
    // Theo dõi khi có thông báo mới THẬT SỰ (không phải thông báo cũ chưa đọc)
    LaunchedEffect(uiState.notifications) {
        val currentIds = uiState.notifications.map { it.id }.toSet()
        
        if (isInitialLoad) {
            // Lần đầu mount - lưu tất cả ID hiện tại, không hiển thị toast
            knownNotificationIds = currentIds
            isInitialLoad = false
        } else {
            // Tìm các notification ID mới (chưa có trong danh sách đã biết)
            val newIds = currentIds - knownNotificationIds
            
            if (newIds.isNotEmpty()) {
                // Tìm thông báo mới nhất trong số các thông báo mới
                val newNotification = uiState.notifications.firstOrNull { it.id in newIds && !it.read }
                
                if (newNotification != null) {
                    latestNotification = newNotification
                    showNewNotificationToast = true
                    
                    // Tự động ẩn sau 4 giây
                    kotlinx.coroutines.delay(4000)
                    showNewNotificationToast = false
                }
            }
            
            // Cập nhật danh sách ID đã biết
            knownNotificationIds = currentIds
        }
    }
    
    // Animation cho bell khi có thông báo mới
    val infiniteTransition = rememberInfiniteTransition(label = "bell_animation")
    val bellScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.unreadCount > 0) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell_scale"
    )

    Box(modifier = modifier) {
        // Bell Icon Button
        IconButton(
            onClick = { showPanel = true },
            modifier = Modifier.scale(if (uiState.unreadCount > 0) bellScale else 1f)
        ) {
            Icon(
                imageVector = if (uiState.unreadCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                contentDescription = "Thông báo",
                tint = if (uiState.unreadCount > 0) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Badge với số thông báo chưa đọc
        if (uiState.unreadCount > 0) {
            Badge(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(18.dp)
            ) {
                Text(
                    text = if (uiState.unreadCount > 99) "99+" else uiState.unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Notification Panel
    if (showPanel) {
        NotificationBottomSheet(
            viewModel = viewModel,
            onDismiss = { showPanel = false }
        )
    }
    
    // Toast Popup cho thông báo mới
    NewNotificationToast(
        visible = showNewNotificationToast,
        notification = latestNotification,
        onDismiss = { showNewNotificationToast = false },
        onClick = {
            showNewNotificationToast = false
            showPanel = true
        }
    )
}

/**
 * Toast Popup hiển thị thông báo mới - Overlay thực sự trên cùng màn hình
 * Sử dụng Popup để hiển thị độc lập với layout hierarchy
 */
@Composable
private fun NewNotificationToast(
    visible: Boolean,
    notification: Notification?,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    if (visible && notification != null) {
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(0, 0),
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            // Wrap trong Box để có animation
            var isVisible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                isVisible = true
            }
            
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                val iconInfo = getNotificationIconInfo(notification.type)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 48.dp) // Padding từ top để tránh status bar
                        .clickable(onClick = onClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconInfo.second.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconInfo.first,
                                contentDescription = null,
                                tint = iconInfo.second,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Content
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notification.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * NotificationBottomSheet - Panel thông báo hiện đại dạng BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    viewModel: NotificationsViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            NotificationPanelHeader(
                unreadCount = uiState.unreadCount,
                isLoading = uiState.isRefreshing || uiState.isActionLoading,
                onClose = onDismiss,
                onRefresh = { viewModel.refresh() },
                onMarkAllRead = { viewModel.markAllAsRead() },
                onSettings = { viewModel.showPreferencesDialog() }
            )
            
            // Content - Filters bên trong LazyColumn để scroll cùng
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        NotificationLoadingState()
                    }
                    uiState.error != null && uiState.notifications.isEmpty() -> {
                        NotificationErrorState(
                            message = uiState.error ?: "Đã xảy ra lỗi",
                            onRetry = { viewModel.loadNotifications() }
                        )
                    }
                    else -> {
                        val filteredNotifications = viewModel.getFilteredNotifications()
                        if (filteredNotifications.isEmpty() && uiState.selectedFilter != NotificationUiState.FILTER_ALL) {
                            // Có filter nhưng không có kết quả - vẫn hiện filter chips
                            NotificationListWithFilters(
                                notifications = emptyList(),
                                selectedFilter = uiState.selectedFilter,
                                onFilterSelected = { viewModel.onFilterSelected(it) },
                                onNotificationClick = {},
                                showEmptyState = true
                            )
                        } else if (filteredNotifications.isEmpty()) {
                            NotificationEmptyState(selectedFilter = uiState.selectedFilter)
                        } else {
                            NotificationListWithFilters(
                                notifications = filteredNotifications,
                                selectedFilter = uiState.selectedFilter,
                                onFilterSelected = { viewModel.onFilterSelected(it) },
                                onNotificationClick = { notification ->
                                    if (!notification.read) {
                                        viewModel.markAsRead(notification.id)
                                    }
                                },
                                showEmptyState = false
                            )
                        }
                    }
                }
            }
            
            // SnackbarHost
            SnackbarHost(snackbarHostState)
        }
        
        // Preferences Dialog
        if (uiState.showPreferencesDialog) {
            NotificationSettingsDialog(
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

/**
 * Header của Notification Panel
 */
@Composable
private fun NotificationPanelHeader(
    unreadCount: Int,
    isLoading: Boolean,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
    onSettings: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column {
            // Drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title with badge
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Thông báo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = if (unreadCount > 99) "99+" else "$unreadCount mới",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Actions
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Thêm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Đánh dấu tất cả đã đọc") },
                            onClick = {
                                showMenu = false
                                onMarkAllRead()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DoneAll, contentDescription = null)
                            },
                            enabled = unreadCount > 0
                        )
                        DropdownMenuItem(
                            text = { Text("Cài đặt thông báo") },
                            onClick = {
                                showMenu = false
                                onSettings()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                    }
                }
                
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Quick Filter Chips - Scrollable Row
 */
@Composable
fun NotificationQuickFiltersRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        NotificationUiState.FILTER_ALL to "Tất cả",
        NotificationUiState.FILTER_UNREAD to "Chưa đọc",
        NotificationUiState.FILTER_ORDER to "Đơn hàng",
        NotificationUiState.FILTER_SYSTEM to "Hệ thống",
        NotificationUiState.FILTER_PROMOTION to "Khuyến mãi"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selectedFilter == filter
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { 
                    Text(
                        label, 
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    enabled = true,
                    selected = false
                )
            )
        }
    }
}

/**
 * Notification List With Filters - LazyColumn với Filters ở đầu để scroll cùng
 */
@Composable
private fun NotificationListWithFilters(
    notifications: List<Notification>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onNotificationClick: (Notification) -> Unit,
    showEmptyState: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Filter Chips - ở đầu list để scroll cùng
        item {
            NotificationQuickFiltersRow(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }
        
        // Empty state nếu cần
        if (showEmptyState) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = when (selectedFilter) {
                                NotificationUiState.FILTER_UNREAD -> "Không có thông báo chưa đọc"
                                NotificationUiState.FILTER_ORDER -> "Không có thông báo đơn hàng"
                                NotificationUiState.FILTER_SYSTEM -> "Không có thông báo hệ thống"
                                NotificationUiState.FILTER_PROMOTION -> "Không có thông báo khuyến mãi"
                                else -> "Không có thông báo nào"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Notification Items
            itemsIndexed(
                items = notifications,
                key = { _, notification -> notification.id }
            ) { index, notification ->
                NotificationItem(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
                
                // Divider - không hiển thị cho item cuối
                if (index < notifications.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * NotificationItem - Flat List Item giống Facebook/Instagram
 */
@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = !notification.read
    val iconInfo = getNotificationIconInfo(notification.type)
    
    // Background color cho unread notification
    val backgroundColor = if (isUnread) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    } else {
        Color.White
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar/Icon - hình tròn 48dp
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconInfo.second.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconInfo.first,
                contentDescription = null,
                tint = iconInfo.second,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title - Bold
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Body - Normal, màu phụ
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Time - font nhỏ, màu xám nhạt
            Text(
                text = formatNotificationTimeCompact(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        
        // Unread Indicator - chấm xanh bên phải
        if (isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/**
 * Loading State
 */
@Composable
private fun NotificationLoadingState() {
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
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Đang tải thông báo...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error State
 */
@Composable
private fun NotificationErrorState(
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
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Text(
                "Không thể tải thông báo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại")
            }
        }
    }
}

/**
 * Empty State
 */
@Composable
private fun NotificationEmptyState(selectedFilter: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Text(
                text = when (selectedFilter) {
                    NotificationUiState.FILTER_UNREAD -> "Không có thông báo chưa đọc"
                    NotificationUiState.FILTER_ORDER -> "Không có thông báo đơn hàng"
                    NotificationUiState.FILTER_SYSTEM -> "Không có thông báo hệ thống"
                    NotificationUiState.FILTER_PROMOTION -> "Không có thông báo khuyến mãi"
                    else -> "Không có thông báo nào"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                "Chúng tôi sẽ thông báo cho bạn khi có tin mới",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Settings Dialog
 */
@Composable
private fun NotificationSettingsDialog(
    preferences: com.example.foodapp.data.model.owner.notification.NotificationPreferences?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (informational: Boolean?, marketing: Boolean?) -> Unit
) {
    var informational by remember(preferences) { mutableStateOf(preferences?.informational ?: true) }
    var marketing by remember(preferences) { mutableStateOf(preferences?.marketing ?: true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Cài đặt thông báo", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (preferences == null && !isLoading) {
                    Text("Không thể tải cài đặt")
                } else {
                    SettingsToggleRow(
                        title = "Đơn hàng & Thanh toán",
                        subtitle = "Thông báo quan trọng, không thể tắt",
                        checked = true,
                        enabled = false,
                        onCheckedChange = {}
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    SettingsToggleRow(
                        title = "Cập nhật & Tổng kết",
                        subtitle = "Tổng kết doanh thu, cập nhật hệ thống",
                        checked = informational,
                        enabled = true,
                        onCheckedChange = { informational = it }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    SettingsToggleRow(
                        title = "Khuyến mãi",
                        subtitle = "Voucher mới, chương trình khuyến mãi",
                        checked = marketing,
                        enabled = true,
                        onCheckedChange = { marketing = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(informational, marketing) },
                enabled = !isLoading && preferences != null,
                shape = RoundedCornerShape(12.dp)
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

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
    }
}

// ==================== Helper Functions ====================

private fun getNotificationIconInfo(type: NotificationType): Pair<ImageVector, Color> {
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

private fun formatNotificationTimeCompact(timestamp: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp)
        
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 60000 -> "Vừa xong"
            diff < 3600000 -> "${diff / 60000}p"
            diff < 86400000 -> "${diff / 3600000}h"
            diff < 604800000 -> "${diff / 86400000}d"
            else -> {
                val displayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        "N/A"
    }
}
