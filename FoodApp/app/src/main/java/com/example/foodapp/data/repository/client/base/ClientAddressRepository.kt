package com.example.foodapp.data.repository.customer.base

import com.example.foodapp.data.model.client.DeliveryAddress
import kotlinx.coroutines.flow.Flow

/**
 * Interface repository cho việc quản lý địa chỉ giao hàng của người mua.
 * Xử lý thêm, sửa, xóa và quản lý địa chỉ mặc định.
 */
interface ClientAddressRepository {

    /**
     * Lấy danh sách tất cả địa chỉ giao hàng của người dùng hiện tại.
     * @return Flow phát ra danh sách các DeliveryAddress
     */
    fun getAddresses(): Flow<List<DeliveryAddress>>

    /**
     * Thêm địa chỉ giao hàng mới.
     * @param address Đối tượng DeliveryAddress cần thêm
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun addAddress(address: DeliveryAddress): Result<Unit>

    /**
     * Cập nhật thông tin địa chỉ giao hàng.
     * @param address Đối tượng DeliveryAddress đã được cập nhật
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun updateAddress(address: DeliveryAddress): Result<Unit>

    /**
     * Xóa một địa chỉ giao hàng.
     * @param addressId ID của địa chỉ cần xóa
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun deleteAddress(addressId: String): Result<Unit>

    /**
     * Thiết lập một địa chỉ làm địa chỉ mặc định.
     * @param addressId ID của địa chỉ cần đặt làm mặc định
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun setDefaultAddress(addressId: String): Result<Unit>

    /**
     * Lấy địa chỉ giao hàng mặc định của người dùng.
     * @return Flow phát ra DeliveryAddress mặc định hoặc null nếu chưa có
     */
    fun getDefaultAddress(): Flow<DeliveryAddress?>

    /**
     * Lấy thông tin chi tiết của một địa chỉ cụ thể.
     * @param addressId ID của địa chỉ cần lấy
     * @return Flow phát ra DeliveryAddress hoặc null nếu không tồn tại
     */
    fun getAddressById(addressId: String): Flow<DeliveryAddress?>
}