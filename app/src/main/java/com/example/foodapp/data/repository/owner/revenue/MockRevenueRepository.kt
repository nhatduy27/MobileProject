package com.example.foodapp.data.repository.owner.revenue

import com.example.foodapp.data.model.owner.PeriodRevenueData
import com.example.foodapp.data.model.owner.RevenueStat
import com.example.foodapp.data.model.owner.TimeSlotRevenue
import com.example.foodapp.data.model.owner.TopProduct
import androidx.compose.ui.graphics.Color

class MockRevenueRepository {

    private val periodData: Map<String, PeriodRevenueData> = mapOf(
        "Hôm nay" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu hôm nay", "1.25M", "↑ 12% so với hôm qua", Color(0xFFFF6B35)),
                RevenueStat("Số đơn hôm nay", "124", "↑ 8% so với hôm qua", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "101K", "↑ 5% so với hôm qua", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu tháng", "38.5M", "11 ngày đầu tháng", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 42, 35, "438K"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 52, 42, "525K"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 30, 23, "287K")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 32, 45_000, "1.44M"),
                TopProduct("🥈", "Phở bò", 28, 50_000, "1.40M"),
                TopProduct("🥉", "Trà sữa trân châu", 45, 25_000, "1.13M")
            )
        ),
        "Tuần này" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu tuần", "8.75M", "↑ 15% so với tuần trước", Color(0xFFFF6B35)),
                RevenueStat("Số đơn tuần", "856", "↑ 12% so với tuần trước", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "102K", "↑ 3% so với tuần trước", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu tháng", "38.5M", "11 ngày đầu tháng", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 48, 38, "3.2M"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 65, 45, "3.8M"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 35, 24, "1.75M")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 120, 45_000, "5.4M"),
                TopProduct("🥈", "Phở bò", 95, 50_000, "4.75M"),
                TopProduct("🥉", "Trà sữa trân châu", 150, 25_000, "3.75M")
            )
        ),
        "Tháng này" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu tháng", "38.5M", "↑ 18% so với tháng trước", Color(0xFFFF6B35)),
                RevenueStat("Số đơn tháng", "3.8K", "↑ 14% so với tháng trước", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "101K", "↑ 3% so với tháng trước", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu TB", "1.78M", "Doanh thu hàng ngày", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 240, 40, "14.2M"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 312, 42, "16.8M"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 168, 22, "7.5M")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 520, 45_000, "23.4M"),
                TopProduct("🥈", "Phở bò", 420, 50_000, "21M"),
                TopProduct("🥉", "Trà sữa trân châu", 680, 25_000, "17M")
            )
        ),
        "Năm nay" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu năm", "450M", "↑ 25% so với năm trước", Color(0xFFFF6B35)),
                RevenueStat("Số đơn năm", "48K", "↑ 20% so với năm trước", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "103K", "↑ 4% so với năm trước", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu TB", "1.23M", "Doanh thu hàng ngày", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 3200, 38, "168M"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 4100, 43, "198M"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 2240, 23, "84M")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 6800, 45_000, "306M"),
                TopProduct("🥈", "Phở bò", 5600, 50_000, "280M"),
                TopProduct("🥉", "Trà sữa trân châu", 8900, 25_000, "222.5M")
            )
        )
    )

    fun getAvailablePeriods(): List<String> = periodData.keys.toList()

    fun getRevenueForPeriod(period: String): PeriodRevenueData {
        return periodData[period] ?: periodData["Hôm nay"]!!
    }
}
