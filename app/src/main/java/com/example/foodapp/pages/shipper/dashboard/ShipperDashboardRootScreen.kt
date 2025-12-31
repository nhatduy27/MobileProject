package com.example.foodapp.pages.shipper.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController

@Composable
fun DrawerMenuItem(
    icon: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color(0xFFFFF3E0) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFF6B35) else Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperDashboardRootScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("home") }
    
    // NavController riêng cho settings navigation
    val settingsNavController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF6B35))
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N", // Replace with user initial if available
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B35)
                            )
                        }
                        Text(
                            text = "Nguyễn Văn A", // Replace with user name
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Shipper",
                            fontSize = 14.sp,
                            color = Color(0xFFFFE5D9),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Menu Items for Shipper
                DrawerMenuItem(
                    icon = "🏠",
                    title = "Trang chủ",
                    isSelected = currentScreen == "home",
                    onClick = {
                        currentScreen = "home"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = "💰",
                    title = "Thu nhập",
                    isSelected = currentScreen == "earnings",
                    onClick = {
                        currentScreen = "earnings"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = "📜",
                    title = "Lịch sử",
                    isSelected = currentScreen == "history",
                    onClick = {
                        currentScreen = "history"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = "🔔",
                    title = "Thông báo",
                    isSelected = currentScreen == "notifications",
                    onClick = {
                        currentScreen = "notifications"
                        scope.launch { drawerState.close() }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE0E0E0)
                )
                DrawerMenuItem(
                    icon = "👤",
                    title = "Hồ sơ",
                    isSelected = currentScreen == "profile",
                    onClick = {
                        currentScreen = "profile"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = "❓",
                    title = "Trợ giúp",
                    isSelected = currentScreen == "help",
                    onClick = {
                        currentScreen = "help"
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = "🚪",
                    title = "Đăng xuất",
                    isSelected = false,
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )

                // Version text at the bottom
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Version 1.0.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentScreen) {
                                    "home" -> "Trang chủ"
                                    "earnings" -> "Thu nhập của tôi"
                                    "history" -> "Lịch sử giao hàng"
                                    "profile" -> "Hồ sơ"
                                    "notifications" -> "Thông báo"
                                    "help" -> "Trợ giúp & Hỗ trợ"
                                    else -> "FoodApp Shipper"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                currentScreen = "notifications"
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Thông báo"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFFF6B35),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentScreen) {
                        "home" -> ShipperHomeScreen()
                        "earnings" -> EarningsScreen()
                        "history" -> HistoryScreen()
                        "profile" -> ShipperSettingsNavHost(navController = settingsNavController)
                        "notifications" -> NotificationsScreen()
                        "help" -> HelpScreen()
                    }
                }
            }
        }
    )
}
