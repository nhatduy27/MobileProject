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
        _internalFoodsFlow.value = listOf(
            // --- CƠM (10 món) ---
            Food(1, "Cơm gà xối mỡ", "Cơm", "Món chính", 4.8, 156, 45000, true, "https://picsum.photos/seed/food1/400/400"),
            Food(2, "Cơm tấm sườn bì", "Cơm", "Món chính", 4.5, 89, 40000, true, "https://picsum.photos/seed/food2/400/400"),
            Food(3, "Cơm chiên dương châu", "Cơm", "Món chính", 4.6, 112, 42000, true, "https://picsum.photos/seed/food3/400/400"),
            Food(4, "Cơm rang thập cẩm", "Cơm", "Món chính", 4.4, 98, 38000, true, "https://picsum.photos/seed/food4/400/400"),
            Food(5, "Cơm gà teriyaki", "Cơm", "Món chính", 4.7, 134, 48000, true, "https://picsum.photos/seed/food5/400/400"),
            Food(6, "Cơm sườn nướng", "Cơm", "Món chính", 4.6, 121, 43000, true, "https://picsum.photos/seed/food6/400/400"),
            Food(7, "Cơm niêu Singapore", "Cơm", "Món chính", 4.3, 56, 55000, true, "https://picsum.photos/seed/food7/400/400"),
            Food(8, "Cơm văn phòng kho tộ", "Cơm", "Món chính", 4.2, 45, 35000, true, "https://picsum.photos/seed/food8/400/400"),
            Food(9, "Cơm bò lúc lắc", "Cơm", "Món chính", 4.9, 210, 60000, true, "https://picsum.photos/seed/food9/400/400"),
            Food(10, "Cơm cà ri gà", "Cơm", "Món chính", 4.5, 78, 45000, false, "https://picsum.photos/seed/food10/400/400"),

            // --- PHỞ / BÚN / MÌ (10 món) ---
            Food(11, "Phở bò tái nạm", "Phở/Bún", "Món chính", 4.7, 203, 50000, true, "https://picsum.photos/seed/food11/400/400"),
            Food(12, "Bún chả Hà Nội", "Phở/Bún", "Món chính", 4.9, 178, 55000, true, "https://picsum.photos/seed/food12/400/400"),
            Food(13, "Bún bò Huế", "Phở/Bún", "Món chính", 4.8, 145, 52000, true, "https://picsum.photos/seed/food13/400/400"),
            Food(14, "Bún riêu cua", "Phở/Bún", "Món chính", 4.5, 102, 48000, true, "https://picsum.photos/seed/food14/400/400"),
            Food(15, "Phở gà ta", "Phở/Bún", "Món chính", 4.6, 156, 48000, true, "https://picsum.photos/seed/food15/400/400"),
            Food(16, "Bún thịt nướng", "Phở/Bún", "Món chính", 4.7, 167, 50000, false, "https://picsum.photos/seed/food16/400/400"),
            Food(17, "Hủ tiếu Nam Vang", "Phở/Bún", "Món chính", 4.4, 110, 45000, true, "https://picsum.photos/seed/food17/400/400"),
            Food(18, "Mì Quảng", "Phở/Bún", "Món chính", 4.6, 95, 40000, true, "https://picsum.photos/seed/food18/400/400"),
            Food(19, "Bánh canh cua", "Phở/Bún", "Món chính", 4.8, 130, 65000, true, "https://picsum.photos/seed/food19/400/400"),
            Food(20, "Mì xào bò", "Phở/Bún", "Món chính", 4.3, 85, 45000, true, "https://picsum.photos/seed/food20/400/400"),

            // --- ĐỒ UỐNG (12 món) ---
            Food(21, "Trà sữa trân châu", "Đồ uống", "Thức uống", 4.6, 142, 25000, true, "https://picsum.photos/seed/food21/400/400"),
            Food(22, "Cafe sữa đá", "Đồ uống", "Thức uống", 4.4, 95, 20000, true, "https://picsum.photos/seed/food22/400/400"),
            Food(23, "Cafe đen đá", "Đồ uống", "Thức uống", 4.3, 87, 18000, true, "https://picsum.photos/seed/food23/400/400"),
            Food(24, "Trà chanh", "Đồ uống", "Thức uống", 4.2, 76, 15000, true, "https://picsum.photos/seed/food24/400/400"),
            Food(25, "Nước ép dưa hấu", "Đồ uống", "Thức uống", 4.5, 91, 22000, true, "https://picsum.photos/seed/food25/400/400"),
            Food(26, "Sinh tố bơ", "Đồ uống", "Thức uống", 4.6, 103, 28000, true, "https://picsum.photos/seed/food26/400/400"),
            Food(27, "Trà đào cam sả", "Đồ uống", "Thức uống", 4.7, 128, 30000, true, "https://picsum.photos/seed/food27/400/400"),
            Food(28, "Sữa tươi trân châu", "Đồ uống", "Thức uống", 4.8, 200, 35000, true, "https://picsum.photos/seed/food28/400/400"),
            Food(29, "Trà vải", "Đồ uống", "Thức uống", 4.5, 60, 30000, true, "https://picsum.photos/seed/food29/400/400"),
            Food(30, "Mojito chanh dây", "Đồ uống", "Thức uống", 4.6, 75, 32000, true, "https://picsum.photos/seed/food30/400/400"),
            Food(31, "Nước cam ép", "Đồ uống", "Thức uống", 4.4, 88, 25000, true, "https://picsum.photos/seed/food31/400/400"),
            Food(32, "Sinh tố dâu", "Đồ uống", "Thức uống", 4.7, 92, 30000, true, "https://picsum.photos/seed/food32/400/400"),

            // --- ĂN VẶT / KHAI VỊ (12 món) ---
            Food(33, "Bánh mì thịt nướng", "Ăn vặt", "Ăn vặt", 4.3, 67, 30000, true, "https://picsum.photos/seed/food33/400/400"),
            Food(34, "Gỏi cuốn tôm thịt", "Ăn vặt", "Ăn vặt", 4.6, 120, 35000, true, "https://picsum.photos/seed/food34/400/400"),
            Food(35, "Nem rán Hà Nội", "Ăn vặt", "Ăn vặt", 4.5, 98, 32000, true, "https://picsum.photos/seed/food35/400/400"),
            Food(36, "Bánh xèo miền Tây", "Ăn vặt", "Ăn vặt", 4.7, 115, 38000, true, "https://picsum.photos/seed/food36/400/400"),
            Food(37, "Bánh bao chiên", "Ăn vặt", "Ăn vặt", 4.4, 82, 25000, true, "https://picsum.photos/seed/food37/400/400"),
            Food(38, "Chả giò rế", "Ăn vặt", "Ăn vặt", 4.6, 107, 35000, false, "https://picsum.photos/seed/food38/400/400"),
            Food(39, "Khoai tây chiên", "Ăn vặt", "Ăn vặt", 4.2, 230, 25000, true, "https://picsum.photos/seed/food39/400/400"),
            Food(40, "Gà rán giòn", "Ăn vặt", "Ăn vặt", 4.5, 180, 35000, true, "https://picsum.photos/seed/food40/400/400"),
            Food(41, "Xúc xích nướng", "Ăn vặt", "Ăn vặt", 4.0, 50, 15000, true, "https://picsum.photos/seed/food41/400/400"),
            Food(42, "Bánh tráng trộn", "Ăn vặt", "Ăn vặt", 4.8, 300, 20000, true, "https://picsum.photos/seed/food42/400/400"),
            Food(43, "Cá viên chiên", "Ăn vặt", "Ăn vặt", 4.3, 110, 25000, true, "https://picsum.photos/seed/food43/400/400"),
            Food(44, "Chân gà sả tắc", "Ăn vặt", "Ăn vặt", 4.7, 140, 50000, true, "https://picsum.photos/seed/food44/400/400"),

            // --- TRÁNG MIỆNG (6 món) ---
            Food(45, "Chè thái sầu riêng", "Tráng miệng", "Tráng miệng", 4.8, 90, 35000, true, "https://picsum.photos/seed/food45/400/400"),
            Food(46, "Sữa chua nếp cẩm", "Tráng miệng", "Tráng miệng", 4.6, 80, 25000, true, "https://picsum.photos/seed/food46/400/400"),
            Food(47, "Kem bơ dừa", "Tráng miệng", "Tráng miệng", 4.7, 110, 30000, true, "https://picsum.photos/seed/food47/400/400"),
            Food(48, "Bánh flan caramen", "Tráng miệng", "Tráng miệng", 4.5, 150, 15000, true, "https://picsum.photos/seed/food48/400/400"),
            Food(49, "Rau câu dừa", "Tráng miệng", "Tráng miệng", 4.4, 70, 20000, true, "https://picsum.photos/seed/food49/400/400"),
            Food(50, "Tàu hũ nước đường", "Tráng miệng", "Tráng miệng", 4.3, 65, 15000, true, "https://picsum.photos/seed/food50/400/400")
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
