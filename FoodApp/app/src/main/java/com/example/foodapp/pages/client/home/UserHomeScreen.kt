package com.example.foodapp.presentation.view.user.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.foodapp.data.model.shared.category.Category
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.home.CategoryState
import com.example.foodapp.pages.client.home.HomeViewModel
import com.example.foodapp.pages.client.home.ProductState
import com.example.foodapp.pages.client.home.UserNameState
import com.example.foodapp.pages.client.components.UserBottomNav
import com.example.foodapp.pages.client.components.UserProductCard

@Composable
fun UserHomeScreen(
    navController: NavHostController,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )

    val nameState by viewModel.userNameState.observeAsState(UserNameState.Idle)
    val productState by viewModel.productState.observeAsState(ProductState.Idle)
    val products by viewModel.products.observeAsState(emptyList())
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)
    val hasMore by viewModel.hasMore.observeAsState(true)
    val searchQuery by viewModel.searchQuery.observeAsState("")

    val categoryState by viewModel.categoryState.observeAsState(CategoryState.Idle)
    val categories by viewModel.categories.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
        viewModel.getProducts()
        viewModel.fetchCategories()
    }

    UserHomeContent(
        navController = navController,
        nameState = nameState,
        productState = productState,
        products = products,
        categories = categories,
        categoryState = categoryState,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        searchQuery = searchQuery,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick,
        onSearch = { query -> viewModel.searchProducts(query) },
        onClearSearch = { viewModel.clearSearch() },
        onRefresh = {
            viewModel.refresh()
            viewModel.fetchCategories()
        },
        onLoadMore = { viewModel.loadMoreProducts() },
        onFilterByCategory = { categoryId -> viewModel.filterByCategory(categoryId) }
    )
}

