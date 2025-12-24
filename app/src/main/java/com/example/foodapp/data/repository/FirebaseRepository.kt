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

    // Đăng ký với email và password
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

    // Xác thực với Google
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

    // Kiểm tra user đã tồn tại trong Firestore chưa
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
        onComplete: (Boolean, String?) -> Unit){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val userID = auth.currentUser?.uid
                if(task.isSuccessful){
                    onComplete(true, userID)
                }
                else{
                    onComplete(false, "Sai email hoặc mật khẩu")
                }
            }
    }

    // Lưu user vào Firestore (đăng ký thường)
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

    // Lưu user Google vào Firestore
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
                userRef.set(mapOf("role" to role), SetOptions.merge())
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
                    val user = document.toObject(User::class.java)
                    onComplete(user)
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
}