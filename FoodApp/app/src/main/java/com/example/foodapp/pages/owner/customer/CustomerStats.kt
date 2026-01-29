package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.owner.theme.OwnerColors

data class StatCardData(val label: String, val value: String, val color: Color)

@Composable
fun CustomerStats(
    totalCustomers: Int,
    vipCustomers: Int,
    regularCustomers: Int,
    newCustomers: Int
) {
    val scrollState = rememberScrollState()
    val stats = listOf(
        StatCardData("Tổng số", totalCustomers.toString(), OwnerColors.Primary),
        StatCardData("VIP", vipCustomers.toString(), OwnerColors.Warning),
        StatCardData("Thường xuyên", regularCustomers.toString(), OwnerColors.Success),
        StatCardData("Mới", newCustomers.toString(), OwnerColors.Info)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Tăng khoảng cách một chút cho thoáng
    ) {
        stats.forEach { stat ->
            StatCard(stat)
        }
    }
}

@Composable
fun StatCard(stat: StatCardData) {
    Card(
        // Tăng nhẹ kích thước để nội dung dễ thở hơn
        modifier = Modifier.size(width = 145.dp, height = 110.dp),
        // Bo góc lớn (24.dp) tạo cảm giác hiện đại, mềm mại
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            // Nền pastel nhẹ nhàng (tăng alpha lên 0.15f để màu rõ hơn chút trên nền trắng)
            containerColor = stat.color.copy(alpha = 0.15f)
        ),
        // Thiết kế phẳng (Flat), bỏ bóng đổ đậm
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            // Đẩy Label lên đỉnh, Value xuống đáy
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Label: Dùng màu chủ đạo nhưng giảm opacity thay vì màu xám chết
            Text(
                text = stat.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = stat.color.copy(alpha = 0.8f)
            )

            // Value: Đậm và rõ ràng
            Text(
                text = stat.value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = stat.color,
                letterSpacing = (-0.5).sp // Kéo chữ lại xíu cho chặt chẽ
            )
        }
    }
}

// --- PREVIEW SECTION ---

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewCustomerStats() {
    Column {
        CustomerStats(
            totalCustomers = 1250,
            vipCustomers = 85,
            regularCustomers = 400,
            newCustomers = 12
        )
    }
}