@Composable
fun UserHomeContent(
    navController: NavHostController,
    nameState: UserNameState,
    productState: ProductState,
    products: List<Product>,
    categories: List<Category>,
    categoryState: CategoryState,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    searchQuery: String,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onFilterByCategory: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categoryMap = remember(categories) {
        buildMap<String?, String> {
            put(null, "Tất cả")
            categories.forEach { category ->
                if (category.isActive) {
                    put(category.id, category.name)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            UserBottomNav(
                navController = navController,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header section với gradient background
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFFFFB74D)
                                )
                            )
                        )
                        .padding(top = 16.dp, bottom = 24.dp)
                ) {
                    // Dòng 1: Header với avatar và filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EnhancedUserHeader(nameState = nameState)

                        CategoryFilterSpinner(
                            categoryState = categoryState,
                            categoryMap = categoryMap,
                            selectedCategory = selectedCategory,
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            onCategorySelected = { categoryId ->
                                selectedCategory = categoryId
                                expanded = false
                                onFilterByCategory(categoryId)
                            }
                        )
                    }

                    // Dòng 2: Search bar với elevation
                    EnhancedSearchBar(
                        onSearch = onSearch,
                        onClearSearch = onClearSearch,
                        currentQuery = searchQuery
                    )
                }
            }

            // Results header
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.background(Color(0xFFFAFAFA))) {
                    if (searchQuery.isNotBlank()) {
                        SearchResultsHeader(
                            query = searchQuery,
                            resultCount = products.size,
                            onClear = onClearSearch
                        )
                    } else if (selectedCategory != null) {
                        val categoryName = categoryMap[selectedCategory] ?: "Danh mục"
                        FilteredResultsHeader(
                            categoryName = categoryName,
                            resultCount = products.size,
                            onClear = {
                                selectedCategory = null
                                onFilterByCategory(null)
                            }
                        )
                    } else {
                        PopularDishesHeader()
                    }
                }
            }

            // Product list states
            when (productState) {
                is ProductState.Loading -> {
                    item(span = { GridItemSpan(2) }) {
                        LoadingView()
                    }
                }
                is ProductState.Error -> {
                    item(span = { GridItemSpan(2) }) {
                        ErrorView(
                            message = (productState as ProductState.Error).message,
                            onRetry = onRefresh
                        )
                    }
                }
                is ProductState.Empty -> {
                    item(span = { GridItemSpan(2) }) {
                        EmptyView(onRefresh = onRefresh)
                    }
                }
                is ProductState.Success -> {
                    items(products) { product ->
                        UserProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }

                    item(span = { GridItemSpan(2) }) {
                        LoadMoreSection(
                            isLoadingMore = isLoadingMore,
                            hasMore = hasMore,
                            onLoadMore = onLoadMore
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CategoryFilterSpinner(
    categoryState: CategoryState,
    categoryMap: Map<String?, String>,
    selectedCategory: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Box {
        val buttonContent: @Composable () -> Unit = when (categoryState) {
            is CategoryState.Loading -> {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                }
            }
            is CategoryState.Error -> {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Lỗi",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lỗi",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            else -> {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = categoryMap[selectedCategory] ?: "Tất cả",
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (categoryState !is CategoryState.Loading) {
                    onExpandedChange(true)
                }
            },
            modifier = Modifier
                .width(130.dp)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.25f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = categoryState !is CategoryState.Loading && categoryMap.isNotEmpty(),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            buttonContent()
        }

        if (expanded && categoryState !is CategoryState.Loading && categoryState !is CategoryState.Error) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .width(200.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Tất cả",
                            fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onCategorySelected(null) },
                    leadingIcon = {
                        if (selectedCategory == null) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                categoryMap.forEach { (id, name) ->
                    if (id != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    name,
                                    fontWeight = if (selectedCategory == id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { onCategorySelected(id) },
                            leadingIcon = {
                                if (selectedCategory == id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedUserHeader(nameState: UserNameState) {
    val userName = when (nameState) {
        is UserNameState.Success -> nameState.userName
        is UserNameState.Loading -> "..."
        else -> "Khách"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Column {
            Text(
                text = "Xin chào,",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = userName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun EnhancedSearchBar(
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    currentQuery: String
) {
    var searchText by remember { mutableStateOf(currentQuery) }

    LaunchedEffect(currentQuery) {
        searchText = currentQuery
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        TextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
                onSearch(newText)
            },
            placeholder = {
                Text(
                    "Tìm món ăn yêu thích...",
                    color = Color(0xFF9E9E9E),
                    fontSize = 15.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                if (searchText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            searchText = ""
                            onClearSearch()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFFF9800)
            )
        )
    }
}

@Composable
fun SearchResultsHeader(
    query: String,
    resultCount: Int,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF8E1),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "\"$query\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$resultCount món ăn",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            TextButton(
                onClick = onClear,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF9800)
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xóa", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun FilteredResultsHeader(
    categoryName: String,
    resultCount: Int,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE3F2FD),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = categoryName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "$resultCount món ăn",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            TextButton(
                onClick = onClear,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF1976D2)
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xóa", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun PopularDishesHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = "Món ăn phổ biến",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF212121)
        )
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = Color(0xFFFF9800)
            )
            Text(
                text = "Đang tải món ăn...",
                fontSize = 15.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFEBEE),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Lỗi",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Đã xảy ra lỗi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF212121)
            )

            Text(
                text = message,
                fontSize = 15.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyView(onRefresh: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍽️",
                    fontSize = 40.sp
                )
            }

            Text(
                text = "Không tìm thấy món ăn",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF212121)
            )

            Text(
                text = "Hãy thử tìm kiếm với từ khóa khác",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                border = BorderStroke(2.dp, Color(0xFFFF9800)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF9800)
                )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Làm mới", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun LoadMoreSection(isLoadingMore: Boolean, hasMore: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoadingMore) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = "Đang tải thêm...",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        } else if (hasMore) {
            Button(
                onClick = onLoadMore,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
                border = BorderStroke(2.dp, Color(0xFFFF9800)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Xem thêm sản phẩm",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}