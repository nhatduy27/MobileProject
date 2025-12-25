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

@Composable
fun DashboardSummaryLists() {
    val recentOrders = listOf(
        RecentOrder("#ORD001", "Khách Hàng A", "Đang xử lý", 245000),
        RecentOrder("#ORD002", "Khách Hàng B", "Đang giao", 189000),
        RecentOrder("#ORD003", "Khách Hàng C", "Hoàn thành", 312000),
        RecentOrder("#ORD004", "Khách Hàng D", "Đang xử lý", 156000),
        RecentOrder("#ORD005", "Khách Hàng E", "Hoàn thành", 428000)
    )

    val topProducts = listOf(
        TopProductItem("🍚 Cơm gà", 156, "1.44M"),
        TopProductItem("🍜 Phở bò", 128, "1.28M"),
        TopProductItem("🥤 Trà sữa", 195, "975K"),
        TopProductItem("🍝 Mì", 87, "522K"),
        TopProductItem("🍗 Gà rán", 92, "644K")
    )

    Row(modifier = Modifier.fillMaxWidth()) {

        // Đơn hàng gần đây
        Card(
            modifier = Modifier
                .weight(1f)
                .height(400.dp),
            elevation = CardDefaults.cardElevation(4.dp)
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
            elevation = CardDefaults.cardElevation(4.dp)
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
fun RecentOrderCard(order: RecentOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F7))
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
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            status,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TopProductCard(product: TopProductItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F7))
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

data class RecentOrder(
    val orderId: String,
    val customer: String,
    val status: String,
    val amount: Int
)

data class TopProductItem(
    val name: String,
    val quantity: Int,
    val revenue: String
)
