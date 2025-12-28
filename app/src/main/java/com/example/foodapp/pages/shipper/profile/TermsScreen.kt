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
fun TermsScreen(onBack: () -> Unit = {}) {
    val mainColor = Color(0xFFFF6B35)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F5))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Điều khoản & Chính sách",
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
                Text("Khi sử dụng ứng dụng, bạn đồng ý với các điều khoản sau:", fontSize = 16.sp)
                Text("• Không sử dụng ứng dụng cho mục đích bất hợp pháp.", fontSize = 15.sp)
                Text("• Tuân thủ quy định về giao nhận, bảo mật và thanh toán.", fontSize = 15.sp)
                Text("• Chính sách hoàn tiền, khiếu nại và hỗ trợ được công khai minh bạch.", fontSize = 15.sp)
                Text("• Chúng tôi có quyền cập nhật điều khoản mà không cần báo trước.", fontSize = 15.sp)
            }
        }
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = mainColor),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Quay lại", color = Color.White) }
    }
}
