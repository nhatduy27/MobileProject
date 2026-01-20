package com.example.foodapp.pages.client.components.home


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.home.ProductState

fun LazyGridScope.productListStates(
    productState: ProductState,
    products: List<Product>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onProductClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    when (productState) {
        is ProductState.Loading -> {
            item(span = { GridItemSpan(2) }) { LoadingView() }
        }
        is ProductState.Error -> {
            item(span = { GridItemSpan(2) }) { ErrorView(message = productState.message, onRetry = onRefresh) }
        }
        is ProductState.Empty -> {
            item(span = { GridItemSpan(2) }) { EmptyView(onRefresh = onRefresh) }
        }
        is ProductState.Success -> {
            if (products.isEmpty()) { // Xử lý trường hợp search/filter không có kết quả
                item(span = { GridItemSpan(2) }) { NoResultsView() }
            } else {
                items(products, key = { it.id }) { product ->
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
        }
        is ProductState.Idle -> { /* Do nothing */ }
    }
}

@Composable
fun ResultsHeader(
    searchQuery: String,
    selectedCategory: String?,
    categoryMap: Map<String?, String>,
    resultCount: Int,
    onClearSearch: () -> Unit,
    onClearFilter: () -> Unit
) {
    when {
        searchQuery.isNotBlank() -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Kết quả cho '$searchQuery'", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("$resultCount kết quả", fontSize = 14.sp, color = Color.Gray)
                }
                IconButton(onClick = onClearSearch) { Icon(Icons.Default.Clear, "Xóa tìm kiếm") }
            }
        }
        selectedCategory != null -> {
            val categoryName = categoryMap[selectedCategory] ?: "Danh mục"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Lọc theo: $categoryName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("$resultCount kết quả", fontSize = 14.sp, color = Color.Gray)
                }
                IconButton(onClick = onClearFilter) { Icon(Icons.Default.Clear, "Xóa bộ lọc") }
            }
        }
        else -> {
            Text(
                "Món ăn phổ biến",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// Các View cho từng trạng thái
@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize().padding(vertical = 100.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFFFF9800))
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 100.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, "Lỗi", modifier = Modifier.size(50.dp), tint = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}

@Composable
private fun EmptyView(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 100.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.RestaurantMenu, "Trống", modifier = Modifier.size(50.dp), tint = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text("Chưa có sản phẩm nào.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRefresh) { Text("Tải lại") }
    }
}

@Composable
private fun NoResultsView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 100.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.SearchOff, "Không có kết quả", modifier = Modifier.size(50.dp), tint = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text("Không tìm thấy kết quả phù hợp.", textAlign = TextAlign.Center, color = Color.Gray)
    }
}


@Composable
private fun LoadMoreSection(isLoadingMore: Boolean, hasMore: Boolean, onLoadMore: () -> Unit) {
    when {
        isLoadingMore -> {
            Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        }
        hasMore -> {
            Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                OutlinedButton(
                    onClick = onLoadMore,
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Text("Xem thêm", color = Color(0xFFFF9800))
                }
            }
        }
        else -> {
            Text(
                "Bạn đã xem hết sản phẩm",
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
