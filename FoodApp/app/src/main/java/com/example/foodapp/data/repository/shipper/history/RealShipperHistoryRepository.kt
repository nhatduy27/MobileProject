package com.example.foodapp.data.repository.shipper.history

import com.example.foodapp.data.model.shipper.DeliveryHistory
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.ShipperHistoryRepository

/**
 * Real Repository cho màn Lịch sử giao hàng của Shipper.
 * Gọi API backend để lấy dữ liệu thực tế.
 * 
 * Backend sẽ implement các API call ở đây.
 */
class RealShipperHistoryRepository(
    private val apiService: ShipperApiService
) : ShipperHistoryRepository {

    override fun getHistoryList(): List<DeliveryHistory> {
        // TODO: Backend implement API call
        // Example:
        // val response = apiService.getDeliveryHistory()
        // return response.history.map { it.toDeliveryHistory() }
        
        throw NotImplementedError("Backend chưa implement API này")
    }
}
