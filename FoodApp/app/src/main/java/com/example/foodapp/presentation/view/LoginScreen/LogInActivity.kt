package com.example.foodapp.presentation.view.LoginScreen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.example.foodapp.presentation.view.MainScreen.MainActivity
import com.example.foodapp.presentation.view.SignupScreen.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LogInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Kiểm tra nếu đã đăng nhập trước đó thì vào thẳng Main (Auto Login)
        if (auth.currentUser != null) {
            navigateToMain()
        }

        // 2. Cấu hình Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        val btnLogin = findViewById<Button>(R.id.btnLogIn)
        val btnGoToSignUp = findViewById<Button>(R.id.btnGoToSignUp)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        // 3. Xử lý nút Google Login
        btnGoogleLogin.setOnClickListener {
            signInGoogle()
        }

        btnLogin.setOnClickListener {
            val etEmail = findViewById<TextInputEditText>(R.id.emailEditText)
            val etPassword = findViewById<TextInputEditText>(R.id.passwordEditText)

            val email = etEmail.text.toString().trim() // Thêm trim() cho sạch
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        } else {
                            Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoToSignUp.setOnClickListener {
            startActivity(Intent(this@LogInActivity, SignUpActivity::class.java))
        }
    }

    // --- CÁC HÀM XỬ LÝ GOOGLE  ---

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcherGoogle.launch(signInIntent)
    }

    private val launcherGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
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
                navigateToMain()
            } else {
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
}