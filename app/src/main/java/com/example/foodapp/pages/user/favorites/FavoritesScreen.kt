package com.example.foodapp.pages.user.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodapp.pages.user.components.UserBottomNav
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign
import com.example.foodapp.R

data class FavoriteItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isFavorite: Boolean = true,
    val imageRes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    var favorites by remember {
        mutableStateOf(
            listOf(
                FavoriteItem("1", "Matcha Latte", "Trà xanh Nhật Bản", 20.0, true, R.drawable.matchalatte),
                FavoriteItem("2", "Classic Pizza", "Bánh pizza truyền thống", 150.0, true, R.drawable.data_3),
                FavoriteItem("3", "Chocolate Cake", "Bánh chocolate hảo hạng", 45.0, true, R.drawable.data_3),
                FavoriteItem("4", "Pho Bo", "Phở bò đặc biệt", 60.0, true, R.drawable.data_3),
                FavoriteItem("5", "Burger", "Hamburger thơm ngon", 80.0, true, R.drawable.data_3),
                FavoriteItem("6", "Sushi Set", "Combo sushi tươi ngon", 120.0, true, R.drawable.data_3),
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Yêu thích",
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
            UserBottomNav(navController = navController, onProfileClick = { })
        },
        containerColor = Color.White
    ) { padding ->
        if (favorites.isEmpty()) {
            EmptyFavoritesContent(modifier = Modifier.padding(padding))
        } else {
            FavoritesContent(
                favorites = favorites,
                onRemoveFavorite = { itemId ->
                    favorites = favorites.filter { it.id != itemId }
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun FavoritesContent(
    favorites: List<FavoriteItem>,
    onRemoveFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(favorites) { item ->
            FavoriteItemCard(
                item = item,
                onRemove = { onRemoveFavorite(item.id) }
            )
        }
    }
}

@Composable
private fun FavoriteItemCard(
    item: FavoriteItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with favorite button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Image/Emoji
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
                    Text(
                        "🍜",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Item name
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1
                )

                // Item description
                Text(
                    item.description,
                    fontSize = 11.sp,
                    color = Color(0xFF999999),
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )

                // Price and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${String.format("%,d", (item.price * 1000).toLong())}đ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800)
                    )
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .height(28.dp)
                            .padding(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Thêm", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "❤️",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "Không có mục yêu thích",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            "Hãy thêm những sản phẩm bạn thích",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
