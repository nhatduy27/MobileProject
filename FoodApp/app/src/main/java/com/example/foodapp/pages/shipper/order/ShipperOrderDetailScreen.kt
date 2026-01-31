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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.example.foodapp.pages.shipper.theme.ShipperColors
import com.example.foodapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: ShipperOrderDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
                        Text(
                            stringResource(R.string.shipper_order_detail_title), 
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        uiState.order?.orderNumber?.let {
                            Text(
                                it, 
                                style = MaterialTheme.typography.bodySmall, 
                                color = ShipperColors.TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = ShipperColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShipperColors.Surface,
                    titleContentColor = ShipperColors.TextPrimary,
                    navigationIconContentColor = ShipperColors.TextPrimary
                )
            )
        },
        containerColor = ShipperColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.order == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ShipperColors.Primary
                )
            }
            
            uiState.order?.let { order ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Card
                    item {
                        OrderStatusCard(order = order)
                    }
                    
                    // Customer & Delivery Card
                    item {
                        CustomerDeliveryCard(
                            order = order,
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
                        ShopInfoCard(order = order)
                    }
                    
                    // Order Items Card
                    item {
                        OrderItemsCard(order = order)
                    }
                    
                    // Payment Summary Card
                    item {
                        PaymentSummaryCard(order = order)
                    }
                    
                    // Action Buttons
                    item {
                        ActionButtonsCard(
                            order = order,
                            isLoading = uiState.isLoading,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onShipping = { viewModel.markShipping(order.id) },
                            onDelivered = { viewModel.markDelivered(order.id) }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusCard(order: ShipperOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                    label = stringResource(R.string.shipper_order_timeline_created),
                    time = formatOrderTime(order.createdAt),
                    isCompleted = true
                )
                TimelineItem(
                    label = stringResource(R.string.shipper_order_timeline_ready),
                    time = formatOrderTime(order.readyAt),
                    isCompleted = order.readyAt != null
                )
                TimelineItem(
                    label = stringResource(R.string.shipper_order_timeline_shipping),
                    time = formatOrderTime(order.shippingAt),
                    isCompleted = order.shippingAt != null
                )
                TimelineItem(
                    label = stringResource(R.string.shipper_order_timeline_delivered),
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
            color = if (isCompleted) ShipperColors.Success else ShipperColors.Divider,
            modifier = Modifier.size(20.dp)
        ) {
            if (isCompleted) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = ShipperColors.Surface,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCompleted) ShipperColors.Success else ShipperColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        if (!time.isNullOrEmpty()) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = ShipperColors.TextTertiary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CustomerDeliveryCard(
    order: ShipperOrder,
    onCallCustomer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title = stringResource(R.string.shipper_order_delivery_info))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer Name & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: stringResource(R.string.shipper_order_customer_default),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = ShipperColors.TextPrimary
                    )
                    order.customerPhone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShipperColors.TextSecondary
                        )
                    }
                }
                
                // Call Button
                if (order.customerPhone != null) {
                    FilledIconButton(
                        onClick = onCallCustomer,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ShipperColors.Success
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = stringResource(R.string.shipper_order_call_customer),
                            tint = ShipperColors.Surface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ShipperColors.Divider)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Delivery Address
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = ShipperColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = order.shippingAddress ?: stringResource(R.string.shipper_order_no_address),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShipperColors.TextPrimary
                    )
                    order.deliveryAddress?.let { addr ->
                        val details = listOfNotNull(
                            addr.building?.let { stringResource(R.string.shipper_order_building, it) },
                            addr.room?.let { stringResource(R.string.shipper_order_room, it) },
                            addr.label
                        )
                        if (details.isNotEmpty()) {
                            Text(
                                text = details.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = ShipperColors.Primary,
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
                    color = ShipperColors.WarningLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Outlined.Notes,
                            contentDescription = null,
                            tint = ShipperColors.Warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.shipper_order_delivery_note),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = ShipperColors.Warning
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = ShipperColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopInfoCard(order: ShipperOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title = stringResource(R.string.shipper_order_shop))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = order.shopName ?: stringResource(R.string.shipper_order_shop_default),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ShipperColors.TextPrimary
            )
        }
    }
}

