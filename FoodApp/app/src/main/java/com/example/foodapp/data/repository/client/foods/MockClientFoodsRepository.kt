package com.example.foodapp.data.repository.client.foods

import com.example.foodapp.data.model.shared.food.Food
import com.example.foodapp.data.repository.client.base.ClientFoodsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Mock implementation của ClientFoodsRepository cho mục đích testing và development.
 * Quản lý danh sách món ăn với dữ liệu giả lập trong bộ nhớ.
 */
class MockClientFoodsRepository : ClientFoodsRepository {

    // State flow để quản lý danh sách món ăn
    private val _foods = MutableStateFlow<List<Food>>(emptyList())

    init {
        // Khởi tạo dữ liệu món ăn mẫu khi repository được tạo
        initializeMockFoods()
    }

    /**
     * Khởi tạo danh sách món ăn mẫu.
     */
    private fun initializeMockFoods() {
        _foods.value = listOf(
            // --- THỨC ĂN ---
            Food(
                id = 1,
                name = "Phở Bò",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.5,
                reviewCount = 120,
                price = 45000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/492/300/200"
            ),
            Food(
                id = 2,
                name = "Bánh Mì Thịt",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.3,
                reviewCount = 85,
                price = 25000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/292/300/200"
            ),
            Food(
                id = 3,
                name = "Bún Chả",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.8,
                reviewCount = 95,
                price = 50000,
                isAvailable = false, // Hết hàng
                imageUrl = "https://picsum.photos/id/493/300/200"
            ),
            Food(
                id = 4,
                name = "Cơm Tấm Sườn",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.6,
                reviewCount = 110,
                price = 40000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/494/300/200"
            ),
            Food(
                id = 5,
                name = "Bánh Xèo",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.4,
                reviewCount = 75,
                price = 35000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/495/300/200"
            ),
            Food(
                id = 6,
                name = "Hủ Tiếu Nam Vang",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.2,
                reviewCount = 65,
                price = 38000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/496/300/200"
            ),
            Food(
                id = 7,
                name = "Mì Quảng",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.7,
                reviewCount = 88,
                price = 42000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/497/300/200"
            ),
            Food(
                id = 8,
                name = "Bánh Canh Cua",
                category = "Thức ăn",
                type = "Món chính",
                rating = 4.5,
                reviewCount = 72,
                price = 48000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/498/300/200"
            ),

            // --- NƯỚC UỐNG ---
            Food(
                id = 9,
                name = "Cà Phê Sữa Đá",
                category = "Nước uống",
                type = "Đồ uống",
                rating = 4.7,
                reviewCount = 200,
                price = 25000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/1060/300/200"
            ),
            Food(
                id = 10,
                name = "Trà Sữa Trân Châu",
                category = "Nước uống",
                type = "Đồ uống",
                rating = 4.6,
                reviewCount = 150,
                price = 35000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/61/300/200"
            ),
            Food(
                id = 11,
                name = "Nước Cam Ép",
                category = "Nước uống",
                type = "Đồ uống",
                rating = 4.4,
                reviewCount = 90,
                price = 30000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/1080/300/200"
            ),
            Food(
                id = 12,
                name = "Sinh Tố Bơ",
                category = "Nước uống",
                type = "Đồ uống",
                rating = 4.8,
                reviewCount = 120,
                price = 40000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/49/300/200"
            ),
            Food(
                id = 13,
                name = "Trà Đào",
                category = "Nước uống",
                type = "Đồ uống",
                rating = 4.5,
                reviewCount = 85,
                price = 32000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/50/300/200"
            ),

            // --- ĂN VẶT ---
            Food(
                id = 14,
                name = "Khoai Tây Chiên",
                category = "Ăn vặt",
                type = "Đồ ăn nhanh",
                rating = 4.2,
                reviewCount = 75,
                price = 30000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/0/300/200"
            ),
            Food(
                id = 15,
                name = "Bánh Tráng Trộn",
                category = "Ăn vặt",
                type = "Đồ ăn nhanh",
                rating = 4.6,
                reviewCount = 130,
                price = 20000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/1/300/200"
            ),
            Food(
                id = 16,
                name = "Xiên Que",
                category = "Ăn vặt",
                type = "Đồ ăn nhanh",
                rating = 4.3,
                reviewCount = 95,
                price = 15000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/2/300/200"
            ),
            Food(
                id = 17,
                name = "Bánh Flan",
                category = "Ăn vặt",
                type = "Tráng miệng",
                rating = 4.7,
                reviewCount = 110,
                price = 25000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/3/300/200"
            ),
            Food(
                id = 18,
                name = "Chè Thái",
                category = "Ăn vặt",
                type = "Tráng miệng",
                rating = 4.5,
                reviewCount = 80,
                price = 28000,
                isAvailable = true,
                imageUrl = "https://picsum.photos/id/4/300/200"
            )
        )
    }

