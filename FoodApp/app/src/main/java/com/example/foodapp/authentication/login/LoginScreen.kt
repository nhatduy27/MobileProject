package com.example.foodapp.authentication.login

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
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
    onCustomerDemo: () -> Unit,
    onShipperDemo: () -> Unit,
    onOwnerDemo: () -> Unit,
    onForgotPasswordClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(context)
    )

    // Khởi tạo Google Sign-In Client
    LaunchedEffect(Unit) {
        val WEB_CLIENT_ID = "884959847866-5qiurc00ii1pnrs1dtou2kvau5oa9s96.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        viewModel.initializeGoogleSignIn(googleSignInClient)
    }

    // Observe ViewModel states
    val logInState by viewModel.logInState.collectAsStateWithLifecycle()
    val googleLogInState by viewModel.googleLogInState.collectAsStateWithLifecycle()

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleGoogleSignInResult(task)
    }

    // Handle successful login and navigate
    LaunchedEffect(logInState, googleLogInState) {
        when (logInState) {
            is LogInState.Success -> {
                val successState = logInState as LogInState.Success
                // Lấy role từ success state (đã có trong data class)
                onLoginSuccess(successState.role)
            }
            else -> {}
        }

        when (googleLogInState) {
            is GoogleLogInState.Success -> {
                val successState = googleLogInState as GoogleLogInState.Success
                // Lấy role từ success state (đã có trong data class)
                onLoginSuccess(successState.role)
            }
            else -> {}
        }
    }

    // Reset error when component is recomposed
    LaunchedEffect(Unit) {
        viewModel.resetStates()
    }

    // UI Content
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
                // Error will be handled by ViewModel state
            }
        },
        onBackClicked = {
            viewModel.resetStates()
            onBackClicked()
        },
        onSignUpClicked = onSignUpClicked,
        onCustomerDemo = onCustomerDemo,
        onShipperDemo = onShipperDemo,
        onOwnerDemo = onOwnerDemo,
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
    onCustomerDemo: () -> Unit,
    onShipperDemo: () -> Unit,
    onOwnerDemo: () -> Unit,
    onForgotPasswordClicked: () -> Unit
) {
    // Local UI State
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    // Derived states from ViewModel
    val isLoading = logInState is LogInState.Loading || googleLogInState is GoogleLogInState.Loading

    // Check for success states
    val isEmailLoginSuccess = logInState is LogInState.Success
    val isGoogleLoginSuccess = googleLogInState is GoogleLogInState.Success
    val isSuccess = isEmailLoginSuccess || isGoogleLoginSuccess

    // Error messages
    val loginError = (logInState as? LogInState.Error)?.message
    val googleError = (googleLogInState as? GoogleLogInState.Error)?.message
    val serverError = loginError ?: googleError

    val displayError = localValidationError ?: serverError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = {
                    viewModel.resetStates()
                    onBackClicked()
                },
                enabled = !isLoading
            ) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Title
        Text(
            text = "Đăng nhập",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )

        // Error Message
        if (displayError != null && !isSuccess) {
            Text(
                text = displayError,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0x1AFF0000), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                fontSize = 14.sp
            )
        }

        // Success Message
        if (isSuccess) {
            Text(
                text = if (isEmailLoginSuccess) "Đăng nhập thành công!"
                else "Đăng nhập Google thành công!",
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0x1A4CAF50), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // TextField Colors
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
            disabledContainerColor = Color(0xFFF0F0F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )

        // Email Field
        LoginTextField(
            value = email,
            onValueChange = {
                email = it
                localValidationError = null // Clear error when typing
            },
            placeholder = "Email",
            enabled = !isLoading && !isSuccess,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        LoginTextField(
            value = password,
            onValueChange = {
                password = it
                localValidationError = null // Clear error when typing
            },
            placeholder = "Mật khẩu",
            enabled = !isLoading && !isSuccess,
            isPassword = true,
            isVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = textFieldColors
        )

        // Forgot Password
        TextButton(
            onClick = onForgotPasswordClicked,
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading && !isSuccess
        ) {
            Text("Quên mật khẩu?", color = Color(0xFF666666))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                // Local validation before calling ViewModel
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
                .height(56.dp),
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
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang đăng nhập...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                isEmailLoginSuccess -> Text("Đăng nhập thành công!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                else -> Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo Role Buttons (only show when not loading/success)
        if (!isLoading && !isSuccess) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onCustomerDemo,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(36.dp).padding(horizontal = 4.dp)
                ) {
                    Text("Customer", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onShipperDemo,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(36.dp).padding(horizontal = 4.dp)
                ) {
                    Text("Shipper", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOwnerDemo,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(36.dp).padding(horizontal = 4.dp)
                ) {
                    Text("Owner", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Divider với "hoặc" (only show when not loading/success)
        if (!isLoading && !isSuccess) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFDDDDDD)
                )
                Text(
                    "hoặc đăng nhập với",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFF666666),
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFDDDDDD)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Google Sign-In Button
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading && !isSuccess,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black,
                disabledContentColor = Color.Gray
            ),
            border = if (isLoading || isSuccess) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            }
        ) {
            when {
                googleLogInState is GoogleLogInState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang đăng nhập...", fontWeight = FontWeight.Bold)
                }
                isGoogleLoginSuccess -> {
                    Text("Đăng nhập thành công!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                else -> {
                    Text("Đăng nhập với Google", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bạn chưa có tài khoản? ",
                color = Color(0xFF666666),
                fontSize = 14.sp
            )
            Text(
                text = "Đăng ký ngay",
                color = PrimaryOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = !isLoading) { onSignUpClicked() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors
) {
    val visualTransformation = if (isPassword && !isVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        visualTransformation = visualTransformation,
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = onToggleVisibility,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = if (isVisible)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = if (isVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        tint = if (enabled) Color.Gray else Color.LightGray
                    )
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}