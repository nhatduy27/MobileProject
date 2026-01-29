package com.example.foodapp.pages.owner.theme

import androidx.compose.ui.graphics.Color

/**
 * Owner Theme Colors - Modern, Clean, Professional
 * Synchronized with Shipper UI style for visual consistency
 * 
 * Design System:
 * - Modern Minimal UI
 * - Card-based design with soft rounded corners
 * - Clean typography hierarchy
 * - Consistent status colors across the app
 */
object OwnerColors {
    // Primary Colors - Blue theme (same as Shipper)
    val Primary = Color(0xFF4F7CF7)          // Main blue
    val PrimaryLight = Color(0xFFE8F0FF)     // Light blue background
    val PrimaryDark = Color(0xFF3D62C4)      // Darker blue for pressed states
    
    // Background Colors
    val Background = Color(0xFFF5F7FA)       // Light gray background (#F7F8FA equivalent)
    val Surface = Color(0xFFFFFFFF)          // White surface/cards
    val SurfaceVariant = Color(0xFFF8F9FB)   // Slightly gray surface
    
    // Text Colors
    val TextPrimary = Color(0xFF1F2937)      // Dark gray - main text (titles, numbers)
    val TextSecondary = Color(0xFF6B7280)    // Medium gray - secondary text, descriptions
    val TextTertiary = Color(0xFF9CA3AF)     // Light gray - hints/captions
    val TextOnPrimary = Color(0xFFFFFFFF)    // White text on primary
    
    // Status Colors
    val Success = Color(0xFF10B981)          // Green - success, completed, bonus
    val SuccessLight = Color(0xFFD1FAE5)     // Light green background
    val Warning = Color(0xFFF59E0B)          // Amber - pending, warning
    val WarningLight = Color(0xFFFEF3C7)     // Light amber background  
    val Error = Color(0xFFEF4444)            // Red - error, cancelled
    val ErrorLight = Color(0xFFFEE2E2)       // Light red background
    val Info = Color(0xFF3B82F6)             // Blue - info, in progress
    val InfoLight = Color(0xFFDBEAFE)        // Light blue background
    
    // Order Status Colors (synchronized with Shipper)
    val StatusPending = Color(0xFFF59E0B)    // Amber - Chờ xác nhận
    val StatusConfirmed = Color(0xFF3B82F6)  // Blue - Đã xác nhận
    val StatusPreparing = Color(0xFF8B5CF6)  // Purple - Đang nấu
    val StatusReady = Color(0xFF10B981)      // Green - Đã xong
    val StatusShipping = Color(0xFFF59E0B)   // Amber - Đang giao
    val StatusDelivered = Color(0xFF059669)  // Dark green - Đã giao
    val StatusCancelled = Color(0xFFEF4444)  // Red - Đã hủy
    
    // Payment Status Colors
    val PaymentUnpaid = Color(0xFFEF4444)    // Red
    val PaymentProcessing = Color(0xFFF59E0B) // Amber
    val PaymentPaid = Color(0xFF10B981)      // Green
    val PaymentRefunded = Color(0xFF6B7280)  // Gray
    
    // Divider & Border
    val Divider = Color(0xFFE5E7EB)          // Light gray divider
    val Border = Color(0xFFD1D5DB)           // Border color
    val BorderLight = Color(0xFFF3F4F6)      // Very light border
    
    // Toggle/Switch
    val ToggleOn = Primary
    val ToggleOff = Color(0xFFD1D5DB)
    
    // Card Shadow (for elevation reference)
    val CardShadow = Color(0x0D000000)       // Very subtle shadow
    
    // Chart Colors
    val ChartPrimary = Primary
    val ChartSecondary = Color(0xFF60A5FA)   // Lighter blue for gradients
    
    // Revenue/Money highlight
    val MoneyAmount = Primary                 // Use primary blue for money amounts
}

/**
 * Common dimension values for consistent spacing
 * Synchronized with Shipper design system
 */
object OwnerDimens {
    // Card & Container
    const val CardRadius = 12              // dp
    const val CardRadiusLarge = 16         // dp
    const val CardElevation = 2            // dp
    const val CardPadding = 16             // dp
    
    // Button
    const val ButtonRadius = 10            // dp
    const val ButtonRadiusLarge = 12       // dp
    const val ButtonHeight = 48            // dp
    const val ButtonHeightSmall = 36       // dp
    
    // Spacing
    const val SpacingXS = 4                // dp
    const val SpacingSM = 8                // dp
    const val SpacingMD = 12               // dp
    const val SpacingLG = 16               // dp
    const val SpacingXL = 24               // dp
    
    // Icon
    const val IconSizeSmall = 16           // dp
    const val IconSizeMedium = 20          // dp
    const val IconSizeLarge = 24           // dp
    const val IconSizeXL = 32              // dp
    
    // Header
    const val HeaderHeight = 64            // dp
    
    // Filter Chip
    const val FilterChipHeight = 36        // dp
    const val FilterChipRadius = 10        // dp
}
