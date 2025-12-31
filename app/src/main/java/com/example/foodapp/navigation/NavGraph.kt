package com.example.foodapp.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodapp.authentication.intro.IntroScreen
import com.example.foodapp.authentication.login.LoginScreen
import com.example.foodapp.pages.user.profile.UserProfileScreen
import com.example.foodapp.authentication.roleselection.RoleSelectionScreen
import com.example.foodapp.authentication.signup.SignUpScreen
import com.example.foodapp.presentation.view.user.home.UserHomeScreen
import com.example.foodapp.pages.user.cart.CartScreen
import com.example.foodapp.pages.user.favorites.FavoritesScreen
import com.example.foodapp.pages.user.notifications.UserNotificationsScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object RoleSelection : Screen("role_selection")
    object UserHome : Screen("user_home")
    object UserProfile : Screen("user_profile")
    object UserCart : Screen("user_cart")
    object UserFavorites : Screen("user_favorites")
    object UserNotifications : Screen("user_notifications")
    object ShipperHome : Screen("shipper_home")
    object OwnerHome : Screen("owner_home")
}

@Composable
fun FoodAppNavHost(
    navController: NavHostController,
) {


    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Intro.route) {
            IntroScreen(
                onCustomerClicked = { navController.navigate(Screen.UserHome.route) },
                onShipperClicked = { navController.navigate(Screen.ShipperHome.route) },
                onOwnerClicked = { navController.navigate(Screen.OwnerHome.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onBackClicked = { navController.popBackStack() },
                onLoginClicked = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ShipperHome.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                },
                onBackClicked = { navController.navigateUp() },
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) },
                onCustomerDemo = { navController.navigate(Screen.UserHome.route) },
                onShipperDemo = { navController.navigate(Screen.ShipperHome.route) },
                onOwnerDemo = { navController.navigate(Screen.OwnerHome.route) }
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSaved = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.UserHome.route) {
            UserHomeScreen(
                navController = navController,
                onProductClick = { /* TODO */ },
                onProfileClick = {
                    navController.navigate(Screen.UserProfile.route)
                }
            )
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)  // xóa toàn bộ stack
                    }
                }
            )
        }

        composable(Screen.UserCart.route) {
            CartScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.UserFavorites.route) {
            FavoritesScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.UserNotifications.route) {
            UserNotificationsScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ShipperHome.route) {
            // Màn hình shipper có sidebar
            com.example.foodapp.pages.shipper.dashboard.ShipperDashboardRootScreen(navController)
        }

        composable(Screen.OwnerHome.route) {
            // Màn hình owner có sidebar (bao gồm cả settings navigation)
            com.example.foodapp.pages.owner.dashboard.DashBoardRootScreen(navController)
        }
    }
}
