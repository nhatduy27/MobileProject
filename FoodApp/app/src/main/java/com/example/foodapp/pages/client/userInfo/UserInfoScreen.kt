package com.example.foodapp.pages.client.userInfo

import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.data.model.client.Client
import com.example.foodapp.pages.client.components.profile.EditUserInfoDialog
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onBackClick: () -> Unit = {},
) {


    val context = LocalContext.current
    val viewModel: UserInfoViewModel = viewModel(
        factory = UserInfoViewModel.factory(context)
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe state từ ViewModel
    val userState by viewModel.userState.observeAsState()
    val currentUser by viewModel.currentUser.observeAsState()
    val uploadAvatarState by viewModel.uploadAvatarState.observeAsState()

    // State cho popup chỉnh sửa
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // State cho image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    handleImageSelection(it, context, viewModel, snackbarHostState)
                }
            }
        }
    )

    // Xử lý upload avatar state
    LaunchedEffect(uploadAvatarState) {
        when (val state = uploadAvatarState) {
            is UploadAvatarState.Loading -> {
                // Đang tải lên, có thể hiển thị loading nếu muốn
            }
            is UploadAvatarState.Success -> {
                val message = "Cập nhật ảnh đại diện thành công"
                viewModel.resetUploadAvatarState()
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            is UploadAvatarState.Error -> {
                val errorMessage = state.message
                viewModel.resetUploadAvatarState()
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Thông tin người dùng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    // Thêm nút refresh ở đây
                    IconButton(
                        onClick = {
                            viewModel.fetchUserData()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Đang tải lại thông tin...",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Tải lại trang",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEditProfileDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa thông tin",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        // Popup chỉnh sửa profile
        if (showEditProfileDialog) {
            EditUserInfoDialog(
                currentUser = currentUser,
                onDismiss = {
                    showEditProfileDialog = false
                },
                onUpdateSuccess = {
                    // Refresh lại data sau khi cập nhật thành công
                    viewModel.fetchUserData()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Cập nhật thông tin thành công",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }

        when (val state = userState) {
            is UserInfoState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
            is UserInfoState.Error -> {
                ErrorScreen(
                    errorMessage = state.message,
                    onRetryClick = { viewModel.fetchUserData() },
                    modifier = Modifier.padding(padding)
                )
            }
            is UserInfoState.Success -> {
                UserInfoContent(
                    user = state.user,
                    onAvatarClick = {
                        // Mở image picker khi nhấn vào avatar
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                )
            }
            else -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

private suspend fun handleImageSelection(
    uri: Uri,
    context: android.content.Context,
    viewModel: UserInfoViewModel,
    snackbarHostState: SnackbarHostState
) {
    try {
        // Tạo file từ URI
        val file = uri.toFile(context)

        if (file != null) {
            // Upload file
            viewModel.uploadAvatar(file)
        } else {
            snackbarHostState.showSnackbar(
                message = "Không thể đọc file ảnh",
                duration = SnackbarDuration.Short
            )
        }
    } catch (e: Exception) {
        snackbarHostState.showSnackbar(
            message = "Lỗi: ${e.message}",
            duration = SnackbarDuration.Short
        )
    }
}

// Extension function để chuyển URI thành File
private fun Uri.toFile(context: android.content.Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(this)
        val file = File.createTempFile("avatar_", ".jpg", context.cacheDir)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        file
    } catch (e: Exception) {
        null
    }
}

@Composable
fun UserInfoContent(
    user: Client,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Format role để hiển thị
    val roleDisplay = remember(user.role) {
        when (user.role) {
            "client" -> "Khách hàng"
            "admin" -> "Quản trị viên"
            else -> user.role
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        // Avatar và tên
        UserAvatarSection(user = user, onAvatarClick = onAvatarClick)

        // Thông tin cá nhân card
        PersonalInfoCard(user = user)

        // Trạng thái tài khoản
        AccountStatusCard(user = user)

        // Thông tin tài khoản
        AccountInfoCard(
            role = roleDisplay,
            isVerified = user.isVerify
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun UserAvatarSection(
    user: Client,
    onAvatarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar với clickable
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onAvatarClick,
                        enabled = true
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!user.imageAvatar.isNullOrEmpty()) {
                    // Hiển thị ảnh từ URL
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.imageAvatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay để chỉ ra rằng có thể click
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Thay đổi avatar",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    // Hiển thị avatar mặc định
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Thêm ảnh",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tên
            Text(
                text = user.fullName ?: "Chưa cập nhật",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Email
            Text(
                text = user.email ?: "Chưa có email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Hướng dẫn nhấn để thay đổi ảnh
            Text(
                text = "Nhấn vào ảnh để thay đổi",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PersonalInfoCard(user: Client) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Thông tin cá nhân",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tên đầy đủ
            InfoRow(
                label = "Họ và tên",
                value = user.fullName ?: "Chưa cập nhật"
            )

            // Số điện thoại
            InfoRow(
                label = "Số điện thoại",
                value = user.phone ?: "Chưa cập nhật"
            )

            // Email
            InfoRow(
                label = "Email",
                value = user.email ?: "Chưa có email"
            )
        }
    }
}

@Composable
fun AccountStatusCard(user: Client) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Trạng thái tài khoản",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xác minh tài khoản",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Badge(
                    containerColor = if (user.isVerify) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    contentColor = if (user.isVerify) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                ) {
                    Text(
                        text = if (user.isVerify) "Đã xác minh" else "Chưa xác minh",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AccountInfoCard(
    role: String,
    isVerified: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Thông tin tài khoản",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Vai trò
            InfoRow(
                label = "Vai trò",
                value = role
            )

            // Chỉ còn trạng thái xác minh
            InfoRow(
                label = "Trạng thái",
                value = if (isVerified) "Đã xác minh" else "Chưa xác minh"
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}