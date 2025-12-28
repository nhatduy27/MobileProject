package com.example.foodapp.pages.user.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UserProfileScreen(
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(context)
    )

    val userDataState by viewModel.userDataState.observeAsState(UserDataState.Idle)

    var isEditMode by remember { mutableStateOf(false) }
    var userPhone by remember { mutableStateOf("") }
    var userFullName by remember { mutableStateOf("") }

    // State cho dialog xác nhận đăng xuất
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    LaunchedEffect(userDataState) {
        if (userDataState is UserDataState.Success) {
            val state = userDataState as UserDataState.Success
            userPhone = state.user.phone
            userFullName = state.user.fullName
        }
    }

    // Lấy tên từ userDataState
    val userName = when (val state = userDataState) {
        is UserDataState.Success -> state.user.fullName
        is UserDataState.Loading -> "Đang tải..."
        else -> "Khách hàng"
    }

    // Kiểm tra xem userPhone có giá trị không
    val displayPhone = if (userPhone.isNotBlank()) userPhone else "Chưa có"

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isEditMode) {
                        // Gọi update profile với cả fullName và phone
                        viewModel.updateProfile(
                            fullName = userFullName,
                            phone = userPhone
                        )
                    }
                    isEditMode = !isEditMode
                },
                icon = {
                    Icon(
                        imageVector = if (isEditMode) Icons.Filled.Save else Icons.Filled.Edit,
                        contentDescription = if (isEditMode) "Lưu" else "Chỉnh sửa"
                    )
                },
                text = { Text(if (isEditMode) "Lưu thay đổi" else "Chỉnh sửa") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(onBackClick = onBackClick, isEditMode = isEditMode)

                UserInfoHeader(userName = userName)

                ProfileInfoSection(
                    userFullName = if (isEditMode) userFullName else userName,
                    userPhone = if (isEditMode) userPhone else displayPhone,
                    isEditMode = isEditMode,
                    onFullNameChanged = { userFullName = it },
                    onPhoneChanged = { userPhone = it }
                )

                // Sửa nút logout để mở dialog
                LogoutButton(
                    onLogoutClick = { showLogoutDialog = true }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Dialog xác nhận đăng xuất
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    onLogoutClick()
                }
            )
        }
    }
}

// Dialog xác nhận đăng xuất
@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xác nhận đăng xuất",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text("Bạn muốn đăng xuất?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Đăng xuất", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ProfileHeader(onBackClick: () -> Unit, isEditMode: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.Black
            )
        }

        Text(
            text = if (isEditMode) "Chỉnh sửa thông tin" else "Thông tin tài khoản",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun UserInfoHeader(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Khách hàng",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileInfoSection(
    userFullName: String,
    userPhone: String,
    isEditMode: Boolean = false,
    onFullNameChanged: (String) -> Unit = {},
    onPhoneChanged: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "Thông tin cá nhân",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Divider(modifier = Modifier.fillMaxWidth())

            // Thông tin tên đầy đủ
            ProfileInfoItem(
                icon = Icons.Filled.Person,
                title = "Tên đầy đủ",
                value = userFullName,
                showDivider = true,
                isEditMode = isEditMode,
                onValueChanged = onFullNameChanged
            )

            // Thông tin số điện thoại
            ProfileInfoItem(
                icon = Icons.Filled.Phone,
                title = "Số điện thoại",
                value = userPhone,
                showDivider = false,
                isEditMode = isEditMode,
                onValueChanged = onPhoneChanged
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    showDivider: Boolean,
    isEditMode: Boolean = false,
    onValueChanged: (String) -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (isEditMode) {
                    TextField(
                        value = value,
                        onValueChange = onValueChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        singleLine = true,
                        label = { Text("Nhập $title") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 56.dp),
                thickness = 0.5.dp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun LogoutButton(onLogoutClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onLogoutClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = "Đăng xuất",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Red
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    MaterialTheme {
        UserProfileScreen(
            onLogoutClick = {},
            onBackClick = {}
        )
    }
}