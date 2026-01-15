package com.example.foodapp.pages.owner.shopsetup

import android.net.Uri

/**
 * UI State cho màn hình Shop Setup
 */
data class ShopSetupUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Form fields
    val shopName: String = "",
    val description: String = "",
    val address: String = "",
    val phone: String = "",
    val openTime: String = "07:00",
    val closeTime: String = "21:00",
    val shipFee: String = "5000",
    val minOrderAmount: String = "20000",
    
    // Image URIs
    val coverImageUri: Uri? = null,
    val logoUri: Uri? = null,
    
    // Validation errors
    val shopNameError: String? = null,
    val descriptionError: String? = null,
    val addressError: String? = null,
    val phoneError: String? = null,
    val openTimeError: String? = null,
    val closeTimeError: String? = null,
    val shipFeeError: String? = null,
    val minOrderAmountError: String? = null,
    val coverImageError: String? = null,
    val logoError: String? = null
)
