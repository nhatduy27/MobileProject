package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.Food
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho Foods Repository của Owner.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerFoodsRepository {
    
    /**
     * Lấy danh sách tất cả món ăn (sử dụng Flow để real-time update)
     */
    fun getFoods(): Flow<List<Food>>
    
    /**
     * Thêm món ăn mới
     */
    fun addFood(food: Food)
    
    /**
     * Cập nhật thông tin món ăn
     */
    fun updateFood(updated: Food)
    
    /**
     * Xóa món ăn
     */
    fun deleteFood(foodId: Int)

    /**
     * Toggle trạng thái còn hàng/hết hàng của món ăn
     */
    fun toggleFoodAvailability(foodId: Int)
}
