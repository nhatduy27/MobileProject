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
fun PrivacyScreen(onBack: () -> Unit = {}) {
    val mainColor = Color(0xFFFF6B35)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F5))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Bảo mật & Quyền riêng tư",
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
                Text("Chúng tôi cam kết bảo vệ thông tin cá nhân của bạn. Dữ liệu của bạn sẽ được mã hóa và chỉ sử dụng cho mục đích vận hành ứng dụng.", fontSize = 16.sp)
                Text("• Không chia sẻ thông tin cá nhân cho bên thứ ba khi chưa có sự đồng ý.", fontSize = 15.sp)
                Text("• Bạn có thể yêu cầu xóa tài khoản và dữ liệu bất cứ lúc nào.", fontSize = 15.sp)
                Text("• Mọi hoạt động đăng nhập, đổi mật khẩu đều được bảo vệ nhiều lớp.", fontSize = 15.sp)
            }
        }
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = mainColor),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Quay lại", color = Color.White) }
    }
}
