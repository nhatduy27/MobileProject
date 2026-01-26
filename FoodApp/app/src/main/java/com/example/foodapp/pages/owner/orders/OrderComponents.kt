package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.foodapp.data.model.owner.order.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailBottomSheet(
    order: OrderDetail,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onPreparing: (String) -> Unit,
    onReady: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val statusColor = getStatusColor(order.status)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = order.orderNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = order.status.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Customer Info Section
            SectionCard(title = "Khách hàng") {
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Tên",
                    value = order.customer.displayName ?: "Không có tên"
                )
                order.customer.phone?.let { phone ->
                    InfoRow(
                        icon = Icons.Default.Phone,
                        label = "SĐT",
                        value = phone
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Address Section
            SectionCard(title = "Địa chỉ giao hàng") {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Địa chỉ",
                    value = order.deliveryAddress.getDisplayAddress()
                )
                order.deliveryNote?.let { note ->
                    InfoRow(
                        icon = Icons.Default.Notes,
                        label = "Ghi chú",
                        value = note
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Items Section
            SectionCard(title = "Món đã đặt (${order.items.size} món)") {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "x${item.quantity} × %,d₫".format(item.price),
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                        Text(
                            text = "%,d₫".format(item.subtotal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )
                    }
                    if (item != order.items.last()) {
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Summary Section
            SectionCard(title = "Thanh toán") {
                SummaryRow("Tạm tính", order.subtotal)
                SummaryRow("Phí giao hàng", order.shipFee)
                if (order.discount > 0) {
                    SummaryRow("Giảm giá", -order.discount, isDiscount = true)
                }
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tổng cộng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "%,d₫".format(order.total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Phương thức",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = order.paymentMethod.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Trạng thái",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                    val paymentColor = getPaymentStatusColor(order.paymentStatus)
                    Text(
                        text = order.paymentStatus.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = paymentColor
                    )
                }
            }

            // Cancellation info if cancelled
            if (order.status == ShopOrderStatus.CANCELLED && order.cancelReason != null) {
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(title = "Thông tin hủy đơn") {
                    InfoRow(
                        icon = Icons.Default.Cancel,
                        label = "Lý do",
                        value = order.cancelReason
                    )
                    order.cancelledBy?.let { by ->
                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "Hủy bởi",
                            value = when (by) {
                                "CUSTOMER" -> "Khách hàng"
                                "OWNER" -> "Chủ quán"
                                "SYSTEM" -> "Hệ thống"
                                else -> by
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            val actions = order.getAvailableActions()
            if (actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button (if available)
                    if (actions.contains(OrderAction.CANCEL)) {
                        OutlinedButton(
                            onClick = { onCancel(order.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF44336)
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hủy đơn", fontSize = 14.sp)
                        }
                    }

                    // Main action button
                    val mainAction = actions.firstOrNull { it != OrderAction.CANCEL }
                    mainAction?.let { action ->
                        Button(
                            onClick = {
                                when (action) {
                                    OrderAction.CONFIRM -> onConfirm(order.id)
                                    OrderAction.PREPARING -> onPreparing(order.id)
                                    OrderAction.READY -> onReady(order.id)
                                    OrderAction.CANCEL -> onCancel(order.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (action) {
                                    OrderAction.CONFIRM -> Color(0xFF4CAF50)
                                    OrderAction.PREPARING -> Color(0xFF9C27B0)
                                    OrderAction.READY -> Color(0xFF2196F3)
                                    OrderAction.CANCEL -> Color(0xFFF44336)
                                }
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                when (action) {
                                    OrderAction.CONFIRM -> Icons.Default.Check
                                    OrderAction.PREPARING -> Icons.Default.Restaurant
                                    OrderAction.READY -> Icons.Default.Done
                                    OrderAction.CANCEL -> Icons.Default.Close
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(action.actionText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF999999),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Long,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = if (isDiscount) "-%,d₫".format(-amount) else "%,d₫".format(amount),
            fontSize = 14.sp,
            color = if (isDiscount) Color(0xFF4CAF50) else Color(0xFF333333)
        )
    }
}
