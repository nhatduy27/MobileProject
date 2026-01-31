package com.example.foodapp.pages.client.orderdetail

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.order.OrderApiModel
import com.example.foodapp.data.remote.client.response.payment.GetPaymentData
import com.example.foodapp.data.remote.client.response.payment.*
import com.example.foodapp.data.remote.client.response.review.ProductReviewRequest
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ============== DATA CLASSES ==============

data class TimelineStep(
    val status: String,
    val label: String,
    val icon: ImageVector,
    val timestamp: String?
)

// ============== ORDER DETAIL SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    orderId: String,
) {
    val context = LocalContext.current
    val viewModel: OrderDetailViewModel = viewModel(factory = OrderDetailViewModel.factory(context))
    val orderDetailState by viewModel.orderDetailState.observeAsState(OrderDetailState.Idle)
    val cancelOrderState by viewModel.cancelOrderState.observeAsState(CancelOrderState.Idle)
    val paymentInfoState by viewModel.paymentInfoState.observeAsState(PaymentInfoState.Idle)
    val reviewState by viewModel.reviewState.observeAsState(ReviewState.Idle)
    val currentOrder by viewModel.currentOrder.observeAsState()
    val hasReviewed by viewModel.hasReviewed.observeAsState(false)

    var showCancelDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showReviewSuccessDialog by remember { mutableStateOf(false) }
    var showPaymentInfoDialog by remember { mutableStateOf(false) }

    // Review dialog state
    var shopRating by remember { mutableIntStateOf(0) }
    var shopComment by remember { mutableStateOf("") }
    var shipperRating by remember { mutableIntStateOf(0) }
    var shipperComment by remember { mutableStateOf("") }
    val productReviews = remember { mutableStateMapOf<String, ProductReviewUI>() }

    // Initialize product reviews when order loads
    LaunchedEffect(currentOrder) {
        currentOrder?.let { order ->
            order.items.forEach { item ->
                if (!productReviews.containsKey(item.productId)) {
                    productReviews[item.productId] = ProductReviewUI(
                        productId = item.productId,
                        productName = item.productName,
                        rating = 0,
                        comment = ""
                    )
                }
            }
        }
    }

    // Load order detail when screen opens
    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetail(orderId)
    }

    // Handle cancel order state
    LaunchedEffect(cancelOrderState) {
        when (cancelOrderState) {
            is CancelOrderState.Success -> {
                showCancelDialog = false
                viewModel.fetchOrderDetail(orderId)
                viewModel.resetCancelState()
            }
            else -> {}
        }
    }

    // Check if should show SEPAY button
    val showSepayButton = remember(currentOrder) {
        currentOrder?.let { order ->
            order.paymentMethod == "SEPAY" && order.paymentStatus != "PAID"
        } ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.order_detail_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF212121),
                    navigationIconContentColor = Color(0xFF212121)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (orderDetailState) {
                is OrderDetailState.Loading -> {
                    LoadingView()
                }
                is OrderDetailState.Success -> {
                    currentOrder?.let { order ->
                        OrderDetailContent(
                            order = order,
                            hasReviewed = hasReviewed,
                            showSepayButton = showSepayButton,
                            onCancelOrder = { showCancelDialog = true },
                            onReviewOrder = {
                                shopRating = 0
                                shopComment = ""
                                shipperRating = 0
                                shipperComment = ""
                                productReviews.clear()
                                order.items.forEach { item ->
                                    productReviews[item.productId] = ProductReviewUI(
                                        productId = item.productId,
                                        productName = item.productName,
                                        rating = 0,
                                        comment = ""
                                    )
                                }
                                showReviewDialog = true
                            },
                            onShowPaymentInfo = {
                                viewModel.fetchPaymentInfo(orderId)
                                showPaymentInfoDialog = true
                            }
                        )
                    }
                }
                is OrderDetailState.Error -> {
                    ErrorView(
                        message = (orderDetailState as OrderDetailState.Error).message,
                        onRetry = { viewModel.fetchOrderDetail(orderId) }
                    )
                }
                is OrderDetailState.Empty -> {
                    EmptyView()
                }
                else -> {}
            }

            // Cancel order loading overlay
            if (cancelOrderState is CancelOrderState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Cancel Dialog
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (cancelOrderState !is CancelOrderState.Loading) {
                        showCancelDialog = false
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.cancel_order_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(stringResource(R.string.cancel_order_confirmation))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.cancelOrder(orderId)
                        },
                        enabled = cancelOrderState !is CancelOrderState.Loading
                    ) {
                        if (cancelOrderState is CancelOrderState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFFF44336),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.cancel_order_button), color = Color(0xFFF44336))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (cancelOrderState !is CancelOrderState.Loading) {
                                showCancelDialog = false
                            }
                        },
                        enabled = cancelOrderState !is CancelOrderState.Loading
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showPaymentInfoDialog) {
            PaymentInfoDialog(
                paymentInfoState = paymentInfoState,
                onDismiss = {
                    showPaymentInfoDialog = false
                    viewModel.resetPaymentInfoState()
                },
                onExit = {
                    showPaymentInfoDialog = false
                    viewModel.resetPaymentInfoState()
                }
            )
        }

        // Review Dialog
        if (showReviewDialog) {
            EnhancedReviewDialog(
                currentOrder = currentOrder,
                isLoading = reviewState is ReviewState.Loading,
                shopRating = shopRating,
                shopComment = shopComment,
                shipperRating = shipperRating,
                shipperComment = shipperComment,
                productReviews = productReviews,
                onShopRatingChange = { rating ->
                    shopRating = rating
                },
                onShopCommentChange = { comment ->
                    shopComment = comment
                },
                onShipperRatingChange = { rating ->
                    shipperRating = rating
                },
                onShipperCommentChange = { comment ->
                    shipperComment = comment
                },
                onProductRatingChange = { productId, rating ->
                    productReviews[productId] = productReviews[productId]?.copy(rating = rating)
                        ?: return@EnhancedReviewDialog
                },
                onProductCommentChange = { productId, comment ->
                    productReviews[productId] = productReviews[productId]?.copy(comment = comment)
                        ?: return@EnhancedReviewDialog
                },
                onSubmit = { shopRating, shopComment, shipperRating, shipperComment, productReviews ->
                    // ĐÓNG DIALOG REVIEW TRƯỚC
                    showReviewDialog = false

                    // Convert to request format
                    val productReviewRequests = productReviews.values.map { review ->
                        ProductReviewRequest(
                            productId = review.productId,
                            rating = review.rating,
                            comment = if (review.comment.isNotBlank()) review.comment else ""
                        )
                    }

                    // Gọi API (chạy ngầm)
                    viewModel.createOrderReview(
                        orderId = orderId,
                        shopRating = shopRating,
                        shopComment = if (shopComment.isNotBlank()) shopComment else null,
                        productReviews = productReviewRequests
                    )

                    // HIỂN THỊ THÔNG BÁO THÀNH CÔNG NGAY
                    showReviewSuccessDialog = true
                },
                onDismiss = {
                    showReviewDialog = false
                    viewModel.resetReviewState()
                },
                errorMessage = if (reviewState is ReviewState.Error)
                    (reviewState as ReviewState.Error).message
                else null
            )
        }

        // Review Success Dialog
        if (showReviewSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showReviewSuccessDialog = false
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.success_title),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                },
                text = {
                    Text(stringResource(R.string.review_submitted_success))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showReviewSuccessDialog = false
                            // Refresh lại order detail sau khi đánh giá
                            viewModel.fetchOrderDetail(orderId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun PaymentInfoDialog(
    paymentInfoState: PaymentInfoState,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (paymentInfoState !is PaymentInfoState.Loading) {
                onDismiss()
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            when (paymentInfoState) {
                is PaymentInfoState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                is PaymentInfoState.Success -> {
                    PaymentInfoContent(
                        paymentData = paymentInfoState.paymentData,
                        onDismiss = onDismiss,
                        onExit = onExit
                    )
                }

                is PaymentInfoState.Error -> {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = paymentInfoState.message,
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // CHỈ CÒN 1 NÚT "ĐÓNG"
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    }
                }

                is PaymentInfoState.Idle -> {
                    // Nothing to show
                }
            }
        }
    }
}

