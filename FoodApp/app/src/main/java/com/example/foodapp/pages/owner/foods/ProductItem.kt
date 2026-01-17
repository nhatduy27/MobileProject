package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.product.Product
import java.text.NumberFormat
import java.util.Locale

/**
 * Product Item Card - Hiển thị thông tin sản phẩm
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image
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
                        tint = Color(0xFFBDBDBD)
                    )
                }

                // Availability badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = if (product.isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (product.isAvailable) "Còn" else "Hết",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
                        color = Color(0xFF1A1A1A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Category
                    Text(
                        text = product.categoryName,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description
                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom row: Price + Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Text(
                        text = "${priceFormatter.format(product.price.toLong())}đ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35)
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
                                    color = Color(0xFF757575)
                                )
                            }
                        }

                        // Sold count
                        if (product.soldCount > 0) {
                            Text(
                                text = "Đã bán: ${product.soldCount}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        // Prep time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF757575)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${product.preparationTime}p",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
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
                // Toggle availability
                IconButton(
                    onClick = onToggleAvailability,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (product.isAvailable)
                                Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (product.isAvailable)
                            Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = "Toggle availability",
                        tint = if (product.isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa sản phẩm") },
            text = { Text("Bạn có chắc muốn xóa \"${product.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
                ) {
                    Text("Xóa")
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
