package com.example.foodapp.authentication.otpverification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
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
    var otpValue by rememberSaveable { mutableStateOf("") }

    val isLoading = otpState is OtpVerificationState.Verifying ||
            otpState is OtpVerificationState.Sending
    val isSuccess = otpState is OtpVerificationState.Success
    val errorMessage = when (otpState) {
        is OtpVerificationState.Error -> otpState.message
        else -> null
    }

    // Auto submit when OTP is complete
    LaunchedEffect(otpValue) {
        if (otpValue.length == 6 && !isLoading && !isSuccess) {
            onVerifyOtp(otpValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    if (!isLoading) onBackClicked()
                },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = if (isLoading) Color.Gray else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            "Xác thực OTP",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email display
        Text(
            "Mã OTP 6 số đã được gửi đến:",
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

        // OTP Input với 6 ô riêng biệt
        SixDigitOtpInput(
            otpText = otpValue,
            onOtpTextChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    otpValue = newValue
                }
            },
            enabled = !isLoading && !isSuccess,
            modifier = Modifier.padding(vertical = 16.dp)
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
                            otpValue = ""
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Verify Button
        Button(
            onClick = {
                if (otpValue.length == 6 && !isLoading && !isSuccess) {
                    onVerifyOtp(otpValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = otpValue.length == 6 && !isLoading && !isSuccess,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B35)
            )
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
@Composable
fun SixDigitOtpInput(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Theo dõi độ dài OTP trước đó
    val previousOtpLength = remember { mutableStateOf(0) }

    // Tự động focus khi nhập hoặc xóa
    LaunchedEffect(otpText) {
        val currentLength = otpText.length

        if (currentLength > previousOtpLength.value) {
            // Đang nhập - focus ô tiếp theo
            if (currentLength < 6) {
                focusRequesters[currentLength].requestFocus()
            }
        } else if (currentLength < previousOtpLength.value) {
            // Đang xóa - focus ô trước đó (thụt lùi)
            if (currentLength < 6) {
                focusRequesters[currentLength].requestFocus()
            }
        }

        previousOtpLength.value = currentLength
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
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
                    isFilled = index < otpText.length
                )
            }
        }

        // Hướng dẫn nhỏ bên dưới
        Text(
            text = "Nhập mã OTP 6 số",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 8.dp)
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
    isFilled: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    // Kích thước cố định cho mỗi ô
    val boxSize = 48.dp
    val borderWidth = 1.5.dp

    Box(
        modifier = Modifier
            .size(boxSize)
            .background(
                color = when {
                    !enabled -> Color(0xFFF5F5F5)
                    isFocused -> Color(0xFFE3F2FD)
                    isFilled -> Color(0xFFE8F5E9)
                    else -> Color.White
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = borderWidth,
                color = when {
                    !enabled -> Color.Gray
                    isFocused -> Color(0xFF2196F3)
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.Black else Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            cursorBrush = SolidColor(Color(0xFFFF6B35)),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Hiển thị placeholder khi chưa có giá trị
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

// Preview Composable
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun OtpVerificationContentPreview() {
    OtpVerificationContent(
        email = "example@gmail.com",
        otpState = OtpVerificationState.Idle,
        remainingTime = 120,
        onVerifyOtp = {},
        onResendOtp = {},
        onBackClicked = {}
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SixDigitOtpInputPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var otpText by remember { mutableStateOf("123") }

        SixDigitOtpInput(
            otpText = otpText,
            onOtpTextChange = { otpText = it },
            enabled = true,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "OTP Text: $otpText",
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun OtpDigitBoxPreview() {
    // Tạo FocusRequester với remember để tránh lỗi @RememberInComposition
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ô trống
        OtpDigitBox(
            index = 0,
            value = "",
            focusRequester = focusRequester1,
            onValueChange = {},
            enabled = true,
            isFilled = false
        )

        // Ô có giá trị
        OtpDigitBox(
            index = 1,
            value = "5",
            focusRequester = focusRequester2,
            onValueChange = {},
            enabled = true,
            isFilled = true
        )

        // Ô disabled
        OtpDigitBox(
            index = 2,
            value = "3",
            focusRequester = focusRequester3,
            onValueChange = {},
            enabled = false,
            isFilled = true
        )
    }
}