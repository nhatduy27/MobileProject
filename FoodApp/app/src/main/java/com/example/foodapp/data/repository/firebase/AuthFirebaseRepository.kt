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
     */
    fun saveFirebaseToken(idToken: String, expiryTime: Long? = null) {
        try {
            val editor = authPrefs.edit()
            editor.putString(KEY_FIREBASE_TOKEN, idToken)

            val expiry = expiryTime ?: (System.currentTimeMillis() + 60 * 60 * 1000)
            editor.putLong(KEY_TOKEN_EXPIRY, expiry)
            editor.putLong(KEY_LAST_REFRESH, System.currentTimeMillis())
            editor.apply()

        } catch (e: Exception) {
        }
    }

    /**
     * Kiểm tra token còn valid không
     */
    fun isTokenValid(): Boolean {
        val token = authPrefs.getString(KEY_FIREBASE_TOKEN, null)
        val expiryTime = authPrefs.getLong(KEY_TOKEN_EXPIRY, 0)

        val isValid = !token.isNullOrEmpty() &&
                (expiryTime - TOKEN_EXPIRY_BUFFER) > System.currentTimeMillis()

        if (!isValid) {
        }

        return isValid
    }

    /**
     * Lấy token hiện tại (nếu valid)
     */
    fun getCurrentToken(): String? {
        return if (isTokenValid()) {
            authPrefs.getString(KEY_FIREBASE_TOKEN, null)
        } else {
            null
        }
    }

    /**
     * Refresh Firebase token (Firebase tự động refresh bằng getIdToken(true))
     */
    suspend fun refreshFirebaseToken(): String? {
        return try {

            val currentUser = auth.currentUser
            if (currentUser == null) {
                clearAuthData()
                return null
            }

            // Force refresh từ Firebase
            val tokenResult = currentUser.getIdToken(true).await()
            val newToken = tokenResult.token

            if (newToken != null) {
                saveFirebaseToken(newToken, tokenResult.expirationTimestamp)
            }

            newToken

        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lấy token valid (tự động refresh nếu cần)
     */
    suspend fun getValidToken(): String? {

        // 1. Kiểm tra token hiện tại còn valid không
        if (isTokenValid()) {
            return getCurrentToken()
        }

        return refreshFirebaseToken()
    }

    // ==================== AUTHENTICATION METHODS ====================

    fun signInWithCustomToken(customToken: String, callback: (Boolean, String?, Exception?) -> Unit) {
        auth.signInWithCustomToken(customToken)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.getIdToken(false)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token
                            if (idToken != null) {
                                saveFirebaseToken(idToken, tokenTask.result?.expirationTimestamp)
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

    // ==================== USER MANAGEMENT ====================

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

    fun isUserLoggedIn(): Boolean {
        val userId = userPrefs.getString("user_id", null)
        val hasFirebaseUser = auth.currentUser != null
        val hasValidToken = isTokenValid()

        val isLoggedIn = userId != null && hasFirebaseUser && hasValidToken

        return isLoggedIn
    }

    fun clearAuthData() {
        authPrefs.edit().clear().apply()
        userPrefs.edit().clear().apply()
        auth.signOut()

    }

    fun getCurrentUserId(): String? {
        return userPrefs.getString("user_id", null)
    }

    fun getCurrentUserEmail(): String? {
        return userPrefs.getString("user_email", null)
    }

    fun getUserInfo(): Map<String, String?> {
        return mapOf(
            "user_id" to userPrefs.getString("user_id", null),
            "user_email" to userPrefs.getString("user_email", null),
            "user_name" to userPrefs.getString("user_name", null),
            "user_role" to userPrefs.getString("user_role", null),
            "user_status" to userPrefs.getString("user_status", null)
        )
    }

    fun getTokenRemainingMinutes(): Long {
        val expiryTime = authPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        val currentTime = System.currentTimeMillis()

        if (expiryTime <= currentTime) return 0
        return (expiryTime - currentTime) / (60 * 1000)
    }

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