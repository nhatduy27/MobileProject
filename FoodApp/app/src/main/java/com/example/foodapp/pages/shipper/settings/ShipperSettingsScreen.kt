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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun ShipperSettingsScreen(
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ShipperSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account Section
        SettingSection(
            title = "Tài khoản",
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
            )
        )
        
        // Shipper Info Section
        SettingSection(
            title = "Thông tin shipper",
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
                    icon = Icons.Outlined.AccountBalanceWallet,
                    onClick = { onNavigate("payment_method") }
                )
            )
        )
        
        // Notification Section
        SettingSection(
            title = "Thông báo",
            items = listOf(
                SettingItemData(
                    title = "Cài đặt thông báo",
                    subtitle = "Quản lý thông báo đẩy",
                    icon = Icons.Outlined.Notifications,
                    onClick = { onNavigate("notification_settings") }
                )
            )
        )
        
        // About Section
        SettingSection(
            title = "Về ứng dụng",
            items = listOf(
                SettingItemData(
                    title = "Điều khoản sử dụng",
                    subtitle = "Quy định và chính sách",
                    icon = Icons.Outlined.Description,
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
                    icon = Icons.Outlined.HelpOutline,
                    onClick = { onNavigate("help_screen") }
                )
            )
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
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ShipperColors.ErrorLight
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "Đăng xuất", 
                color = ShipperColors.Error, 
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
        
        // Version Info
        Text(
            text = "FoodApp Shipper v1.0.0",
            fontSize = 12.sp,
            color = ShipperColors.TextTertiary,
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
    items: List<SettingItemData>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShipperColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItemRow(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = ShipperColors.Divider
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
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = ShipperColors.PrimaryLight,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = ShipperColors.Primary,
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
                color = ShipperColors.TextPrimary
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary
                )
            }
        }
        
        // Arrow
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ShipperColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private data class SettingItemData(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)
