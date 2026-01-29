# 07. Jetpack Compose vs XML

> **Mục đích**: Giải thích sự khác biệt giữa Declarative UI (Compose) và Imperative UI (XML).  
> **Dành cho**: Bảo vệ đồ án – trả lời câu hỏi về lý do chọn Compose.

---

## 1. TỔNG QUAN

### 1.1 Project này dùng gì?

| Aspect | This Project |
|--------|--------------|
| **UI Framework** | 100% Jetpack Compose |
| **XML Layouts** | ❌ Không sử dụng |
| **Navigation** | Compose Navigation (NavHost) |
| **State Management** | StateFlow + collectAsState() |

### 1.2 So sánh nhanh

| Đặc điểm | XML (Imperative) | Compose (Declarative) |
|----------|------------------|----------------------|
| **Cách viết** | XML + Kotlin riêng | Thuần Kotlin |
| **Update UI** | findViewById → setText() | State thay đổi → UI tự rebuild |
| **Lifecycle** | Phức tạp (View lifecycle) | Đơn giản (Composable lifecycle) |
| **Preview** | Limited | @Preview annotation |
| **Lines of code** | Nhiều hơn | Ít hơn 30-50% |
| **Learning curve** | Thấp (quen thuộc) | Cao (paradigm mới) |

---

## 2. IMPERATIVE UI (XML) - Cách cũ

### 2.1 Mô hình hoạt động

```
┌──────────────────────────────────────────────────────────────────┐
│                    IMPERATIVE UI FLOW                            │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│   │  XML Layout │───▶│  Activity/  │───▶│   Update    │         │
│   │   Define    │    │  Fragment   │    │   Views     │         │
│   │   Views     │    │  Inflate    │    │  Manually   │         │
│   └─────────────┘    └─────────────┘    └─────────────┘         │
│                             │                  │                  │
│                             ▼                  ▼                  │
│                      ┌─────────────────────────────┐             │
│                      │   findViewById<TextView>()  │             │
│                      │   textView.text = "Hello"   │             │
│                      │   button.setOnClickListener │             │
│                      └─────────────────────────────┘             │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Ví dụ code XML

```xml
<!-- activity_login.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <EditText
        android:id="@+id/etEmail"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email" />

    <EditText
        android:id="@+id/etPassword"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword" />

    <Button
        android:id="@+id/btnLogin"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:visibility="gone" />
</LinearLayout>
```

```kotlin
// LoginActivity.kt (Imperative)
class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Find views by ID
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // 2. Set click listener
        btnLogin.setOnClickListener {
            login()
        }

        // 3. Observe ViewModel
        viewModel.loginState.observe(this) { state ->
            // 4. MANUALLY update each view
            when (state) {
                is Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                }
                is Success -> {
                    progressBar.visibility = View.GONE
                    navigateToHome()
                }
                is Error -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    showError(state.message)
                }
            }
        }
    }
}
```

### 2.3 Vấn đề của XML/Imperative

| Vấn đề | Giải thích |
|--------|------------|
| **Boilerplate nhiều** | findViewById, ViewBinding, DataBinding |
| **State không nhất quán** | Phải tự đồng bộ state với UI |
| **Khó maintain** | XML và Kotlin ở 2 file khác nhau |
| **Null safety** | findViewById có thể trả null |
| **Deep nesting** | XML layouts dễ bị nest sâu |

---

## 3. DECLARATIVE UI (Compose) - Cách mới

### 3.1 Mô hình hoạt động

```
┌──────────────────────────────────────────────────────────────────┐
│                    DECLARATIVE UI FLOW                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│   │   State     │───▶│ Composable  │───▶│    UI       │         │
│   │  Changes    │    │  Function   │    │  Recompose  │         │
│   └─────────────┘    └─────────────┘    └─────────────┘         │
│                             │                                     │
│                             ▼                                     │
│                      ┌─────────────────────────────┐             │
│                      │   UI = f(State)             │             │
│                      │   State thay đổi → UI rebuild│             │
│                      │   (Automatic, no manual)    │             │
│                      └─────────────────────────────┘             │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### 3.2 Ví dụ code Compose (Thực tế trong project)

```kotlin
// LoginScreen.kt - Actual code from project
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(context))
) {
    // 1. Collect state from ViewModel
    val logInState by viewModel.logInState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 2. Email input - State tự đồng bộ
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.setEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Password input
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.setPassword(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Login button - UI tự thay đổi theo state
        Button(
            onClick = { viewModel.logIn() },
            enabled = logInState !is LogInState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (logInState is LogInState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Login")
            }
        }

        // 5. Handle state changes declaratively
        LaunchedEffect(logInState) {
            when (logInState) {
                is LogInState.Success -> navController.navigate(Screen.UserHome.route)
                is LogInState.Error -> { /* Show snackbar */ }
                else -> {}
            }
        }
    }
}
```

### 3.3 Ưu điểm của Compose

| Ưu điểm | Giải thích |
|---------|------------|
| **Single Language** | Chỉ dùng Kotlin, không cần XML |
| **State-driven** | UI tự động update khi state thay đổi |
| **Less boilerplate** | Không cần findViewById, ViewBinding |
| **Type safe** | Compile-time checking |
| **Easy preview** | @Preview annotation |
| **Reusable** | Composable functions dễ reuse |

---

## 4. STATE-DRIVEN UI

### 4.1 Core Concept

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                  │
│                    UI = f(State)                                 │
│                                                                  │
│   ┌──────────┐         ┌──────────┐         ┌──────────┐       │
│   │  User    │────────▶│  State   │────────▶│   UI     │       │
│   │  Action  │         │  Change  │         │ Recompose│       │
│   └──────────┘         └──────────┘         └──────────┘       │
│                                                                  │
│   Ví dụ:                                                        │
│   - Click login button → isLoading = true → Button disabled     │
│   - API success → state = Success → Navigate to home            │
│   - API error → state = Error → Show error message              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Thực tế trong Project

