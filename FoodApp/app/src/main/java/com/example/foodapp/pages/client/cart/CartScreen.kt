package com.example.foodapp.pages.client.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.data.model.shared.product.FoodCategory
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.components.home.UserBottomNav
import com.example.foodapp.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onCheckoutShop: (List<Product>, List<Int>, String, String) -> Unit = { _, _, _, _ -> }
) {

    val viewModel: CartViewModel = viewModel(
        factory = CartViewModel.factory(LocalContext.current)
    )

    // State observables
    val cartState by viewModel.cartState.observeAsState(CartState.Idle)
    val shopGroups by viewModel.shopGroups.observeAsState(emptyList())
    val filteredShopGroups by viewModel.filteredShopGroups.observeAsState(emptyList())
    val filteredTotalAmount by viewModel.filteredTotalAmount.observeAsState(0.0)
    val filteredFormattedTotalAmount by viewModel.filteredFormattedTotalAmount.observeAsState("0đ")
    val filteredTotalShippingFee by viewModel.filteredTotalShippingFee.observeAsState(0.0)
    val filteredGrandTotal by viewModel.filteredGrandTotal.observeAsState(0.0)
    val clearCartState by viewModel.clearCartState.observeAsState(ClearCartState.Idle)
    val showClearCartDialog by viewModel.showClearCartDialog.observeAsState(false)
    val removeItemState by viewModel.removeItemState.observeAsState(RemoveItemState.Idle)
    val removingItemId by viewModel.removingItemId.observeAsState(null)
    val updateQuantityState by viewModel.updateQuantityState.observeAsState(UpdateQuantityState.Idle)
    val updatingItemId by viewModel.updatingItemId.observeAsState(null)
    val pendingQuantityChanges by viewModel.pendingQuantityChanges.observeAsState(mapOf())

    // Lấy danh sách shop options và shop filter hiện tại
    val shopFilterOptions by viewModel.shopFilterOptions.observeAsState(emptyList())
    val selectedShopFilter by viewModel.selectedShopFilter.observeAsState(ShopFilterOption())

    // Load cart khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    // State cho dropdown
    var filterDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Giỏ hàng",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            // Hiển thị thông tin lọc nếu có
                            val totalItems = filteredShopGroups.sumOf { it.totalItems }
                            if (totalItems > 0) {
                                val filterInfo = if (selectedShopFilter.id.isNotEmpty()) {
                                    "Đang xem: ${selectedShopFilter.name} • $totalItems sản phẩm"
                                } else {
                                    "${filteredShopGroups.size} shop • $totalItems sản phẩm"
                                }
                                Text(
                                    filterInfo,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Nút Filter chỉ hiện khi có nhiều cửa hàng
                        if (shopGroups.size > 1) {
                            // Box để đặt dropdown
                            Box {
                                IconButton(
                                    onClick = { filterDropdownExpanded = true },
                                    enabled = cartState !is CartState.Loading
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Lọc cửa hàng",
                                        tint = if (selectedShopFilter.id.isNotEmpty()) Color(0xFFFFD700) else Color.White
                                    )
                                }

                                // Dropdown menu - ĐẶT Ở ĐÂY, bên ngoài Scaffold content
                                if (filterDropdownExpanded && shopFilterOptions.isNotEmpty()) {
                                    DropdownMenu(
                                        expanded = filterDropdownExpanded,
                                        onDismissRequest = { filterDropdownExpanded = false },
                                        modifier = Modifier
                                            .width(280.dp)
                                            .heightIn(max = 400.dp)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        ) {
                                            // Header của dropdown
                                            Text(
                                                text = "Lọc theo cửa hàng",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFFFF9800)
                                            )

                                            Divider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                color = Color.LightGray.copy(alpha = 0.5f)
                                            )

                                            shopFilterOptions.forEach { shopOption ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = shopOption.name,
                                                                fontWeight = if (shopOption.id == selectedShopFilter.id) FontWeight.Bold else FontWeight.Normal,
                                                                fontSize = 14.sp,
                                                                color = if (shopOption.id == selectedShopFilter.id) Color(0xFFFF9800) else Color.Black
                                                            )
                                                            if (shopOption.id.isNotEmpty()) {
                                                                Text(
                                                                    text = "${shopOption.itemCount} sản phẩm • ${shopOption.totalItems} cái",
                                                                    fontSize = 12.sp,
                                                                    color = Color.Gray
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        viewModel.setShopFilter(shopOption)
                                                        filterDropdownExpanded = false
                                                    },
                                                    trailingIcon = {
                                                        if (shopOption.id == selectedShopFilter.id) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Đã chọn",
                                                                tint = Color(0xFFFF9800),
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                if (shopOption.id.isNotEmpty() && shopOption != shopFilterOptions.last()) {
                                                    Divider(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        color = Color.LightGray.copy(alpha = 0.3f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Nút xóa toàn bộ giỏ hàng
                        if (filteredShopGroups.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.showClearCartDialog() },
                                enabled = clearCartState !is ClearCartState.Loading
                            ) {
                                if (clearCartState is ClearCartState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = "Xóa toàn bộ",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Hiển thị nút lưu khi có thay đổi số lượng
                        if (pendingQuantityChanges.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.saveAllQuantityChanges() },
                                enabled = updateQuantityState !is UpdateQuantityState.Loading
                            ) {
                                if (updateQuantityState is UpdateQuantityState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = "Lưu thay đổi",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF9800),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                if (filteredShopGroups.isNotEmpty()) {
                    Column {
                        CartBottomBar(
                            totalPrice = filteredGrandTotal,
                            formattedTotalPrice = filteredFormattedTotalAmount,
                            totalShippingFee = filteredTotalShippingFee,
                            subtotal = filteredTotalAmount,
                            onCheckout = {
                                handleCheckoutAll(filteredShopGroups, onCheckoutShop)
                            }
                        )
                        UserBottomNav(navController = navController, onProfileClick = { })
                    }
                } else {
                    UserBottomNav(navController = navController, onProfileClick = { })
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Hiển thị dialog xác nhận xóa toàn bộ
                if (showClearCartDialog) {
                    ClearCartConfirmationDialog(
                        onConfirm = { viewModel.clearCart() },
                        onDismiss = { viewModel.hideClearCartDialog() }
                    )
                }

                // Nội dung chính cart
                when (cartState) {
                    is CartState.Loading -> {
                        LoadingCartContent()
                    }
                    is CartState.Empty -> {
                        EmptyCartContent()
                    }
                    is CartState.Error -> {
                        ErrorCartContent(
                            errorMessage = (cartState as CartState.Error).message,
                            onRetry = { viewModel.loadCart() }
                        )
                    }
                    is CartState.Success -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Thông tin lọc hiện tại
                            CurrentFilterInfo(
                                selectedShopFilter = selectedShopFilter,
                                onClearFilter = {
                                    viewModel.setShopFilter(ShopFilterOption())
                                }
                            )

                            // Hiển thị sản phẩm theo từng shop
                            ShopGroupsContent(
                                shopGroups = filteredShopGroups,
                                pendingQuantityChanges = pendingQuantityChanges,
                                onRemoveItem = { itemId ->
                                    viewModel.removeItem(itemId)
                                },
                                onQuantityChange = { itemId, newQuantity ->
                                    viewModel.setPendingQuantityChange(itemId, newQuantity)
                                },
                                onSaveQuantityChange = { itemId ->
                                    viewModel.saveQuantityChange(itemId)
                                },
                                onDeleteShop = { shopGroup ->
                                    viewModel.showDeleteShopDialog(shopGroup)
                                },
                                onCheckoutShop = { shopGroup ->
                                    handleCheckoutShop(shopGroup, onCheckoutShop)
                                },
                                removingItemId = removingItemId,
                                updatingItemId = updatingItemId
                            )
                        }
                    }
                    CartState.Idle -> {
                        LoadingCartContent()
                    }
                }
            }
        }

        // Hiển thị dialog xóa shop
        val showDeleteShopDialog by viewModel.showDeleteShopDialog.observeAsState(false)
        val selectedShopForDelete by viewModel.selectedShopForDelete.observeAsState(null)
        val deleteShopState by viewModel.deleteShopState.observeAsState(DeleteShopState.Idle)

        if (showDeleteShopDialog && selectedShopForDelete != null) {
            DeleteShopConfirmationDialog(
                shopGroup = selectedShopForDelete!!,
                deleteShopState = deleteShopState,
                onConfirm = { viewModel.deleteShop() },
                onDismiss = { viewModel.hideDeleteShopDialog() }
            )
        }
    }
}

private fun handleCheckoutShop(
    shopGroup: ShopGroup,
    onCheckoutShop: (List<Product>, List<Int>, String, String) -> Unit
) {
    // DEBUG: Log thông tin CartItem
    println("🔴 DEBUG CartScreen - handleCheckoutShop")
    println("🔴 Shop: ${shopGroup.shopName}")
    println("🔴 Items count: ${shopGroup.items.size}")

    val products = shopGroup.items.map { cartItem ->
        // DEBUG từng CartItem
        println("🔴 CartItem: ${cartItem.name}")
        println("🔴   - id: ${cartItem.id}")
        println("🔴   - imageUrl: '${cartItem.imageUrl}'")
        println("🔴   - imageUrl is null? ${cartItem.imageUrl == null}")
        println("🔴   - imageUrl is empty? ${cartItem.imageUrl.isNullOrEmpty()}")

        // Tạo Product với fallback nếu imageUrl null
        Product(
            id = cartItem.id,
            name = cartItem.name,
            description = cartItem.name,
            price = cartItem.formattedPrice,
            priceValue = cartItem.price,
            category = FoodCategory.FOOD,
            imageUrl = cartItem.imageUrl ?: run {
                println("⚠️ WARNING: imageUrl is null for ${cartItem.name}, using placeholder")
                "https://via.placeholder.com/150" // Fallback URL
            },
            shopId = shopGroup.shopId,
            shopName = shopGroup.shopName
        )
    }

    // Debug Product đã tạo
    products.forEachIndexed { index, product ->
        println("🟢 Product $index: ${product.name}")
        println("🟢   - imageUrl: '${product.imageUrl}'")
    }

    val quantities = shopGroup.items.map { it.quantity }
    onCheckoutShop(products, quantities, shopGroup.shopId, shopGroup.shopName)
}

private fun handleCheckoutAll(
    shopGroups: List<ShopGroup>,
    onCheckoutShop: (List<Product>, List<Int>, String, String) -> Unit
) {
    if (shopGroups.size == 1) {
        handleCheckoutShop(shopGroups.first(), onCheckoutShop)
    } else {
        val allProducts = mutableListOf<Product>()
        val allQuantities = mutableListOf<Int>()

        shopGroups.forEach { shopGroup ->
            shopGroup.items.forEach { cartItem ->
                allProducts.add(
                    Product(
                        id = cartItem.id,
                        name = cartItem.name,
                        description = cartItem.name,
                        price = cartItem.formattedPrice,
                        priceValue = cartItem.price,
                        category = FoodCategory.FOOD,
                        imageUrl = cartItem.imageUrl ?: "",
                        shopId = shopGroup.shopId,
                        shopName = shopGroup.shopName
                    )
                )
                allQuantities.add(cartItem.quantity)
            }
        }
        onCheckoutShop(allProducts, allQuantities, "", "Tất cả cửa hàng")
    }
}

@Composable
private fun ShopGroupsContent(
    shopGroups: List<ShopGroup>,
    pendingQuantityChanges: Map<String, Int>,
    onRemoveItem: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onSaveQuantityChange: (String) -> Unit,
    onDeleteShop: (ShopGroup) -> Unit,
    onCheckoutShop: (ShopGroup) -> Unit,
    removingItemId: String?,
    updatingItemId: String?
) {
    if (shopGroups.isEmpty()) {
        EmptyCartContent()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shopGroups, key = { it.shopId }) { shopGroup ->
                ShopGroupSection(
                    shopGroup = shopGroup,
                    pendingQuantityChanges = pendingQuantityChanges,
                    onRemoveItem = onRemoveItem,
                    onQuantityChange = onQuantityChange,
                    onSaveQuantityChange = onSaveQuantityChange,
                    onDeleteShop = onDeleteShop,
                    onCheckoutShop = onCheckoutShop,
                    removingItemId = removingItemId,
                    updatingItemId = updatingItemId
                )
            }
        }
    }
}

@Composable
private fun ShopGroupSection(
    shopGroup: ShopGroup,
    pendingQuantityChanges: Map<String, Int>,
    onRemoveItem: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onSaveQuantityChange: (String) -> Unit,
    onDeleteShop: (ShopGroup) -> Unit,
    onCheckoutShop: (ShopGroup) -> Unit,
    removingItemId: String?,
    updatingItemId: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            ShopHeader(
                shopGroup = shopGroup,
                onDeleteShop = onDeleteShop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                shopGroup.items.forEach { item ->
                    ShopItemCard(
                        item = item,
                        pendingQuantity = pendingQuantityChanges[item.id],
                        onRemove = { onRemoveItem(item.id) },
                        onQuantityChange = { newQuantity ->
                            onQuantityChange(item.id, newQuantity)
                        },
                        onSave = { onSaveQuantityChange(item.id) },
                        isRemoving = item.id == removingItemId,
                        isUpdating = item.id == updatingItemId
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ShopFooterWithCheckout(
                shopGroup = shopGroup,
                onCheckout = { onCheckoutShop(shopGroup) }
            )
        }
    }
}

@Composable
private fun ShopFooterWithCheckout(
    shopGroup: ShopGroup,
    onCheckout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Phí vận chuyển:",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = CurrencyUtils.formatCurrency(shopGroup.shipFee),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tổng shop:",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = CurrencyUtils.formatCurrency(shopGroup.subtotal + shopGroup.shipFee),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "Thanh toán shop này",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ShopHeader(
    shopGroup: ShopGroup,
    onDeleteShop: (ShopGroup) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = "Shop",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = shopGroup.shopName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${shopGroup.itemCount} sản phẩm • ${shopGroup.totalItems} cái",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Trạng thái shop
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (shopGroup.isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (shopGroup.isOpen) "🟢 Mở cửa" else "🔴 Đóng cửa",
                            fontSize = 10.sp,
                            color = if (shopGroup.isOpen) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = { onDeleteShop(shopGroup) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Xóa shop",
                tint = Color(0xFFF44336),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    item: CartItemUi,
    pendingQuantity: Int?,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onSave: () -> Unit,
    isRemoving: Boolean = false,
    isUpdating: Boolean = false
) {
    val currentQuantity = pendingQuantity ?: item.quantity
    val hasPendingChanges = pendingQuantity != null && pendingQuantity != item.quantity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPendingChanges) Color(0xFFF3F3F3) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hình ảnh sản phẩm
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("📦", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin sản phẩm và số lượng
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.formattedPrice,
                    fontSize = 13.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuantitySelector(
                    currentQuantity = currentQuantity,
                    originalQuantity = item.quantity,
                    hasPendingChanges = hasPendingChanges,
                    isUpdating = isUpdating,
                    onQuantityChange = onQuantityChange,
                    onSave = onSave
                )
            }

            // Giá và nút xóa
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatCurrency(item.price * currentQuantity),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (hasPendingChanges) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                    if (hasPendingChanges) {
                        Text(
                            text = CurrencyUtils.formatCurrency(item.subtotal),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp),
                    enabled = !isRemoving && !isUpdating
                ) {
                    if (isRemoving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.Red
                        )
                    } else {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Xóa",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    currentQuantity: Int,
    originalQuantity: Int,
    hasPendingChanges: Boolean,
    isUpdating: Boolean,
    onQuantityChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút giảm
        IconButton(
            onClick = { onQuantityChange(currentQuantity - 1) },
            modifier = Modifier.size(28.dp),
            enabled = !isUpdating && currentQuantity > 1
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFFFF9800), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Hiển thị số lượng
        if (isUpdating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFFFF9800)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentQuantity.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (hasPendingChanges) {
                        Text(
                            text = "($originalQuantity)",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Nút tăng
        IconButton(
            onClick = { onQuantityChange(currentQuantity + 1) },
            modifier = Modifier.size(28.dp),
            enabled = !isUpdating && currentQuantity < 999
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFFFF9800), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Nút lưu thay đổi (chỉ hiện khi có thay đổi)
        if (hasPendingChanges) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSave,
                modifier = Modifier.size(28.dp),
                enabled = !isUpdating
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Lưu",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteShopConfirmationDialog(
    shopGroup: ShopGroup,
    deleteShopState: DeleteShopState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (deleteShopState is DeleteShopState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 3.dp,
                        color = Color(0xFFFF9800)
                    )
                } else {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa shop",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFF44336)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (deleteShopState is DeleteShopState.Loading)
                        "Đang xóa..."
                    else
                        "Xóa sản phẩm của shop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    shopGroup.shopName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFFFF9800),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (deleteShopState !is DeleteShopState.Loading) {
                    Text(
                        "Bạn có chắc chắn muốn xóa ${shopGroup.itemCount} sản phẩm của shop này?",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Số sản phẩm:",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${shopGroup.itemCount} sản phẩm",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng số lượng:",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${shopGroup.totalItems} cái",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng tiền:",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = CurrencyUtils.formatCurrency(shopGroup.subtotal),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (deleteShopState !is DeleteShopState.Loading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Gray
                            )
                        ) {
                            Text("Hủy")
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            )
                        ) {
                            Text("Xóa shop")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentFilterInfo(
    selectedShopFilter: ShopFilterOption,
    onClearFilter: () -> Unit
) {
    if (selectedShopFilter.id.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Đang xem: ${selectedShopFilter.name}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "${selectedShopFilter.itemCount} sản phẩm • ${selectedShopFilter.totalItems} cái",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }

                TextButton(
                    onClick = onClearFilter,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Xóa lọc",
                        color = Color(0xFFF44336),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ClearCartConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Xóa toàn bộ",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Xóa toàn bộ giỏ hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Bạn có chắc chắn muốn xóa toàn bộ sản phẩm trong giỏ hàng?",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) {
                        Text("Xóa")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartBottomBar(
    totalPrice: Double,
    formattedTotalPrice: String,
    totalShippingFee: Double,
    subtotal: Double,
    onCheckout: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Phí vận chuyển:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    CurrencyUtils.formatCurrency(totalShippingFee),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tạm tính:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    CurrencyUtils.formatCurrency(subtotal),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray
            )

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
                    formattedTotalPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFFF9800)
                )
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Thanh toán tất cả",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyCartContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🛒", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Giỏ hàng trống",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Gray
        )
        Text(
            "Hãy thêm sản phẩm để tiếp tục mua sắm",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LoadingCartContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFFF9800))
    }
}

@Composable
private fun ErrorCartContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            errorMessage,
            textAlign = TextAlign.Center,
            color = Color.Red,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Text("Thử lại")
        }
    }
}