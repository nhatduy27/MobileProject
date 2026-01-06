package com.example.foodapp.data.repository.shipper.home

import com.example.foodapp.data.model.shipper.DeliveryTask
import com.example.foodapp.data.model.shipper.ShipperStats
import com.example.foodapp.data.model.shipper.TaskStatus
import com.example.foodapp.data.repository.shipper.base.ShipperHomeRepository

/**
 * Repository mock cho màn Home của Shipper.
 * Toàn bộ dữ liệu hiển thị được tách khỏi Composable.
 */
class MockShipperHomeRepository : ShipperHomeRepository {

    override fun getStats(): ShipperStats = ShipperStats(
        todayOrders = 12,
        todayEarnings = 240_000,
        completionRate = 98,
        rating = 4.8
    )

    override fun getTasks(): List<DeliveryTask> = listOf(
        DeliveryTask(
            "#ORD10245",
            "Trần Thị B",
            "KTX Food Store, Khu A",
            "KTX Khu B, Phòng 305",
            "1.2km",
            20_000,
            TaskStatus.PENDING
        ),
        DeliveryTask(
            "#ORD10246",
            "Lê Văn C",
            "KTX Food Store, Khu A",
            "KTX Khu A, Phòng 108",
            "0.5km",
            15_000,
            TaskStatus.PICKING_UP
        ),
        DeliveryTask(
            "#ORD10247",
            "Phạm Thị D",
            "KTX Food Store, Khu A",
            "KTX Khu C, Phòng 412",
            "2.1km",
            25_000,
            TaskStatus.DELIVERING
        )
    )
}
