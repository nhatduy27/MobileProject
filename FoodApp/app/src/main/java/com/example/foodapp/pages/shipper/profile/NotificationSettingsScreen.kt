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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.pages.shipper.theme.ShipperColors
import kotlinx.coroutines.launch

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
    
    var orderNoti by remember { mutableStateOf(true) }
    var promoNoti by remember { mutableStateOf(true) }
    var systemNoti by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }
    
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
        // Online Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) ShipperColors.SuccessLight else ShipperColors.Surface
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
                        imageVector = if (isOnline) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint = if (isOnline) ShipperColors.Success else ShipperColors.TextSecondary
                    )
                    Text(
                        "Trạng thái hoạt động", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 15.sp, 
                        color = ShipperColors.TextPrimary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            if (isOnline) "Đang hoạt động" else "Ngoại tuyến",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isOnline) ShipperColors.Success else ShipperColors.TextSecondary
                        )
                        Text(
                            if (isOnline) "Bạn sẽ nhận thông báo đơn hàng mới" 
                            else "Bật để nhận đơn hàng mới",
                            fontSize = 12.sp,
                            color = ShipperColors.TextSecondary
                        )
                    }
                    
                    if (isTogglingOnline) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ShipperColors.Primary,
                            strokeWidth = 2.dp
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
        
        // Notification Types Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Loại thông báo", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 15.sp, 
                        color = ShipperColors.TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                NotificationToggleRow("Đơn hàng mới", orderNoti) { orderNoti = it }
                HorizontalDivider(color = ShipperColors.Divider)
                NotificationToggleRow("Khuyến mãi & Ưu đãi", promoNoti) { promoNoti = it }
                HorizontalDivider(color = ShipperColors.Divider)
                NotificationToggleRow("Thông báo hệ thống", systemNoti) { systemNoti = it }
            }
        }
        
        // Sound & Vibration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Âm thanh & Rung", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 15.sp, 
                        color = ShipperColors.TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                NotificationToggleRow("Âm thanh thông báo", sound) { sound = it }
                HorizontalDivider(color = ShipperColors.Divider)
                NotificationToggleRow("Rung khi nhận thông báo", vibrate) { vibrate = it }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ShipperColors.TextSecondary
                )
            ) {
                Text("Hủy")
            }
            Button(
                onClick = {
                    Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
            ) {
                Text("Lưu", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = ShipperColors.TextPrimary,
            fontSize = 14.sp
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ShipperColors.Surface,
                checkedTrackColor = ShipperColors.Primary,
                uncheckedThumbColor = ShipperColors.Surface,
                uncheckedTrackColor = ShipperColors.TextTertiary
            )
        )
    }
}
