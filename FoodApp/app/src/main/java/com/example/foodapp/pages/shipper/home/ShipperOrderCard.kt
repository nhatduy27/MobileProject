package com.example.foodapp.pages.shipper.home

import android.icu.text.NumberFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import java.util.Locale

@Composable
fun ShipperOrderCard(
    order: ShipperOrder,
    onAccept: () -> Unit = {},
    onClick: () -> Unit = {},
    showAcceptButton: Boolean = true
) {
    val mainColor = Color(0xFFFF6B35)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (order.createdAt != null) {
                        Text(
                            text = formatOrderTime(order.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                StatusBadge(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Shop Info
            InfoRow(
                icon = Icons.Default.Store,
                iconTint = mainColor,
                label = order.shopName ?: "Cửa hàng",
                value = "${order.displayItemCount} món"
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Customer & Delivery Info
            InfoRow(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF2196F3),
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
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.shippingAddress ?: "Địa chỉ giao hàng",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    order.deliveryAddress?.let { addr ->
                        if (addr.building != null || addr.room != null) {
                            Text(
                                text = listOfNotNull(
                                    addr.building?.let { "Tòa $it" },
                                    addr.room?.let { "Phòng $it" }
                                ).joinToString(" - "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
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
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ghi chú: $note",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF795548),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer: Payment Info and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Info
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PaymentMethodBadge(paymentMethod = order.paymentMethod)
                        Spacer(modifier = Modifier.width(8.dp))
                        PaymentStatusBadge(paymentStatus = order.paymentStatus)
                    }
                }
                
                // Total Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tổng tiền",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formatCurrency(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = mainColor
                    )
                }
            }
            
            // Accept Button for available orders
            if (showAcceptButton && order.isAvailableForPickup) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NHẬN ĐƠN", fontWeight = FontWeight.Bold)
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text, icon) = when (status) {
        "PENDING" -> Triple(Color(0xFF9E9E9E), "Chờ xác nhận", Icons.Default.Schedule)
        "CONFIRMED" -> Triple(Color(0xFF03A9F4), "Đã xác nhận", Icons.Default.CheckCircle)
        "PREPARING" -> Triple(Color(0xFF9C27B0), "Đang chuẩn bị", Icons.Default.Restaurant)
        "READY" -> Triple(Color(0xFF2196F3), "Sẵn sàng", Icons.Default.Inventory)
        "SHIPPING" -> Triple(Color(0xFFFF9800), "Đang giao", Icons.Default.LocalShipping)
        "DELIVERED" -> Triple(Color(0xFF4CAF50), "Hoàn thành", Icons.Default.DoneAll)
        "CANCELLED" -> Triple(Color(0xFFF44336), "Đã hủy", Icons.Default.Cancel)
        else -> Triple(Color.Gray, status, Icons.Default.Info)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PaymentMethodBadge(paymentMethod: String?) {
    val (text, color) = when(paymentMethod) {
        "COD" -> "COD" to Color(0xFF795548)
        "ZALOPAY" -> "ZaloPay" to Color(0xFF0068FF)
        "MOMO" -> "MoMo" to Color(0xFFAE2070)
        "SEPAY" -> "SePay" to Color(0xFF00BFA5)
        else -> (paymentMethod ?: "N/A") to Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun PaymentStatusBadge(paymentStatus: String?) {
    val (text, color) = when(paymentStatus) {
        "PAID" -> "Đã TT" to Color(0xFF4CAF50)
        "UNPAID" -> "Chưa TT" to Color(0xFFF44336)
        "PROCESSING" -> "Đang xử lý" to Color(0xFFFF9800)
        "REFUNDED" -> "Đã hoàn" to Color(0xFF9C27B0)
        else -> (paymentStatus ?: "") to Color.Gray
    }
    
    if (text.isNotEmpty()) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        // Backend returns ISO-8601 format: 2026-01-18T15:12:20.059Z
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        val date = inputFormat.parse(timestamp.substringBefore(".").substringBefore("Z"))
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        timestamp.take(16).replace("T", " ")
    }
}
