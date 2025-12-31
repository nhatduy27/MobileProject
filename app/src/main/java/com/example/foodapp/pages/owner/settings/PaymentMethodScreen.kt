package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class BankAccount(
    val id: String,
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(navController: NavHostController) {
    var bankAccounts by remember {
        mutableStateOf(
            listOf(
                BankAccount(
                    "1",
                    "Vietcombank",
                    "1234567890",
                    "NGUYEN VAN A",
                    true
                ),
                BankAccount(
                    "2",
                    "Techcombank",
                    "0987654321",
                    "NGUYEN VAN A",
                    false
                )
            )
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Phương thức thanh toán", fontWeight = FontWeight.Bold) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "ℹ️", fontSize = 24.sp)
                    Text(
                        text = "Thêm tài khoản ngân hàng để nhận thanh toán từ khách hàng. Bạn có thể thêm nhiều tài khoản và chọn một làm mặc định.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        lineHeight = 20.sp
                    )
                }
            }

            // Bank Accounts List
            bankAccounts.forEach { account ->
                BankAccountCard(
                    account = account,
                    onSetDefault = { accountId ->
                        bankAccounts = bankAccounts.map {
                            it.copy(isDefault = it.id == accountId)
                        }
                    },
                    onDelete = { accountId ->
                        bankAccounts = bankAccounts.filter { it.id != accountId }
                    }
                )
            }

            // Add Bank Account Button
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF6B35)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm tài khoản ngân hàng", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }

    if (showAddDialog) {
        AddBankAccountDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { bankName, accountNumber, accountName ->
                val newAccount = BankAccount(
                    id = (bankAccounts.size + 1).toString(),
                    bankName = bankName,
                    accountNumber = accountNumber,
                    accountName = accountName,
                    isDefault = bankAccounts.isEmpty()
                )
                bankAccounts = bankAccounts + newAccount
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BankAccountCard(
    account: BankAccount,
    onSetDefault: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (account.isDefault) Color(0xFFFFF3E0) else Color.White
        ),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFF6B35), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏦", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = account.bankName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = account.accountNumber,
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                if (account.isDefault) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF6B35)
                    ) {
                        Text(
                            text = "Mặc định",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Chủ tài khoản: ${account.accountName}",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!account.isDefault) {
                    OutlinedButton(
                        onClick = { onSetDefault(account.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Đặt làm mặc định", fontSize = 13.sp)
                    }
                }
                OutlinedButton(
                    onClick = { onDelete(account.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AddBankAccountDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm tài khoản ngân hàng", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Chủ tài khoản") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bankName.isNotEmpty() && accountNumber.isNotEmpty() && accountName.isNotEmpty()) {
                        onAdd(bankName, accountNumber, accountName)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF666666))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
