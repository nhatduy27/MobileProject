package com.example.foodapp.authentication.login

import androidx.compose.foundation.background
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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
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

    val logInState by viewModel.logInState.collectAsStateWithLifecycle()
    val googleLogInState by viewModel.googleLogInState.collectAsStateWithLifecycle()
    val existAccountState by viewModel.existAccountState.collectAsStateWithLifecycle()

    LaunchedEffect(existAccountState) {
        if (existAccountState == true) {
            onLoginSuccess()
        }
    }

    LoginContent(
        logInState = logInState,
        googleLogInState = googleLogInState,
        onLoginClick = { email, password -> viewModel.logInWithEmail(email, password) },
        onGoogleClick = { /* Logic Google Sign In Client */ },
        onBackClicked = onBackClicked,
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fieldErrors by remember { mutableStateOf(mapOf<String, String>()) }

    // Rút trích trạng thái tổng hợp
    val isLoading = logInState is LogInState.Loading || googleLogInState is GoogleLogInState.Loading
    val isSuccess = logInState is LogInState.Success || googleLogInState is GoogleLogInState.Success
    val serverError = (logInState as? LogInState.Error)?.message
        ?: (googleLogInState as? GoogleLogInState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút Back
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClicked, enabled = !isLoading) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Đăng nhập",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )

        if (serverError != null) {
            Text(text = serverError, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp))
        }

        val customTextFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
            disabledContainerColor = Color(0xFFF0F0F0)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ô nhập Email
        TextField(
            value = email,
            onValueChange = { email = it },
            enabled = !isLoading && !isSuccess,
            placeholder = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = customTextFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ô nhập Password
        TextField(
            value = password,
            onValueChange = { password = it },
            enabled = !isLoading && !isSuccess,
            placeholder = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = customTextFieldColors,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                    Icon(imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        TextButton(
            onClick = onForgotPasswordClicked,
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) { Text("Quên mật khẩu?", color = Color(0xFF666666)) }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút Đăng nhập
        Button(
            onClick = {
                val errors = validateInput(email, password)
                if (errors.isEmpty()) onLoginClick(email, password) else fieldErrors = errors
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
                    else -> "Đăng nhập"
                },
                fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3 nút demo chuyển vai trò
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

        // Separator
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDDDDD))
            Text("hoặc đăng nhập với", modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFF666666), fontSize = 14.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDDDDD))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading && !isSuccess,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
        ) {
            Text("Đăng nhập với Google", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bạn chưa có tài khoản?", color = Color(0xFF666666))
            TextButton(onClick = onSignUpClicked, enabled = !isLoading) {
                Text("Đăng ký ngay", color = PrimaryOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun validateInput(email: String, password: String): Map<String, String> {
    val errors = mutableMapOf<String, String>()
    if (email.isEmpty()) errors["email"] = "Vui lòng nhập Email"
    if (password.length < 6) errors["password"] = "Mật khẩu tối thiểu 6 ký tự"
    return errors
}