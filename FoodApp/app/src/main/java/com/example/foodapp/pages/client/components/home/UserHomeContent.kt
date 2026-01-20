package com.example.foodapp.pages.client.components.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.foodapp.data.model.shared.category.Category
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.home.*

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
            categories.forEach { if (it.isActive) put(it.id, it.name) }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = { UserBottomNav(navController = navController, onProfileClick = onProfileClick) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                HomeHeaderSection(
                    nameState = nameState,
                    categoryState = categoryState,
                    categoryMap = categoryMap,
                    selectedCategory = selectedCategory,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onCategorySelected = { categoryId ->
                        selectedCategory = categoryId
                        expanded = false
                        onFilterByCategory(categoryId)
                    },
                    searchQuery = searchQuery,
                    onSearch = onSearch,
                    onClearSearch = onClearSearch
                )
            }

            item(span = { GridItemSpan(2) }) {
                ResultsHeader(
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    categoryMap = categoryMap,
                    resultCount = if(productState is ProductState.Success) products.size else 0,
                    onClearSearch = onClearSearch,
                    onClearFilter = {
                        selectedCategory = null
                        onFilterByCategory(null)
                    }
                )
            }

            productListStates(
                productState = productState,
                products = products,
                isLoadingMore = isLoadingMore,
                hasMore = hasMore,
                onProductClick = onProductClick,
                onLoadMore = onLoadMore,
                onRefresh = onRefresh
            )
        }
    }
}
