package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.DashboardDayRevenue
import com.example.foodapp.data.model.owner.DashboardRecentOrder
import com.example.foodapp.data.model.owner.DashboardStat
import com.example.foodapp.data.model.owner.DashboardTopProduct

/**
 * Interface cho Dashboard Repository của Owner.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerDashboardRepository {
    
    /**
     * Lấy danh sách thống kê nhanh (tổng đơn, doanh thu, đơn đang giao, món bán chạy)
     */
    fun getStats(): List<DashboardStat>
    
    /**
     * Lấy doanh thu theo tuần
     */
    fun getWeeklyRevenue(): List<DashboardDayRevenue>
    
    /**
     * Lấy danh sách đơn hàng gần đây
     */
    fun getRecentOrders(): List<DashboardRecentOrder>
    
    /**
     * Lấy danh sách món ăn bán chạy nhất
     */
    fun getTopProducts(): List<DashboardTopProduct>
}
