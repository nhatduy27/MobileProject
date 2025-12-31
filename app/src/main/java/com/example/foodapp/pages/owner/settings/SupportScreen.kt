package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class FAQItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavHostController) {
    val faqItems = remember {
        listOf(
            FAQItem(
                "Làm thế nào để thêm món ăn mới?",
                "Vào menu Quản lý món ăn, nhấn nút + ở góc dưới bên phải. Điền đầy đủ thông tin món ăn bao gồm tên, giá, mô tả và ảnh. Sau đó nhấn Lưu để hoàn tất."
            ),
            FAQItem(
                "Cách xử lý đơn hàng mới?",
                "Khi có đơn hàng mới, bạn sẽ nhận được thông báo. Vào mục Đơn hàng, chọn đơn cần xử lý và cập nhật trạng thái: Đang chuẩn bị → Đang giao → Hoàn thành."
            ),
            FAQItem(
                "Làm sao để thay đổi giờ mở cửa?",
                "Vào Cài đặt → Thông tin cửa hàng, chọn chỉnh sửa và cập nhật giờ mở cửa/đóng cửa. Thông tin sẽ hiển thị cho khách hàng."
            ),
            FAQItem(
                "Tôi quên mật khẩu, phải làm sao?",
                "Tại màn hình đăng nhập, chọn 'Quên mật khẩu'. Nhập email đã đăng ký, chúng tôi sẽ gửi link đặt lại mật khẩu."
            ),
            FAQItem(
                "Làm thế nào để xem báo cáo doanh thu?",
                "Vào mục Thống kê trên menu chính. Bạn có thể xem báo cáo theo ngày, tuần hoặc tháng với biểu đồ chi tiết."
            ),
            FAQItem(
                "Tôi có thể thêm nhiều tài khoản ngân hàng không?",
                "Có, vào Cài đặt → Phương thức thanh toán. Bạn có thể thêm nhiều tài khoản và chọn một làm mặc định."
            ),
            FAQItem(
                "Cách tắt thông báo đơn hàng?",
                "Vào Cài đặt → cuộn xuống mục Thông báo. Tắt công tắc 'Đơn hàng mới' hoặc các loại thông báo khác tùy ý."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Trợ giúp & Hỗ trợ", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF333333)
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Contact Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ContactCard(
                        icon = "📧",
                        title = "Email",
                        subtitle = "support@ktxfood.com",
                        modifier = Modifier.weight(1f)
                    )
                    ContactCard(
                        icon = "📞",
                        title = "Hotline",
                        subtitle = "1900-xxxx",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                ContactCard(
                    icon = "💬",
                    title = "Chat trực tuyến",
                    subtitle = "Nhấn để trò chuyện với đội ngũ hỗ trợ",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // FAQ Section Header
            item {
                Text(
                    text = "Câu hỏi thường gặp",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // FAQ Items
            items(faqItems) { faq ->
                FAQCard(faq = faq)
            }
        }
    }
}

@Composable
fun ContactCard(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { /* TODO: Handle click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFF6B35), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
            }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Thu gọn" else "Mở rộng",
                    tint = Color(0xFF666666)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 22.sp
                )
            }
        }
    }
}
