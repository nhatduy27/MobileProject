package com.example.foodapp.pages.client.productdetail

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.components.productdetail.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProductDetailScreen(
    productId: String,
    onBackPressed: () -> Unit = {},
    onNavigateToPayment: (Product, Int) -> Unit = { _, _ -> }
) {
    val viewModel: ProductDetailViewModel = viewModel(
        factory = ProductDetailViewModel.factory(LocalContext.current)
    )

    val productDetailState by viewModel.productDetailState.observeAsState()
    val product by viewModel.product.observeAsState()
    val favoriteState by viewModel.favoriteState.observeAsState()
    val addToCartState by viewModel.addToCartState.observeAsState()
    val quantity by viewModel.quantity.observeAsState()

    // Load product detail
    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            viewModel.getProductDetail(productId)
        }
    }

    // Reset states after success
    LaunchedEffect(favoriteState) {
        if (favoriteState is FavoriteState.Success) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetFavoriteState()
        }
    }

    LaunchedEffect(addToCartState) {
        if (addToCartState is AddToCartState.Success) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetAddToCartState()
        }
    }

    // Snackbar handling
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

    LaunchedEffect(addToCartState) {
        when (val state = addToCartState) {
            is AddToCartState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is AddToCartState.Error -> {
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
                    quantity = quantity ?: 1,
                    onQuantityIncrease = { viewModel.increaseQuantity() },
                    onQuantityDecrease = { viewModel.decreaseQuantity() },
                    onAddToCart = { viewModel.addToCart() },
                    onBuyNow = { onNavigateToPayment(product!!, quantity ?: 1) },
                    isAddingToCart = addToCartState == AddToCartState.Loading
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF323232),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = productDetailState) {
                is ProductDetailState.Loading -> {
                    LoadingState()
                }
                is ProductDetailState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.getProductDetail(productId) },
                        onBackPressed = onBackPressed
                    )
                }
                is ProductDetailState.Success -> {
                    val currentProduct = product ?: state.product
                    ProductDetailContent(
                        product = currentProduct,
                        isLoading = favoriteState == FavoriteState.Loading,
                        onFavoriteClick = {
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

@Composable
private fun LoadingState() {
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
                text = stringResource(R.string.loading_product_info),
                color = Color(0xFF666666),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBackPressed: () -> Unit
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
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color(0xFFFFEBEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = stringResource(R.string.error_content_description),
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.error_occurred_title),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF212121)
            )
            Text(
                text = message,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.height(52.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.retry), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onBackPressed,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.height(52.dp),
                    border = BorderStroke(2.dp, Color(0xFFE0E0E0))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF424242)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.back_button), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                }
            }
        }
    }
}