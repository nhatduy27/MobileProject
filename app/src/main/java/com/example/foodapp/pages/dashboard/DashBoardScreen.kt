package com.example.foodapp.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// LƯU Ý: Xóa dòng import R cũ nếu package của bạn khác
// import com.example.foodapp.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoardScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        // SỬA LỖI QUAN TRỌNG: Cần bọc nội dung trong ModalDrawerSheet
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    onClose = {
                        scope.launch { drawerState.close() }
                    },
                    onMenuClick = { menuItem ->
                        println("Click: $menuItem") // Log để kiểm tra
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        // Main content
        Scaffold( // Nên dùng Scaffold thay vì Column bọc ngoài để quản lý TopBar tốt hơn
            topBar = {
                // Top AppBar tùy chỉnh của bạn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFFFF6B35))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }

                    Text(
                        text = "KTX Food Dashboard",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            // Nội dung chính của màn hình
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding từ Scaffold để tránh bị che bởi TopBar
                    .background(Color(0xFFFDFDFD)),
                contentAlignment = Alignment.Center
            ) {
                Text("Dashboard Content", color = Color.Gray, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun DrawerContent(
    onClose: () -> Unit,
    onMenuClick: (String) -> Unit
) {
    // Không cần set width cứng 280.dp ở đây nữa vì ModalDrawerSheet đã lo việc đó
    // Nhưng nếu bạn muốn custom thì vẫn giữ được.
    Column(
        modifier = Modifier
            .fillMaxHeight()
            // .width(280.dp) // ModalDrawerSheet thường tự quản lý width (khoảng 360dp max)
            .background(MaterialTheme.colorScheme.surface)
    ) {

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFFFF6B35))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Nút đóng drawer
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // TRY-CATCH để tránh crash nếu ảnh không tồn tại
            // Thay R.mipmap.ic_launcher bằng icon có sẵn để test trước
            Icon(
                imageVector = Icons.Filled.Info, // Dùng tạm icon có sẵn để test tránh lỗi Resource
                contentDescription = "Logo",
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.White),
                tint = Color(0xFFFF6B35)
            )
            // Nếu chắc chắn có ảnh, dùng code cũ:
            /*
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                ...
            )
             */

            Text(
                text = "KTX Food Store",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            Text(
                text = "Chủ cửa hàng",
                color = Color(0xFFFFE5D9),
                fontSize = 13.sp
            )
        }

        // Menu Items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            DrawerItem("Dashboard", Icons.Filled.Info) { onMenuClick("dashboard") }
            DrawerItem("Quản lý đơn hàng", Icons.Filled.Menu) { onMenuClick("orders") }
            DrawerItem("Quản lý món ăn", Icons.Filled.Menu) { onMenuClick("foods") }
            DrawerItem("Quản lý Shipper", Icons.Filled.Menu) { onMenuClick("shippers") }
            DrawerItem("Khách hàng", Icons.Filled.Menu) { onMenuClick("customers") }
            DrawerItem("Báo cáo doanh thu", Icons.Filled.Menu) { onMenuClick("revenue") }

            // Material 3 dùng HorizontalDivider
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            DrawerItem("Cài đặt", Icons.Filled.Menu) { onMenuClick("settings") }
        }

        Text(
            text = "Version 1.0.0",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = title) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}