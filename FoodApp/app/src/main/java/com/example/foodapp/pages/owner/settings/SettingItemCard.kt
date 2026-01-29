package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
    
    // Opacity cho item bị disabled (chưa phát triển)
    val contentAlpha = if (item.isDisabled) 0.4f else 1f

    Surface(
        onClick = { if (!item.hasSwitch && item.onClick != null && !item.isDisabled) item.onClick.invoke() },
        enabled = (!item.hasSwitch && item.onClick != null && !item.isDisabled),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f * contentAlpha),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title and Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )

                item.subtitle?.let {
                    Text(
                        text = if (item.isDisabled) "$it (Sắp ra mắt)" else it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
            }

            // Switch or Arrow
            if (item.hasSwitch) {
                Switch(
                    checked = switchState,
                    onCheckedChange = if (item.isDisabled) null else { newValue ->
                        switchState = newValue
                        onSwitchChanged?.invoke(newValue)
                    },
                    enabled = !item.isDisabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            } else {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f * contentAlpha)
                )
            }
        }
    }
}

