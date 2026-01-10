package com.example.foodapp.authentication.forgotpassword.resetpassword

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.ui.theme.PrimaryOrange

@Composable
fun ResetPasswordScreen(
    onBackClicked: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ResetPasswordViewModel = viewModel(
        factory = ResetPasswordViewModel.factory(context)
    )

    // Lấy email từ SharedPreferences
    val email = remember { getEmailFromPrefs(context) }

    // Khởi tạo email cho ViewModel
    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            viewModel.setEmail(email)
        }
    }

    // Observe states
    val state by viewModel.state.observeAsState(ResetPasswordState.Idle)
    val newPassword by viewModel.newPassword.observeAsState("")
    val confirmPassword by viewModel.confirmPassword.observeAsState("")
    val passwordError by viewModel.passwordError.observeAsState(null)
    val confirmPasswordError by viewModel.confirmPasswordError.observeAsState(null)

    // UI state
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Xử lý khi thành công
    LaunchedEffect(state) {
        if (state is ResetPasswordState.Success) {

            clearEmailFromPrefs(context)
            onSuccess()
        }
    }

    // Clear error khi người dùng bắt đầu nhập
    LaunchedEffect(newPassword, confirmPassword) {
        if (newPassword.isNotEmpty() && passwordError != null) {
            viewModel.setNewPassword(newPassword)
        }
        if (confirmPassword.isNotEmpty() && confirmPasswordError != null) {
            viewModel.setConfirmPassword(confirmPassword)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackClicked,
                enabled = state !is ResetPasswordState.Loading
            ) {
                Text(
                    "←",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Title
        Text(
            text = "Đặt lại mật khẩu",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )

        // Description với email (ẩn một phần email)
        if (email.isNotEmpty()) {
            val maskedEmail = maskEmail(email)
            Text(
                text = "Nhập mật khẩu mới cho tài khoản\n$maskedEmail",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            // Nếu không có email, hiển thị thông báo lỗi
            Text(
                text = "Không tìm thấy thông tin email",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Hiển thị lỗi từ server
        if (state is ResetPasswordState.Error) {
            val errorMessage = (state as ResetPasswordState.Error).message
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            )
        }

        // Kiểm tra nếu không có email, disable form
        val isFormDisabled = email.isEmpty() || state is ResetPasswordState.Loading

        // New Password Input
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newText ->
                viewModel.setNewPassword(newText)
            },
            enabled = !isFormDisabled,
            label = { Text("Mật khẩu mới") },
            placeholder = { Text("Ít nhất 6 ký tự") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            isError = passwordError != null,
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                if (!isFormDisabled) {
                    IconButton(
                        onClick = { newPasswordVisible = !newPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (newPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (newPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                }
            }
        )

        // New Password Error
        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Input
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { newText ->
                viewModel.setConfirmPassword(newText)
            },
            enabled = !isFormDisabled,
            label = { Text("Xác nhận mật khẩu") },
            placeholder = { Text("Nhập lại mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            isError = confirmPasswordError != null,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                if (!isFormDisabled) {
                    IconButton(
                        onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                }
            }
        )

        // Confirm Password Error
        if (confirmPasswordError != null) {
            Text(
                text = confirmPasswordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        Button(
            onClick = {
                viewModel.resetPassword(newPassword, confirmPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormDisabled) Color.Gray else PrimaryOrange
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isFormDisabled &&
                    newPassword.isNotEmpty() &&
                    confirmPassword.isNotEmpty()
        ) {
            if (state is ResetPasswordState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang xử lý...")
            } else {
                Text(
                    "Đặt lại mật khẩu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Password Requirements
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Yêu cầu mật khẩu:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = if (newPassword.length >= 6) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.Error
                        },
                        contentDescription = null,
                        tint = if (newPassword.length >= 6) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ít nhất 6 ký tự",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (newPassword.length >= 6) Color(0xFF4CAF50) else Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = if (newPassword == confirmPassword && confirmPassword.isNotEmpty()) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.Error
                        },
                        contentDescription = null,
                        tint = if (newPassword == confirmPassword && confirmPassword.isNotEmpty()) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mật khẩu khớp",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (newPassword == confirmPassword && confirmPassword.isNotEmpty()) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        }

        // Thông báo nếu không có email
        if (email.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚠ Không tìm thấy email. Vui lòng quay lại và thử lại.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

// Hàm lấy email từ SharedPreferences
private fun getEmailFromPrefs(context: Context): String {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("reset_password_email", "") ?: ""
}

// Hàm xóa email khỏi SharedPreferences
private fun clearEmailFromPrefs(context: Context) {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    sharedPref.edit()
        .remove("reset_password_email")
        .apply()
}

// Hàm mask email để bảo mật (ví dụ: te****@gmail.com)
private fun maskEmail(email: String): String {
    return try {
        val atIndex = email.indexOf('@')
        if (atIndex > 3) {
            val prefix = email.substring(0, 3)
            val domain = email.substring(atIndex)
            "$prefix***$domain"
        } else {
            // Nếu email quá ngắn, trả về nguyên bản
            email
        }
    } catch (e: Exception) {
        email
    }
}