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
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))

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
        onSearch = viewModel::searchProducts,
        onClearSearch = viewModel::clearSearch,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMoreProducts,
        onFilterByCategory = viewModel::filterByCategory
    )
}
