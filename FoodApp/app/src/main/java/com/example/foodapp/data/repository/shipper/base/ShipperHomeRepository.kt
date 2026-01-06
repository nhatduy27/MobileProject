package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.DeliveryTask
import com.example.foodapp.data.model.shipper.ShipperStats

/**
 * Interface cho Home Repository của Shipper.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface ShipperHomeRepository {
    
    /**
     * Lấy thống kê nhanh của shipper (đơn hôm nay, thu nhập, tỷ lệ hoàn thành, rating)
     */
    fun getStats(): ShipperStats
    
    /**
     * Lấy danh sách các task giao hàng hiện tại
     */
    fun getTasks(): List<DeliveryTask>
}
