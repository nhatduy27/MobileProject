package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun EditProfileScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(TextFieldValue("Nguyễn Văn A")) }
    var phone by remember { mutableStateOf(TextFieldValue("0901234567")) }
    var email by remember { mutableStateOf(TextFieldValue("shipper@example.com")) }
    var address by remember { mutableStateOf(TextFieldValue("123 Đường ABC, Quận 1, TP.HCM")) }
    // Ảnh đại diện mock, dùng tên resource có sẵn trong drawable
    var avatarName by remember { mutableStateOf("avatar_esther") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Chỉnh sửa thông tin", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            val avatarId = when (avatarName) {

                "avatar_esther" -> com.example.foodapp.R.drawable.avatar_esther
                "avatar_jacob" -> com.example.foodapp.R.drawable.avatar_jacob
                else -> com.example.foodapp.R.drawable.avatar_esther
            }
            Image(
                painter = painterResource(id = avatarId),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable {
                        // Đổi qua lại giữa 2 avatar mẫu
                        avatarName = if (avatarName == "avatar_esther") "avatar_jacob" else "avatar_esther"
                    }
            )
            Text(
                text = "Tải ảnh lên",
                fontSize = 12.sp,
                color = Color(0xFF1976D2),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 80.dp)
                    .clickable {
                        avatarName = if (avatarName == "avatar_esther") "avatar_jacob" else "avatar_esther"
                    }
            )
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Số điện thoại") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Địa chỉ") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                onSave()
            }) { Text("Lưu") }
            OutlinedButton(onClick = onCancel) { Text("Hủy") }
        }
    }
}
