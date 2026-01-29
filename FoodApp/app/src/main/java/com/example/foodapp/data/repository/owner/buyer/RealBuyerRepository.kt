package com.example.foodapp.data.repository.owner.buyer

import android.util.Log
import com.example.foodapp.data.model.owner.buyer.BuyerDetail
import com.example.foodapp.data.model.owner.buyer.PaginatedBuyerList
import com.example.foodapp.data.remote.owner.BuyerApiService
import org.json.JSONObject
import retrofit2.Response

/**
 * Real Repository cho Buyer Management
 * Kết nối với backend API
 */
class RealBuyerRepository(
    private val apiService: BuyerApiService
) {
    private val TAG = "BuyerRepository"

    /**
     * Lấy danh sách buyers với filter và pagination
     */
    suspend fun listBuyers(
        page: Int = 1,
        limit: Int = 20,
        tier: String = "ALL",
        search: String? = null,
        sort: String = "createdAt"
    ): Result<PaginatedBuyerList> {
        return try {
            Log.d(TAG, "🔍 Fetching buyers: page=$page, limit=$limit, tier=$tier, search=$search")
            val response = apiService.listBuyers(page, limit, tier, search, sort)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                Log.d(TAG, "✅ Got ${data?.items?.size ?: 0} buyers, total: ${data?.pagination?.total ?: 0}")
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching buyers: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching buyers", e)
            Result.failure(e)
        }
    }

    /**
     * Lấy chi tiết buyer với recent orders
     */
    suspend fun getBuyerDetail(customerId: String): Result<BuyerDetail> {
        return try {
            Log.d(TAG, "🔍 Fetching buyer detail: $customerId")
            val response = apiService.getBuyerDetail(customerId)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                Log.d(TAG, "✅ Got buyer detail: ${data?.displayName}")
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching buyer detail: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching buyer detail", e)
            Result.failure(e)
        }
    }

    private fun <T> parseErrorBody(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "Error: ${response.code()}")
            } else {
                "Error: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            "Error: ${response.code()} ${response.message()}"
        }
    }
}
