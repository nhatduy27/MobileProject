package com.example.foodapp.authentication.signup

import android.util.Patterns
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.ui.theme.PrimaryOrange

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModel.factory(context)
    )

    val signUpState by viewModel.signUpState.observeAsState(SignUpState.Idle)
    val saveUserState by viewModel.saveUserState.observeAsState(null)

    LaunchedEffect(signUpState) {
        if (signUpState is SignUpState.Success) {
            onSignUpSuccess()
        }
    }

    LaunchedEffect(saveUserState) {
        if (saveUserState == true) {
            onSignUpSuccess()
        }
    }

    SignUpContent(
        signUpState = signUpState,
        onRegisterClick = { fullName, email, password, confirmPassword ->
            viewModel.registerWithEmail(fullName, email, password, confirmPassword)
        },
        onLoginClicked = onLoginClicked,
        onBackClicked = {
            viewModel.resetStates()
            onBackClicked()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    signUpState: SignUpState,
    onRegisterClick: (String, String, String, String) -> Unit,
    onLoginClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isLoading = signUpState is SignUpState.Loading
    val isSuccess = signUpState is SignUpState.Success
    val serverErrorMessage = (signUpState as? SignUpState.Error)?.message
    val displayError = validationError ?: serverErrorMessage

    LaunchedEffect(fullName, email, password, confirmPassword) {
        if (validationError != null) {
            validationError = null
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
                .verticalScroll(rememberScrollState())
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
                            contentDescription = stringResource(R.string.back_button),
                            tint = PrimaryOrange
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
                    // Icon
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = PrimaryOrange.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.signup_title),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.signup_subtitle),
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Error Message
            AnimatedVisibility(
                visible = displayError != null,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            ModernSignUpTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(R.string.full_name_hint),
                leadingIcon = Icons.Default.Person,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModernSignUpTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email_hint),
                leadingIcon = Icons.Default.Email,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModernSignUpTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password_signup_hint),
                leadingIcon = Icons.Default.Lock,
                enabled = !isLoading,
                isPassword = true,
                isVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModernSignUpTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.confirm_password_hint),
                leadingIcon = Icons.Default.LockClock,
                enabled = !isLoading,
                isPassword = true,
                isVisible = confirmPasswordVisible,
                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Password Requirements
            if (password.isNotEmpty() && !isSuccess) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFE0E0E0)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.password_requirements_title),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PasswordRequirement(
                            text = stringResource(R.string.password_length_requirement),
                            isMet = password.length >= 6
                        )
                        PasswordRequirement(
                            text = stringResource(R.string.password_match_requirement),
                            isMet = password == confirmPassword && confirmPassword.isNotEmpty()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }


            val errorFullNameRequired = stringResource(R.string.error_full_name_required)
            val errorFullNameLength = stringResource(R.string.error_full_name_length)
            val errorEmailRequired = stringResource(R.string.error_email_required)
            val errorInvalidEmail = stringResource(R.string.error_invalid_email)
            val errorPasswordRequired = stringResource(R.string.error_password_required)
            val errorPasswordLength = stringResource(R.string.error_password_length)
            val errorPasswordMismatch = stringResource(R.string.error_password_mismatch)
            // Register Button
            Button(
                onClick = {
                    validationError = when {
                        fullName.isBlank() -> errorFullNameRequired
                        fullName.length < 2 -> errorFullNameLength
                        email.isBlank() -> errorEmailRequired
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorInvalidEmail
                        password.isBlank() -> errorPasswordRequired
                        password.length < 6 -> errorPasswordLength
                        password != confirmPassword -> errorPasswordMismatch
                        else -> null
                    }

                    if (validationError == null) {
                        onRegisterClick(fullName, email, password, confirmPassword)
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
                    containerColor = if (isSuccess) Color(0xFF4CAF50) else PrimaryOrange,
                    disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading
            ) {
                when {
                    isLoading && signUpState is SignUpState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.processing), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    isSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.success), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    else -> Text(stringResource(R.string.signup_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )
                Text(
                    stringResource(R.string.or_divider),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    color = Color(0xFF666666),
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.login_now),
                    color = PrimaryOrange,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(enabled = !isLoading) { onLoginClicked() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSignUpTextField(
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
                            contentDescription = if (isVisible) stringResource(R.string.hide_password)
                            else stringResource(R.string.show_password),
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
fun PasswordRequirement(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color(0xFFCCCCCC),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isMet) Color(0xFF4CAF50) else Color(0xFF666666)
        )
    }
}