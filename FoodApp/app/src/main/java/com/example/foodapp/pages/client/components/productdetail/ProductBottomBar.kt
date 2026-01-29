package com.example.foodapp.pages.client.components.productdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shared.product.Product

@Composable
fun ProductBottomBar(
    product: Product,
    quantity: Int,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit, // Có thể giữ hoặc xóa tham số này
    isAddingToCart: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 24.dp,
        tonalElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Quantity selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF5F5F5),
                border = BorderStroke(1.5.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onQuantityDecrease,
                        modifier = Modifier.size(48.dp),
                        enabled = product.isAvailable && quantity > 1
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Giảm",
                            tint = if (quantity > 1 && product.isAvailable) Color(0xFF424242) else Color(0xFFBDBDBD),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .width(52.dp)
                            .height(48.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = quantity.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (product.isAvailable) Color(0xFF212121) else Color(0xFFBDBDBD)
                            )
                        }
                    }

                    IconButton(
                        onClick = onQuantityIncrease,
                        modifier = Modifier.size(48.dp),
                        enabled = product.isAvailable
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tăng",
                            tint = if (product.isAvailable) Color(0xFF424242) else Color(0xFFBDBDBD),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // CHỈ CÒN NÚT THÊM VÀO GIỎ HÀNG
            if (isAddingToCart) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAddToCart,
                    enabled = product.isAvailable,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm vào giỏ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}