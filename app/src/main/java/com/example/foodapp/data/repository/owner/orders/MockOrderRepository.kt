package com.example.foodapp.data.repository.owner.orders

import com.example.foodapp.data.model.owner.Order
import com.example.foodapp.data.model.owner.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockOrderRepository {

    private val _internalOrdersFlow = MutableStateFlow<List<Order>>(emptyList())

    init {
        _internalOrdersFlow.value = listOf(
            // --- ĐƠN MỚI (PENDING / PROCESSING) ---
            Order("#ORD10299", "Nguyễn Thùy Linh", "KTX Khu A, P.502", "• Cơm sườn bì chả x1\n• Canh khổ qua x1", "11:45 AM", 65_000, OrderStatus.PENDING),
            Order("#ORD10298", "Trần Minh Tuấn", "KTX Khu B, P.301", "• Phở bò tái nạm x2\n• Quẩy giòn x1", "11:42 AM", 110_000, OrderStatus.PENDING),
            Order("#ORD10297", "Lê Thị Thu Hà", "Chung cư Sky, B.1205", "• Trà sữa trân châu đường đen x2", "11:40 AM", 104_000, OrderStatus.PROCESSING),
            Order("#ORD10296", "Phạm Quốc Bảo", "KTX Khu A, P.102", "• Bún đậu mắm tôm x2\n• Nước sấu x2", "11:35 AM", 150_000, OrderStatus.PROCESSING),
            Order("#ORD10295", "Hoàng Gia Bảo", "Nhà số 12, Đường số 5", "• Pizza Hải sản x1\n• Coca Cola 1.5L x1", "11:30 AM", 210_000, OrderStatus.PROCESSING),
            Order("#ORD10294", "Vũ Thị Ngọc", "KTX Khu C, P.404", "• Mì cay 7 cấp độ x1\n• Trà đào cam sả x1", "11:25 AM", 75_000, OrderStatus.PROCESSING),
            Order("#ORD10293", "Đặng Văn Hùng", "Tòa nhà Bitexco, Lễ tân", "• Cơm văn phòng (Cá kho) x5", "11:20 AM", 250_000, OrderStatus.PROCESSING),
            Order("#ORD10292", "Bùi Phương Thảo", "KTX Khu B, P.210", "• Gà rán 3 miếng x1\n• Khoai tây chiên x1", "11:15 AM", 95_000, OrderStatus.PROCESSING),

            // --- ĐANG GIAO (DELIVERING) ---
            Order("#ORD10291", "Ngô Kiến Huy", "KTX Khu A, P.303", "• Bún bò Huế đặc biệt x1", "11:10 AM", 60_000, OrderStatus.DELIVERING),
            Order("#ORD10290", "Đỗ Mỹ Linh", "Chung cư Masteri, T5.09", "• Salad cá ngừ x1\n• Nước ép ổi x1", "11:05 AM", 85_000, OrderStatus.DELIVERING),
            Order("#ORD10289", "Lý Hải", "Văn phòng ABC, Q.1", "• Cafe sữa đá x10", "11:00 AM", 250_000, OrderStatus.DELIVERING),
            Order("#ORD10288", "Trương Quỳnh Anh", "KTX Khu D, P.601", "• Hủ tiếu Nam Vang x2", "10:55 AM", 90_000, OrderStatus.DELIVERING),
            Order("#ORD10287", "Phan Mạnh Quỳnh", "Nhà trọ 15/2, Hẻm 3", "• Cơm chiên dương châu x1\n• Canh rong biển x1", "10:50 AM", 55_000, OrderStatus.DELIVERING),
            Order("#ORD10286", "Hồ Ngọc Hà", "Biệt thự Lan Anh", "• Sashimi tổng hợp x1", "10:45 AM", 500_000, OrderStatus.DELIVERING),
            Order("#ORD10285", "Trấn Thành", "Phim trường Galaxy", "• Bánh mì Huỳnh Hoa x5", "10:40 AM", 300_000, OrderStatus.DELIVERING),
            Order("#ORD10284", "Hari Won", "Phim trường Galaxy", "• Trà sữa Gongcha x5", "10:40 AM", 280_000, OrderStatus.DELIVERING),

            // --- HOÀN THÀNH (COMPLETED) ---
            Order("#ORD10283", "Nguyễn Văn Toàn", "Sân vận động QK7", "• Nước khoáng x24", "10:30 AM", 120_000, OrderStatus.COMPLETED),
            Order("#ORD10282", "Lê Công Vinh", "KTX Khu A, P.111", "• Cháo lòng x1", "10:25 AM", 35_000, OrderStatus.COMPLETED),
            Order("#ORD10281", "Phạm Nhật Vượng", "Landmark 81, P.Sky", "• Phở chọc trời x1", "10:20 AM", 920_000, OrderStatus.COMPLETED),
            Order("#ORD10280", "Đặng Lê Nguyên Vũ", "Động Trung Nguyên", "• Cafe chồn x1", "10:15 AM", 150_000, OrderStatus.COMPLETED),
            Order("#ORD10279", "Nguyễn Thúc Thùy Tiên", "KTX Khu B, P.505", "• Bánh tráng trộn x3\n• Trà tắc x3", "10:10 AM", 90_000, OrderStatus.COMPLETED),
            Order("#ORD10278", "H'Hen Niê", "Chung cư Gold View", "• Cơm gạo lứt x1\n• Ức gà áp chảo x1", "10:05 AM", 110_000, OrderStatus.COMPLETED),
            Order("#ORD10277", "Sơn Tùng MTP", "Công ty M-TP", "• Kem dâu x1", "10:00 AM", 25_000, OrderStatus.COMPLETED),
            Order("#ORD10276", "Đen Vâu", "KTX Khu C, P.202", "• Đen đá không đường x1", "09:55 AM", 20_000, OrderStatus.COMPLETED),
            Order("#ORD10275", "Bích Phương", "Nhà riêng Q.3", "• Khoai lang nướng x2", "09:50 AM", 40_000, OrderStatus.COMPLETED),
            Order("#ORD10274", "Isaac", "KTX Khu A, P.301", "• Bún riêu cua x1", "09:45 AM", 45_000, OrderStatus.COMPLETED),
            Order("#ORD10273", "Tóc Tiên", "Villa Thảo Điền", "• Smoothie Bowl x1", "09:40 AM", 120_000, OrderStatus.COMPLETED),
            Order("#ORD10272", "Hoàng Thùy Linh", "Chung cư Royal", "• Bánh cuốn Thanh Trì x1", "09:35 AM", 50_000, OrderStatus.COMPLETED),
            Order("#ORD10271", "Đức Phúc", "KTX Khu D, P.101", "• Sữa đậu nành x1\n• Bánh tiêu x2", "09:30 AM", 25_000, OrderStatus.COMPLETED),
            Order("#ORD10270", "Erik", "KTX Khu D, P.101", "• Bánh mì ốp la x1", "09:30 AM", 30_000, OrderStatus.COMPLETED),
            Order("#ORD10269", "Hòa Minzy", "Bắc Ninh (Ship xa)", "• Nem chua rán x10", "09:25 AM", 150_000, OrderStatus.COMPLETED),
            Order("#ORD10268", "Văn Mai Hương", "KTX Khu B, P.404", "• Mì Quảng ếch x1", "09:20 AM", 55_000, OrderStatus.COMPLETED),
            Order("#ORD10267", "Trung Quân Idol", "Đà Lạt House", "• Bánh căn x1 phần", "09:15 AM", 60_000, OrderStatus.COMPLETED),
            Order("#ORD10266", "Uyên Linh", "Phòng thu âm", "• Trà gừng mật ong x1", "09:10 AM", 35_000, OrderStatus.COMPLETED),
            Order("#ORD10265", "Mỹ Tâm", "Tòa nhà Mỹ Tâm", "• Bún mắm nêm x1", "09:05 AM", 45_000, OrderStatus.COMPLETED),
            Order("#ORD10264", "Đàm Vĩnh Hưng", "Biệt thự Q.10", "• Cháo trắng hột vịt muối x1", "09:00 AM", 50_000, OrderStatus.COMPLETED),
            Order("#ORD10263", "Tuấn Hưng", "KTX Khu A, P.205", "• Phở tái lăn x1", "08:55 AM", 60_000, OrderStatus.COMPLETED),
            Order("#ORD10262", "Bằng Kiều", "Sân bay TSN", "• Thuốc lào (Mã: Kẹo) x1", "08:50 AM", 10_000, OrderStatus.COMPLETED),
            Order("#ORD10261", "Quang Lê", "Nhà hàng Chay", "• Cơm chay thập cẩm x1", "08:45 AM", 40_000, OrderStatus.COMPLETED),
            Order("#ORD10260", "Lệ Quyên", "Phòng trà Không Tên", "• Yến chưng đường phèn x1", "08:40 AM", 200_000, OrderStatus.COMPLETED),

            // --- ĐÃ HỦY (CANCELLED) ---
            Order("#ORD10259", "Nguyễn Văn Hủy", "KTX Khu C, P.999", "• Boom hàng x1", "08:35 AM", 0, OrderStatus.CANCELLED),
            Order("#ORD10258", "Trần Thị Đổi Ý", "KTX Khu A, P.101", "• Trà sữa (Quên mang tiền)", "08:30 AM", 55_000, OrderStatus.CANCELLED),
            Order("#ORD10257", "Lê Văn Bận", "Công ty X", "• Cơm gà (Họp đột xuất)", "08:25 AM", 45_000, OrderStatus.CANCELLED),
            Order("#ORD10256", "Phạm Thị Sai Món", "Nhà riêng", "• Đặt nhầm món", "08:20 AM", 120_000, OrderStatus.CANCELLED),
            Order("#ORD10255", "Hoàng Văn Ngủ Quên", "KTX Khu B", "• Gọi không nghe máy", "08:15 AM", 30_000, OrderStatus.CANCELLED),
            Order("#ORD10254", "Võ Thị Hết Tiền", "ATM Vietcombank", "• Rút tiền không được", "08:10 AM", 500_000, OrderStatus.CANCELLED),
            Order("#ORD10253", "Đặng Văn Troll", "Địa chỉ ma", "• 10 ly Trà sữa full top", "08:05 AM", 600_000, OrderStatus.CANCELLED),
            Order("#ORD10252", "Bùi Thị Đợi Lâu", "Văn phòng Y", "• Shipper giao trễ", "08:00 AM", 70_000, OrderStatus.CANCELLED),
            Order("#ORD10251", "Ngô Văn Khó Tính", "KTX Khu D", "• Không có hành phi", "07:55 AM", 35_000, OrderStatus.CANCELLED),
            Order("#ORD10250", "Dương Thị Xa Quá", "Ngoại thành", "• Phí ship cao", "07:50 AM", 40_000, OrderStatus.CANCELLED)
        )
    }

    fun getOrders(): Flow<List<Order>> = _internalOrdersFlow.asStateFlow()

    fun addOrder(order: Order) {
        _internalOrdersFlow.update { current -> current + order }
    }

    fun updateOrder(updated: Order) {
        _internalOrdersFlow.update { current ->
            current.map { if (it.id == updated.id) updated else it }
        }
    }

    fun deleteOrder(id: String) {
        _internalOrdersFlow.update { current -> current.filterNot { it.id == id } }
    }
}
