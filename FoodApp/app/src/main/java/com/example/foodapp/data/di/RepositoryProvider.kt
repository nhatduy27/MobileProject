package com.example.foodapp.data.di

import com.example.foodapp.data.repository.owner.base.*
import com.example.foodapp.data.repository.owner.customer.MockCustomerRepository
import com.example.foodapp.data.repository.owner.foods.MockFoodRepository
import com.example.foodapp.data.repository.owner.revenue.MockRevenueRepository
import com.example.foodapp.data.repository.shipper.base.ShipperEarningsRepository
import com.example.foodapp.data.repository.shipper.base.ShipperHistoryRepository
import com.example.foodapp.data.repository.shipper.base.ShipperHomeRepository
import com.example.foodapp.data.repository.shipper.base.ShipperProfileRepository
import com.example.foodapp.data.repository.shipper.earnings.MockShipperEarningsRepository
import com.example.foodapp.data.repository.shipper.home.MockShipperHomeRepository
import com.example.foodapp.data.repository.shipper.history.MockShipperHistoryRepository
import com.example.foodapp.data.repository.shipper.profile.MockShipperProfileRepository

/**
 * Service Locator Pattern cho Dependency Injection
 * 
 * Đây là trung tâm quản lý việc cung cấp Repository cho toàn ứng dụng.
 * Tất cả ViewModel sẽ lấy repository thông qua class này.
 * Khi backend sẵn sàng, chỉ cần update logic trong class này mà không cần sửa ViewModel.
 * 
 * USAGE trong ViewModel:
 * ```kotlin
 * class ShipperHomeViewModel : ViewModel() {
 *     private val repository = RepositoryProvider.getHomeRepository()
 *     // ... rest of code
 * }
 * ```
 * 
 * KHI BACKEND SẴN SÀNG:
 * 1. Uncomment các import RealRepository ở trên
 * 2. Thay đổi return value trong các function dưới
 * 3. Xong! Không cần sửa ViewModel
 */
object RepositoryProvider {
    
    /**
     * Config global: Sử dụng Mock hay Real Repository
     * - true: Dùng Mock (development)
     * - false: Dùng Real (production với backend)
     */
    private const val USE_MOCK = true
    
    /**
     * Hoặc có thể dùng BuildConfig để tự động switch:
     * private val USE_MOCK = BuildConfig.DEBUG
     */
    
    // ==================== SHIPPER HOME REPOSITORY ====================
    
    /**
     * Cung cấp Home Repository cho Shipper
     */
    fun getHomeRepository(): ShipperHomeRepository {
        return if (USE_MOCK) {
            MockShipperHomeRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            MockShipperHomeRepository()  // Tạm thời vẫn dùng Mock
        }
    }
    
    // ==================== SHIPPER EARNINGS REPOSITORY ====================
    
    /**
     * Cung cấp Earnings Repository cho Shipper
     */
    fun getEarningsRepository(): ShipperEarningsRepository {
        return if (USE_MOCK) {
            MockShipperEarningsRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            // RealShipperEarningsRepository(getApiService())
            MockShipperEarningsRepository()  // Tạm thời vẫn dùng Mock
        }
    }
    
    // ==================== SHIPPER HISTORY REPOSITORY ====================
    
    /**
     * Cung cấp History Repository cho Shipper
     */
    fun getHistoryRepository(): ShipperHistoryRepository {
        return if (USE_MOCK) {
            MockShipperHistoryRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            // RealShipperHistoryRepository(getApiService())
            MockShipperHistoryRepository()  // Tạm thời vẫn dùng Mock
        }
    }
    
    // ==================== SHIPPER PROFILE REPOSITORY ====================
    
    /**
     * Cung cấp Profile Repository cho Shipper
     */
    fun getProfileRepository(): ShipperProfileRepository {
        return if (USE_MOCK) {
            MockShipperProfileRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            // RealShipperProfileRepository(getApiService())
            MockShipperProfileRepository()  // Tạm thời vẫn dùng Mock
        }
    }
    
    // ==================== OWNER DASHBOARD REPOSITORY ====================
    
