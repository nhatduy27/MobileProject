package com.example.foodapp.pages.owner.orders

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
import com.example.foodapp.data.model.owner.Order
import androidx.compose.ui.text.font.FontWeight
@Composable
fun OrdersScreen(
    ordersViewModel: OrdersViewModel = viewModel()
) {
    val uiState by ordersViewModel.uiState.collectAsState()

    val filteredOrders = remember(uiState.orders, uiState.selectedFilter, uiState.searchQuery) {
        ordersViewModel.getFilteredOrders()
    }

    var isEditing by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<Order?>(null) }

    if (!isEditing) {
        val totalOrders = remember(uiState.orders) { ordersViewModel.getTotalOrders() }
        val pendingOrders = remember(uiState.orders) { ordersViewModel.getPendingOrders() }
        val deliveringOrders = remember(uiState.orders) { ordersViewModel.getDeliveringOrders() }

        Scaffold(
            topBar = {
                OrdersSearchHeader(
                    query = uiState.searchQuery,
                    onQueryChange = ordersViewModel::onSearchQueryChanged
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingOrder = null
                        isEditing = true
                    },
                    containerColor = Color(0xFFFF6B35),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Thêm đơn hàng")
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val filters = listOf(
                    "Tất cả",
                    "Chờ xác nhận",
                    "Đang chuẩn bị",
                    "Đang giao",
                    "Hoàn thành",
                    "Đã hủy"
                )

                OrdersFilterRow(filters, uiState.selectedFilter) { selected ->
                    ordersViewModel.onFilterSelected(selected)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats ở đầu list để cuộn theo
                    item {
                        OrdersStatsRow(totalOrders, pendingOrders, deliveringOrders)
                    }

                    items(filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            onClick = {
                                editingOrder = order
                                isEditing = true
                            }
                        )
                    }
                }
            }
        }
    } else {
        AddEditOrderScreen(
            initialOrder = editingOrder,
            onBack = {
                isEditing = false
                editingOrder = null
            },
            onSave = { order ->
                ordersViewModel.addOrUpdateOrder(order)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersSearchHeader(
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
                    Text("Đơn hàng", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text("Quản lý đơn hàng", fontSize = 14.sp, color = Color.Gray)
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
                        placeholder = { Text("Tìm mã đơn, tên khách...", color = Color.Gray, fontSize = 14.sp) },
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
