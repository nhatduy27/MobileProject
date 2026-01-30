package com.example.foodapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.navigation.FoodAppNavHost
import com.example.foodapp.ui.theme.FoodAppTheme
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.utils.LocaleHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ApiClient.init(this)

        // Áp dụng locale đã lưu khi khởi động
        val currentLanguage = LocaleHelper.getCurrentLanguage(this)
        val country = if (currentLanguage == "vi") "VN" else "US"
        LocaleHelper.setLocale(this, currentLanguage, country)

        setContent {
            FoodAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // State để theo dõi khi cần restart app
    val restartRequired = remember { mutableStateOf(false) }

    // Khi cần restart, trigger recreate activity
    LaunchedEffect(restartRequired.value) {
        if (restartRequired.value) {
            // Delay một chút để animation hoàn tất
            kotlinx.coroutines.delay(300)
            (context as? ComponentActivity)?.recreate()
            restartRequired.value = false
        }
    }

    FoodAppNavHost(
        navController = navController,
        onLanguageChanged = { restartRequired.value = true }
    )
}