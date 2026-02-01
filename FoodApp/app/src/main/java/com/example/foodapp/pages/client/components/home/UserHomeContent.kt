package com.example.foodapp.pages.client.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodapp.R
import com.example.foodapp.data.model.shared.category.Category
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.home.*

@OptIn(ExperimentalMaterial3Api::class)
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
    isSearching: Boolean = false,
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onFilterByCategory: (String?) -> Unit,
    onPageChange: (Int) -> Unit,
    onShopViewClick: () -> Unit,
    onChatBotClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val allCategoriesText = stringResource(R.string.all_categories)

    val categoryMap = remember(categories, allCategoriesText) {
        mutableMapOf<String?, String>().apply {
            this[null] = allCategoriesText
            categories.forEach { category ->
                if (category.isActive) {
                    this[category.id] = category.name
                }
            }
        }
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { UserBottomNav(navController = navController, onProfileClick = onProfileClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onChatBotClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 70.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = stringResource(R.string.chat_with_bot)
                )
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                ShopViewButton(onClick = onShopViewClick)
            }

            item(span = { GridItemSpan(2) }) {
                SearchStatusSection(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    resultCount = if (productState is ProductState.Success) products.size else 0,
                    onClearSearch = onClearSearch
                )
            }

            item(span = { GridItemSpan(2) }) {
                ResultsHeader(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    categoryMap = categoryMap,
                    resultCount = if (productState is ProductState.Success) products.size else 0,
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
                isLoadingMore = isLoadingMore && !isSearching,
                hasMore = hasMore && !isSearching,
                onProductClick = onProductClick,
                onLoadMore = onLoadMore,
                onRefresh = onRefresh
            )

            if (!isSearching && totalPages > 1 && totalItems > 0) {
                item(span = { GridItemSpan(2) }) {
                    PaginationSection(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        totalItems = totalItems,
                        onPageChange = onPageChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopViewButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF6B35),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = stringResource(R.string.shop),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.view_all_shops),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SearchStatusSection(
    isSearching: Boolean,
    searchQuery: String,
    resultCount: Int,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSearching || searchQuery.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSearching)
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.searching_for, searchQuery),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = stringResource(R.string.found_results_count, resultCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (searchQuery.isNotEmpty() && !isSearching) {
                    TextButton(
                        onClick = onClearSearch,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.clear),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultsHeader(
    isSearching: Boolean = false,
    searchQuery: String,
    selectedCategory: String?,
    categoryMap: Map<String?, String>,
    resultCount: Int,
    onClearSearch: () -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSearching) return

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categoryName = categoryMap[selectedCategory] ?: stringResource(R.string.all_categories)

                    Icon(
                        imageVector = Icons.Default.ViewModule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            stringResource(R.string.products_found_count, resultCount)
                        } else {
                            stringResource(R.string.category_and_products_count, categoryName, resultCount)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (searchQuery.isNotEmpty() || selectedCategory != null) {
                    TextButton(
                        onClick = {
                            if (searchQuery.isNotEmpty()) onClearSearch()
                            if (selectedCategory != null) onClearFilter()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.clear),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationSection(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1 || totalItems <= 0) return

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.pagination_summary, totalItems, currentPage, totalPages),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onPageChange(currentPage - 1) },
                    enabled = currentPage > 1,
                    modifier = Modifier.size(44.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (currentPage > 1)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.previous_page),
                                tint = if (currentPage > 1)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pageNumbers = generatePageNumbers(currentPage, totalPages)

                    pageNumbers.forEach { page ->
                        when (page) {
                            "..." -> {
                                Text(
                                    text = "⋯",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            else -> {
                                val pageNum = page.toInt()
                                val isCurrentPage = pageNum == currentPage

                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = !isCurrentPage) {
                                            onPageChange(pageNum)
                                        },
                                    color = if (isCurrentPage)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    shadowElevation = if (isCurrentPage) 3.dp else 0.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = page,
                                            color = if (isCurrentPage)
                                                Color.White
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isCurrentPage)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { onPageChange(currentPage + 1) },
                    enabled = currentPage < totalPages,
                    modifier = Modifier.size(44.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (currentPage < totalPages)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = stringResource(R.string.next_page),
                                tint = if (currentPage < totalPages)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generatePageNumbers(currentPage: Int, totalPages: Int): List<String> {
    return when {
        totalPages <= 7 -> {
            (1..totalPages).map { it.toString() }
        }
        currentPage <= 3 -> {
            listOf("1", "2", "3", "4", "5", "...", totalPages.toString())
        }
        currentPage >= totalPages - 2 -> {
            listOf("1", "...",
                (totalPages - 4).toString(),
                (totalPages - 3).toString(),
                (totalPages - 2).toString(),
                (totalPages - 1).toString(),
                totalPages.toString())
        }
        else -> {
            listOf("1", "...",
                (currentPage - 1).toString(),
                currentPage.toString(),
                (currentPage + 1).toString(),
                "...",
                totalPages.toString())
        }
    }
}