@Composable
fun PaymentInfoContent(
    paymentData: GetPaymentData,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sepay_payment_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
            }
        }

        // QR Code
        paymentData.payment?.providerData?.qrCodeUrl?.let { qrCodeUrl ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = qrCodeUrl),
                        contentDescription = stringResource(R.string.qr_code_content_description),
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .padding(16.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.scan_qr_to_pay),
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Payment Details
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentInfoRow(stringResource(R.string.transaction_code), paymentData.payment?.providerData?.sepayContent ?: "")
            PaymentInfoRow(stringResource(R.string.amount_label), formatPrice(paymentData.payment?.amount?.toDouble() ?: 0.0 ))
            PaymentInfoRow(stringResource(R.string.account_number_label), paymentData.payment?.providerData?.accountNumber ?: "")
            PaymentInfoRow(stringResource(R.string.account_name_label), paymentData.payment?.providerData?.accountName ?: "")
            PaymentInfoRow(stringResource(R.string.bank_name_label), paymentData.payment?.providerData?.bankCode ?: "")

        }

        Divider(color = Color(0xFFE0E0E0))

        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.cancel))
        }
    }
}

@Composable
fun PaymentInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReviewDialog(
    currentOrder: OrderApiModel?,
    isLoading: Boolean,
    shopRating: Int,
    shopComment: String,
    shipperRating: Int,
    shipperComment: String,
    productReviews: Map<String, ProductReviewUI>,
    onShopRatingChange: (Int) -> Unit,
    onShopCommentChange: (String) -> Unit,
    onShipperRatingChange: (Int) -> Unit,
    onShipperCommentChange: (String) -> Unit,
    onProductRatingChange: (String, Int) -> Unit,
    onProductCommentChange: (String, String) -> Unit,
    onSubmit: (Int, String, Int, String, Map<String, ProductReviewUI>) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null
) {
    var showValidationError by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.order_review_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = {
                            if (!isLoading) {
                                onDismiss()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }

                // Error message (if any)
                errorMessage?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                fontSize = 14.sp,
                                color = Color(0xFFF44336),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Shop Rating Section
                ReviewSection(
                    title = stringResource(R.string.shop_review_title),
                    subtitle = stringResource(R.string.shop_review_subtitle),
                    currentRating = shopRating,
                    currentComment = shopComment,
                    onRatingChange = onShopRatingChange,
                    onCommentChange = onShopCommentChange,
                    isLoading = isLoading
                )

                // Shipper Rating Section
                ReviewSection(
                    title = stringResource(R.string.shipper_review_title),
                    subtitle = stringResource(R.string.shipper_review_subtitle),
                    currentRating = shipperRating,
                    currentComment = shipperComment,
                    onRatingChange = onShipperRatingChange,
                    onCommentChange = onShipperCommentChange,
                    isLoading = isLoading,
                    showOptional = true
                )

                // Product Reviews Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.product_review_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    currentOrder?.items?.forEach { item ->
                        val productReview = productReviews[item.productId] ?: return@forEach

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = item.productName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                // Star rating for product
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (i in 1..5) {
                                        IconButton(
                                            onClick = {
                                                if (!isLoading) {
                                                    onProductRatingChange(item.productId, i)
                                                }
                                            },
                                            enabled = !isLoading,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = stringResource(R.string.star_rating_description, i),
                                                modifier = Modifier.size(32.dp),
                                                tint = if (i <= productReview.rating) Color(0xFFFFD700) else Color.LightGray
                                            )
                                        }
                                    }
                                }

                                // Product comment
                                OutlinedTextField(
                                    value = productReview.comment,
                                    onValueChange = {
                                        if (!isLoading) {
                                            onProductCommentChange(item.productId, it)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(stringResource(R.string.product_comment_placeholder))
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedIndicatorColor = Color(0xFF4CAF50),
                                        unfocusedIndicatorColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    maxLines = 2,
                                    enabled = !isLoading
                                )
                            }
                        }
                    }
                }

                // Validation error
                if (showValidationError) {
                    Text(
                        text = stringResource(R.string.review_validation_error),
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = {
                            if (!isLoading) {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    // Submit button
                    Button(
                        onClick = {
                            // Validate
                            val hasShopRating = shopRating > 0
                            val hasAllProductRatings = productReviews.values.all { it.rating > 0 }

                            if (!hasShopRating || !hasAllProductRatings) {
                                showValidationError = true
                            } else {
                                showValidationError = false
                                onSubmit(shopRating, shopComment, shipperRating, shipperComment, productReviews)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.submit_review_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSection(
    title: String,
    subtitle: String,
    currentRating: Int,
    currentComment: String,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    isLoading: Boolean,
    showOptional: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            if (showOptional) {
                Text(
                    text = stringResource(R.string.optional_label),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // Star rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..5) {
                IconButton(
                    onClick = {
                        if (!isLoading) {
                            onRatingChange(i)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.star_rating_description, i),
                        modifier = Modifier.size(40.dp),
                        tint = if (i <= currentRating) Color(0xFFFFD700) else Color.LightGray
                    )
                }
            }
        }

        // Comment
        OutlinedTextField(
            value = currentComment,
            onValueChange = {
                if (!isLoading) {
                    onCommentChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (showOptional) {
                    Text(stringResource(R.string.optional_comment_placeholder))
                } else {
                    Text(stringResource(R.string.shop_comment_placeholder))
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFF4CAF50),
                unfocusedIndicatorColor = Color.LightGray
            ),
            shape = RoundedCornerShape(8.dp),
            maxLines = 2,
            enabled = !isLoading
        )
    }
}

@Composable
fun OrderDetailContent(
    order: OrderApiModel,
    hasReviewed: Boolean,
    showSepayButton: Boolean,
    onCancelOrder: () -> Unit,
    onReviewOrder: () -> Unit,
    onShowPaymentInfo: () -> Unit
) {
    val canReview = order.status == "DELIVERED" && !hasReviewed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Order Status Timeline
        OrderStatusTimeline(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Shop Info
        ShopInfoSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Order Items
        OrderItemsSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Delivery Info
        DeliveryInfoSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Payment Info
        PaymentInfoSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Order Summary
        OrderSummarySection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // THÊM: SEPAY Button (chỉ hiện khi phương thức SEPAY và chưa thanh toán)
        if (showSepayButton) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onShowPaymentInfo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.qr_code_payment_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action Buttons
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Review Button (only show if order is delivered and not reviewed yet)
            if (canReview) {
                Button(
                    onClick = onReviewOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.review_order_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Already Reviewed Button (show if order is delivered and already reviewed)
            if (order.status == "DELIVERED" && hasReviewed) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4CAF50)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.already_reviewed_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Cancel Button (only show if order can be cancelled)
            if (order.status == "PENDING" || order.status == "CONFIRMED") {
                Button(
                    onClick = onCancelOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cancel_order_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OrderStatusTimeline(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.order_status_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val steps = listOf(
                TimelineStep("PENDING", stringResource(R.string.order_status_pending), Icons.Default.ShoppingCart, order.createdAt),
                TimelineStep("CONFIRMED", stringResource(R.string.order_status_confirmed), Icons.Default.CheckCircle, order.confirmedAt),
                TimelineStep("PREPARING", stringResource(R.string.order_status_preparing), Icons.Default.Restaurant, order.preparingAt),
                TimelineStep("SHIPPING", stringResource(R.string.order_status_shipping), Icons.Default.LocalShipping, order.shippingAt),
                TimelineStep("DELIVERED", stringResource(R.string.order_status_delivered), Icons.Default.Done, order.deliveredAt)
            )

            val currentStatusIndex = steps.indexOfFirst { it.status == order.status }

            steps.forEachIndexed { index, step ->
                val isCompleted = index <= currentStatusIndex
                val isActive = index == currentStatusIndex

                TimelineItem(
                    step = step,
                    isCompleted = isCompleted,
                    isActive = isActive,
                    isLast = index == steps.size - 1
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    step: TimelineStep,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(50),
                color = when {
                    isActive -> Color(0xFF4CAF50)
                    isCompleted -> Color(0xFF4CAF50)
                    else -> Color(0xFFE0E0E0)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50)
                            else Color(0xFFE0E0E0)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = step.label,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted) Color(0xFF212121) else Color(0xFF9E9E9E)
            )

            step.timestamp?.let { timestamp ->
                Text(
                    text = formatDateTime(timestamp),
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ShopInfoSection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = order.shopName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = stringResource(R.string.order_code_display, order.orderNumber),
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun OrderItemsSection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.order_items_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = stringResource(R.string.quantity_label, item.quantity),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = item.productName,
                            fontSize = 14.sp,
                            color = Color(0xFF424242)
                        )
                    }
                    Text(
                        text = formatPrice(item.subtotal),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryInfoSection(order: OrderApiModel) {
    order.deliveryAddress?.let { address ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.delivery_info_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = address.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = address.fullAddress,
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                        address.building?.let {
                            Text(
                                text = stringResource(R.string.building_label, it),
                                fontSize = 13.sp,
                                color = Color(0xFF757575)
                            )
                        }
                        address.room?.let {
                            Text(
                                text = stringResource(R.string.room_label, it),
                                fontSize = 13.sp,
                                color = Color(0xFF757575)
                            )
                        }
                        address.note?.let {
                            if (it.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.note_label, it),
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentInfoSection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.payment_method_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (order.paymentMethod == "COD")
                            Icons.Default.AccountBalanceWallet
                        else
                            Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text(
                        text = when (order.paymentMethod) {
                            "COD" -> stringResource(R.string.payment_method_cod)
                            "MOMO" -> stringResource(R.string.payment_method_momo)
                            "BANKING" -> stringResource(R.string.payment_method_banking)
                            else -> order.paymentMethod
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (order.paymentStatus) {
                        "PAID" -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                        "UNPAID" -> Color(0xFFFF5722).copy(alpha = 0.12f)
                        "FAILED" -> Color(0xFFF44336).copy(alpha = 0.12f)
                        "REFUNDED" -> Color(0xFF9E9E9E).copy(alpha = 0.12f)
                        else -> Color.Gray.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = when (order.paymentStatus) {
                            "PAID" -> stringResource(R.string.payment_status_paid)
                            "UNPAID" -> stringResource(R.string.payment_status_unpaid)
                            "FAILED" -> stringResource(R.string.payment_status_failed)
                            "REFUNDED" -> stringResource(R.string.payment_status_refunded)
                            else -> order.paymentStatus
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (order.paymentStatus) {
                            "PAID" -> Color(0xFF4CAF50)
                            "UNPAID" -> Color(0xFFFF5722)
                            "FAILED" -> Color(0xFFF44336)
                            "REFUNDED" -> Color(0xFF9E9E9E)
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummarySection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.order_summary_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(stringResource(R.string.subtotal_label), formatPrice(order.subtotal))
            SummaryRow(stringResource(R.string.shipping_fee_label), formatPrice(order.shipFee))
            if (order.discount > 0) {
                SummaryRow(
                    stringResource(R.string.discount_label),
                    stringResource(R.string.discount_value, formatPrice(order.discount)),
                    valueColor = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = formatPrice(order.total),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF424242)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.error_content_description),
                tint = Color(0xFFF44336),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = stringResource(R.string.no_order_content_description),
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = stringResource(R.string.no_order_found),
                fontSize = 16.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions
fun formatPrice(price: Double): String {
    return try {
        String.format("%,.0f", price) + "đ"
    } catch (e: Exception) {
        "0đ"
    }
}

fun formatDateTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}