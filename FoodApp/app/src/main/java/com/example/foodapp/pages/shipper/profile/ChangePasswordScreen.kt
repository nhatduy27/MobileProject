package com.example.foodapp.pages.shipper.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun ChangePasswordScreen(
    onCancel: () -> Unit = {},
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Show toast for success message
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.InfoLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ShipperColors.Info,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Yêu cầu mật khẩu mới:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ShipperColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Tối thiểu 6 ký tự\n• Nên chứa chữ hoa và chữ thường\n• Nên chứa ít nhất 1 số",
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Password fields card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PasswordField(
                    label = "Mật khẩu hiện tại",
                    value = uiState.currentPassword,
                    onValueChange = { viewModel.updateCurrentPassword(it) },
                    passwordVisible = uiState.currentPasswordVisible,
                    onVisibilityChange = { viewModel.toggleCurrentPasswordVisibility() },
                    icon = Icons.Outlined.Lock
                )

                HorizontalDivider(color = ShipperColors.Divider)

                PasswordField(
                    label = "Mật khẩu mới",
                    value = uiState.newPassword,
                    onValueChange = { viewModel.updateNewPassword(it) },
                    passwordVisible = uiState.newPasswordVisible,
                    onVisibilityChange = { viewModel.toggleNewPasswordVisibility() },
                    icon = Icons.Outlined.Key
                )

                HorizontalDivider(color = ShipperColors.Divider)

                PasswordField(
                    label = "Xác nhận mật khẩu mới",
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
                    passwordVisible = uiState.confirmPasswordVisible,
                    onVisibilityChange = { viewModel.toggleConfirmPasswordVisibility() },
                    icon = Icons.Outlined.CheckCircle
                )
            }
        }

        // Error message
        if (uiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ShipperColors.ErrorLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = null,
                        tint = ShipperColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = uiState.error!!,
                        color = ShipperColors.Error,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Success message
        if (uiState.successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ShipperColors.SuccessLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = ShipperColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = uiState.successMessage!!,
                        color = ShipperColors.Success,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Submit button
        Button(
            onClick = { viewModel.changePassword() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = ShipperColors.Surface,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Đổi mật khẩu", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onVisibilityChange: () -> Unit,
    icon: ImageVector
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShipperColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = ShipperColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        tint = ShipperColors.TextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ShipperColors.Primary,
                unfocusedBorderColor = ShipperColors.Divider
            ),
            singleLine = true
        )
    }
}
