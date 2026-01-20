// com.example.foodapp.pages.client.favorites.FavoritesScreen.kt
package com.example.foodapp.pages.client.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.pages.client.components.home.UserBottomNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onProductClick :(String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.Factory.getInstance(context)
    )

    val favoritesState by viewModel.favoritesState.observeAsState()
    val removeState by viewModel.removeFavoriteState.observeAsState()
    val currentFavorites by viewModel.currentFavorites.observeAsState()

    // Xử lý khi có sự kiện xóa thành công
    LaunchedEffect(removeState) {
        when (removeState) {
            is RemoveFavoriteState.Success -> {
                // Reset state sau 2 giây
                kotlinx.coroutines.delay(2000)
                viewModel.resetRemoveFavoriteState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Yêu thích",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Nút refresh
                    IconButton(
                        onClick = { viewModel.refreshFavorites() },
                        enabled = favoritesState !is FavoritesState.Loading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (favoritesState is FavoritesState.Loading) Color.LightGray else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        },
        bottomBar = {
            UserBottomNav(navController = navController, onProfileClick = {})
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = favoritesState) {
                is FavoritesState.Loading -> {
                    LoadingContent()
                }
                is FavoritesState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refreshFavorites() }
                    )
                }
                is FavoritesState.Success -> {
                    val products = currentFavorites ?: emptyList()
                    if (products.isEmpty()) {
                        EmptyFavoritesContent()
                    } else {
                        FavoritesContent(
                            products = products,
                            onRemoveFavorite = { productId ->
                                viewModel.removeFromFavorites(productId)
                            },
                            onProductClick = onProductClick

                        )
                    }
                }
                // Xử lý trường hợp null hoặc các trạng thái khác (như Idle)
                else -> {
                    LoadingContent()
                }
            }

            // Hiển thị loading khi đang xóa
            if (removeState is RemoveFavoriteState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    products: List<com.example.foodapp.data.model.shared.product.Product>,
    onRemoveFavorite: (String) -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(products) { product ->
            FavoriteProductCard(
                product = product,
                onRemove = { onRemoveFavorite(product.id) },
                onClick = { onProductClick(product.id) }
            )
        }
    }
}

@Composable
private fun FavoriteProductCard(
    product: com.example.foodapp.data.model.shared.product.Product,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh sản phẩm
            if (!product.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍜", fontSize = 36.sp)
                }
            }

            // Thông tin sản phẩm
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tên sản phẩm
                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tên cửa hàng
                Text(
                    product.shopName ?: "Cửa hàng",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                // Giá và đánh giá
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Giá
                    Text(
                        product.price,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFFF9800)
                    )

                    // Đánh giá (nếu có)
                    if (product.rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⭐ ${String.format("%.1f", product.rating)}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )

                            if (product.totalRatings > 0) {
                                Text(
                                    "(${product.totalRatings})",
                                    fontSize = 10.sp,
                                    color = Color(0xFF9E9E9E),
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Nút yêu thích (đã thích)
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Color(0xFFFF9800))
        Text(
            "Đang tải danh sách yêu thích...",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "❌",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            message,
            fontSize = 14.sp,
            color = Color.Red,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "❤️",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "Không có sản phẩm yêu thích",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            "Hãy thêm những sản phẩm bạn thích",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}