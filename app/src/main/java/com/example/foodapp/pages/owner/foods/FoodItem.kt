package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.foodapp.data.model.owner.Food

@Composable
fun FoodItem(
    food: Food,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // Tạo khoảng cách giữa các item
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // Style hiện đại: Bóng đổ rất nhẹ hoặc bằng 0, dùng border nhạt để tách biệt
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp), // Bo góc mềm mại hơn (16dp)
        onClick = onClick
        // Thêm viền mỏng tinh tế nếu muốn style phẳng hoàn toàn (Optional)
        // border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Padding tổng thể thoáng hơn
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. ẢNH MÓN ĂN (Bo góc đồng bộ) ---
            AsyncImage(
                model = food.imageUrl,
                contentDescription = food.name,
                contentScale = ContentScale.Crop, // Cắt ảnh cho vừa khung
                modifier = Modifier
                    .size(90.dp) // Kích thước ảnh lớn hơn xíu cho đẹp
                    .clip(RoundedCornerShape(12.dp)) // Bo góc ảnh
                    .background(Color(0xFFF5F5F5)) // Nền chờ load ảnh
            )

            // --- 2. NỘI DUNG ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp) // Khoảng cách đều giữa các dòng
            ) {
                // Tên món + Trạng thái
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = food.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, // Dùng SemiBold thay vì Bold quá đậm
                        color = Color(0xFF212121),
                        maxLines = 2, // Giới hạn 2 dòng nếu tên dài
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        lineHeight = 20.sp
                    )
                }

                // Info: Category & Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category
                    Text(
                        text = food.category,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Rating (Icon + Số)
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107), // Màu vàng sao
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${food.rating} (${food.reviewCount})",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Price & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${"%,d".format(food.price)}đ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35) // Màu cam chủ đạo
                    )

                    // Status Badge (Style Pastel)
                    val statusColor = if (food.isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val statusText = if (food.isAvailable) "Còn hàng" else "Hết hàng"

                    Box(
                        modifier = Modifier
                            .background(
                                color = statusColor.copy(alpha = 0.1f), // Nền nhạt (10% opacity)
                                shape = RoundedCornerShape(100) // Bo tròn hoàn toàn (Pill shape)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor // Chữ đậm cùng tông
                        )
                    }
                }
            }
        }
    }
}