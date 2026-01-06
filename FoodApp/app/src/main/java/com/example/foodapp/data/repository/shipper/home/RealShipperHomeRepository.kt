package com.example.foodapp.data.repository.shipper.home

import com.example.foodapp.data.model.shipper.DeliveryTask
import com.example.foodapp.data.model.shipper.ShipperStats
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.ShipperHomeRepository

/**
 * Real Repository cho màn Home của Shipper.
 * Gọi API backend để lấy dữ liệu thực tế.
 * 
 * Backend sẽ implement các API call ở đây.
 */
class RealShipperHomeRepository(
    private val apiService: ShipperApiService
) : ShipperHomeRepository {

    override fun getStats(): ShipperStats {
        // TODO: Backend implement API call
        // Example:
        // val response = apiService.getShipperStats()
        // return response.toShipperStats()
        
        throw NotImplementedError("Backend chưa implement API này")
    }

    override fun getTasks(): List<DeliveryTask> {
        // TODO: Backend implement API call
        // Example:
        // val response = apiService.getCurrentTasks()
        // return response.tasks.map { it.toDeliveryTask() }
        
        throw NotImplementedError("Backend chưa implement API này")
    }
}
