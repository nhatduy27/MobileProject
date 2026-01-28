package com.example.foodapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.foodapp.authentication.forgotpassword.emailinput.ForgotPasswordEmailScreen
import com.example.foodapp.authentication.forgotpassword.resetpassword.ResetPasswordScreen
import com.example.foodapp.authentication.forgotpassword.verifyotp.ForgotPasswordOTPScreen
import com.example.foodapp.authentication.intro.IntroScreen
import com.example.foodapp.authentication.login.LoginScreen
import com.example.foodapp.authentication.otpverification.OtpVerificationScreen
import com.example.foodapp.authentication.roleselection.RoleSelectionScreen
import com.example.foodapp.authentication.signup.SignUpScreen
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.pages.client.cart.CartScreen
import com.example.foodapp.pages.client.favorites.FavoritesScreen
import com.example.foodapp.pages.client.home.UserHomeScreen
import com.example.foodapp.pages.client.notifications.NotificationsScreen
import com.example.foodapp.pages.client.payment.PaymentScreen
import com.example.foodapp.pages.client.productdetail.UserProductDetailScreen
import com.example.foodapp.pages.client.userInfo.UserInfoScreen
import com.example.foodapp.pages.client.profile.UserProfileScreen
import com.example.foodapp.pages.client.orderdetail.OrderDetailScreen
import com.example.foodapp.pages.client.setting.SettingsScreen
import com.example.foodapp.pages.client.order.OrderScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.example.foodapp.pages.owner.dashboard.DashBoardRootScreen
import com.example.foodapp.pages.shipper.dashboard.ShipperDashboardRootScreen

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
    object UserOrder : Screen("user_order")

    object UserInfo : Screen("user_info")

    object UserProductDetail : Screen ("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    // SỬA: Screen cho thanh toán nhận danh sách sản phẩm dưới dạng chuỗi JSON
    object UserPayment : Screen("payment/{productsJson}/{quantitiesJson}") {
        fun createRoute(productsJson: String, quantitiesJson: String): String {
            val encodedProductsJson = URLEncoder.encode(productsJson, StandardCharsets.UTF_8.toString())
            val encodedQuantitiesJson = URLEncoder.encode(quantitiesJson, StandardCharsets.UTF_8.toString())
            return "payment/$encodedProductsJson/$encodedQuantitiesJson"
        }
    }
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object ShipperOrderDetail : Screen("shipper_order_detail/{orderId}") {
        fun createRoute(orderId: String) = "shipper_order_detail/$orderId"
    }
    object ShipperApply : Screen("shipper_apply")
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
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) }
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

        composable(Screen.UserCart.route) {
            CartScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() },
                onCheckoutShop = { products, quantities, _, _ ->
                    // Chuyển cả products và quantities thành JSON
                    val productsJson = Gson().toJson(products)
                    val quantitiesJson = Gson().toJson(quantities)
                    navController.navigate(Screen.UserPayment.createRoute(productsJson, quantitiesJson))
                }
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
                onNavigateToPayment = { product, quantity ->
                    // Tạo list products và quantities
                    val productsList = listOf(product)
                    val quantitiesList = listOf(quantity ?: 1)

                    val productsJson = Gson().toJson(productsList)
                    val quantitiesJson = Gson().toJson(quantitiesList)

                    navController.navigate(Screen.UserPayment.createRoute(productsJson, quantitiesJson))
                }
            )
        }

        composable(
            route = Screen.UserPayment.route,
            arguments = listOf(
                navArgument("productsJson") { type = NavType.StringType },
                navArgument("quantitiesJson") { type = NavType.StringType } // Thêm argument này
            )
        ) { backStackEntry ->
            val gson = Gson()

            // Lấy và decode products JSON
            val encodedProductsJson = backStackEntry.arguments?.getString("productsJson") ?: "[]"
            val productsJson = try {
                URLDecoder.decode(encodedProductsJson, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                "[]"
            }

            // Lấy và decode quantities JSON (THÊM PHẦN NÀY)
            val encodedQuantitiesJson = backStackEntry.arguments?.getString("quantitiesJson") ?: "[]"
            val quantitiesJson = try {
                URLDecoder.decode(encodedQuantitiesJson, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                "[]"
            }

            // Chuyển JSON thành List
            val productListType = object : TypeToken<List<Product>>() {}.type
            val quantityListType = object : TypeToken<List<Int>>() {}.type

            val productsToPay: List<Product> = try {
                gson.fromJson(productsJson, productListType)
            } catch (e: Exception) {
                emptyList()
            }

            val quantitiesToPay: List<Int> = try {
                gson.fromJson(quantitiesJson, quantityListType)
            } catch (e: Exception) {
                // Nếu không có quantity, tạo mặc định 1 cho mỗi sản phẩm
                List(productsToPay.size) { 1 }
            }

            if (productsToPay.isNotEmpty()) {
                PaymentScreen(
                    products = productsToPay,
                    quantities = quantitiesToPay, // TRUYỀN quantities vào PaymentScreen
                    onBackPressed = { navController.navigateUp() },
                    onOrderPlaced = {
                        navController.popBackStack(Screen.UserHome.route, false)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có sản phẩm nào để thanh toán.")
                }
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onBackClick = { navController.navigateUp() },
                onChangePasswordClick = { navController.navigate(Screen.UserSetting.route) },
                onOrderButtonClick = {
                    navController.navigate(Screen.UserOrder.route)
                },
                onUserInfoClick = {
                    navController.navigate(Screen.UserInfo.route)
                }
            )
        }

        composable(Screen.UserInfo.route) {
            UserInfoScreen(
                onBackClick = { navController.navigateUp() },
            )
        }

        // Thêm sau composable UserOrder (khoảng dòng 304 trở đi)
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.UserOrder.route) {
            OrderScreen(
                onBack = {
                    navController.navigateUp()
                },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
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
            NotificationsScreen( onBack = { navController.navigateUp() })
        }

        // Composable cho các vai trò khác (Shipper, Owner)
        composable(Screen.ShipperHome.route) {
            ShipperDashboardRootScreen(navController = navController)
        }
        
        composable(
            route = Screen.ShipperOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            com.example.foodapp.pages.shipper.order.ShipperOrderDetailScreen(
                orderId = orderId,
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(Screen.ShipperApply.route) {
            com.example.foodapp.pages.shipper.application.ShopSelectionScreen(
                onApplicationSubmitted = { navController.navigateUp() }
            )
        }

        composable(Screen.OwnerHome.route) {
            // Thay thế bằng màn hình Owner Home thực tế của bạn
            DashBoardRootScreen(navController = navController)
        }
    }
}
