package com.example.foodapp.pages.owner.foods

import com.example.foodapp.data.model.owner.Food

/**
 * File này định nghĩa trạng thái giao diện (UI State) cho màn hình FoodsScreen.
 *
 * Nó sử dụng một 'data class' để đóng gói tất cả các thông tin cần thiết
 * để vẽ lên giao diện. Việc này giúp cho Composable (UI) trở nên "thụ động",
 * chỉ nhận dữ liệu và hiển thị, trong khi mọi logic xử lý được chuyển sang ViewModel.
 */
data class FoodUiState(
    // Danh sách món ăn sẽ được hiển thị trên màn hình
    val foods: List<Food> = emptyList(),

    // Bộ lọc category đang được chọn ("Tất cả", "Cơm", "Phở/Bún", ...)
    val selectedCategory: String = "Tất cả",

    // Cờ báo hiệu màn hình đang trong quá trình tải dữ liệu
    val isLoading: Boolean = false,

    // Thông báo lỗi nếu có sự cố xảy ra
    val errorMessage: String? = null,

    // Hiển thị dialog thêm món ăn
    val showAddDialog: Boolean = false,

    // Query tìm kiếm món ăn theo tên
    val searchQuery: String = ""
)
