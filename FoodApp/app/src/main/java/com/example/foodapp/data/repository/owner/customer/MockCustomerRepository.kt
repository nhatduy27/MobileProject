package com.example.foodapp.data.repository.owner.customer

import com.example.foodapp.data.model.owner.Customer
import com.example.foodapp.data.repository.owner.base.OwnerCustomerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update


class MockCustomerRepository : OwnerCustomerRepository {

    // Đổi tên biến này để tránh xung đột, thêm dấu gạch dưới là một quy ước phổ biến
    private val _internalCustomersFlow = MutableStateFlow<List<Customer>>(emptyList())

    init {
        // Khởi tạo dữ liệu mẫu khi repository được tạo
        _internalCustomersFlow.value = listOf(
            // --- KHÁCH HÀNG VIP (Chi tiêu cao, lâu năm) ---
            Customer("1", "Nguyễn Phúc Hậu", "VIP", "0901234567 • KTX Khu A, P201", "67 đơn • Tham gia 15/08/2024", "3.2M", "https://picsum.photos/id/1005/200"),
            Customer("2", "Trần Thị Mai", "VIP", "0912345678 • KTX Khu B, P305", "55 đơn • Tham gia 20/08/2024", "2.9M", "https://picsum.photos/id/1011/200"),
            Customer("3", "Lê Văn Hùng", "VIP", "0923456789 • KTX Khu A, P402", "48 đơn • Tham gia 01/09/2024", "2.5M", "https://picsum.photos/id/1012/200"),
            Customer("4", "Phạm Minh Tuấn", "VIP", "0934567890 • KTX Khu C, P510", "60 đơn • Tham gia 10/08/2024", "3.5M", "https://picsum.photos/id/1025/200"),
            Customer("5", "Hoàng Thu Thảo", "VIP", "0945678901 • KTX Khu B, P212", "50 đơn • Tham gia 05/09/2024", "2.7M", "https://picsum.photos/id/1027/200"),
            Customer("6", "Đặng Văn Lâm", "VIP", "0956789012 • KTX Khu A, P101", "45 đơn • Tham gia 12/09/2024", "2.1M", "https://picsum.photos/id/103/200"),
            Customer("7", "Vũ Thị Lan", "VIP", "0967890123 • KTX Khu C, P605", "70 đơn • Tham gia 01/08/2024", "4.0M", "https://picsum.photos/id/1035/200"),
            Customer("8", "Ngô Kiến Huy", "VIP", "0978901234 • KTX Khu B, P301", "52 đơn • Tham gia 15/09/2024", "2.8M", "https://picsum.photos/id/1040/200"),
            Customer("9", "Bùi Tiến Dũng", "VIP", "0989012345 • KTX Khu A, P205", "58 đơn • Tham gia 20/08/2024", "3.0M", "https://picsum.photos/id/1050/200"),
            Customer("10", "Đỗ Mỹ Linh", "VIP", "0990123456 • KTX Khu C, P401", "49 đơn • Tham gia 10/09/2024", "2.6M", "https://picsum.photos/id/106/200"),

            // --- KHÁCH HÀNG THƯỜNG XUYÊN (Ổn định) ---
            Customer("11", "Hồ Quang Hiếu", "Thường xuyên", "0901112223 • KTX Khu A, P303", "30 đơn • Tham gia 01/10/2024", "1.5M", "https://picsum.photos/id/1062/200"),
            Customer("12", "Dương Triệu Vũ", "Thường xuyên", "0912223334 • KTX Khu B, P404", "25 đơn • Tham gia 05/10/2024", "1.2M", "https://picsum.photos/id/1074/200"),
            Customer("13", "Cao Thái Sơn", "Thường xuyên", "0923334445 • KTX Khu C, P505", "28 đơn • Tham gia 10/10/2024", "1.4M", "https://picsum.photos/id/1084/200"),
            Customer("14", "Lý Nhã Kỳ", "Thường xuyên", "0934445556 • KTX Khu A, P202", "35 đơn • Tham gia 15/10/2024", "1.8M", "https://picsum.photos/id/111/200"),
            Customer("15", "Mai Phương Thúy", "Thường xuyên", "0945556667 • KTX Khu B, P306", "22 đơn • Tham gia 20/10/2024", "1.1M", "https://picsum.photos/id/129/200"),
            Customer("16", "Trấn Thành", "Thường xuyên", "0956667778 • KTX Khu C, P408", "40 đơn • Tham gia 25/10/2024", "2.0M", "https://picsum.photos/id/130/200"),
            Customer("17", "Trường Giang", "Thường xuyên", "0967778889 • KTX Khu A, P105", "32 đơn • Tham gia 01/11/2024", "1.6M", "https://picsum.photos/id/149/200"),
            Customer("18", "Ninh Dương Lan", "Thường xuyên", "0978889990 • KTX Khu B, P208", "27 đơn • Tham gia 05/11/2024", "1.3M", "https://picsum.photos/id/152/200"),
            Customer("19", "Phan Mạnh Quỳnh", "Thường xuyên", "0989990001 • KTX Khu C, P310", "20 đơn • Tham gia 10/11/2024", "1.0M", "https://picsum.photos/id/16/200"),
            Customer("20", "Sơn Tùng MTP", "Thường xuyên", "0902223331 • KTX Khu A, P501", "38 đơn • Tham gia 15/11/2024", "1.9M", "https://picsum.photos/id/164/200"),
            Customer("21", "Jack 97", "Thường xuyên", "0913334442 • KTX Khu B, P410", "24 đơn • Tham gia 20/11/2024", "1.2M", "https://picsum.photos/id/177/200"),
            Customer("22", "Đen Vâu", "Thường xuyên", "0924445553 • KTX Khu C, P204", "33 đơn • Tham gia 25/11/2024", "1.7M", "https://picsum.photos/id/18/200"),
            Customer("23", "Bích Phương", "Thường xuyên", "0935556664 • KTX Khu A, P308", "29 đơn • Tham gia 01/12/2024", "1.4M", "https://picsum.photos/id/193/200"),
            Customer("24", "Min", "Thường xuyên", "0946667775 • KTX Khu B, P102", "26 đơn • Tham gia 05/12/2024", "1.3M", "https://picsum.photos/id/201/200"),
            Customer("25", "Erik", "Thường xuyên", "0957778886 • KTX Khu C, P601", "21 đơn • Tham gia 08/12/2024", "1.0M", "https://picsum.photos/id/203/200"),
            Customer("26", "Đức Phúc", "Thường xuyên", "0968889997 • KTX Khu A, P405", "36 đơn • Tham gia 12/12/2024", "1.8M", "https://picsum.photos/id/204/200"),
            Customer("27", "Hòa Minzy", "Thường xuyên", "0979990008 • KTX Khu B, P210", "31 đơn • Tham gia 15/12/2024", "1.5M", "https://picsum.photos/id/206/200"),
            Customer("28", "Tóc Tiên", "Thường xuyên", "0980001119 • KTX Khu C, P302", "23 đơn • Tham gia 18/12/2024", "1.1M", "https://picsum.photos/id/208/200"),
            Customer("29", "Noo Phước Thịnh", "Thường xuyên", "0901113330 • KTX Khu A, P512", "34 đơn • Tham gia 20/12/2024", "1.7M", "https://picsum.photos/id/216/200"),
            Customer("30", "Đông Nhi", "Thường xuyên", "0912224441 • KTX Khu B, P108", "37 đơn • Tham gia 22/12/2024", "1.8M", "https://picsum.photos/id/217/200"),

            // --- KHÁCH HÀNG MỚI (Chi tiêu ít, mới tham gia) ---
            Customer("31", "Ông Cao Thắng", "Mới", "0923335552 • KTX Khu C, P206", "5 đơn • Tham gia 01/12/2024", "250K", "https://picsum.photos/id/237/200"),
            Customer("32", "Khởi My", "Mới", "0934446663 • KTX Khu A, P304", "3 đơn • Tham gia 05/12/2024", "150K", "https://picsum.photos/id/238/200"),
            Customer("33", "Kelvin Khánh", "Mới", "0945557774 • KTX Khu B, P406", "2 đơn • Tham gia 08/12/2024", "100K", "https://picsum.photos/id/239/200"),
            Customer("34", "Hari Won", "Mới", "0956668885 • KTX Khu C, P502", "1 đơn • Tham gia 10/12/2024", "50K", "https://picsum.photos/id/24/200"),
            Customer("35", "Tiến Luật", "Mới", "0967779996 • KTX Khu A, P106", "4 đơn • Tham gia 12/12/2024", "200K", "https://picsum.photos/id/25/200"),
            Customer("36", "Thu Trang", "Mới", "0978880007 • KTX Khu B, P203", "6 đơn • Tham gia 15/12/2024", "300K", "https://picsum.photos/id/26/200"),
            Customer("37", "Diệu Nhi", "Mới", "0989991118 • KTX Khu C, P307", "2 đơn • Tham gia 18/12/2024", "90K", "https://picsum.photos/id/27/200"),
            Customer("38", "Anh Tú", "Mới", "0902225559 • KTX Khu A, P403", "3 đơn • Tham gia 20/12/2024", "140K", "https://picsum.photos/id/28/200"),
            Customer("39", "Kiều Minh Tuấn", "Mới", "0913336660 • KTX Khu B, P506", "1 đơn • Tham gia 22/12/2024", "45K", "https://picsum.photos/id/29/200"),
            Customer("40", "Cát Phượng", "Mới", "0924447771 • KTX Khu C, P103", "5 đơn • Tham gia 24/12/2024", "240K", "https://picsum.photos/id/30/200"),
            Customer("41", "Lê Dương Bảo", "Mới", "0935558882 • KTX Khu A, P207", "2 đơn • Tham gia 25/12/2024", "95K", "https://picsum.photos/id/32/200"),
            Customer("42", "Hieuthuhai", "Mới", "0946669993 • KTX Khu B, P309", "4 đơn • Tham gia 26/12/2024", "190K", "https://picsum.photos/id/33/200"),
            Customer("43", "Mono Việt Hoàng", "Mới", "0957770004 • KTX Khu C, P407", "3 đơn • Tham gia 27/12/2024", "135K", "https://picsum.photos/id/34/200"),
            Customer("44", "Phương Ly", "Mới", "0968881115 • KTX Khu A, P504", "1 đơn • Tham gia 28/12/2024", "55K", "https://picsum.photos/id/35/200"),
            Customer("45", "JustaTee", "Mới", "0979992226 • KTX Khu B, P104", "6 đơn • Tham gia 29/12/2024", "290K", "https://picsum.photos/id/36/200"),
            Customer("46", "Rhymastic", "Mới", "0980003337 • KTX Khu C, P209", "2 đơn • Tham gia 29/12/2024", "85K", "https://picsum.photos/id/39/200"),
            Customer("47", "Suboi", "Mới", "0901114448 • KTX Khu A, P312", "5 đơn • Tham gia 30/12/2024", "230K", "https://picsum.photos/id/40/200"),
            Customer("48", "Karik", "Mới", "0912225559 • KTX Khu B, P411", "3 đơn • Tham gia 30/12/2024", "160K", "https://picsum.photos/id/41/200"),
            Customer("49", "Wowy", "Mới", "0923336660 • KTX Khu C, P508", "1 đơn • Tham gia 31/12/2024", "60K", "https://picsum.photos/id/42/200"),
            Customer("50", "Binz Da Poet", "Mới", "0934447771 • KTX Khu A, P109", "4 đơn • Tham gia 31/12/2024", "210K", "https://picsum.photos/id/43/200")
        )
    }

