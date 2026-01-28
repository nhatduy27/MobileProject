package com.example.foodapp.pages.shipper.home

import android.icu.text.NumberFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import java.util.Locale

@Composable
fun ShipperOrderCard(
    order: ShipperOrder,
    onAccept: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                StatusBadge(status = order.status)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
            
            // Shop/Pickup Info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = order.shopName ?: "Cửa hàng",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Hiển thị số lượng món thay vì địa chỉ shop
                    Text(
                        text = "${order.items.size} món • ${order.orderNumber ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer/Delivery Info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50), // Green for delivery
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = order.customerName ?: "Khách hàng",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = order.shippingAddress ?: "Địa chỉ giao hàng",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer: Total and Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tổng tiền",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (order.status == "READY" && order.shippingAt == null) { // Simple check for available
                     Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Nhận đơn")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) { // Map status from backend
        "READY" -> Color(0xFF2196F3) to "Sẵn sàng"
        "SHIPPING" -> Color(0xFFFF9800) to "Đang giao"
        "DELIVERED" -> Color(0xFF4CAF50) to "Hoàn thành"
        "CANCELLED" -> Color(0xFFF44336) to "Đã hủy"
        else -> Color.Gray to status
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
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
