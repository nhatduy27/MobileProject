package com.example.foodapp.data.repository.firebase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.Date

class AuthManager(private val context: Context) {

    // Firebase Auth instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // SharedPreferences instances
    private val authPrefs: SharedPreferences =
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val userPrefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        // Keys for SharedPreferences
        private const val KEY_FIREBASE_TOKEN = "firebase_id_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_LAST_REFRESH = "last_token_refresh"

        // Token expiry buffer (refresh trước khi hết hạn 5 phút)
        private const val TOKEN_EXPIRY_BUFFER = 5 * 60 * 1000L // 5 minutes
    }

    // ==================== TOKEN MANAGEMENT ====================

    /**
     * Lưu Firebase ID Token với thời gian hết hạn
     * @param idToken Firebase ID Token
     * @param expiryTime Thời gian hết hạn (timestamp), mặc định 1 giờ
     */
    fun saveFirebaseToken(idToken: String, expiryTime: Long? = null) {
        try {
            val editor = authPrefs.edit()

            // Save token
            editor.putString(KEY_FIREBASE_TOKEN, idToken)

            // Save expiry time (default 1 hour if not provided)
            val expiry = expiryTime ?: (System.currentTimeMillis() + 60 * 60 * 1000)
            editor.putLong(KEY_TOKEN_EXPIRY, expiry)

            // Save last refresh time
            editor.putLong(KEY_LAST_REFRESH, System.currentTimeMillis())

            editor.apply()

            Log.d("AuthManager", "✅ Đã lưu Firebase ID Token")
            Log.d("AuthManager", "   Token: ${idToken.take(10)}...")
            Log.d("AuthManager", "   Hết hạn: ${Date(expiry)}")

        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Lỗi khi lưu token", e)
        }
    }

    /**
     * Kiểm tra token còn valid không
     * @return true nếu token còn valid, false nếu không
     */
    fun isTokenValid(): Boolean {
        val token = authPrefs.getString(KEY_FIREBASE_TOKEN, null)
        val expiryTime = authPrefs.getLong(KEY_TOKEN_EXPIRY, 0)

        // Token valid nếu tồn tại và chưa hết hạn (trừ buffer 5 phút)
        val isValid = !token.isNullOrEmpty() &&
                (expiryTime - TOKEN_EXPIRY_BUFFER) > System.currentTimeMillis()

        if (!isValid) {
            Log.w("AuthManager", "⚠ Token không hợp lệ hoặc sắp hết hạn")
            Log.w("AuthManager", "   Token exists: ${!token.isNullOrEmpty()}")
            Log.w("AuthManager", "   Expiry time: ${Date(expiryTime)}")
            Log.w("AuthManager", "   Current time: ${Date(System.currentTimeMillis())}")
        }

        return isValid
    }

    /**
     * Lấy token hiện tại (nếu valid)
     * @return Firebase ID Token hoặc null nếu không valid
     */
    fun getCurrentToken(): String? {
        return if (isTokenValid()) {
            authPrefs.getString(KEY_FIREBASE_TOKEN, null)
        } else {
            null
        }
    }

    /**
     * Refresh Firebase token
     * @return New token hoặc null nếu refresh thất bại
     */
    suspend fun refreshFirebaseToken(): String? {
        return try {
            Log.d("AuthManager", "🔄 Đang refresh Firebase token...")

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w("AuthManager", "❌ Không có user để refresh token")
                clearAuthData()
                return null
            }

            // Force refresh từ Firebase
            val tokenResult = currentUser.getIdToken(true).await()
            val newToken = tokenResult.token

            if (newToken != null) {
                // Lưu token mới với expiry time từ Firebase
                saveFirebaseToken(newToken, tokenResult.expirationTimestamp)
                Log.d("AuthManager", "✅ Đã refresh token mới")
                Log.d("AuthManager", "   New expiry: ${Date(tokenResult.expirationTimestamp)}")
            } else {
                Log.e("AuthManager", "❌ Firebase trả về null token")
            }

            newToken

        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Lỗi khi refresh token", e)
            null
        }
    }

    /**
     * Lấy token valid (tự động refresh nếu cần)
     * @return Valid token hoặc null nếu không thể lấy
     */
    suspend fun getValidToken(): String? {
        // 1. Kiểm tra token hiện tại còn valid không
        if (isTokenValid()) {
            Log.d("AuthManager", "✅ Token còn valid, sử dụng token cache")
            return getCurrentToken()
        }

        // 2. Token không valid, thử refresh
        Log.d("AuthManager", "🔁 Token không valid, đang refresh...")
        return refreshFirebaseToken()
    }

    /**
     * Xử lý khi API trả về 401 (Unauthorized)
     * @return true nếu refresh thành công, false nếu thất bại
     */
    suspend fun handleUnauthorizedError(): Boolean {
        Log.d("AuthManager", "🔐 API trả 401, đang refresh token...")

        // Thử refresh token
        val newToken = refreshFirebaseToken()

        return newToken != null
    }

    fun signInWithCustomToken(customToken: String, callback: (Boolean, String?, Exception?) -> Unit) {
        auth.signInWithCustomToken(customToken) //Lưu thông tin Firebase Auth
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.getIdToken(false)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token

                            if (idToken != null) {
                                saveAuthToken(idToken)
                            }
                            callback(true, idToken, null)
                        } else {
                            callback(true, null, tokenTask.exception)
                        }
                    }
                } else {
                    callback(false, null, task.exception)
                }
            }
    }

    // Hàm lưu token vào SharedPreferences
    private fun saveAuthToken(token: String) {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("firebase_id_token", token)
            apply()
        }

        // Debug log
        println("Token saved to SharedPreferences: ${token.take(10)}...")
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Lưu thông tin user vào SharedPreferences
     * @param userId User ID
     * @param email User email
     * @param name User display name
     * @param role User role
     * @param status User status
     */
    fun saveUserInfo(userId: String, email: String, name: String, role: String, status: String) {
        try {
            val editor = userPrefs.edit()
            editor.putString("user_id", userId)
            editor.putString("user_email", email)
            editor.putString("user_name", name)
            editor.putString("user_role", role)
            editor.putString("user_status", status)
            editor.apply()

            Log.d("AuthManager", "✅ Đã lưu thông tin user: $email")

        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Lỗi khi lưu user info", e)
        }
    }

    /**
     * Kiểm tra user đã login chưa
     * @return true nếu user đã login, false nếu chưa
     */
    fun isUserLoggedIn(): Boolean {
        val userId = userPrefs.getString("user_id", null)
        val hasFirebaseUser = auth.currentUser != null
        val hasValidToken = isTokenValid()

        val isLoggedIn = userId != null && hasFirebaseUser && hasValidToken

        Log.d("AuthManager", "🔍 Kiểm tra login state:")
        Log.d("AuthManager", "   User ID: $userId")
        Log.d("AuthManager", "   Firebase User: $hasFirebaseUser")
        Log.d("AuthManager", "   Valid Token: $hasValidToken")
        Log.d("AuthManager", "   => Logged in: $isLoggedIn")

        return isLoggedIn
    }

    /**
     * Xóa toàn bộ auth data
     */
    fun clearAuthData() {
        // Clear SharedPreferences
        authPrefs.edit().clear().apply()
        userPrefs.edit().clear().apply()

        // Sign out from Firebase
        auth.signOut()

        Log.d("AuthManager", "🧹 Đã xóa toàn bộ auth data và logout Firebase")
    }

    /**
     * Lấy user ID hiện tại
     * @return User ID hoặc null nếu chưa login
     */
    fun getCurrentUserId(): String? {
        return userPrefs.getString("user_id", null)
    }

    /**
     * Lấy user email hiện tại
     * @return User email hoặc null nếu chưa login
     */
    fun getCurrentUserEmail(): String? {
        return userPrefs.getString("user_email", null)
    }

    /**
     * Lấy thông tin user đầy đủ
     * @return Map chứa thông tin user
     */
    fun getUserInfo(): Map<String, String?> {
        return mapOf(
            "user_id" to userPrefs.getString("user_id", null),
            "user_email" to userPrefs.getString("user_email", null),
            "user_name" to userPrefs.getString("user_name", null),
            "user_role" to userPrefs.getString("user_role", null),
            "user_status" to userPrefs.getString("user_status", null)
        )
    }

    /**
     * Lấy thời gian token còn lại (tính bằng phút)
     * @return Số phút còn lại trước khi token hết hạn, hoặc 0 nếu đã hết hạn
     */
    fun getTokenRemainingMinutes(): Long {
        val expiryTime = authPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        val currentTime = System.currentTimeMillis()

        if (expiryTime <= currentTime) return 0

        val remainingMillis = expiryTime - currentTime
        return remainingMillis / (60 * 1000) // Convert to minutes
    }

    /**
     * Debug: In thông tin token
     */
    fun debugTokenInfo() {
        Log.d("AuthManager", "=== DEBUG TOKEN INFO ===")
        Log.d("AuthManager", "Token exists: ${authPrefs.contains(KEY_FIREBASE_TOKEN)}")
        Log.d("AuthManager", "Token: ${authPrefs.getString(KEY_FIREBASE_TOKEN, null)?.take(10)}...")
        Log.d("AuthManager", "Expiry: ${Date(authPrefs.getLong(KEY_TOKEN_EXPIRY, 0))}")
        Log.d("AuthManager", "Current time: ${Date(System.currentTimeMillis())}")
        Log.d("AuthManager", "Remaining minutes: ${getTokenRemainingMinutes()}")
        Log.d("AuthManager", "Is valid: ${isTokenValid()}")
        Log.d("AuthManager", "Firebase user: ${auth.currentUser?.uid}")
        Log.d("AuthManager", "=========================")
    }
}