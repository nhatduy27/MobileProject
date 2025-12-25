package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerFilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val tabs = listOf("Tất cả", "VIP", "Thường xuyên", "Mới")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(Color.White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { title ->
            val isSelected = selectedFilter == title
            Box(
                modifier = Modifier
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFFFF6B35) else Color(0xFFCCCCCC),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        color = if (isSelected) Color(0xFFFF6B35) else Color.White,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onFilterSelected(title) }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color(0xFF757575)
                )
            }
        }
    }
}