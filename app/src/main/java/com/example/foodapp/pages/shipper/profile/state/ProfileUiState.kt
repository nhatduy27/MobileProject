package com.example.foodapp.pages.shipper.profile.state

import com.example.foodapp.data.model.shipper.ProfileMenuItem
import com.example.foodapp.data.model.shipper.ShipperProfile

// UI state cho màn Hồ sơ Shipper

data class ProfileUiState(
    val profile: ShipperProfile? = null,
    val accountItems: List<ProfileMenuItem> = emptyList(),
    val settingsItems: List<ProfileMenuItem> = emptyList(),
    val otherItems: List<ProfileMenuItem> = emptyList()
)
