package com.example.foodapp.data.repository.owner.dashboard

import android.R
import androidx.compose.ui.graphics.Color
import com.example.foodapp.data.model.owner.DashboardDayRevenue
import com.example.foodapp.data.model.owner.DashboardRecentOrder
import com.example.foodapp.data.model.owner.DashboardStat
import com.example.foodapp.data.model.owner.DashboardTopProduct
import com.example.foodapp.data.repository.owner.base.OwnerDashboardRepository

/**
 * Repository mock cho màn hình Dashboard.
 * Toàn bộ dữ liệu hiển thị chỉ nằm trong lớp này,
 * không hard-code trực tiếp trong màn hình Compose.
 */
class MockDashboardRepository : OwnerDashboardRepository {

    override fun getStats(): List<DashboardStat> = listOf(
        DashboardStat(
            iconRes = R.drawable.ic_menu_sort_by_size,
            value = "124",
            label = "Tổng đơn hôm nay",
            color = Color(0xFF2196F3)
        ),
        DashboardStat(
            iconRes = R.drawable.ic_dialog_email,
            value = "1.250.000đ",
            label = "Doanh thu hôm nay",
            color = Color(0xFF4CAF50)
        ),
        DashboardStat(
            iconRes = R.drawable.ic_menu_directions,
            value = "8",
            label = "Đơn đang giao",
            color = Color(0xFFFF9800)
        ),
        DashboardStat(
            iconRes = R.drawable.btn_star_big_on,
            value = "Cơm gà xối mỡ",
            label = "Món bán chạy nhất",
            color = Color(0xFFFFC107)
        )
    )

    override fun getWeeklyRevenue(): List<DashboardDayRevenue> = listOf(
        DashboardDayRevenue("T2", 1250),
        DashboardDayRevenue("T3", 1870),
        DashboardDayRevenue("T4", 1560),
        DashboardDayRevenue("T5", 2150),
        DashboardDayRevenue("T6", 1890),
        DashboardDayRevenue("T7", 2380),
        DashboardDayRevenue("CN", 2050)
    )

    override fun getRecentOrders(): List<DashboardRecentOrder> = listOf(
        DashboardRecentOrder("#ORD001", "Khách Hàng A", "Đang xử lý", 245000),
        DashboardRecentOrder("#ORD002", "Khách Hàng B", "Đang giao", 189000),
        DashboardRecentOrder("#ORD003", "Khách Hàng C", "Hoàn thành", 312000),
        DashboardRecentOrder("#ORD004", "Khách Hàng D", "Đang xử lý", 156000),
        DashboardRecentOrder("#ORD005", "Khách Hàng E", "Hoàn thành", 428000)
    )

    override fun getTopProducts(): List<DashboardTopProduct> = listOf(
        DashboardTopProduct("🍚 Cơm gà", 156, "1.44M"),
        DashboardTopProduct("🍜 Phở bò", 128, "1.28M"),
        DashboardTopProduct("🥤 Trà sữa", 195, "975K"),
        DashboardTopProduct("🍝 Mì", 87, "522K"),
        DashboardTopProduct("🍗 Gà rán", 92, "644K")
    )
}
