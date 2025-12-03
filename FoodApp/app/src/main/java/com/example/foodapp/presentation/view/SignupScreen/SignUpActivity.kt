package com.example.foodapp.presentation.view.SignupScreen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.example.foodapp.presentation.view.LoginScreen.LogInActivity
import com.example.foodapp.presentation.view.MainScreen.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient // 1. Thêm biến Client Google

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 2. Cấu hình Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Firebase tự sinh string này
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoToLogin = findViewById<Button>(R.id.btnGoToLogin)
        val btnGoogleSignUp = findViewById<Button>(R.id.btnGoogleSignUp)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnGoogleSignUp.setOnClickListener {
            signInGoogle()
        }

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LogInActivity::class.java))
        }
    }

    // --- CÁC HÀM XỬ LÝ GOOGLE ---

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcherGoogle.launch(signInIntent)
    }

    // Bộ lắng nghe kết quả trả về từ Google
    private val launcherGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Lấy được tài khoản Google -> Gửi token lên Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Đăng nhập thành công -> Lưu/Kiểm tra Firestore
                    val user = auth.currentUser
                    checkAndSaveGoogleUser(user)
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndSaveGoogleUser(user: com.google.firebase.auth.FirebaseUser?) {
        if (user == null) return

        val userId = user.uid
        val docRef = db.collection("users").document(userId)

        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // User đã có trong database -> Chuyển thẳng vào Main
                Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                // User mới -> Lưu thông tin
                val userMap = hashMapOf(
                    "id" to userId,
                    "fullName" to (user.displayName ?: "Google User"),
                    "email" to user.email,
                    "role" to "user",
                    "createdAt" to System.currentTimeMillis()
                )

                docRef.set(userMap).addOnSuccessListener {
                    Toast.makeText(this, "Tạo tài khoản Google thành công!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }.addOnFailureListener {
                    Toast.makeText(this, "Lỗi lưu dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- CÁC HÀM XỬ LÝ ĐĂNG KÝ THƯỜNG ---

    private fun registerUser() {
        if (!validateInput()) {
            return
        }

        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    val userMap = hashMapOf(
                        "id" to userId,
                        "fullName" to fullName,
                        "email" to email,
                        "role" to "user",
                        "createdAt" to System.currentTimeMillis()
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LogInActivity::class.java)
                                // Xóa stack để không back lại được
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Lỗi lưu database: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val error = task.exception?.message
                    Toast.makeText(this, "Lỗi: $error", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateInput(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            etFullName.error = "Vui lòng nhập họ và tên"
            etFullName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Vui lòng nhập Email"
            etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email không đúng định dạng"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Vui lòng nhập mật khẩu"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            etPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Vui lòng xác nhận lại mật khẩu"
            etConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            etConfirmPassword.requestFocus()
            etConfirmPassword.setText("")
            return false
        }

        return true
    }
}