    /**
     * READ: Lấy tất cả khách hàng.
     * Phương thức này trả về một StateFlow, cho phép UI lắng nghe sự thay đổi dữ liệu theo thời gian thực
     * (ví dụ: sau khi thêm hoặc xóa).
     * @return Một StateFlow chứa danh sách khách hàng.
     */
    override fun getCustomers(): Flow<List<Customer>> {
        // Chỉ cần trả về StateFlow đã có.
        // Bất cứ khi nào _internalCustomersFlow thay đổi, Flow này cũng sẽ phát ra giá trị mới.
        return _internalCustomersFlow.asStateFlow()
    }

    /**
     * Mô phỏng việc gọi API để lấy danh sách khách hàng chỉ một lần.
     * Phương thức này sử dụng 'flow' builder để tạo ra một cold flow.
     * Nó sẽ phát ra danh sách khách hàng và sau đó kết thúc.
     * Thêm 'delay' để giả lập độ trễ mạng.
     * @return Một Flow chỉ phát ra dữ liệu một lần.
     */
    fun fetchCustomersFromApi(): Flow<List<Customer>> = flow {
        // Giả lập độ trễ mạng
        delay(1000) // Chờ 1 giây
        // Phát ra giá trị hiện tại của danh sách
        emit(_internalCustomersFlow.value)
    }


