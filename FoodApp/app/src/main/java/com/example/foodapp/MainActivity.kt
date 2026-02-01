package com.example.foodapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.navigation.FoodAppNavHost
import com.example.foodapp.ui.theme.FoodAppTheme
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.utils.LanguageManager

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"

        const val CHANNEL_ORDERS = "orders_channel"
        const val CHANNEL_GENERAL = "general_channel"
        const val CHANNEL_CHAT = "chat_channel"
    }
    
    // Permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied - user won't receive push notifications")
        }
    }
    
    override fun attachBaseContext(newBase: Context) {
        // Áp dụng ngôn ngữ đã lưu trước khi attach context
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ApiClient.init(this)

        // Áp dụng locale - LanguageManager đã sync với LocaleHelper
        val currentLanguage = LanguageManager.getCurrentLanguage(this)
        LanguageManager.saveLanguage(this, currentLanguage) // Ensure locale is applied

        // Create notification channels early
        createNotificationChannels()
        
        // Request notification permission for Android 13+
        requestNotificationPermission()

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
    
    /**
     * Request notification permission for Android 13 (API 33) and above
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    // Show explanation to user why we need this permission
                    // For now, just request the permission
                    notificationPermissionLauncher.launch(permission)
                }
                else -> {
                    // Request the permission
                    notificationPermissionLauncher.launch(permission)
                }
            }
        }
    }
    
    /**
     * Create notification channels for different types of notifications
     * Must be called before showing any notifications on Android O+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Orders channel (high importance for new orders)
            val ordersChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Đơn hàng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo về đơn hàng mới và cập nhật trạng thái"
                enableVibration(true)
                enableLights(true)
            }

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Thông báo chung",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo chung từ ứng dụng"
            }

            // Chat messages channel
            val chatChannel = NotificationChannel(
                CHANNEL_CHAT,
                "Tin nhắn",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tin nhắn từ khách hàng và shipper"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(ordersChannel, generalChannel, chatChannel)
            )

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
            kotlinx.coroutines.delay(300)
            (context as? ComponentActivity)?.recreate()
            restartRequired.value = false
        }
    }
    FoodAppNavHost(
        navController = navController,
    )
}
