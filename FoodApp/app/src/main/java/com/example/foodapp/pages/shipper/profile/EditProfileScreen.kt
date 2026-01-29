package com.example.foodapp.pages.shipper.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun EditProfileScreen(
    onCancel: () -> Unit = {},
    viewModel: EditProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

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
                // Avatar with clickable to change
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ShipperColors.Primary)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = uiState.displayName.firstOrNull()?.uppercase() ?: "S",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShipperColors.Surface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ShipperColors.Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
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
