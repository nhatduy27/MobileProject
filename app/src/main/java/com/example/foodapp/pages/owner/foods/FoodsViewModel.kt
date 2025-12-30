package com.example.foodapp.pages.owner.foods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.owner.Food
import com.example.foodapp.data.repository.owner.foods.MockFoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * File này định nghĩa ViewModel cho màn hình FoodsScreen.
 *
 * ViewModel đóng vai trò là "bộ não" của màn hình, có các nhiệm vụ chính:
 * 1. Giao tiếp với lớp Repository (nguồn dữ liệu) để lấy và cập nhật dữ liệu.
 * 2. Giữ và quản lý trạng thái giao diện (FoodUiState) trong vòng đời của màn hình,
 *    giúp trạng thái không bị mất khi xoay màn hình hay có thay đổi cấu hình.
 * 3. Cung cấp các hàm để xử lý sự kiện từ người dùng (ví dụ: thay đổi bộ lọc, thêm món).
 * 4. Tách biệt logic nghiệp vụ khỏi tầng giao diện (Composable).
 */
class FoodsViewModel : ViewModel() {

    // Khởi tạo Repository. Sau này có thể inject bằng Hilt/Dagger.
    private val foodRepository = MockFoodRepository()

    // StateFlow nội bộ, chỉ ViewModel mới có quyền chỉnh sửa.
    private val _uiState = MutableStateFlow(FoodUiState())

    // StateFlow công khai, chỉ cho phép đọc từ bên ngoài (UI).
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    // Danh sách các category có thể lọc
    val categories = listOf("Tất cả", "Cơm", "Phở/Bún", "Đồ uống", "Ăn vặt")

    init {
        // Ngay khi ViewModel được tạo, bắt đầu lắng nghe sự thay đổi từ Repository.
        loadFoods()
    }

    /**
     * Lắng nghe luồng dữ liệu món ăn từ Repository và cập nhật UI State.
     */
    private fun loadFoods() {
        // viewModelScope tự động hủy coroutine khi ViewModel bị hủy, tránh rò rỉ bộ nhớ.
        viewModelScope.launch {
            // Bắt đầu lắng nghe Flow từ repository
            foodRepository.getFoods().collect { foods ->
                // Cập nhật StateFlow với danh sách món ăn mới nhất.
                _uiState.update { currentState ->
                    currentState.copy(foods = foods)
                }
            }
        }
    }

    /**
     * Lọc danh sách món ăn theo category được chọn.
     * Computed property, tự động tính toán lại khi foods hoặc selectedCategory thay đổi.
     */
    fun getFilteredFoods(): List<Food> {
        val currentState = _uiState.value

        // Lọc theo category trước
        val categoryFiltered = if (currentState.selectedCategory == "Tất cả") {
            currentState.foods
        } else {
            currentState.foods.filter { it.category == currentState.selectedCategory }
        }

        // Sau đó lọc tiếp theo query tìm kiếm (tên món)
        val query = currentState.searchQuery.trim()
        return if (query.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    /**
     * Lấy tổng số món ăn
     */
    fun getTotalFoods(): Int = _uiState.value.foods.size

    /**
     * Lấy số món còn hàng
     */
    fun getAvailableFoods(): Int = _uiState.value.foods.count { it.isAvailable }

    /**
     * Lấy số món hết hàng
     */
    fun getOutOfStockFoods(): Int = _uiState.value.foods.count { !it.isAvailable }

    /**
     * Người dùng chọn một bộ lọc category mới.
     */
    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Cập nhật nội dung query tìm kiếm món ăn.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    /**
     * Hiển thị dialog thêm món ăn
     */
    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    /**
     * Ẩn dialog thêm món ăn
     */
    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    /**
     * Thêm món ăn mới
     */
    fun addFood(food: Food) {
        foodRepository.addFood(food)
        hideAddDialog()
    }

    /**
     * Cập nhật thông tin món ăn
     */
    fun updateFood(food: Food) {
        foodRepository.updateFood(food)
    }

    /**
     * Xóa món ăn
     */
    fun deleteFood(foodId: Int) {
        foodRepository.deleteFood(foodId)
    }

    /**
     * Toggle trạng thái còn hàng/hết hàng
     */
    fun toggleFoodAvailability(foodId: Int) {
        foodRepository.toggleFoodAvailability(foodId)
    }
}
