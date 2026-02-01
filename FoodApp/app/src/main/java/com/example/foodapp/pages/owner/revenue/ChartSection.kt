package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@Composable
fun ChartSection() {
    val dayLabels = listOf(
        stringResource(R.string.revenue_day_mon),
        stringResource(R.string.revenue_day_tue),
        stringResource(R.string.revenue_day_wed),
        stringResource(R.string.revenue_day_thu),
        stringResource(R.string.revenue_day_fri),
        stringResource(R.string.revenue_day_sat),
        stringResource(R.string.revenue_day_sun)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = OwnerDimens.CardElevation.dp),
        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(OwnerDimens.CardPadding.dp)
        ) {
            Text(
                text = stringResource(R.string.revenue_chart_7_days),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OwnerColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mock chart with gradient bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(OwnerColors.SurfaceVariant, RoundedCornerShape(OwnerDimens.CardRadius.dp))
                    .padding(OwnerDimens.CardPadding.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val mockData = listOf(1250, 1870, 1560, 2150, 1890, 2380, 2050)
                val maxValue = mockData.maxOrNull() ?: 1

                mockData.forEach { value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val barHeight = (value.toFloat() / maxValue * 120).dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeight.coerceAtLeast(4.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            OwnerColors.Primary,
                                            OwnerColors.ChartSecondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayLabels.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = OwnerColors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}

