package com.example.foodapp.pages.shipper.application

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.application.ShipperApplication
import com.example.foodapp.data.model.shipper.application.ShopForApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ShopSelectionUiState(
    val shops: List<ShopForApplication> = emptyList(),
    val myApplications: List<ShipperApplication> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedShop: ShopForApplication? = null,
    val showApplyDialog: Boolean = false,
    val isApplying: Boolean = false,
    val applySuccess: Boolean = false,
    val hasPendingApplication: Boolean = false
)

class ShopSelectionViewModel : ViewModel() {
    private val repository = RepositoryProvider.getShipperApplicationRepository()
    
    private val _uiState = MutableStateFlow(ShopSelectionUiState())
    val uiState: StateFlow<ShopSelectionUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        loadShops()
        loadMyApplications()
    }
    
    fun loadShops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getShops(1, 50, _uiState.value.searchQuery.ifBlank { null })
                .onSuccess { response ->
                    _uiState.update { 
                        it.copy(
                            shops = response.shops,
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { e ->
                    Log.e("ShopSelectionVM", "Failed to load shops", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
    
    fun loadMyApplications() {
        viewModelScope.launch {
            repository.getMyApplications()
                .onSuccess { applications ->
                    val hasPending = applications.any { it.status == "PENDING" }
                    _uiState.update { 
                        it.copy(
                            myApplications = applications,
                            hasPendingApplication = hasPending
                        ) 
                    }
                }
                .onFailure { e ->
                    Log.e("ShopSelectionVM", "Failed to load applications", e)
                }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadShops()
    }
    
    fun onShopSelected(shop: ShopForApplication) {
        // Kiểm tra xem đã apply shop này chưa
        val alreadyApplied = _uiState.value.myApplications.any { 
            it.shopId == shop.id && it.status == "PENDING" 
        }
        
        if (alreadyApplied) {
            _uiState.update { it.copy(error = "Bạn đã gửi đơn đến cửa hàng này rồi") }
            return
        }
        
        _uiState.update { 
            it.copy(selectedShop = shop, showApplyDialog = true) 
        }
    }
    
    fun onDismissApplyDialog() {
        _uiState.update { it.copy(showApplyDialog = false, selectedShop = null) }
    }
    
    fun applyToShop(
        vehicleType: String,
        vehicleNumber: String,
        idCardNumber: String,
        message: String?,
        idCardFrontFile: File,
        idCardBackFile: File,
        driverLicenseFile: File
    ) {
        val shop = _uiState.value.selectedShop ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true, error = null) }
            
            repository.applyShipper(
                shopId = shop.id,
                vehicleType = vehicleType,
                vehicleNumber = vehicleNumber,
                idCardNumber = idCardNumber,
                message = message,
                idCardFrontFile = idCardFrontFile,
                idCardBackFile = idCardBackFile,
                driverLicenseFile = driverLicenseFile
            ).onSuccess { application ->
                _uiState.update { 
                    it.copy(
                        isApplying = false,
                        showApplyDialog = false,
                        applySuccess = true,
                        selectedShop = null
                    ) 
                }
                loadMyApplications() // Refresh applications
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(isApplying = false, error = e.message) 
                }
            }
        }
    }
    
    fun cancelApplication(applicationId: String) {
        viewModelScope.launch {
            repository.cancelApplication(applicationId)
                .onSuccess {
                    loadMyApplications()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(applySuccess = false) }
    }
}
