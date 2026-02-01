package com.example.foodapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing notification permissions
 * Required for Android 13 (API 33) and above
 */
object NotificationPermissionHelper {
    private const val TAG = "NotificationPermission"
    
    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission is automatically granted on Android 12 and below
            true
        }
    }

    /**
     * Check if we should show rationale for notification permission
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }

    /**
     * Request notification permission
     * Call this from your Activity/Fragment with a permission launcher
     */
    fun requestPermissionIfNeeded(
        activity: Activity,
        onPermissionResult: (Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                Log.d(TAG, "Requesting notification permission...")
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                Log.d(TAG, "Notification permission already granted")
                onPermissionResult(true)
            }
        } else {
            // Permission is automatically granted on Android 12 and below
            onPermissionResult(true)
        }
    }

    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
}
