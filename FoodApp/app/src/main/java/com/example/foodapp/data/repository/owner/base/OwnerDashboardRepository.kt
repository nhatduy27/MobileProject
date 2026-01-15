package com.example.foodapp.data.repository.owner.base

/**
 * Interface cho Dashboard Repository của Owner.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerDashboardRepository {
    
    /**
     * Lấy dữ liệu analytics từ API
     */
    suspend fun getShopAnalytics(from: String? = null, to: String? = null): Result<com.example.foodapp.data.model.owner.DashboardData>
}
