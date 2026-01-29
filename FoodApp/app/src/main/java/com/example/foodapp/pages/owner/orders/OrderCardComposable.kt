package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.order.ShopOrder
import com.example.foodapp.data.model.owner.order.ShopOrderStatus
import com.example.foodapp.data.model.owner.order.PaymentStatus
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@Composable
fun OrderFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) OwnerColors.Primary else OwnerColors.Surface,
            contentColor = if (isSelected) OwnerColors.Surface else OwnerColors.Primary
        ),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text = text, fontSize = 12.sp)
    }
}

@Composable
fun OrderStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.size(width = 110.dp, height = 90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.8f),
                maxLines = 1
            )

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

/**
 * Get status color (synchronized with Shipper theme)
 */
fun getStatusColor(status: ShopOrderStatus): Color {
    return when (status) {
        ShopOrderStatus.PENDING -> OwnerColors.StatusPending
        ShopOrderStatus.CONFIRMED -> OwnerColors.StatusConfirmed
        ShopOrderStatus.PREPARING -> OwnerColors.StatusPreparing
        ShopOrderStatus.READY -> OwnerColors.StatusReady
        ShopOrderStatus.SHIPPING -> OwnerColors.StatusShipping
        ShopOrderStatus.DELIVERED -> OwnerColors.StatusDelivered
        ShopOrderStatus.CANCELLED -> OwnerColors.StatusCancelled
    }
}

/**
 * Get payment status color (synchronized with Shipper theme)
 */
fun getPaymentStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.UNPAID -> OwnerColors.PaymentUnpaid
        PaymentStatus.PROCESSING -> OwnerColors.PaymentProcessing
        PaymentStatus.PAID -> OwnerColors.PaymentPaid
        PaymentStatus.REFUNDED -> OwnerColors.PaymentRefunded
    }
}

@Composable
fun OrderCard(
    order: ShopOrder,
    onClick: () -> Unit = {},
    onActionClick: ((String) -> Unit)? = null
) {
    val statusColor = getStatusColor(order.status)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = OwnerDimens.CardElevation.dp),
        shape = RoundedCornerShape(OwnerDimens.CardRadius.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(OwnerDimens.CardPadding.dp)) {
            // Header: Order number + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OwnerColors.TextPrimary
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = order.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer info
            order.customer?.let { customer ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = OwnerColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customer.displayName ?: "Khách hàng",
                        fontSize = 14.sp,
                        color = OwnerColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    customer.phone?.let { phone ->
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = OwnerColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = phone,
                            fontSize = 13.sp,
                            color = OwnerColors.TextSecondary
                        )
                    }
                }
            }

            // Delivery address
            order.deliveryAddress?.let { address ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OwnerColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = address.getDisplayAddress(),
                        fontSize = 13.sp,
                        color = OwnerColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items preview
            Text(
                text = order.getItemsDisplayText(),
                fontSize = 13.sp,
                color = OwnerColors.TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = OwnerColors.BorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Time, Payment status, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = OwnerColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.getDisplayTime(),
                        fontSize = 12.sp,
                        color = OwnerColors.TextTertiary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Payment status badge
                    val paymentColor = getPaymentStatusColor(order.paymentStatus)
                    Surface(
                        color = paymentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = order.paymentStatus.displayName,
                            fontSize = 10.sp,
                            color = paymentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "%,d₫".format(order.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OwnerColors.Primary
                )
            }

            // Quick action button for pending orders
            if (onActionClick != null && order.status == ShopOrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onActionClick(order.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OwnerColors.Success
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = "Xác nhận đơn",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
