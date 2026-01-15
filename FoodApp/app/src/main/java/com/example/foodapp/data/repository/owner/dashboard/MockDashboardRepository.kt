package com.example.foodapp.data.repository.owner.dashboard

import com.example.foodapp.data.repository.owner.base.OwnerDashboardRepository

/**
 * Repository mock cho màn hình Dashboard.
 * Toàn bộ dữ liệu hiển thị chỉ nằm trong lớp này,
 * không hard-code trực tiếp trong màn hình Compose.
 */
class MockDashboardRepository : OwnerDashboardRepository {

    override suspend fun getShopAnalytics(
        from: String?,
        to: String?
    ): Result<com.example.foodapp.data.model.owner.DashboardData> {
        return Result.failure(Exception("Mock not implemented properly for new analytics"))
    }
}
