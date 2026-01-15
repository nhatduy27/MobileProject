package com.example.foodapp.presentation.view.user.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Thêm state cho categories từ API
    val categoryState by viewModel.categoryState.observeAsState(CategoryState.Idle)
    val categories by viewModel.categories.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
        viewModel.getProducts()
        viewModel.fetchCategories() // Thêm fetch categories
    }

    UserHomeContent(
        navController = navController,
        nameState = nameState,
        productState = productState,
        products = products,
        categories = categories, // Truyền categories từ API
        categoryState = categoryState, // Truyền category state
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        searchQuery = searchQuery,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick,
        onSearch = { query -> viewModel.searchProducts(query) },
        onClearSearch = { viewModel.clearSearch() },
        onRefresh = {
            viewModel.refresh()
            viewModel.fetchCategories() // Refresh categories
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
    categories: List<Category>, // Thêm parameter categories
    categoryState: CategoryState, // Thêm parameter category state
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
    // State cho spinner filter
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Tạo map categories từ API (thay vì mock data)
    val categoryMap = remember(categories) {
        buildMap<String?, String> {
            put(null, "Tất cả") // Mục mặc định
            categories.forEach { category ->
                if (category.isActive) { // Chỉ lấy categories active
                    put(category.id, category.name)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
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
            contentPadding = PaddingValues(8.dp)
        ) {
            // Header với search và filter
            item(span = { GridItemSpan(2) }) {
                Column {
                    // Dòng 1: Header với filter spinner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SimpleUserHeader(nameState = nameState)

                        // Filter spinner với data từ API
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

                    // Dòng 2: Search bar
                    EnhancedSearchBar(
                        onSearch = onSearch,
                        onClearSearch = onClearSearch,
                        currentQuery = searchQuery
                    )

                    // Dòng 3: Hiển thị kết quả filter hoặc search
                    if (searchQuery.isNotBlank()) {
                        SearchResultsHeader(
                            query = searchQuery,
                            resultCount = products.size,
                            onClear = onClearSearch
                        )
                    } else if (selectedCategory != null) {
                        // Lấy tên category từ categoryMap
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
                        // Tiêu đề món ăn phổ biến
                        Text(
                            text = "Món ăn phổ biến",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Trạng thái hiển thị danh sách sản phẩm
            when (productState) {
                is ProductState.Loading -> {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
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
                    // Danh sách sản phẩm chính
                    items(products) { product ->
                        UserProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }

                    // Nút "Xem thêm" hoặc Loading khi load phân trang
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
        // Xác định nội dung hiển thị trên button dựa vào state
        val buttonContent: @Composable () -> Unit = when (categoryState) {
            is CategoryState.Loading -> {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            is CategoryState.Error -> {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lỗi",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = categoryMap[selectedCategory] ?: "Tất cả",
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                // Chỉ cho phép mở dropdown khi không phải đang loading
                if (categoryState !is CategoryState.Loading) {
                    onExpandedChange(true)
                }
            },
            modifier = Modifier.width(140.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            enabled = categoryState !is CategoryState.Loading && categoryMap.isNotEmpty()
        ) {
            buttonContent()
        }

        // Dropdown menu chỉ hiển thị khi không có lỗi và đã có data
        if (expanded && categoryState !is CategoryState.Loading && categoryState !is CategoryState.Error) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.width(140.dp)
            ) {
                // Thêm item "Tất cả" đầu tiên
                DropdownMenuItem(
                    text = { Text("Tất cả") },
                    onClick = {
                        onCategorySelected(null)
                    }
                )

                // Thêm các categories từ API
                categoryMap.forEach { (id, name) ->
                    // Bỏ qua item null (Tất cả) đã thêm ở trên
                    if (id != null) {
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                onCategorySelected(id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleUserHeader(nameState: UserNameState) {
    val userName = when (nameState) {
        is UserNameState.Success -> nameState.userName
        is UserNameState.Loading -> "Đang tải..."
        else -> "Khách"
    }
    Text(
        text = "Xin chào, $userName!",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun EnhancedSearchBar(
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    currentQuery: String
) {
    var searchText by remember { mutableStateOf(currentQuery) }

    // Cập nhật searchText khi currentQuery thay đổi từ ViewModel
    LaunchedEffect(currentQuery) {
        searchText = currentQuery
    }

    OutlinedTextField(
        value = searchText,
        onValueChange = { newText ->
            searchText = newText
            onSearch(newText)
        },
        placeholder = { Text("Tìm kiếm món ăn...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
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
                        contentDescription = "Xóa tìm kiếm"
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun SearchResultsHeader(
    query: String,
    resultCount: Int,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Kết quả tìm kiếm: \"$query\"",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = "$resultCount món ăn",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            onClick = onClear,
            modifier = Modifier.height(36.dp)
        ) {
            Text("Xóa", fontSize = 13.sp)
        }
    }
}

@Composable
fun FilteredResultsHeader(
    categoryName: String,
    resultCount: Int,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Danh mục: $categoryName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = "$resultCount món ăn",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            onClick = onClear,
            modifier = Modifier.height(36.dp)
        ) {
            Text("Xóa lọc", fontSize = 13.sp)
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Lỗi: $message", color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}

@Composable
fun EmptyView(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = "Không có sản phẩm",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không có sản phẩm nào",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRefresh) {
            Text("Làm mới")
        }
    }
}

@Composable
fun LoadMoreSection(isLoadingMore: Boolean, hasMore: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoadingMore) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        } else if (hasMore) {
            TextButton(onClick = onLoadMore) {
                Text("Xem thêm sản phẩm")
            }
        }
    }
}