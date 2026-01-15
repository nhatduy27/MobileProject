package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.remote.owner.response.*

/**
 * Interface định nghĩa các API endpoint cho Owner.
 * Backend sẽ implement các endpoint này.
 * 
 * Framework: Retrofit hoặc Ktor (tùy backend chọn)
 * 
 * TODO Backend: Implement các API endpoint sau:
 * - GET /api/owner/dashboard - Lấy dữ liệu dashboard
 * - GET /api/owner/orders - Lấy danh sách đơn hàng
 * - GET /api/owner/foods - Lấy danh sách món ăn
 * - GET /api/owner/revenue - Lấy dữ liệu doanh thu
 * - GET /api/owner/customers - Lấy danh sách khách hàng
 * - GET /api/owner/shippers - Lấy danh sách shipper
 */
interface OwnerApiService {
    
    /**
     * Lấy dữ liệu cho màn Dashboard
     * Endpoint: GET /api/owner/dashboard
     */
    // @GET("api/owner/dashboard")
    // suspend fun getDashboardData(): DashboardResponse
    
    /**
     * Lấy danh sách đơn hàng
     * Endpoint: GET /api/owner/orders
     */
    // @GET("api/owner/orders")
    suspend fun getOrders(): OrdersResponse
    
    /**
     * Lấy danh sách món ăn
     * Endpoint: GET /api/owner/foods
     */
    // @GET("api/owner/foods")
    suspend fun getFoods(): FoodsResponse
    
    /**
     * Lấy dữ liệu doanh thu theo kỳ
     * Endpoint: GET /api/owner/revenue?period={period}
     */
    // @GET("api/owner/revenue")
    suspend fun getRevenue(period: String): RevenueResponse
    
    /**
     * Lấy danh sách khách hàng
     * Endpoint: GET /api/owner/customers
     */
    // @GET("api/owner/customers")
    suspend fun getCustomers(): CustomersResponse
    
    /**
     * Lấy danh sách shipper
     * Endpoint: GET /api/owner/shippers
     */
    // @GET("api/owner/shippers")
    suspend fun getShippers(): ShippersResponse
}
