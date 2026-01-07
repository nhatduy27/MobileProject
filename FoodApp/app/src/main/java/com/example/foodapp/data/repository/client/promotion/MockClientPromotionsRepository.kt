package com.example.foodapp.data.repository.client.promotion

import com.example.foodapp.data.model.shared.DiscountType
import com.example.foodapp.data.model.shared.food.Food
import com.example.foodapp.data.model.shared.Promotion
import com.example.foodapp.data.repository.customer.base.ClientPromotionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.Calendar

/**
 * Mock implementation của ClientPromotionsRepository cho mục đích testing và development.
 * Quản lý khuyến mãi với dữ liệu giả lập trong bộ nhớ.
 */
class MockClientPromotionsRepository : ClientPromotionsRepository {

    // State flow để quản lý danh sách khuyến mãi
    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    private val _usedPromotions = MutableStateFlow<Set<String>>(emptySet())

    init {
        // Khởi tạo dữ liệu khuyến mãi mẫu
        initializeMockPromotions()
    }

    /**
     * Khởi tạo danh sách khuyến mãi mẫu.
     */
    private fun initializeMockPromotions() {
        val calendar = Calendar.getInstance()

        // Khuyến mãi còn hiệu lực
        val activePromotions = listOf(
            Promotion(
                id = "promo_001",
                code = "WELCOME20",
                name = "Giảm 20% cho lần đầu đặt hàng",
                description = "Áp dụng cho tất cả đơn hàng đầu tiên",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 20.0,
                minOrderAmount = 0.0,
                maxDiscount = 50000.0,
                applicableFoodIds = null,
                totalUses = 1000,
                usedCount = 350,
                startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -30) }.time,
                endDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 60) }.time,
                ownerId = "", // Áp dụng cho tất cả
                isActive = true
            ),
            Promotion(
                id = "promo_002",
                code = "FOODIE50K",
                name = "Giảm 50K cho đơn từ 200K",
                description = "Áp dụng cho đơn hàng thức ăn từ 200.000đ",
                discountType = DiscountType.FIXED_AMOUNT,
                discountValue = 50000.0,
                minOrderAmount = 200000.0,
                maxDiscount = null,
                applicableFoodIds = null,
                totalUses = 500,
                usedCount = 120,
                startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -15) }.time,
                endDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 15) }.time,
                ownerId = "owner_001",
                isActive = true
            ),
            Promotion(
                id = "promo_003",
                code = "DRINK30",
                name = "Giảm 30% đồ uống",
                description = "Áp dụng cho tất cả đồ uống",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 30.0,
                minOrderAmount = 50000.0,
                maxDiscount = 30000.0,
                applicableFoodIds = null,
                totalUses = 300,
                usedCount = 45,
                startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -7) }.time,
                endDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 23) }.time,
                ownerId = "owner_003",
                isActive = true
            ),
            Promotion(
                id = "promo_004",
                code = "FREESHIP",
                name = "Miễn phí vận chuyển",
                description = "Miễn phí vận chuyển cho đơn từ 150K",
                discountType = DiscountType.FIXED_AMOUNT,
                discountValue = 20000.0, // Giả sử phí ship là 20K
                minOrderAmount = 150000.0,
                maxDiscount = null,
                applicableFoodIds = null,
                totalUses = 200,
                usedCount = 80,
                startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -10) }.time,
                endDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 20) }.time,
                ownerId = "owner_002",
                isActive = true
            )
        )

        // Khuyến mãi hết hạn (cho testing)
        val expiredPromotions = listOf(
            Promotion(
                id = "promo_expired",
                code = "OLDCODE",
                name = "Khuyến mãi cũ",
                description = "Đã hết hạn",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10.0,
                minOrderAmount = 0.0,
                totalUses = 100,
                usedCount = 100,
                startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -60) }.time,
                endDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -30) }.time,
                ownerId = "owner_001",
                isActive = false
            )
        )

        _promotions.value = activePromotions + expiredPromotions
    }

    override fun getAvailablePromotions(): Flow<List<Promotion>> {
        val now = Date()
        return _promotions.map { promotions ->
            promotions.filter { promotion ->
                promotion.isActive &&
                        promotion.isValid() &&
                        now.after(promotion.startDate) &&
                        now.before(promotion.endDate)
            }
        }
    }

    override fun getPromotionById(promotionId: String): Flow<Promotion> {
        return _promotions.map { promotions ->
            promotions.find { it.id == promotionId }
                ?: throw IllegalArgumentException("Khuyến mãi không tồn tại")
        }
    }

    override fun getPromotionByCode(code: String): Flow<Promotion?> {
        return _promotions.map { promotions ->
            promotions.find { it.code.equals(code, ignoreCase = true) }
        }
    }

    override suspend fun validatePromotionCode(
        code: String,
        cartTotal: Double
    ): Result<Promotion> {
        val promotion = _promotions.value.find {
            it.code.equals(code, ignoreCase = true)
        }

        if (promotion == null) {
            return Result.failure(Exception("Mã khuyến mãi không tồn tại"))
        }

        if (!promotion.isActive) {
            return Result.failure(Exception("Khuyến mãi không còn hiệu lực"))
        }

        val now = Date()
        if (now.before(promotion.startDate)) {
            return Result.failure(Exception("Khuyến mãi chưa có hiệu lực"))
        }

        if (now.after(promotion.endDate)) {
            return Result.failure(Exception("Khuyến mãi đã hết hạn"))
        }

        if (promotion.usedCount >= promotion.totalUses) {
            return Result.failure(Exception("Khuyến mãi đã hết lượt sử dụng"))
        }

        if (cartTotal < promotion.minOrderAmount) {
            return Result.failure(
                Exception("Đơn hàng tối thiểu ${promotion.minOrderAmount.toInt()}đ để áp dụng")
            )
        }

        if (_usedPromotions.value.contains(promotion.id)) {
            return Result.failure(Exception("Bạn đã sử dụng khuyến mãi này rồi"))
        }

        return Result.success(promotion)
    }

    override fun getApplicablePromotions(cartTotal: Double): Flow<List<Promotion>> {
        val now = Date()
        return _promotions.map { promotions ->
            promotions.filter { promotion ->
                promotion.isActive &&
                        promotion.isValid() &&
                        now.after(promotion.startDate) &&
                        now.before(promotion.endDate) &&
                        cartTotal >= promotion.minOrderAmount &&
                        !_usedPromotions.value.contains(promotion.id)
            }
        }
    }

    override suspend fun markPromotionAsUsed(promotionId: String): Result<Unit> {
        val index = _promotions.value.indexOfFirst { it.id == promotionId }

        if (index == -1) {
            return Result.failure(Exception("Khuyến mãi không tồn tại"))
        }

        val promotion = _promotions.value[index]

        if (promotion.usedCount >= promotion.totalUses) {
            return Result.failure(Exception("Khuyến mãi đã hết lượt sử dụng"))
        }

        val updatedPromotion = promotion.copy(usedCount = promotion.usedCount + 1)
        val updatedPromotions = _promotions.value.toMutableList()
        updatedPromotions[index] = updatedPromotion

        _promotions.value = updatedPromotions
        _usedPromotions.value = _usedPromotions.value + promotionId

        return Result.success(Unit)
    }

    override fun getUsedPromotions(): Flow<List<Promotion>> {
        return _promotions.map { promotions ->
            promotions.filter { promotion ->
                _usedPromotions.value.contains(promotion.id)
            }
        }
    }

    override fun getPromotionsByOwner(ownerId: String): Flow<List<Promotion>> {
        val now = Date()
        return _promotions.map { promotions ->
            promotions.filter { promotion ->
                promotion.ownerId == ownerId &&
                        promotion.isActive &&
                        promotion.isValid() &&
                        now.after(promotion.startDate) &&
                        now.before(promotion.endDate)
            }
        }
    }
}