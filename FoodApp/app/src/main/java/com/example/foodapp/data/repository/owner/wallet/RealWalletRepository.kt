package com.example.foodapp.data.repository.owner.wallet

import android.util.Log
import com.example.foodapp.data.model.owner.wallet.*
import com.example.foodapp.data.remote.owner.WalletApiService
import com.example.foodapp.data.repository.owner.base.LedgerResult
import com.example.foodapp.data.repository.owner.base.OwnerWalletRepository
import org.json.JSONObject
import retrofit2.Response

/**
 * Real Repository for Owner Wallet
 * Connects to backend API for wallet operations
 */
class RealWalletRepository(
    private val apiService: WalletApiService
) : OwnerWalletRepository {

    private val TAG = "WalletRepository"

    /**
     * Get current user's wallet
     */
    override suspend fun getMyWallet(): Result<Wallet> {
        return try {
            Log.d(TAG, "🔍 Fetching wallet...")
            val response = apiService.getMyWallet()
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val walletDto = wrapper?.wallet
                if (walletDto != null) {
                    Log.d(TAG, "✅ Got wallet: balance=${walletDto.balance}")
                    Result.success(walletDto.toWallet())
                } else {
                    Log.e(TAG, "❌ Wallet data is null")
                    Result.failure(Exception("Wallet data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching wallet: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching wallet", e)
            Result.failure(e)
        }
    }

    /**
     * Get ledger (transaction history)
     */
    override suspend fun getLedger(page: Int, limit: Int): Result<LedgerResult> {
        return try {
            Log.d(TAG, "🔍 Fetching ledger: page=$page, limit=$limit")
            val response = apiService.getLedger(page, limit)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                if (wrapper != null) {
                    val entries = wrapper.entries?.map { it.toLedgerEntry() } ?: emptyList()
                    Log.d(TAG, "✅ Got ${entries.size} ledger entries")
                    Result.success(LedgerResult(
                        entries = entries,
                        page = wrapper.page,
                        limit = wrapper.limit,
                        total = wrapper.total,
                        totalPages = wrapper.totalPages
                    ))
                } else {
                    Result.failure(Exception("Ledger data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching ledger: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching ledger", e)
            Result.failure(e)
        }
    }

    /**
     * Request payout (withdraw funds)
     */
    override suspend fun requestPayout(request: RequestPayoutRequest): Result<PayoutRequest> {
        return try {
            Log.d(TAG, "🔍 Requesting payout: amount=${request.amount}")
            val response = apiService.requestPayout(request)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val payoutDto = wrapper?.payoutRequest
                if (payoutDto != null) {
                    Log.d(TAG, "✅ Payout request created: id=${payoutDto.id}")
                    Result.success(payoutDto.toPayoutRequest())
                } else {
                    Log.d(TAG, "✅ Payout request created (no details returned)")
                    // Return a placeholder if no details returned
                    Result.success(PayoutRequest(
                        id = "",
                        amount = request.amount,
                        status = "PENDING",
                        bankCode = request.bankCode,
                        accountNumber = request.accountNumber,
                        accountName = request.accountName,
                        createdAt = ""
                    ))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error requesting payout: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception requesting payout", e)
            Result.failure(e)
        }
    }

    /**
     * Get revenue statistics
     */
    override suspend fun getRevenue(period: RevenuePeriod): Result<RevenueStats> {
        return try {
            Log.d(TAG, "🔍 Fetching revenue: period=${period.apiValue}")
            val response = apiService.getRevenue(period.apiValue)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                if (wrapper != null) {
                    val dailyBreakdown = wrapper.dailyBreakdown?.map { it.toDailyRevenue() } ?: emptyList()
                    Log.d(TAG, "✅ Got revenue: today=${wrapper.today}, month=${wrapper.month}")
                    Result.success(RevenueStats(
                        today = wrapper.today,
                        week = wrapper.week,
                        month = wrapper.month,
                        year = wrapper.year,
                        all = wrapper.all,
                        dailyBreakdown = dailyBreakdown,
                        calculatedAt = wrapper.calculatedAt ?: ""
                    ))
                } else {
                    Result.failure(Exception("Revenue data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching revenue: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching revenue", e)
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
