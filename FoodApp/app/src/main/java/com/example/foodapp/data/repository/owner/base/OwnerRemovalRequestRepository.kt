package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequest
import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequestStatus

/**
 * Repository interface for Owner Removal Requests management
 */
interface OwnerRemovalRequestRepository {
    /**
     * Get list of removal requests for a shop
     * @param shopId Shop ID to get requests for
     * @param status Optional status filter
     * @return Result containing list of removal requests
     */
    suspend fun getShopRemovalRequests(
        shopId: String,
        status: OwnerRemovalRequestStatus? = null
    ): Result<List<OwnerRemovalRequest>>
    
    /**
     * Approve a removal request
     * @param requestId Request ID to approve
     * @return Result containing the updated request
     */
    suspend fun approveRequest(requestId: String): Result<OwnerRemovalRequest>
    
    /**
     * Reject a removal request
     * @param requestId Request ID to reject
     * @param reason Reason for rejection
     * @return Result containing the updated request
     */
    suspend fun rejectRequest(requestId: String, reason: String): Result<OwnerRemovalRequest>
}
