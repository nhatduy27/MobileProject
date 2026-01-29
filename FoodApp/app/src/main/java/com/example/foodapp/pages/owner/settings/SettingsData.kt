package com.example.foodapp.pages.owner.settings

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val isEnabled: Boolean = false,
    val isDisabled: Boolean = false, // Đánh dấu tính năng chưa phát triển (sẽ làm mờ UI)
    val onClick: (() -> Unit)? = null
)

data class SettingSection(
    val title: String,
    val items: List<SettingItem>
)
