package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    val mainColor = Color(0xFFFF6B35)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F5))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Trợ giúp & Hỗ trợ",
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
                Text("Bạn cần hỗ trợ? Hãy xem các câu hỏi thường gặp hoặc liên hệ với chúng tôi:", fontSize = 16.sp)
                Text("• Hotline: 1900 1234 (8:00 - 22:00)", fontSize = 15.sp)
                Text("• Email: support@foodapp.vn", fontSize = 15.sp)
                Text("• Trung tâm trợ giúp: https://foodapp.vn/help", fontSize = 15.sp, color = mainColor)
                Text("• Fanpage: fb.com/foodapp.vn", fontSize = 15.sp, color = mainColor)
            }
        }
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = mainColor),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Quay lại", color = Color.White) }
    }
}
