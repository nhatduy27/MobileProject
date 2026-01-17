package com.example.foodapp.pages.owner.shippers

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.shipper.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card hiển thị đơn xin làm shipper
 */
@Composable
fun ApplicationItem(
    application: ShipperApplication,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    isProcessing: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
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
                    // Avatar
                    AsyncImage(
                        model = application.userAvatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5)),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = application.userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = application.userPhone,
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Status Badge
                StatusBadge(application.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.DirectionsBike,
                    label = getVehicleTypeName(application.vehicleType),
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    icon = Icons.Default.Pin,
                    label = application.vehicleNumber,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expanded Content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(16.dp))

                // Message
                if (application.message.isNotBlank()) {
                    Column {
                        Text(
                            "Lời nhắn:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            application.message,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ID Card Number
                InfoRow("CMND/CCCD:", application.idCardNumber)

                // Documents
                Text(
                    "Giấy tờ đã tải lên:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DocumentPreview("CMND mặt trước", application.idCardFrontUrl, Modifier.weight(1f))
                    DocumentPreview("CMND mặt sau", application.idCardBackUrl, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                DocumentPreview("Bằng lái xe", application.driverLicenseUrl)

                // Created Date
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("Ngày nộp:", formatDate(application.createdAt))

                // Reviewed Info (if rejected)
                if (application.status == ApplicationStatus.REJECTED && application.rejectReason != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Lý do từ chối:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                application.rejectReason,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }

            // Action Buttons (only for PENDING)
            if (application.status == ApplicationStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF44336)),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Từ chối")
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duyệt")
                        }
                    }
                }
            }
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        var rejectReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(48.dp))
            },
            title = { Text("Từ chối đơn xin làm shipper") },
            text = {
                Column {
                    Text("Vui lòng nhập lý do từ chối:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("VD: Không đủ điều kiện") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            onReject(rejectReason)
                            showRejectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    enabled = rejectReason.isNotBlank()
                ) {
                    Text("Từ chối")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: ApplicationStatus) {
    val (color, text) = when (status) {
        ApplicationStatus.PENDING -> Color(0xFFFF9800) to "Chờ duyệt"
        ApplicationStatus.APPROVED -> Color(0xFF4CAF50) to "Đã duyệt"
        ApplicationStatus.REJECTED -> Color(0xFFF44336) to "Đã từ chối"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFF6B35), modifier = Modifier.size(16.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF1A1A1A))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF757575))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
    }
}

@Composable
private fun DocumentPreview(label: String, imageUrl: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(4.dp))
        AsyncImage(
            model = imageUrl,
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5)),
            contentScale = ContentScale.Crop
        )
    }
}

private fun getVehicleTypeName(type: VehicleType): String = when (type) {
    VehicleType.MOTORBIKE -> "Xe máy"
    VehicleType.CAR -> "Ô tô"
    VehicleType.BICYCLE -> "Xe đạp"
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
