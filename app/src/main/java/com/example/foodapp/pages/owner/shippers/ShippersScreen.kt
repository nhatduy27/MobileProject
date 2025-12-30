package com.example.foodapp.pages.owner.shippers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ShippersScreen(
    shippersViewModel: ShippersViewModel = viewModel()
) {
    val uiState by shippersViewModel.uiState.collectAsState()

    val filteredShippers = remember(uiState.shippers, uiState.selectedStatus, uiState.searchQuery) {
        shippersViewModel.getFilteredShippers()
    }

    var isEditing by remember { mutableStateOf(false) }
    var editingShipper by remember { mutableStateOf<com.example.foodapp.data.model.owner.Shipper?>(null) }

    if (!isEditing) {
        val totalShippers = remember(uiState.shippers) { shippersViewModel.getTotalShippers() }
        val activeShippers = remember(uiState.shippers) { shippersViewModel.getActiveShippers() }
        val todayDeliveries = remember(uiState.shippers) { shippersViewModel.getTodayDeliveries() }

        Scaffold(
            topBar = {
                ShippersSearchHeader(
                    query = uiState.searchQuery,
                    onQueryChange = shippersViewModel::onSearchQueryChanged
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingShipper = null
                        isEditing = true
                    },
                    containerColor = Color(0xFFFF6B35),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Thêm shipper")
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Status Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = listOf("Tất cả", "Đang rảnh", "Đang giao", "Nghỉ")
                    statuses.forEach { status ->
                        ShipperFilterChip(
                            status = status,
                            isSelected = uiState.selectedStatus == status,
                            onClick = { shippersViewModel.onStatusSelected(it) }
                        )
                    }
                }
                // Shippers List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Statistics ở đầu list để cuộn theo
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ShipperStatCard(
                                title = "Tổng shipper",
                                value = totalShippers.toString(),
                                color = Color(0xFFFF6B35),
                            )
                            ShipperStatCard(
                                title = "Đang hoạt động",
                                value = activeShippers.toString(),
                                color = Color(0xFF4CAF50),
                            )
                            ShipperStatCard(
                                title = "Đơn hôm nay",
                                value = todayDeliveries.toString(),
                                color = Color(0xFF2196F3),
                            )
                        }
                    }

                    items(filteredShippers) { shipper ->
                        ShipperCard(
                            shipper = shipper,
                            onClick = {
                                editingShipper = shipper
                                isEditing = true
                            }
                        )
                    }
                }
            }
        }
    } else {
        AddEditShipperScreen(
            initialShipper = editingShipper,
            onBack = {
                isEditing = false
                editingShipper = null
            },
            onSave = { shipper ->
                shippersViewModel.addOrUpdateShipper(shipper)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippersSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
        else focusManager.clearFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // STATE 1: Header bình thường
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn(tween(300)) + slideInHorizontally(),
            exit = fadeOut(tween(300)) + slideOutHorizontally { -it / 2 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Shipper", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text("Quản lý đội ngũ giao hàng", fontSize = 14.sp, color = Color.Gray)
                }
                IconButton(
                    onClick = { isSearchActive = true },
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1A1A1A))
                }
            }
        }

        // STATE 2: Thanh Search mở rộng
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End, animationSpec = tween(300)),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End, animationSpec = tween(300))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchActive = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A1A1A))
                    }
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Tìm tên, SĐT shipper...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFFFF6B35)
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
