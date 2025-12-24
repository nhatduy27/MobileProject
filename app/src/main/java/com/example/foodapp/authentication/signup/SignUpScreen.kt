package com.example.foodapp.authentication.signup

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.authentication.login.LoginViewModel
import com.example.foodapp.data.repository.FirebaseRepository
import com.example.foodapp.ui.theme.PrimaryOrange

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit
) {


    val context = LocalContext.current
    //khởi tạo viewModel
    val viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModel.factory(context)
    )

    val signUpState by viewModel.signUpState.observeAsState(SignUpState.Idle)
    val googleSignInState by viewModel.googleSignInState.observeAsState(GoogleSignInState.Idle)
    val saveUserState by viewModel.saveUserState.observeAsState(null)

    LaunchedEffect(saveUserState) {
        if (saveUserState == true) {
            onSignUpSuccess()
        }
    }

    SignUpContent(
        signUpState = signUpState,
        googleSignInState = googleSignInState,
        onRegisterClick = { fullName, email, password ->
            viewModel.registerWithEmail(fullName, email, password)
        },
        onGoogleClick = { /* Gọi Google Auth SDK */ },
        onLoginClicked = onLoginClicked,
        onBackClicked = onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    signUpState: SignUpState,
    googleSignInState: GoogleSignInState,
    onRegisterClick: (String, String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onLoginClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val isLoading = signUpState is SignUpState.Loading || googleSignInState is GoogleSignInState.Loading
    val isSuccess = signUpState is SignUpState.Success || googleSignInState is GoogleSignInState.Success
    val serverErrorMessage = (signUpState as? SignUpState.Error)?.message
        ?: (googleSignInState as? GoogleSignInState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClicked, enabled = !isLoading) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text("Tạo tài khoản", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))

        val displayError = localError ?: serverErrorMessage
        if (displayError != null) {
            Text(text = displayError, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp))
        }

        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
            disabledContainerColor = Color(0xFFF0F0F0)
        )

        SignUpTextField(fullName, { fullName = it }, "Họ và tên", !isLoading && !isSuccess, colors = textFieldColors)
        SignUpTextField(email, { email = it }, "Email", !isLoading && !isSuccess, colors = textFieldColors, keyboardType = KeyboardType.Email)

        SignUpTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Mật khẩu",
            enabled = !isLoading && !isSuccess,
            isPassword = true,
            isVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            colors = textFieldColors
        )

        SignUpTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Xác nhận mật khẩu",
            enabled = !isLoading && !isSuccess,
            isPassword = true,
            isVisible = confirmPasswordVisible,
            onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    localError = "Vui lòng nhập đầy đủ thông tin"
                } else if (password != confirmPassword) {
                    localError = "Mật khẩu xác nhận không khớp"
                } else {
                    localError = null
                    onRegisterClick(fullName, email, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSuccess) Color(0xFF4CAF50) else PrimaryOrange
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading && !isSuccess
        ) {
            Text(
                text = when {
                    isLoading -> "Đang xử lý..."
                    isSuccess -> "Thành công!"
                    else -> "Đăng ký"
                },
                fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
            Text(text = " Hoặc đăng ký với ", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading && !isSuccess,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("Đăng nhập với Google", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer: Tách biệt Text và Clickable Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bạn đã có tài khoản? ",
                color = Color(0xFF666666),
                fontSize = 14.sp
            )
            Text(
                text = "Đăng nhập",
                color = PrimaryOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = !isLoading) { onLoginClicked() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    colors: TextFieldColors
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onToggleVisibility, enabled = enabled) {
                    Icon(imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}