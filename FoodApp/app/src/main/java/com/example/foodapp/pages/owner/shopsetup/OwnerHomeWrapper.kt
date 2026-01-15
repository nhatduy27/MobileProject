package com.example.foodapp.pages.owner.shopsetup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.foodapp.data.repository.owner.shop.ShopRepository
import kotlinx.coroutines.launch

/**
 * Wrapper screen để kiểm tra xem owner đã setup shop chưa
 * Nếu chưa thì redirect đến ShopSetup
 * Nếu rồi thì hiển thị OwnerHome
 */
@Composable
fun OwnerHomeWrapper(
    navController: NavHostController,
    shopSetupRoute: String,
    content: @Composable () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasShop by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { ShopRepository(context) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val result = repository.getMyShop()
            result.onSuccess {
                hasShop = true
                isLoading = false
            }.onFailure {
                // Chưa có shop, cần setup
                hasShop = false
                isLoading = false
            }
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        if (hasShop) {
            content()
        } else {
            // Redirect to shop setup
            LaunchedEffect(Unit) {
                navController.navigate(shopSetupRoute) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                }
            }
        }
    }
}
