package com.example.foodapp.data.repository.firebase

import android.content.Context
import com.example.foodapp.R
import com.example.foodapp.data.model.Client
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserFirebaseRepository(private val context : Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        // Cấu hình Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return googleSignInClient
    }


    fun signInWithCustomToken(customToken: String, callback: (Boolean, Exception?) -> Unit) {
        auth.signInWithCustomToken(customToken)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    callback(true, null)
                } else {
                    callback(false, task.exception)
                }
            }
    }


    fun getVerifyStateByUid(onComplete: (Boolean) -> Unit){

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            onComplete(false)
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val verify = document.getBoolean("isVerify") ?: false
                    onComplete(verify)
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
    }


    fun checkEmailExists(
        email: String,
        onComplete: (Boolean) -> Unit
    ) {
        // Kiểm tra email hợp lệ
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onComplete(false)
            return
        }

        db.collection("users")
            .whereEqualTo("email", email.trim().lowercase())
            .limit(1) // Chỉ cần 1 kết quả
            .get()
            .addOnSuccessListener { querySnapshot ->
                val exists = !querySnapshot.isEmpty
                onComplete(exists)
            }
            .addOnFailureListener { exception ->
                println("Lỗi kiểm tra email: ${exception.message}")
                onComplete(false)
            }
    }



    fun setUserVerified(onComplete: (Boolean) -> Unit) {

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            onComplete(false)
            return
        }
        db.collection("users").document(userId)
            .update("isVerify", true, "updatedAt", System.currentTimeMillis())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    //lấy email của tài khoản hiện tại
    fun getUserEmailByUid(onComplete: (String?) -> Unit) {
        val authEmail = auth.currentUser?.email

        if (authEmail != null) {
            onComplete(authEmail)
            return
        }

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            onComplete(null)
            return
        }


        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val email = document.getString("email")
                    onComplete(email)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                onComplete(null)
            }
    }



    // Lấy role của user theo userId
    fun getUserRole(userId: String, onComplete: (String?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    onComplete(role)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null) // Lỗi khi truy vấn
            }
    }


    fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        imageAvatar: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: run {
            onComplete(false, "Không tìm thấy người dùng")
            return
        }

        val updates = mutableMapOf<String, Any>()

        if (fullName != null) updates["fullName"] = fullName
        if (phone != null) updates["phone"] = phone
        if (imageAvatar != null) updates["imageAvatar"] = imageAvatar

        updates["updatedAt"] = System.currentTimeMillis()

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message ?: "Lỗi không xác định")
            }
    }


    fun getCurrentUserWithDetails(onComplete: (Client?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(null)
            return
        }

        val userId = currentUser.uid

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val fullName = document.getString("fullName") ?: currentUser.displayName ?: ""
                        val email = document.getString("email") ?: currentUser.email ?: ""
                        val phone = document.getString("phone") ?: ""
                        val isVerify = document.getBoolean("isVerify") ?: false
                        val role = document.getString("role") ?: "user"
                        val imageAvatar = document.getString("imageAvatar") ?: ""
                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                        val updatedAt = document.getLong("updatedAt") ?: System.currentTimeMillis()

                        val user = Client(
                            id = userId,
                            fullName = fullName,
                            email = email,
                            isVerify = isVerify,
                            phone = phone,
                            role = role,
                            imageAvatar = imageAvatar,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )

                        onComplete(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(null)
                    }
                } else {
                    val user = Client(
                        id = userId,
                        fullName = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        isVerify = false,
                        phone = "",
                        role = "user",
                        imageAvatar = "",
                        createdAt = System.currentTimeMillis()
                    )
                    onComplete(user)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
    // Lưu vai trò user vào Firestore (RoleSelection)
    fun saveUserRole(userId: String, role: String, onComplete: (Boolean, String?) -> Unit) {
        val userRef = db.collection("users").document(userId)

        // Cập nhật trường role (merge nếu chưa có document)
        userRef.update("role", role)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener {
                // Sử dụng mapOf với explicit type
                val data = mapOf<String, Any>(
                    "role" to role,
                    "id" to userId,
                    "updatedAt" to System.currentTimeMillis()
                )
                userRef.set(data, SetOptions.merge())
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e.message) }
            }
    }

    fun getUserById(userId: String, onComplete: (Client?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        // Đọc từng field riêng lẻ để tránh lỗi deserialization
                        val id = document.getString("id") ?: userId
                        val fullName = document.getString("fullName") ?: ""
                        val email = document.getString("email") ?: ""
                        val isVerify = document.getBoolean("isVerify") ?: false
                        val phone = document.getString("phone") ?: ""
                        val role = document.getString("role") ?: "user"
                        val imageAvatar = document.getString("imageAvatar") ?: ""
                        val createdAt = document.getLong("createdAt") ?: 0L
                        val updatedAt = document.getLong("updatedAt") ?: 0L

                        val user = Client(
                            id = id,
                            fullName = fullName,
                            email = email,
                            isVerify = isVerify,
                            phone = phone,
                            role = role,
                            imageAvatar = imageAvatar,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                        onComplete(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(null)
                    }
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    // Lấy User hiện tại đang đăng nhập
    fun getCurrentUser(onComplete: (Client?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(null)
            return
        }

        getUserById(userId, onComplete)
    }

    fun getCurrentUserName(onComplete: (String?) -> Unit) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedName = sharedPref.getString("user_name", null)
        onComplete(savedName)
    }
}