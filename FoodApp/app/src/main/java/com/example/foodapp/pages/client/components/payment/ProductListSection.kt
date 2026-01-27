package com.example.foodapp.pages.client.components.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.payment.CartItem
import com.example.foodapp.ui.theme.*

@Composable
fun ProductListSection(
    items: List<CartItem>,
    onQuantityIncrease: (String) -> Unit,
    onQuantityDecrease: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            // Header Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sản phẩm đã chọn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách sản phẩm
            items.forEachIndexed { index, cartItem ->
                ProductInfoRow(
                    product = cartItem.product,
                    quantity = cartItem.quantity
                )
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductInfoRow(
    product: Product,
    quantity: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // INFO SECTION
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Số lượng: $quantity",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        // PRICE SECTION
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = product.price,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            )
        }
    }
}