package com.example.foodapp.data.model.owner.revenue

import com.google.gson.annotations.SerializedName
import androidx.compose.ui.graphics.Color

/**
 * Revenue period enum (maps to backend)
 */
enum class RevenuePeriod(val apiValue: String, val displayKey: String) {
    TODAY("today", "today"),
    WEEK("week", "week"),
    MONTH("month", "month"),
    YEAR("year", "year");
    
    companion object {
        fun fromApiValue(value: String): RevenuePeriod {
            return values().find { it.apiValue == value } ?: TODAY
        }
        
        fun fromDisplayKey(key: String): RevenuePeriod {
            return values().find { it.displayKey == key } ?: TODAY
        }
    }
}


/**
 * KPI Stat Card - từ API response
 */
data class KpiStat(
    @SerializedName("title") val title: String,
    @SerializedName("value") val value: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("color") val colorHex: String
) {
    // Convert hex color to Compose Color
    val color: Color
        get() = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFFFF6B35)
        }
}

/**
 * Time Slot Revenue - từ API response
 */
data class TimeSlotData(
    @SerializedName("emoji") val emoji: String,
    @SerializedName("title") val title: String,
    @SerializedName("ordersCount") val ordersCount: Int = 0,
    @SerializedName("percentage") val percentage: Int = 0,
    @SerializedName("amount") val amount: String
)

/**
 * Top Product - từ API response
 */
data class TopProductData(
    @SerializedName("rank") val rank: String,
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int = 0,
    @SerializedName("unitPrice") val unitPrice: Double = 0.0,
    @SerializedName("totalRevenue") val totalRevenue: String
) {
    val unitPriceFormatted: String
        get() = formatCurrency(unitPrice)
}

/**
 * Revenue Analytics DTO - từ GET /owner/revenue
 */
data class RevenueAnalytics(
    @SerializedName("period") val periodValue: String,
    @SerializedName("stats") val stats: List<KpiStat> = emptyList(),
    @SerializedName("timeSlots") val timeSlots: List<TimeSlotData> = emptyList(),
    @SerializedName("topProducts") val topProducts: List<TopProductData> = emptyList()
) {
    val period: RevenuePeriod
        get() = RevenuePeriod.fromApiValue(periodValue)
}

// Wrapper response for API
data class WrappedRevenueAnalyticsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: RevenueAnalytics? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

// Utility function to format currency
private fun formatCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format("%.1fB", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