    fun getDashboardRepository(): OwnerDashboardRepository {
        // Use Real Repository for Dashboard API integration
        return com.example.foodapp.data.repository.owner.dashboard.RealDashboardRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.ShopApiService::class.java
            )
        )
    }
    
    // ==================== OWNER ORDERS REPOSITORY ====================
    
    fun getOrdersRepository(): OwnerOrdersRepository {
        return com.example.foodapp.data.repository.owner.orders.RealOrderRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.OrderApiService::class.java
            )
        )
    }
    
    // ==================== OWNER FOODS REPOSITORY ====================
    
    fun getFoodsRepository(): OwnerFoodsRepository {
        return if (USE_MOCK) {
            MockFoodRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            // RealFoodRepository(getOwnerApiService())
            MockFoodRepository()
        }
    }
    
    // ==================== OWNER REVENUE REPOSITORY ====================
    
    fun getRevenueRepository(): OwnerRevenueRepository {
        return if (USE_MOCK) {
            MockRevenueRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            // RealRevenueRepository(getOwnerApiService())
            MockRevenueRepository()
        }
    }
    
    // ==================== OWNER CUSTOMER REPOSITORY ====================
    
    fun getCustomerRepository(): OwnerCustomerRepository {
        return if (USE_MOCK) {
            MockCustomerRepository()
        } else {
            // TODO: Khi backend ready, uncomment dòng dưới
            // RealCustomerRepository(getOwnerApiService())
            MockCustomerRepository()
        }
    }
    
    // ==================== OWNER SHIPPER REPOSITORY ====================
    
    fun getOwnerShipperRepository(): com.example.foodapp.data.repository.owner.base.OwnerShipperRepository {
        return com.example.foodapp.data.repository.owner.shippers.RealShipperRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.ShipperApiService::class.java
            )
        )
    }
    
    // ==================== OWNER PRODUCT REPOSITORY ====================
    
    fun getProductRepository(): com.example.foodapp.data.repository.owner.base.OwnerProductRepository {
        return com.example.foodapp.data.repository.owner.products.RealProductRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.ProductApiService::class.java
            )
        )
    }
    
    // ==================== OWNER CATEGORY REPOSITORY ====================
    
    fun getCategoryRepository(): OwnerCategoryRepository {
        // Use Real Repository for Category API integration (Public API)
        return com.example.foodapp.data.repository.owner.categories.RealCategoryRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.admin.CategoriesApiService::class.java
            )
        )
    }
    
    // ==================== OWNER VOUCHER REPOSITORY ====================
    
    fun getVoucherRepository(): com.example.foodapp.data.repository.owner.base.OwnerVoucherRepository {
        return com.example.foodapp.data.repository.owner.vouchers.RealVoucherRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.VoucherApiService::class.java
            )
        )
    }
    
    // ==================== OWNER NOTIFICATION REPOSITORY ====================
    
    fun getNotificationRepository(): com.example.foodapp.data.repository.owner.base.OwnerNotificationRepository {
        return com.example.foodapp.data.repository.owner.notifications.RealNotificationRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.NotificationApiService::class.java
            )
        )
    }
    
    // ==================== OWNER REVIEW REPOSITORY ====================
    
    fun getReviewRepository(): com.example.foodapp.data.repository.owner.base.OwnerReviewRepository {
        return com.example.foodapp.data.repository.owner.reviews.RealReviewRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.owner.ReviewApiService::class.java
            )
        )
    }
    
    // ==================== USER PROFILE REPOSITORY ====================
    
    fun getUserProfileRepository(): com.example.foodapp.data.repository.user.base.UserProfileRepository {
        return com.example.foodapp.data.repository.user.profile.RealUserProfileRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.user.UserProfileApiService::class.java
            )
        )
    }
    
    // ==================== API SERVICE (cho Real Repository) ====================
    
    /**
     * Cung cấp API Service instance
     * Backend sẽ config Retrofit/Ktor ở đây
     */
    /*
    private fun getApiService(): ShipperApiService {
        // TODO: Backend implement Retrofit/Ktor setup
        // Example với Retrofit:
        // return Retrofit.Builder()
        //     .baseUrl("https://api.yourbackend.com/")
        //     .addConverterFactory(GsonConverterFactory.create())
        //     .build()
        //     .create(ShipperApiService::class.java)
        
        throw NotImplementedError("Backend chưa setup API Service")
    }
    */
    // ==================== SHIPPER ORDER REPOSITORY ====================

    fun getShipperOrderRepository(): com.example.foodapp.data.repository.shipper.base.ShipperOrderRepository {
        return com.example.foodapp.data.repository.shipper.orders.RealShipperOrderRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.shipper.ShipperApiService::class.java
            )
        )
    }
    
    // ==================== SHIPPER APPLICATION REPOSITORY ====================
    
    fun getShipperApplicationRepository(): com.example.foodapp.data.repository.shipper.application.ShipperApplicationRepository {
        return com.example.foodapp.data.repository.shipper.application.RealShipperApplicationRepository(
            com.example.foodapp.data.remote.api.ApiClient.createService(
                com.example.foodapp.data.remote.shipper.ShipperApplicationApiService::class.java
            )
        )
    }
}
