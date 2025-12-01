package com.example.foodapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)


        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoToLogin =  findViewById<Button>(R.id.btnGoToLogin)

        btnRegister.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, SignUpActivity::class.java ))
        }
        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LogInActivity::class.java ))
        }


    }
}