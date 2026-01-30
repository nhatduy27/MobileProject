// com.example.foodapp.pages.client.favorites.FavoritesScreen.kt
package com.example.foodapp.pages.client.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.R
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
                kotlinx.coroutines.delay(2000)
                viewModel.resetRemoveFavoriteState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFFFF6F00)
                                )
                            )
                        )
                ) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        stringResource(R.string.favorites_title),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                    val count = currentFavorites?.size ?: 0
                                    if (count > 0) {
                                        Text(
                                            stringResource(R.string.product_count, count),
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.back_button),
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.refreshFavorites() },
                                enabled = favoritesState !is FavoritesState.Loading,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                if (favoritesState is FavoritesState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.refresh_button),
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        },
        bottomBar = {
            UserBottomNav(navController = navController, onProfileClick = {})
        },
        containerColor = Color(0xFFF8F9FA)
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
                else -> {
                    LoadingContent()
                }
            }

            // Hiển thì loading overlay khi đang xóa
            AnimatedVisibility(
                visible = removeState is RemoveFavoriteState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFFFF9800),
                                strokeWidth = 3.dp
                            )
                            Text(
                                stringResource(R.string.removing_favorite),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Snackbar khi xóa thành công
            AnimatedVisibility(
                visible = removeState is RemoveFavoriteState.Success,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.removed_from_favorites),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
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
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = products,
            key = { it.id }
        ) { product ->
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
    var isFavorite by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ảnh sản phẩm với gradient overlay
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFE0B2),
                                        Color(0xFFFFCC80)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍜", fontSize = 40.sp)
                    }
                }
            }

            // Thông tin sản phẩm
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tên sản phẩm
                    Text(
                        product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    // Tên cửa hàng với icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Text(
                            product.shopName ?: stringResource(R.string.default_shop_name),
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Giá và đánh giá
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Giá với background
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.1f)
                    ) {
                        Text(
                            product.price,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFFF6F00)
                        )
                    }

                    // Đánh giá
                    if (product.rating > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    String.format("%.1f", product.rating),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF424242)
                                )
                                if (product.totalRatings > 0) {
                                    Text(
                                        "(${product.totalRatings})",
                                        fontSize = 11.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Nút yêu thích với animation
            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                IconButton(
                    onClick = {
                        isFavorite = false
                        onRemove()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFEBEE),
                                    Color.White
                                )
                            )
                        )
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.remove_from_favorites_button),
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Loading animation với pulse effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF9800).copy(alpha = 0.2f),
                                Color(0xFFFF9800).copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFFFF9800),
                    strokeWidth = 4.dp
                )
            }

            Text(
                stringResource(R.string.loading_favorites),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Error icon với gradient background
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFEBEE),
                                Color(0xFFFCE4EC)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "❌",
                    fontSize = 48.sp
                )
            }

            Text(
                stringResource(R.string.load_favorites_error_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Text(
                message,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.retry),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            // Empty heart icon với animation
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFEBEE),
                                Color(0xFFFCE4EC),
                                Color.White
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color(0xFFE91E63).copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.empty_favorites_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF212121),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    stringResource(R.string.empty_favorites_subtitle),
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}