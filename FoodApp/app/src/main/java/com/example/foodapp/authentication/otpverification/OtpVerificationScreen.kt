package com.example.foodapp.authentication.otpverification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    LaunchedEffect(otpValue) {
        if (otpValue.length == 6 && !isLoading && !isSuccess) {
            onVerifyOtp(otpValue)
        }
    }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8F0),
                        Color(0xFFFFFFFF),
                        Color(0xFFFFF5EB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isLoading) { onBackClicked() },
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF6B35)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Shield Icon
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color(0xFFFF6B35).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Xác thực OTP",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mã OTP 6 số đã được gửi đến:",
                        fontSize = 15.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = email,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Error Message
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OTP Input
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

            Spacer(modifier = Modifier.height(32.dp))

            // Timer/Resend Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFE0E0E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (remainingTime > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Gửi lại sau: ",
                            color = Color(0xFF666666),
                            fontSize = 15.sp
                        )
                        val minutes = remainingTime / 60
                        val seconds = remainingTime % 60
                        Text(
                            String.format("%02d:%02d", minutes, seconds),
                            color = Color(0xFFFF6B35),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (isLoading) Color.Gray else Color(0xFFFF6B35),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Không nhận được mã? ",
                            color = Color(0xFF666666),
                            fontSize = 15.sp
                        )
                        Text(
                            "Gửi lại",
                            color = if (isLoading) Color.Gray else Color(0xFFFF6B35),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
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
                    .height(56.dp)
                    .shadow(
                        elevation = if (otpValue.length == 6 && !isLoading) 8.dp else 0.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                enabled = otpValue.length == 6 && !isLoading && !isSuccess,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFFF6B35),
                    disabledContainerColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
                )
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Đang xác thực...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    isSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Thành công!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Xác thực",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE3F2FD),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFF2196F3).copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Nếu không nhận được mã, vui lòng kiểm tra hộp thư spam hoặc thử lại sau ít phút.",
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
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

    LaunchedEffect(otpText) {
        val currentLength = otpText.length

        if (currentLength > previousOtpLength.value) {
            if (currentLength < 6) {
                focusRequesters[currentLength].requestFocus()
            }
        } else if (currentLength < previousOtpLength.value) {
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
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (index in 0 until 6) {
                OtpDigitBox(
                    index = index,
                    value = otpText.getOrNull(index)?.toString() ?: "",
                    focusRequester = focusRequesters[index],
                    onValueChange = { newChar ->
                        if (newChar.isNotEmpty()) {
                            val newOtp = buildString {
                                append(otpText.take(index))
                                append(newChar)
                                append(otpText.drop(index + 1))
                            }.take(6)
                            onOtpTextChange(newOtp)
                        } else {
                            if (index > 0) {
                                val newOtp = buildString {
                                    append(otpText.take(index - 1))
                                    append(otpText.drop(index))
                                }
                                onOtpTextChange(newOtp)
                            } else if (index == 0) {
                                onOtpTextChange("")
                            }
                        }
                    },
                    enabled = enabled,
                    isFilled = index < otpText.length
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nhập mã OTP 6 số",
            fontSize = 13.sp,
            color = Color(0xFF999999),
            fontWeight = FontWeight.Medium
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
    val boxSize = 52.dp
    val borderWidth = 2.dp

    // Animation for fill state
    val animatedScale by animateFloatAsState(
        targetValue = if (isFilled) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .size(boxSize)
            .background(
                color = when {
                    !enabled -> Color(0xFFF5F5F5)
                    isFocused -> Color(0xFFFFE8DC)
                    isFilled -> Color(0xFFE8F5E9)
                    else -> Color.White
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = borderWidth,
                color = when {
                    !enabled -> Color.Gray
                    isFocused -> Color(0xFFFF6B35)
                    isFilled -> Color(0xFF4CAF50)
                    else -> Color(0xFFE0E0E0)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = if (isFocused || isFilled) 4.dp else 0.dp,
                shape = RoundedCornerShape(16.dp)
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
                color = if (enabled) Color(0xFF2D2D2D) else Color.Gray,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color(0xFFFF6B35)),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            text = "•",
                            fontSize = 28.sp,
                            color = Color(0xFFCCCCCC),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

private fun String.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
}

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