package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.Customer
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho Customer Repository của Owner.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface OwnerCustomerRepository {
    
    /**
     * Lấy danh sách tất cả khách hàng (sử dụng Flow để real-time update)
     */
    fun getCustomers(): Flow<List<Customer>>
    
    /**
     * Thêm khách hàng mới
     */
    fun addCustomer(customer: Customer)
    
    /**
     * Cập nhật thông tin khách hàng
     */
    fun updateCustomer(updated: Customer)
    
    /**
     * Xóa khách hàng
     */
    fun deleteCustomer(customerId: String)
}
