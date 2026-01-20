package com.example.foodapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.foodapp.authentication.intro.IntroScreen
import com.example.foodapp.authentication.login.LoginScreen
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.pages.client.payment.PaymentScreen
import com.example.foodapp.pages.client.profile.UserProfileScreen
import com.example.foodapp.authentication.roleselection.RoleSelectionScreen
import com.example.foodapp.authentication.forgotpassword.emailinput.ForgotPasswordEmailScreen
import com.example.foodapp.authentication.forgotpassword.verifyotp.ForgotPasswordOTPScreen
import com.example.foodapp.authentication.forgotpassword.resetpassword.ResetPasswordScreen
import com.example.foodapp.pages.client.productdetail.UserProductDetailScreen
import com.example.foodapp.pages.client.setting.SettingsScreen
import com.example.foodapp.authentication.otpverification.OtpVerificationScreen
import com.example.foodapp.authentication.signup.SignUpScreen
import com.example.foodapp.pages.client.home.UserHomeScreen
import com.example.foodapp.pages.client.cart.CartScreen
import com.example.foodapp.pages.client.favorites.FavoritesScreen
import com.example.foodapp.pages.client.notifications.UserNotificationsScreen
import kotlinx.coroutines.launch

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
    object InputEmail : Screen("input_email")
    object OtpResetPassword : Screen("otp_resetpassword")
    object ResetPassword : Screen("resetpassword")
    object UserSetting : Screen ("setting")

    object UserProductDetail : Screen ("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    // THÊM: Screen cho thanh toán
    object UserPayment : Screen("payment")
}

@Composable
fun FoodAppNavHost(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val repository = remember { UserFirebaseRepository(context) }
    val authManager = remember { AuthManager(context) }

    var isLoading by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(Screen.Intro.route) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val isLoggedIn = authManager.isUserLoggedIn()
            if (isLoggedIn) {
                val token = authManager.getValidToken()
                if (token != null) {
                    val userId = authManager.getCurrentUserId()
                    if (userId != null) {
                        repository.getUserRole(userId) { role ->
                            if (role != null) {
                                repository.getVerifyStateByUid() { isVerified ->
                                    destination = if (isVerified) {
                                        when (role.uppercase()) {
                                            "CUSTOMER" -> Screen.UserHome.route
                                            "OWNER" -> Screen.OwnerHome.route
                                            "SHIPPER" -> Screen.ShipperHome.route
                                            else -> Screen.UserHome.route
                                        }
                                    } else {
                                        Screen.OtpVerification.route
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
                } else {
                    authManager.clearAuthData()
                    destination = Screen.Intro.route
                    isLoading = false
                }
            } else {
                destination = Screen.Intro.route
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                onSignUpSuccess = { navController.navigate(Screen.OtpVerification.route) },
                onBackClicked = { navController.navigateUp() },
                onLoginClicked = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.OtpVerification.route) {
            OtpVerificationScreen(
                onVerificationSuccess = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                },
                onBackClicked = { navController.navigateUp() },
                onResendRequest = { /* Logic gửi lại OTP */ }
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSaved = { role ->
                    val dest = when (role.uppercase()) {
                        "CUSTOMER" -> Screen.UserHome.route
                        "OWNER" -> Screen.OwnerHome.route
                        "SHIPPER" -> Screen.ShipperHome.route
                        else -> Screen.UserHome.route
                    }
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val userId = authManager.getCurrentUserId()
                    if (userId != null) {
                        repository.getVerifyStateByUid() { isVerified ->
                            if (isVerified) {
                                val dest = when (role.uppercase()) {
                                    "CUSTOMER" -> Screen.UserHome.route
                                    "OWNER" -> Screen.OwnerHome.route
                                    "SHIPPER" -> Screen.ShipperHome.route
                                    else -> Screen.UserHome.route
                                }
                                navController.navigate(dest) {
                                    popUpTo(Screen.Intro.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.OtpVerification.route)
                            }
                        }
                    }
                },
                onForgotPasswordClicked = { navController.navigate(Screen.InputEmail.route) },
                onBackClicked = { navController.navigateUp() },
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) },
                onCustomerDemo = { navController.navigate(Screen.UserHome.route) },
                onShipperDemo = { navController.navigate(Screen.ShipperHome.route) },
                onOwnerDemo = { navController.navigate(Screen.OwnerHome.route) }
            )
        }

        composable(Screen.InputEmail.route) {
            ForgotPasswordEmailScreen(
                onBackClicked = { navController.navigateUp() },
                onSuccess = { navController.navigate(Screen.OtpResetPassword.route) }
            )
        }

        composable(Screen.OtpResetPassword.route) {
            ForgotPasswordOTPScreen(
                onBackClicked = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onSuccess = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onBackClicked = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onSuccess = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.UserHome.route) {
            UserHomeScreen(
                navController = navController,
                onProductClick = { productId ->
                    navController.navigate(Screen.UserProductDetail.createRoute(productId))
                },
                onProfileClick = { navController.navigate(Screen.UserProfile.route) }
            )
        }

        composable(
            route = Screen.UserProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UserProductDetailScreen(
                productId = productId,
                onBackPressed = {
                    navController.navigateUp()
                },
                onNavigateToPayment = { product ->
                    // Truyền product qua SavedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("payment_product", product)
                    navController.navigate(Screen.UserPayment.route)
                }
            )
        }

        // Màn hình thanh toána
        composable(Screen.UserPayment.route) { backStackEntry ->
            val product = remember {
                navController.previousBackStackEntry?.savedStateHandle?.get<Product>("payment_product")
            }

            if (product != null) {
                PaymentScreen(
                    product = product,
                    onBackPressed = { navController.navigateUp() },
                    onOrderPlaced = {
                        navController.popBackStack(Screen.UserHome.route, false)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onBackClick = { navController.navigateUp() },
                onEditAddressClick = { },
                onChangePasswordClick = { navController.navigate(Screen.UserSetting.route) }
            )
        }

        composable(Screen.UserSetting.route) {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                onLogout = {
                    authManager.clearAuthData()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onChangePassword= { },
                onDeleteAccount = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserCart.route) {
            CartScreen(navController = navController, onBackClick = { navController.navigateUp() })
        }

        composable(Screen.UserFavorites.route) {
            FavoritesScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() },
                onProductClick = { productId ->
                    navController.navigate(Screen.UserProductDetail.createRoute(productId))
                }
            )
        }

        composable(Screen.UserNotifications.route) {
            UserNotificationsScreen(navController = navController, onBackClick = { navController.navigateUp() })
        }

        composable(Screen.ShipperHome.route) {
            com.example.foodapp.pages.shipper.dashboard.ShipperDashboardRootScreen(navController)
        }

        composable(Screen.OwnerHome.route) {
            com.example.foodapp.pages.owner.dashboard.DashBoardRootScreen(navController)
        }
    }
}