@Composable
private fun OrderItemsCard(order: ShipperOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title = stringResource(R.string.shipper_order_items, order.displayItemCount))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val displayItems = order.items.ifEmpty { order.itemsPreview ?: emptyList() }
            
            displayItems.forEachIndexed { index, item ->
                OrderItemRow(item = item)
                if (index < displayItems.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = ShipperColors.BorderLight)
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
                color = ShipperColors.PrimaryLight,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${item.quantity}x",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.Primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = ShipperColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            text = formatCurrency(item.subtotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ShipperColors.TextPrimary
        )
    }
}

@Composable
private fun PaymentSummaryCard(order: ShipperOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title = stringResource(R.string.shipper_order_payment))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Payment Method & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.shipper_order_payment_method), color = ShipperColors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentMethodBadge(paymentMethod = order.paymentMethod)
                    PaymentStatusBadge(paymentStatus = order.paymentStatus)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ShipperColors.BorderLight)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtotal
            if (order.subtotal > 0) {
                SummaryRow(label = stringResource(R.string.shipper_order_subtotal), value = formatCurrency(order.subtotal))
            }
            
            // Shipping Fee
            SummaryRow(label = stringResource(R.string.shipper_order_shipping_fee), value = formatCurrency(order.shipFee))
            
            // Discount
            if (order.discount > 0) {
                SummaryRow(
                    label = stringResource(R.string.shipper_order_discount) + (order.voucherCode?.let { " ($it)" } ?: ""),
                    value = "-${formatCurrency(order.discount)}",
                    valueColor = ShipperColors.Success
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ShipperColors.Divider, thickness = 1.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.shipper_order_total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.TextPrimary
                )
                Text(
                    text = formatCurrency(order.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ShipperColors.Primary
                )
            }
            
            // COD Note
            if (order.paymentMethod == "COD" && order.paymentStatus == "UNPAID") {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ShipperColors.WarningLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = ShipperColors.Warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.shipper_order_cod_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShipperColors.Warning
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
    valueColor: androidx.compose.ui.graphics.Color = ShipperColors.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = ShipperColors.TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

@Composable
private fun ActionButtonsCard(
    order: ShipperOrder,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onShipping: () -> Unit,
    onDelivered: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (order.status) {
                "READY" -> {
                    if (order.isAvailableForPickup) {
                        ActionButton(
                            text = stringResource(R.string.shipper_order_accept_btn),
                            color = ShipperColors.Primary,
                            isLoading = isLoading,
                            onClick = onAccept
                        )
                    } else {
                        ActionButton(
                            text = stringResource(R.string.shipper_order_start_delivery_btn),
                            color = ShipperColors.Info,
                            isLoading = isLoading,
                            onClick = onShipping
                        )
                    }
                }
                "SHIPPING" -> {
                    ActionButton(
                        text = stringResource(R.string.shipper_order_confirm_delivered_btn),
                        color = ShipperColors.Success,
                        isLoading = isLoading,
                        onClick = onDelivered
                    )
                }
                "DELIVERED" -> {
                    Surface(
                        color = ShipperColors.SuccessLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = ShipperColors.Success
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.shipper_order_completed),
                                fontWeight = FontWeight.Medium,
                                color = ShipperColors.Success
                            )
                        }
                    }
                }
                "CANCELLED" -> {
                    Surface(
                        color = ShipperColors.ErrorLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Cancel,
                                    contentDescription = null,
                                    tint = ShipperColors.Error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.shipper_order_cancelled_title),
                                    fontWeight = FontWeight.Medium,
                                    color = ShipperColors.Error
                                )
                            }
                            order.cancelReason?.let { reason ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.shipper_order_cancel_reason, reason),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ShipperColors.Error
                                )
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(R.string.shipper_order_processing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShipperColors.TextSecondary,
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
    color: androidx.compose.ui.graphics.Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = ShipperColors.Surface,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = ShipperColors.TextPrimary
    )
}
