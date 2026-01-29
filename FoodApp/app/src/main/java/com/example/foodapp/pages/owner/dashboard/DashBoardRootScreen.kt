package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

// Import FoodsScreen
import com.example.foodapp.pages.owner.foods.FoodsScreen
// Import OrdersScreen
import com.example.foodapp.pages.owner.orders.OrdersScreen
// Import RevenueScreen
import com.example.foodapp.pages.owner.revenue.RevenueScreen
// Import ShippersScreen
import com.example.foodapp.pages.owner.shippers.ShippersScreen
// Import VouchersScreen
import com.example.foodapp.pages.owner.vouchers.VouchersScreen
// Import ReviewsScreen
import com.example.foodapp.pages.owner.reviews.ReviewsScreen
// Import ChatbotScreen
import com.example.foodapp.pages.owner.chatbot.ChatbotScreen
// Import NotificationBell
import com.example.foodapp.pages.owner.notifications.NotificationBell
// Import SettingsScreen
import com.example.foodapp.pages.owner.settings.SettingsNavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodapp.pages.owner.customer.CustomerScreenMain
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.owner.shopmanagement.ShopManagementViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.foodapp.pages.owner.chat.OwnerConversationsScreen
import com.example.foodapp.pages.owner.chat.OwnerChatDetailScreen

