package com.example.foodapp.data.repository

import android.content.Context
import com.example.foodapp.R
import com.example.foodapp.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirebaseRepository(context: Context) {
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

    fun getCurrentUserWithDetails(onComplete: (User?) -> Unit) {
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
                        // Đọc từng field riêng lẻ
                        val fullName = document.getString("fullName") ?: currentUser.displayName ?: ""
                        val email = document.getString("email") ?: currentUser.email ?: ""
                        val phone = document.getString("phone") ?: ""
                        val role = document.getString("role") ?: "user"
                        val imageAvatar = document.getString("imageAvatar") ?: ""
                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                        val updatedAt = document.getLong("updatedAt") ?: System.currentTimeMillis()

                        val user = User(
                            id = userId,
                            fullName = fullName,
                            email = email,
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
                    // Nếu chưa có document, tạo user cơ bản
                    val user = User(
                        id = userId,
                        fullName = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
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

    fun registerWithEmail(
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    onComplete(true, userId)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun authWithGoogle(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    onComplete(true, userId)
                } else {
                    onComplete(false, null)
                }
            }
    }

    fun checkUserExists(userId: String, onComplete: (Boolean) -> Unit) {
        val docRef = db.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                onComplete(document.exists())
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun logInWithEmail(
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val userID = auth.currentUser?.uid
                if (task.isSuccessful) {
                    onComplete(true, userID)
                } else {
                    onComplete(false, "Sai email hoặc mật khẩu")
                }
            }
    }

    fun saveUserToFirestore(
        userId: String,
        fullName: String,
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = User(
            id = userId,
            fullName = fullName,
            email = email,
            phone = "",
            role = "user",
            imageAvatar = "",
            createdAt = System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun saveGoogleUserToFirestore(
        userId: String,
        displayName: String?,
        email: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = User(
            id = userId,
            fullName = displayName ?: "Google User",
            email = email ?: "",
            phone = "",
            role = "user",
            imageAvatar = "",
            createdAt = System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
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

    // Lấy User object từ Firestore theo ID
    fun getUserById(userId: String, onComplete: (User?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        // Đọc từng field riêng lẻ để tránh lỗi deserialization
                        val id = document.getString("id") ?: userId
                        val fullName = document.getString("fullName") ?: ""
                        val email = document.getString("email") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val role = document.getString("role") ?: "user"
                        val imageAvatar = document.getString("imageAvatar") ?: ""
                        val createdAt = document.getLong("createdAt") ?: 0L
                        val updatedAt = document.getLong("updatedAt") ?: 0L

                        val user = User(
                            id = id,
                            fullName = fullName,
                            email = email,
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
    fun getCurrentUser(onComplete: (User?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(null)
            return
        }

        getUserById(userId, onComplete)
    }

    // Lấy tên người dùng hiện tại
    fun getCurrentUserName(onComplete: (String?) -> Unit) {
        getCurrentUser { user ->
            onComplete(user?.fullName)
        }
    }

    // ========== ĐĂNG XUẤT ==========
    fun logout() {
        auth.signOut()
    }
}