package com.example.foodapp.pages.owner.shopsetup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.owner.CreateShopRequest
import com.example.foodapp.data.repository.owner.shop.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Shop Setup
 */
class ShopSetupViewModel(context: Context) : ViewModel() {
    
    private val repository = ShopRepository(context)
    
    private val _uiState = MutableStateFlow(ShopSetupUiState())
    val uiState: StateFlow<ShopSetupUiState> = _uiState.asStateFlow()
    
    /**
     * Cập nhật tên shop
     */
    fun updateShopName(name: String) {
        _uiState.update { it.copy(shopName = name, shopNameError = null) }
    }
    
    /**
     * Cập nhật mô tả
     */
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }
    
    /**
     * Cập nhật địa chỉ
     */
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address, addressError = null) }
    }
    
    /**
     * Cập nhật số điện thoại
     */
    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }
    
    /**
     * Cập nhật giờ mở cửa
     */
    fun updateOpenTime(time: String) {
        _uiState.update { it.copy(openTime = time, openTimeError = null) }
    }
    
    /**
     * Cập nhật giờ đóng cửa
     */
    fun updateCloseTime(time: String) {
        _uiState.update { it.copy(closeTime = time, closeTimeError = null) }
    }
    
    /**
     * Cập nhật phí ship
     */
    fun updateShipFee(fee: String) {
        _uiState.update { it.copy(shipFee = fee, shipFeeError = null) }
    }
    
    /**
     * Cập nhật đơn tối thiểu
     */
    fun updateMinOrderAmount(amount: String) {
        _uiState.update { it.copy(minOrderAmount = amount, minOrderAmountError = null) }
    }
    
    /**
     * Cập nhật ảnh bìa
     */
    fun updateCoverImage(uri: Uri?) {
        _uiState.update { it.copy(coverImageUri = uri, coverImageError = null) }
    }
    
    /**
     * Cập nhật logo
     */
    fun updateLogo(uri: Uri?) {
        _uiState.update { it.copy(logoUri = uri, logoError = null) }
    }
    
    /**
     * Xóa error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
        
        // Validate cover image
        if (state.coverImageUri == null) {
            _uiState.update { it.copy(coverImageError = "Vui lòng chọn ảnh bìa") }
            isValid = false
        }
        
        // Validate logo
        if (state.logoUri == null) {
            _uiState.update { it.copy(logoError = "Vui lòng chọn logo") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Tạo shop mới
     */
    fun createShop(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val state = _uiState.value
            
            // Gọi method mới với ảnh
            val result = repository.createShopWithImages(
                name = state.shopName,
                description = state.description,
                address = state.address,
                phone = state.phone,
                openTime = state.openTime,
                closeTime = state.closeTime,
                shipFeePerOrder = state.shipFee.toInt(),
                minOrderAmount = state.minOrderAmount.toInt(),
                coverImageUri = state.coverImageUri!!,
                logoUri = state.logoUri!!
            )
            
            result.onSuccess {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Tạo shop thành công!"
                    ) 
                }
                onSuccess()
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra khi tạo shop"
                    ) 
                }
            }
        }
    }
}
