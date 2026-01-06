package com.example.foodapp.data.remote.shipper

import com.example.foodapp.data.remote.shipper.response.EarningsResponse
import com.example.foodapp.data.remote.shipper.response.HistoryResponse
import com.example.foodapp.data.remote.shipper.response.HomeResponse
import com.example.foodapp.data.remote.shipper.response.ProfileResponse

/**
 * Interface định nghĩa các API endpoint cho Shipper.
 * Backend sẽ implement các endpoint này.
 * 
 * Framework: Retrofit hoặc Ktor (tùy backend chọn)
 * 
 * TODO Backend: Implement các API endpoint sau:
 * - GET /api/shipper/home - Lấy thống kê và danh sách task
 * - GET /api/shipper/earnings - Lấy lịch sử thu nhập
 * - GET /api/shipper/history - Lấy lịch sử giao hàng
 * - GET /api/shipper/profile - Lấy thông tin profile
 */
interface ShipperApiService {
    
    /**
     * Lấy dữ liệu cho màn Home
     * Endpoint: GET /api/shipper/home
     * 
     * @return HomeResponse chứa stats và tasks
     */
    // @GET("api/shipper/home")
    suspend fun getHomeData(): HomeResponse
    
    /**
     * Lấy lịch sử thu nhập
     * Endpoint: GET /api/shipper/earnings
     * 
     * @return EarningsResponse chứa danh sách earnings
     */
    // @GET("api/shipper/earnings")
    suspend fun getEarningsHistory(): EarningsResponse
    
    /**
     * Lấy lịch sử giao hàng
     * Endpoint: GET /api/shipper/history
     * 
     * @return HistoryResponse chứa danh sách history
     */
    // @GET("api/shipper/history")
    suspend fun getDeliveryHistory(): HistoryResponse
    
    /**
     * Lấy thông tin profile shipper
     * Endpoint: GET /api/shipper/profile
     * 
     * @return ProfileResponse chứa thông tin shipper
     */
    // @GET("api/shipper/profile")
    suspend fun getShipperProfile(): ProfileResponse
}
