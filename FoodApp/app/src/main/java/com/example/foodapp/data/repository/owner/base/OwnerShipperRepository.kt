package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.Shipper
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho Shipper Repository của Owner (quản lý shipper).
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerShipperRepository {
    
    /**
     * Lấy danh sách tất cả shipper (sử dụng Flow để real-time update)
     */
    fun getShippers(): Flow<List<Shipper>>
    
    /**
     * Thêm shipper mới
     */
    fun addShipper(shipper: Shipper)
    
    /**
     * Cập nhật thông tin shipper
     */
    fun updateShipper(updated: Shipper)
    
    /**
     * Xóa shipper
     */
    fun deleteShipper(shipperId: String)
}
