package com.example.foodapp.pages.client.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(context)
    )

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // State cho Confirm Delete Dialog
    var agreeToTerms by remember { mutableStateOf(false) }

    // THÊM MỚI: State cho notification settings
    var transactionalEnabled by remember { mutableStateOf(false) }
    var informationalEnabled by remember { mutableStateOf(false) }
    var marketingEnabled by remember { mutableStateOf(false) }

    // Observe state từ ViewModel
    val deleteAccountState by viewModel.deleteAccountState.observeAsState()
    val changePasswordState by viewModel.changePasswordState.observeAsState()
    val logoutState by viewModel.logoutState.observeAsState()
    val navigateToLogin by viewModel.navigateToLogin.observeAsState()

    // THÊM MỚI: Observe notification preferences state
    val notificationPreferencesState by viewModel.notificationPreferencesState.observeAsState()
    val notificationPreferences by viewModel.notificationPreferences.observeAsState()

    // THÊM MỚI: Load notification preferences khi screen loads
    LaunchedEffect(Unit) {
        viewModel.loadNotificationPreferences()
    }

    // THÊM MỚI: Update local states khi notification preferences được load
    LaunchedEffect(notificationPreferences) {
        notificationPreferences?.let { prefs ->
            transactionalEnabled = prefs.transactional
            informationalEnabled = prefs.informational
            marketingEnabled = prefs.marketing
        }
    }

    // Xử lý khi logout thành công
    LaunchedEffect(logoutState) {
        if (logoutState == true) {
            println("DEBUG [SettingsScreen] Logout successful, navigating...")
            onLogout()
        }
    }

    // Xử lý khi xóa tài khoản thành công
    LaunchedEffect(deleteAccountState) {
        when (deleteAccountState) {
            is DeleteAccountState.Success -> {
                val message = (deleteAccountState as DeleteAccountState.Success).message
                println("DEBUG [SettingsScreen] Delete account success: $message")

                // Delay để hiển thị message trước khi navigate
                delay(1500)

                // Đóng dialog
                showConfirmDeleteDialog = false

                // Điều hướng về màn hình đăng nhập
                onDeleteAccount()
            }
            is DeleteAccountState.Error -> {
                val error = (deleteAccountState as DeleteAccountState.Error).message
                println("DEBUG [SettingsScreen] Delete account error: $error")
            }
            is DeleteAccountState.Loading -> {
                println("DEBUG [SettingsScreen] Delete account loading...")
            }
            else -> {}
        }
    }

    // Xử lý navigate to login khi cần
    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin == true) {
            println("DEBUG [SettingsScreen] Navigate to login triggered")
            onDeleteAccount()
            viewModel.resetNavigateToLogin()
        }
    }

    // Xử lý change password success
    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is ChangePasswordState.Success -> {
                val message = (changePasswordState as ChangePasswordState.Success).message
                println("DEBUG [SettingsScreen] Change password success: $message")

                // Tự động đóng dialog sau 1.5 giây
                delay(1500)
                showChangePasswordDialog = false
            }
            is ChangePasswordState.Error -> {
                val error = (changePasswordState as ChangePasswordState.Error).message
                println("DEBUG [SettingsScreen] Change password error: $error")
            }
            else -> {}
        }
    }

    // Reset password state khi đóng dialog
    LaunchedEffect(showChangePasswordDialog) {
        if (!showChangePasswordDialog) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            // Reset state trong ViewModel
            viewModel.resetDeleteAccountState()
        }
    }

    // Reset dialog state khi đóng
    LaunchedEffect(showConfirmDeleteDialog) {
        if (!showConfirmDeleteDialog) {
            agreeToTerms = false
            viewModel.resetDeleteAccountState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // THÊM MỚI: Notification Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cài đặt thông báo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Show loading indicator khi đang tải
                        when (notificationPreferencesState) {
                            is NotificationPreferencesState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            is NotificationPreferencesState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NotificationSettingItem(
                        title = "Thông báo thông tin",
                        description = "Cập nhật, thông báo từ hệ thống",
                        checked = informationalEnabled,
                        onCheckedChange = {
                            informationalEnabled = it
                            viewModel.updateNotificationPreferences(informational = it)
                        },
                        enabled = notificationPreferences != null
                    )

                    NotificationSettingItem(
                        title = "Thông báo khuyến mãi",
                        description = "Khuyến mãi, ưu đãi đặc biệt",
                        checked = marketingEnabled,
                        onCheckedChange = {
                            marketingEnabled = it
                            viewModel.updateNotificationPreferences(marketing = it)
                        },
                        enabled = notificationPreferences != null
                    )

                    // Show error message nếu có lỗi
                    when (notificationPreferencesState) {
                        is NotificationPreferencesState.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (notificationPreferencesState as NotificationPreferencesState.Error).message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )

                            // Retry button nếu có lỗi
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.loadNotificationPreferences() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Thử lại")
                            }
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tài khoản
            SettingsSection(title = "Tài khoản") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    description = "Thay đổi mật khẩu đăng nhập",
                    onClick = {
                        println("DEBUG [SettingsScreen] Change password clicked")
                        showChangePasswordDialog = true
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Xóa tài khoản",
                    description = "Xóa vĩnh viễn tài khoản của bạn",
                    onClick = {
                        println("DEBUG [SettingsScreen] Delete account clicked")
                        showConfirmDeleteDialog = true
                    },
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hệ thống
            SettingsSection(title = "Hệ thống") {
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = "Đăng xuất",
                    description = "Đăng xuất khỏi tài khoản hiện tại",
                    onClick = {
                        println("DEBUG [SettingsScreen] Logout clicked")
                        showLogoutConfirmDialog = true
                    },
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ==================== DIALOGS ====================

    // Dialog xác nhận đăng xuất
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Xác nhận đăng xuất",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này không?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        println("DEBUG [SettingsScreen] Logout confirmed")
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        println("DEBUG [SettingsScreen] Logout cancelled")
                        showLogoutConfirmDialog = false
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog đổi mật khẩu
    if (showChangePasswordDialog) {
        Dialog(
            onDismissRequest = {
                println("DEBUG [SettingsScreen] Change password dialog dismissed")
                showChangePasswordDialog = false
                viewModel.resetDeleteAccountState()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Đổi mật khẩu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Hiển thị thông báo
                    when (changePasswordState) {
                        is ChangePasswordState.Success -> {
                            val success = (changePasswordState as ChangePasswordState.Success).message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✅ $success",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is ChangePasswordState.Error -> {
                            val error = (changePasswordState as ChangePasswordState.Error).message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "❌ $error",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        else -> {}
                    }

                    // Hiển thị loading
                    if (changePasswordState is ChangePasswordState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // Chỉ hiển thị form khi không loading/success/error
                    if (changePasswordState !is ChangePasswordState.Loading &&
                        changePasswordState !is ChangePasswordState.Success &&
                        changePasswordState !is ChangePasswordState.Error
                    ) {
                        // Mật khẩu hiện tại
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Mật khẩu hiện tại") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Hiện mật khẩu"
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mật khẩu mới
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Hiện mật khẩu"
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Xác nhận mật khẩu
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Hiện mật khẩu"
                                    )
                                }
                            }
                        )

                        // Kiểm tra mật khẩu
                        if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text(
                                text = "Mật khẩu không khớp",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nếu đang loading/success/error, chỉ hiển thị nút đóng
                        if (changePasswordState is ChangePasswordState.Loading ||
                            changePasswordState is ChangePasswordState.Success ||
                            changePasswordState is ChangePasswordState.Error
                        ) {
                            Button(
                                onClick = {
                                    showChangePasswordDialog = false
                                    viewModel.resetDeleteAccountState()
                                }
                            ) {
                                Text("Đóng")
                            }
                        } else {
                            // Trạng thái bình thường
                            TextButton(
                                onClick = {
                                    showChangePasswordDialog = false
                                    viewModel.resetDeleteAccountState()
                                }
                            ) {
                                Text("Hủy")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    println("DEBUG [SettingsScreen] Confirm change password clicked")
                                    viewModel.changePassword(currentPassword, newPassword)
                                },
                                enabled = currentPassword.isNotBlank() &&
                                        newPassword.isNotBlank() &&
                                        confirmPassword.isNotBlank() &&
                                        newPassword == confirmPassword &&
                                        newPassword.length >= 6
                            ) {
                                if (changePasswordState is ChangePasswordState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Xác nhận")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa tài khoản
    if (showConfirmDeleteDialog) {
        viewModel.showDeleteAccountConfirmation()
        Dialog(
            onDismissRequest = {
                println("DEBUG [SettingsScreen] Delete dialog dismissed")
                showConfirmDeleteDialog = false
                viewModel.resetDeleteAccountState()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "⚠️ Cảnh báo",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Xóa tài khoản",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Hiển thị thông báo từ ViewModel
                    when (deleteAccountState) {
                        is DeleteAccountState.Success -> {
                            val success = (deleteAccountState as DeleteAccountState.Success).message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✅ $success",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is DeleteAccountState.Error -> {
                            val error = (deleteAccountState as DeleteAccountState.Error).message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "❌ $error",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        else -> {}
                    }

                    // Hiển thị loading
                    if (deleteAccountState is DeleteAccountState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // Chỉ hiển thị content khi không ở trạng thái loading/success/error
                    if (deleteAccountState !is DeleteAccountState.Loading &&
                        deleteAccountState !is DeleteAccountState.Success &&
                        deleteAccountState !is DeleteAccountState.Error
                    ) {
                        Text(
                            text = "Bạn có chắc chắn muốn xóa tài khoản này? Hành động này sẽ:",
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                        ) {
                            Text("• Xóa vĩnh viễn tài khoản của bạn")
                            Text("• Xóa tất cả dữ liệu liên quan")
                            Text("• Không thể khôi phục lại")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Checkbox(
                                checked = agreeToTerms,
                                onCheckedChange = { agreeToTerms = it }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Tôi hiểu hậu quả và muốn xóa tài khoản",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nếu đang loading hoặc đã thành công/lỗi, chỉ hiển thị nút đóng
                        if (deleteAccountState is DeleteAccountState.Loading ||
                            deleteAccountState is DeleteAccountState.Success ||
                            deleteAccountState is DeleteAccountState.Error
                        ) {
                            Button(
                                onClick = {
                                    showConfirmDeleteDialog = false
                                    viewModel.resetDeleteAccountState()
                                }
                            ) {
                                Text("Đóng")
                            }
                        } else {
                            // Trạng thái bình thường
                            TextButton(
                                onClick = {
                                    println("DEBUG [SettingsScreen] Delete cancelled")
                                    showConfirmDeleteDialog = false
                                    viewModel.resetDeleteAccountState()
                                }
                            ) {
                                Text("Hủy")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    println("DEBUG [SettingsScreen] Confirm delete account clicked")
                                    viewModel.confirmDeleteAccount()
                                    onDeleteAccount()
                                },
                                enabled = agreeToTerms,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("Xóa tài khoản")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            description?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// THÊM MỚI: NotificationSettingItem Composable
@Composable
fun NotificationSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}