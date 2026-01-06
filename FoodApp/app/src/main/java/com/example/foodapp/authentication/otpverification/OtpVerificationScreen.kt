package com.example.foodapp.authentication.otpverification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OtpVerificationScreen(
    onVerificationSuccess: () -> Unit,
    onBackClicked: () -> Unit,
    onResendRequest: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: OtpVerificationViewModel = viewModel(
        factory = OtpVerificationViewModel.factory(context)
    )

    val otpState by viewModel.otpState.observeAsState(OtpVerificationState.Idle)
    val remainingTime by viewModel.remainingTime.observeAsState(0)
    val userEmail by viewModel.userEmail.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getCurrentUserEmail()
    }

    LaunchedEffect(otpState) {
        if (otpState is OtpVerificationState.Success) {
            onVerificationSuccess()
        }
    }

    OtpVerificationContent(
        email = userEmail ?: "Đang tải email...",
        otpState = otpState,
        remainingTime = remainingTime,
        onVerifyOtp = { otp ->
            viewModel.verifyOtp(otp)
        },
        onResendOtp = {
            viewModel.resendOtp()
            onResendRequest()
        },
        onBackClicked = {
            viewModel.resetState()
            onBackClicked()
        }
    )
}

@Composable
fun OtpVerificationContent(
    email: String,
    otpState: OtpVerificationState,
    remainingTime: Int,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBackClicked: () -> Unit
) {
    var otpCode by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isLoading = otpState is OtpVerificationState.Verifying ||
            otpState is OtpVerificationState.Sending
    val isSuccess = otpState is OtpVerificationState.Success
    val errorMessage = when (otpState) {
        is OtpVerificationState.Error -> otpState.message
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        IconButton(
            onClick = {
                if (!isLoading) onBackClicked()
            },
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = if (isLoading) Color.Gray else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            "Xác thực Email",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email display
        Text(
            "Chúng tôi đã gửi mã OTP 6 số đến:",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )

        Text(
            email,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // OTP Input
        TextField(
            value = otpCode,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    otpCode = newValue
                    if (newValue.length == 6) {
                        onVerifyOtp(newValue)
                        keyboardController?.hide()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Nhập mã OTP 6 số", color = Color.Gray)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpCode.length == 6) onVerifyOtp(otpCode)
                    keyboardController?.hide()
                }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            enabled = !isLoading && !isSuccess
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Timer/Resend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (remainingTime > 0) {
                val minutes = remainingTime / 60
                val seconds = remainingTime % 60
                Text("Gửi lại mã sau: ", color = Color(0xFF666666))
                Text(
                    String.format("%02d:%02d", minutes, seconds),
                    color = Color(0xFFFF6B35),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text("Không nhận được mã? ", color = Color(0xFF666666))
                Text(
                    "Gửi lại",
                    color = if (isLoading) Color.Gray else Color(0xFFFF6B35),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        enabled = !isLoading,
                        onClick = {
                            onResendOtp()
                            otpCode = ""
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Verify Button
        Button(
            onClick = {
                if (otpCode.length == 6 && !isLoading && !isSuccess) {
                    onVerifyOtp(otpCode)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = otpCode.length == 6 && !isLoading && !isSuccess,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang xác thực...")
            } else if (isSuccess) {
                Text("Xác thực thành công!")
            } else {
                Text("Xác thực", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instruction
        Text(
            "Nếu không nhận được mã OTP, vui lòng kiểm tra hộp thư spam\nhoặc thử lại sau ít phút.",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}