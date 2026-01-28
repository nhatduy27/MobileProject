package com.example.foodapp.pages.shipper.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.ProfileMenuItem
import com.example.foodapp.data.model.shipper.ProfileAction
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun ProfileMenuCard(
    title: String,
    items: List<ProfileMenuItem>,
    onItemClick: (ProfileAction) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.TextSecondary,
                modifier = Modifier.padding(16.dp).padding(bottom = 0.dp)
            )

            items.forEachIndexed { index, item ->
                ProfileMenuItemRow(
                    item = item,
                    onClick = { onItemClick(item.action) }
                )
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = ShipperColors.Divider
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (item.icon) {
        "person" -> Icons.Outlined.Person
        "lock" -> Icons.Outlined.Lock
        "bike" -> Icons.Outlined.DirectionsBike
        "wallet" -> Icons.Outlined.AccountBalanceWallet
        "notifications" -> Icons.Outlined.Notifications
        "terms" -> Icons.Outlined.Description
        "privacy" -> Icons.Outlined.PrivacyTip
        "help" -> Icons.Outlined.HelpOutline
        "language" -> Icons.Outlined.Language
        else -> Icons.Outlined.Settings
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ShipperColors.PrimaryLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShipperColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ShipperColors.TextPrimary
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = ShipperColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
