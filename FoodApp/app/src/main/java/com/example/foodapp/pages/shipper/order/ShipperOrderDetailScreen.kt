package com.example.foodapp.pages.shipper.order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.pages.shipper.home.ShipperOrderCard
import com.example.foodapp.pages.shipper.home.formatCurrency

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
                title = { Text("Chi tiết đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
            
            uiState.order?.let { order ->
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                         ShipperOrderCard(order = order, onClick = {})
                    }
                    item {
                         Spacer(modifier = Modifier.height(16.dp))
                         Text("Chi tiết món ăn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                         Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(order.items.size) { index ->
                        val item = order.items[index]
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}x ${item.name}", modifier = Modifier.weight(1f))
                            Text(formatCurrency(item.price * item.quantity))
                        }
                         HorizontalDivider(color = Color.LightGray.copy(alpha=0.5f))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        // Action Buttons
                        ActionButtons(
                            status = order.status,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onShipping = { viewModel.markShipping(order.id) },
                            onDelivered = { viewModel.markDelivered(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    status: String,
    onAccept: () -> Unit,
    onShipping: () -> Unit,
    onDelivered: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (status) {
            "READY" -> {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("NHẬN ĐƠN")
                }
            }
            // If accepted, state might still be READY in some systems or ASSIGNED.
            // Backend Controller says: Accept -> SHIPPING (Wait, really?)
            // Controller: "Accept an order for delivery... (both transition to SHIPPING)"?
            // "Mark order as shipping/picked up... Kept for API completeness... redundant with accept"
            // So if status is SHIPPING, show Delivered button.
            // If I accepted, status becomes SHIPPING?
            // Let's assume:
            // READY -> Accept -> SHIPPING
            // SHIPPING -> Delivered -> DELIVERED
            "SHIPPING" -> {
                Button(
                    onClick = onDelivered,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("ĐÃ GIAO HÀNG")
                }
            }
        }
    }
}
