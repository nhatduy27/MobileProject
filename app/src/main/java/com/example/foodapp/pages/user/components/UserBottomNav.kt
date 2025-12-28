package com.example.foodapp.pages.user.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodapp.R
import com.example.foodapp.navigation.Screen

@Composable
fun UserBottomNav(
    navController: NavHostController,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFFFF9800)), // Màu orange
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Triple("Trang chủ", R.drawable.btn_1, { navController.navigate(Screen.UserHome.route) }),
            Triple("Giỏ hàng", R.drawable.btn_2, { navController.navigate(Screen.UserCart.route) }),
            Triple("Yêu thích", R.drawable.btn_3, { navController.navigate(Screen.UserFavorites.route) }),
            Triple("Thông báo", R.drawable.btn_4, { navController.navigate(Screen.UserNotifications.route) }),
            Triple("Tôi", R.drawable.btn_5, onProfileClick)
        )

        items.forEach { (label, iconRes, onClick) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onClick() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = label, color = Color.White, fontSize = 10.sp)
            }
        }
    }
}