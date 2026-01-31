package com.example.foodapp.pages.shipper.notifications

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun NotificationsScreen() {
    val viewModel: NotificationsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        if (uiState.unreadCount > 0) {
            Text(
                text = stringResource(R.string.shipper_notif_unread_count, uiState.unreadCount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ShipperColors.Primary,
                modifier = Modifier.padding(16.dp).padding(bottom = 0.dp)
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ShipperColors.Primary)
                }
            }
            uiState.error != null && uiState.notifications.isEmpty() -> {
                Text(
                    text = uiState.error ?: stringResource(R.string.shipper_notif_load_error),
                    fontSize = 14.sp,
                    color = ShipperColors.Error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.notifications.forEach { notification ->
                        val titleText = mapTitle(notification, context)
                        val bodyText = mapBody(notification, context)
                        NotificationCard(
                            notification = notification,
                            timeText = formatNotificationTime(notification.createdAt, context),
                            titleText = titleText,
                            bodyText = bodyText,
                            onClick = {
                                if (!notification.read) {
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatNotificationTime(timestamp: String, context: Context): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        when {
            diff < 60000 -> context.getString(R.string.shipper_notif_time_just_now)
            diff < 3600000 -> context.getString(R.string.shipper_notif_time_minutes_ago, (diff / 60000).toInt())
            diff < 86400000 -> context.getString(R.string.shipper_notif_time_hours_ago, (diff / 3600000).toInt())
            diff < 604800000 -> context.getString(R.string.shipper_notif_time_days_ago, (diff / 86400000).toInt())
            else -> {
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        context.getString(R.string.shipper_notif_time_unknown)
    }
}

private fun mapTitle(notification: Notification, context: Context): String {
    return when (notification.type) {
        NotificationType.NEW_ORDER -> context.getString(R.string.shipper_notif_new_order)
        NotificationType.ORDER_CONFIRMED -> context.getString(R.string.shipper_notif_order_confirmed)
        NotificationType.ORDER_PREPARING -> context.getString(R.string.shipper_notif_order_preparing)
        NotificationType.ORDER_READY -> context.getString(R.string.shipper_notif_order_ready)
        NotificationType.ORDER_SHIPPING -> context.getString(R.string.shipper_notif_order_shipping)
        NotificationType.ORDER_DELIVERED -> context.getString(R.string.shipper_notif_order_delivered)
        NotificationType.ORDER_CANCELLED -> context.getString(R.string.shipper_notif_order_cancelled)

        NotificationType.PAYMENT_SUCCESS -> context.getString(R.string.shipper_notif_payment_success)
        NotificationType.PAYMENT_FAILED -> context.getString(R.string.shipper_notif_payment_failed)
        NotificationType.PAYMENT_REFOUNDED -> context.getString(R.string.shipper_notif_payment_refunded)

        NotificationType.SHIPPER_ASSIGNED -> context.getString(R.string.shipper_notif_shipper_assigned)
        NotificationType.SHIPPER_APPLIED -> context.getString(R.string.shipper_notif_shipper_applied)
        NotificationType.SHIPPER_APPLICATION_APPROVED -> context.getString(R.string.shipper_notif_app_approved)
        NotificationType.SHIPPER_APPLICATION_REJECTED -> context.getString(R.string.shipper_notif_app_rejected)

        NotificationType.DAILY_SUMMARY -> context.getString(R.string.shipper_notif_daily_summary)
        NotificationType.SUBSCRIPTION_EXPIRING -> context.getString(R.string.shipper_notif_subscription_expiring)

        NotificationType.PROMOTION -> context.getString(R.string.shipper_notif_promotion)
        NotificationType.VOUCHER_AVAILABLE -> context.getString(R.string.shipper_notif_voucher_available)

        NotificationType.UNKNOWN -> notification.title
    }
}

private fun mapBody(notification: Notification, context: Context): String {
    val orderId = notification.orderId
    return when (notification.type) {
        NotificationType.NEW_ORDER ->
            if (orderId != null) context.getString(R.string.shipper_notif_new_order_id, orderId)
            else context.getString(R.string.shipper_notif_new_order_generic)
        NotificationType.ORDER_CONFIRMED ->
            if (orderId != null) context.getString(R.string.shipper_notif_order_confirmed_id, orderId)
            else context.getString(R.string.shipper_notif_order_confirmed_generic)
        NotificationType.ORDER_PREPARING ->
            if (orderId != null) context.getString(R.string.shipper_notif_order_preparing_id, orderId)
            else context.getString(R.string.shipper_notif_order_preparing_generic)
        NotificationType.ORDER_READY ->
            if (orderId != null) context.getString(R.string.shipper_notif_order_ready_id, orderId)
            else context.getString(R.string.shipper_notif_order_ready_generic)
        NotificationType.ORDER_SHIPPING ->
            if (orderId != null) context.getString(R.string.shipper_notif_order_shipping_id, orderId)
            else context.getString(R.string.shipper_notif_order_shipping_generic)
        NotificationType.ORDER_DELIVERED ->
            if (orderId != null) context.getString(R.string.shipper_notif_order_delivered_id, orderId)
            else context.getString(R.string.shipper_notif_order_delivered_generic)
        NotificationType.ORDER_CANCELLED ->
            if (orderId != null) context.getString(R.string.shipper_notif_order_cancelled_id, orderId)
            else context.getString(R.string.shipper_notif_order_cancelled_generic)

        NotificationType.PAYMENT_SUCCESS -> context.getString(R.string.shipper_notif_payment_success_body)
        NotificationType.PAYMENT_FAILED -> context.getString(R.string.shipper_notif_payment_failed_body)
        NotificationType.PAYMENT_REFOUNDED -> context.getString(R.string.shipper_notif_payment_refunded_body)

        NotificationType.SHIPPER_ASSIGNED -> context.getString(R.string.shipper_notif_shipper_assigned_body)
        NotificationType.SHIPPER_APPLIED -> context.getString(R.string.shipper_notif_shipper_applied_body)
        NotificationType.SHIPPER_APPLICATION_APPROVED -> context.getString(R.string.shipper_notif_app_approved_body)
        NotificationType.SHIPPER_APPLICATION_REJECTED -> context.getString(R.string.shipper_notif_app_rejected_body)

        NotificationType.DAILY_SUMMARY -> context.getString(R.string.shipper_notif_daily_summary_body)
        NotificationType.SUBSCRIPTION_EXPIRING -> context.getString(R.string.shipper_notif_subscription_expiring_body)

        NotificationType.PROMOTION -> context.getString(R.string.shipper_notif_promotion_body)
        NotificationType.VOUCHER_AVAILABLE -> context.getString(R.string.shipper_notif_voucher_available_body)

        NotificationType.UNKNOWN -> notification.body
    }
}
