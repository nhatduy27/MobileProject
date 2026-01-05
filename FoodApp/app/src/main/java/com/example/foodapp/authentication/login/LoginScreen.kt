package com.example.foodapp.authentication.login

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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.ui.theme.PrimaryOrange
import com.google.android.gms.auth.api.signin.GoogleSignIn

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

    // Observe ViewModel states
    val logInState by viewModel.logInState.collectAsStateWithLifecycle()
    val googleLogInState by viewModel.googleLogInState.collectAsStateWithLifecycle()
    val existAccountState by viewModel.existAccountState.collectAsStateWithLifecycle()

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleGoogleSignInResult(task)
    }

    // Handle lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handlePendingGoogleSignIn(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Navigate on success
    LaunchedEffect(existAccountState) {
        if (existAccountState == true) {
            val userId = when {
                logInState is LogInState.Success -> (logInState as LogInState.Success).userId
                googleLogInState is GoogleLogInState.Success -> (googleLogInState as GoogleLogInState.Success).userId
                else -> null
            }

            userId?.let { uid ->
                viewModel.getUserRole(uid) { role ->
                    onLoginSuccess(role ?: "user")
                }
            }
        }
    }

    // UI Content
    LoginContent(
        logInState = logInState,
        googleLogInState = googleLogInState,
        onLoginClick = { email, password ->
            val validation = viewModel.validateInput(email, password)
            if (validation is LoginViewModel.ValidationResult.Success) {
                viewModel.logInWithEmail(email, password)
            }
        },
        onGoogleClick = {
            val signInIntent = viewModel.getGoogleSignInClient().signInIntent
            googleSignInLauncher.launch(signInIntent)
        },
        onBackClicked = {
            viewModel.resetStates()
            onBackClicked()
        },
        onSignUpClicked = onSignUpClicked,
        onCustomerDemo = onCustomerDemo,
        onShipperDemo = onShipperDemo,
        onOwnerDemo = onOwnerDemo,
        onForgotPasswordClicked = onForgotPasswordClicked,
        onValidateInput = { email, password ->
            viewModel.validateInput(email, password)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    logInState: LogInState,
    googleLogInState: GoogleLogInState,
    onLoginClick: (String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onBackClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    onCustomerDemo: () -> Unit,
    onShipperDemo: () -> Unit,
    onOwnerDemo: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onValidateInput: (String, String) -> LoginViewModel.ValidationResult
) {
    // Local UI State - KHÔNG chứa business logic
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Derived state từ ViewModel
    val isLoading = logInState is LogInState.Loading || googleLogInState is GoogleLogInState.Loading
    val isSuccess = logInState is LogInState.Success || googleLogInState is GoogleLogInState.Success
    val serverErrorMessage = (logInState as? LogInState.Error)?.message
        ?: (googleLogInState as? GoogleLogInState.Error)?.message

    // Display error
    val displayError = validationError ?: serverErrorMessage

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
            IconButton(onClick = onBackClicked, enabled = !isLoading) {
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
        if (displayError != null) {
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
            onValueChange = { email = it },
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
            onValueChange = { password = it },
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
            enabled = !isLoading
        ) {
            Text("Quên mật khẩu?", color = Color(0xFF666666))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                val validation = onValidateInput(email, password)
                if (validation is LoginViewModel.ValidationResult.Success) {
                    validationError = null
                    onLoginClick(email, password)
                } else if (validation is LoginViewModel.ValidationResult.Error) {
                    validationError = validation.message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSuccess) Color(0xFF4CAF50) else PrimaryOrange,
                disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading && !isSuccess
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang xử lý...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                isSuccess -> Text("Thành công!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                else -> Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo Role Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCustomerDemo,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).height(36.dp).padding(horizontal = 4.dp),
                enabled = !isLoading
            ) {
                Text("Customer", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onShipperDemo,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).height(36.dp).padding(horizontal = 4.dp),
                enabled = !isLoading
            ) {
                Text("Shipper", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOwnerDemo,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).height(36.dp).padding(horizontal = 4.dp),
                enabled = !isLoading
            ) {
                Text("Owner", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider với "hoặc"
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
            if (googleLogInState is GoogleLogInState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang đăng nhập...", fontWeight = FontWeight.Bold)
            } else {
                // Có thể thêm icon Google ở đây
                // Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null)
                // Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng nhập với Google", color = Color.Black, fontWeight = FontWeight.Bold)
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
) {// Tạo visual transformation cho password

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