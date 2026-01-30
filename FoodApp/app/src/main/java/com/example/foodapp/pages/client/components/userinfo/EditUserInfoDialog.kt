package com.example.foodapp.pages.client.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.model.client.Client
import com.example.foodapp.pages.client.userInfo.UpdateUserInfoState
import com.example.foodapp.pages.client.userInfo.UserInfoViewModel

@Composable
fun EditUserInfoDialog(
    currentUser: Client?,
    onDismiss: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: UserInfoViewModel = viewModel(
        factory = UserInfoViewModel.factory(context)
    )
    val updateState by viewModel.updateState.observeAsState()

    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }

    // Reset form khi currentUser thay đổi
    LaunchedEffect(currentUser) {
        fullName = currentUser?.fullName ?: ""
        phone = currentUser?.phone ?: ""
    }

    // Xử lý khi update thành công
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateUserInfoState.Success -> {
                onUpdateSuccess()
                onDismiss()
                viewModel.resetUpdateState()
            }
            is UpdateUserInfoState.Error -> {
                // Xử lý lỗi ở đây nếu cần
            }
            else -> {}
        }
    }

    Dialog(
        onDismissRequest = {
            if (updateState !is UpdateUserInfoState.Loading) {
                onDismiss()
                viewModel.resetUpdateState()
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_user_info_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = {
                            if (updateState !is UpdateUserInfoState.Loading) {
                                onDismiss()
                                viewModel.resetUpdateState()
                            }
                        },
                        enabled = updateState !is UpdateUserInfoState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_icon)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Họ và tên
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(stringResource(R.string.full_name_label)) },
                        placeholder = { Text(stringResource(R.string.full_name_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = updateState !is UpdateUserInfoState.Loading
                    )

                    // Số điện thoại
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.phone_label)) },
                        placeholder = { Text(stringResource(R.string.phone_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = updateState !is UpdateUserInfoState.Loading
                    )

                    // Email (readonly)
                    OutlinedTextField(
                        value = currentUser?.email ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.email_label)) },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút lưu
                Button(
                    onClick = {
                        viewModel.updateProfile(fullName, phone)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updateState !is UpdateUserInfoState.Loading
                ) {
                    if (updateState is UpdateUserInfoState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(stringResource(R.string.save_changes_button))
                    }
                }
            }
        }
    }
}