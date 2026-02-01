package com.example.foodapp.pages.client.components.payment

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.ui.theme.*

@Composable
fun PaymentBottomBar(
    totalPrice: Double,
    shippingFee: Double = 15000.0, // Đưa shipping fee ra parameter để linh hoạt
    isLoading: Boolean,
    onPlaceOrder: () -> Unit,
    context: Context
) {
    val finalPrice = totalPrice + shippingFee

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.payment_total_label),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = formatPrice(finalPrice, context),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
            }

            Button(
                onClick = onPlaceOrder,
                enabled = !isLoading,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.place_order_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Double, context: Context): String {
    return String.format(context.getString(R.string.currency_vnd_format), price)
}