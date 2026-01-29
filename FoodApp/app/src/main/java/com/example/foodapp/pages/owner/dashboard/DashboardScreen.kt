package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.owner.*
import com.example.foodapp.pages.owner.notifications.NotificationBell
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OwnerColors.Background)
    ) {
        // Header
        DashboardHeaderNew(onMenuClick)

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: ${uiState.error}", color = OwnerColors.Error, modifier = Modifier.padding(16.dp))
            }
        } else {
            val data = uiState.data
            if (data != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Overview Cards (Grid 2x2) - Hiển thị dữ liệu tháng này
                    Text("Tổng quan tháng này", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = OwnerColors.TextPrimary)
                    
                    // Tính tổng đơn từ ordersByStatus
                    val totalOrders = data.ordersByStatus.values.sum()
                    val pendingOrders = (data.ordersByStatus["PENDING"] ?: 0) + 
                                       (data.ordersByStatus["CONFIRMED"] ?: 0) + 
                                       (data.ordersByStatus["PREPARING"] ?: 0)
                    val completedOrders = data.ordersByStatus["COMPLETED"] ?: 0
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModernStatCard(
                                title = "Doanh thu tháng",
                                value = formatCurrency(data.thisMonth.revenue),
                                icon = Icons.Default.AttachMoney,
                                color = OwnerColors.Success,
                                modifier = Modifier.weight(1f)
                            )
                            ModernStatCard(
                                title = "Tổng đơn hàng",
                                value = "$totalOrders",
                                icon = Icons.Default.Receipt,
                                color = OwnerColors.Info,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModernStatCard(
                                title = "Đang xử lý",
                                value = "$pendingOrders",
                                icon = Icons.Default.HourglassEmpty,
                                color = OwnerColors.Warning,
                                modifier = Modifier.weight(1f)
                            )
                            ModernStatCard(
                                title = "Hoàn thành",
                                value = "$completedOrders",
                                icon = Icons.Default.CheckCircle,
                                color = OwnerColors.StatusDelivered,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // 2. Orders Status Distribution (Chart)
                    Text("Phân bố trạng thái đơn", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = OwnerColors.TextPrimary)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
                        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp),
                        elevation = CardDefaults.cardElevation(OwnerDimens.CardElevation.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            DonutChart(
                                data = data.ordersByStatus,
                                modifier = Modifier.size(120.dp),
                                thickness = 20.dp
                            )
                            
                            // Legend
                            Column {
                                LegendItem("Chờ xác nhận", ColorPending, data.ordersByStatus["PENDING"] ?: 0)
                                LegendItem("Đang nấu", ColorPreparing, data.ordersByStatus["PREPARING"] ?: 0)
                                LegendItem("Đang giao", ColorDelivering, data.ordersByStatus["DELIVERING"] ?: 0)
                                LegendItem("Hoàn thành", ColorCompleted, data.ordersByStatus["COMPLETED"] ?: 0)
                                LegendItem("Đã hủy", ColorCancelled, data.ordersByStatus["CANCELLED"] ?: 0)
                            }
                        }
                    }

                    // 3. Top Products (With Bar)
                    Text("Top món bán chạy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = OwnerColors.TextPrimary)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
                        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp),
                        elevation = CardDefaults.cardElevation(OwnerDimens.CardElevation.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            if (data.topProducts.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Chưa có dữ liệu", style = MaterialTheme.typography.bodyMedium, color = OwnerColors.TextSecondary)
                                }
                            } else {
                                val maxRevenue = data.topProducts.maxOfOrNull { it.revenue } ?: 1.0
                                
                                data.topProducts.take(5).forEachIndexed { index, product ->
                                    val progress = (product.revenue / maxRevenue).toFloat()
                                    
                                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(formatCurrency(product.revenue), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OwnerColors.Success)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        HorizontalBar(value = progress, color = OwnerColors.Primary)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${product.soldCount} đã bán", style = MaterialTheme.typography.labelSmall, color = OwnerColors.TextSecondary)
                                    }
                                    if (index < data.topProducts.size - 1 && index < 4) {
                                        Divider(color = OwnerColors.BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 4. Recent Orders
                    Text("Đơn hàng gần đây", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = OwnerColors.TextPrimary)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
                        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp),
                        elevation = CardDefaults.cardElevation(OwnerDimens.CardElevation.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                             if (data.recentOrders.isEmpty()) {
                                 Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Chưa có đơn hàng", style = MaterialTheme.typography.bodyMedium, color = OwnerColors.TextSecondary)
                                }
                            } else {
                                data.recentOrders.forEachIndexed { index, order ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(40.dp).background(OwnerColors.BorderLight, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = OwnerColors.TextSecondary, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(order.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text(formatDate(order.createdAt), fontSize = 12.sp, color = OwnerColors.TextSecondary)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(formatCurrency(order.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.height(4.dp))
                                            StatusBadge(order.status)
                                        }
                                    }
                                    if (index < data.recentOrders.lastIndex) {
                                        Divider(color = OwnerColors.BorderLight)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có dữ liệu", color = OwnerColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
fun DashboardHeaderNew(onMenuClick: () -> Unit) {
    Surface(
        color = OwnerColors.Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = OwnerColors.TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Tổng quan", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // Notification Bell Icon
            NotificationBell()
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = OwnerColors.TextSecondary)
        Spacer(Modifier.width(4.dp))
        Text(text = "($count)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (text, color) = when(status) {
        "PENDING" -> "Chờ xác nhận" to ColorPending
        "CONFIRMED" -> "Đã xác nhận" to ColorPreparing
        "PREPARING" -> "Đang nấu" to ColorPreparing
        "READY" -> "Đã xong" to ColorReady
        "DELIVERING" -> "Đang giao" to ColorDelivering
        "COMPLETED" -> "Hoàn thành" to ColorCompleted
        "CANCELLED" -> "Đã hủy" to ColorCancelled
        else -> status to Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Helper functions
fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
}

fun formatDate(isoString: String): String {
    return try {
        // Example: 2026-01-11T10:00:00.000Z
        if (isoString.length >= 16) {
             val datePart = isoString.substring(0, 10) // 2026-01-11
             val timePart = isoString.substring(11, 16) // 10:00
             "$timePart $datePart"
        } else {
             isoString
        }
    } catch (e: Exception) {
        isoString
    }
}
