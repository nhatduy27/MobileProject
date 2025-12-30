package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.Order

@Composable
fun OrderFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFF6B35) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFFFF6B35)
        ),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}

@Composable
fun OrderStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        // Style đồng nhất với StatCard (Food/Customer)
        modifier = modifier.size(width = 145.dp, height = 110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.8f)
            )

            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.id, fontSize = 16.sp)
                // Badge trạng thái pastel giống Food/Customer
                val statusColor = order.status.color
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = order.status.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
                Text("${order.customerName} - ${order.location}", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
            }

            Text(order.items, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE0E0E0))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.time, fontSize = 13.sp, color = Color(0xFF999999))
                Text("${"%,d".format(order.price)}đ", fontSize = 18.sp, color = Color(0xFFFF6B35))
            }
        }
    }
}
