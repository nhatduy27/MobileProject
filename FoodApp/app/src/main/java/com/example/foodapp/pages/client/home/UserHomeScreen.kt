package com.example.foodapp.presentation.view.user.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.home.HomeViewModel
import com.example.foodapp.pages.client.home.ProductState
import com.example.foodapp.pages.client.home.UserNameState
import com.example.foodapp.pages.client.components.UserBottomNav
import com.example.foodapp.pages.client.components.UserCategoryList
import com.example.foodapp.pages.client.components.UserHeader
import com.example.foodapp.pages.client.components.UserProductCard
import com.example.foodapp.pages.client.components.UserSearchBar

@Composable
fun UserHomeScreen(
    navController: NavHostController,
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current

    // Khởi tạo ViewModel
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )

    // Quan sát các state từ ViewModel
    val nameState by viewModel.userNameState.observeAsState(UserNameState.Idle)
    val productState by viewModel.productState.observeAsState(ProductState.Idle)
    val products by viewModel.products.observeAsState(emptyList())
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)
    val hasMore by viewModel.hasMore.observeAsState(true)

    // Gọi API khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
        viewModel.getProducts() // Lấy sản phẩm lần đầu
    }

    // Xử lý khi search - Đã sử dụng trong UserSearchBar
    val onSearch: (String) -> Unit = { query ->
        viewModel.searchProducts(query)
    }

    // Gọi hàm Content để hiển thị giao diện
    UserHomeContent(
        navController = navController,
        nameState = nameState,
        productState = productState,
        products = products,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick,
        onSearch = onSearch,
        onRefresh = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMoreProducts() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserHomeContent(
    navController: NavHostController,
    nameState: UserNameState,
    productState: ProductState,
    products: List<Product>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
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
            // Phần Header và Danh mục (Chiếm 2 cột)
            item(span = { GridItemSpan(2) }) {
                Column {
                    // Lựa chọn 1: Hiển thị trực tiếp
                    SimpleUserHeader(nameState = nameState)

                    // Lựa chọn 2: Sử dụng SearchBar với callback
                    UserSearchBarWithCallback(onSearch = onSearch)

                    // Tạm thời sử dụng CategoryList
                    UserCategoryList()

                    Text(
                        text = "Món ăn phổ biến",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Xử lý các trạng thái loading/error/empty
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Đã xảy ra lỗi")
                                Text(text = (productState as ProductState.Error).message)
                                Button(onClick = onRefresh) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    }
                }
                is ProductState.Empty -> {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Không tìm thấy sản phẩm nào")
                                Button(onClick = onRefresh) {
                                    Text("Làm mới")
                                }
                            }
                        }
                    }
                }
                is ProductState.Success -> {
                    // Danh sách sản phẩm
                    items(products) { product ->
                        UserProductCard(
                            product = product,
                            onClick = { onProductClick(product) }
                        )
                    }

                    // Load more indicator
                    if (isLoadingMore) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (hasMore && products.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = onLoadMore) {
                                    Text("Xem thêm sản phẩm")
                                }
                            }
                        }
                    }
                }
                else -> {
                    // ProductState.Idle - không hiển thị gì
                }
            }
        }
    }
}

// Component hiển thị header đơn giản
@Composable
fun SimpleUserHeader(nameState: UserNameState) {
    val userName = when (nameState) {
        is UserNameState.Success -> nameState.userName
        is UserNameState.Loading -> "Đang tải..."
        is UserNameState.Error -> "Khách"
        is UserNameState.Empty -> "Khách"
        else -> "Khách"
    }

    // Hiển thị header đơn giản
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Xin chào, $userName!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// SearchBar với callback
@Composable
fun UserSearchBarWithCallback(onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Tìm kiếm món ăn...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        trailingIcon = {
            IconButton(onClick = { onSearch(searchText) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm"
                )
            }
        }
    )
}