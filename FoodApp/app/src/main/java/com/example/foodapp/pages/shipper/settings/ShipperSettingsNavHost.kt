package com.example.foodapp.pages.shipper.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodapp.pages.shipper.profile.*

@Composable
fun ShipperSettingsNavHost(
    navController: NavHostController,
    onLogout: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "settings_main"
    ) {
        composable("settings_main") {
            ShipperSettingsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = onLogout
            )
        }
        
        composable("edit_profile") {
            EditProfileScreen(onCancel = { navController.navigateUp() })
        }
        
        composable("change_password") {
            ChangePasswordScreen(onCancel = { navController.navigateUp() })
        }
        
        composable("vehicle_info") {
            VehicleInfoScreen(onCancel = { navController.navigateUp() })
        }
        
        composable("payment_method") {
            PaymentMethodScreen(onCancel = { navController.navigateUp() })
        }
        
        composable("notification_settings") {
            NotificationSettingsScreen(onCancel = { navController.navigateUp() })
        }
        
        composable("language") {
            LanguageScreen(onCancel = { navController.navigateUp() })
        }
        
        composable("terms") {
            TermsScreen(onBack = { navController.navigateUp() })
        }
        
        composable("privacy") {
            PrivacyScreen(onBack = { navController.navigateUp() })
        }
        
        composable("help_screen") {
            HelpScreen(onBack = { navController.navigateUp() })
        }
    }
}

