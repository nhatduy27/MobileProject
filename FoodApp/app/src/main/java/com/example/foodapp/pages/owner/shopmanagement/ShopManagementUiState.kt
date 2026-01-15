package com.example.foodapp.pages.owner.shopmanagement

import android.net.Uri

/**
 * UI State cho màn hình Shop Management
 */
data class ShopManagementUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Shop data
    val shopId: String = "",
    val shopName: String = "",
    val description: String = "",
    val address: String = "",
    val phone: String = "",
    val openTime: String = "",
    val closeTime: String = "",
    val shipFee: String = "",
    val minOrderAmount: String = "",
    val coverImageUrl: String = "",
    val logoUrl: String = "",
    
    // New images (for update)
    val newCoverImageUri: Uri? = null,
    val newLogoUri: Uri? = null,
    
    // Validation errors
    val shopNameError: String? = null,
    val descriptionError: String? = null,
    val addressError: String? = null,
    val phoneError: String? = null,
    val openTimeError: String? = null,
    val closeTimeError: String? = null,
    val shipFeeError: String? = null,
    val minOrderAmountError: String? = null
)
