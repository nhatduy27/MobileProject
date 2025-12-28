package com.example.foodapp.presentation.view.user.home


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.foodapp.pages.user.home.HomeViewModel
import com.example.foodapp.pages.user.home.UserNameState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.foodapp.R
import com.example.foodapp.data.model.Product
import com.example.foodapp.data.model.FoodCategory
import com.example.foodapp.pages.user.components.UserBottomNav
import com.example.foodapp.pages.user.components.UserCategoryList
import com.example.foodapp.pages.user.components.UserHeader
import com.example.foodapp.pages.user.components.UserProductCard
import com.example.foodapp.pages.user.components.UserSearchBar

@Composable
fun UserHomeScreen(
    navController: NavHostController,
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit
) {

    val context = LocalContext.current
    //khởi tạo viewModel
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )
    // Quan sát trạng thái từ ViewModel
    val nameState by viewModel.userNameState.observeAsState(UserNameState.Idle)

    // Gọi fetchUserName khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
    }


    val productList = listOf(
        Product(
            name = "Matcha Latte",
            description = "Ngon tuyệt",
            price = "20.000đ",
            priceValue = 20.0,
            imageRes = R.drawable.matchalatte,
            category = FoodCategory.DRINK
        ),
        Product(
            name = "Classic Pizza",
            description = "Nhiều phô mai",
            price = "150.000đ",
            priceValue = 150.0,
            imageRes = R.drawable.data_3,
            category = FoodCategory.FOOD
        ),
        Product(
            name = "Chocolate Cake",
            description = "Bánh ngọt hảo hạng",
            price = "45.000đ",
            priceValue = 45.0,
            imageRes = R.drawable.data_3,
            category = FoodCategory.FOOD
        ),
        Product(
            name = "Caesar Salad",
            description = "Rau tươi ngon",
            price = "60.000đ",
            priceValue = 60.0,
            imageRes = R.drawable.data_3,
            category = FoodCategory.FOOD // SỬA: Đúng category
        ),
        Product(
            name = "Espresso",
            description = "Cà phê đậm đà",
            price = "25.000đ",
            priceValue = 25.0,
            imageRes = R.drawable.data_3,
            category = FoodCategory.DRINK
        ),
        Product(
            name = "Hamburger",
            description = "Thịt bò tươi",
            price = "80.000đ",
            priceValue = 80.0,
            imageRes = R.drawable.data_3,
            category = FoodCategory.FOOD
        ),
        Product(
            name = "Ice Cream Sundae",
            description = "Kem mát lạnh",
            price = "35.000đ",
            priceValue = 35.0,
            imageRes = R.drawable.data_3,
            category = FoodCategory.FOOD
        )
    )
    // Gọi hàm Content để hiển thị giao diện
    UserHomeContent(
        navController = navController,
        nameState = nameState,
        productList = productList,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick
    )
}

@Composable
fun UserHomeContent(
    navController: NavHostController,
    nameState: UserNameState,
    productList: List<Product>,
    onProductClick: (Product) -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            UserBottomNav(
                navController = navController,
                onProfileClick = onProfileClick
            )
        }
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