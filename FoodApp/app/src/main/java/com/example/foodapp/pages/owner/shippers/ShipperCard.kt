package com.example.foodapp.pages.owner.shippers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.shipper.*
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card hiển thị shipper đang hoạt động
 */
@Composable
fun ShipperCard(
    shipper: Shipper,
    onRemove: () -> Unit,
    isProcessing: Boolean = false
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        elevation = CardDefaults.cardElevation(OwnerDimens.CardElevation.dp),
        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar with status indicator
                    Box {
                        AsyncImage(
                            model = shipper.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(OwnerColors.SurfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        // Status dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.White, CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(getStatusColor(shipper.shipperInfo?.status ?: ShipperStatus.OFFLINE), CircleShape)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = shipper.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OwnerColors.TextPrimary
                        )
                        Text(
                            text = shipper.phone ?: "",
                            fontSize = 14.sp,
                            color = OwnerColors.TextSecondary
                        )
                    }
                }

                // Remove button
                IconButton(
                    onClick = { showRemoveDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(OwnerColors.ErrorLight, RoundedCornerShape(8.dp)),
                    enabled = !isProcessing
                ) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = "Remove",
                        tint = OwnerColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            ShipperStatusBadge(shipper.shipperInfo?.status ?: ShipperStatus.OFFLINE)

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    icon = Icons.Default.Star,
                    label = "Rating",
                    value = String.format("%.1f", shipper.shipperInfo?.rating ?: 0.0),
                    color = Color(0xFFFFC107),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    icon = Icons.Default.LocalShipping,
                    label = "Đơn đã giao",
                    value = (shipper.shipperInfo?.totalDeliveries ?: 0).toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OwnerColors.SurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = OwnerColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            getVehicleTypeName(shipper.shipperInfo?.vehicleType ?: VehicleType.MOTORBIKE),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OwnerColors.TextPrimary
                        )
                        Text(
                            shipper.shipperInfo?.vehicleNumber ?: "",
                            fontSize = 12.sp,
                            color = OwnerColors.TextSecondary
                        )
                    }
                }

                // Current orders badge
                val currentOrders = shipper.shipperInfo?.currentOrders ?: emptyList()
                if (currentOrders.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(OwnerColors.Primary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${currentOrders.size} đơn",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Joined date
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = OwnerColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Tham gia: ${formatDate(shipper.shipperInfo?.joinedAt ?: "")}",
                    fontSize = 12.sp,
                    color = OwnerColors.TextSecondary
                )
            }
        }
    }

    // Remove confirmation dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = OwnerColors.Error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Xóa shipper?") },
            text = {
                Column {
                    Text("Bạn có chắc muốn xóa \"${shipper.name}\" khỏi danh sách shipper?")
                    val dialogCurrentOrders = shipper.shipperInfo?.currentOrders ?: emptyList()
                    if (dialogCurrentOrders.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Shipper này đang có ${dialogCurrentOrders.size} đơn hàng chưa hoàn thành!",
                            color = OwnerColors.Error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove()
                        showRemoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ShipperStatusBadge(status: ShipperStatus) {
    val (gradient, text) = when (status) {
        ShipperStatus.AVAILABLE -> Brush.horizontalGradient(
            colors = listOf(OwnerColors.Success, Color(0xFF66BB6A))
        ) to "Sẵn sàng"
        ShipperStatus.BUSY -> Brush.horizontalGradient(
            colors = listOf(OwnerColors.Warning, Color(0xFFFFB74D))
        ) to "Đang giao hàng"
        ShipperStatus.OFFLINE -> Brush.horizontalGradient(
            colors = listOf(OwnerColors.TextTertiary, OwnerColors.BorderLight)
        ) to "Offline"
    }

    Box(
        modifier = Modifier
            .background(gradient, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.White, CircleShape)
            )
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OwnerColors.TextPrimary
            )
            Text(
                label,
                fontSize = 11.sp,
                color = OwnerColors.TextSecondary
            )
        }
    }
}

private fun getStatusColor(status: ShipperStatus): Color = when (status) {
    ShipperStatus.AVAILABLE -> OwnerColors.Success
    ShipperStatus.BUSY -> OwnerColors.Warning
    ShipperStatus.OFFLINE -> OwnerColors.TextTertiary
}

private fun getVehicleTypeName(type: VehicleType): String = when (type) {
    VehicleType.MOTORBIKE -> "Xe máy"
    VehicleType.CAR -> "Ô tô"
    VehicleType.BICYCLE -> "Xe đạp"
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
