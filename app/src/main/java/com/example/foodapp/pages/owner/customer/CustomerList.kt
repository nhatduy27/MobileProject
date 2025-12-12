package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomerList() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        val customers = listOf(
            Customer("Nguyễn Thị Mai", "VIP", "0901234567 • KTX Khu A, P201", "67 đơn • Tham gia 15/08/2024", "3.2M"),
            Customer("Trần Văn Bình", "VIP", "0912345678 • KTX Khu B, P305", "52 đơn • Tham gia 10/09/2024", "2.8M"),
            Customer("Lê Hoàng Anh", "Thường xuyên", "0923456789 • KTX Khu A, P108", "28 đơn • Tham gia 05/10/2024", "1.4M"),
            Customer("Phạm Thị Hương", "Thường xuyên", "0934567890 • KTX Khu C, P412", "19 đơn • Tham gia 22/10/2024", "950K"),
            Customer("Hoàng Minh Tuấn", "Mới", "0945678901 • KTX Khu B, P215", "3 đơn • Tham gia 01/12/2024", "180K"),
            Customer("Võ Thị Lan", "Mới", "0956789012 • KTX Khu A, P520", "1 đơn • Tham gia 08/12/2024", "45K"),
        )

        customers.forEach { customer ->
            CustomerCard(customer)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}