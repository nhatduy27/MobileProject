package com.example.foodapp.presentation.view.IntroScreen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.example.foodapp.presentation.view.LoginScreen.LogInActivity
import com.example.foodapp.presentation.view.SignupScreen.SignUpActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnLogIn = findViewById<Button>(R.id.btnLogIn)

        btnStart.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
        btnLogIn.setOnClickListener {
            startActivity(Intent(this@IntroActivity, LogInActivity::class.java))
        }


    }
}