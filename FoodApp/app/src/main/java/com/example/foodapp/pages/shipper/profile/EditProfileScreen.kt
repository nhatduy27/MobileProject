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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun EditProfileScreen(
    onCancel: () -> Unit = {},
    viewModel: EditProfileViewModel = viewModel()
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

    // Show toast for error message
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ShipperColors.Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ShipperColors.Primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = RoundedCornerShape(44.dp),
                    color = ShipperColors.Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.displayName.firstOrNull()?.toString() ?: "S",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ShipperColors.Surface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { /* TODO: Upload avatar */ }) {
                    Text("Thay đổi ảnh đại diện", color = ShipperColors.Primary)
                }
            }
        }

        // Profile fields card
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileField(
                    label = "Họ và tên",
                    value = uiState.displayName,
                    onValueChange = { viewModel.updateDisplayName(it) },
                    enabled = uiState.isEditing,
                    icon = Icons.Outlined.Person
                )
                HorizontalDivider(color = ShipperColors.Divider)
                ProfileField(
                    label = "Email",
                    value = uiState.email,
                    onValueChange = { },
                    enabled = false,
                    icon = Icons.Outlined.Email
                )
                HorizontalDivider(color = ShipperColors.Divider)
                ProfileField(
                    label = "Số điện thoại",
                    value = uiState.phone,
                    onValueChange = { viewModel.updatePhone(it) },
                    enabled = uiState.isEditing,
                    icon = Icons.Outlined.Phone
                )
                HorizontalDivider(color = ShipperColors.Divider)
                ProfileField(
                    label = "Địa chỉ",
                    value = uiState.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    enabled = uiState.isEditing,
                    icon = Icons.Outlined.LocationOn,
                    singleLine = false
                )
                HorizontalDivider(color = ShipperColors.Divider)
                ProfileField(
                    label = "Loại phương tiện",
                    value = uiState.vehicleType,
                    onValueChange = { viewModel.updateVehicleType(it) },
                    enabled = uiState.isEditing,
                    icon = Icons.Outlined.DirectionsBike
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isEditing) {
                OutlinedButton(
                    onClick = {
                        viewModel.setEditing(false)
                        viewModel.loadProfile() // Reset to original values
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ShipperColors.TextSecondary
                    )
                ) {
                    Text("Hủy")
                }
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ShipperColors.Surface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Lưu", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.setEditing(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
                ) {
                    Text("Chỉnh sửa thông tin", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = ShipperColors.TextPrimary,
                disabledBorderColor = ShipperColors.Divider,
                disabledContainerColor = ShipperColors.Background,
                focusedBorderColor = ShipperColors.Primary,
                unfocusedBorderColor = ShipperColors.Divider
            ),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 3
        )
    }
}
