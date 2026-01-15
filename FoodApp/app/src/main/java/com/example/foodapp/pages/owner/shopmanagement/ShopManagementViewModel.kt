package com.example.foodapp.pages.owner.shopmanagement

import android.content.Context
import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.owner.shop.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Shop Management
 */
class ShopManagementViewModel(context: Context) : ViewModel() {
    
    private val repository = ShopRepository(context)
    
    private val _uiState = MutableStateFlow(ShopManagementUiState())
    val uiState: StateFlow<ShopManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadShopData()
    }
    

    /**
     * Load thông tin shop hiện tại
     */
    private fun loadShopData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            Log.d("ShopDebug", "Start loading shop data...")
            val result = repository.getMyShop()
            
            result.onSuccess { shop ->
                Log.d("ShopDebug", "Shop loaded successfully: $shop")
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        shopId = shop.id ?: "",
                        shopName = shop.name ?: "",
                        description = shop.description ?: "",
                        address = shop.address ?: "",
                        phone = shop.phone ?: "",
                        openTime = shop.openTime ?: "",
                        closeTime = shop.closeTime ?: "",
                        shipFee = shop.shipFeePerOrder?.toString() ?: "0",
                        minOrderAmount = shop.minOrderAmount?.toString() ?: "0",
                        coverImageUrl = shop.coverImageUrl ?: "",
                        logoUrl = shop.logoUrl ?: ""
                    )
                }
            }.onFailure { error ->
                Log.e("ShopDebug", "Failed to load shop: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Không thể tải thông tin shop"
                    )
                }
            }
        }
    }
    
    // Update methods
    fun updateShopName(name: String) {
        _uiState.update { it.copy(shopName = name, shopNameError = null) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }
    
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address, addressError = null) }
    }
    
    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }
    
    fun updateOpenTime(time: String) {
        _uiState.update { it.copy(openTime = time, openTimeError = null) }
    }
    
    fun updateCloseTime(time: String) {
        _uiState.update { it.copy(closeTime = time, closeTimeError = null) }
    }
    
    fun updateShipFee(fee: String) {
        _uiState.update { it.copy(shipFee = fee, shipFeeError = null) }
    }
    
    fun updateMinOrderAmount(amount: String) {
        _uiState.update { it.copy(minOrderAmount = amount, minOrderAmountError = null) }
    }
    
    fun updateCoverImage(uri: Uri?) {
        _uiState.update { it.copy(newCoverImageUri = uri) }
    }
    
    fun updateLogo(uri: Uri?) {
        _uiState.update { it.copy(newLogoUri = uri) }
    }
    
    /**
     * Xóa error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Xóa success message
     */
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Validate form
     */
    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        // Validate shop name
        if (state.shopName.isBlank()) {
            _uiState.update { it.copy(shopNameError = "Tên shop không được để trống") }
            isValid = false
        } else if (state.shopName.length < 3) {
            _uiState.update { it.copy(shopNameError = "Tên shop phải có ít nhất 3 ký tự") }
            isValid = false
        } else if (state.shopName.length > 100) {
            _uiState.update { it.copy(shopNameError = "Tên shop không được quá 100 ký tự") }
            isValid = false
        }
        
        // Validate description
        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Mô tả không được để trống") }
            isValid = false
        } else if (state.description.length > 500) {
            _uiState.update { it.copy(descriptionError = "Mô tả không được quá 500 ký tự") }
            isValid = false
        }
        
        // Validate address
        if (state.address.isBlank()) {
            _uiState.update { it.copy(addressError = "Địa chỉ không được để trống") }
            isValid = false
        } else if (state.address.length > 200) {
            _uiState.update { it.copy(addressError = "Địa chỉ không được quá 200 ký tự") }
            isValid = false
        }
        
        // Validate phone
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Số điện thoại không được để trống") }
            isValid = false
        } else if (!state.phone.matches(Regex("^[0-9]{10}$"))) {
            _uiState.update { it.copy(phoneError = "Số điện thoại phải là 10 chữ số") }
            isValid = false
        }
        
        // Validate open time
        if (!state.openTime.matches(Regex("^([0-1][0-9]|2[0-3]):[0-5][0-9]$"))) {
            _uiState.update { it.copy(openTimeError = "Giờ mở cửa không hợp lệ (HH:mm)") }
            isValid = false
        }
        
        // Validate close time
        if (!state.closeTime.matches(Regex("^([0-1][0-9]|2[0-3]):[0-5][0-9]$"))) {
            _uiState.update { it.copy(closeTimeError = "Giờ đóng cửa không hợp lệ (HH:mm)") }
            isValid = false
        }
        
        // Validate ship fee
        val shipFee = state.shipFee.toIntOrNull()
        if (shipFee == null) {
            _uiState.update { it.copy(shipFeeError = "Phí ship phải là số") }
            isValid = false
        } else if (shipFee < 3000) {
            _uiState.update { it.copy(shipFeeError = "Phí ship tối thiểu 3,000đ") }
            isValid = false
        }
        
        // Validate min order amount
        val minOrder = state.minOrderAmount.toIntOrNull()
        if (minOrder == null) {
            _uiState.update { it.copy(minOrderAmountError = "Đơn tối thiểu phải là số") }
            isValid = false
        } else if (minOrder < 10000) {
            _uiState.update { it.copy(minOrderAmountError = "Đơn tối thiểu phải từ 10,000đ") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Cập nhật thông tin shop
     */
    fun updateShop(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            
            val state = _uiState.value
            
            val result = repository.updateShopWithImages(
                name = state.shopName,
                description = state.description,
                address = state.address,
                phone = state.phone,
                openTime = state.openTime,
                closeTime = state.closeTime,
                shipFeePerOrder = state.shipFee.toInt(),
                minOrderAmount = state.minOrderAmount.toInt(),
                coverImageUri = state.newCoverImageUri,
                logoUri = state.newLogoUri
            )
            
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Cập nhật shop thành công!",
                        newCoverImageUri = null,
                        newLogoUri = null
                    )
                }
                // Reload shop data to get updated URLs
                loadShopData()
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra khi cập nhật shop"
                    )
                }
            }
        }
    }
}
