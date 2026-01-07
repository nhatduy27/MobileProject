package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.DashboardRecentOrder
import com.example.foodapp.data.model.owner.DashboardTopProduct

@Composable
fun DashboardSummaryLists(
    recentOrders: List<DashboardRecentOrder>,
    topProducts: List<DashboardTopProduct>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recent Orders Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Đơn hàng gần đây",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1A1A1A)
                    )
                    TextButton(onClick = { /* TODO: Navigate to orders */ }) {
                        Text(
                            "Xem tất cả",
                            fontSize = 13.sp,
                            color = Color(0xFFFF6B35)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Show only first 3 orders
                recentOrders.take(3).forEach { order ->
                    RecentOrderCard(order)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Top Products Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Món bán chạy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1A1A1A)
                    )
                    TextButton(onClick = { /* TODO: Navigate to foods */ }) {
                        Text(
                            "Xem tất cả",
                            fontSize = 13.sp,
                            color = Color(0xFFFF6B35)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Show only first 3 products
                topProducts.take(3).forEach { product ->
                    TopProductCard(product)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun RecentOrderCard(order: DashboardRecentOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    order.orderId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    order.customer,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${String.format("%,d", order.amount)}đ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
            
            StatusBadge(order.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Đang xử lý" -> Pair(Color(0xFFFFF3E0), Color(0xFFF57C00))
        "Đang giao" -> Pair(Color(0xFFE3F2FD), Color(0xFF1976D2))
        "Hoàn thành" -> Pair(Color(0xFFE8F5E9), Color(0xFF388E3C))
        else -> Pair(Color(0xFFF5F5F5), Color(0xFF757575))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            status,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TopProductCard(product: DashboardTopProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product emoji/icon
                Text(
                    product.name.take(2), // Get emoji
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        product.name.drop(2).trim(), // Get name without emoji
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${product.quantity} đơn",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            Text(
                product.revenue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            )
        }
    }
}
