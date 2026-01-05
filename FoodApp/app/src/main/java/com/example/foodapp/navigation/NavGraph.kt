package com.example.foodapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodapp.authentication.intro.IntroScreen
import com.example.foodapp.authentication.login.LoginScreen
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.pages.user.profile.UserProfileScreen
import com.example.foodapp.authentication.roleselection.RoleSelectionScreen
import com.example.foodapp.authentication.forgotPassword.NewPasswordScreen
import com.example.foodapp.authentication.OtpVerification.OtpVerificationScreen
import com.example.foodapp.authentication.signup.SignUpScreen
import com.example.foodapp.presentation.view.user.home.UserHomeScreen
import com.example.foodapp.pages.user.cart.CartScreen
import com.example.foodapp.pages.user.favorites.FavoritesScreen
import com.example.foodapp.pages.user.notifications.UserNotificationsScreen

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import com.example.foodapp.authentication.OtpVerification.OtpVerificationScreen

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object OtpVerification : Screen("otp_verification")

    object RoleSelection : Screen("role_selection")
    object UserHome : Screen("user_home")
    object UserProfile : Screen("user_profile")
    object UserCart : Screen("user_cart")
    object UserFavorites : Screen("user_favorites")
    object UserNotifications : Screen("user_notifications")
    object ShipperHome : Screen("shipper_home")
    object OwnerHome : Screen("owner_home")
    object NewPasswordScreen : Screen("new_password")
}

@Composable
fun FoodAppNavHost(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val repository = remember { UserFirebaseRepository(context) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var isLoading by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(Screen.Intro.route) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            //Kiểm tra role
            repository.getUserRole(currentUser.uid) { role ->
                if (role != null) {
                    //Kiểm tra verify state
                    repository.getVerifyStateByUid() { isVerified ->
                        destination = if (isVerified) {
                            //Vào trang home theo role
                            when (role) {
                                "user" -> Screen.UserHome.route
                                "seller" -> Screen.OwnerHome.route
                                "delivery" -> Screen.ShipperHome.route
                                else -> Screen.UserHome.route
                            }
                        } else {
                            Screen.Intro.route
                        }
                        isLoading = false
                    }
                } else {
                    destination = Screen.Intro.route
                    isLoading = false
                }
            }
        } else {
            destination = Screen.Intro.route
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = destination
    ) {
        composable(Screen.Intro.route) {
            IntroScreen(
                onStartClicked = { navController.navigate(Screen.SignUp.route) },
                onLoginClicked = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.OtpVerification.route)
                },
                onBackClicked = { navController.navigateUp() },
                onLoginClicked = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.OtpVerification.route,
        ) {
            OtpVerificationScreen(
                onVerificationSuccess = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                },
                onBackClicked = {
                    navController.navigateUp()
                },
                onResendRequest = {
                    // Gửi lại OTP (cần thêm logic sau)
                    // viewModel.resendOtp(email)
                }
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSaved = { role ->
                    val destination = when (role) {
                        "user" -> Screen.UserHome.route
                        "seller" -> Screen.OwnerHome.route
                        "delivery" -> Screen.ShipperHome.route
                        else -> Screen.UserHome.route
                    }

                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    repository.getVerifyStateByUid() { isVerified ->
                        if (isVerified) {
                            val destination = when (role) {
                                "user" -> Screen.UserHome.route
                                "seller" -> Screen.OwnerHome.route
                                "delivery" -> Screen.ShipperHome.route
                                else -> Screen.UserHome.route
                            }

                            navController.navigate(destination) {
                                popUpTo(Screen.Intro.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.OtpVerification.route)
                        }
                    }
                },
                onForgotPasswordClicked = {
                    navController.navigate(Screen.NewPasswordScreen.route)
                },
                onBackClicked = { navController.navigateUp() },
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) },
                onCustomerDemo = { navController.navigate(Screen.UserHome.route) },
                onShipperDemo = { navController.navigate(Screen.ShipperHome.route) },
                onOwnerDemo = { navController.navigate(Screen.OwnerHome.route) }
            )
        }


        composable(Screen.NewPasswordScreen.route) {
            NewPasswordScreen(
                onBackClicked = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onResetEmailSent = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
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
                onBackClick = { navController.navigateUp() },
                onLogoutClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.UserCart.route) {
            CartScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.UserFavorites.route) {
            FavoritesScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.UserNotifications.route) {
            UserNotificationsScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.ShipperHome.route) {
            com.example.foodapp.pages.shipper.dashboard.ShipperDashboardRootScreen(navController)
        }

        composable(Screen.OwnerHome.route) {
            com.example.foodapp.pages.owner.dashboard.DashBoardRootScreen(navController)
        }
    }
}