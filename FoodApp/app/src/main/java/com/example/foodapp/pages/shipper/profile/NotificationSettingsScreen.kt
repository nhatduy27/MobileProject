package com.example.foodapp.pages.shipper.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.pages.shipper.theme.ShipperColors
import kotlinx.coroutines.launch

/**
 * Màn hình cài đặt thông báo cho Shipper
 * API có sẵn:
 * - POST /api/shippers/notifications/online - Bật nhận đơn (subscribe to topic)
 * - DELETE /api/shippers/notifications/online - Tắt nhận đơn (unsubscribe from topic)
 */
@Composable
fun NotificationSettingsScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { RepositoryProvider.getShipperOrderRepository() }
    
    var isOnline by remember { mutableStateOf(false) }
    var isTogglingOnline by remember { mutableStateOf(false) }
    
    fun toggleOnlineStatus() {
        scope.launch {
            isTogglingOnline = true
            val result = if (isOnline) {
                repository.goOffline()
            } else {
                repository.goOnline()
            }
            
            result.onSuccess { topic ->
                isOnline = !isOnline
                val message = if (isOnline) "Đang nhận đơn hàng mới" else "Đã tắt nhận đơn"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d("NotificationSettings", "Online status: $isOnline, topic: $topic")
            }.onFailure { e ->
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("NotificationSettings", "Toggle failed", e)
            }
            
            isTogglingOnline = false
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
        // Online Status Card - Đây là tính năng chính có API
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) ShipperColors.SuccessLight else ShipperColors.Surface
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnline) ShipperColors.Success.copy(alpha = 0.15f) 
                               else ShipperColors.TextTertiary.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isOnline) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) ShipperColors.Success else ShipperColors.TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            "Nhận đơn hàng", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp, 
                            color = ShipperColors.TextPrimary
                        )
                        Text(
                            "Quản lý trạng thái hoạt động",
                            fontSize = 13.sp,
                            color = ShipperColors.TextSecondary
                        )
                    }
                }
                
                HorizontalDivider(color = ShipperColors.Divider)
                
                // Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isOnline) "Đang hoạt động" else "Tạm nghỉ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (isOnline) ShipperColors.Success else ShipperColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isOnline) 
                                "Bạn sẽ nhận được thông báo khi có đơn hàng mới từ cửa hàng" 
                            else 
                                "Bật để nhận thông báo đơn hàng mới",
                            fontSize = 13.sp,
                            color = ShipperColors.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    if (isTogglingOnline) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = ShipperColors.Primary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Switch(
                            checked = isOnline, 
                            onCheckedChange = { toggleOnlineStatus() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShipperColors.Surface,
                                checkedTrackColor = ShipperColors.Success,
                                uncheckedThumbColor = ShipperColors.Surface,
                                uncheckedTrackColor = ShipperColors.TextTertiary
                            )
                        )
                    }
                }
            }
        }
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.InfoLight),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ShipperColors.Info,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        "Về tính năng này",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ShipperColors.Info
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Khi bật \"Đang hoạt động\", bạn sẽ được đăng ký nhận thông báo đẩy từ cửa hàng mà bạn đang làm việc. Mỗi khi có đơn hàng sẵn sàng để giao, bạn sẽ nhận được thông báo ngay lập tức.",
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Back Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ShipperColors.TextSecondary
            )
        ) {
            Text("Quay lại", fontWeight = FontWeight.Medium)
        }
    }
}
