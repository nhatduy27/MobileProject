package com.example.foodapp.shipper.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.shipper.earnings.EarningsScreen
import com.example.foodapp.shipper.help.HelpScreen
import com.example.foodapp.shipper.history.HistoryScreen
import com.example.foodapp.shipper.home.ShipperHomeScreen
import com.example.foodapp.shipper.notifications.NotificationsScreen
import com.example.foodapp.shipper.profile.ProfileScreen
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

// SỬA LỖI: Thêm Annotation này để bỏ qua cảnh báo API thử nghiệm
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperDashboardRootScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("home") }

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
                                text = "N",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B35)
                            )
                        }
                        Text(
                            text = "Nguyễn Văn A",
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

                // Menu Items
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
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    ModalDrawerSheet(
                                        drawerContainerColor = Color.White
                                    ) {
                                        // ===== Sidebar Header =====
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(240.dp)
                                                .background(Color(0xFFFF6B35))
                                                .padding(20.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .background(Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "N",
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF6B35)
                                                )
                                            }
                                            Text(
                                                text = "Nguyễn Văn A",
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

                                        // ===== Sidebar Items =====
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .verticalScroll(rememberScrollState())
                                                .padding(horizontal = 12.dp, vertical = 16.dp)
                                        ) {
                                            DrawerMenuItem(icon = "🏠", title = "Trang chủ", isSelected = currentScreen == "home") {
                                                currentScreen = "home"
                                                scope.launch { drawerState.close() }
                                            }
                                            DrawerMenuItem(icon = "💰", title = "Thu nhập", isSelected = currentScreen == "earnings") {
                                                currentScreen = "earnings"
                                                scope.launch { drawerState.close() }
                                            }
                                            DrawerMenuItem(icon = "📦", title = "Lịch sử giao hàng", isSelected = currentScreen == "history") {
                                                currentScreen = "history"
                                                scope.launch { drawerState.close() }
                                            }
                                            DrawerMenuItem(icon = "👤", title = "Hồ sơ", isSelected = currentScreen == "profile") {
                                                currentScreen = "profile"
                                                scope.launch { drawerState.close() }
                                            }
                                            DrawerMenuItem(icon = "🔔", title = "Thông báo", isSelected = currentScreen == "notifications") {
                                                currentScreen = "notifications"
                                                scope.launch { drawerState.close() }
                                            }
                                            DrawerMenuItem(icon = "❓", title = "Trợ giúp & Hỗ trợ", isSelected = currentScreen == "help") {
                                                currentScreen = "help"
                                                scope.launch { drawerState.close() }
                                            }
                                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                                            DrawerMenuItem(icon = "🚪", title = "Đăng xuất", isSelected = false) {
                                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                                navController.navigate("login") {
                                                    popUpTo(0)
                                                }
                                            }
                                        }

                                        // ===== Footer Version =====
                                        Text(
                                            "Version 1.0.0",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            color = Color(0xFF999999),
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                },
