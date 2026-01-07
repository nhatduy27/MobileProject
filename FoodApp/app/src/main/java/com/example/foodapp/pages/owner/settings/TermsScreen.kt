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
fun TermsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Điều khoản sử dụng", fontWeight = FontWeight.Bold) },
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
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Điều khoản sử dụng",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Cập nhật lần cuối: 31/12/2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TermsSection(
                        title = "1. Chấp nhận điều khoản",
                        content = "Bằng việc sử dụng ứng dụng KTX Food Store, bạn đồng ý tuân thủ các điều khoản và điều kiện sử dụng được nêu trong tài liệu này. Nếu bạn không đồng ý với bất kỳ phần nào của các điều khoản này, vui lòng không sử dụng ứng dụng."
                    )

                    TermsSection(
                        title = "2. Sử dụng dịch vụ",
                        content = "Ứng dụng cung cấp nền tảng để quản lý cửa hàng đồ ăn và kết nối với khách hàng. Người dùng cam kết sử dụng dịch vụ một cách hợp pháp và không vi phạm quyền lợi của bên thứ ba."
                    )

                    TermsSection(
                        title = "3. Tài khoản người dùng",
                        content = "Bạn chịu trách nhiệm duy trì tính bảo mật của tài khoản và mật khẩu của mình. Bạn đồng ý chịu trách nhiệm cho tất cả các hoạt động xảy ra dưới tài khoản của bạn."
                    )

                    TermsSection(
                        title = "4. Quyền sở hữu trí tuệ",
                        content = "Tất cả nội dung, thiết kế, văn bản, đồ họa và các tài liệu khác trong ứng dụng đều thuộc quyền sở hữu của KTX Food Store hoặc được cấp phép hợp pháp."
                    )

                    TermsSection(
                        title = "5. Thanh toán và hoàn tiền",
                        content = "Các giao dịch thanh toán được xử lý qua các cổng thanh toán an toàn. Chính sách hoàn tiền sẽ được áp dụng theo từng trường hợp cụ thể và tuân thủ quy định của pháp luật."
                    )

                    TermsSection(
                        title = "6. Giới hạn trách nhiệm",
                        content = "KTX Food Store không chịu trách nhiệm cho bất kỳ thiệt hại trực tiếp, gián tiếp, ngẫu nhiên hoặc hậu quả phát sinh từ việc sử dụng hoặc không thể sử dụng dịch vụ."
                    )

                    TermsSection(
                        title = "7. Thay đổi điều khoản",
                        content = "Chúng tôi có quyền sửa đổi các điều khoản này bất kỳ lúc nào. Các thay đổi sẽ có hiệu lực ngay khi được đăng tải lên ứng dụng."
                    )

                    TermsSection(
                        title = "8. Liên hệ",
                        content = "Nếu bạn có bất kỳ câu hỏi nào về Điều khoản sử dụng, vui lòng liên hệ với chúng tôi qua email: support@ktxfood.com hoặc số điện thoại: 1900-xxxx."
                    )
                }
            }
        }
    }
}

@Composable
fun TermsSection(title: String, content: String) {
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
