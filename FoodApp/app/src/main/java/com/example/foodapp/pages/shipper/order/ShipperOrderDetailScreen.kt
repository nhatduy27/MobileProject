package com.example.foodapp.pages.shipper.order

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.data.model.shipper.order.ShipperOrderItem
import com.example.foodapp.pages.shipper.home.formatCurrency
import com.example.foodapp.pages.shipper.home.formatOrderTime
import com.example.foodapp.pages.shipper.home.StatusBadge
import com.example.foodapp.pages.shipper.home.PaymentMethodBadge
import com.example.foodapp.pages.shipper.home.PaymentStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: ShipperOrderDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val mainColor = Color(0xFFFF6B35)
    
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold)
                        uiState.order?.orderNumber?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = mainColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading && uiState.order == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = mainColor
                )
            }
            
            uiState.order?.let { order ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Card
                    item {
                        OrderStatusCard(order = order, mainColor = mainColor)
                    }
                    
                    // Customer & Delivery Card
                    item {
                        CustomerDeliveryCard(
                            order = order,
                            mainColor = mainColor,
                            onCallCustomer = {
                                order.customerPhone?.let { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                    
                    // Shop Info Card
                    item {
                        ShopInfoCard(order = order, mainColor = mainColor)
                    }
                    
                    // Order Items Card
                    item {
                        OrderItemsCard(order = order, mainColor = mainColor)
                    }
                    
                    // Payment Summary Card
                    item {
                        PaymentSummaryCard(order = order, mainColor = mainColor)
                    }
                    
                    // Action Buttons
                    item {
                        ActionButtonsCard(
                            order = order,
                            mainColor = mainColor,
                            isLoading = uiState.isLoading,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onShipping = { viewModel.markShipping(order.id) },
                            onDelivered = { viewModel.markDelivered(order.id) }
                        )
                    }
                    
                    // Spacing at bottom
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusCard(order: ShipperOrder, mainColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusBadge(status = order.status)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimelineItem(
                    label = "Tạo đơn",
                    time = formatOrderTime(order.createdAt),
                    isCompleted = true
                )
                TimelineItem(
                    label = "Sẵn sàng",
                    time = formatOrderTime(order.readyAt),
                    isCompleted = order.readyAt != null
                )
                TimelineItem(
                    label = "Đang giao",
                    time = formatOrderTime(order.shippingAt),
                    isCompleted = order.shippingAt != null
                )
                TimelineItem(
                    label = "Giao xong",
                    time = formatOrderTime(order.deliveredAt),
                    isCompleted = order.deliveredAt != null
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    label: String,
    time: String?,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCompleted) Color(0xFF4CAF50) else Color.Gray,
            textAlign = TextAlign.Center
        )
        if (!time.isNullOrEmpty()) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CustomerDeliveryCard(
    order: ShipperOrder,
    mainColor: Color,
    onCallCustomer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionTitle(icon = Icons.Default.Person, title = "Thông tin giao hàng", color = mainColor)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Customer Name & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: "Khách hàng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    order.customerPhone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                
                // Call Button
                if (order.customerPhone != null) {
                    FilledIconButton(
                        onClick = onCallCustomer,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Gọi khách",
                            tint = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Delivery Address
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = order.shippingAddress ?: "Chưa có địa chỉ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    order.deliveryAddress?.let { addr ->
                        val details = listOfNotNull(
                            addr.building?.let { "Tòa $it" },
                            addr.room?.let { "Phòng $it" },
                            addr.label
                        )
                        if (details.isNotEmpty()) {
                            Text(
                                text = details.joinToString(" • "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = mainColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Delivery Note
            order.deliveryNote?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ghi chú giao hàng",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF795548)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopInfoCard(order: ShipperOrder, mainColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionTitle(icon = Icons.Default.Store, title = "Cửa hàng", color = mainColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = order.shopName ?: "Cửa hàng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OrderItemsCard(order: ShipperOrder, mainColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionTitle(
                icon = Icons.Default.Restaurant,
                title = "Đơn hàng (${order.displayItemCount} món)",
                color = mainColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Use items or itemsPreview
            val displayItems = order.items.ifEmpty { order.itemsPreview ?: emptyList() }
            
            displayItems.forEachIndexed { index, item ->
                OrderItemRow(item = item)
                if (index < displayItems.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF5F5F5))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: ShipperOrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Badge
            Surface(
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${item.quantity}x",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            text = formatCurrency(item.subtotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242)
        )
    }
}

@Composable
private fun PaymentSummaryCard(order: ShipperOrder, mainColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionTitle(icon = Icons.Default.Payment, title = "Thanh toán", color = mainColor)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment Method & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Phương thức", color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethodBadge(paymentMethod = order.paymentMethod)
                    PaymentStatusBadge(paymentStatus = order.paymentStatus)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtotal
            if (order.subtotal > 0) {
                SummaryRow(label = "Tạm tính", value = formatCurrency(order.subtotal))
            }
            
            // Shipping Fee
            SummaryRow(label = "Phí giao hàng", value = formatCurrency(order.shipFee))
            
            // Discount
            if (order.discount > 0) {
                SummaryRow(
                    label = "Giảm giá" + (order.voucherCode?.let { " ($it)" } ?: ""),
                    value = "-${formatCurrency(order.discount)}",
                    valueColor = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TỔNG CỘNG",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatCurrency(order.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = mainColor
                )
            }
            
            // COD Note
            if (order.paymentMethod == "COD" && order.paymentStatus == "UNPAID") {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thu tiền mặt khi giao hàng",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF424242)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

@Composable
private fun ActionButtonsCard(
    order: ShipperOrder,
    mainColor: Color,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onShipping: () -> Unit,
    onDelivered: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            when (order.status) {
                "READY" -> {
                    if (order.isAvailableForPickup) {
                        // Available order - can accept
                        ActionButton(
                            text = "NHẬN ĐƠN HÀNG",
                            icon = Icons.Default.CheckCircle,
                            color = mainColor,
                            isLoading = isLoading,
                            onClick = onAccept
                        )
                    } else {
                        // Already assigned, can start shipping
                        ActionButton(
                            text = "BẮT ĐẦU GIAO HÀNG",
                            icon = Icons.Default.LocalShipping,
                            color = Color(0xFF2196F3),
                            isLoading = isLoading,
                            onClick = onShipping
                        )
                    }
                }
                "SHIPPING" -> {
                    ActionButton(
                        text = "XÁC NHẬN ĐÃ GIAO",
                        icon = Icons.Default.DoneAll,
                        color = Color(0xFF4CAF50),
                        isLoading = isLoading,
                        onClick = onDelivered
                    )
                }
                "DELIVERED" -> {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Đơn hàng đã hoàn thành",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                "CANCELLED" -> {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Đơn hàng đã hủy",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFC62828)
                                )
                            }
                            order.cancelReason?.let { reason ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Lý do: $reason",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Other statuses - no action available
                    Text(
                        text = "Đơn hàng đang được xử lý",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
