package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodapp.pages.owner.customer.CustomerHeader
import com.example.foodapp.pages.owner.customer.CustomerFilterTabs
import com.example.foodapp.pages.owner.customer.CustomerStats
import com.example.foodapp.pages.owner.customer.CustomerCard
import com.example.foodapp.pages.owner.customer.Customer

@Composable
fun CustomerScreen() {
    var selectedFilter by remember { mutableStateOf("Tất cả") }

    val customers = listOf(
        Customer("Nguyễn Thị Mai", "VIP", "0901234567 • KTX Khu A, P201", "67 đơn • Tham gia 15/08/2024", "3.2M"),
        Customer("Trần Văn Bình", "VIP", "0912345678 • KTX Khu B, P305", "52 đơn • Tham gia 10/09/2024", "2.8M"),
        Customer("Lê Hoàng Anh", "Thường xuyên", "0923456789 • KTX Khu A, P108", "28 đơn • Tham gia 05/10/2024", "1.4M"),
        Customer("Phạm Thị Hương", "Thường xuyên", "0934567890 • KTX Khu C, P412", "19 đơn • Tham gia 22/10/2024", "950K"),
        Customer("Hoàng Minh Tuấn", "Mới", "0945678901 • KTX Khu B, P215", "3 đơn • Tham gia 01/12/2024", "180K"),
        Customer("Võ Thị Lan", "Mới", "0956789012 • KTX Khu A, P520", "1 đơn • Tham gia 08/12/2024", "45K"),
    )

    // Lọc khách hàng theo type được chọn
    val filteredCustomers = if (selectedFilter == "Tất cả") {
        customers
    } else {
        customers.filter { it.type == selectedFilter }
    }

    val totalCustomers = customers.size
    val vipCustomers = customers.count { it.type == "VIP" }
    val regularCustomers = customers.count { it.type == "Thường xuyên" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // Header
        CustomerHeader()

        // Filter Tabs
        CustomerFilterTabs(selectedFilter = selectedFilter) { selectedFilter = it }

        // Statistics cards
        CustomerStats(totalCustomers, vipCustomers, regularCustomers)

        // Customer list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCustomers) { customer ->
                CustomerCard(customer)
            }
        }
    }
}
