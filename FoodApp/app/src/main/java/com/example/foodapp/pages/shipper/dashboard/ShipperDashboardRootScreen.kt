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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodapp.pages.shipper.earnings.EarningsScreen
import com.example.foodapp.pages.shipper.help.HelpScreen
import com.example.foodapp.pages.shipper.history.HistoryScreen
import com.example.foodapp.pages.shipper.home.ShipperHomeScreen
import com.example.foodapp.pages.shipper.notifications.NotificationsScreen
import com.example.foodapp.pages.shipper.settings.ShipperSettingsNavHost
import com.example.foodapp.pages.shipper.theme.ShipperColors
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
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = ShipperColors.PrimaryLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "N",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShipperColors.Primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "Nguyễn Văn A",
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
                    icon = Icons.Outlined.Description,
                    title = "Đơn ứng tuyển",
                    isSelected = currentScreen == "applications",
                    onClick = {
                        currentScreen = "applications"
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
                                    currentScreen == "home" -> "Trang chủ"
                                    currentScreen == "earnings" -> "Thu nhập của tôi"
                                    currentScreen == "history" -> "Lịch sử giao hàng"
                                    currentScreen == "applications" -> "Đơn ứng tuyển"
                                    currentScreen == "settings" -> "Cài đặt"
                                    currentScreen == "notifications" -> "Thông báo"
                                    currentScreen == "help" -> "Trợ giúp & Hỗ trợ"
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
                            if (!isInSettingsChild) {
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
                    }
                }
            }
        }
    )
}
