package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun PrivacyScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Security,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Chính sách bảo mật",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = ShipperColors.TextPrimary
                    )
                }
                
                HorizontalDivider(color = ShipperColors.Divider)
                
                Text(
                    "Chúng tôi cam kết bảo vệ thông tin cá nhân của bạn. Dữ liệu của bạn sẽ được mã hóa và chỉ sử dụng cho mục đích vận hành ứng dụng.",
                    fontSize = 14.sp,
                    color = ShipperColors.TextPrimary,
                    lineHeight = 22.sp
                )
                
                PrivacyItem("Không chia sẻ thông tin cá nhân cho bên thứ ba khi chưa có sự đồng ý.")
                PrivacyItem("Bạn có thể yêu cầu xóa tài khoản và dữ liệu bất cứ lúc nào.")
                PrivacyItem("Mọi hoạt động đăng nhập, đổi mật khẩu đều được bảo vệ nhiều lớp.")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
        ) {
            Text("Đã hiểu", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PrivacyItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "•",
            fontSize = 14.sp,
            color = ShipperColors.Primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text,
            fontSize = 14.sp,
            color = ShipperColors.TextSecondary,
            lineHeight = 22.sp
        )
    }
}
