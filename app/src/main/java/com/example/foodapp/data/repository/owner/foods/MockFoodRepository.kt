package com.example.foodapp.data.repository.owner.foods

import com.example.foodapp.data.model.owner.Food
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository chứa dữ liệu mẫu (mock data) cho danh sách món ăn.
 * 
 * Nhiệm vụ của Repository:
 * 1. Cung cấp nguồn dữ liệu thống nhất cho ViewModel
 * 2. Quản lý việc thêm, sửa, xóa món ăn
 * 3. Phát ra Flow để ViewModel lắng nghe khi dữ liệu thay đổi
 * 4. Sau này có thể thay thế bằng API hoặc Database mà không ảnh hưởng tới ViewModel
 */
class MockFoodRepository {

    // StateFlow nội bộ chứa danh sách món ăn
    private val _internalFoodsFlow = MutableStateFlow<List<Food>>(emptyList())

    init {
        // Khởi tạo dữ liệu mẫu khi repository được tạo
        _internalFoodsFlow.value = listOf(
            // --- MÓN CƠM ---
            Food(1, "Cơm gà xối mỡ", "Cơm", "Món chính", 4.8, 156, 45000, true),
            Food(5, "Cơm tấm sườn bì", "Cơm", "Món chính", 4.5, 89, 40000, false),
            Food(9, "Cơm chiên dương châu", "Cơm", "Món chính", 4.6, 112, 42000, true),
            Food(10, "Cơm rang thập cẩm", "Cơm", "Món chính", 4.4, 98, 38000, true),
            Food(11, "Cơm gà teriyaki", "Cơm", "Món chính", 4.7, 134, 48000, true),
            Food(12, "Cơm sườn nướng", "Cơm", "Món chính", 4.6, 121, 43000, true),

            // --- PHỞ/BÚN ---
            Food(2, "Phở bò", "Phở/Bún", "Món chính", 4.7, 203, 50000, true),
            Food(3, "Bún chả Hà Nội", "Phở/Bún", "Món chính", 4.9, 178, 55000, true),
            Food(13, "Bún bò Huế", "Phở/Bún", "Món chính", 4.8, 145, 52000, true),
            Food(14, "Bún riêu cua", "Phở/Bún", "Món chính", 4.5, 102, 48000, true),
            Food(15, "Phở gà", "Phở/Bún", "Món chính", 4.6, 156, 48000, true),
            Food(16, "Bún thịt nướng", "Phở/Bún", "Món chính", 4.7, 167, 50000, false),

            // --- ĐỒ UỐNG ---
            Food(4, "Trà sữa trân châu", "Đồ uống", "Thức uống", 4.6, 142, 25000, true),
            Food(6, "Cafe sữa đá", "Đồ uống", "Thức uống", 4.4, 95, 20000, true),
            Food(17, "Cafe đen đá", "Đồ uống", "Thức uống", 4.3, 87, 18000, true),
            Food(18, "Trà chanh", "Đồ uống", "Thức uống", 4.2, 76, 15000, true),
            Food(19, "Nước ép dưa hấu", "Đồ uống", "Thức uống", 4.5, 91, 22000, true),
            Food(20, "Sinh tố bơ", "Đồ uống", "Thức uống", 4.6, 103, 28000, true),
            Food(21, "Trà đào cam sả", "Đồ uống", "Thức uống", 4.7, 128, 30000, true),

            // --- ĂN VẶT ---
            Food(7, "Bánh mì thịt nướng", "Ăn vặt", "Ăn vặt", 4.3, 67, 30000, true),
            Food(8, "Gỏi cuốn tôm thịt", "Ăn vặt", "Ăn vặt", 4.6, 120, 35000, true),
            Food(22, "Nem rán", "Ăn vặt", "Ăn vặt", 4.5, 98, 32000, true),
            Food(23, "Bánh xèo", "Ăn vặt", "Ăn vặt", 4.7, 115, 38000, true),
            Food(24, "Bánh bao chiên", "Ăn vặt", "Ăn vặt", 4.4, 82, 25000, true),
            Food(25, "Chả giò", "Ăn vặt", "Ăn vặt", 4.6, 107, 35000, false),
        )
    }

    /**
     * Trả về Flow để ViewModel lắng nghe sự thay đổi của danh sách món ăn
     */
    fun getFoods(): Flow<List<Food>> = _internalFoodsFlow.asStateFlow()

    /**
     * Thêm món ăn mới vào danh sách
     */
    fun addFood(food: Food) {
        _internalFoodsFlow.update { currentList ->
            currentList + food
        }
    }

    /**
     * Cập nhật thông tin món ăn
     */
    fun updateFood(food: Food) {
        _internalFoodsFlow.update { currentList ->
            currentList.map { if (it.id == food.id) food else it }
        }
    }

    /**
     * Xóa món ăn khỏi danh sách
     */
    fun deleteFood(foodId: Int) {
        _internalFoodsFlow.update { currentList ->
            currentList.filter { it.id != foodId }
        }
    }

    /**
     * Cập nhật trạng thái còn hàng/hết hàng của món ăn
     */
    fun toggleFoodAvailability(foodId: Int) {
        _internalFoodsFlow.update { currentList ->
            currentList.map { 
                if (it.id == foodId) it.copy(isAvailable = !it.isAvailable) 
                else it 
            }
        }
    }
}
