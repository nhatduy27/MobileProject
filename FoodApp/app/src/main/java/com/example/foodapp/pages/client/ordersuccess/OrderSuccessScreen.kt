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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
                Text("Đang tải thông tin đơn hàng...")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Đơn hàng thành công",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackToHome() }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
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
                            contentDescription = "Thành công",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Green
                        )
                    }
                }

                item {
                    Text(
                        text = "🎉 Đặt hàng thành công!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Text(
                        text = "Cảm ơn bạn đã đặt hàng. Đơn hàng của bạn đã được xác nhận và đang được xử lý.",
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
                                    contentDescription = "Thông tin",
                                    tint = PrimaryColor
                                )
                                Text(
                                    text = "Thông tin từ cửa hàng",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor
                                )
                            }
                            Text(
                                text = "Cửa hàng sẽ liên hệ với bạn trong thời gian sớm nhất để xác nhận đơn hàng và thời gian giao hàng.",
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
                                text = "Bạn có thể:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            InfoRow(
                                icon = Icons.Default.Home,
                                title = "Tiếp tục mua sắm",
                                description = "Quay lại trang chủ để xem thêm sản phẩm"
                            )

                            Divider()

                            InfoRow(
                                icon = Icons.Default.Receipt,
                                title = "Xem chi tiết đơn hàng",
                                description = "Theo dõi trạng thái và chi tiết đơn hàng"
                            )

                            Divider()

                            InfoRow(
                                icon = Icons.Default.ShoppingCart,
                                title = "Kiểm tra lịch sử đơn hàng",
                                description = "Xem tất cả đơn hàng của bạn"
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
                text = "📦 Sản phẩm đã đặt",
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
                text = "${price.formatVND()}",
                fontSize = 14.sp,
                color = PrimaryColor
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "x$quantity",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "${subtotal.formatVND()}",
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
                text = "📋 Thông tin đơn hàng",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Divider()

            InfoItem(
                label = "Mã đơn hàng:",
                value = order.orderNumber,
                isHighlighted = true
            )

            InfoItem(
                label = "Cửa hàng:",
                value = order.shopName
            )

            InfoItem(
                label = "Số lượng sản phẩm:",
                value = "${order.items.size} sản phẩm"
            )

            InfoItem(
                label = "Tổng tiền:",
                value = "${order.total.formatVND()}"
            )

            InfoItem(
                label = "Phương thức thanh toán:",
                value = when (order.paymentMethod) {
                    "COD" -> "Thanh toán khi nhận hàng"
                    "SEPAY" -> "Chuyển khoản ngân hàng"
                    else -> order.paymentMethod
                }
            )

            InfoItem(
                label = "Trạng thái:",
                value = when (order.status) {
                    "PENDING" -> "Đang chờ xác nhận"
                    "CONFIRMED" -> "Đã xác nhận"
                    "PREPARING" -> "Đang chuẩn bị"
                    "READY" -> "Sẵn sàng giao"
                    "SHIPPING" -> "Đang giao hàng"
                    "DELIVERED" -> "Đã giao"
                    "CANCELLED" -> "Đã hủy"
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
                    label = "Địa chỉ giao hàng:",
                    value = "${order.deliveryAddress.fullAddress}${if (order.deliveryAddress.room != null) " - Phòng ${order.deliveryAddress.room}" else ""}",
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
                contentDescription = "Chi tiết",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Xem chi tiết đơn hàng",
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
                contentDescription = "Trang chủ",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tiếp tục mua sắm",
                fontSize = 16.sp
            )
        }
    }
}

// Các hàm helper
fun Double.formatVND(): String {
    return String.format("%,.0fđ", this).replace(",", ".")
}
