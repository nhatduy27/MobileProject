package com.example.foodapp.pages.client.components.payment

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import com.example.foodapp.ui.theme.*


@Composable
fun OrderSummarySection(
    productPrice: Double,
    discount: Double = 0.0,
    modifier: Modifier = Modifier,
    context: Context
) {
    val total = productPrice - discount

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.order_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Giá sản phẩm
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = context.getString(R.string.product_price_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(productPrice, context),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Hiển thị discount nếu có
            if (discount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.discount_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessColor
                    )
                    Text(
                        text = "-${formatCurrency(discount, context)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessColor
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = BorderColor
            )

            // Tổng cộng
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.total_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = formatCurrency(total, context),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryColor
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double, context: Context): String {
    return String.format(context.getString(R.string.currency_format), amount)
}