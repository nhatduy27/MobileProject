/*
package com.example.foodapp.pages.client.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.foodapp.R

// Data classes để mô phỏng dữ liệu từ backend
data class UserProfile(
    val id: String = "uid_xxx",
    val email: String = "user@example.com",
    val displayName: String = "Nguyễn Văn A",
    val phone: String = "0901234567",
    val avatarUrl: String? = null,
    val role: String = "CUSTOMER",
    val status: String = "ACTIVE",
    val createdAt: String = "2026-01-05T10:00:00Z",
    val addresses: List<UserAddress> = emptyList()
)

data class UserAddress(
    val id: String,
    val label: String,
    val fullAddress: String,
    val isDefault: Boolean = false
)

@Composable
fun UserProfileScreen(
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onAddAddressClick: () -> Unit = {},
    onEditAddressClick: (String) -> Unit = {},
    onChangePasswordClick: () -> Unit = {}
) {
    val viewModel: ProfileViewModel = viewModel()
    val userDataState by viewModel.userDataState.observeAsState(UserDataState.Idle)

    // Mock data cho preview
    val mockUser = remember {
        UserProfile(
            addresses = listOf(
                UserAddress("addr_1", "Nhà", "Tòa A, Phòng 101, 123 Đường ABC, Quận 1, TP.HCM", true),
                UserAddress("addr_2", "Công ty", "Tầng 5, Tòa nhà XYZ, 456 Đường DEF, Quận 2, TP.HCM")
            )
        )
    }

    var isEditMode by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // State cho form chỉnh sửa
    var displayName by remember { mutableStateOf(mockUser.displayName) }
    var phone by remember { mutableStateOf(mockUser.phone) }

    // Format ngày tham gia
    val joinDate = remember(mockUser.createdAt) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDateTime.parse(mockUser.createdAt, DateTimeFormatter.ISO_DATE_TIME)
                .format(formatter)
        } catch (e: Exception) {
            "05/01/2026"
        }
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (!isEditMode) {
                ExtendedFloatingActionButton(
                    onClick = { isEditMode = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Chỉnh sửa"
                        )
                    },
                    text = { Text("Chỉnh sửa") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    ProfileHeader(onBackClick = onBackClick)
                    UserInfoHeader(
                        user = mockUser,
                        isEditMode = isEditMode,
                        displayName = displayName,
                        phone = phone,
                        onDisplayNameChanged = { displayName = it },
                        onPhoneChanged = { phone = it }
                    )
                }

                if (mockUser.addresses.isNotEmpty()) {
                    item {
                        AddressSection(
                            addresses = mockUser.addresses,
                            onAddClick = onAddAddressClick,
                            onEditClick = onEditAddressClick
                        )
                    }
                }

                item {
                    AccountInfoSection(
                        userId = mockUser.id,
                        joinDate = joinDate,
                        status = mockUser.status
                    )
                }

                item {
                    ActionButtonsSection(
                        onChangePasswordClick = { showChangePasswordDialog = true },
                        onLogoutClick = { showLogoutDialog = true }
                    )
                }
            }

            // Hiển thị các nút hành động khi ở chế độ chỉnh sửa
            if (isEditMode) {
                EditModeActions(
                    onSave = {
                        // Gọi API update profile
                        viewModel.updateProfile(displayName, phone)
                        isEditMode = false
                    },
                    onCancel = {
                        // Reset values
                        displayName = mockUser.displayName
                        phone = mockUser.phone
                        isEditMode = false
                    }
                )
            }
        }

        // Dialog xác nhận đăng xuất
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text("Xác nhận đăng xuất", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Bạn có chắc chắn muốn đăng xuất?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogoutClick()
                        }
                    ) {
                        Text("Đăng xuất", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Dialog đổi mật khẩu
        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = { showChangePasswordDialog = false },
                onConfirm = {
                    showChangePasswordDialog = false
                    onChangePasswordClick()
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.Black
            )
        }

        Text(
            text = "Hồ sơ cá nhân",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun UserInfoHeader(
    user: UserProfile,
    isEditMode: Boolean,
    displayName: String,
    phone: String,
    onDisplayNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Avatar và tên
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Avatar
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Thông tin cơ bản
                Column {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = onDisplayNameChanged,
                            label = { Text("Họ và tên") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = user.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "Khách hàng",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Divider(modifier = Modifier.fillMaxWidth())

            // Thông tin chi tiết
            ProfileInfoItem(
                icon = Icons.Filled.Person,
                title = "Họ và tên",
                value = if (isEditMode) displayName else user.displayName,
                isEditMode = isEditMode,
                onValueChanged = onDisplayNameChanged,
                showDivider = true
            )

            ProfileInfoItem(
                icon = Icons.Filled.Email,
                title = "Email",
                value = user.email,
                isEditMode = false, // Email không cho chỉnh sửa
                showDivider = true
            )

            ProfileInfoItem(
                icon = Icons.Filled.Phone,
                title = "Số điện thoại",
                value = if (isEditMode) phone else user.phone,
                isEditMode = isEditMode,
                onValueChanged = onPhoneChanged,
                showDivider = false
            )
        }
    }
}

@Composable
fun AddressSection(
    addresses: List<UserAddress>,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Địa chỉ giao hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                TextButton(
                    onClick = onAddClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Thêm địa chỉ",
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Thêm", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            addresses.forEach { address ->
                AddressItem(
                    address = address,
                    onEditClick = { onEditClick(address.id) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AddressItem(
    address: UserAddress,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (address.isDefault)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            else Color.White
        ),
        border = if (address.isDefault) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = address.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        if (address.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("Mặc định", fontSize = 10.sp)
                            }
                        }
                    }

                    Text(
                        text = address.fullAddress,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AccountInfoSection(
    userId: String,
    joinDate: String,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thông tin tài khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InfoRow(
                label = "Mã người dùng",
                value = userId,
                showDivider = true
            )

            InfoRow(
                label = "Ngày tham gia",
                value = joinDate,
                showDivider = true
            )

            InfoRow(
                label = "Trạng thái",
                value = when (status) {
                    "ACTIVE" -> "Đang hoạt động"
                    "INACTIVE" -> "Không hoạt động"
                    "SUSPENDED" -> "Tạm khóa"
                    else -> status
                },
                valueColor = when (status) {
                    "ACTIVE" -> Color.Green
                    else -> Color.Red
                },
                showDivider = false
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Gray,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = valueColor
            )
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Nút đổi mật khẩu
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onChangePasswordClick,
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Đổi mật khẩu",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Đổi mật khẩu",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Nút đăng xuất
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLogoutClick,
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = "Đăng xuất",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EditModeActions(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text("Hủy")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Lưu thay đổi")
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Đổi mật khẩu", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mật khẩu hiện tại") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (showPassword) "Ẩn mật khẩu" else "Hiện mật khẩu"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                        onConfirm()
                    }
                },
                enabled = newPassword == confirmPassword && newPassword.isNotEmpty() && currentPassword.isNotEmpty()
            ) {
                Text("Đổi mật khẩu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    isEditMode: Boolean = false,
    onValueChanged: (String) -> Unit = {},
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                if (isEditMode && title != "Email") {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 36.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    MaterialTheme {
        UserProfileScreen(
            onLogoutClick = {},
            onBackClick = {},
            onAddAddressClick = {},
            onEditAddressClick = {},
            onChangePasswordClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenEditModePreview() {
    MaterialTheme {
        var isEditMode by remember { mutableStateOf(true) }

        Scaffold(
            containerColor = Color.White,
            floatingActionButton = {
                if (!isEditMode) {
                    ExtendedFloatingActionButton(
                        onClick = { isEditMode = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Chỉnh sửa"
                            )
                        },
                        text = { Text("Chỉnh sửa") }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        ProfileHeader(onBackClick = {})
                        UserInfoHeader(
                            user = UserProfile(),
                            isEditMode = true,
                            displayName = "Nguyễn Văn A",
                            phone = "0901234567",
                            onDisplayNameChanged = {},
                            onPhoneChanged = {}
                        )
                    }

                    item {
                        AddressSection(
                            addresses = listOf(
                                UserAddress("addr_1", "Nhà", "Tòa A, Phòng 101", true),
                                UserAddress("addr_2", "Công ty", "Tầng 5, Tòa nhà XYZ")
                            ),
                            onAddClick = {},
                            onEditClick = {}
                        )
                    }

                    item {
                        AccountInfoSection(
                            userId = "uid_xxx",
                            joinDate = "05/01/2026",
                            status = "ACTIVE"
                        )
                    }

                    item {
                        ActionButtonsSection(
                            onChangePasswordClick = {},
                            onLogoutClick = {}
                        )
                    }
                }

                if (isEditMode) {
                    EditModeActions(
                        onSave = { isEditMode = false },
                        onCancel = { isEditMode = false }
                    )
                }
            }
        }
    }
}
 */