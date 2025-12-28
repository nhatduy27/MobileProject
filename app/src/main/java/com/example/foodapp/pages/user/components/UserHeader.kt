package com.example.foodapp.pages.user.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.pages.user.home.UserNameState

@Composable
fun UserHeader(state: UserNameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp) // Điều chỉnh padding
            .height(56.dp), // Chiều cao tiêu chuẩn hơn
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Xin chào! 👋",
                color = Color.Gray,
                fontSize = 14.sp // Tăng kích thước chút
            )

            // Xử lý tất cả các trạng thái
            val nameText = when (state) {
                is UserNameState.Success -> state.userName
                UserNameState.Loading -> "Đang tải..."
                is UserNameState.Error -> "Khách" // Hiển thị khi có lỗi
                UserNameState.Empty -> "Khách" // Khi chưa có dữ liệu
                UserNameState.Idle -> "Đang tải..." // Trạng thái ban đầu
            }

            Text(
                text = nameText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp, // Tăng kích thước cho dễ đọc
                color = Color.Black
            )
        }

        Image(
            painter = painterResource(id = R.drawable.logo_2),
            contentDescription = "Logo ứng dụng",
            modifier = Modifier
                .size(48.dp)
                .padding(start = 8.dp) // Chỉ padding bên trái
        )
    }
}