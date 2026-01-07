package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chính sách bảo mật", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chính sách bảo mật",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Cập nhật lần cuối: 31/12/2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    PrivacySection(
                        title = "1. Thu thập thông tin",
                        content = "Chúng tôi thu thập các thông tin cá nhân mà bạn cung cấp khi đăng ký tài khoản, bao gồm tên, email, số điện thoại và địa chỉ. Chúng tôi cũng tự động thu thập dữ liệu về cách bạn sử dụng ứng dụng."
                    )

                    PrivacySection(
                        title = "2. Sử dụng thông tin",
                        content = "Thông tin của bạn được sử dụng để:\n• Cung cấp và cải thiện dịch vụ\n• Xử lý giao dịch và đơn hàng\n• Gửi thông báo quan trọng\n• Phân tích và cải thiện trải nghiệm người dùng\n• Ngăn chặn gian lận và đảm bảo bảo mật"
                    )

                    PrivacySection(
                        title = "3. Bảo vệ thông tin",
                        content = "Chúng tôi áp dụng các biện pháp bảo mật kỹ thuật và tổ chức phù hợp để bảo vệ thông tin cá nhân của bạn khỏi truy cập trái phép, mất mát hoặc tiết lộ."
                    )

                    PrivacySection(
                        title = "4. Chia sẻ thông tin",
                        content = "Chúng tôi không bán hoặc cho thuê thông tin cá nhân của bạn cho bên thứ ba. Thông tin chỉ được chia sẻ trong các trường hợp:\n• Có sự đồng ý của bạn\n• Yêu cầu của pháp luật\n• Bảo vệ quyền lợi của chúng tôi\n• Với các đối tác dịch vụ đáng tin cậy"
                    )

                    PrivacySection(
                        title = "5. Cookies và công nghệ theo dõi",
                        content = "Ứng dụng sử dụng cookies và các công nghệ tương tự để cải thiện trải nghiệm người dùng, phân tích lưu lượng truy cập và cá nhân hóa nội dung."
                    )

                    PrivacySection(
                        title = "6. Quyền của người dùng",
                        content = "Bạn có quyền:\n• Truy cập và xem thông tin cá nhân\n• Yêu cầu sửa đổi thông tin không chính xác\n• Yêu cầu xóa tài khoản và dữ liệu\n• Phản đối việc xử lý dữ liệu\n• Rút lại sự đồng ý bất kỳ lúc nào"
                    )

                    PrivacySection(
                        title = "7. Lưu trữ dữ liệu",
                        content = "Chúng tôi lưu trữ thông tin cá nhân của bạn trong thời gian cần thiết để cung cấp dịch vụ hoặc tuân thủ các nghĩa vụ pháp lý."
                    )

                    PrivacySection(
                        title = "8. Thay đổi chính sách",
                        content = "Chúng tôi có thể cập nhật Chính sách bảo mật này theo thời gian. Mọi thay đổi sẽ được thông báo qua ứng dụng hoặc email."
                    )

                    PrivacySection(
                        title = "9. Liên hệ",
                        content = "Nếu bạn có câu hỏi về Chính sách bảo mật, vui lòng liên hệ:\nEmail: privacy@ktxfood.com\nĐiện thoại: 1900-xxxx\nĐịa chỉ: KTX ĐHQG, Dĩ An, Bình Dương"
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}
