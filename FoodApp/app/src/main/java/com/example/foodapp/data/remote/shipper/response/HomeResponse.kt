package com.example.foodapp.data.remote.shipper.response

import com.example.foodapp.data.model.shipper.DeliveryTask
import com.example.foodapp.data.model.shipper.ShipperStats
import com.example.foodapp.data.model.shipper.TaskStatus

/**
 * Response DTO cho API Home của Shipper
 * Mapping từ JSON response sang domain model
 * 
 * Backend sẽ trả về JSON format này từ endpoint /api/shipper/home
 */
data class HomeResponse(
    val stats: StatsDto,
    val tasks: List<TaskDto>
) {
    /**
     * DTO cho thống kê shipper
     */
    data class StatsDto(
        val todayOrders: Int,
        val todayEarnings: Int,
        val completionRate: Int,
        val rating: Double
    )
    
    /**
     * DTO cho delivery task
     */
    data class TaskDto(
        val orderId: String,
        val customerName: String,
        val pickupAddress: String,
        val deliveryAddress: String,
        val distance: String,
        val fee: Int,
        val status: String  // "PENDING", "PICKING_UP", "DELIVERING", "COMPLETED"
    )
    
    /**
     * Extension function để convert Response DTO sang Domain Model
     */
    fun toShipperStats(): ShipperStats = ShipperStats(
        todayOrders = stats.todayOrders,
        todayEarnings = stats.todayEarnings,
        completionRate = stats.completionRate,
        rating = stats.rating
    )
    
    fun toDeliveryTasks(): List<DeliveryTask> = tasks.map { task ->
        DeliveryTask(
            orderId = task.orderId,
            customerName = task.customerName,
            pickupAddress = task.pickupAddress,
            deliveryAddress = task.deliveryAddress,
            distance = task.distance,
            fee = task.fee,
            status = when (task.status) {
                "PENDING" -> TaskStatus.PENDING
                "PICKING_UP" -> TaskStatus.PICKING_UP
                "DELIVERING" -> TaskStatus.DELIVERING
                "COMPLETED" -> TaskStatus.COMPLETED
                else -> TaskStatus.PENDING
            }
        )
    }
}
