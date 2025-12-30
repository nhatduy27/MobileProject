package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.data.model.owner.Customer

@Composable
fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp), // Tạo khoảng cách giữa các card
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp), // Bo góc card lớn hơn (16dp) cho mềm mại
        colors = CardDefaults.cardColors(containerColor = Color.White), // Nền trắng sạch sẽ
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp) // Padding bên trong card
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. AVATAR ---
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(customer.avatar)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp) // Tăng kích thước avatar một chút
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)) // Màu nền xám nhẹ khi load ảnh
            )

            Spacer(modifier = Modifier.width(16.dp))

            // --- 2. INFO SECTION ---
            Column(modifier = Modifier.weight(1f)) {
                // Tên + Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false) // Để tên không đè lên badge
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Gọi hàm vẽ Badge trạng thái
                    StatusBadge(type = customer.type)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Số điện thoại / Địa chỉ (Có Icon)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF95A5A6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.contact,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF636E72),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Số đơn hàng (Có Icon)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFF95A5A6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.ordersInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF636E72)
                    )
                }
            }

            // --- 3. REVENUE (DOANH THU) ---
            // Đặt doanh thu sang bên phải để chủ quán dễ nhìn thấy nhất
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = customer.revenueInfo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6B35), // Màu cam nổi bật cho tiền
                    fontSize = 17.sp
                )
                Text(
                    text = "Tổng chi",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB2BEC3),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// --- COMPONENT CON: BADGE TRẠNG THÁI ---
@Composable
fun StatusBadge(type: String) {
    // Định nghĩa màu sắc theo phong cách "Chip" (Nền nhạt, Chữ đậm)
    val (backgroundColor, contentColor) = when (type) {
        "VIP" -> Pair(Color(0xFFFFF4E5), Color(0xFFFF9800)) // Cam nhạt / Cam đậm
        "Thường xuyên" -> Pair(Color(0xFFE8F5E9), Color(0xFF4CAF50)) // Xanh lá nhạt / Xanh lá đậm
        "Mới" -> Pair(Color(0xFFE3F2FD), Color(0xFF2196F3)) // Xanh dương nhạt / Xanh dương đậm
        else -> Pair(Color(0xFFF5F6FA), Color(0xFF95A5A6)) // Xám
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50), // Bo tròn hoàn toàn (dạng viên thuốc)
    ) {
        Text(
            text = type,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun PreviewModernCardVIP() {
    Box(modifier = Modifier.padding(16.dp)) {
        CustomerCard(
            customer = Customer(
                id = "1",
                name = "Nguyễn Phúc Hậu",
                type = "VIP",
                contact = "0909 123 456",
                ordersInfo = "67 đơn hàng",
                revenueInfo = "3.2M",
                avatar = "https://picsum.photos/id/1005/200"
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun PreviewModernCardNew() {
    Box(modifier = Modifier.padding(16.dp)) {
        CustomerCard(
            customer = Customer(
                id = "2",
                name = "Trần Thị Bích",
                type = "Mới",
                contact = "0123 456 789",
                ordersInfo = "1 đơn hàng",
                revenueInfo = "250K",
                avatar = "https://picsum.photos/id/1027/200"
            )
        )
    }
}