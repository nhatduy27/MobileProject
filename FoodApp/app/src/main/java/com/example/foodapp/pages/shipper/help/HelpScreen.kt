package com.example.foodapp.pages.shipper.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun HelpScreen() {
    val categories = listOf(
        HelpCategory(
            "1",
            "rocket",
            "Bắt đầu với FoodApp",
            "Hướng dẫn cho người mới",
            emptyList()
        ),
        HelpCategory(
            "2",
            "package",
            "Quản lý đơn hàng",
            "Nhận, giao và hoàn thành đơn",
            emptyList()
        ),
        HelpCategory(
            "3",
            "wallet",
            "Thu nhập & Thanh toán",
            "Cách tính phí và nhận tiền",
            emptyList()
        ),
        HelpCategory(
            "4",
            "settings",
            "Cài đặt tài khoản",
            "Quản lý thông tin cá nhân",
            emptyList()
        ),
        HelpCategory(
            "5",
            "help",
            "Khác",
            "Các câu hỏi khác",
            emptyList()
        )
    )

    val faqs = listOf(
        FAQ(
            "Làm thế nào để nhận đơn hàng?",
            "Khi có đơn hàng mới phù hợp với khu vực của bạn, hệ thống sẽ gửi thông báo. Bạn nhấn 'Nhận đơn' để xác nhận nhận đơn hàng đó."
        ),
        FAQ(
            "Tôi có thể hủy đơn hàng không?",
            "Bạn có thể hủy đơn hàng trước khi lấy hàng. Sau khi đã lấy hàng, vui lòng liên hệ hotline để được hỗ trợ."
        ),
        FAQ(
            "Khi nào tôi nhận được tiền?",
            "Tiền sẽ được chuyển vào tài khoản ngân hàng của bạn vào mỗi thứ 2 hàng tuần cho tất cả đơn hàng hoàn thành tuần trước."
        ),
        FAQ(
            "Làm sao để tăng thu nhập?",
            "Bạn có thể tăng thu nhập bằng cách: (1) Hoạt động trong khung giờ cao điểm, (2) Duy trì tỷ lệ hoàn thành đơn cao, (3) Nhận đánh giá tốt từ khách hàng."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContactSupportCard()

            Text(
                text = "Danh mục trợ giúp",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            categories.forEach { category ->
                HelpCategoryCard(category)
            }

            Text(
                text = "Câu hỏi thường gặp",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            faqs.forEach { faq ->
                FAQCard(faq)
            }
        }
    }
}
