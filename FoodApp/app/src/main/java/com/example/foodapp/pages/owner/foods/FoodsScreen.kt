package com.example.foodapp.pages.owner.foods

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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import com.example.foodapp.R
import com.example.foodapp.data.model.owner.product.Product
import com.example.foodapp.pages.owner.notifications.NotificationBell
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

/**
 * Màn hình quản lý sản phẩm - FoodsScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsScreen(
    onMenuClick: () -> Unit,
    viewModel: FoodsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Filtered products
    val filteredProducts = remember(uiState.products, uiState.searchQuery) {
        viewModel.getFilteredProducts()
    }

    // Stats
    val stats = remember(uiState.products) {
        viewModel.getStats()
    }

    // Screen state: list or edit
    var isEditing by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error/success messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearSuccess()
        }
    }

    if (!isEditing) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                FoodsSearchHeader(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onMenuClick = onMenuClick,
                    onRefresh = { viewModel.refreshProducts() }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        isEditing = true
                    },
                    containerColor = OwnerColors.Primary,
                    contentColor = OwnerColors.Surface,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.foods_add_food))
                }
            },
            containerColor = OwnerColors.Background
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Loading indicator
                if (uiState.isLoading && uiState.products.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OwnerColors.Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.foods_loading), color = OwnerColors.TextSecondary)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filter Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(OwnerColors.Surface)
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.categories.forEach { category ->
                                FilterChip(
                                    category = category.name,
                                    isSelected = uiState.selectedCategoryName == category.name,
                                    onClick = { viewModel.onCategorySelected(category) }
                                )
                            }
                        }

                        // Product list with pull-to-refresh
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refreshProducts() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Statistics Cards
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        StatCard(
                                            title = stringResource(R.string.foods_total),
                                            value = stats.total.toString(),
                                            color = OwnerColors.Primary
                                        )
                                        StatCard(
                                            title = stringResource(R.string.foods_available),
                                            value = stats.available.toString(),
                                            color = OwnerColors.Success
                                        )
                                        StatCard(
                                            title = stringResource(R.string.foods_unavailable),
                                            value = stats.outOfStock.toString(),
                                            color = OwnerColors.Error
                                        )
                                    }
                                }

                                // Empty state
                                if (filteredProducts.isEmpty() && !uiState.isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = if (uiState.searchQuery.isNotEmpty())
                                                        stringResource(R.string.foods_not_found)
                                                    else
                                                        stringResource(R.string.foods_no_foods),
                                                    fontSize = 16.sp,
                                                    color = OwnerColors.TextSecondary
                                                )
                                                if (uiState.searchQuery.isEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = stringResource(R.string.foods_add_hint),
                                                        fontSize = 14.sp,
                                                        color = OwnerColors.TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Product items
                                items(filteredProducts, key = { it.id }) { product ->
                                    ProductItem(
                                        product = product,
                                        onClick = {
                                            editingProduct = product
                                            isEditing = true
                                        },
                                        onToggleAvailability = {
                                            viewModel.toggleAvailability(product.id, product.isAvailable)
                                        },
                                        onDelete = {
                                            viewModel.deleteProduct(product.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Add/Edit screen
        AddEditProductScreen(
            initialProduct = editingProduct,
            categories = uiState.categories.filter { it.id != null },
            isLoading = uiState.isCreating || uiState.isUpdating,
            onBack = {
                isEditing = false
                editingProduct = null
            },
            onSave = { name, description, price, categoryId, prepTime, imageFiles ->
                if (editingProduct == null) {
                    // Create new - must have at least 1 image
                    if (imageFiles.isNotEmpty()) {
                        viewModel.createProduct(
                            name = name,
                            description = description,
                            price = price,
                            categoryId = categoryId,
                            preparationTime = prepTime,
                            imageFiles = imageFiles,
                            onSuccess = {
                                isEditing = false
                                editingProduct = null
                            }
                        )
                    }
                } else {
                    // Update existing - imageFiles can be empty (keep existing images)
                    viewModel.updateProduct(
                        productId = editingProduct!!.id,
                        name = name,
                        description = description,
                        price = price,
                        categoryId = categoryId,
                        preparationTime = prepTime,
                        imageFiles = imageFiles.ifEmpty { null },
                        onSuccess = {
                            isEditing = false
                            editingProduct = null
                        }
                    )
                }
            }
        )
    }
}

/**
 * Search header with animation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onRefresh: () -> Unit
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
        // Normal header
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
                        Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.nav_dashboard), tint = OwnerColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.foods_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.TextPrimary
                    )
                }
                
                // Notification Bell Icon
                NotificationBell()

                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.owner_retry), tint = OwnerColors.TextPrimary)
                    }
                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier
                            .background(OwnerColors.SurfaceVariant, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.owner_search), tint = OwnerColors.TextPrimary)
                    }
                }
            }
        }

        // Search mode
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End, animationSpec = tween(300)),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End, animationSpec = tween(300))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isSearchActive = false
                        onQueryChange("")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.owner_close), tint = OwnerColors.TextPrimary)
                    }
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.foods_search_hint), color = OwnerColors.TextTertiary, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = OwnerColors.Primary
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.owner_close), tint = OwnerColors.TextTertiary)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
