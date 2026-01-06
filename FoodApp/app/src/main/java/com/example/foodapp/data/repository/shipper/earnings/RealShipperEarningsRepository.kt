package com.example.foodapp.data.repository.shipper.earnings

import com.example.foodapp.data.model.shipper.EarningsData
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.ShipperEarningsRepository

/**
 * Real Repository cho màn Thu nhập của Shipper.
 * Gọi API backend để lấy dữ liệu thực tế.
 * 
 * Backend sẽ implement các API call ở đây.
 */
class RealShipperEarningsRepository(
    private val apiService: ShipperApiService
) : ShipperEarningsRepository {

    override fun getAllEarningsHistory(): List<EarningsData> {
        // TODO: Backend implement API call
        // Example:
        // val response = apiService.getEarningsHistory()
        // return response.earnings.map { it.toEarningsData() }
        
        throw NotImplementedError("Backend chưa implement API này")
    }
}
