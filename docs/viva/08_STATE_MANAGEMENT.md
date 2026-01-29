# 08. State Management

> **Mục đích**: Giải thích cách quản lý state trong Android app với MVVM + StateFlow.  
> **Dành cho**: Bảo vệ đồ án – trả lời câu hỏi về state management, UI events, error handling.

---

## 1. TỔNG QUAN ARCHITECTURE

### 1.1 State Flow trong App

```
┌─────────────────────────────────────────────────────────────────┐
│                    STATE MANAGEMENT FLOW                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐ │
│   │   User   │───▶│   UI     │───▶│ ViewModel│───▶│Repository│ │
│   │  Action  │    │  Event   │    │  Update  │    │  /API    │ │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘ │
│                                                         │        │
│                                                         ▼        │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐ │
│   │   UI     │◀───│ Compose  │◀───│StateFlow │◀───│  Result  │ │
│   │ Display  │    │Recompose │    │  Emit    │    │  Return  │ │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 State Management Tools trong Project

| Tool | Mục đích | File sử dụng |
|------|----------|--------------|
| `StateFlow` | UI state chính | Tất cả ViewModels |
| `MutableStateFlow` | Internal state (private) | ViewModels |
| `collectAsState()` | Consume state trong Compose | Screens |
| `Sealed class` | Type-safe UI states | *State.kt files |
| `viewModelScope` | Coroutine scope | ViewModels |

---

## 2. VIEWMODEL STATE PATTERN

### 2.1 Basic Pattern

```kotlin
// PATTERN: MutableStateFlow (private) + StateFlow (public)
class LoginViewModel : ViewModel() {
    // Private: Chỉ ViewModel có thể modify
    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    
    // Public: UI chỉ có thể observe, không modify được
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()
    
    fun logIn() {
        viewModelScope.launch {
            _logInState.value = LogInState.Loading
            
            val result = authRepository.login(email, password)
            
            _logInState.value = when {
                result.isSuccess -> LogInState.Success(result.data)
                else -> LogInState.Error(result.message)
            }
        }
    }
}
```

### 2.2 Thực tế trong Project (LoginViewModel.kt)

```kotlin
// LoginViewModel.kt - Lines 40-80
class LoginViewModel(
    private val authApiClient: AuthApiClient,
    private val userApiClient: UserApiClient,
    private val authManager: AuthManager
) : ViewModel() {

    // Multiple states for different concerns
    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()

    private val _googleLogInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val googleLogInState: StateFlow<LogInState> = _googleLogInState.asStateFlow()

    private val _existAccountState = MutableStateFlow<ExistAccountState>(ExistAccountState.Idle)
    val existAccountState: StateFlow<ExistAccountState> = _existAccountState.asStateFlow()

    // Form input states
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Setters for form inputs
    fun setEmail(value: String) { _email.value = value }
    fun setPassword(value: String) { _password.value = value }
}
```

### 2.3 State Update Methods

```kotlin
// Method 1: Direct assignment
_logInState.value = LogInState.Loading

// Method 2: update{} - atomic update with current value
_uiState.update { currentState ->
    currentState.copy(isLoading = true, error = null)
}

// Method 3: emit() - trong suspend function
_logInState.emit(LogInState.Success(data))
```

---

## 3. SEALED CLASSES FOR UI STATES

### 3.1 Pattern

```kotlin
// Định nghĩa tất cả states có thể xảy ra
sealed class LogInState {
    object Idle : LogInState()
    object Loading : LogInState()
    data class Success(val user: User) : LogInState()
    data class Error(val message: String) : LogInState()
}
```

### 3.2 Thực tế trong Project

```kotlin
// LoginViewModel.kt - Sealed class
sealed class LogInState {
    object Idle : LogInState()
    object Loading : LogInState()
    data class Success(val message: String) : LogInState()
    data class Error(val message: String) : LogInState()
}

// ChatViewModel.kt - Sealed class
sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Success(val data: Any) : ChatState()
    data class Error(val message: String) : ChatState()
}

