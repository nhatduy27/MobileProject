package com.example.foodapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnLogIn = findViewById<Button>(R.id.btnLogIn)

        btnStart.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java ))
        }
        btnLogIn.setOnClickListener {
            startActivity(Intent(this@IntroActivity, LogInActivity::class.java ))
        }


    }
}