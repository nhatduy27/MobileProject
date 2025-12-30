package com.example.foodapp.pages.owner.customer

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.foodapp.data.model.owner.Customer

// --- 1. COMPONENT ĐIỀU HƯỚNG CHÍNH ---
@Composable
fun CustomerScreenMain() {
    // State để quản lý màn hình hiện tại: "LIST" (danh sách) hoặc "ADD" (thêm mới)
    var currentScreen by remember { mutableStateOf("LIST") }

    // Lưu khách hàng đang được chọn (khi bấm vào từ danh sách)
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }

    // Cờ xác định đang ở chế độ chỉ xem (true) hay thêm/sửa (false)
    var isReadOnly by remember { mutableStateOf(false) }

    BackHandler(enabled = currentScreen == "ADD") {
        currentScreen = "LIST"
        selectedCustomer = null
        isReadOnly = false
    }

    // AnimatedContent tạo hiệu ứng chuyển đổi mượt mà giữa các màn hình
    AnimatedContent(
        targetState = currentScreen,
        label = "ScreenTransition",
        transitionSpec = {
            // Định nghĩa hiệu ứng trượt ngang
            (fadeIn(tween(300)) + slideInHorizontally { fullWidth -> fullWidth })
                .togetherWith(fadeOut(tween(300)) + slideOutHorizontally { fullWidth -> -fullWidth })
        }
    ) { screen ->
        // Dựa vào state để quyết định hiển thị màn hình nào
        when (screen) {
            "LIST" -> CustomerScreen(
                // Truyền một hàm để khi bấm nút, state sẽ đổi thành "ADD" (thêm mới)
                onNavigateToAdd = {
                    selectedCustomer = null
                    isReadOnly = false
                    currentScreen = "ADD"
                },
                // Khi bấm vào một khách hàng cụ thể -> mở màn hình xem chi tiết (readonly)
                onCustomerClick = { customer ->
                    selectedCustomer = customer
                    isReadOnly = true
                    currentScreen = "ADD"
                }
            )
            "ADD" -> AddCustomerScreen(
                // Truyền một hàm để khi bấm nút Back, state sẽ đổi lại thành "LIST"
                onBack = {
                    currentScreen = "LIST"
                    selectedCustomer = null
                    isReadOnly = false
                },
                customer = selectedCustomer,
                isReadOnly = isReadOnly
            )
        }
    }
}


// --- 2. MÀN HÌNH DANH SÁCH KHÁCH HÀNG ---
@Composable
fun CustomerScreen(
    customerViewModel: CustomerViewModel = viewModel(),
    onNavigateToAdd: () -> Unit, // Nhận hàm điều hướng từ CustomerScreenMain
    onCustomerClick: (Customer) -> Unit // Khi bấm vào 1 khách hàng trong danh sách
) {
    val uiState by customerViewModel.uiState.collectAsState()

    // Logic lọc danh sách
    val filteredCustomers = uiState.customers.filter { customer ->
        val typeMatches = uiState.selectedFilter == "Tất cả" || customer.type == uiState.selectedFilter
        val queryMatches = customer.name.contains(uiState.searchQuery, ignoreCase = true) ||
                customer.contact.contains(uiState.searchQuery, ignoreCase = true)
        typeMatches && queryMatches
    }

    Scaffold(
        topBar = {
            ExpandableSearchHeader(
                query = uiState.searchQuery,
                onQueryChange = customerViewModel::onSearchQueryChanged
            )
        },
        floatingActionButton = {
            // Nút FAB gọi hàm onNavigateToAdd khi được bấm
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Color(0xFFFF6B35),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm mới")
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CustomerFilterTabs(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = customerViewModel::onFilterChanged
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Chừa chỗ cho FAB
            ) {
                // Stats ở đầu list để cuộn cùng danh sách
                item {
                    CustomerStats(
                        uiState.customers.size,
                        uiState.customers.count { it.type == "VIP" },
                        uiState.customers.count { it.type == "Thường xuyên" },
                        uiState.customers.count { it.type == "Mới" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(items = filteredCustomers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onCustomerClick(customer) }
                    )
                }
            }
        }
    }
}

// --- 3. HEADER TÌM KIẾM ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSearchHeader(
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
                    Text("Khách hàng", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text("Quản lý danh sách", fontSize = 14.sp, color = Color.Gray)
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
                        placeholder = { Text("Tìm tên, SĐT...", color = Color.Gray, fontSize = 14.sp) },
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
