package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun TermsScreen(onBack: () -> Unit = {}) {
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
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Điều khoản sử dụng",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = ShipperColors.TextPrimary
                    )
                }
                
                HorizontalDivider(color = ShipperColors.Divider)
                
                Text(
                    "Khi sử dụng ứng dụng, bạn đồng ý với các điều khoản sau:",
                    fontSize = 14.sp,
                    color = ShipperColors.TextPrimary,
                    lineHeight = 22.sp
                )
                
                TermItem("Không sử dụng ứng dụng cho mục đích bất hợp pháp.")
                TermItem("Tuân thủ quy định về giao nhận, bảo mật và thanh toán.")
                TermItem("Chính sách hoàn tiền, khiếu nại và hỗ trợ được công khai minh bạch.")
                TermItem("Chúng tôi có quyền cập nhật điều khoản mà không cần báo trước.")
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
private fun TermItem(text: String) {
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
