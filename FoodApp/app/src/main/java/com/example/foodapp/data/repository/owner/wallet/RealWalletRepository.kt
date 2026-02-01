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
                val walletDto = wrapper?.data?.wallet
                if (walletDto != null) {
                    Log.d(TAG, "✅ Got wallet: balance=${walletDto.balance}")
                    Result.success(walletDto.toWallet())
                } else {
                    Log.e(TAG, "❌ Wallet data is null. Wrapper: $wrapper")
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
                val ledgerData = wrapper?.data
                if (ledgerData != null) {
                    val entries = ledgerData.entries?.map { it.toLedgerEntry() } ?: emptyList()
                    Log.d(TAG, "✅ Got ${entries.size} ledger entries")
                    Result.success(LedgerResult(
                        entries = entries,
                        page = ledgerData.page,
                        limit = ledgerData.limit,
                        total = ledgerData.total,
                        totalPages = ledgerData.totalPages
                    ))
                } else {
                    Log.e(TAG, "❌ Ledger data is null. Wrapper: $wrapper")
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
                val payoutData = wrapper?.data
                val payoutDto = payoutData?.payoutRequest
                if (payoutDto != null) {
                    Log.d(TAG, "✅ Payout request created: id=${payoutDto.id}")
                    Result.success(payoutDto.toPayoutRequest())
                } else {
                    Log.d(TAG, "✅ Payout request created (no details returned). Message: ${payoutData?.message}")
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
                val revenueData = wrapper?.data
                if (revenueData != null) {
                    val dailyBreakdown = revenueData.dailyBreakdown?.map { it.toDailyRevenue() } ?: emptyList()
                    Log.d(TAG, "✅ Got revenue: today=${revenueData.today}, month=${revenueData.month}")
                    Result.success(RevenueStats(
                        today = revenueData.today,
                        week = revenueData.week,
                        month = revenueData.month,
                        year = revenueData.year,
                        all = revenueData.all,
                        dailyBreakdown = dailyBreakdown,
                        calculatedAt = revenueData.calculatedAt ?: ""
                    ))
                } else {
                    Log.e(TAG, "❌ Revenue data is null. Wrapper: $wrapper")
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
