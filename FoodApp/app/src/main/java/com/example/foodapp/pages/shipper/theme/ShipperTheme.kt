package com.example.foodapp.pages.shipper.theme

import androidx.compose.ui.graphics.Color

/**
 * Shipper Theme Colors - Modern, Clean, Professional
 * Synchronized with Owner UI style
 */
object ShipperColors {
    // Primary Colors
    val Primary = Color(0xFF4F7CF7)          // Main blue
    val PrimaryLight = Color(0xFFE8F0FF)     // Light blue background
    val PrimaryDark = Color(0xFF3D62C4)      // Darker blue for pressed states
    
    // Background Colors
    val Background = Color(0xFFF5F7FA)       // Light gray background
    val Surface = Color(0xFFFFFFFF)          // White surface/cards
    val SurfaceVariant = Color(0xFFF8F9FB)   // Slightly gray surface
    
    // Text Colors
    val TextPrimary = Color(0xFF1F2937)      // Dark gray - main text
    val TextSecondary = Color(0xFF6B7280)    // Medium gray - secondary text
    val TextTertiary = Color(0xFF9CA3AF)     // Light gray - hints/captions
    val TextOnPrimary = Color(0xFFFFFFFF)    // White text on primary
    
    // Status Colors
    val Success = Color(0xFF10B981)          // Green
    val SuccessLight = Color(0xFFD1FAE5)     // Light green background
    val Warning = Color(0xFFF59E0B)          // Amber
    val WarningLight = Color(0xFFFEF3C7)     // Light amber background  
    val Error = Color(0xFFEF4444)            // Red
    val ErrorLight = Color(0xFFFEE2E2)       // Light red background
    val Info = Color(0xFF3B82F6)             // Blue
    val InfoLight = Color(0xFFDBEAFE)        // Light blue background
    
    // Order Status Colors
    val StatusPending = Color(0xFF6B7280)    // Gray
    val StatusConfirmed = Color(0xFF3B82F6)  // Blue
    val StatusPreparing = Color(0xFF8B5CF6)  // Purple
    val StatusReady = Color(0xFF10B981)      // Green
    val StatusShipping = Color(0xFFF59E0B)   // Amber
    val StatusDelivered = Color(0xFF059669)  // Dark green
    val StatusCancelled = Color(0xFFEF4444)  // Red
    
    // Divider & Border
    val Divider = Color(0xFFE5E7EB)          // Light gray divider
    val Border = Color(0xFFD1D5DB)           // Border color
    val BorderLight = Color(0xFFF3F4F6)      // Very light border
    
    // Toggle/Switch
    val ToggleOn = Primary
    val ToggleOff = Color(0xFFD1D5DB)
    
    // Card Shadow (for elevation reference)
    val CardShadow = Color(0x0D000000)       // Very subtle shadow
}

/**
 * Common dimension values for consistent spacing
 */
object ShipperDimens {
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
}
