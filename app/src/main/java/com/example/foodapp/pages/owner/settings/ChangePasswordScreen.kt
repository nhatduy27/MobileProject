package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavHostController) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF333333)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "ℹ️", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Yêu cầu mật khẩu mới:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Tối thiểu 8 ký tự\n• Chứa chữ hoa và chữ thường\n• Chứa ít nhất 1 số",
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Password Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Current Password
                    PasswordField(
                        label = "Mật khẩu hiện tại",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        passwordVisible = currentPasswordVisible,
                        onVisibilityChange = { currentPasswordVisible = it },
                        icon = "🔒"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // New Password
                    PasswordField(
                        label = "Mật khẩu mới",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        passwordVisible = newPasswordVisible,
                        onVisibilityChange = { newPasswordVisible = it },
                        icon = "🔑"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // Confirm Password
                    PasswordField(
                        label = "Xác nhận mật khẩu mới",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        passwordVisible = confirmPasswordVisible,
                        onVisibilityChange = { confirmPasswordVisible = it },
                        icon = "✅"
                    )
                }
            }

            // Error/Success Messages
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }

            if (successMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF388E3C),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // Change Password Button
            Button(
                onClick = {
                    errorMessage = ""
                    successMessage = ""

                    when {
                        currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                            errorMessage = "Vui lòng điền đầy đủ thông tin"
                        }
                        newPassword.length < 8 -> {
                            errorMessage = "Mật khẩu mới phải có ít nhất 8 ký tự"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Mật khẩu xác nhận không khớp"
                        }
                        else -> {
                            isLoading = true
                            val user = FirebaseAuth.getInstance().currentUser
                            val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
                            
                            user?.reauthenticate(credential)?.addOnCompleteListener { reauth ->
                                if (reauth.isSuccessful) {
                                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            successMessage = "Đổi mật khẩu thành công!"
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                        } else {
                                            errorMessage = "Đổi mật khẩu thất bại: ${task.exception?.message}"
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Mật khẩu hiện tại không đúng"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Đổi mật khẩu", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    icon: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { onVisibilityChange(!passwordVisible) }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF6B35),
                unfocusedBorderColor = Color(0xFFEEEEEE)
            ),
            singleLine = true
        )
    }
}
