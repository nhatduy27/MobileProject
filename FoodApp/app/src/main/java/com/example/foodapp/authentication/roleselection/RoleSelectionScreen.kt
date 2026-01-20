package com.example.foodapp.authentication.roleselection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.ui.theme.PrimaryOrange
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun RoleSelectionScreen(
    onRoleSaved: (String) -> Unit,
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val repository = UserFirebaseRepository(context)

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000)
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tiêu đề
        Text(
            text = "Chọn vai trò của bạn",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp, bottom = 8.dp)
        )

        Text(
            text = "Lựa chọn này sẽ xác định trải nghiệm của bạn trong ứng dụng",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Hiển thị lỗi nếu có
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Role values phải khớp với backend UserRole enum: CUSTOMER, OWNER, SHIPPER
        val roles = listOf(
            Triple("CUSTOMER", "Người dùng", "Đặt món và đánh giá nhà hàng"),
            Triple("OWNER", "Nhà bán hàng", "Quản lý cửa hàng và thực đơn"),
            Triple("SHIPPER", "Người giao hàng", "Nhận đơn và giao hàng tận nơi")
        )


        roles.forEach { role ->
            RoleItem(
                title = role.second,
                description = role.third,
                isSelected = selectedRole == role.first,
                onClick = { selectedRole = role.first }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nút Tiếp tục
        Button(
            onClick = {
                val userId = auth.currentUser?.uid
                if (userId != null && selectedRole != null) {
                    isLoading = true
                    val role = selectedRole!!
                    repository.saveUserRole(userId, selectedRole!!) { success, msg ->
                        isLoading = false
                        if (success) onRoleSaved(role) else errorMessage = msg
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryOrange,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = selectedRole != null && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RoleItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryOrange else Color(0xFFEEEEEE),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF7F0) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryOrange else Color.Black
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}