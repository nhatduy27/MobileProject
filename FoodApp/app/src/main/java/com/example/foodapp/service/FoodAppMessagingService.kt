package com.example.foodapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foodapp.MainActivity
import com.example.foodapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications and displays them in the notification bar
 */
class FoodAppMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        
        // Channel IDs for different notification types
        const val CHANNEL_ORDERS = "orders_channel"
        const val CHANNEL_GENERAL = "general_channel"
        const val CHANNEL_CHAT = "chat_channel"
        
        // Notification IDs
        private var notificationId = 0
        fun getNextNotificationId(): Int = notificationId++
    }

    /**
     * Called when a new FCM token is generated
     * This happens on first app launch and when token is refreshed
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 New FCM token: $token")
        // TODO: Send this token to your backend server
        // The token is already sent during login/signup, but this handles token refresh
    }

    /**
     * Called when a message is received
     * This is called when app is in foreground or background
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "📩 Message received from: ${remoteMessage.from}")
        Log.d(TAG, "📦 Data payload: ${remoteMessage.data}")
        Log.d(TAG, "🔔 Notification payload: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
        
        // Ưu tiên notification payload nếu có
        val notification = remoteMessage.notification
        if (notification != null) {
            // Backend gửi notification + data payload
            val title = notification.title ?: getString(R.string.app_name)
            val body = notification.body ?: ""
            val type = remoteMessage.data["type"] ?: "general"
            
            // Chọn channel dựa trên type
            val channelId = when {
                type.contains("ORDER", ignoreCase = true) -> CHANNEL_ORDERS
                type.contains("MESSAGE", ignoreCase = true) || type.contains("CHAT", ignoreCase = true) -> CHANNEL_CHAT
                else -> CHANNEL_GENERAL
            }
            
            showNotification(
                title = title,
                body = body,
                channelId = channelId,
                data = remoteMessage.data
            )
        } else if (remoteMessage.data.isNotEmpty()) {
            // Data-only message (không có notification payload)
            handleDataMessage(remoteMessage.data)
        }
    }

    /**
     * Handle data messages (custom data from backend)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: "general"
        val title = data["title"] ?: getString(R.string.app_name)
        val body = data["body"] ?: data["message"] ?: ""
        
        when (type) {
            "NEW_ORDER" -> {
                showNotification(
                    title = title,
                    body = body,
                    channelId = CHANNEL_ORDERS,
                    data = data
                )
            }
            "ORDER_STATUS_UPDATE" -> {
                showNotification(
                    title = title,
                    body = body,
                    channelId = CHANNEL_ORDERS,
                    data = data
                )
            }
            "NEW_MESSAGE" -> {
                showNotification(
                    title = title,
                    body = body,
                    channelId = CHANNEL_CHAT,
                    data = data
                )
            }
            else -> {
                showNotification(
                    title = title,
                    body = body,
                    channelId = CHANNEL_GENERAL,
                    data = data
                )
            }
        }
    }

    /**
     * Display notification in the system notification bar
     */
    private fun showNotification(
        title: String,
        body: String,
        channelId: String = CHANNEL_GENERAL,
        data: Map<String, String> = emptyMap()
    ) {
        // Create intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Pass notification data to the activity
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            getNextNotificationId(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Default notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager)
        }

        // Show the notification
        notificationManager.notify(getNextNotificationId(), notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed: $title")
    }

    /**
     * Create notification channels for Android O+
     */
    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
