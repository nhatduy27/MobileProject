package com.example.foodapp.pages.owner.customer

import com.example.foodapp.data.model.owner.Customer

/**
 * File này định nghĩa trạng thái giao diện (UI State) cho màn hình CustomerScreen.
 *
 * Nó sử dụng một 'data class' để đóng gói tất cả các thông tin cần thiết
 * để vẽ lên giao diện. Việc này giúp cho Composable (UI) trở nên "thụ động",
 * chỉ nhận dữ liệu và hiển thị, trong khi mọi logic xử lý được chuyển sang ViewModel.
 */
data class CustomerUiState(
    // Danh sách khách hàng sẽ được hiển thị trên màn hình
    val customers: List<Customer> = emptyList(),

    // Bộ lọc đang được chọn ("Tất cả", "VIP", ...)
    val selectedFilter: String = "Tất cả",

    val searchQuery: String = "",


    // Cờ báo hiệu màn hình đang trong quá trình tải dữ liệu
    val isLoading: Boolean = false,

    // Thông báo lỗi nếu có sự cố xảy ra
    val errorMessage: String? = null
)
