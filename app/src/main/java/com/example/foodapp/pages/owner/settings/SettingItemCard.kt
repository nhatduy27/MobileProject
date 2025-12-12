package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingItemCard(
    item: SettingItem,
    onSwitchChanged: ((Boolean) -> Unit)? = null
) {
    var switchState by remember { mutableStateOf(item.isEnabled) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!item.hasSwitch && item.onClick != null) {
                    Modifier.clickable { item.onClick.invoke() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = item.icon,
                fontSize = 24.sp,
                modifier = Modifier.size(40.dp)
            )

            // Title and Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )

                item.subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Switch or Arrow
            if (item.hasSwitch) {
                Switch(
                    checked = switchState,
                    onCheckedChange = {
                        switchState = it
                        onSwitchChanged?.invoke(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF6B35),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
            } else {
                Text(
                    text = "›",
                    fontSize = 24.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}
