package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        // Kích thước chuẩn theo style mới (rộng hơn chút, cao hơn chút)
        modifier = Modifier.size(width = 145.dp, height = 110.dp),
        // Bo góc lớn mềm mại
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            // Nền pastel (lấy màu chính giảm độ đậm còn 15%)
            containerColor = color.copy(alpha = 0.15f)
        ),
        // Bỏ bóng đổ (Flat design)
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            // Đẩy Title lên trên, Value xuống dưới
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Title: Màu chủ đạo nhưng nhạt hơn chút
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.8f)
            )

            // Value: Đậm, rõ ràng
            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = (-0.5).sp // Kéo chữ lại gần xíu cho chặt chẽ
            )
        }
    }
}