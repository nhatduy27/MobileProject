package com.example.foodapp.authentication.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.ui.theme.PrimaryOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPasswordScreen(
    onBackClicked: () -> Unit,
    onResetEmailSent: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.factory(context)
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Khi gửi email thành công
    LaunchedEffect(state) {
        if (state is ForgotPasswordState.Success) {
            // Delay để user đọc thông báo
            kotlinx.coroutines.delay(2000)
            onResetEmailSent()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quên mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        ForgotPasswordContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onSendResetEmail = { email ->
                viewModel.sendResetEmail(email)
            },
            onBackClicked = onBackClicked
        )
    }
}

@Composable
fun ForgotPasswordContent(
    modifier: Modifier = Modifier,
    state: ForgotPasswordState,
    onSendResetEmail: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    val isLoading = state is ForgotPasswordState.Sending
    val isSuccess = state is ForgotPasswordState.Success
    val errorMessage = when (state) {
        is ForgotPasswordState.Error -> state.message
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = PrimaryOrange,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Quên mật khẩu",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = "Nhập email của bạn để nhận link đặt lại mật khẩu",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Hiển thị lỗi
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x1AFF0000)
                )
            ) {
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Hiển thị thành công
        if (isSuccess) {
            val successEmail = (state as ForgotPasswordState.Success).email
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x1A4CAF50)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ Đã gửi email thành công!",
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Link đặt lại mật khẩu đã được gửi đến:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = successEmail,
                        fontSize = 14.sp,
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vui lòng kiểm tra hộp thư đến và spam",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Ô nhập email (chỉ hiện khi chưa thành công)
        if (!isSuccess) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                placeholder = { Text("Nhập email của bạn") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = {
                    emailError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button gửi email
            Button(
                onClick = {
                    // Validate
                    if (email.isEmpty() || !email.contains("@")) {
                        emailError = "Vui lòng nhập email hợp lệ"
                        return@Button
                    }

                    onSendResetEmail(email)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOrange,
                    disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f)
                ),
                enabled = email.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang gửi...")
                } else {
                    Text(
                        text = "Gửi link đặt lại mật khẩu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Thông tin hướng dẫn
        if (!isSuccess) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "• Link sẽ có hiệu lực trong 1 giờ\n• Kiểm tra cả hộp thư spam\n• Nếu không nhận được email, thử lại sau ít phút",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}