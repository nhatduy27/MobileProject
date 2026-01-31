package com.example.foodapp.pages.client.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.foodapp.pages.client.components.home.UserHomeContent

@Composable
fun UserHomeScreen(
    navController: NavHostController,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onShopViewClick: () -> Unit,
    onChatBotClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))

    val nameState by viewModel.userNameState.observeAsState(UserNameState.Idle)
    val productState by viewModel.productState.observeAsState(ProductState.Idle)
    val products by viewModel.products.observeAsState(emptyList())
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    val isSearching by viewModel.isSearching.observeAsState(false)
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)
    val hasMore by viewModel.hasMore.observeAsState(true)
    val searchQuery by viewModel.searchQuery.observeAsState("")
    val categoryState by viewModel.categoryState.observeAsState(CategoryState.Idle)
    val categories by viewModel.categories.observeAsState(emptyList())

    // THÊM CÁC STATE PHÂN TRANG
    val currentPage by viewModel.currentPage.observeAsState(1)
    val totalPages by viewModel.totalPages.observeAsState(1)
    val totalItems by viewModel.totalItems.observeAsState(0)

    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
        viewModel.getProducts()
        viewModel.fetchCategories()
    }

    // Chọn hiển thị products nào: search results nếu đang search, không thì products thường
    val displayProducts = if (isSearching) searchResults else products

    UserHomeContent(
        navController = navController,
        nameState = nameState,
        productState = productState,
        products = displayProducts,
        categories = categories,
        categoryState = categoryState,
        isLoadingMore = isLoadingMore && !isSearching,
        hasMore = hasMore && !isSearching,
        searchQuery = searchQuery,
        isSearching = isSearching,
        currentPage = currentPage,
        totalPages = totalPages,
        totalItems = totalItems,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick,
        onChatBotClick = onChatBotClick,
        onSearch = viewModel::searchProducts,
        onClearSearch = viewModel::clearSearch,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMoreProducts,
        onFilterByCategory = viewModel::filterByCategory,
        onPageChange = viewModel::goToPage,
        onShopViewClick = onShopViewClick
    )
}