package com.example.foodapp.data.repository.owner.shippers

import android.util.Log
import com.example.foodapp.data.model.owner.shipper.*
import com.example.foodapp.data.remote.owner.ShipperApiService
import com.example.foodapp.data.repository.owner.base.OwnerShipperRepository

/**
 * Real implementation của OwnerShipperRepository
 * Gọi API thực sự từ backend
 */
class RealShipperRepository(
    private val apiService: ShipperApiService
) : OwnerShipperRepository {

    companion object {
        private const val TAG = "RealShipperRepository"
    }

    override suspend fun getApplications(status: ApplicationStatus?): Result<List<ShipperApplication>> {
        return try {
            Log.d(TAG, "🔄 Fetching applications (client-side filter: $status)")

            // IMPORTANT: Always fetch ALL applications (no status param)
            // to avoid Firestore composite index requirement.
            // We'll filter by status on client-side instead.
            val response = apiService.getApplications(status = null)

            if (response.isSuccessful && response.body() != null) {
                var applications = response.body()!!.data
                
                // Client-side filter by status if needed
                if (status != null) {
                    applications = applications.filter { it.status == status }
                    Log.d(TAG, "✅ Got ${applications.size} applications (filtered for $status)")
                } else {
                    Log.d(TAG, "✅ Got ${applications.size} applications (all)")
                }
                
                Result.success(applications)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error fetching applications: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching applications", e)
            Result.failure(e)
        }
    }

    override suspend fun approveApplication(applicationId: String): Result<String> {
        return try {
            Log.d(TAG, "🔄 Approving application: $applicationId")

            val response = apiService.approveApplication(applicationId)

            if (response.isSuccessful && response.body() != null) {
                val message = response.body()!!.message
                Log.d(TAG, "✅ Approved: $message")
                Result.success(message)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error approving application: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception approving application", e)
            Result.failure(e)
        }
    }

    override suspend fun rejectApplication(applicationId: String, reason: String): Result<String> {
        return try {
            Log.d(TAG, "🔄 Rejecting application: $applicationId")

            val request = RejectApplicationRequest(reason)
            val response = apiService.rejectApplication(applicationId, request)

            if (response.isSuccessful && response.body() != null) {
                val message = response.body()!!.message
                Log.d(TAG, "✅ Rejected: $message")
                Result.success(message)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error rejecting application: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception rejecting application", e)
            Result.failure(e)
        }
    }

    override suspend fun getShippers(): Result<List<Shipper>> {
        return try {
            Log.d(TAG, "🔄 Fetching shippers")

            val response = apiService.getShippers()

            if (response.isSuccessful && response.body() != null) {
                val shippers = response.body()!!.data
                Log.d(TAG, "✅ Got ${shippers.size} shippers")
                Result.success(shippers)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error fetching shippers: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching shippers", e)
            Result.failure(e)
        }
    }

    override suspend fun removeShipper(shipperId: String): Result<String> {
        return try {
            Log.d(TAG, "🔄 Removing shipper: $shipperId")

            val response = apiService.removeShipper(shipperId)

            if (response.isSuccessful && response.body() != null) {
                val message = response.body()!!.message
                Log.d(TAG, "✅ Removed: $message")
                Result.success(message)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error removing shipper: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception removing shipper", e)
            Result.failure(e)
        }
    }
}
