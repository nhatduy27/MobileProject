package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Customer(
    val name: String,
    val type: String,
    val contact: String,
    val orders: String,
    val revenue: String
)

@Composable
fun CustomerCard(customer: Customer) {
    val typeColor = when (customer.type) {
        "VIP" -> Color(0xFFFFD700)
        "Thường xuyên" -> Color(0xFF4CAF50)
        "Mới" -> Color(0xFF2196F3)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(typeColor)
            )

            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.name, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(customer.type, fontSize = 11.sp, color = Color.White, modifier = Modifier
                        .background(typeColor)
                        .padding(horizontal = 10.dp, vertical = 3.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(customer.contact, fontSize = 13.sp, color = Color(0xFF757575))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.orders, fontSize = 12.sp, color = Color(0xFF999999), modifier = Modifier.weight(1f))
                    Text(customer.revenue, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFFFF6B35))
                }
            }
        }
    }
}