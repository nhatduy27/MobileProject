package com.example.foodapp.data.repository.shipper.wallet

import android.util.Log
import com.example.foodapp.data.model.shipper.wallet.*
import com.example.foodapp.data.remote.shipper.wallet.PayoutRequestBody
import com.example.foodapp.data.remote.shipper.wallet.WalletApiService
import com.example.foodapp.data.repository.shipper.base.LedgerResult
import com.example.foodapp.data.repository.shipper.base.ShipperWalletRepository

/**
 * Real implementation của ShipperWalletRepository
 * Kết nối với backend API để lấy dữ liệu ví
 */
class RealShipperWalletRepository(
    private val apiService: WalletApiService
) : ShipperWalletRepository {
    
    companion object {
        private const val TAG = "RealWalletRepo"
    }
    
    override suspend fun getMyWallet(): Result<Wallet> {
        return try {
            Log.d(TAG, "Fetching my wallet...")
            val response = apiService.getMyWallet()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Log.d(TAG, "Wallet fetched successfully: ${body.data.wallet.id}")
                    Result.success(body.data.wallet)
                } else {
                    Log.e(TAG, "Wallet response not successful")
                    Result.failure(Exception("Không thể lấy thông tin ví"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error fetching wallet: ${response.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Lỗi khi lấy thông tin ví"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching wallet", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getLedger(page: Int, limit: Int): Result<LedgerResult> {
        return try {
            Log.d(TAG, "Fetching ledger: page=$page, limit=$limit")
            val response = apiService.getLedger(page, limit)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val data = body.data
                    Log.d(TAG, "Ledger fetched: ${data.entries.size} entries, total=${data.total}")
                    Result.success(
                        LedgerResult(
                            entries = data.entries,
                            page = data.page,
                            limit = data.limit,
                            total = data.total,
                            totalPages = data.totalPages
                        )
                    )
                } else {
                    Log.e(TAG, "Ledger response not successful")
                    Result.failure(Exception("Không thể lấy lịch sử giao dịch"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error fetching ledger: ${response.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Lỗi khi lấy lịch sử giao dịch"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching ledger", e)
            Result.failure(e)
        }
    }
    
    override suspend fun requestPayout(
        amount: Long,
        bankCode: String,
        accountNumber: String,
        accountName: String,
        note: String?
    ): Result<PayoutRequest> {
        return try {
            Log.d(TAG, "Requesting payout: amount=$amount, bank=$bankCode")
            
            val request = PayoutRequestBody(
                amount = amount,
                bankCode = bankCode,
                accountNumber = accountNumber,
                accountName = accountName,
                note = note
            )
            
            val response = apiService.requestPayout(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data?.payoutRequest != null) {
                    Log.d(TAG, "Payout request created: ${body.data.payoutRequest.id}")
                    Result.success(body.data.payoutRequest)
                } else {
                    Log.e(TAG, "Payout response not successful")
                    Result.failure(Exception("Không thể tạo yêu cầu rút tiền"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error requesting payout: ${response.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Lỗi khi yêu cầu rút tiền"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting payout", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getRevenue(period: RevenuePeriod): Result<RevenueStats> {
        return try {
            Log.d(TAG, "Fetching revenue: period=${period.value}")
            val response = apiService.getRevenue(period.value)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Log.d(TAG, "Revenue fetched: today=${body.data.today}, month=${body.data.month}")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "Revenue response not successful")
                    Result.failure(Exception("Không thể lấy thống kê doanh thu"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error fetching revenue: ${response.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Lỗi khi lấy thống kê doanh thu"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching revenue", e)
            Result.failure(e)
        }
    }
    
    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrEmpty()) return null
        return try {
            // Try to parse JSON error
            val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(errorBody)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
