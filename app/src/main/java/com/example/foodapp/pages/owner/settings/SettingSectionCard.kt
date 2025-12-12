package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingSectionCard(
    section: SettingSection,
    onSwitchChanged: ((String, Boolean) -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Title
        Text(
            text = section.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF757575),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Section Items
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.items.forEach { item ->
                SettingItemCard(
                    item = item,
                    onSwitchChanged = { enabled ->
                        onSwitchChanged?.invoke(item.title, enabled)
                    }
                )
            }
        }
    }
}
