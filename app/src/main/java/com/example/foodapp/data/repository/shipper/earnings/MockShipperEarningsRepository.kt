package com.example.foodapp.data.repository.shipper.earnings

import com.example.foodapp.data.model.shipper.EarningsData

/**
 * Repository mock cho màn Thu nhập của Shipper.
 */
class MockShipperEarningsRepository {

    fun getAllEarningsHistory(): List<EarningsData> = listOf(
        EarningsData("Hôm nay, 25/12/2025", 10, 90_000, 5_000),
        EarningsData("24/12/2025", 12, 85_000, 10_000),
        EarningsData("23/12/2025", 15, 102_000, 0),
        EarningsData("22/12/2025", 18, 125_000, 15_000),
        EarningsData("21/12/2025", 14, 95_000, 0),
        EarningsData("20/12/2025", 16, 110_000, 10_000),
        EarningsData("19/12/2025", 13, 88_000, 0),
        EarningsData("18/12/2025", 17, 118_000, 12_000),
        EarningsData("10/12/2025", 11, 80_000, 0),
        EarningsData("01/12/2025", 9, 70_000, 0),
        EarningsData("25/11/2025", 8, 60_000, 0),
        EarningsData("10/11/2025", 7, 50_000, 0)
    )
}
