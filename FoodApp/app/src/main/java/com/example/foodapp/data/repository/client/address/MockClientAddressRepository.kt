package com.example.foodapp.data.repository.client.address

import com.example.foodapp.data.model.client.DeliveryAddress
import com.example.foodapp.data.repository.customer.base.ClientAddressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Mock implementation của ClientAddressRepository cho mục đích testing và development.
 * Quản lý địa chỉ giao hàng với dữ liệu giả lập trong bộ nhớ.
 */
class MockClientAddressRepository : ClientAddressRepository {

    // State flow để quản lý danh sách địa chỉ
    private val _addresses = MutableStateFlow<List<DeliveryAddress>>(emptyList())

    init {
        // Khởi tạo dữ liệu địa chỉ mẫu
        initializeMockAddresses()
    }

    /**
     * Khởi tạo danh sách địa chỉ mẫu.
     */
    private fun initializeMockAddresses() {
        _addresses.value = listOf(
            DeliveryAddress(
                id = "addr_001",
                clientId = "client_001",
                name = "Nguyễn Văn A",
                phone = "0901234567",
                address = "123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM",
                note = "Chung cư Sunrise, tầng 5, căn hộ 501",
                isDefault = true,
                latitude = 10.772,
                longitude = 106.698
            ),
            DeliveryAddress(
                id = "addr_002",
                clientId = "client_001",
                name = "Nguyễn Văn A",
                phone = "0901234567",
                address = "456 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM",
                note = "Văn phòng công ty, phòng 302",
                isDefault = false,
                latitude = 10.776,
                longitude = 106.702
            ),
            DeliveryAddress(
                id = "addr_003",
                clientId = "client_001",
                name = "Trần Thị B (Vợ)",
                phone = "0907654321",
                address = "789 Đường Cách Mạng Tháng 8, Phường 11, Quận 3, TP.HCM",
                note = "Nhà riêng, cổng màu xanh",
                isDefault = false,
                latitude = 10.781,
                longitude = 106.688
            )
        )
    }

    override fun getAddresses(): Flow<List<DeliveryAddress>> = _addresses

    override suspend fun addAddress(address: DeliveryAddress): Result<Unit> {
        val newAddress = if (address.id.isEmpty()) {
            address.copy(id = "addr_${UUID.randomUUID()}")
        } else {
            address
        }

        // Nếu đây là địa chỉ mặc định, bỏ default của các địa chỉ khác
        val updatedAddresses = if (newAddress.isDefault) {
            _addresses.value.map { it.copy(isDefault = false) } + newAddress
        } else {
            _addresses.value + newAddress
        }

        _addresses.value = updatedAddresses
        return Result.success(Unit)
    }

    override suspend fun updateAddress(address: DeliveryAddress): Result<Unit> {
        val index = _addresses.value.indexOfFirst { it.id == address.id }

        if (index == -1) {
            return Result.failure(Exception("Địa chỉ không tồn tại"))
        }

        val updatedAddresses = _addresses.value.toMutableList()

        // Nếu địa chỉ này được đặt làm mặc định
        if (address.isDefault) {
            // Bỏ default của tất cả địa chỉ khác
            updatedAddresses.replaceAll { it.copy(isDefault = false) }
        }

        updatedAddresses[index] = address
        _addresses.value = updatedAddresses

        return Result.success(Unit)
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        val addressToDelete = _addresses.value.find { it.id == addressId }
            ?: return Result.failure(Exception("Địa chỉ không tồn tại"))

        // Không cho phép xóa địa chỉ mặc định nếu đây là địa chỉ duy nhất
        if (addressToDelete.isDefault && _addresses.value.size == 1) {
            return Result.failure(Exception("Không thể xóa địa chỉ mặc định duy nhất"))
        }

        val updatedAddresses = _addresses.value.filter { it.id != addressId }
        _addresses.value = updatedAddresses

        // Nếu xóa địa chỉ mặc định, đặt địa chỉ đầu tiên làm mặc định
        if (addressToDelete.isDefault && updatedAddresses.isNotEmpty()) {
            val newDefaultAddress = updatedAddresses.first().copy(isDefault = true)
            return updateAddress(newDefaultAddress)
        }

        return Result.success(Unit)
    }

    override suspend fun setDefaultAddress(addressId: String): Result<Unit> {
        val address = _addresses.value.find { it.id == addressId }
            ?: return Result.failure(Exception("Địa chỉ không tồn tại"))

        return updateAddress(address.copy(isDefault = true))
    }

    override fun getDefaultAddress(): Flow<DeliveryAddress?> {
        return _addresses.map { addresses ->
            addresses.find { it.isDefault }
        }
    }

    override fun getAddressById(addressId: String): Flow<DeliveryAddress?> {
        return _addresses.map { addresses ->
            addresses.find { it.id == addressId }
        }
    }
}