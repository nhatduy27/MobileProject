package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider

@Composable
fun SettingsScreen(navController: NavHostController) {
    val sections = listOf(
        SettingSection(
            title = "TÀI KHOẢN",
            items = listOf(
                SettingItem(
                    title = "Thông tin cá nhân",
                    subtitle = "Chỉnh sửa thông tin tài khoản",
                    icon = androidx.compose.material.icons.Icons.Outlined.Person,
                    onClick = { navController.navigate("personal_info") }
                ),
                SettingItem(
                    title = "Đổi mật khẩu",
                    subtitle = "Thay đổi mật khẩu đăng nhập",
                    icon = androidx.compose.material.icons.Icons.Outlined.Lock,
                    onClick = { navController.navigate("change_password") }
                )
            )
        ),
        SettingSection(
            title = "CỬA HÀNG",
            items = listOf(
                SettingItem(
                    title = "Thông tin cửa hàng",
                    subtitle = "Tên, địa chỉ, giờ mở cửa",
                    icon = androidx.compose.material.icons.Icons.Outlined.Store,
                    onClick = { navController.navigate("store_info") }
                ),
                SettingItem(
                    title = "Phương thức thanh toán",
                    subtitle = "Quản lý tài khoản ngân hàng",
                    icon = androidx.compose.material.icons.Icons.Outlined.CreditCard,
                    onClick = { navController.navigate("payment_method") }
                )
            )
        ),
        SettingSection(
            title = "THÔNG BÁO",
            items = listOf(
                SettingItem(
                    title = "Đơn hàng mới",
                    subtitle = "Nhận thông báo khi có đơn mới",
                    icon = androidx.compose.material.icons.Icons.Outlined.Notifications,
                    hasSwitch = true,
                    isEnabled = true
                ),
                SettingItem(
                    title = "Cập nhật đơn hàng",
                    subtitle = "Thông báo trạng thái đơn hàng",
                    // Inventory2 might be missing, using List or a generic Box
                    icon = androidx.compose.material.icons.Icons.Default.List,
                    hasSwitch = true,
                    isEnabled = true
                ),
                SettingItem(
                    title = "Khuyến mãi",
                    subtitle = "Nhận thông báo ưu đãi",
                    icon = androidx.compose.material.icons.Icons.Outlined.CardGiftcard,
                    hasSwitch = true,
                    isEnabled = false
                )
            )
        ),
        SettingSection(
            title = "BẢO MẬT",
            items = listOf(
                SettingItem(
                    title = "Xác thực 2 bước",
                    subtitle = "Tăng cường bảo mật tài khoản",
                    icon = androidx.compose.material.icons.Icons.Outlined.Security,
                    hasSwitch = true,
                    isEnabled = false
                ),
                SettingItem(
                    title = "Lịch sử đăng nhập",
                    subtitle = "Xem các phiên đăng nhập gần đây",
                    icon = androidx.compose.material.icons.Icons.Outlined.History,
                    onClick = { navController.navigate("login_history") }
                )
            )
        ),
        SettingSection(
            title = "VỀ ỨNG DỤNG",
            items = listOf(
                SettingItem(
                    title = "Điều khoản sử dụng",
                    subtitle = "Quy định và chính sách",
                     // AutoMirrored.Outlined.List might be tricky, using Default.List for safety
                    icon = androidx.compose.material.icons.Icons.Default.List,
                    onClick = { navController.navigate("terms") }
                ),
                SettingItem(
                    title = "Chính sách bảo mật",
                    subtitle = "Cách chúng tôi bảo vệ dữ liệu",
                    icon = androidx.compose.material.icons.Icons.Outlined.PrivacyTip,
                    onClick = { navController.navigate("privacy") }
                ),
                SettingItem(
                    title = "Trợ giúp & Hỗ trợ",
                    subtitle = "Liên hệ với chúng tôi",
                    icon = androidx.compose.material.icons.Icons.Outlined.SupportAgent,
                    onClick = { navController.navigate("support") }
                )
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp) // Wider padding
                .padding(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            sections.forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = section.title, 
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Card chứa các items
                    androidx.compose.material3.Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                         elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                         Column {
                             section.items.forEachIndexed { index, item ->
                                 SettingItemCard(
                                     item = item,
                                     onSwitchChanged = { enabled ->
                                         println("${item.title} switched to $enabled")
                                     }
                                 )
                                 if (index < section.items.size - 1) {
                                     HorizontalDivider(
                                         color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), 
                                         thickness = 1.dp,
                                         modifier = Modifier.padding(horizontal = 16.dp)
                                     )
                                 }
                             }
                         }
                    }
                }
            }

            // Nút đăng xuất
            Button(
                onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Đăng xuất", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Version Info
            Text(
                text = "KTX Food Store v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
