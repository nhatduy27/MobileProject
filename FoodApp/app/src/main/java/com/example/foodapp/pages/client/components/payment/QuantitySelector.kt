package com.example.foodapp.pages.client.components.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.ui.theme.*

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BackgroundGray,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(36.dp),
                enabled = quantity > 1
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = stringResource(id = R.string.decrease_quantity),
                    tint = if (quantity > 1) PrimaryOrange else TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = quantity.toString(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.widthIn(min = 32.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrange.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.increase_quantity),
                    tint = PrimaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}