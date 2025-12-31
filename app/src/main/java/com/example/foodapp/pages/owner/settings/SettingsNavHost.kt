package com.example.foodapp.pages.owner.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SettingsNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "settings_main"
    ) {
        composable("settings_main") {
            SettingsScreen(navController = navController)
        }
        
        composable("personal_info") {
            PersonalInfoScreen(navController = navController)
        }
        
        composable("change_password") {
            ChangePasswordScreen(navController = navController)
        }
        
        composable("store_info") {
            StoreInfoScreen(navController = navController)
        }
        
        composable("payment_method") {
            PaymentMethodScreen(navController = navController)
        }
        
        composable("login_history") {
            LoginHistoryScreen(navController = navController)
        }
        
        composable("terms") {
            TermsScreen(navController = navController)
        }
        
        composable("privacy") {
            PrivacyScreen(navController = navController)
        }
        
        composable("support") {
            SupportScreen(navController = navController)
        }
    }
}
