package com.example.foodapp.user.profile
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodapp.user.components.UserBottomNav
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun UserProfileScreen(
    userName: String = "Nguyễn Văn A",
    userPhone: String = "0987654321",
    userAddress: String = "123 Đường ABC, Quận 1, TP.HCM",
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = { UserBottomNav(onProfileClick = onProfileClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header với nút back
            ProfileHeader(onBackClick = onBackClick)

            // Avatar và tên
            UserInfoHeader(userName = userName)

            // Thông tin chi tiết
            ProfileInfoSection(
                userName = userName,
                userPhone = userPhone,
                userAddress = userAddress
            )

            // Nút đăng xuất
            LogoutButton(onLogoutClick = onLogoutClick)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.Black
            )
        }

        Text(
            text = "Thông tin tài khoản",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun UserInfoHeader(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Card(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Khách hàng",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileInfoSection(
    userName: String,
    userPhone: String,
    userAddress: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Thông tin cá nhân
            Text(
                text = "Thông tin cá nhân",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Divider(modifier = Modifier.fillMaxWidth())

            // Tên
            ProfileInfoItem(
                icon = Icons.Filled.Person,
                title = "Họ và tên",
                value = userName,
                showDivider = true
            )

            // Số điện thoại
            ProfileInfoItem(
                icon = Icons.Filled.Phone,
                title = "Số điện thoại",
                value = userPhone,
                showDivider = true
            )

            // Địa chỉ
            ProfileInfoItem(
                icon = Icons.Filled.LocationOn,
                title = "Địa chỉ",
                value = userAddress,
                showDivider = false
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 56.dp),
                thickness = 0.5.dp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun LogoutButton(onLogoutClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onLogoutClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center  // <-- Căn giữa nội dung trong Box
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = "Đăng xuất",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Red
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    MaterialTheme {
        UserProfileScreen()
    }
}