package com.example.foodapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.foodapp.ui.theme.FoodAppTheme
import com.example.foodapp.pages.dashboard.DashBoardRootScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FoodAppTheme {
                DashBoardRootScreen()
            }
        }

    }
}
