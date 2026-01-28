package com.example.foodapp.pages.shipper.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) ShipperColors.Surface else ShipperColors.PrimaryLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        notification.type.color.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.NEW_ORDER -> Icons.Outlined.ShoppingBag
                        NotificationType.ORDER_UPDATE -> Icons.Outlined.LocalShipping
                        NotificationType.PAYMENT -> Icons.Outlined.AccountBalanceWallet
                        NotificationType.PROMOTION -> Icons.Outlined.LocalOffer
                        NotificationType.SYSTEM -> Icons.Outlined.Info
                    },
                    contentDescription = null,
                    tint = notification.type.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ShipperColors.Primary, CircleShape)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = ShipperColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 20.sp
                )

                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = ShipperColors.TextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
