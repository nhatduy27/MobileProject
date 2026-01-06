package com.example.foodapp.data.repository.shipper.history

import com.example.foodapp.data.model.shipper.DeliveryHistory
import com.example.foodapp.data.model.shipper.HistoryStatus
import com.example.foodapp.data.repository.shipper.base.ShipperHistoryRepository

/**
 * Repository mock cho màn Lịch sử giao hàng của Shipper.
 */
class MockShipperHistoryRepository : ShipperHistoryRepository {

    override fun getHistoryList(): List<DeliveryHistory> = listOf(
        DeliveryHistory(
            "#ORD10247",
            "Phạm Thị D",
            "12/12/2024",
            "14:30",
            "KTX Food Store, Khu A",
            "KTX Khu C, Phòng 412",
            "2.1km",
            25_000,
            HistoryStatus.COMPLETED,
            4.5
        ),
        DeliveryHistory(
            "#ORD10246",
            "Lê Văn C",
            "12/12/2024",
            "13:15",
            "KTX Food Store, Khu A",
            "KTX Khu A, Phòng 108",
            "0.5km",
            15_000,
            HistoryStatus.COMPLETED,
            5.0
        ),
        DeliveryHistory(
            "#ORD10243",
            "Nguyễn Văn E",
            "11/12/2024",
            "19:20",
            "KTX Food Store, Khu A",
            "KTX Khu B, Phòng 201",
            "1.5km",
            20_000,
            HistoryStatus.CANCELLED
        ),
        DeliveryHistory(
            "#ORD10240",
            "Trần Thị F",
            "11/12/2024",
            "12:45",
            "KTX Food Store, Khu A",
            "KTX Khu C, Phòng 310",
            "1.8km",
            22_000,
            HistoryStatus.COMPLETED,
            4.8
        ),
        DeliveryHistory(
            "#ORD10235",
            "Hoàng Văn G",
            "10/12/2024",
            "18:00",
            "KTX Food Store, Khu A",
            "KTX Khu A, Phòng 505",
            "0.8km",
            15_000,
            HistoryStatus.COMPLETED,
            4.2
        )
    )
}