// GpsViewModel.kt - Data class với multiple fields
data class GpsUiState(
    val isLoading: Boolean = false,
    val currentTrip: Trip? = null,
    val acceptedOrders: List<Order> = emptyList(),
    val availableOrders: List<Order> = emptyList(),
    val error: String? = null
)
```

### 3.3 Khi nào dùng Sealed Class vs Data Class?

| Use Case | Recommendation | Ví dụ |
|----------|----------------|-------|
| Discrete states | Sealed class | Login: Idle, Loading, Success, Error |
| Multiple related fields | Data class | GpsUiState với nhiều fields |
| Exhaustive handling | Sealed class | Khi cần `when` check tất cả cases |
| Simple loading/error | Data class | `isLoading: Boolean, error: String?` |

---

## 4. UI CONSUMPTION

### 4.1 collectAsState() Pattern

```kotlin
// LoginScreen.kt
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    // Collect StateFlow as Compose State
    val logInState by viewModel.logInState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    // UI recomposes automatically when state changes
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.setEmail(it) }
        )

        Button(
            onClick = { viewModel.logIn() },
            enabled = logInState !is LogInState.Loading
        ) {
            when (logInState) {
                is LogInState.Loading -> CircularProgressIndicator()
                else -> Text("Login")
            }
        }
    }
}
```

### 4.2 Handling State Changes với LaunchedEffect

```kotlin
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    val logInState by viewModel.logInState.collectAsState()

    // Side effects khi state thay đổi
    LaunchedEffect(logInState) {
        when (logInState) {
            is LogInState.Success -> {
                // Navigate to home
                navController.navigate(Screen.UserHome.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is LogInState.Error -> {
                // Show snackbar or toast
            }
            else -> {}
        }
    }

    // UI content...
}
```

### 4.3 Multiple State Sources

```kotlin
@Composable
fun ShopDetailScreen(viewModel: ShopDetailViewModel) {
    // Collect multiple states
    val shopState by viewModel.shopState.collectAsState()
    val productsState by viewModel.productsState.collectAsState()
    val cartState by viewModel.cartState.collectAsState()

    // Handle based on combined states
    when {
        shopState is ShopState.Loading || productsState is ProductsState.Loading -> {
            LoadingScreen()
        }
        shopState is ShopState.Error -> {
            ErrorScreen((shopState as ShopState.Error).message)
        }
        else -> {
            // Normal content
            ShopContent(
                shop = (shopState as ShopState.Success).shop,
                products = (productsState as ProductsState.Success).products,
                cartCount = (cartState as CartState.Success).count
            )
        }
    }
}
```

---

## 5. UI EVENTS VS UI STATE

### 5.1 Phân biệt

| Aspect | UI State | UI Event |
|--------|----------|----------|
| **Lifetime** | Persistent | One-time |
| **Example** | Loading, data list | Navigation, Toast, Snackbar |
| **Storage** | StateFlow | Channel/SharedFlow hoặc callback |
| **Consumption** | collectAsState() | LaunchedEffect |

### 5.2 Pattern cho One-time Events

```kotlin
// ViewModel
class LoginViewModel : ViewModel() {
    // State: persistent
    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()

    // Event: one-time (optional pattern)
    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome: SharedFlow<Unit> = _navigateToHome.asSharedFlow()

    fun logIn() {
        viewModelScope.launch {
            _logInState.value = LogInState.Loading
            
            val result = authRepository.login(email, password)
            
            if (result.isSuccess) {
                _logInState.value = LogInState.Success(result.data)
                _navigateToHome.emit(Unit)  // One-time event
            } else {
                _logInState.value = LogInState.Error(result.message)
            }
        }
    }
}

// UI
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    // Consume one-time event
    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect {
            navController.navigate(Screen.UserHome.route)
        }
    }
}
```

### 5.3 Thực tế trong Project

Project này dùng **State-based navigation** thay vì Event:

```kotlin
// Pattern trong project: Check state trong LaunchedEffect
LaunchedEffect(logInState) {
    if (logInState is LogInState.Success) {
        navController.navigate(Screen.UserHome.route)
    }
}
```

---

## 6. NAVIGATION STATE

### 6.1 NavController trong Compose

```kotlin
// NavGraph.kt - Navigation setup
@Composable
fun FoodAppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Intro.route
    ) {
        composable(Screen.Intro.route) { IntroScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.UserHome.route) { UserHomeScreen(navController) }
    }
}
```

### 6.2 Navigation Patterns

```kotlin
// 1. Simple navigation
navController.navigate(Screen.UserHome.route)

// 2. Navigate với arguments
navController.navigate(Screen.ShopDetail.createRoute("shop_123"))

// 3. Pop back stack
navController.popBackStack()

// 4. Navigate và clear back stack (login → home)
navController.navigate(Screen.UserHome.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}

