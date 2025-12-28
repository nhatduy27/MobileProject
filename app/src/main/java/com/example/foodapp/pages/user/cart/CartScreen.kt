package com.example.foodapp.pages.user.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodapp.R
import com.example.foodapp.pages.user.components.UserBottomNav

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val imageRes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    var cartItems by remember {
        mutableStateOf(
            listOf(
                CartItem("1", "Matcha Latte", 20.0, 2, R.drawable.matchalatte),
                CartItem("2", "Classic Pizza", 150.0, 1, R.drawable.data_3),
                CartItem("3", "Chocolate Cake", 45.0, 1, R.drawable.data_3),
                CartItem("4", "Caesar Salad", 60.0, 3, R.drawable.data_3),
            )
        )
    }

    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Giỏ hàng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Column {
                    CartBottomBar(totalPrice = totalPrice)
                    UserBottomNav(navController = navController, onProfileClick = { })
                }
            } else {
                UserBottomNav(navController = navController, onProfileClick = { })
            }
        },
        containerColor = Color.White
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartContent(modifier = Modifier.padding(padding))
        } else {
            CartContent(
                cartItems = cartItems,
                onRemoveItem = { itemId ->
                    cartItems = cartItems.filter { it.id != itemId }
                },
                onQuantityChange = { itemId, newQuantity ->
                    cartItems = cartItems.map { item ->
                        if (item.id == itemId) {
                            item.copy(quantity = newQuantity.coerceAtLeast(1))
                        } else item
                    }
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun CartContent(
    cartItems: List<CartItem>,
    onRemoveItem: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cartItems) { item ->
            CartItemCard(
                item = item,
                onRemove = { onRemoveItem(item.id) },
                onQuantityChange = { newQuantity ->
                    onQuantityChange(item.id, newQuantity)
                }
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageRes != 0) {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("📦", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Item details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        "${String.format("%,d", (item.price * 1000).toLong())}đ",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quantity controls
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("-", fontSize = 16.sp, color = Color(0xFFFF9800))
                    }
                    Text(
                        item.quantity.toString(),
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("+", fontSize = 16.sp, color = Color(0xFFFF9800))
                    }
                }
            }

            // Delete button
            IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CartBottomBar(totalPrice: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tổng cộng:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "${String.format("%,d", (totalPrice * 1000).toLong())}đ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFFF9800)
                )
            }

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Thanh toán", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun EmptyCartContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🛒",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "Giỏ hàng trống",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            "Hãy thêm sản phẩm vào giỏ hàng",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
