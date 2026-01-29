package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.owner.notifications.NotificationsViewModel

@Composable
fun SettingsScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    notificationViewModel: NotificationsViewModel = viewModel()
) {
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val preferences = notificationUiState.preferences

    // Load preferences on first composition
    androidx.compose.runtime.LaunchedEffect(Unit) {
        notificationViewModel.loadPreferences()
    }

    val accountItems = listOf(
        SettingItem(
            title = "Thông tin cá nhân",
            subtitle = "Chỉnh sửa thông tin tài khoản",
            icon = Icons.Outlined.Person,
            onClick = { navController.navigate("personal_info") }
        ),
        SettingItem(
            title = "Đổi mật khẩu",
            subtitle = "Thay đổi mật khẩu đăng nhập",
            icon = Icons.Outlined.Lock,
            onClick = { navController.navigate("change_password") }
        )
    )

    val storeItems = listOf(
        SettingItem(
            title = "Thông tin cửa hàng",
            subtitle = "Tên, địa chỉ, giờ mở cửa",
            icon = Icons.Outlined.Store,
            onClick = { navController.navigate("store_info") }
        ),
        SettingItem(
            title = "Phương thức thanh toán",
            subtitle = "Quản lý tài khoản ngân hàng",
            icon = Icons.Outlined.CreditCard,
            onClick = { navController.navigate("payment_method") }
        )
    )

    val securityItems = listOf(
        SettingItem(
            title = "Xác thực 2 bước",
            subtitle = "Tăng cường bảo mật tài khoản",
            icon = Icons.Outlined.Security,
            hasSwitch = true,
            isEnabled = false,
            isDisabled = true // Tính năng chưa phát triển
        ),
        SettingItem(
            title = "Lịch sử đăng nhập",
            subtitle = "Xem các phiên đăng nhập gần đây",
            icon = Icons.Outlined.History,
            onClick = { navController.navigate("login_history") },
            isDisabled = true // Tính năng chưa phát triển
        )
    )

    val aboutItems = listOf(
        SettingItem(
            title = "Điều khoản sử dụng",
            subtitle = "Quy định và chính sách",
            icon = Icons.Default.List,
            onClick = { navController.navigate("terms") }
        ),
        SettingItem(
            title = "Chính sách bảo mật",
            subtitle = "Cách chúng tôi bảo vệ dữ liệu",
            icon = Icons.Outlined.PrivacyTip,
            onClick = { navController.navigate("privacy") }
        ),
        SettingItem(
            title = "Trợ giúp & Hỗ trợ",
            subtitle = "Liên hệ với chúng tôi",
            icon = Icons.Outlined.SupportAgent,
            onClick = { navController.navigate("support") }
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
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Tài khoản
            SettingSectionCard(title = "TÀI KHOẢN", items = accountItems)
            
            // Cửa hàng
            SettingSectionCard(title = "CỬA HÀNG", items = storeItems)
            
            // Thông báo - with API integration
            NotificationSettingsSection(
                preferences = preferences,
                onTransactionalChanged = { /* Không thể tắt */ },
                onInformationalChanged = { enabled ->
                    notificationViewModel.updatePreferences(
                        informational = enabled,
                        marketing = preferences?.marketing
                    )
                },
                onMarketingChanged = { enabled ->
                    notificationViewModel.updatePreferences(
                        informational = preferences?.informational,
                        marketing = enabled
                    )
                }
            )
            
            // Bảo mật
            SettingSectionCard(title = "BẢO MẬT", items = securityItems)
            
            // Về ứng dụng
            SettingSectionCard(title = "VỀ ỨNG DỤNG", items = aboutItems)

            // Nút đăng xuất
            Button(
                onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onLogout()
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

@Composable
private fun SettingSectionCard(
    title: String,
    items: List<SettingItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.material3.Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItemCard(
                        item = item,
                        onSwitchChanged = { enabled ->
                            println("${item.title} switched to $enabled")
                        }
                    )
                    if (index < items.size - 1) {
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

/**
 * Section cho Notification Settings - kết nối với API
 */
@Composable
private fun NotificationSettingsSection(
    preferences: com.example.foodapp.data.model.owner.notification.NotificationPreferences?,
    onTransactionalChanged: (Boolean) -> Unit,
    onInformationalChanged: (Boolean) -> Unit,
    onMarketingChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "THÔNG BÁO",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.material3.Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                // Đơn hàng & Thanh toán - Luôn bật, không thể tắt
                NotificationToggleItem(
                    title = "Đơn hàng & Thanh toán",
                    subtitle = "Thông báo quan trọng, không thể tắt",
                    icon = Icons.Default.ShoppingCart,
                    checked = true,
                    enabled = false,
                    onCheckedChange = onTransactionalChanged
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Cập nhật & Tổng kết (informational)
                NotificationToggleItem(
                    title = "Cập nhật & Tổng kết",
                    subtitle = "Tổng kết doanh thu, cập nhật hệ thống",
                    icon = Icons.Default.Update,
                    checked = preferences?.informational ?: true,
                    enabled = preferences != null,
                    onCheckedChange = onInformationalChanged
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Khuyến mãi (marketing)
                NotificationToggleItem(
                    title = "Khuyến mãi",
                    subtitle = "Voucher mới, chương trình khuyến mãi",
                    icon = Icons.Default.LocalOffer,
                    checked = preferences?.marketing ?: true,
                    enabled = preferences != null,
                    onCheckedChange = onMarketingChanged
                )
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Icon Background
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title and Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Switch
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}
