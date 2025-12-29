package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerHeader(
    onAddClick: () -> Unit = {}, // Callback khi bấm nút thêm
    onSearchClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Nền trắng hiện đại
            .padding(horizontal = 24.dp, vertical = 20.dp), // Padding rộng rãi
        verticalAlignment = Alignment.CenterVertically, // Căn giữa theo chiều dọc
        horizontalArrangement = Arrangement.SpaceBetween // Đẩy nội dung sang 2 bên
    ) {
        // Phần Text bên trái
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Quản lý khách hàng",
                fontSize = 26.sp, // Giảm nhẹ size để tinh tế hơn
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A), // Màu đen xám (Soft Black) thay vì đen tuyền
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Danh sách và lịch sử mua hàng",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E), // Màu xám nhạt cho subtext
                fontWeight = FontWeight.Medium
            )
        }

        // Phần Action Buttons bên phải (Thường app quản lý sẽ có nút này)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Search nhỏ (Icon Only)
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Nút "Thêm mới" nổi bật (Filled Tonal hoặc Primary)
            Surface(
                onClick = onAddClick,
                shape = CircleShape,
                color = Color(0xFFFF6B35).copy(alpha = 0.1f), // Nền cam nhạt
                contentColor = Color(0xFFFF6B35), // Icon màu cam đậm
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Customer",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PreviewCustomerHeader() {
    Column {
        CustomerHeader()
        // Giả lập đường kẻ mờ ngăn cách header và content
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF0F0F0))
        )
    }
}