package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Define standard status colors
val ColorPending = Color(0xFFFF9800)   // Orange
val ColorPreparing = Color(0xFF2196F3) // Blue
val ColorDelivering = Color(0xFF9C27B0) // Purple
val ColorCompleted = Color(0xFF4CAF50) // Green
val ColorCancelled = Color(0xFFF44336) // Red
val ColorReady = Color(0xFF009688)     // Teal

@Composable
fun DonutChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    thickness: Dp = 20.dp
) {
    val total = data.values.sum()
    val proportions = data.mapValues { if (total > 0) it.value.toFloat() / total else 0f }
    
    // Fixed order for consistency
    val orderedKeys = listOf("PENDING", "PREPARING", "READY", "DELIVERING", "COMPLETED", "CANCELLED")
    
    val colors = mapOf(
        "PENDING" to ColorPending,
        "PREPARING" to ColorPreparing,
        "READY" to ColorReady,
        "DELIVERING" to ColorDelivering,
        "COMPLETED" to ColorCompleted,
        "CANCELLED" to ColorCancelled
    )

    Canvas(modifier = modifier) {
        var startAngle = -90f
        val strokeWidth = thickness.toPx()
        val radius = size.minDimension / 2 - strokeWidth / 2
        val center = Offset(size.width / 2, size.height / 2)

        if (total == 0) {
            drawCircle(
                color = Color.LightGray.copy(alpha=0.2f),
                style = Stroke(width = strokeWidth)
            )
        } else {
            orderedKeys.forEach { key ->
                val value = proportions[key] ?: 0f
                if (value > 0) {
                    val sweepAngle = value * 360f
                    drawArc(
                        color = colors[key] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt) // Butt cap for cleaner segments
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.Black)
            }
        }
    }
}

@Composable
fun HorizontalBar(
    value: Float, // 0..1
    color: Color,
    height: Dp = 8.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.LightGray.copy(alpha = 0.2f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(value)
                .fillMaxHeight()
                .background(color, CircleShape)
        )
    }
}
