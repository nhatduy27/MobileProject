package com.example.foodapp.authentication.forgotpassword.verifyotp

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.ui.theme.PrimaryOrange

@Composable
fun ForgotPasswordOTPScreen(
    onBackClicked: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: VerifyOTPViewModel = viewModel( // ĐÃ SỬA TÊN
        factory = VerifyOTPViewModel.factory(context) // ĐÃ SỬA TÊN
    )

    // Lấy email từ SharedPreferences
    val email = remember { getEmailFromPrefs(context) }

    // State observables
    val otpState by viewModel.otpState.observeAsState(OtpVerificationState.Idle) // ĐÃ SỬA: Idle thay vì LoadingEmail
    val remainingTime by viewModel.remainingTime.observeAsState(0)
    val userEmail by viewModel.userEmail.observeAsState(null)

    // Khởi tạo email cho ViewModel
    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            viewModel.setEmail(email)
            viewModel.sendOtpResetPassword(email)
        }
    }

    // Xử lý khi thành công
    LaunchedEffect(otpState) {
        if (otpState is OtpVerificationState.Success) {
            onSuccess()
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                enabled = otpState !is OtpVerificationState.Verifying &&
                        otpState !is OtpVerificationState.Sending
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
            text = "Xác thực OTP",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )

        // Description với email
        Text(
            text = "Nhập mã OTP 6 số đã gửi đến",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Hiển thị lỗi
        if (otpState is OtpVerificationState.Error) {
            val errorMessage = (otpState as OtpVerificationState.Error).message
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

        // OTP Input với 6 ô riêng biệt
        var otpText by rememberSaveable { mutableStateOf("") }

        // Auto verify when OTP is complete
        LaunchedEffect(otpText) {
            if (otpText.length == 6 &&
                otpState !is OtpVerificationState.Verifying &&
                otpState !is OtpVerificationState.Sending &&
                remainingTime > 0) {
                viewModel.verifyOtp(otpText)
            }
        }

        SixDigitOtpInput(
            otpText = otpText,
            onOtpTextChange = { newOtp ->
                otpText = newOtp
            },
            enabled = otpState !is OtpVerificationState.Verifying &&
                    otpState !is OtpVerificationState.Sending &&
                    remainingTime > 0,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Timer
        val minutes = remainingTime / 60
        val seconds = remainingTime % 60
        val timerText = if (remainingTime > 0) {
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "00:00"
        }

        Text(
            text = "Mã OTP hết hạn sau: $timerText",
            style = MaterialTheme.typography.bodySmall,
            color = if (remainingTime < 60) Color.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button (chỉ hiển thị khi chưa auto verify)
        if (otpText.length < 6 ||
            otpState is OtpVerificationState.Verifying ||
            otpState is OtpVerificationState.Sending) {

            Button(
                onClick = { viewModel.verifyOtp(otpText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOrange
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = otpText.length == 6 &&
                        otpState !is OtpVerificationState.Verifying &&
                        otpState !is OtpVerificationState.Sending &&
                        remainingTime > 0
            ) {
                when (otpState) {
                    is OtpVerificationState.Verifying -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang xác thực...")
                    }
                    else -> {
                        Text(
                            "Xác nhận",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Resend OTP Button
        TextButton(
            onClick = {
                viewModel.resendOtp()
                otpText = "" // Reset OTP khi gửi lại
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = otpState !is OtpVerificationState.Sending &&
                    otpState !is OtpVerificationState.Verifying
        ) {
            when (otpState) {
                is OtpVerificationState.Sending -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang gửi lại...")
                }
                else -> {
                    Text("Gửi lại mã OTP")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SixDigitOtpInput(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val previousOtpLength = remember { mutableStateOf(0) }

    // Tự động focus khi nhập hoặc xóa
    LaunchedEffect(otpText) {
        val currentLength = otpText.length

        if (currentLength > previousOtpLength.value) {
            // Đang nhập - focus ô tiếp theo
            if (currentLength < 6 && enabled) {
                focusRequesters[currentLength].requestFocus()
            }
        } else if (currentLength < previousOtpLength.value) {
            // Đang xóa - focus ô trước đó (thụt lùi)
            if (currentLength < 6 && enabled) {
                focusRequesters[currentLength].requestFocus()
            }
        }

        previousOtpLength.value = currentLength
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Sử dụng Box với weight cho các ô đều nhau
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (index in 0 until 6) {
                OtpDigitBox(
                    index = index,
                    value = otpText.getOrNull(index)?.toString() ?: "",
                    focusRequester = focusRequesters[index],
                    onValueChange = { newChar ->
                        if (newChar.isNotEmpty()) {
                            // Nhập số
                            val newOtp = buildString {
                                append(otpText.take(index))
                                append(newChar)
                                append(otpText.drop(index + 1))
                            }.take(6)
                            onOtpTextChange(newOtp)
                        } else {
                            // Xóa (backspace)
                            if (index > 0) {
                                // Xóa ký tự tại vị trí index-1
                                val newOtp = buildString {
                                    append(otpText.take(index - 1))
                                    append(otpText.drop(index))
                                }
                                onOtpTextChange(newOtp)
                            } else if (index == 0) {
                                // Xóa ở ô đầu tiên
                                onOtpTextChange("")
                            }
                        }
                    },
                    enabled = enabled,
                    isFilled = index < otpText.length,
                    modifier = Modifier.weight(1f)
                )
                // Thêm khoảng cách giữa các ô (trừ ô cuối cùng)
                if (index < 5) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        // Hướng dẫn
        Text(
            text = "Nhập mã OTP 6 số",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun OtpDigitBox(
    index: Int,
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    isFilled: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Đảm bảo ô vuông
            .background(
                color = when {
                    !enabled -> Color(0xFFF5F5F5)
                    isFocused -> Color(0xFFE3F2FD)
                    isFilled -> Color(0xFFE8F5E9)
                    else -> Color(0xFFFAFAFA)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = when {
                    !enabled -> Color.Gray
                    isFocused -> PrimaryOrange
                    isFilled -> Color(0xFF4CAF50)
                    else -> Color(0xFFE0E0E0)
                },
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.Black else Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            cursorBrush = SolidColor(PrimaryOrange),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            text = "•",
                            fontSize = 24.sp,
                            color = Color(0xFFBDBDBD),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

// Extension function để lấy ký tự tại index
private fun String.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
}

// Hàm lấy email từ SharedPreferences
private fun getEmailFromPrefs(context: Context): String {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("reset_password_email", "") ?: ""
}