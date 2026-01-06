package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.DeliveryHistory

/**
 * Interface cho History Repository của Shipper.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface ShipperHistoryRepository {
    
    /**
     * Lấy danh sách lịch sử giao hàng
     */
    fun getHistoryList(): List<DeliveryHistory>
}
