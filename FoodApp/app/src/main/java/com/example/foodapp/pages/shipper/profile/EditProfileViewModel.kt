package com.example.foodapp.pages.shipper.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel cho EditProfileScreen
 * Handles profile loading and updating via API
 */
class EditProfileViewModel : ViewModel() {

    private val repository = RepositoryProvider.getUserProfileRepository()

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.getProfile()
            result.onSuccess { profile ->
                Log.d("EditProfileVM", "✅ Profile loaded: ${profile.displayName}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    displayName = profile.displayName,
                    email = profile.email,
                    phone = profile.phone ?: "",
                    avatarUrl = profile.avatarUrl ?: ""
                )
            }.onFailure { error ->
                Log.e("EditProfileVM", "❌ Failed to load profile: ${error.message}")
                // Fallback to Firebase Auth data
                val currentUser = FirebaseAuth.getInstance().currentUser
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    email = currentUser?.email ?: "",
                    displayName = currentUser?.displayName ?: "",
                    phone = currentUser?.phoneNumber ?: "",
                    error = error.message
                )
            }
        }
    }

    fun updateDisplayName(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value)
    }

    fun updatePhone(value: String) {
        _uiState.value = _uiState.value.copy(phone = value)
    }

    fun updateAddress(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
    }

    fun updateVehicleType(value: String) {
        _uiState.value = _uiState.value.copy(vehicleType = value)
    }

    fun setEditing(editing: Boolean) {
        _uiState.value = _uiState.value.copy(isEditing = editing)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, successMessage = null)

            val state = _uiState.value
            val result = repository.updateProfile(
                displayName = state.displayName.takeIf { it.isNotBlank() },
                phone = state.phone.takeIf { it.isNotBlank() }
            )

            result.onSuccess { profile ->
                Log.d("EditProfileVM", "✅ Profile updated successfully")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isEditing = false,
                    profile = profile,
                    successMessage = "Cập nhật thông tin thành công!"
                )
            }.onFailure { error ->
                Log.e("EditProfileVM", "❌ Failed to update profile: ${error.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = error.message ?: "Cập nhật thất bại"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val profile: UserProfile? = null,
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val vehicleType: String = "",
    val avatarUrl: String = "",
    val error: String? = null,
    val successMessage: String? = null
)
