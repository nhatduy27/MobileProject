package com.example.foodapp.pages.owner.settings

data class SettingItem(
    val title: String,
    val subtitle: String? = null,
    val icon: String,
    val hasSwitch: Boolean = false,
    val isEnabled: Boolean = false,
    val onClick: (() -> Unit)? = null
)

data class SettingSection(
    val title: String,
    val items: List<SettingItem>
)
