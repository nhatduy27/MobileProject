package com.example.foodapp.data.repository.client.base


import com.example.foodapp.data.model.shared.food.Food
import kotlinx.coroutines.flow.Flow

/**
 * Interface repository cho việc tương tác với danh sách món ăn.
 * Xử lý lấy, tìm kiếm, lọc và gợi ý món ăn cho người mua.
 */
interface ClientFoodsRepository {

    /**
     * Lấy danh sách tất cả món ăn đang có sẵn.
     * Sử dụng Flow để cập nhật real-time khi có thay đổi.
     * @return Flow phát ra danh sách các món ăn
     */
    fun getFoods(): Flow<List<Food>>

    /**
     * Lấy danh sách món ăn theo danh mục.
     * @param category Tên danh mục (ví dụ: "Thức ăn", "Nước uống", "Ăn vặt")
     * @return Flow phát ra danh sách món ăn thuộc danh mục
     */
    fun getFoodsByCategory(category: String): Flow<List<Food>>

    /**
     * Tìm kiếm món ăn theo từ khóa.
     * @param query Từ khóa tìm kiếm (tên món ăn, mô tả)
     * @return Flow phát ra danh sách món ăn khớp với từ khóa
     */
    fun searchFoods(query: String): Flow<List<Food>>

    /**
     * Lọc món ăn theo nhiều tiêu chí.
     * @param minPrice Giá tối thiểu (có thể null nếu không giới hạn)
     * @param maxPrice Giá tối đa (có thể null nếu không giới hạn)
     * @param category Danh mục (có thể null nếu lấy tất cả)
     * @return Flow phát ra danh sách món ăn đã lọc
     */
    fun filterFoods(
        minPrice: Double? = null,
        maxPrice: Double? = null,
        category: String? = null
    ): Flow<List<Food>>

    /**
     * Lấy chi tiết của một món ăn cụ thể.
     * @param foodId ID của món ăn cần lấy chi tiết
     * @return Flow phát ra thông tin chi tiết của món ăn
     */
    fun getFoodDetails(foodId: String): Flow<Food>

    /**
     * Lấy danh sách món ăn được gợi ý cho người dùng.
     * Dựa trên lịch sử mua hàng, khung giờ, và sở thích.
     * @return Flow phát ra danh sách món ăn gợi ý
     */
    fun getRecommendedFoods(): Flow<List<Food>>

    /**
     * Lấy danh sách món ăn phổ biến (được đặt nhiều nhất).
     * @param limit Số lượng món ăn tối đa trả về
     * @return Flow phát ra danh sách món ăn phổ biến
     */
    fun getPopularFoods(limit: Int = 10): Flow<List<Food>>
}