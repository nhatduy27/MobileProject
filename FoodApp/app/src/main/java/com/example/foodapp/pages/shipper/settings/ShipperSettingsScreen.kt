package com.example.foodapp.pages.shipper.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ShipperSettingsScreen(
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ShipperSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val mainColor = Color(0xFFFF6B35)
    
    // Show toast for messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    
    // Show toast for errors  
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Online Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isOnline) Color(0xFFE8F5E9) else Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (uiState.isOnline) Color(0xFF4CAF50) else Color.Gray
                    )
                    Text(
                        "TRẠNG THÁI HOẠT ĐỘNG", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp, 
                        color = mainColor
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            if (uiState.isOnline) "Đang hoạt động" else "Ngoại tuyến",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (uiState.isOnline) Color(0xFF4CAF50) else Color.Gray
                        )
                        Text(
                            if (uiState.isOnline) "Bạn sẽ nhận thông báo đơn hàng mới" 
                            else "Bật để nhận đơn hàng mới",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    if (uiState.isTogglingOnline) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = mainColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Switch(
                            checked = uiState.isOnline, 
                            onCheckedChange = { viewModel.toggleOnlineStatus() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }
        
        // Account Section
        SettingSection(
            title = "TÀI KHOẢN",
            items = listOf(
                SettingItemData(
                    title = "Thông tin cá nhân",
                    subtitle = "Chỉnh sửa thông tin tài khoản",
                    icon = Icons.Outlined.Person,
                    onClick = { onNavigate("edit_profile") }
                ),
                SettingItemData(
                    title = "Đổi mật khẩu",
                    subtitle = "Thay đổi mật khẩu đăng nhập",
                    icon = Icons.Outlined.Lock,
                    onClick = { onNavigate("change_password") }
                )
            ),
            mainColor = mainColor
        )
        
        // Shipper Info Section
        SettingSection(
            title = "THÔNG TIN SHIPPER",
            items = listOf(
                SettingItemData(
                    title = "Phương tiện",
                    subtitle = "Thông tin xe và biển số",
                    icon = Icons.Outlined.DirectionsBike,
                    onClick = { onNavigate("vehicle_info") }
                ),
                SettingItemData(
                    title = "Phương thức thanh toán",
                    subtitle = "Tài khoản nhận tiền",
                    icon = Icons.Outlined.CreditCard,
                    onClick = { onNavigate("payment_method") }
                )
            ),
            mainColor = mainColor
        )
        
        // Notification Section
        SettingSection(
            title = "THÔNG BÁO",
            items = listOf(
                SettingItemData(
                    title = "Cài đặt thông báo",
                    subtitle = "Quản lý thông báo đẩy",
                    icon = Icons.Outlined.Notifications,
                    onClick = { onNavigate("notification_settings") }
                )
            ),
            mainColor = mainColor
        )
        
        // About Section
        SettingSection(
            title = "VỀ ỨNG DỤNG",
            items = listOf(
                SettingItemData(
                    title = "Điều khoản sử dụng",
                    subtitle = "Quy định và chính sách",
                    icon = Icons.Default.List,
                    onClick = { onNavigate("terms") }
                ),
                SettingItemData(
                    title = "Chính sách bảo mật",
                    subtitle = "Cách chúng tôi bảo vệ dữ liệu",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { onNavigate("privacy") }
                ),
                SettingItemData(
                    title = "Trợ giúp & Hỗ trợ",
                    subtitle = "Liên hệ với chúng tôi",
                    icon = Icons.Outlined.SupportAgent,
                    onClick = { onNavigate("help_screen") }
                )
            ),
            mainColor = mainColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Logout Button
        Button(
            onClick = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEBEE)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Đăng xuất", 
                color = Color(0xFFF44336), 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        // Version Info
        Text(
            text = "FoodApp Shipper v1.0.0",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun SettingSection(
    title: String,
    items: List<SettingItemData>,
    mainColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = mainColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItemRow(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFEEEEEE)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItemRow(item: SettingItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF3E0),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Arrow
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(24.dp)
        )
    }
}

private data class SettingItemData(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)
