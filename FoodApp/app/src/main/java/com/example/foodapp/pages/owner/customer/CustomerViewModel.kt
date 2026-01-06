package com.example.foodapp.pages.owner.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * File này định nghĩa ViewModel cho màn hình CustomerScreen.
 *
 * ViewModel đóng vai trò là "bộ não" của màn hình, có các nhiệm vụ chính:
 * 1. Giao tiếp với lớp Repository (nguồn dữ liệu) để lấy và cập nhật dữ liệu.
 * 2. Giữ và quản lý trạng thái giao diện (CustomerUiState) trong vòng đời của màn hình,
 *    giúp trạng thái không bị mất khi xoay màn hình hay có thay đổi cấu hình.
 * 3. Cung cấp các hàm để xử lý sự kiện từ người dùng (ví dụ: thay đổi bộ lọc).
 * 4. Tách biệt logic nghiệp vụ khỏi tầng giao diện (Composable).
 */
class CustomerViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    private val customerRepository = RepositoryProvider.getCustomerRepository()

    // StateFlow nội bộ, chỉ ViewModel mới có quyền chỉnh sửa.
    private val _uiState = MutableStateFlow(CustomerUiState())

    // StateFlow công khai, chỉ cho phép đọc từ bên ngoài (UI).
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        // Ngay khi ViewModel được tạo, bắt đầu lắng nghe sự thay đổi từ Repository.
        loadCustomers()
    }

    /**
     * Lắng nghe luồng dữ liệu khách hàng từ Repository và cập nhật UI State.
     */
    private fun loadCustomers() {
        // viewModelScope tự động hủy coroutine khi ViewModel bị hủy, tránh rò rỉ bộ nhớ.
        viewModelScope.launch {
            // Bắt đầu lắng nghe Flow từ repository
            customerRepository.getCustomers().collect { customers ->
                // Cập nhật StateFlow với danh sách khách hàng mới nhất.
                _uiState.update { currentState ->
                    currentState.copy(customers = customers)
                }
            }
        }
    }

    /**
     * Xử lý sự kiện khi người dùng thay đổi bộ lọc.
     * @param newFilter Bộ lọc mới được chọn ("Tất cả", "VIP", ...).
     */
    fun onFilterChanged(newFilter: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedFilter = newFilter)
        }
    }

    // --- HÀM BỊ THIẾU ĐƯỢC THÊM VÀO ĐÂY ---
    /**
     * Cập nhật trạng thái query tìm kiếm khi người dùng nhập liệu vào SearchBar.
     * @param newQuery Nội dung mới từ thanh tìm kiếm.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { currentState ->
            currentState.copy(searchQuery = newQuery)
        }
    }
}
