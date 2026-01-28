package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.ProfileAction
import com.example.foodapp.pages.shipper.profile.components.ProfileMenuCard
import com.example.foodapp.pages.shipper.profile.state.ProfileViewModel
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onVehicleInfo: () -> Unit = {},
    onPaymentMethod: () -> Unit = {},
    onNotificationSettings: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onTerms: () -> Unit = {},
    onHelp: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileMenuCard(
                title = "Tài khoản",
                items = uiState.accountItems,
                onItemClick = {
                    when (it) {
                        ProfileAction.EDIT_PROFILE -> onEditProfile()
                        ProfileAction.CHANGE_PASSWORD -> onChangePassword()
                        ProfileAction.VEHICLE_INFO -> onVehicleInfo()
                        ProfileAction.PAYMENT_METHOD -> onPaymentMethod()
                        ProfileAction.NOTIFICATIONS -> onNotificationSettings()
                        else -> {}
                    }
                }
            )
            ProfileMenuCard(
                title = "Cài đặt",
                items = uiState.settingsItems,
                onItemClick = {
                    when (it) {
                        ProfileAction.NOTIFICATIONS -> onNotificationSettings()
                        ProfileAction.LANGUAGE -> onLanguage()
                        ProfileAction.PRIVACY -> onPrivacy()
                        ProfileAction.TERMS -> onTerms()
                        else -> {}
                    }
                }
            )
            ProfileMenuCard(
                title = "Khác",
                items = uiState.otherItems,
                onItemClick = {
                    if (it == ProfileAction.HELP) onHelp()
                }
            )
        }
    }
}