// 5. Navigate to specific destination trong back stack
navController.navigate(Screen.UserHome.route) {
    popUpTo(Screen.UserHome.route) {
        inclusive = false
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

### 6.3 Deep Linking và Arguments

```kotlin
// Định nghĩa route với argument
sealed class Screen(val route: String) {
    object ShopDetail : Screen("shop_detail/{shopId}") {
        fun createRoute(shopId: String) = "shop_detail/$shopId"
    }
}

// NavHost setup
composable(
    route = Screen.ShopDetail.route,
    arguments = listOf(
        navArgument("shopId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
    ShopDetailScreen(navController, shopId)
}
```

---

## 7. ERROR/LOADING HANDLING

### 7.1 Pattern 1: Sealed Class

```kotlin
sealed class ShopDetailState {
    object Loading : ShopDetailState()
    data class Success(val shop: Shop) : ShopDetailState()
    data class Error(val message: String) : ShopDetailState()
}

@Composable
fun ShopDetailScreen(viewModel: ShopDetailViewModel) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is ShopDetailState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ShopDetailState.Success -> {
            val shop = (state as ShopDetailState.Success).shop
            ShopContent(shop)
        }
        is ShopDetailState.Error -> {
            val message = (state as ShopDetailState.Error).message
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.retry() }) {
                    Text("Retry")
                }
            }
        }
    }
}
```

### 7.2 Pattern 2: Data Class với flags

```kotlin
data class GpsUiState(
    val isLoading: Boolean = false,
    val currentTrip: Trip? = null,
    val orders: List<Order> = emptyList(),
    val error: String? = null
)

@Composable
fun GpsScreen(viewModel: GpsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        // Main content
        if (state.currentTrip != null) {
            TripContent(state.currentTrip, state.orders)
        } else {
            EmptyState()
        }

        // Loading overlay
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error snackbar
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}
```

### 7.3 ViewModel Error Handling

```kotlin
class GpsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GpsUiState())
    val uiState: StateFlow<GpsUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val orders = gpsApiClient.getAvailableOrders()
                _uiState.update { it.copy(
                    isLoading = false,
                    orders = orders
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
```

---

## 8. DEPENDENCY INJECTION FOR VIEWMODELS

### 8.1 Manual Factory Pattern (Project này dùng)

```kotlin
// LoginViewModelFactory.kt
class LoginViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authApiClient = AuthApiClient(context)
        val userApiClient = UserApiClient(context)
        val authManager = AuthManager(context)

        return LoginViewModel(
            authApiClient = authApiClient,
            userApiClient = userApiClient,
            authManager = authManager
        ) as T
    }
}

// Usage trong Composable
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context)
    )
    // ...
}
```

### 8.2 Tại sao không dùng Hilt/Koin?

| Reason | Explanation |
|--------|-------------|
| Learning focus | Hiểu rõ manual DI trước khi dùng framework |
| Less dependencies | Giảm complexity cho project học |
| Explicit | Dễ thấy dependencies của mỗi ViewModel |

---

## 9. COMPOSE LIFECYCLE & EFFECTS

### 9.1 Effect Handlers

```kotlin
@Composable
fun MyScreen() {
    // 1. LaunchedEffect: Chạy suspend function khi key thay đổi
    LaunchedEffect(key1) {
        // Runs when key1 changes
        // Use for: API calls, one-time events
    }

    // 2. DisposableEffect: Cleanup khi composable leaves composition
    DisposableEffect(key1) {
        // Setup
        onDispose {
            // Cleanup
        }
    }

    // 3. SideEffect: Chạy sau mỗi successful recomposition
    SideEffect {
        // Update non-Compose state
    }
}
```

### 9.2 Thực tế trong Project

```kotlin
// Load data khi screen appear
@Composable
fun ShopDetailScreen(shopId: String, viewModel: ShopDetailViewModel) {
    // Load shop khi shopId thay đổi
    LaunchedEffect(shopId) {
        viewModel.loadShop(shopId)
    }

    // ...
}

// Handle state changes
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    val logInState by viewModel.logInState.collectAsState()

    // Navigate khi login thành công
    LaunchedEffect(logInState) {
        if (logInState is LogInState.Success) {
            navController.navigate(Screen.UserHome.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
}
```

---

## 10. BEST PRACTICES SUMMARY

### 10.1 State Management Rules

| Rule | Why |
|------|-----|
| **Private MutableStateFlow** | Prevent external modification |
| **Public StateFlow** | Read-only for UI |
| **Single source of truth** | ViewModel owns state |
| **Immutable state objects** | Prevent accidental mutations |

### 10.2 Common Mistakes

| Mistake | Correct Approach |
|---------|------------------|
| Modify state từ UI | Gọi ViewModel methods |
| State trong Composable | Dùng remember hoặc ViewModel |
| Business logic trong UI | Đưa vào ViewModel |
| Observe trong ViewModel | Chỉ observe trong UI layer |

---

## 11. VẤN ĐÁP THƯỜNG GẶP

**Q: StateFlow khác gì LiveData?**  
A: StateFlow là Kotlin Flow-based (hot stream), type-safe hơn, không cần Lifecycle owner. LiveData là Android-specific, auto-pause khi không active.

**Q: Khi nào dùng `remember` vs StateFlow?**  
A: `remember` cho UI-local state (toggle, text input tạm). StateFlow cho business state cần survive configuration change.

**Q: collectAsState() hoạt động như nào?**  
A: Subscribes to StateFlow, returns Compose State. Khi StateFlow emit value mới → Compose State update → UI recompose.

**Q: Tại sao cần sealed class cho state?**  
A: Exhaustive checking - compiler đảm bảo xử lý tất cả cases trong `when`. Type-safe hơn so với string/enum.

**Q: viewModelScope là gì?**  
A: CoroutineScope gắn với ViewModel lifecycle. Auto-cancel khi ViewModel cleared.

**Q: Làm sao tránh recomposition không cần thiết?**  
A: Dùng `remember`, `derivedStateOf`, `key()`, pass stable/immutable objects.