    /**
     * READ: Lấy tất cả món ăn.
     * Phương thức này trả về một StateFlow, cho phép UI lắng nghe sự thay đổi dữ liệu theo thời gian thực.
     * @return Một StateFlow chứa danh sách món ăn.
     */
    override fun getFoods(): Flow<List<Food>> {
        return _foods.asStateFlow()
    }

    /**
     * Mô phỏng việc gọi API để lấy danh sách món ăn chỉ một lần.
     * Phương thức này sử dụng 'flow' builder để tạo ra một cold flow.
     * Thêm 'delay' để giả lập độ trễ mạng.
     * @return Một Flow chỉ phát ra dữ liệu một lần.
     */
    fun fetchFoodsFromApi(): Flow<List<Food>> = flow {
        // Giả lập độ trễ mạng
        delay(800)
        // Phát ra giá trị hiện tại của danh sách
        emit(_foods.value)
    }

    /**
     * READ: Lấy danh sách món ăn theo danh mục.
     * @param category Tên danh mục cần lọc.
     * @return Flow chứa danh sách món ăn thuộc danh mục.
     */
    override fun getFoodsByCategory(category: String): Flow<List<Food>> {
        return _foods.map { foods ->
            foods.filter { it.category == category }
        }
    }

    /**
     * READ: Tìm kiếm món ăn theo từ khóa.
     * @param query Từ khóa tìm kiếm.
     * @return Flow chứa danh sách món ăn khớp với từ khóa.
     */
    override fun searchFoods(query: String): Flow<List<Food>> {
        val lowerQuery = query.lowercase()
        return _foods.map { foods ->
            foods.filter {
                it.name.lowercase().contains(lowerQuery) ||
                        it.type.lowercase().contains(lowerQuery) ||
                        it.category.lowercase().contains(lowerQuery)
            }
        }
    }

    /**
     * READ: Lọc món ăn theo nhiều tiêu chí.
     * @param minPrice Giá tối thiểu.
     * @param maxPrice Giá tối đa.
     * @param category Danh mục.
     * @return Flow chứa danh sách món ăn đã lọc.
     */
    override fun filterFoods(
        minPrice: Double?,
        maxPrice: Double?,
        category: String?
    ): Flow<List<Food>> {
        return _foods.map { foods ->
            foods.filter { food ->
                // Lọc theo category
                val categoryMatch = category == null || food.category == category

                // Lọc theo minPrice
                val minPriceMatch = minPrice == null || food.price >= minPrice.toInt()

                // Lọc theo maxPrice
                val maxPriceMatch = maxPrice == null || food.price <= maxPrice.toInt()

                categoryMatch && minPriceMatch && maxPriceMatch
            }
        }
    }

    /**
     * READ: Lấy chi tiết của một món ăn cụ thể.
     * @param foodId ID của món ăn cần lấy chi tiết.
     * @return Flow chứa thông tin chi tiết của món ăn.
     */
    override fun getFoodDetails(foodId: String): Flow<Food> {
        return _foods.map { foods ->
            foods.find { it.id == foodId.toInt() }
                ?: throw IllegalArgumentException("Món ăn không tồn tại")
        }
    }

    /**
     * READ: Lấy danh sách món ăn được gợi ý.
     * @return Flow chứa danh sách món ăn gợi ý.
     */
    override fun getRecommendedFoods(): Flow<List<Food>> {
        return _foods.map { foods ->
            // Giả lập gợi ý: 5 món có rating cao nhất và còn hàng
            foods.filter { it.isAvailable }
                .sortedByDescending { it.rating }
                .take(5)
        }
    }

    /**
     * READ: Lấy danh sách món ăn phổ biến.
     * @param limit Số lượng món ăn tối đa trả về.
     * @return Flow chứa danh sách món ăn phổ biến.
     */
    override fun getPopularFoods(limit: Int): Flow<List<Food>> {
        return _foods.map { foods ->
            // Giả lập phổ biến: nhiều review nhất
            foods.filter { it.isAvailable }
                .sortedByDescending { it.reviewCount }
                .take(limit)
        }
    }

    /**
     * READ: Tìm một món ăn theo ID.
     * @param id ID của món ăn cần tìm.
     * @return Món ăn tìm thấy hoặc null.
     */
    fun getFoodById(id: Int): Food? {
        return _foods.value.find { it.id == id }
    }

    /**
     * UPDATE: Cập nhật trạng thái có sẵn của món ăn.
     * @param foodId ID của món ăn cần cập nhật.
     * @param isAvailable Trạng thái mới.
     */
    fun updateFoodAvailability(foodId: Int, isAvailable: Boolean) {
        _foods.update { currentList ->
            currentList.map { food ->
                if (food.id == foodId) {
                    food.copy(isAvailable = isAvailable)
                } else {
                    food
                }
            }
        }
    }
}