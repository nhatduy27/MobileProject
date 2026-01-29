package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.removal.CreateRemovalRequestDto
import com.example.foodapp.data.model.shipper.removal.RemovalRequest
import com.example.foodapp.data.model.shipper.removal.RemovalRequestStatus

/**
 * Repository interface for Shipper Removal Requests
 */
interface RemovalRequestRepository {
    
    /**
     * Create a new removal request to leave a shop
     */
    suspend fun createRemovalRequest(dto: CreateRemovalRequestDto): Result<RemovalRequest>
    
    /**
     * Get list of shipper's removal requests
     * @param status Optional filter by status (PENDING, APPROVED, REJECTED)
     */
    suspend fun getMyRemovalRequests(status: RemovalRequestStatus? = null): Result<List<RemovalRequest>>
}
