package com.example.foodapp.pages.owner.foods

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.product.Product
import com.example.foodapp.pages.owner.theme.OwnerColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Product Item Card - Hiển thị thông tin sản phẩm
 * 
 * Với sản phẩm không hoạt động (isAvailable = false):
 * - Hiệu ứng mờ (alpha 0.6)
 * - Nhãn "Đã ẩn" overlay
 * - Giá bị gạch ngang
 */
@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    onToggleAvailability: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val priceFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    // Animation cho hiệu ứng mờ khi không hoạt động
    val cardAlpha by animateFloatAsState(
        targetValue = if (product.isAvailable) 1f else 0.7f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (product.isAvailable) 1f else 0.98f,
        animationSpec = tween(300),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .alpha(cardAlpha)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isAvailable) OwnerColors.Surface else OwnerColors.SurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (product.isAvailable) 2.dp else 1.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image với Status Badge
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center),
                        tint = OwnerColors.BorderLight
                    )
                }

                // Modern Status Badge - Gradient style
                StatusBadge(
                    isAvailable = product.isAvailable,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )

                // Overlay "Đã ẩn" cho sản phẩm không hoạt động
                if (!product.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐÃ ẨN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    OwnerColors.Error,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Product Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Name
                    Text(
                        text = product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isAvailable) OwnerColors.TextPrimary else OwnerColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Category với icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Label,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = OwnerColors.Primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.categoryName,
                            fontSize = 12.sp,
                            color = OwnerColors.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description
                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        color = OwnerColors.TextTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom row: Price + Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price - gạch ngang nếu không hoạt động
                    Text(
                        text = "${priceFormatter.format(product.price.toLong())}đ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isAvailable) OwnerColors.Primary else OwnerColors.BorderLight,
                        textDecoration = if (product.isAvailable) TextDecoration.None else TextDecoration.LineThrough
                    )

                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rating
                        if (product.totalRatings > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFC107)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f", product.rating),
                                    fontSize = 12.sp,
                                    color = OwnerColors.TextSecondary
                                )
                            }
                        }

                        // Sold count
                        if (product.soldCount > 0) {
                            Text(
                                text = "Đã bán: ${product.soldCount}",
                                fontSize = 12.sp,
                                color = OwnerColors.TextSecondary
                            )
                        }

                        // Prep time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = OwnerColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${product.preparationTime}p",
                                fontSize = 12.sp,
                                color = OwnerColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Toggle availability (Ẩn/Hiện)
                IconButton(
                    onClick = onToggleAvailability,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (product.isAvailable)
                                OwnerColors.SuccessLight else OwnerColors.WarningLight,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (product.isAvailable)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (product.isAvailable) "Ẩn sản phẩm" else "Hiện sản phẩm",
                        tint = if (product.isAvailable) OwnerColors.Success else OwnerColors.Warning,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button - Chỉ hiện khi sản phẩm đã bị ẩn (xóa mềm trước, xóa cứng sau)
                if (!product.isAvailable) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = OwnerColors.ErrorLight,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "Xóa vĩnh viễn",
                            tint = OwnerColors.Error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = OwnerColors.Error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    "Xóa vĩnh viễn?",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Column {
                    Text("Bạn có chắc muốn xóa vĩnh viễn \"${product.name}\"?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ Hành động này không thể hoàn tác!",
                        color = OwnerColors.Error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OwnerColors.Error
                    )
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

/**
 * Modern Status Badge Component
 * Sử dụng gradient để tạo hiệu ứng hiện đại
 * Kích thước nhỏ gọn phù hợp với ảnh sản phẩm
 */
@Composable
fun StatusBadge(
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = if (isAvailable) {
                    Brush.horizontalGradient(
                        colors = listOf(OwnerColors.Success, Color(0xFF66BB6A))
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(OwnerColors.Error, Color(0xFFEF5350))
                    )
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isAvailable) "Còn hàng" else "Hết hàng",
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
