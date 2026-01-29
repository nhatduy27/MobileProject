package com.example.foodapp.pages.owner.customer

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.foodapp.data.model.owner.buyer.BuyerListItem
import com.example.foodapp.data.model.owner.buyer.BuyerTier
import com.example.foodapp.pages.owner.notifications.NotificationBell
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

// --- 1. COMPONENT ĐIỀU HƯỚNG CHÍNH ---
@Composable
fun CustomerScreenMain(onMenuClick: () -> Unit) {
    var currentScreen by remember { mutableStateOf("LIST") }
    var selectedBuyerId by remember { mutableStateOf<String?>(null) }
    var isReadOnly by remember { mutableStateOf(false) }

    BackHandler(enabled = currentScreen == "ADD") {
        currentScreen = "LIST"
        selectedBuyerId = null
        isReadOnly = false
    }

    AnimatedContent(
        targetState = currentScreen,
        label = "ScreenTransition",
        transitionSpec = {
            (fadeIn(tween(300)) + slideInHorizontally { fullWidth -> fullWidth })
                .togetherWith(fadeOut(tween(300)) + slideOutHorizontally { fullWidth -> -fullWidth })
        }
    ) { screen ->
        when (screen) {
            "LIST" -> CustomerScreen(
                onNavigateToAdd = {
                    selectedBuyerId = null
                    isReadOnly = false
                    currentScreen = "ADD"
                },
                onBuyerClick = { buyer ->
                    selectedBuyerId = buyer.customerId
                    isReadOnly = true
                    currentScreen = "ADD"
                },
                onMenuClick = onMenuClick
            )
            "ADD" -> AddCustomerScreen(
                onBack = {
                    currentScreen = "LIST"
                    selectedBuyerId = null
                    isReadOnly = false
                },
                customerId = selectedBuyerId,
                isReadOnly = isReadOnly
            )
        }
    }
}


// --- 2. MÀN HÌNH DANH SÁCH KHÁCH HÀNG ---
@Composable
fun CustomerScreen(
    customerViewModel: CustomerViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onBuyerClick: (BuyerListItem) -> Unit,
    onMenuClick: () -> Unit
) {
    val uiState by customerViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // Detect when reaching end of list for pagination
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= uiState.buyers.size - 5
        }
    }
    
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !uiState.isLoadingMore && uiState.currentPage < uiState.totalPages) {
            customerViewModel.loadMoreBuyers()
        }
    }

    Scaffold(
        topBar = {
            ExpandableSearchHeader(
                query = uiState.searchQuery,
                onQueryChange = customerViewModel::onSearchQueryChanged,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = OwnerColors.Primary,
                contentColor = OwnerColors.Surface,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm mới")
            }
        },
        containerColor = OwnerColors.Background
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
            
            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OwnerColors.Primary)
                }
            }
            // Error state
            else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "Đã xảy ra lỗi",
                            color = OwnerColors.Error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { customerViewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Primary)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            // Empty state
            else if (uiState.buyers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có khách hàng nào",
                        color = OwnerColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Content
            else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Stats
                    item {
                        val vipCount = uiState.buyers.count { it.tier == BuyerTier.VIP }
                        val normalCount = uiState.buyers.count { it.tier == BuyerTier.NORMAL }
                        val newCount = uiState.buyers.count { it.tier == BuyerTier.NEW }
                        
                        CustomerStats(
                            totalCustomers = uiState.totalBuyers,
                            vipCustomers = vipCount,
                            regularCustomers = normalCount,
                            newCustomers = newCount
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(items = uiState.buyers, key = { it.customerId }) { buyer ->
                        CustomerCard(
                            buyer = buyer,
                            onClick = { onBuyerClick(buyer) }
                        )
                    }
                    
                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = OwnerColors.Primary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
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
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit
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
            .height(64.dp)
            .background(OwnerColors.Surface)
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = OwnerColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Khách hàng",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.TextPrimary
                    )
                }
                
                // Notification Bell Icon
                NotificationBell()
                
                IconButton(
                    onClick = { isSearchActive = true },
                    modifier = Modifier
                        .background(OwnerColors.SurfaceVariant, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = OwnerColors.TextPrimary)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OwnerColors.TextPrimary)
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
                            cursorColor = OwnerColors.Primary
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
