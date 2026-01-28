package com.example.foodapp.pages.shipper.settings

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShipperSettingsUiState(
    val isOnline: Boolean = false,
    val isTogglingOnline: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ShipperSettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = RepositoryProvider.getShipperOrderRepository()
    private val prefs = application.getSharedPreferences("shipper_settings", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(ShipperSettingsUiState())
    val uiState: StateFlow<ShipperSettingsUiState> = _uiState.asStateFlow()
    
    init {
        // Load saved online status from SharedPreferences
        val savedOnlineStatus = prefs.getBoolean("is_online", false)
        _uiState.update { it.copy(isOnline = savedOnlineStatus) }
        Log.d("ShipperSettingsVM", "Loaded saved online status: $savedOnlineStatus")
    }
    
    fun toggleOnlineStatus() {
        val currentlyOnline = _uiState.value.isOnline
        
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingOnline = true, error = null) }
            
            val result = if (currentlyOnline) {
                repository.goOffline()
            } else {
                repository.goOnline()
            }
            
            result.onSuccess { topic ->
                val newOnlineState = !currentlyOnline
                
                // Save to SharedPreferences
                prefs.edit().putBoolean("is_online", newOnlineState).apply()
                
                val message = if (newOnlineState) "Đang nhận đơn hàng mới" else "Đã tắt nhận đơn"
                
                _uiState.update {
                    it.copy(
                        isOnline = newOnlineState,
                        isTogglingOnline = false,
                        message = message
                    )
                }
                
                Log.d("ShipperSettingsVM", "Online status changed to: $newOnlineState, topic: $topic")
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isTogglingOnline = false,
                        error = e.message
                    )
                }
                Log.e("ShipperSettingsVM", "Failed to toggle online status", e)
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
