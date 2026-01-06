package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.PeriodRevenueData

/**
 * Interface cho Revenue Repository của Owner.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerRevenueRepository {
    
    /**
     * Lấy dữ liệu doanh thu theo khoảng thời gian
     * @param period "Hôm nay", "Tuần này", "Tháng này", "Năm nay"
     */
    fun getRevenueData(period: String): PeriodRevenueData?

    /**
     * Lấy danh sách các khoảng thời gian có sẵn (chuỗi hiển thị)
     */
    fun getAvailablePeriods(): List<String>
}
