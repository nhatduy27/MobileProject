package com.example.foodapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodapp.authentication.intro.IntroScreen
import com.example.foodapp.authentication.login.LoginScreen
import com.example.foodapp.user.profile.UserProfileScreen
import com.example.foodapp.authentication.roleselection.RoleSelectionScreen
import com.example.foodapp.authentication.signup.SignUpScreen
import com.example.foodapp.presentation.view.user.home.UserHomeScreen
import com.example.foodapp.user.home.*
import com.example.foodapp.data.model.Product

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object RoleSelection : Screen("role_selection")
    object UserHome : Screen("user_home")
    object  UserProfile : Screen ("user_profile")
}

@Composable
fun FoodAppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Intro.route
) {
    // Lấy context từ Composable
    val context = LocalContext.current

    // Tạo MainViewModel với factory
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModel.factory(context)
    )

    val sampleProducts = listOf(
        Product(
            name = "Matcha Latte",
            description = "Ngon tuyệt",
            price = "20.000đ",
            priceValue = 20.0,
            imageRes = com.example.foodapp.R.drawable.matchalatte,
            category = com.example.foodapp.data.model.FoodCategory.DRINK
        ),
        Product(
            name = "Classic Pizza",
            description = "Nhiều phô mai",
            price = "150.000đ",
            priceValue = 150.0,
            imageRes = com.example.foodapp.R.drawable.data_3,
            category = com.example.foodapp.data.model.FoodCategory.FOOD
        )
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                },
                onBackClicked = { navController.navigateUp() },
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) }
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
                viewModel = mainViewModel,
                productList = sampleProducts,
                onProductClick = { /* TODO */ },
                onProfileClick = {
                    navController.navigate(Screen.UserProfile.route) {

                    }
                }
            )
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onBackClick = { navController.popBackStack() }, // Quay về home
                onLogoutClick = {
                    // Đăng xuất về login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                },
                onProfileClick = { /* Không làm gì */ }
            )
        }
    }
}

@Composable
fun DashboardPlaceholder(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}