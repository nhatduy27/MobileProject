package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.Order
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho Orders Repository của Owner.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerOrdersRepository {
    
    /**
     * Lấy danh sách tất cả đơn hàng (sử dụng Flow để real-time update)
     */
    fun getOrders(): Flow<List<Order>>
    
    /**
     * Cập nhật trạng thái đơn hàng
     */
    fun updateOrderStatus(orderId: String, newStatus: com.example.foodapp.data.model.owner.OrderStatus)

    /**
     * Thêm đơn hàng mới
     */
    fun addOrder(order: com.example.foodapp.data.model.owner.Order)

    /**
     * Cập nhật đơn hàng (toàn bộ đối tượng)
     */
    fun updateOrder(updated: com.example.foodapp.data.model.owner.Order)
    
    /**
     * Xóa đơn hàng
     */
    fun deleteOrder(orderId: String)
}
