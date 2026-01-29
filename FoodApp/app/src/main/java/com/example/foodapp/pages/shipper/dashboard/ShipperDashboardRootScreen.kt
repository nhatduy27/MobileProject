package com.example.foodapp.pages.shipper.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.user.UserProfile
import com.example.foodapp.pages.shipper.earnings.EarningsScreen
import com.example.foodapp.pages.shipper.help.HelpScreen
import com.example.foodapp.pages.shipper.history.HistoryScreen
import com.example.foodapp.pages.shipper.home.ShipperHomeScreen
import com.example.foodapp.pages.shipper.notifications.NotificationsScreen
import com.example.foodapp.pages.shipper.settings.ShipperSettingsNavHost
import com.example.foodapp.pages.shipper.theme.ShipperColors
import com.example.foodapp.pages.shipper.gps.GpsScreen
import com.example.foodapp.pages.shipper.gps.TripDetailScreen
import com.example.foodapp.pages.shipper.gps.TripHistoryScreen
import com.example.foodapp.pages.shipper.gps.DeliveryMapScreen
import com.example.foodapp.pages.shipper.removal.RemovalRequestScreen
import com.example.foodapp.pages.shipper.chatbot.ShipperChatbotScreen
import com.example.foodapp.pages.shipper.chat.ConversationsScreen
import com.example.foodapp.pages.shipper.chat.ChatDetailScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) ShipperColors.PrimaryLight else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (isSelected) ShipperColors.Primary else ShipperColors.TextSecondary
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) ShipperColors.Primary else ShipperColors.TextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperDashboardRootScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("home") }
    var currentSettingsRoute by remember { mutableStateOf("settings_main") }
    var currentGpsRoute by remember { mutableStateOf("gps_main") }
    var currentTripId by remember { mutableStateOf<String?>(null) }
    var currentChatRoute by remember { mutableStateOf("conversations_list") }
    var currentConversationId by remember { mutableStateOf<String?>(null) }
    
    // User profile state
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    val repository = remember { RepositoryProvider.getUserProfileRepository() }
    
    // Fetch user profile on launch
    LaunchedEffect(Unit) {
        val result = repository.getProfile()
        result.onSuccess { profile ->
            userProfile = profile
        }
    }
    
    val settingsNavController = rememberNavController()
    
    // Listen to settings navigation changes
    LaunchedEffect(settingsNavController) {
        settingsNavController.currentBackStackEntryFlow.collect { entry ->
            currentSettingsRoute = entry.destination.route ?: "settings_main"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ShipperColors.Surface,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header - Clean white style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ShipperColors.Surface)
                        .padding(24.dp)
                ) {
                    Column {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ShipperColors.PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarUrl = userProfile?.avatarUrl
                            if (!avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = userProfile?.displayName?.firstOrNull()?.uppercase() ?: "S",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShipperColors.Primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = userProfile?.displayName ?: "Shipper",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ShipperColors.TextPrimary
                        )
                        Text(
                            text = "Shipper",
                            fontSize = 13.sp,
                            color = ShipperColors.TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                HorizontalDivider(color = ShipperColors.Divider, thickness = 1.dp)

                Spacer(modifier = Modifier.height(8.dp))

                // Menu Items
                DrawerMenuItem(
                    icon = Icons.Outlined.Home,
                    title = "Trang chủ",
                    isSelected = currentScreen == "home",
                    onClick = {
                        currentScreen = "home"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "Thu nhập",
                    isSelected = currentScreen == "earnings",
                    onClick = {
                        currentScreen = "earnings"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.History,
                    title = "Lịch sử giao hàng",
                    isSelected = currentScreen == "history",
                    onClick = {
                        currentScreen = "history"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Thông báo",
                    isSelected = currentScreen == "notifications",
                    onClick = {
                        currentScreen = "notifications"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.Chat,
                    title = "Tin nhắn",
                    isSelected = currentScreen == "chat",
                    onClick = {
                        currentScreen = "chat"
                        currentChatRoute = "conversations_list"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.Route,
                    title = "Lộ trình giao hàng",
                    isSelected = currentScreen == "gps",
                    onClick = {
                        currentScreen = "gps"
                        currentGpsRoute = "gps_main"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.Description,
                    title = "Đơn ứng tuyển",
                    isSelected = currentScreen == "applications",
                    onClick = {
                        currentScreen = "applications"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.ExitToApp,
                    title = "Yêu cầu rời shop",
                    isSelected = currentScreen == "removal_requests",
                    onClick = {
                        currentScreen = "removal_requests"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "Cài đặt",
                    isSelected = currentScreen == "settings",
                    onClick = {
                        currentScreen = "settings"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Trợ giúp",
                    isSelected = currentScreen == "help",
                    onClick = {
                        currentScreen = "help"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Outlined.SmartToy,
                    title = "Trợ lý AI",
                    isSelected = currentScreen == "chatbot",
                    onClick = {
                        currentScreen = "chatbot"
                        scope.launch { drawerState.close() }
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(color = ShipperColors.Divider, thickness = 1.dp)
                
                // Logout
                DrawerMenuItem(
                    icon = Icons.Outlined.Logout,
                    title = "Đăng xuất",
                    isSelected = false,
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("intro") {
                            popUpTo(0)
                        }
                    }
                )

                Text(
                    "Version 1.0.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = ShipperColors.TextTertiary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        content = {
            Scaffold(
                topBar = {
                    val isInSettingsChild = currentScreen == "settings" && currentSettingsRoute != "settings_main"
                    val isInGpsChild = currentScreen == "gps" && currentGpsRoute != "gps_main"
                    val isInChatChild = currentScreen == "chat" && currentChatRoute == "chat_detail"
                    TopAppBar(
                        title = {
                            Text(
                                text = when {
                                    currentScreen == "settings" && currentSettingsRoute != "settings_main" -> {
                                        when (currentSettingsRoute) {
                                            "edit_profile" -> "Thông tin cá nhân"
                                            "change_password" -> "Đổi mật khẩu"
                                            "vehicle_info" -> "Thông tin phương tiện"
                                            "payment_method" -> "Phương thức thanh toán"
                                            "notification_settings" -> "Cài đặt thông báo"
                                            "language" -> "Ngôn ngữ"
                                            "terms" -> "Điều khoản & Chính sách"
                                            "privacy" -> "Bảo mật & Quyền riêng tư"
                                            "help_screen" -> "Trợ giúp & Hỗ trợ"
                                            else -> "Cài đặt"
                                        }
                                    }
                                    currentScreen == "gps" && currentGpsRoute != "gps_main" -> {
                                        when (currentGpsRoute) {
                                            "trip_detail" -> "Chi tiết lộ trình"
                                            "trip_history" -> "Lịch sử chuyến đi"
                                            "delivery_map" -> "Bản đồ giao hàng"
                                            "delivery_map_from_home" -> "Bản đồ giao hàng"
                                            else -> "Lộ trình giao hàng"
                                        }
                                    }
                                    currentScreen == "chat" && currentChatRoute == "chat_detail" -> "Chi tiết tin nhắn"
                                    currentScreen == "home" -> "Trang chủ"
                                    currentScreen == "earnings" -> "Thu nhập của tôi"
                                    currentScreen == "history" -> "Lịch sử giao hàng"
                                    currentScreen == "applications" -> "Đơn ứng tuyển"
                                    currentScreen == "removal_requests" -> "Yêu cầu rời shop"
                                    currentScreen == "gps" -> "Lộ trình giao hàng"
                                    currentScreen == "settings" -> "Cài đặt"
                                    currentScreen == "notifications" -> "Thông báo"
                                    currentScreen == "chat" -> "Tin nhắn"
                                    currentScreen == "help" -> "Trợ giúp & Hỗ trợ"
                                    currentScreen == "chatbot" -> "Trợ lý AI"
                                    else -> "FoodApp Shipper"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        },
                        navigationIcon = {
                            if (isInSettingsChild) {
                                IconButton(onClick = { settingsNavController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowBack,
                                        contentDescription = "Quay lại",
                                        tint = ShipperColors.TextPrimary
                                    )
                                }
                            } else if (isInGpsChild) {
                                IconButton(onClick = { 
                                    if (currentGpsRoute == "delivery_map_from_home") {
                                        currentScreen = "home"
                                        currentGpsRoute = "gps_main"
                                        currentTripId = null
                                    } else {
                                        currentGpsRoute = "gps_main"
                                        currentTripId = null
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowBack,
                                        contentDescription = "Quay lại",
                                        tint = ShipperColors.TextPrimary
                                    )
                                }
                            } else if (isInChatChild) {
                                IconButton(onClick = { 
                                    currentChatRoute = "conversations_list"
                                    currentConversationId = null
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowBack,
                                        contentDescription = "Quay lại",
                                        tint = ShipperColors.TextPrimary
                                    )
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = ShipperColors.TextPrimary
                                    )
                                }
                            }
                        },
                        actions = {
                            if (!isInSettingsChild && !isInGpsChild && !isInChatChild) {
                                IconButton(onClick = {
                                    currentScreen = "notifications"
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Thông báo",
                                        tint = ShipperColors.TextPrimary
                                    )
                                }
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
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentScreen) {
                        "home" -> ShipperHomeScreen(
                            onOrderClick = { orderId ->
                                navController.navigate("shipper_order_detail/$orderId")
                            },
                            onViewMap = { orderId ->
                                currentTripId = orderId
                                currentGpsRoute = "delivery_map_from_home"
                                currentScreen = "gps"
                            },
                            onApplyShipper = {
                                navController.navigate("shipper_apply")
                            }
                        )
                        "earnings" -> EarningsScreen()
                        "history" -> HistoryScreen()
                        "applications" -> com.example.foodapp.pages.shipper.application.MyApplicationsScreen(
                            onBack = { currentScreen = "home" },
                            showTopBar = false
                        )
                        "settings" -> ShipperSettingsNavHost(
                            navController = settingsNavController,
                            onLogout = {
                                navController.navigate("intro") {
                                    popUpTo(0)
                                }
                            }
                        )
                        "notifications" -> NotificationsScreen()
                        "help" -> HelpScreen()
                        "removal_requests" -> RemovalRequestScreen(
                            onBack = { currentScreen = "home" },
                            showTopBar = false
                        )
                        "chatbot" -> ShipperChatbotScreen()
                        "chat" -> {
                            when (currentChatRoute) {
                                "chat_detail" -> ChatDetailScreen(
                                    conversationId = currentConversationId ?: "",
                                    onBack = {
                                        currentChatRoute = "conversations_list"
                                        currentConversationId = null
                                    }
                                )
                                else -> ConversationsScreen(
                                    onConversationClick = { conversationId ->
                                        currentConversationId = conversationId
                                        currentChatRoute = "chat_detail"
                                    }
                                )
                            }
                        }
                        "gps" -> {
                            when (currentGpsRoute) {
                                "trip_detail" -> TripDetailScreen(
                                    tripId = currentTripId ?: "",
                                    onBack = {
                                        currentGpsRoute = "gps_main"
                                        currentTripId = null
                                    },
                                    onNavigateToMap = { tripId ->
                                        currentTripId = tripId
                                        currentGpsRoute = "delivery_map"
                                    }
                                )
                                "delivery_map" -> DeliveryMapScreen(
                                    tripId = currentTripId ?: "",
                                    onBack = {
                                        currentGpsRoute = "trip_detail"
                                    },
                                    onFinish = {
                                        currentGpsRoute = "gps_main"
                                        currentTripId = null
                                    }
                                )
                                "delivery_map_from_home" -> DeliveryMapScreen(
                                    tripId = currentTripId ?: "",
                                    isOrderId = true,  // When coming from home, we pass orderId
                                    onBack = {
                                        currentScreen = "home"
                                        currentGpsRoute = "gps_main"
                                        currentTripId = null
                                    },
                                    onFinish = {
                                        currentScreen = "home"
                                        currentGpsRoute = "gps_main"
                                        currentTripId = null
                                    }
                                )
                                "trip_history" -> TripHistoryScreen(
                                    onBack = { currentGpsRoute = "gps_main" },
                                    onNavigateToTripDetail = { tripId ->
                                        currentTripId = tripId
                                        currentGpsRoute = "trip_detail"
                                    }
                                )
                                else -> GpsScreen(
                                    onNavigateToTripDetail = { tripId ->
                                        currentTripId = tripId
                                        currentGpsRoute = "trip_detail"
                                    },
                                    onNavigateToTripHistory = {
                                        currentGpsRoute = "trip_history"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
