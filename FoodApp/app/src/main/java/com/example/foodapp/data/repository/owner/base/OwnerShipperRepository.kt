package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.shipper.*

/**
 * Interface cho Owner Shipper Repository
 */
interface OwnerShipperRepository {

    /**
     * Lấy danh sách đơn xin làm shipper
     */
    suspend fun getApplications(status: ApplicationStatus? = null): Result<List<ShipperApplication>>

    /**
     * Duyệt đơn xin làm shipper
     */
    suspend fun approveApplication(applicationId: String): Result<String>

    /**
     * Từ chối đơn xin làm shipper
     */
    suspend fun rejectApplication(applicationId: String, reason: String): Result<String>

    /**
     * Lấy danh sách shipper đang hoạt động
     */
    suspend fun getShippers(): Result<List<Shipper>>

    /**
     * Xóa shipper khỏi shop
     */
    suspend fun removeShipper(shipperId: String): Result<String>
}
