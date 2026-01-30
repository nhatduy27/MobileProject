package com.example.foodapp.pages.client.components.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.foodapp.R
import com.example.foodapp.data.model.shared.product.Product

// Import cho AsyncImage
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.Icon as M3Icon

@Composable
fun UserProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(
                enabled = product.isAvailable,
                onClick = onClick
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Phần hình ảnh sản phẩm
            ProductImage(product = product)

            // Phần thông tin sản phẩm
            ProductInfo(product = product)
        }
    }
}

@Composable
fun ProductImage(product: Product) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ưu tiên hiển thị ảnh từ URL (API)
        if (!product.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // Nếu không có URL, kiểm tra local resource
        else if (product.imageRes != null && product.imageRes != 0) {
            // Nếu bạn thực sự có local resource
            // Image(painter = painterResource(id = product.imageRes), ...)
            // Nhưng nếu không có, fallback xuống dưới
            NoImagePlaceholder(productName = product.name)
        }
        // Nếu không có ảnh nào
        else {
            NoImagePlaceholder(productName = product.name)
        }

        // Badge cho sản phẩm đã bán hết
        if (!product.isAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.out_of_stock),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun NoImagePlaceholder(productName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dùng Icon từ Material Icons thay vì drawable
            M3Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = stringResource(R.string.no_image),
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = productName.take(10), // Lấy 10 ký tự đầu
                color = Color(0xFF616161),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProductInfo(product: Product) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        // Tên sản phẩm
        Text(
            text = product.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (!product.isAvailable) Color.Gray else Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Mô tả (nếu có)
        if (!product.description.isNullOrBlank()) {
            Text(
                text = product.description,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Giá
        Text(
            text = product.displayPrice,
            color = if (!product.isAvailable) Color.Gray else Color(0xFFFF9800),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Rating và số lượng bán
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rating
            if (product.rating > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dùng Icon từ Material Icons
                    M3Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.rating),
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.ratingText,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                    if (product.totalRatings > 0) {
                        Text(
                            text = stringResource(R.string.rating_count_parentheses, product.totalRatings),
                            fontSize = 12.sp,
                            color = Color(0xFFBDBDBD)
                        )
                    }
                }
            } else {
                // Hiển thị "Chưa có đánh giá" nếu rating = 0
                Text(
                    text = stringResource(R.string.no_ratings_yet),
                    fontSize = 12.sp,
                    color = Color(0xFFBDBDBD)
                )
            }

            // Số lượng bán
            if (product.soldCount > 0) {
                Text(
                    text = stringResource(R.string.sold_count, product.soldCountText),
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        // Thời gian chuẩn bị và shop name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thời gian chuẩn bị
            if (product.preparationTime > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    M3Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = stringResource(R.string.preparation_time),
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.preparation_time_minutes, product.preparationTime),
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            // Tên shop
            if (product.shopName.isNotBlank()) {
                Text(
                    text = product.shopName.take(15),
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}