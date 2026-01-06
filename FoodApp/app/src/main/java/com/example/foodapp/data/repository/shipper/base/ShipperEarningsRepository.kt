package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.EarningsData

/**
 * Interface cho Earnings Repository của Shipper.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface ShipperEarningsRepository {
    
    /**
     * Lấy toàn bộ lịch sử thu nhập của shipper
     */
    fun getAllEarningsHistory(): List<EarningsData>
}
