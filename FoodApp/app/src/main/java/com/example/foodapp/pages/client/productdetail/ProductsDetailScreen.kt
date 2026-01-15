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

    // Load product detail khi vào màn hình
    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            println("DEBUG: [UserProductDetailScreen] Loading product detail for: $productId")
            viewModel.getProductDetail(productId)
        }
    }

    // Reset favorite state khi thành công
    LaunchedEffect(favoriteState) {
        if (favoriteState is FavoriteState.Success) {
            // Sau 2 giây reset state
            kotlinx.coroutines.delay(2000)
            viewModel.resetFavoriteState()
        }
    }

    // Hiển thị Snackbar khi có thông báo
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Hiển thị Snackbar khi có thành công/error từ favoriteState
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
            // Bottom bar chứa số lượng và 2 button
            if (product != null) {
                ProductBottomBar(
                    product = product!!,
                    quantity = quantity,
                    onQuantityChange = { newQuantity -> quantity = newQuantity },
                    onAddToCart = {
                        // TODO: Xử lý thêm vào giỏ hàng
                        println("Thêm vào giỏ hàng: ${product!!.name} x $quantity")
                    },
                    onBuyNow = {
                        // TODO: Xử lý mua ngay
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = "Đang tải thông tin sản phẩm...",
                                color = Color.Gray
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Lỗi",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Đã xảy ra lỗi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = state.message,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.getProductDetail(productId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9800)
                                    )
                                ) {
                                    Text("Thử lại")
                                }

                                Button(
                                    onClick = onBackPressed,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.LightGray,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Quay lại",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Quay lại")
                                    }
                                }
                            }
                        }
                    }
                }
                is ProductDetailState.Success -> {
                    // Sử dụng product từ LiveData thay vì từ state để có cập nhật real-time
                    val currentProduct = product ?: state.product

                    // Debug log để kiểm tra trạng thái yêu thích
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
                else -> {
                    // ProductDetailState.Idle - không hiển thị gì
                }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button quay lại kế bên tiêu đề
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Chi tiết sản phẩm",
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF9800)  // Màu cam giống bottom bar
        ),
        // KHÔNG CÓ actions nữa - bỏ nút trái tim trên top bar
    )
}

@Composable
fun ProductDetailContent(
    product: Product,
    isLoading: Boolean = false,
    onFavoriteClick: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    // Debug log
    println("DEBUG: [ProductDetailContent] Rendering product - ID: ${product.id}, isFavorite: ${product.isFavorite}, isLoading: $isLoading")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Product Image với nút yêu thích
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
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
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🍜",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Không có ảnh",
                            color = Color.Gray
                        )
                    }
                }
            }

            // Badge trạng thái (phía trên bên phải)
            if (!product.isAvailable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "HẾT HÀNG",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nút yêu thích trên ảnh (phía dưới bên phải)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                FavoriteIconOnImage(
                    isFavorite = product.isFavorite == true,
                    isLoading = isLoading,
                    onClick = onFavoriteClick
                )
            }
        }

        // Product Info
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Rating and sold count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${String.format("%.1f", product.rating)} (${product.totalRatings} đánh giá)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Đã bán: ${product.soldCount}",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }

            // Price
            Text(
                text = if (product.price is String) product.price
                else product.price.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Description
            Text(
                text = "Mô tả",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = product.description ?: "Không có mô tả",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Color(0xFF444444),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Shop Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = "Cửa hàng",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thông tin cửa hàng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = product.shopName ?: "Không có thông tin cửa hàng",
                        fontSize = 16.sp,
                        color = Color(0xFF444444)
                    )
                }
            }

            // Preparation Time và trạng thái
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Preparation Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Thời gian chuẩn bị",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chuẩn bị: ${product.preparationTime ?: 0} phút",
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                }

                // Availability status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (product.isAvailable) Color(0xFF4CAF50)
                                else Color.Red
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (product.isAvailable) "Còn hàng" else "Hết hàng",
                        fontSize = 16.sp,
                        color = if (product.isAvailable) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Khoảng trống để không bị bottom bar che
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun FavoriteIconOnImage(
    isFavorite: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    // Debug log để kiểm tra
    println("DEBUG: [FavoriteIconOnImage] isFavorite: $isFavorite, isLoading: $isLoading")

    Surface(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color = Color.Red
                )
            } else {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxSize(),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = if (isFavorite) "Bỏ yêu thích" else "Thêm vào yêu thích",
                        tint = if (isFavorite) Color.Red else Color(0xFF666666),
                        modifier = Modifier.size(32.dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần chọn số lượng
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Số lượng:",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Container cho nút - + và số lượng
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút giảm (-)
                    IconButton(
                        onClick = {
                            if (quantity > 1) {
                                onQuantityChange(quantity - 1)
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = product.isAvailable
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Giảm số lượng",
                            tint = if (quantity > 1 && product.isAvailable) Color(0xFF333333) else Color(0xFFCCCCCC)
                        )
                    }

                    // Số lượng
                    Box(
                        modifier = Modifier.width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = quantity.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (product.isAvailable) Color.Black else Color.Gray
                        )
                    }

                    // Nút tăng (+)
                    IconButton(
                        onClick = {
                            // Có thể thêm giới hạn tối đa nếu cần
                            onQuantityChange(quantity + 1)
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = product.isAvailable
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tăng số lượng",
                            tint = if (product.isAvailable) Color(0xFF333333) else Color(0xFFCCCCCC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Hai button bên phải
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button Thêm vào giỏ hàng
                Button(
                    onClick = onAddToCart,
                    enabled = product.isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF9800),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        disabledContentColor = Color.Gray
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Thêm vào giỏ",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Giỏ hàng",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Button Mua ngay
                Button(
                    onClick = onBuyNow,
                    enabled = product.isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Mua ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}