    /**
     * CREATE: Thêm một khách hàng mới vào danh sách.
     * @param customer Khách hàng mới cần thêm.
     */
    override fun addCustomer(customer: Customer) {
        _internalCustomersFlow.update { currentList ->
            // Đảm bảo không trùng ID, tạo ID mới nếu cần
            val newId = (currentList.maxOfOrNull { it.id.toInt() } ?: 0) + 1
            currentList + customer.copy(id = newId.toString())
        }
    }

    /**
     * READ: Tìm một khách hàng theo ID.
     * @param id ID của khách hàng cần tìm.
     * @return Khách hàng tìm thấy hoặc null.
     */
    fun getCustomerById(id: String): Customer? {
        return _internalCustomersFlow.value.find { it.id == id }
    }

    /**
     * UPDATE: Cập nhật thông tin của một khách hàng đã tồn tại.
     * @param updatedCustomer Khách hàng với thông tin đã được cập nhật.
     */
    override fun updateCustomer(updatedCustomer: Customer) {
        _internalCustomersFlow.update { currentList ->
            currentList.map { customer ->
                if (customer.id == updatedCustomer.id) {
                    updatedCustomer // Thay thế khách hàng cũ bằng khách hàng mới
                } else {
                    customer
                }
            }
        }
    }

    /**
     * DELETE: Xóa một khách hàng khỏi danh sách.
     * @param customerId ID của khách hàng cần xóa.
     */
    override fun deleteCustomer(customerId: String) {
        _internalCustomersFlow.update { currentList ->
            currentList.filterNot { it.id == customerId }
        }
    }
}
