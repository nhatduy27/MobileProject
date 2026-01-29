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
import com.example.foodapp.data.model.owner.revenue.TopProductData
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@Composable
fun TopProductCard(product: TopProductData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = OwnerDimens.CardElevation.dp),
        shape = RoundedCornerShape(OwnerDimens.CardRadius.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OwnerDimens.CardPadding.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank medal with background
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(OwnerDimens.ButtonRadius.dp),
                color = when (product.rank) {
                    "🥇" -> Color(0xFFFFF3E0)
                    "🥈" -> Color(0xFFECEFF1)
                    "🥉" -> Color(0xFFFFEBEE)
                    else -> OwnerColors.SurfaceVariant
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
                    fontWeight = FontWeight.SemiBold,
                    color = OwnerColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${product.quantity} phần • ${product.unitPriceFormatted}đ/phần",
                    fontSize = 12.sp,
                    color = OwnerColors.TextSecondary
                )
            }

            Text(
                text = product.totalRevenue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OwnerColors.Primary
            )
        }
    }
}