```kotlin
// ViewModel - Nguồn state
class LoginViewModel : ViewModel() {
    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()

    fun logIn() {
        viewModelScope.launch {
            _logInState.value = LogInState.Loading  // UI tự hiện loading
            
            val result = authRepository.login(email, password)
            
            _logInState.value = if (result.isSuccess) {
                LogInState.Success(result.data)  // UI tự navigate
            } else {
                LogInState.Error(result.message)  // UI tự hiện error
            }
        }
    }
}

// UI - Tự động react với state
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val state by viewModel.logInState.collectAsState()
    
    // UI declarations based on state
    when (state) {
        is LogInState.Idle -> LoginForm(onSubmit = viewModel::logIn)
        is LogInState.Loading -> LoadingIndicator()
        is LogInState.Success -> LaunchedEffect(Unit) { navigateToHome() }
        is LogInState.Error -> ErrorMessage(state.message)
    }
}
```

---

## 5. PREVIEW & TESTING

### 5.1 @Preview Annotation

```kotlin
// Có thể preview ngay trong Android Studio
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FoodAppTheme {
        LoginScreen(
            navController = rememberNavController(),
            viewModel = FakeLoginViewModel()  // Mock for preview
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenDarkPreview() {
    FoodAppTheme(darkTheme = true) {
        LoginScreen(...)
    }
}
```

### 5.2 Testing Composables

```kotlin
// UI Testing với Compose Test
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun loginButton_showsLoading_whenClicked() {
    composeTestRule.setContent {
        LoginScreen(viewModel = testViewModel)
    }
    
    // Click login
    composeTestRule.onNodeWithText("Login").performClick()
    
    // Verify loading is shown
    composeTestRule.onNode(hasProgressBarRangeInfo()).assertIsDisplayed()
}
```

---

## 6. NAVIGATION TRONG COMPOSE

### 6.1 NavHost + NavController

```kotlin
// NavGraph.kt - Actual project code
sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object UserHome : Screen("user_home")
    object ShopDetail : Screen("shop_detail/{shopId}") {
        fun createRoute(shopId: String) = "shop_detail/$shopId"
    }
}

@Composable
fun FoodAppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Intro.route
    ) {
        composable(Screen.Intro.route) { IntroScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.UserHome.route) { UserHomeScreen(navController) }
        
        // Route với arguments
        composable(
            route = Screen.ShopDetail.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            ShopDetailScreen(navController, shopId)
        }
    }
}
```

### 6.2 Navigation Actions

```kotlin
// Navigate forward
navController.navigate(Screen.UserHome.route)

// Navigate with arguments
navController.navigate(Screen.ShopDetail.createRoute("shop_123"))

// Pop back
navController.popBackStack()

// Navigate and clear backstack
navController.navigate(Screen.UserHome.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}
```

---

## 7. TẠI SAO CHỌN COMPOSE?

### 7.1 Lý do kỹ thuật

| Lý do | Giải thích |
|-------|------------|
| **Google khuyến nghị** | Compose là future của Android UI |
| **Less code** | Giảm 30-50% so với XML |
| **State management** | Tích hợp tốt với ViewModel + StateFlow |
| **Modern tooling** | Preview, hot reload, better IDE support |
| **Kotlin-first** | Leverage full Kotlin features |

### 7.2 Lý do học thuật

| Lý do | Giải thích |
|-------|------------|
| **Industry trend** | Nhiều công ty đang migrate sang Compose |
| **Better understanding** | Hiểu declarative paradigm (giống SwiftUI, Flutter) |
| **Easier testing** | UI tests đơn giản hơn |

---

## 8. SO SÁNH CODE

### 8.1 Cùng một màn hình Login

| Metric | XML + Kotlin | Compose |
|--------|--------------|---------|
| **Files** | 2 (XML + Kotlin) | 1 (Kotlin only) |
| **Lines of code** | ~150 | ~80 |
| **State sync** | Manual observe + update | Automatic recomposition |
| **Null safety** | Potential NPE | Type safe |

### 8.2 Productivity

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEVELOPMENT SPEED                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   XML:     [██████████████████████████████] 100%                │
│   Compose: [████████████████████]          65%                  │
│                                                                  │
│   Compose faster by ~35% cho UI development                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 9. VẤN ĐÁP THƯỜNG GẶP

**Q: Tại sao không dùng XML?**  
A: Compose là công nghệ mới của Google, code ngắn gọn hơn, state management tốt hơn, phù hợp với architecture hiện đại (MVVM + StateFlow).

**Q: Compose có nhược điểm gì không?**  
A: Learning curve cao hơn, performance có thể kém hơn XML trong một số trường hợp phức tạp, cần Android Studio bản mới.

**Q: Recomposition là gì?**  
A: Khi state thay đổi, Compose sẽ "recompose" (rebuild) những phần UI phụ thuộc vào state đó. Chỉ những composable bị ảnh hưởng mới rebuild, không phải toàn bộ.

**Q: Có thể mix XML và Compose không?**  
A: Có thể dùng `ComposeView` trong XML hoặc `AndroidView` trong Compose. Project này chọn 100% Compose để consistency.

**Q: LaunchedEffect, remember dùng để làm gì?**  
A: 
- `remember`: Lưu giữ giá trị qua các lần recomposition
- `LaunchedEffect`: Chạy side effects (API calls, navigation) khi key thay đổi
- `rememberCoroutineScope`: Lấy scope để launch coroutines từ event handlers
