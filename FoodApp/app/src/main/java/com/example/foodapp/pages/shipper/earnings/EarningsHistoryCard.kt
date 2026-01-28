package com.example.foodapp.pages.shipper.earnings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.EarningsData
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun EarningsHistoryCard(earnings: EarningsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = earnings.date,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ShipperColors.TextPrimary
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${earnings.totalOrders} đơn",
                        fontSize = 12.sp,
                        color = ShipperColors.TextSecondary
                    )
                    if (earnings.bonusEarnings > 0) {
                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = ShipperColors.TextSecondary
                        )
                        Text(
                            text = "+${"%,d".format(earnings.bonusEarnings)}đ thưởng",
                            fontSize = 12.sp,
                            color = ShipperColors.Success
                        )
                    }
                }
            }

            Text(
                text = "%,dđ".format(earnings.totalEarnings),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ShipperColors.Primary
            )
        }
    }
}
