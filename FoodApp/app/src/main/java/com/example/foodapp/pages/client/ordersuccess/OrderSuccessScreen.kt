package com.example.foodapp.pages.client.ordersuccess

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.order.OrderApiModel
import com.example.foodapp.ui.theme.*
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    orderJson: String?,
    onBackToHome: () -> Unit,
    onViewOrderDetails: (String) -> Unit
) {
    val viewModel: OrderSuccessViewModel = viewModel()
    val gson = Gson()

    // Lấy thông tin đơn hàng từ JSON
    val order = remember(orderJson) {
        if (!orderJson.isNullOrEmpty()) {
            try {
                gson.fromJson<OrderApiModel>(orderJson, OrderApiModel::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // Khởi tạo ViewModel với thông tin đơn hàng
    LaunchedEffect(order) {
        if (order != null) {
            viewModel.initializeWithOrder(order)
        }
    }

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(stringResource(R.string.loading_order_info))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.order_success_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackToHome() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }
            )
        },
        bottomBar = {
            OrderSuccessBottomBar(
                orderId = order.id,
                onBackToHome = onBackToHome,
                onViewOrderDetails = {
                    onViewOrderDetails(order.id)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGray)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Icon thành công
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Green.copy(alpha = 0.1f))
                            .border(2.dp, Color.Green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.success_content_description),
                            modifier = Modifier.size(64.dp),
                            tint = Color.Green
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.order_success_header),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.order_success_message),
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Thông tin đơn hàng
                    OrderInfoCard(order = order)
                }

                item {
                    OrderProductsCard(order = order)
                }

                item {
                    // Lời nhắn từ cửa hàng
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.info_content_description),
                                    tint = PrimaryColor
                                )
                                Text(
                                    text = stringResource(R.string.shop_info_title),
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor
                                )
                            }
                            Text(
                                text = stringResource(R.string.shop_info_message),
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item {
                    // Hướng dẫn tiếp theo
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.next_steps_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            InfoRow(
                                icon = Icons.Default.Home,
                                title = stringResource(R.string.continue_shopping_title),
                                description = stringResource(R.string.continue_shopping_desc)
                            )

                            Divider()

                            InfoRow(
                                icon = Icons.Default.Receipt,
                                title = stringResource(R.string.view_order_details_title),
                                description = stringResource(R.string.view_order_details_desc)
                            )

                            Divider()

                            InfoRow(
                                icon = Icons.Default.ShoppingCart,
                                title = stringResource(R.string.check_order_history_title),
                                description = stringResource(R.string.check_order_history_desc)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun OrderProductsCard(order: OrderApiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.ordered_products_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Divider()

            order.items.forEachIndexed { index, item ->
                ProductRow(
                    productName = item.productName,
                    price = item.price,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
                if (index < order.items.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ProductRow(
    productName: String,
    price: Double,
    quantity: Int,
    subtotal: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hình ảnh sản phẩm
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            // Placeholder for product image
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = productName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = price.formatVND(),
                fontSize = 14.sp,
                color = PrimaryColor
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.quantity_display, quantity),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = subtotal.formatVND(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun OrderInfoCard(order: OrderApiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.order_information_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Divider()

            InfoItem(
                label = stringResource(R.string.order_code_label),
                value = order.orderNumber,
                isHighlighted = true
            )

            InfoItem(
                label = stringResource(R.string.shop_name_label),
                value = order.shopName
            )

            InfoItem(
                label = stringResource(R.string.product_count_label),
                value = stringResource(R.string.product_count_value, order.items.size)
            )

            InfoItem(
                label = stringResource(R.string.total_amount_label),
                value = order.total.formatVND()
            )

            InfoItem(
                label = stringResource(R.string.payment_method_label),
                value = when (order.paymentMethod) {
                    "COD" -> stringResource(R.string.payment_method_cod)
                    "SEPAY" -> stringResource(R.string.payment_method_sepay)
                    else -> order.paymentMethod
                }
            )

            InfoItem(
                label = stringResource(R.string.order_status_label),
                value = when (order.status) {
                    "PENDING" -> stringResource(R.string.order_status_pending)
                    "CONFIRMED" -> stringResource(R.string.order_status_confirmed)
                    "PREPARING" -> stringResource(R.string.order_status_preparing)
                    "READY" -> stringResource(R.string.order_status_ready)
                    "SHIPPING" -> stringResource(R.string.order_status_shipping)
                    "DELIVERED" -> stringResource(R.string.order_status_delivered)
                    "CANCELLED" -> stringResource(R.string.order_status_cancelled)
                    else -> order.status
                },
                valueColor = when (order.status) {
                    "PENDING" -> Color.Cyan
                    "CONFIRMED" -> Color.Blue
                    "DELIVERED" -> Color.Green
                    "CANCELLED" -> Color.Red
                    else -> Color.Black
                }
            )
            if (order.deliveryAddress != null) {
                InfoItem(
                    label = stringResource(R.string.delivery_address_label),
                    value = stringResource(
                        R.string.delivery_address_value,
                        order.deliveryAddress.fullAddress,
                        if (order.deliveryAddress.room != null) stringResource(R.string.room_label, order.deliveryAddress.room) else ""
                    ),
                    isMultiline = true
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    isMultiline: Boolean = false,
    valueColor: Color = if (isHighlighted) PrimaryColor else Color.Black
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = valueColor,
            modifier = if (isMultiline) Modifier.fillMaxWidth() else Modifier,
            maxLines = if (isMultiline) 3 else 1,
            overflow = if (isMultiline) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryColor,
            modifier = Modifier.size(24.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OrderSuccessBottomBar(
    orderId: String,
    onBackToHome: () -> Unit,
    onViewOrderDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nút chính: Xem chi tiết đơn hàng
        Button(
            onClick = onViewOrderDetails,
            enabled = orderId.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = stringResource(R.string.details_content_description),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.view_order_details_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Nút phụ: Về trang chủ
        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryColor
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PrimaryColor)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.home_content_description),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.continue_shopping_button),
                fontSize = 16.sp
            )
        }
    }
}

// Các hàm helper
fun Double.formatVND(): String {
    return String.format("%,.0fđ", this).replace(",", ".")
}