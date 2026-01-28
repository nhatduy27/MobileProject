package com.example.foodapp.pages.shipper.home

import android.icu.text.NumberFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.util.Locale

@Composable
fun ShipperOrderCard(
    order: ShipperOrder,
    onAccept: () -> Unit = {},
    onClick: () -> Unit = {},
    showAcceptButton: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Order Number and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber ?: "#${order.id.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.TextPrimary
                    )
                    if (order.createdAt != null) {
                        Text(
                            text = formatOrderTime(order.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = ShipperColors.TextSecondary
                        )
                    }
                }
                
                StatusBadge(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ShipperColors.Divider)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Shop Info
            InfoRow(
                icon = Icons.Outlined.Store,
                iconTint = ShipperColors.Primary,
                label = order.shopName ?: "Cửa hàng",
                value = "${order.displayItemCount} món"
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Customer & Delivery Info
            InfoRow(
                icon = Icons.Outlined.Person,
                iconTint = ShipperColors.Info,
                label = order.customerName ?: "Khách hàng",
                value = order.customerPhone
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Delivery Address
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = ShipperColors.Success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.shippingAddress ?: "Địa chỉ giao hàng",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = ShipperColors.TextPrimary
                    )
                    order.deliveryAddress?.let { addr ->
                        if (addr.building != null || addr.room != null) {
                            Text(
                                text = listOfNotNull(
                                    addr.building?.let { "Tòa $it" },
                                    addr.room?.let { "Phòng $it" }
                                ).joinToString(" - "),
                                style = MaterialTheme.typography.bodySmall,
                                color = ShipperColors.TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Delivery Note if present
            order.deliveryNote?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ShipperColors.WarningLight, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notes,
                        contentDescription = null,
                        tint = ShipperColors.Warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ghi chú: $note",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShipperColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ShipperColors.Divider)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer: Payment Info and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethodBadge(paymentMethod = order.paymentMethod)
                    PaymentStatusBadge(paymentStatus = order.paymentStatus)
                }
                
                // Total Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tổng tiền",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShipperColors.TextSecondary
                    )
                    Text(
                        text = formatCurrency(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShipperColors.Primary
                    )
                }
            }
            
            // Accept Button for available orders
            if (showAcceptButton && order.isAvailableForPickup) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NHẬN ĐƠN", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ShipperColors.TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = ShipperColors.TextSecondary
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text, icon) = when (status) {
        "PENDING" -> Triple(ShipperColors.StatusPending, "Chờ xác nhận", Icons.Outlined.Schedule)
        "CONFIRMED" -> Triple(ShipperColors.StatusConfirmed, "Đã xác nhận", Icons.Outlined.CheckCircle)
        "PREPARING" -> Triple(ShipperColors.StatusPreparing, "Đang chuẩn bị", Icons.Outlined.Restaurant)
        "READY" -> Triple(ShipperColors.StatusReady, "Sẵn sàng", Icons.Outlined.Inventory)
        "SHIPPING" -> Triple(ShipperColors.StatusShipping, "Đang giao", Icons.Outlined.LocalShipping)
        "DELIVERED" -> Triple(ShipperColors.StatusDelivered, "Hoàn thành", Icons.Outlined.DoneAll)
        "CANCELLED" -> Triple(ShipperColors.StatusCancelled, "Đã hủy", Icons.Outlined.Cancel)
        else -> Triple(ShipperColors.TextSecondary, status, Icons.Outlined.Info)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PaymentMethodBadge(paymentMethod: String?) {
    val (text, color) = when(paymentMethod) {
        "COD" -> "COD" to Color(0xFF6B7280)
        "ZALOPAY" -> "ZaloPay" to Color(0xFF0068FF)
        "MOMO" -> "MoMo" to Color(0xFFAE2070)
        "SEPAY" -> "SePay" to Color(0xFF00BFA5)
        else -> (paymentMethod ?: "N/A") to Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PaymentStatusBadge(paymentStatus: String?) {
    val (text, color) = when(paymentStatus) {
        "PAID" -> "Đã TT" to ShipperColors.Success
        "UNPAID" -> "Chưa TT" to ShipperColors.Error
        "PROCESSING" -> "Đang xử lý" to ShipperColors.Warning
        "REFUNDED" -> "Đã hoàn" to Color(0xFF8B5CF6)
        else -> (paymentStatus ?: "") to Color.Gray
    }
    
    if (text.isNotEmpty()) {
        Surface(
            color = color.copy(alpha = 0.08f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    return try {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        formatter.format(amount)
    } catch (e: Exception) {
        "${amount.toLong()}đ"
    }
}

fun formatOrderTime(timestamp: String?): String {
    if (timestamp == null) return ""
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        val date = inputFormat.parse(timestamp.substringBefore(".").substringBefore("Z"))
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        timestamp.take(16).replace("T", " ")
    }
}
