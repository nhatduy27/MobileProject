package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    Row(modifier = Modifier.fillMaxWidth()) {

        // Đơn hàng gần đây
        Card(
            modifier = Modifier
                .weight(1f)
                .height(400.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Đơn hàng gần đây", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentOrders) { order ->
                        RecentOrderCard(order)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Món bán chạy
        Card(
            modifier = Modifier
                .weight(1f)
                .height(400.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Món bán chạy", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(topProducts) { product ->
                        TopProductCard(product)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentOrderCard(order: DashboardRecentOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        order.orderId,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        order.customer,
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }
                
                StatusBadge(order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "₫${String.format("%,d", order.amount)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            )
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

    Box(
        modifier = Modifier
            .background(bgColor.copy(alpha = 0.1f), shape = RoundedCornerShape(100))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            status,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TopProductCard(product: DashboardTopProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        "${product.quantity} đơn",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }

                Text(
                    product.revenue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
        }
    }
}

