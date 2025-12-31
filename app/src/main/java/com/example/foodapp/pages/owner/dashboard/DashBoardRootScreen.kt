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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
// SỬA LỖI: Thêm import cho CustomerScreen
import com.example.foodapp.pages.owner.customer.CustomerScreen
// SỬA LỖI: Import DashboardScreen thật sự, không phải placeholder
import com.example.foodapp.pages.owner.dashboard.DashboardScreen
// Import FoodsScreen
import com.example.foodapp.pages.owner.foods.FoodsScreen
// Import OrdersScreen
import com.example.foodapp.pages.owner.orders.OrdersScreen
// Import RevenueScreen
import com.example.foodapp.pages.owner.revenue.RevenueScreen
// Import ShippersScreen
import com.example.foodapp.pages.owner.shippers.ShippersScreen
// Import SettingsScreen
import com.example.foodapp.pages.owner.settings.SettingsNavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodapp.pages.owner.customer.CustomerScreenMain

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
                        .height(240.dp)
                        .background(Color(0xFFFF6B35))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.End)
                            .offset(y = (-16).dp)
                            .size(32.dp)
                            .clickable { scope.launch { drawerState.close() } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "KTX Food Store",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "Chủ cửa hàng",
                        color = Color(0xFFFFE5D9),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // ===== Sidebar Items =====
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {

                    DrawerItem("Dashboard", R.drawable.ic_dashboard) {
                        currentScreen = "dashboard"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem("Quản lý đơn hàng", R.drawable.ic_shopping_cart) {
                        currentScreen = "orders"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem("Quản lý món ăn", R.drawable.ic_restaurant) {
                        currentScreen = "foods"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem("Quản lý Shipper", R.drawable.ic_delivery) {
                        currentScreen = "shippers"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem("Khách hàng", R.drawable.ic_customer) {
                        currentScreen = "customers"
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem("Báo cáo doanh thu", R.drawable.ic_bar_chart) {
                        currentScreen = "revenue"
                        scope.launch { drawerState.close() }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    DrawerItem("Cài đặt", R.drawable.ic_settings) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFFFF6B35))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = Color.White)
                    }

                    Text(
                        text = when (currentScreen) {
                            "dashboard" -> "KTX Food Dashboard"
                            "orders" -> "Quản lý đơn hàng"
                            "foods" -> "Quản lý món ăn"
                            "shippers" -> "Quản lý Shipper"
                            "customers" -> "Quản lý khách hàng"
                            "revenue" -> "Báo cáo doanh thu"
                            "settings" -> "Cài đặt"
                            else -> "KTX Food Dashboard"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
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
                    "dashboard" -> DashboardScreen()
                    "orders" -> OrdersScreen()
                    "foods" -> FoodsScreen()
                    "shippers" -> ShippersScreen()
                    "customers" -> CustomerScreenMain()
                    "revenue" -> RevenueScreen()
                    "settings" -> SettingsNavHost(navController = settingsNavController)
                    else -> DashboardScreen()
                }
            }
        }
    }
}

@Composable
fun DrawerItem(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF333333))
    }
}

// Placeholder screens
// SỬA LỖI: Xóa hàm DashboardScreen() placeholder ở đây đi vì đã import màn hình thật
