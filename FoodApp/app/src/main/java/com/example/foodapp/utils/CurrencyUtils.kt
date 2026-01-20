package com.example.foodapp.utils


import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    /**
     * Format số tiền sang định dạng VND
     * Ví dụ: 100000 -> "100.000đ"
     */
    fun formatCurrency(amount: Double): String {
        return try {
            val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            formatter.maximumFractionDigits = 0
            "${formatter.format(amount)}đ"
        } catch (e: Exception) {
            "${amount.toInt()}đ"
        }
    }

    /**
     * Format số tiền từ Int
     * Ví dụ: 100000 -> "100.000đ"
     */
    fun formatCurrency(amount: Int): String {
        return formatCurrency(amount.toDouble())
    }

    /**
     * Format số tiền từ String
     * Ví dụ: "100000" -> "100.000đ"
     */
    fun formatCurrency(amount: String): String {
        return try {
            val amountDouble = amount.toDoubleOrNull() ?: 0.0
            formatCurrency(amountDouble)
        } catch (e: Exception) {
            "0đ"
        }
    }
}