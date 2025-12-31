package com.example.foodapp.pages.shipper.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodapp.pages.shipper.profile.*

@Composable
fun ShipperSettingsNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "settings_main"
    ) {
        composable("settings_main") {
            ProfileScreen(
                onEditProfile = { navController.navigate("edit_profile") },
                onChangePassword = { navController.navigate("change_password") },
                onVehicleInfo = { navController.navigate("vehicle_info") },
                onPaymentMethod = { navController.navigate("payment_method") },
                onNotificationSettings = { navController.navigate("notification_settings") },
                onLanguage = { navController.navigate("language") },
                onPrivacy = { navController.navigate("privacy") },
                onTerms = { navController.navigate("terms") },
                onHelp = { navController.navigate("help_screen") }
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
