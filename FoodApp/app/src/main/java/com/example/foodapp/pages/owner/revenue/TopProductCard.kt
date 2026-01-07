package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.TopProduct

@Composable
fun TopProductCard(product: TopProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank medal with background
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = when (product.rank) {
                    "🥇" -> Color(0xFFFFF3E0)
                    "🥈" -> Color(0xFFECEFF1)
                    "🥉" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFF5F5F5)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = product.rank,
                        fontSize = 20.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${product.quantity} phần • ${"%,d".format(product.unitPrice)}đ/phần",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            Text(
                text = product.totalRevenue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            )
        }
    }
}
