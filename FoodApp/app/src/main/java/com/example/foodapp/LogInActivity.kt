package com.example.foodapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)


        val btnLogin = findViewById<Button>(R.id.btnLogIn)
        val btnGoToSignUp = findViewById<Button>(R.id.btnGoToSignUp)

        btnLogin.setOnClickListener {
            startActivity(Intent(this@LogInActivity, MainActivity::class.java ))
        }

        btnGoToSignUp.setOnClickListener {
            startActivity(Intent(this@LogInActivity, SignUpActivity::class.java ))
        }
    }
}