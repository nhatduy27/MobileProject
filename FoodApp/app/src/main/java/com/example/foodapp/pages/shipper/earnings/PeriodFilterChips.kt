package com.example.foodapp.pages.shipper.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.data.model.shipper.wallet.RevenuePeriod
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun PeriodFilterChips(
    selectedPeriod: RevenuePeriod,
    onPeriodSelected: (RevenuePeriod) -> Unit,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chỉ hiển thị 4 period chính cho UX tốt hơn (bỏ YEAR vì quá dài)
        listOf(
            RevenuePeriod.TODAY,
            RevenuePeriod.WEEK,
            RevenuePeriod.MONTH,
            RevenuePeriod.ALL
        ).forEach { period ->
            val displayText = when (period) {
                RevenuePeriod.TODAY -> stringResource(R.string.shipper_wallet_today)
                RevenuePeriod.WEEK -> stringResource(R.string.shipper_wallet_this_week)
                RevenuePeriod.MONTH -> stringResource(R.string.shipper_wallet_this_month)
                RevenuePeriod.YEAR -> stringResource(R.string.shipper_wallet_this_month) // Not used
                RevenuePeriod.ALL -> stringResource(R.string.shipper_wallet_all_time)
            }
            PeriodChip(
                text = displayText,
                isSelected = period == selectedPeriod,
                onClick = { if (!isLoading) onPeriodSelected(period) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PeriodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(
                color = if (isSelected) ShipperColors.Primary else ShipperColors.Surface,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) ShipperColors.Primary else ShipperColors.BorderLight,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) ShipperColors.Surface else ShipperColors.TextSecondary
        )
    }
}
