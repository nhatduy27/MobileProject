package com.example.foodapp.pages.owner.customer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.owner.theme.OwnerColors

@Composable
fun CustomerFilterTabs(
    selectedFilter: BuyerFilter,
    onFilterSelected: (BuyerFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    val tabs = BuyerFilter.values().toList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OwnerColors.Surface)
            .horizontalScroll(scrollState)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tabs.forEach { filter ->
            val isSelected = selectedFilter == filter

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) OwnerColors.Primary else OwnerColors.Surface,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "bgColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) OwnerColors.Surface else OwnerColors.TextSecondary,
                label = "textColor"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) OwnerColors.Primary else OwnerColors.Divider,
                label = "borderColor"
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = filter.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PreviewFilterTabs() {
    Column {
        CustomerFilterTabs(selectedFilter = BuyerFilter.ALL, onFilterSelected = {})
        Spacer(modifier = Modifier.height(10.dp))
        CustomerFilterTabs(selectedFilter = BuyerFilter.VIP, onFilterSelected = {})
    }
}
