package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun PaymentMethodScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var bankName by remember { mutableStateOf("Vietcombank") }
    var accountNumber by remember { mutableStateOf("0123456789") }
    var accountHolder by remember { mutableStateOf("Nguyễn Văn A") }
    var isDefault by remember { mutableStateOf(true) }

    val mainColor = Color(0xFFFF6B35)
    val secondaryColor = Color(0xFFFFA07A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF5F5), Color(0xFFFFE5D9), Color.White)
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Phương thức thanh toán",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = mainColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = mainColor),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ngân hàng", color = Color.White, fontSize = 14.sp)
                Text(bankName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Số tài khoản", color = Color.White, fontSize = 14.sp)
                Text(accountNumber, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Chủ tài khoản", color = Color.White, fontSize = 14.sp)
                Text(accountHolder, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("Tên ngân hàng") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it },
            label = { Text("Số tài khoản") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = accountHolder,
            onValueChange = { accountHolder = it },
            label = { Text("Chủ tài khoản") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isDefault,
                onCheckedChange = { isDefault = it },
                colors = SwitchDefaults.colors(checkedThumbColor = mainColor)
            )
            Text("Đặt làm mặc định", color = mainColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                colors = ButtonDefaults.buttonColors(containerColor = mainColor)
            ) { Text("Lưu", color = Color.White) }
            OutlinedButton(onClick = onCancel, colors = ButtonDefaults.outlinedButtonColors(contentColor = mainColor)) { Text("Hủy") }
        }
    }
}
