package com.example.foodapp.pages.client.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.review.MyOrderReviewApiModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: ReviewsViewModel = viewModel(
        factory = ReviewsViewModel.factory(context)
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe state từ ViewModel
    val reviewsState by viewModel.reviewsState.observeAsState()
    val reviews by viewModel.reviews.observeAsState(emptyList())
    val deleteReviewState by viewModel.deleteReviewState.observeAsState()

    // State cho dialog xóa review
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<String?>(null) }

    // Lazy list state cho infinite scroll
    val lazyListState = rememberLazyListState()

    // Xử lý scroll để load more
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            if (lastVisibleItem >= totalItems - 3) {
                viewModel.loadMoreReviews()
            }
        }
    }

    // Xử lý khi xóa review thành công
    LaunchedEffect(deleteReviewState) {
        deleteReviewState?.let { state ->
            when (state) {
                is DeleteReviewState.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                        showDeleteDialog = false
                        reviewToDelete = null
                    }
                    viewModel.resetDeleteState()
                }
                is DeleteReviewState.Error -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    showDeleteDialog = false
                    reviewToDelete = null
                    viewModel.resetDeleteState()
                }
                else -> {}
            }
        }
    }

    // Dialog xác nhận xóa review
    if (showDeleteDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                reviewToDelete = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    stringResource(R.string.delete_review_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_review_confirmation),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { viewModel.deleteReview(it) }
                    },
                    enabled = deleteReviewState !is DeleteReviewState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (deleteReviewState is DeleteReviewState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            stringResource(R.string.delete),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        reviewToDelete = null
                    },
                    enabled = deleteReviewState !is DeleteReviewState.Loading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_reviews_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshReviews() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                            tint = Color(0xFFFBBB00)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(8.dp),
                        containerColor = Color(0xFF323232),
                        contentColor = Color.White
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (reviewsState) {
                is ReviewsState.Loading -> {
                    LoadingScreen(modifier = Modifier.padding(padding))
                }
                is ReviewsState.Error -> {
                    ErrorScreen(
                        errorMessage = (reviewsState as ReviewsState.Error).message,
                        onRetryClick = { viewModel.refreshReviews() },
                        modifier = Modifier.padding(padding)
                    )
                }
                is ReviewsState.Success -> {
                    if (reviews.isEmpty()) {
                        EmptyReviewsScreen(modifier = Modifier.padding(padding))
                    } else {
                        ReviewsList(
                            reviews = reviews,
                            lazyListState = lazyListState,
                            onEditClick = { reviewId, orderId ->
                                // TODO: Implement edit review
                            },
                            onDeleteClick = { reviewId ->
                                reviewToDelete = reviewId
                                showDeleteDialog = true
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
                is ReviewsState.Idle -> {
                    LoadingScreen(modifier = Modifier.padding(padding))
                }
                null -> {
                    LoadingScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun ReviewsList(
    reviews: List<MyOrderReviewApiModel>,
    lazyListState: LazyListState,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reviews, key = { it.id }) { review ->
            ReviewCard(
                review = review,
                onEditClick = { onEditClick(review.id, review.orderId) },
                onDeleteClick = { onDeleteClick(review.id) }
            )
        }

        // Loading indicator cho load more
        item {
            if (reviews.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = Color(0xFFFBBB00)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: MyOrderReviewApiModel,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Shop name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF9E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        tint = Color(0xFFFBBB00),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.shopName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.order_code, review.orderId),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ratings section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Shop rating
                if (review.shopRating > 0) {
                    RatingSection(
                        title = stringResource(R.string.shop),
                        rating = review.shopRating,
                        comment = review.shopComment
                    )
                }

                // Shipper rating
                review.shipperRating?.let { shipperRating ->
                    if (shipperRating > 0) {
                        RatingSection(
                            title = stringResource(R.string.shipper),
                            rating = shipperRating,
                            comment = review.shipperComment ?: ""
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product reviews
            if (review.productReviews.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.product_reviews_title),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                review.productReviews.forEach { productReview ->
                    ProductReviewItem(
                        productName = productReview.productName ?: stringResource(R.string.product),
                        rating = productReview.rating,
                        comment = productReview.comment
                    )
                }
            }

            // Owner reply (nếu có)
            review.ownerReply?.let { reply ->
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBBB00))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.shop_response_title),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF424242)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = reply,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp,
                                color = Color(0xFF616161)
                            )
                        )

                        review.ownerReplyAt?.let { replyAt ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatDate(replyAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E9E9E),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Date và actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(review.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Edit button
                    FilledTonalIconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF1976D2)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    FilledTonalIconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingSection(
    title: String,
    rating: Int,
    comment: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$title:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            StarRating(rating = rating)
            Text(
                text = stringResource(R.string.rating_value, rating),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBB00)
                )
            )
        }

        if (comment.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = Color(0xFF616161)
                )
            )
        }
    }
}

@Composable
fun ProductReviewItem(
    productName: String?,
    rating: Int,
    comment: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = productName ?: stringResource(R.string.product),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    ),
                    modifier = Modifier.weight(1f)
                )
                StarRating(rating = rating, size = 16)
            }

            if (comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp,
                        color = Color(0xFF616161)
                    )
                )
            }
        }
    }
}

@Composable
fun StarRating(rating: Int, maxRating: Int = 5, size: Int = 20) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFBBB00) else Color(0xFFBDBDBD),
                modifier = Modifier.size(size.dp)
            )
        }
    }
}

@Composable
fun EmptyReviewsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF9E6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.RateReview,
                contentDescription = stringResource(R.string.no_reviews_icon_desc),
                tint = Color(0xFFFBBB00),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.no_reviews_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.no_reviews_message),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = Color(0xFFFBBB00)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.loading),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = stringResource(R.string.error_icon_desc),
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFBBB00)
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.retry),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// Function để format date
fun formatDate(dateString: String): String {
    return try {
        // Giả sử format ISO 8601: "2026-01-28T03:37:40.307Z"
        val datePart = dateString.substring(0, 10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}