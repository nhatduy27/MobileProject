package com.example.foodapp.pages.shipper.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.wallet.LedgerEntry
import com.example.foodapp.data.model.shipper.wallet.LedgerType
import com.example.foodapp.pages.shipper.theme.ShipperColors


/**
 * Card hiển thị mỗi giao dịch trong lịch sử
 */
@Composable
fun LedgerEntryCard(entry: LedgerEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon theo loại giao dịch
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (entry.isIncome()) 
                                ShipperColors.SuccessLight 
                            else 
                                ShipperColors.ErrorLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (entry.type) {
                            LedgerType.ORDER_PAYOUT -> Icons.Outlined.ShoppingBag
                            LedgerType.WITHDRAWAL -> Icons.Outlined.AccountBalanceWallet
                            LedgerType.PAYOUT -> Icons.Outlined.AccountBalanceWallet
                            LedgerType.ADJUSTMENT -> Icons.Outlined.Tune
                        },
                        contentDescription = null,
                        tint = if (entry.isIncome()) ShipperColors.Success else ShipperColors.Error,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.getDisplayDescription(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ShipperColors.TextPrimary,
                        maxLines = 1
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDateTime(entry.createdAt),
                            fontSize = 12.sp,
                            color = ShipperColors.TextSecondary
                        )
                        
                        entry.orderNumber?.let { orderNum ->
                            Text(
                                text = " • #$orderNum",
                                fontSize = 12.sp,
                                color = ShipperColors.TextTertiary
                            )
                        }
                    }
                }
            }
            
            // Số tiền
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.getFormattedAmount(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isIncome()) ShipperColors.Success else ShipperColors.Error
                )
                
                // Số dư sau giao dịch
                Text(
                    text = "Còn lại: ${String.format("%,d", entry.balanceAfter)}đ",
                    fontSize = 11.sp,
                    color = ShipperColors.TextTertiary
                )
            }
        }
    }
}

/**
 * Format datetime from ISO string
 */
private fun formatDateTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoString) ?: return ""
        
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        outputFormat.timeZone = java.util.TimeZone.getDefault()
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}
