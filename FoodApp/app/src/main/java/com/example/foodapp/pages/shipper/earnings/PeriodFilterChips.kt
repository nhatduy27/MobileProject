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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.EarningsPeriod
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun PeriodFilterChips(
    selectedPeriod: EarningsPeriod,
    onPeriodSelected: (EarningsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EarningsPeriod.entries.forEach { period ->
            PeriodChip(
                text = period.displayName,
                isSelected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
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
