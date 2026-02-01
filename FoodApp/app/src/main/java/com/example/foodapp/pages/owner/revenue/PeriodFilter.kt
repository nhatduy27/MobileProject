package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.data.model.owner.revenue.RevenuePeriod
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@Composable
fun getPeriodDisplayName(period: RevenuePeriod): String {
    return when (period) {
        RevenuePeriod.TODAY -> stringResource(R.string.revenue_period_today)
        RevenuePeriod.WEEK -> stringResource(R.string.revenue_period_week)
        RevenuePeriod.MONTH -> stringResource(R.string.revenue_period_month)
        RevenuePeriod.YEAR -> stringResource(R.string.revenue_period_year)
    }
}

@Composable
fun PeriodFilter(
    selectedPeriod: RevenuePeriod,
    onPeriodSelected: (RevenuePeriod) -> Unit
) {
    val periods = RevenuePeriod.values().toList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OwnerColors.Surface)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periods.forEach { period ->
            PeriodChip(
                text = getPeriodDisplayName(period),
                isSelected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) }
            )
        }
    }
}


@Composable
fun PeriodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) OwnerColors.Primary else OwnerColors.Surface,
        shape = RoundedCornerShape(OwnerDimens.FilterChipRadius.dp),
        modifier = Modifier
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) OwnerColors.Primary else OwnerColors.BorderLight,
                shape = RoundedCornerShape(OwnerDimens.FilterChipRadius.dp)
            )
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) OwnerColors.Surface else OwnerColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}
