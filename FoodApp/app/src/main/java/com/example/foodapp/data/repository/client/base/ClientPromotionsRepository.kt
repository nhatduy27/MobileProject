package com.example.foodapp.data.repository.customer.base

import com.example.foodapp.data.model.shared.Promotion
import kotlinx.coroutines.flow.Flow

/**
 * Interface repository cho việc quản lý khuyến mãi cho người mua.
 * Xử lý lấy, kiểm tra và sử dụng mã giảm giá.
 */
interface ClientPromotionsRepository {

    /**
     * Lấy danh sách tất cả khuyến mãi đang có hiệu lực.
     * @return Flow phát ra danh sách các Promotion
     */
    fun getAvailablePromotions(): Flow<List<Promotion>>

    /**
     * Lấy khuyến mãi theo ID.
     * @param promotionId ID của khuyến mãi cần lấy
     * @return Flow phát ra thông tin khuyến mãi
     */
    fun getPromotionById(promotionId: String): Flow<Promotion>

    /**
     * Lấy khuyến mãi theo mã code.
     * @param code Mã khuyến mãi cần tìm
     * @return Flow phát ra thông tin khuyến mãi hoặc null nếu không tìm thấy
     */
    fun getPromotionByCode(code: String): Flow<Promotion?>

    /**
     * Kiểm tra tính hợp lệ của một mã khuyến mãi.
     * Kiểm tra thời hạn, số lượt sử dụng, điều kiện áp dụng.
     * @param code Mã khuyến mãi cần kiểm tra
     * @param cartTotal Tổng giá trị giỏ hàng
     * @return Result chứa Promotion nếu hợp lệ, hoặc thông báo lỗi
     */
    suspend fun validatePromotionCode(
        code: String,
        cartTotal: Double
    ): Result<Promotion>

    /**
     * Lấy danh sách khuyến mãi có thể áp dụng cho giỏ hàng hiện tại.
     * @param cartTotal Tổng giá trị giỏ hàng
     * @return Flow phát ra danh sách khuyến mãi phù hợp
     */
    fun getApplicablePromotions(cartTotal: Double): Flow<List<Promotion>>

    /**
     * Đánh dấu một khuyến mãi đã được sử dụng.
     * Tăng số lượt đã sử dụng của khuyến mãi.
     * @param promotionId ID của khuyến mãi đã sử dụng
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun markPromotionAsUsed(promotionId: String): Result<Unit>

    /**
     * Lấy lịch sử các khuyến mãi đã sử dụng bởi người dùng hiện tại.
     * @return Flow phát ra danh sách khuyến mãi đã dùng
     */
    fun getUsedPromotions(): Flow<List<Promotion>>

    /**
     * Lấy danh sách khuyến mãi theo người bán.
     * @param ownerId ID của người bán
     * @return Flow phát ra danh sách khuyến mãi của người bán đó
     */
    fun getPromotionsByOwner(ownerId: String): Flow<List<Promotion>>
}