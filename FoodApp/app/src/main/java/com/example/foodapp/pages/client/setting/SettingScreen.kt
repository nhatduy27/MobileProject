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
        factory = SettingsViewModel.Factory(context)
    )

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var showFinalDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) } // Thêm dialog xác nhận logout


    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // State cho Confirm Delete Dialog
    var agreeToTerms by remember { mutableStateOf(false) }

    // State cho Final Delete Dialog
    var deletePassword by remember { mutableStateOf("") }
    var showDeletePassword by remember { mutableStateOf(false) }

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
            // Tài khoản
            SettingsSection(title = "Tài khoản") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    description = "Thay đổi mật khẩu đăng nhập",
                    onClick = { showChangePasswordDialog = true }
                )

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Xóa tài khoản",
                    description = "Xóa vĩnh viễn tài khoản của bạn",
                    onClick = { showConfirmDeleteDialog = true },
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
                    onClick = { showLogoutConfirmDialog = true }, // Mở dialog xác nhận
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
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                        onLogout()
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
                    onClick = { showLogoutConfirmDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog đổi mật khẩu
    if (showChangePasswordDialog) {
        // Thêm state tracking
        val changePasswordState: ChangePasswordState? by viewModel.changePasswordState.observeAsState()

        Dialog(
            onDismissRequest = { showChangePasswordDialog = false },
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

                    // Hiển thị thông báo thành công
                    if (changePasswordState is ChangePasswordState.Success) {
                        val success = (changePasswordState as ChangePasswordState.Success).message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✅ $success",
                                color = Color(0xFF2E7D32), // Màu xanh lá
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

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

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showChangePasswordDialog = false
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        ) {
                            Text("Hủy")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.changePassword(currentPassword, newPassword)
                                // KHÔNG đóng dialog ngay, để hiển thị thông báo
                            },
                            enabled = currentPassword.isNotBlank() &&
                                    newPassword.isNotBlank() &&
                                    confirmPassword.isNotBlank() &&
                                    newPassword == confirmPassword
                        ) {
                            Text("Xác nhận")
                        }
                    }
                }
            }
        }

        // Tự động đóng dialog sau 2 giây khi thành công
        LaunchedEffect(changePasswordState) {
            if (changePasswordState is ChangePasswordState.Success) {
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            }
        }
    }

    // Dialog xác nhận xóa (bước 1)
    if (showConfirmDeleteDialog) {
        Dialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showConfirmDeleteDialog = false
                                agreeToTerms = false
                            }
                        ) {
                            Text("Hủy")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                showConfirmDeleteDialog = false
                                showFinalDeleteDialog = true
                                agreeToTerms = false
                            },
                            enabled = agreeToTerms,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Tiếp tục")
                        }
                    }
                }
            }
        }
    }

    // Dialog nhập mật khẩu xác nhận xóa (bước 2)
    if (showFinalDeleteDialog) {
        Dialog(
            onDismissRequest = { showFinalDeleteDialog = false },
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
                        text = "🔒 Xác nhận cuối cùng",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Vui lòng nhập mật khẩu của bạn để xác nhận xóa tài khoản:",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Mật khẩu hiện tại") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showDeletePassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showDeletePassword = !showDeletePassword }) {
                                Icon(
                                    if (showDeletePassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Hiện mật khẩu"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showFinalDeleteDialog = false
                                deletePassword = ""
                            }
                        ) {
                            Text("Hủy")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onDeleteAccount()
                                showFinalDeleteDialog = false
                                deletePassword = ""
                            },
                            enabled = deletePassword.isNotBlank(),
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