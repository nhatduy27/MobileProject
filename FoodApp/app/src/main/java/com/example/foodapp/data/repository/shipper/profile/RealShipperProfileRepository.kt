package com.example.foodapp.data.repository.shipper.profile

import com.example.foodapp.data.model.shipper.ProfileMenuItem
import com.example.foodapp.data.model.shipper.ShipperProfile
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.ShipperProfileRepository

/**
 * Real Repository cho màn Hồ sơ Shipper.
 * Gọi API backend để lấy dữ liệu thực tế.
 * 
 * Backend sẽ implement các API call ở đây.
 */
class RealShipperProfileRepository(
    private val apiService: ShipperApiService
) : ShipperProfileRepository {

    override fun getProfile(): ShipperProfile {
        // TODO: Backend implement API call
        // Example:
        // val response = apiService.getShipperProfile()
        // return response.toShipperProfile()
        
        throw NotImplementedError("Backend chưa implement API này")
    }

    override fun getAccountItems(profile: ShipperProfile): List<ProfileMenuItem> {
        // TODO: Backend có thể lấy từ API hoặc dùng logic local
        // Tạm thời dùng logic local giống Mock
        // Backend quyết định sau
        
        throw NotImplementedError("Backend chưa implement logic này")
    }

    override fun getSettingsItems(): List<ProfileMenuItem> {
        // TODO: Backend có thể lấy từ API hoặc dùng logic local
        // Tạm thời dùng logic local giống Mock
        // Backend quyết định sau
        
        throw NotImplementedError("Backend chưa implement logic này")
    }

    override fun getOtherItems(): List<ProfileMenuItem> {
        // TODO: Backend có thể lấy từ API hoặc dùng logic local
        // Tạm thời dùng logic local giống Mock
        // Backend quyết định sau
        
        throw NotImplementedError("Backend chưa implement logic này")
    }
}
