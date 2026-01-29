package com.example.foodapp.authentication.login

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.ui.theme.PrimaryOrange
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onBackClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(context)
    )

    LaunchedEffect(Unit) {
        // Sử dụng đúng Web Client ID từ google-services.json
        val WEB_CLIENT_ID = "884959847866-5qiurc00ii1pnrs1dtou2kvau5oa9s96.apps.googleusercontent.com"


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)  // ← ĐÚNG RỒI!
            .requestEmail()
            .requestProfile()  // Thêm dòng này để lấy profile info
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        viewModel.initializeGoogleSignIn(googleSignInClient)
    }

    val logInState by viewModel.logInState.collectAsStateWithLifecycle()
    val googleLogInState by viewModel.googleLogInState.collectAsStateWithLifecycle()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleGoogleSignInResult(task)
    }

    LaunchedEffect(logInState, googleLogInState) {
        when (logInState) {
            is LogInState.Success -> {
                val successState = logInState as LogInState.Success
                onLoginSuccess(successState.role)
            }
            else -> {}
        }

        when (googleLogInState) {
            is GoogleLogInState.Success -> {
                val successState = googleLogInState as GoogleLogInState.Success
                onLoginSuccess(successState.role)
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetStates()
    }

    LoginContent(
        viewModel = viewModel,
        logInState = logInState,
        googleLogInState = googleLogInState,
        onLoginClick = { email, password ->
            viewModel.login(email, password)
        },
        onGoogleClick = {
            try {
                viewModel.getGoogleSignInIntent()?.let { signInIntent ->
                    googleSignInLauncher.launch(signInIntent)
                }
            } catch (e: Exception) {
                // Error handled by ViewModel
            }
        },
        onBackClicked = {
            viewModel.resetStates()
            onBackClicked()
        },
        onSignUpClicked = onSignUpClicked,
        onForgotPasswordClicked = onForgotPasswordClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    viewModel: LoginViewModel,
    logInState: LogInState,
    googleLogInState: GoogleLogInState,
    onLoginClick: (String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onBackClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val isLoading = logInState is LogInState.Loading || googleLogInState is GoogleLogInState.Loading
    val isEmailLoginSuccess = logInState is LogInState.Success
    val isGoogleLoginSuccess = googleLogInState is GoogleLogInState.Success
    val isSuccess = isEmailLoginSuccess || isGoogleLoginSuccess

    val loginError = (logInState as? LogInState.Error)?.message
    val googleError = (googleLogInState as? GoogleLogInState.Error)?.message
    val serverError = loginError ?: googleError
    val displayError = localValidationError ?: serverError

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Back Button with modern design
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isLoading) {
                            viewModel.resetStates()
                            onBackClicked()
                        },
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Section with Animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo or Icon
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = PrimaryOrange.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Chào mừng trở lại!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Đăng nhập để tiếp tục",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Error/Success Messages with Animation
            AnimatedVisibility(
                visible = displayError != null && !isSuccess,
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
                            text = displayError ?: "",
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSuccess,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isEmailLoginSuccess) "Đăng nhập thành công!"
                            else "Đăng nhập Google thành công!",
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Modern Input Fields
            ModernTextField(
                value = email,
                onValueChange = {
                    email = it
                    localValidationError = null
                },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                enabled = !isLoading && !isSuccess,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModernTextField(
                value = password,
                onValueChange = {
                    password = it
                    localValidationError = null
                },
                label = "Mật khẩu",
                leadingIcon = Icons.Default.Lock,
                enabled = !isLoading && !isSuccess,
                isPassword = true,
                isVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            // Forgot Password
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onForgotPasswordClicked,
                    enabled = !isLoading && !isSuccess
                ) {
                    Text(
                        "Quên mật khẩu?",
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Modern Login Button
            Button(
                onClick = {
                    localValidationError = when {
                        email.isBlank() -> "Vui lòng nhập email"
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ"
                        password.isBlank() -> "Vui lòng nhập mật khẩu"
                        password.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
                        else -> null
                    }

                    if (localValidationError == null) {
                        onLoginClick(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = if (!isLoading && !isSuccess) 8.dp else 0.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmailLoginSuccess) Color(0xFF4CAF50) else PrimaryOrange,
                    disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading && !isSuccess
            ) {
                when {
                    isLoading && logInState is LogInState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Đang đăng nhập...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    isEmailLoginSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thành công!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    else -> Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            // Google Sign-In Button
            OutlinedButton(
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading && !isSuccess,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2D2D2D)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isLoading || isSuccess) Color.Transparent else Color(0xFFE0E0E0)
                )
            ) {
                when {
                    googleLogInState is GoogleLogInState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryOrange
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Đang kết nối...", fontWeight = FontWeight.Bold)
                    }
                    isGoogleLoginSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đã kết nối!", fontWeight = FontWeight.Bold)
                    }
                    else -> {
                        // Google Logo SVG as Text
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4),
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, CircleShape)
                                .wrapContentSize(Alignment.Center)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tiếp tục với Google", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sign Up Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    color = Color(0xFF666666),
                    fontSize = 15.sp
                )
                Text(
                    text = "Đăng ký ngay",
                    color = PrimaryOrange,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(enabled = !isLoading) { onSignUpClicked() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val visualTransformation = if (isPassword && !isVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = if (enabled) 2.dp else 0.dp
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            label = { Text(label, color = Color(0xFF999999)) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (value.isNotEmpty()) PrimaryOrange else Color(0xFFCCCCCC)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = onToggleVisibility,
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = if (isVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (isVisible) "Ẩn" else "Hiện",
                            tint = if (enabled) Color(0xFF999999) else Color(0xFFCCCCCC)
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLabelColor = PrimaryOrange,
                unfocusedLabelColor = Color(0xFF999999)
            ),
            visualTransformation = visualTransformation,
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun DemoChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        color = PrimaryOrange.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = PrimaryOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}