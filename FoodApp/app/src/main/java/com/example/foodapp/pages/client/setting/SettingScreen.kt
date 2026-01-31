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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.utils.LocaleHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLanguageChanged: () -> Unit  // THÊM: callback khi ngôn ngữ thay đổi
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

    // THÊM: State cho ngôn ngữ
    val isVietnamese = remember { mutableStateOf(LocaleHelper.isVietnamese(context)) }

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

    // THÊM: Cập nhật state ngôn ngữ khi context thay đổi
    LaunchedEffect(context) {
        isVietnamese.value = LocaleHelper.isVietnamese(context)
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
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
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
                            text = stringResource(R.string.notification_settings_title),
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
                                    contentDescription = stringResource(R.string.error),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NotificationSettingItem(
                        title = stringResource(R.string.info_notification_title),
                        description = stringResource(R.string.info_notification_desc),
                        checked = informationalEnabled,
                        onCheckedChange = {
                            informationalEnabled = it
                            viewModel.updateNotificationPreferences(informational = it)
                        },
                        enabled = notificationPreferences != null
                    )

                    NotificationSettingItem(
                        title = stringResource(R.string.marketing_notification_title),
                        description = stringResource(R.string.marketing_notification_desc),
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
                                Text(stringResource(R.string.retry))
                            }
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THÊM: Language Setting Section
            SettingsSection(title = stringResource(R.string.language_settings)) {
                LanguageSettingItem(
                    isVietnamese = isVietnamese.value,
                    onLanguageChange = {
                        LocaleHelper.toggleLanguage(context)
                        isVietnamese.value = !isVietnamese.value
                        onLanguageChanged()  // Gọi callback để restart activity
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tài khoản
            SettingsSection(title = stringResource(R.string.account_section_title)) {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.change_password_title),
                    description = stringResource(R.string.change_password_desc),
                    onClick = {
                        println("DEBUG [SettingsScreen] Change password clicked")
                        showChangePasswordDialog = true
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.delete_account_title),
                    description = stringResource(R.string.delete_account_desc),
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
            SettingsSection(title = stringResource(R.string.system_section_title)) {
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = stringResource(R.string.logout_title),
                    description = stringResource(R.string.logout_desc),
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
                    text = stringResource(R.string.logout_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirm_message),
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
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        println("DEBUG [SettingsScreen] Logout cancelled")
                        showLogoutConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
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
                        text = stringResource(R.string.change_password_dialog_title),
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
                            label = { Text(stringResource(R.string.current_password)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.show_password)
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mật khẩu mới
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(stringResource(R.string.new_password)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.show_password)
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Xác nhận mật khẩu
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(stringResource(R.string.confirm_new_password)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.show_password)
                                    )
                                }
                            }
                        )

                        // Kiểm tra mật khẩu
                        if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text(
                                text = stringResource(R.string.password_mismatch),
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
                                Text(stringResource(R.string.close))
                            }
                        } else {
                            // Trạng thái bình thường
                            TextButton(
                                onClick = {
                                    showChangePasswordDialog = false
                                    viewModel.resetDeleteAccountState()
                                }
                            ) {
                                Text(stringResource(R.string.cancel))
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
                                    Text(stringResource(R.string.confirm))
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
                        text = "⚠️ ${stringResource(R.string.warning)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(R.string.delete_account_dialog_title),
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
                            text = stringResource(R.string.delete_account_warning_message),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                        ) {
                            Text(stringResource(R.string.delete_account_point_1))
                            Text(stringResource(R.string.delete_account_point_2))
                            Text(stringResource(R.string.delete_account_point_3))
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
                                text = stringResource(R.string.delete_account_terms_confirmation),
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
                                Text(stringResource(R.string.close))
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
                                Text(stringResource(R.string.cancel))
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
                                Text(stringResource(R.string.delete_account_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

// THÊM: LanguageSettingItem Composable
@Composable
fun LanguageSettingItem(
    isVietnamese: Boolean,
    onLanguageChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLanguageChange)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(R.string.language),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isVietnamese) {
                        stringResource(R.string.language_vietnamese)
                    } else {
                        stringResource(R.string.language_english)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isVietnamese) "VI" else "EN",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isVietnamese,
                onCheckedChange = { onLanguageChange() }
            )
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