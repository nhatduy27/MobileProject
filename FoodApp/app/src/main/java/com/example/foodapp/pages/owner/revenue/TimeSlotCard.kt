package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.revenue.TimeSlotData
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@Composable
fun TimeSlotCard(timeSlot: TimeSlotData) {
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
            // Emoji icon with background
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(OwnerDimens.CardRadius.dp),
                color = OwnerColors.PrimaryLight
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeSlot.emoji,
                        fontSize = 24.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = timeSlot.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OwnerColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${timeSlot.ordersCount} đơn hàng • ${timeSlot.percentage}% tổng doanh thu",
                    fontSize = 12.sp,
                    color = OwnerColors.TextSecondary
                )
            }

            Text(
                text = timeSlot.amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OwnerColors.Primary
            )
        }
    }
}
