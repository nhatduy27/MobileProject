package com.example.foodapp.pages.shipper.earnings

import androidx.compose.foundation.background
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
import com.example.foodapp.data.model.shipper.EarningsSummary
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun EarningsSummaryCard(summary: EarningsSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ShipperColors.Primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tổng thu nhập tháng này",
                fontSize = 14.sp,
                color = ShipperColors.Surface.copy(alpha = 0.8f)
            )
            Text(
                text = "%,dđ".format(summary.monthEarnings),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ShipperColors.Surface,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EarningsInfoItem(
                    label = "Số đơn",
                    value = summary.completedOrders.toString()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(ShipperColors.Surface.copy(alpha = 0.3f))
                )
                EarningsInfoItem(
                    label = "TB/đơn",
                    value = "%,dđ".format(summary.averagePerOrder)
                )
            }
        }
    }
}

@Composable
fun EarningsInfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ShipperColors.Surface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = ShipperColors.Surface.copy(alpha = 0.8f)
        )
    }
}