@Preview(showBackground = true, backgroundColor = 0xFF00FF00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoardRootScreen(navController: NavHostController) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Thống nhất dùng chữ thường cho tên màn hình để tránh lỗi
    var currentScreen by remember { mutableStateOf("dashboard") }
    
    // NavController riêng cho settings navigation
    val settingsNavController = rememberNavController()
    
    // Chat navigation state
    var currentChatRoute by remember { mutableStateOf("conversations_list") }
    var currentConversationId by remember { mutableStateOf<String?>(null) }

    // ViewModel for Shop Info
    val context = LocalContext.current
    val shopViewModel: ShopManagementViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ShopManagementViewModel(context) as T
            }
        }
    )
    val shopState by shopViewModel.uiState.collectAsState()
    
    // Reload shop data when returning from settings screen
    LaunchedEffect(currentScreen) {
        if (currentScreen != "settings") {
            // Reload shop data to get latest updates
            // This ensures sidebar shows updated shop name and logo
            shopViewModel.refreshShopData()
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(),
                drawerContainerColor = Color.White
            ) {

                // ===== Sidebar Header =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp), // More whitespace
                ) {
                    // Close Button
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(24.dp)
                            .clickable { scope.launch { drawerState.close() } }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Brand Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                             if (shopState.logoUrl.isNotEmpty()) {
                                 AsyncImage(
                                     model = shopState.logoUrl,
                                     contentDescription = "Logo",
                                     modifier = Modifier.fillMaxSize(),
                                     contentScale = ContentScale.Crop
                                 )
                             } else {
                                 Box(contentAlignment = Alignment.Center) {
                                     val initial = if (shopState.shopName.isNotEmpty()) shopState.shopName.first().toString() else "K"
                                     Text(initial, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                 }
                             }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = shopState.shopName.ifEmpty { "Đang tải..." },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Chủ cửa hàng",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ===== Sidebar Items =====
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {

                    DrawerItem(
                        text = "Dashboard", 
                        iconRes = R.drawable.ic_dashboard, 
                        isSelected = currentScreen == "dashboard"
                    ) {
                        currentScreen = "dashboard"
                        scope.launch { drawerState.close() }
                    }



                    DrawerItem(
                        text = "Quản lý đơn hàng", 
                        iconRes = R.drawable.ic_shopping_cart,
                        isSelected = currentScreen == "orders"
                    ) {
                        currentScreen = "orders"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Quản lý món ăn", 
                        iconRes = R.drawable.ic_restaurant,
                        isSelected = currentScreen == "foods"
                    ) {
                        currentScreen = "foods"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Quản lý Shipper", 
                        iconRes = R.drawable.ic_delivery,
                        isSelected = currentScreen == "shippers"
                    ) {
                        currentScreen = "shippers"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Quản lý Voucher", 
                        iconRes = R.drawable.ic_voucher,
                        isSelected = currentScreen == "vouchers"
                    ) {
                        currentScreen = "vouchers"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Khách hàng", 
                        iconRes = R.drawable.ic_customer,
                        isSelected = currentScreen == "customers"
                    ) {
                        currentScreen = "customers"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Báo cáo doanh thu", 
                        iconRes = R.drawable.ic_bar_chart,
                        isSelected = currentScreen == "revenue"
                    ) {
                        currentScreen = "revenue"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Đánh giá", 
                        iconRes = R.drawable.ic_review,
                        isSelected = currentScreen == "reviews"
                    ) {
                        currentScreen = "reviews"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Trợ lý AI", 
                        iconRes = R.drawable.ic_chat,
                        isSelected = currentScreen == "chatbot"
                    ) {
                        currentScreen = "chatbot"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Tin nhắn", 
                        iconRes = R.drawable.ic_chat,
                        isSelected = currentScreen == "chat"
                    ) {
                        currentScreen = "chat"
                        currentChatRoute = "conversations_list"
                        scope.launch { drawerState.close() }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))

                    DrawerItem(
                        text = "Cài đặt", 
                        iconRes = R.drawable.ic_settings,
                        isSelected = currentScreen == "settings"
                    ) {
                        currentScreen = "settings"
                        scope.launch { drawerState.close() }
                    }
                }

                // ===== Footer Version =====
                Text(
                    "Version 1.0.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) {

        // ===== Main Screen =====
        Scaffold(
            topBar = {
                // Show TopBar for screens that don't have their own TopBar
                val isInChatDetail = currentScreen == "chat" && currentChatRoute == "chat_detail"
                if (currentScreen != "dashboard" && currentScreen != "orders" && currentScreen != "foods" && currentScreen != "shippers" && currentScreen != "vouchers" && currentScreen != "customers" && currentScreen != "reviews" && currentScreen != "chatbot" && !isInChatDetail) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentScreen == "chat" && currentChatRoute == "conversations_list") {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = when (currentScreen) {
                                "revenue" -> "Báo cáo doanh thu"
                                "reviews" -> "Đánh giá"
                                "settings" -> "Cài đặt"
                                "chat" -> "Tin nhắn"
                                else -> shopState.shopName.ifEmpty { "KTX Food" }
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Notification Bell Icon
                        NotificationBell()
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(onMenuClick = { scope.launch { drawerState.open() } }) // Pass Open Drawer

                    "orders" -> OrdersScreen(onMenuClick = { scope.launch { drawerState.open() } })
                    "foods" -> FoodsScreen(onMenuClick = { scope.launch { drawerState.open() } })
                    "shippers" -> ShippersScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        shopId = shopState.shopId
                    )
                    "vouchers" -> VouchersScreen(onMenuClick = { scope.launch { drawerState.open() } })
                    "customers" -> CustomerScreenMain(onMenuClick = { scope.launch { drawerState.open() } }) 
                    "revenue" -> RevenueScreen()
                    "reviews" -> ReviewsScreen(
                        shopId = shopState.shopId,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                    "chatbot" -> ChatbotScreen(
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                    "chat" -> {
                        when (currentChatRoute) {
                            "chat_detail" -> OwnerChatDetailScreen(
                                conversationId = currentConversationId ?: "",
                                onBack = {
                                    currentChatRoute = "conversations_list"
                                    currentConversationId = null
                                }
                            )
                            else -> OwnerConversationsScreen(
                                onConversationClick = { conversationId ->
                                    currentConversationId = conversationId
                                    currentChatRoute = "chat_detail"
                                }
                            )
                        }
                    }
                    "settings" -> SettingsNavHost(
                        navController = settingsNavController,
                        onLogout = {
                            navController.navigate("intro") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    else -> DashboardScreen(onMenuClick = { scope.launch { drawerState.open() } })
                }
            }
        }
    }
}

@Composable
fun DrawerItem(text: String, iconRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Surface(
        color = backgroundColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight), color = contentColor)
        }
    }
}

// Placeholder screens
// SỬA LỖI: Xóa hàm DashboardScreen() placeholder ở đây đi vì đã import màn hình thật
