package com.example.foodapp.pages.shipper.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bank Card Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Primary),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bankName, color = ShipperColors.Surface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = ShipperColors.Surface.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text("Số tài khoản", color = ShipperColors.Surface.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(accountNumber, color = ShipperColors.Surface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Text(accountHolder.uppercase(), color = ShipperColors.Surface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PaymentField("Tên ngân hàng", bankName, { bankName = it }, Icons.Outlined.AccountBalance)
                HorizontalDivider(color = ShipperColors.Divider)
                PaymentField("Số tài khoản", accountNumber, { accountNumber = it }, Icons.Outlined.CreditCard)
                HorizontalDivider(color = ShipperColors.Divider)
                PaymentField("Chủ tài khoản", accountHolder, { accountHolder = it }, Icons.Outlined.Person)
                
                HorizontalDivider(color = ShipperColors.Divider)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = ShipperColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Đặt làm mặc định",
                            color = ShipperColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ShipperColors.Surface,
                            checkedTrackColor = ShipperColors.Primary,
                            uncheckedThumbColor = ShipperColors.Surface,
                            uncheckedTrackColor = ShipperColors.TextTertiary
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ShipperColors.TextSecondary
                )
            ) {
                Text("Hủy")
            }
            Button(
                onClick = {
                    Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
            ) {
                Text("Lưu", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PaymentField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShipperColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = ShipperColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ShipperColors.Primary,
                unfocusedBorderColor = ShipperColors.Divider
            ),
            singleLine = true
        )
    }
}
