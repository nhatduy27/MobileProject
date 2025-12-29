package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CustomerScreen(
    // Nhận ViewModel, sử dụng hàm viewModel() để tự động tạo và quản lý vòng đời.
    customerViewModel: CustomerViewModel = viewModel()
) {
    // Lắng nghe và lấy trạng thái mới nhất từ ViewModel.
    // Giao diện sẽ tự động cập nhật mỗi khi uiState thay đổi.
    val uiState by customerViewModel.uiState.collectAsState()

    // Lọc khách hàng dựa trên trạng thái từ ViewModel
    val filteredCustomers = if (uiState.selectedFilter == "Tất cả") {
        uiState.customers
    } else {
        uiState.customers.filter { it.type == uiState.selectedFilter }
    }

    // Tính toán các số liệu thống kê
    val totalCustomers = uiState.customers.size
    val vipCustomers = uiState.customers.count { it.type == "VIP" }
    val regularCustomers = uiState.customers.count { it.type == "Thường xuyên" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        CustomerHeader()

        // Truyền trạng thái và sự kiện vào các Composable con
        CustomerFilterTabs(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = { newFilter ->
                // Khi người dùng chọn filter, gọi hàm trong ViewModel
                customerViewModel.onFilterChanged(newFilter)
            }
        )

        CustomerStats(totalCustomers, vipCustomers, regularCustomers)

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
