// UserProductDetailScreen.kt
package com.example.foodapp.pages.client.productdetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.data.model.shared.product.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProductDetailScreen(
    productId: String,
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProductDetailViewModel = viewModel(
        factory = ProductDetailViewModel.factory(context)
    )

    val productDetailState by viewModel.productDetailState.observeAsState(ProductDetailState.Idle)
    val product by viewModel.product.observeAsState(null)
    val favoriteState by viewModel.favoriteState.observeAsState(FavoriteState.Idle)

    var quantity by remember { mutableStateOf(1) }

    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            println("DEBUG: [UserProductDetailScreen] Loading product detail for: $productId")
            viewModel.getProductDetail(productId)
        }
    }

    LaunchedEffect(favoriteState) {
        if (favoriteState is FavoriteState.Success) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetFavoriteState()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(favoriteState) {
        when (val state = favoriteState) {
            is FavoriteState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is FavoriteState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            ProductDetailTopBar(
                onBackPressed = onBackPressed
            )
        },
        bottomBar = {
            if (product != null) {
                ProductBottomBar(
                    product = product!!,
                    quantity = quantity,
                    onQuantityChange = { newQuantity -> quantity = newQuantity },
                    onAddToCart = {
                        println("Thêm vào giỏ hàng: ${product!!.name} x $quantity")
                    },
                    onBuyNow = {
                        println("Mua ngay: ${product!!.name} x $quantity")
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = productDetailState) {
                is ProductDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = Color(0xFFFF9800),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Đang tải thông tin sản phẩm...",
                                color = Color(0xFF666666),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is ProductDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = "Lỗi",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Text(
                                text = "Đã xảy ra lỗi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = state.message,
                                color = Color(0xFF757575),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.getProductDetail(productId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9800)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Thử lại", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = onBackPressed,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFE0E0E0))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF424242)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Quay lại", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                                }
                            }
                        }
                    }
                }
                is ProductDetailState.Success -> {
                    val currentProduct = product ?: state.product
                    println("DEBUG: [UserProductDetailScreen] Product loaded - ID: ${currentProduct.id}, isFavorite: ${currentProduct.isFavorite}")

                    ProductDetailContent(
                        product = currentProduct,
                        isLoading = favoriteState == FavoriteState.Loading,
                        onFavoriteClick = {
                            println("DEBUG: [UserProductDetailScreen] Favorite button clicked for product: ${currentProduct.id}")
                            viewModel.toggleFavorite(currentProduct.id)
                        },
                        onBackPressed = onBackPressed
                    )
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailTopBar(
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Chi tiết sản phẩm",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF9800)
        )
    )
}

@Composable
fun ProductDetailContent(
    product: Product,
    isLoading: Boolean = false,
    onFavoriteClick: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    println("DEBUG: [ProductDetailContent] Rendering product - ID: ${product.id}, isFavorite: ${product.isFavorite}, isLoading: $isLoading")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Product Image với gradient overlay và nút yêu thích
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            if (product.imageUrl != null && product.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay để làm nổi badge và button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF3E0),
                                    Color(0xFFFFE0B2)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🍜",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "Không có ảnh",
                            color = Color(0xFF757575),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Badge trạng thái
            if (!product.isAvailable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFD32F2F))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "HẾT HÀNG",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Nút yêu thích
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp)
            ) {
                FavoriteIconOnImage(
                    isFavorite = product.isFavorite == true,
                    isLoading = isLoading,
                    onClick = onFavoriteClick
                )
            }
        }

        // Product Info với card design
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Tên sản phẩm
            Text(
                text = product.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                lineHeight = 34.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Rating và sold count trong card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF8E1),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("%.1f", product.rating),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = " (${product.totalRatings})",
                            fontSize = 15.sp,
                            color = Color(0xFF757575)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = "Đã bán",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${product.soldCount} đã bán",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }

            // Price với background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF9800),
                                Color(0xFFFFB74D)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Giá bán",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (product.price is String) product.price
                        else product.price.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description section
            Text(
                text = "Mô tả sản phẩm",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAFAFA)
            ) {
                Text(
                    text = product.description ?: "Không có mô tả",
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Shop Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = "Cửa hàng",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Cửa hàng",
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = product.shopName ?: "Không có thông tin",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF212121)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Thông tin bổ sung
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preparation Time
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Thời gian",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${product.preparationTime ?: 0} phút",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = "Chuẩn bị",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }

                // Availability status
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (product.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (product.isAvailable) Color(0xFF4CAF50)
                                    else Color(0xFFD32F2F)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (product.isAvailable) "Còn hàng" else "Hết hàng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (product.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = "Trạng thái",
                                fontSize = 12.sp,
                                color = if (product.isAvailable) Color(0xFF66BB6A) else Color(0xFFEF5350)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun FavoriteIconOnImage(
    isFavorite: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    println("DEBUG: [FavoriteIconOnImage] isFavorite: $isFavorite, isLoading: $isLoading")

    Surface(
        modifier = Modifier
            .size(60.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        color = Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    strokeWidth = 3.dp,
                    color = Color(0xFFFF6B6B)
                )
            } else {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = if (isFavorite) "Bỏ yêu thích" else "Thêm vào yêu thích",
                        tint = if (isFavorite) Color(0xFFFF6B6B) else Color(0xFF9E9E9E),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductBottomBar(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        tonalElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần chọn số lượng
            Column(
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = "Số lượng",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF5F5F5),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                            modifier = Modifier.size(44.dp),
                            enabled = product.isAvailable && quantity > 1
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Giảm",
                                tint = if (quantity > 1 && product.isAvailable) Color(0xFF424242) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = quantity.toString(),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (product.isAvailable) Color(0xFF212121) else Color(0xFFBDBDBD)
                            )
                        }

                        IconButton(
                            onClick = { onQuantityChange(quantity + 1) },
                            modifier = Modifier.size(44.dp),
                            enabled = product.isAvailable
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Tăng",
                                tint = if (product.isAvailable) Color(0xFF424242) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Hai button
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Button Giỏ hàng
                OutlinedButton(
                    onClick = onAddToCart,
                    enabled = product.isAvailable,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, if (product.isAvailable) Color(0xFFFF9800) else Color(0xFFE0E0E0)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF9800),
                        disabledContentColor = Color(0xFFBDBDBD)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Button Mua ngay
                Button(
                    onClick = onBuyNow,
                    enabled = product.isAvailable,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "Mua ngay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}