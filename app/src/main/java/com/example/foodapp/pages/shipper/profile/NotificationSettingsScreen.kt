package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun NotificationSettingsScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var orderNoti by remember { mutableStateOf(true) }
    var promoNoti by remember { mutableStateOf(true) }
    var systemNoti by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }

    val mainColor = Color(0xFFFF6B35)

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
