package com.example.foodapp.pages.user.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.foodapp.R

@Composable
fun UserCategoryList() {
    val categories = listOf(
        "Tất cả" to R.drawable.all_image,
        "Món ăn" to R.drawable.food_image,
        "Thức uống" to R.drawable.drink_image,
        "Ăn vặt" to R.drawable.snack_image
    )

    Column(modifier = Modifier.padding(12.dp)) {
        Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEach { (name, icon) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFFFF5F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painter = painterResource(icon), contentDescription = name, modifier = Modifier.size(30.dp))
                    }
                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}