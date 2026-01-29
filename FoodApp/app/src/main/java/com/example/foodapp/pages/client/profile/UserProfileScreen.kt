package com.example.foodapp.pages.client.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.Client
import com.example.foodapp.data.remote.client.response.profile.AddressResponse
import com.example.foodapp.pages.client.components.profile.*
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit = {},
    onUserInfoClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onOrderButtonClick: () -> Unit = {},
    onReviewsButtonClick: () -> Unit = {},
    onChatsButtonClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(context)
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State cho các popup
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showEditAddressDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSetDefaultConfirmDialog by remember { mutableStateOf(false) }

    var addressToEdit by remember { mutableStateOf<AddressResponse?>(null) }
    var addressToDelete by remember { mutableStateOf<String?>(null) }
    var addressToSetDefault by remember { mutableStateOf<String?>(null) }

    // Observe state từ ViewModel
    val userState by viewModel.userState.observeAsState()
    val addresses by viewModel.addresses.observeAsState(emptyList())
    val createAddressState by viewModel.createAddressState.observeAsState()
    val deleteAddressState by viewModel.deleteAddressState.observeAsState()
    val updateAddressState by viewModel.updateAddressState.observeAsState()
    val setDefaultAddressState by viewModel.setDefaultAddressState.observeAsState()

    // Xử lý khi thêm địa chỉ thành công
    LaunchedEffect(createAddressState) {
        createAddressState?.let { state ->
            when (state) {
                is CreateAddressState.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                        showAddAddressDialog = false
                    }
                    viewModel.resetCreateAddressState()
                }
                else -> {}
            }
        }
    }

    // Xử lý khi cập nhật địa chỉ thành công
    LaunchedEffect(updateAddressState) {
        updateAddressState?.let { state ->
            when (state) {
                is UpdateAddressState.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                        showEditAddressDialog = false
                        addressToEdit = null
                    }
                    viewModel.resetUpdateAddressState()
                }
                else -> {}
            }
        }
    }

    // Xử lý khi xóa địa chỉ thành công
    LaunchedEffect(deleteAddressState) {
        deleteAddressState?.let { state ->
            when (state) {
                is DeleteAddressState.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                        showDeleteConfirmDialog = false
                        addressToDelete = null
                    }
                    viewModel.resetDeleteAddressState()
                }
                is DeleteAddressState.Error -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    showDeleteConfirmDialog = false
                    addressToDelete = null
                    viewModel.resetDeleteAddressState()
                }
                else -> {}
            }
        }
    }

    // Xử lý khi đặt địa chỉ mặc định thành công
    LaunchedEffect(setDefaultAddressState) {
        setDefaultAddressState?.let { state ->
            when (state) {
                is SetDefaultAddressState.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                        showSetDefaultConfirmDialog = false
                        addressToSetDefault = null
                    }
                    viewModel.resetSetDefaultAddressState()
                }
                is SetDefaultAddressState.Error -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    showSetDefaultConfirmDialog = false
                    addressToSetDefault = null
                    viewModel.resetSetDefaultAddressState()
                }
                else -> {}
            }
        }
    }

    // Popup thêm địa chỉ
    if (showAddAddressDialog) {
        AddAddressDialog(
            viewModel = viewModel,
            createAddressState = createAddressState,
            onDismiss = {
                showAddAddressDialog = false
                viewModel.resetCreateAddressState()
            }
        )
    }

    // Popup chỉnh sửa địa chỉ
    if (showEditAddressDialog && addressToEdit != null) {
        EditAddressDialog(
            address = addressToEdit!!,
            viewModel = viewModel,
            updateAddressState = updateAddressState,
            onDismiss = {
                showEditAddressDialog = false
                addressToEdit = null
                viewModel.resetUpdateAddressState()
            }
        )
    }

    // Dialog xác nhận xóa địa chỉ
    if (showDeleteConfirmDialog && addressToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                addressToDelete = null
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa địa chỉ này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressToDelete?.let { viewModel.deleteAddress(it) }
                    },
                    enabled = deleteAddressState !is DeleteAddressState.Loading
                ) {
                    if (deleteAddressState is DeleteAddressState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Xóa")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        addressToDelete = null
                    },
                    enabled = deleteAddressState !is DeleteAddressState.Loading
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog xác nhận đặt địa chỉ mặc định
    if (showSetDefaultConfirmDialog && addressToSetDefault != null) {
        AlertDialog(
            onDismissRequest = {
                showSetDefaultConfirmDialog = false
                addressToSetDefault = null
            },
            title = { Text("Đặt địa chỉ mặc định") },
            text = { Text("Bạn có muốn đặt địa chỉ này làm mặc định?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressToSetDefault?.let { viewModel.setDefaultAddress(it) }
                    },
                    enabled = setDefaultAddressState !is SetDefaultAddressState.Loading
                ) {
                    if (setDefaultAddressState is SetDefaultAddressState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Đồng ý")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSetDefaultConfirmDialog = false
                        addressToSetDefault = null
                    },
                    enabled = setDefaultAddressState !is SetDefaultAddressState.Loading
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            ProfileTopBar(
                onBackClick = onBackClick,
                onRefreshClick = { viewModel.fetchUserData() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        userState?.let { state ->
            when (state) {
                is ProfileState.Loading -> {
                    LoadingScreen(modifier = Modifier.padding(padding))
                }
                is ProfileState.Error -> {
                    ErrorScreen(
                        errorMessage = state.message,
                        onRetryClick = { viewModel.fetchUserData() },
                        modifier = Modifier.padding(padding)
                    )
                }
                is ProfileState.Success -> {
                    ProfileContent(
                        user = state.user,
                        addresses = state.addresses,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        onUserInfoClick = onUserInfoClick,
                        onAddAddressClick = { showAddAddressDialog = true },
                        onEditAddressClick = { addressId ->
                            val address = state.addresses.find { it.id == addressId }
                            if (address != null) {
                                addressToEdit = address
                                showEditAddressDialog = true
                            }
                        },
                        onChangePasswordClick = onChangePasswordClick,
                        onDeleteAddressClick = { addressId ->
                            addressToDelete = addressId
                            showDeleteConfirmDialog = true
                        },
                        onSetDefaultAddressClick = { addressId ->
                            addressToSetDefault = addressId
                            showSetDefaultConfirmDialog = true
                        },
                        onOrderButtonClick = onOrderButtonClick,
                        onReviewsButtonClick = onReviewsButtonClick,
                        onChatsButtonClick = onChatsButtonClick // Thêm callback
                    )
                }
                is ProfileState.Idle -> {
                    LoadingScreen(modifier = Modifier.padding(padding))
                }
            }
        } ?: run {
            LoadingScreen(modifier = Modifier.padding(padding))
        }
    }
}

@Composable
fun ProfileContent(
    user: Client,
    addresses: List<AddressResponse>,
    modifier: Modifier = Modifier,
    onUserInfoClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAddressClick: (String) -> Unit,
    onSetDefaultAddressClick: (String) -> Unit,
    onOrderButtonClick: () -> Unit,
    onReviewsButtonClick: () -> Unit,
    onChatsButtonClick: () -> Unit // Thêm tham số
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(vertical = 12.dp)
    ) {
        // SECTION: Đơn hàng & Đánh giá & Chat
        ActionButtonsSection(
            onOrderButtonClick = onOrderButtonClick,
            onReviewsButtonClick = onReviewsButtonClick,
            onChatsButtonClick = onChatsButtonClick // Truyền callback
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION: Thông tin người dùng
        UserInfoButtonSection(onUserInfoClick = onUserInfoClick)

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION: Địa chỉ
        if (addresses.isNotEmpty()) {
            AddressCard(
                addresses = addresses,
                onAddClick = onAddAddressClick,
                onEditClick = onEditAddressClick,
                onDeleteClick = onDeleteAddressClick,
                onSetDefaultClick = onSetDefaultAddressClick
            )
        } else {
            EmptyAddressCard(onAddClick = onAddAddressClick)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION: Cài đặt
        SettingsCard(
            onChangePasswordClick = onChangePasswordClick
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ActionButtonsSection(
    onOrderButtonClick: () -> Unit,
    onReviewsButtonClick: () -> Unit,
    onChatsButtonClick: () -> Unit // Thêm tham số
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Button Đơn mua
            ProfileActionItem(
                onClick = onOrderButtonClick,
                icon = Icons.Outlined.ShoppingBag, // Icon cho đơn hàng
                iconTint = Color(0xFFFBBB00),
                title = "Đơn mua",
                showDivider = true
            )

            // Button Đoạn chat
            ProfileActionItem(
                onClick = onChatsButtonClick,
                icon = Icons.Outlined.ChatBubbleOutline,
                iconTint = Color(0xFFFBBB00),
                title = "Đoạn chat",
                showDivider = true
            )

            // Button Đánh giá
            ProfileActionItem(
                onClick = onReviewsButtonClick,
                icon = Icons.Outlined.RateReview,
                iconTint = Color(0xFFFBBB00),
                title = "Đánh giá của tôi",
                showDivider = false
            )
        }
    }
}

@Composable
fun ProfileActionItem(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Xem chi tiết",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFFBBB00)
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
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFBBB00)
            )
        ) {
            Text("Thử lại")
        }
    }
}