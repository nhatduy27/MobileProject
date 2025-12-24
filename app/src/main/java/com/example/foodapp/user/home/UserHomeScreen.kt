package com.example.foodapp.presentation.view.user.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.user.components.*
import com.example.foodapp.user.home.MainViewModel
import com.example.foodapp.user.home.UserNameState
import com.example.foodapp.data.model.Product

@Composable
fun UserHomeScreen(
    viewModel: MainViewModel,
    productList: List<Product>,
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit
) {
    // Quan sát trạng thái từ ViewModel
    val nameState by viewModel.userNameState.observeAsState(UserNameState.Idle)

    // Gọi fetchUserName khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
    }

    // Gọi hàm Content để hiển thị giao diện
    UserHomeContent(
        nameState = nameState,
        productList = productList,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick
    )
}

@Composable
fun UserHomeContent(
    nameState: UserNameState,
    productList: List<Product>,
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = { UserBottomNav(onProfileClick = onProfileClick) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(8.dp)
        ) {
            // Phần Header và Danh mục (Chiếm 2 cột)
            item(span = { GridItemSpan(2) }) {
                Column {
                    UserHeader(nameState)
                    UserSearchBar()
                    UserCategoryList()
                    Text(
                        text = "Món ăn phổ biến",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Danh sách sản phẩm
            items(productList) { product ->
                UserProductCard(
                    product = product,
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}