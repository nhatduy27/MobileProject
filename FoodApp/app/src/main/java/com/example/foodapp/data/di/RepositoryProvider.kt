package com.example.foodapp.data.di

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
            // RealShipperHomeRepository(getApiService())
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
}
