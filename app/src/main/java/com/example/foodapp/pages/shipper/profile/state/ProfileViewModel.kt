package com.example.foodapp.pages.shipper.profile.state

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.repository.shipper.profile.MockShipperProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val repository = MockShipperProfileRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val profile = repository.getProfile()
        _uiState.value = ProfileUiState(
            profile = profile,
            accountItems = repository.getAccountItems(profile),
            settingsItems = repository.getSettingsItems(),
            otherItems = repository.getOtherItems()
        )
    }
}
