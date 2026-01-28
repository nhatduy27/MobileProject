package com.example.foodapp.pages.shipper.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { RepositoryProvider.getShipperOrderRepository() }
    
    // Online status state
    var isOnline by remember { mutableStateOf(false) }
    var isTogglingOnline by remember { mutableStateOf(false) }
    
    // Other notification settings
    var orderNoti by remember { mutableStateOf(true) }
    var promoNoti by remember { mutableStateOf(true) }
    var systemNoti by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }

    val mainColor = Color(0xFFFF6B35)
    
    // Function to toggle online status
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
            .background(Color(0xFFFFF5F5))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Cài đặt thông báo",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = mainColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        // Online Status Card - NEW
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) Color(0xFFE8F5E9) else Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
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
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isOnline) Color(0xFF4CAF50) else Color.Gray
                    )
                    Text(
                        "Trạng thái hoạt động", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp, 
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
                            if (isOnline) "Đang hoạt động" else "Ngoại tuyến",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (isOnline) Color(0xFF4CAF50) else Color.Gray
                        )
                        Text(
                            if (isOnline) "Bạn sẽ nhận thông báo đơn hàng mới" 
                            else "Bật để nhận đơn hàng mới",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    if (isTogglingOnline) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = mainColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Switch(
                            checked = isOnline, 
                            onCheckedChange = { toggleOnlineStatus() },
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
        
        // Notification Types Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Loại thông báo", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = mainColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Đơn hàng mới", modifier = Modifier.weight(1f))
                    Switch(checked = orderNoti, onCheckedChange = { orderNoti = it }, colors = SwitchDefaults.colors(checkedThumbColor = mainColor))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Khuyến mãi & Ưu đãi", modifier = Modifier.weight(1f))
                    Switch(checked = promoNoti, onCheckedChange = { promoNoti = it }, colors = SwitchDefaults.colors(checkedThumbColor = mainColor))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thông báo hệ thống", modifier = Modifier.weight(1f))
                    Switch(checked = systemNoti, onCheckedChange = { systemNoti = it }, colors = SwitchDefaults.colors(checkedThumbColor = mainColor))
                }
            }
        }
        
        // Sound & Vibration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Âm thanh & Rung", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = mainColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Âm thanh thông báo", modifier = Modifier.weight(1f))
                    Switch(checked = sound, onCheckedChange = { sound = it }, colors = SwitchDefaults.colors(checkedThumbColor = mainColor))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rung khi nhận thông báo", modifier = Modifier.weight(1f))
                    Switch(checked = vibrate, onCheckedChange = { vibrate = it }, colors = SwitchDefaults.colors(checkedThumbColor = mainColor))
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                colors = ButtonDefaults.buttonColors(containerColor = mainColor)
            ) { Text("Lưu", color = Color.White) }
            OutlinedButton(onClick = onCancel, colors = ButtonDefaults.outlinedButtonColors(contentColor = mainColor)) { Text("Hủy") }
        }
    }
}

