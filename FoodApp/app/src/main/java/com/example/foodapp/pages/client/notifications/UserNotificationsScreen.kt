// NotificationsScreen.kt
package com.example.foodapp.pages.client.notifications

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.notification.NotificationResponse
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (notificationId: String, notification: NotificationResponse) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.factory(context)
    )

    // Observe các state từ ViewModel
    val notificationsState by viewModel.notificationsState.observeAsState()
    val currentNotifications by viewModel.currentNotifications.observeAsState()
    val unreadCount by viewModel.unreadCount.observeAsState(0)
    val markReadState by viewModel.markReadState.observeAsState()
    val markAllReadState by viewModel.markAllReadState.observeAsState()

    // Local state
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var showFilterDropdown by remember { mutableStateOf(false) }
    var showMarkAllDialog by remember { mutableStateOf(false) }

    // Xử lý kết quả đánh dấu đã đọc
    LaunchedEffect(markReadState) {
        when (markReadState) {
            is MarkReadState.Success -> {
                delay(2000)
                viewModel.resetMarkReadState()
            }
            is MarkReadState.Error -> {
                delay(3000)
                viewModel.resetMarkReadState()
            }
            else -> {}
        }
    }

    // Xử lý kết quả đánh dấu tất cả đã đọc
    LaunchedEffect(markAllReadState) {
        when (markAllReadState) {
            is MarkAllReadState.Success -> {
                delay(2000)
                viewModel.resetMarkAllReadState()
            }
            is MarkAllReadState.Error -> {
                delay(3000)
                viewModel.resetMarkAllReadState()
            }
            else -> {}
        }
    }

    // Hiển thị loading/error/success states
    LaunchedEffect(notificationsState) {
        when (notificationsState) {
            is NotificationsState.Success -> {
                println("DEBUG: Notifications loaded successfully")
            }
            is NotificationsState.Error -> {
                val error = (notificationsState as NotificationsState.Error).message
                println("DEBUG: Error loading notifications: $error")
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                stringResource(R.string.notifications_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AnimatedContent(
                                    targetState = unreadCount,
                                    transitionSpec = {
                                        (slideInVertically { -it } + fadeIn()).togetherWith(
                                            slideOutVertically { it } + fadeOut()
                                        )
                                    },
                                    label = "badge"
                                ) { count ->
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .shadow(4.dp, CircleShape)
                                    ) {
                                        Text(
                                            text = if (count > 9) "9+" else "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back_button),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Nút đánh dấu tất cả đã đọc (chỉ hiển thị khi có thông báo chưa đọc)
                        if (unreadCount > 0) {
                            IconButton(
                                onClick = { showMarkAllDialog = true },
                                enabled = markAllReadState !is MarkAllReadState.Loading
                            ) {
                                if (markAllReadState is MarkAllReadState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = stringResource(R.string.mark_all_read_button),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Filter button with badge
                        Box {
                            BadgedBox(
                                badge = {
                                    if (selectedFilter != NotificationFilter.ALL) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(8.dp)
                                        )
                                    }
                                }
                            ) {
                                IconButton(onClick = { showFilterDropdown = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = stringResource(R.string.filter_notifications_button),
                                        tint = if (selectedFilter != NotificationFilter.ALL) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }

                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall = RoundedCornerShape(16.dp)
                                )
                            ) {
                                DropdownMenu(
                                    expanded = showFilterDropdown,
                                    onDismissRequest = { showFilterDropdown = false },
                                    modifier = Modifier
                                        .widthIn(min = 200.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Text(
                                        stringResource(R.string.filter_notifications_dialog_title),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    NotificationFilter.entries.forEach { filter ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(
                                                        imageVector = filter.icon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp),
                                                        tint = if (selectedFilter == filter) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        stringResource(filter.displayNameRes),
                                                        fontWeight = if (selectedFilter == filter) {
                                                            FontWeight.SemiBold
                                                        } else {
                                                            FontWeight.Normal
                                                        },
                                                        color = if (selectedFilter == filter) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    if (selectedFilter == filter) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(20.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedFilter = filter
                                                showFilterDropdown = false
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Refresh button
                        IconButton(
                            onClick = { viewModel.refreshNotifications() },
                            enabled = notificationsState !is NotificationsState.Loading
                        ) {
                            if (notificationsState is NotificationsState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.refresh_button),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        floatingActionButton = {
            // Floating Action Button để đánh dấu tất cả đã đọc
            if (unreadCount > 0 && currentNotifications?.isNotEmpty() == true) {
                AnimatedVisibility(
                    visible = unreadCount > 0,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showMarkAllDialog = true },
                        icon = {
                            if (markAllReadState is MarkAllReadState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = null
                                )
                            }
                        },
                        text = {
                            Text(
                                stringResource(R.string.mark_all_read),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Nội dung chính
                when (val state = notificationsState) {
                    is NotificationsState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(R.string.loading_notifications),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is NotificationsState.Error -> {
                        val error = (state as NotificationsState.Error).message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                // Error icon with gradient background
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = stringResource(R.string.error_content_description),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Text(
                                    text = stringResource(R.string.load_notifications_error_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )

                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Button(
                                    onClick = { viewModel.refreshNotifications() },
                                    modifier = Modifier.padding(top = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.retry),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    is NotificationsState.Success -> {
                        val notifications = currentNotifications ?: emptyList()

                        // Lọc theo loại nếu cần
                        val filteredNotifications = when (selectedFilter) {
                            NotificationFilter.ALL -> notifications
                            NotificationFilter.UNREAD -> notifications.filter { !it.read }
                            NotificationFilter.ORDER -> notifications.filter {
                                it.type?.contains("ORDER", ignoreCase = true) == true
                            }
                            NotificationFilter.SYSTEM -> notifications.filter {
                                it.type?.contains("SYSTEM", ignoreCase = true) == true
                            }
                        }

                        // Kiểm tra nếu danh sách rỗng
                        if (filteredNotifications.isEmpty()) {
                            EmptyNotificationsView(selectedFilter = selectedFilter)
                        } else {
                            // Hiển thị danh sách thông báo
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = filteredNotifications,
                                    key = { it.id ?: it.hashCode().toString() }
                                ) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onNotificationClick = {
                                            // Đánh dấu thông báo đã đọc khi click
                                            notification.id?.let { notificationId ->
                                                if (!notification.read) {
                                                    viewModel.markNotificationAsRead(notificationId)
                                                }
                                            }
                                            // Gọi callback để xử lý navigation nếu cần
                                            onNotificationClick(notification.id ?: "", notification)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // State ban đầu hoặc idle
                        if (notificationsState == null) {
                            EmptyNotificationsView(selectedFilter = NotificationFilter.ALL)
                        }
                    }
                }
            }

            // Hiển thị Snackbar khi đánh dấu đã đọc thành công
            AnimatedVisibility(
                visible = markReadState is MarkReadState.Success ||
                        markReadState is MarkReadState.Error ||
                        markAllReadState is MarkAllReadState.Success ||
                        markAllReadState is MarkAllReadState.Error,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                when {
                    markReadState is MarkReadState.Success -> {
                        Snackbar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.marked_as_read_success),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    markAllReadState is MarkAllReadState.Success -> {
                        val updatedCount = (markAllReadState as MarkAllReadState.Success).updatedCount
                        Snackbar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.mark_all_read_success, updatedCount),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    markReadState is MarkReadState.Error -> {
                        val error = (markReadState as MarkReadState.Error).message
                        Snackbar(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.mark_as_read_error, error),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    markAllReadState is MarkAllReadState.Error -> {
                        val error = (markAllReadState as MarkAllReadState.Error).message
                        Snackbar(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.mark_all_read_error, error),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog xác nhận đánh dấu tất cả đã đọc
    if (showMarkAllDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllDialog = false },
            title = {
                Text(
                    stringResource(R.string.mark_all_read_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.mark_all_read_dialog_message, unreadCount))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllNotificationsAsRead()
                        showMarkAllDialog = false
                    },
                    enabled = markAllReadState !is MarkAllReadState.Loading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (markAllReadState is MarkAllReadState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMarkAllDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun NotificationItem(
    notification: NotificationResponse,
    onNotificationClick: () -> Unit,
    context: Context = LocalContext.current
) {
    val isUnread = !notification.read

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onNotificationClick,
                indication = null
            )
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
            // Icon theo loại thông báo với gradient background
            val (icon, iconColor, gradientColors) = when {
                notification.type?.contains("ORDER", ignoreCase = true) == true ->
                    Triple(
                        Icons.Default.ShoppingBag,
                        MaterialTheme.colorScheme.primary,
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                notification.type?.contains("SYSTEM", ignoreCase = true) == true ->
                    Triple(
                        Icons.Default.Info,
                        MaterialTheme.colorScheme.secondary,
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                    )
                notification.type?.contains("PROMOTION", ignoreCase = true) == true ->
                    Triple(
                        Icons.Default.LocalOffer,
                        Color(0xFF4CAF50),
                        listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                            Color(0xFF4CAF50).copy(alpha = 0.05f)
                        )
                    )
                else ->
                    Triple(
                        Icons.Default.Notifications,
                        MaterialTheme.colorScheme.tertiary,
                        listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                        )
                    )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(colors = gradientColors)
                    )
                    .shadow(
                        elevation = if (isUnread) 4.dp else 2.dp,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = notification.type,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nội dung thông báo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title ?: stringResource(R.string.notification_default_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.body ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata với divider
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thời gian với icon
                    notification.createdAt?.let { createdAt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatNotificationTime(createdAt, context),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Trạng thái đọc/chưa đọc
                    if (isUnread) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.shadow(2.dp, RoundedCornerShape(8.dp))
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
                                    text = stringResource(R.string.notification_status_new),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.notification_status_read),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView(selectedFilter: NotificationFilter) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            // Empty icon with gradient background
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
                    contentDescription = stringResource(R.string.empty_notifications_content_description),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = when (selectedFilter) {
                    NotificationFilter.ALL -> stringResource(R.string.empty_notifications_all)
                    NotificationFilter.UNREAD -> stringResource(R.string.empty_notifications_unread)
                    NotificationFilter.ORDER -> stringResource(R.string.empty_notifications_order)
                    NotificationFilter.SYSTEM -> stringResource(R.string.empty_notifications_system)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = stringResource(R.string.empty_notifications_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Helper function để format thời gian
private fun formatNotificationTime(timestamp: String, context: Context): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        when {
            diff < 60000 -> context.getString(R.string.just_now)
            diff < 3600000 -> context.getString(R.string.minutes_ago, (diff / 60000).toInt())
            diff < 86400000 -> context.getString(R.string.hours_ago, (diff / 3600000).toInt())
            diff < 604800000 -> context.getString(R.string.days_ago, (diff / 86400000).toInt())
            else -> {
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        context.getString(R.string.unknown_time)
    }
}

enum class NotificationFilter(val displayNameRes: Int, val icon: ImageVector) {
    ALL(R.string.filter_all, Icons.Default.Notifications),
    UNREAD(R.string.filter_unread, Icons.Default.MarkEmailUnread),
    ORDER(R.string.filter_order, Icons.Default.ShoppingBag),
    SYSTEM(R.string.filter_system, Icons.Default.Info)
}