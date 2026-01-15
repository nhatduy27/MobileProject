# 🏗️ Kiến trúc Frontend - Food App

## 📋 Mục lục
1. [Tổng quan kiến trúc](#tổng-quan-kiến-trúc)
2. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
3. [Luồng hoạt động](#luồng-hoạt-động)
4. [Chi tiết từng layer](#chi-tiết-từng-layer)
5. [Ví dụ thực tế](#ví-dụ-thực-tế)

---

## 🎯 Tổng quan kiến trúc

App sử dụng **MVVM (Model-View-ViewModel)** kết hợp với **Repository Pattern**:

```
┌─────────────────────────────────────────────────────────────┐
│                         USER                                │
│                           ↓                                 │
├─────────────────────────────────────────────────────────────┤
│                    UI LAYER (View)                          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Composable Functions (@Composable)                   │  │
│  │  - ShopSetupScreen.kt                                 │  │
│  │  - RoleSelectionScreen.kt                             │  │
│  │  - LoginScreen.kt                                     │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↕                                 │
├─────────────────────────────────────────────────────────────┤
│                  VIEWMODEL LAYER                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ViewModels (Business Logic)                          │  │
│  │  - ShopSetupViewModel.kt                              │  │
│  │  - RoleSelectionViewModel.kt                          │  │
│  │  - LoginViewModel.kt                                  │  │
│  │                                                        │  │
│  │  StateFlow<UiState> ← Quản lý state                   │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↕                                 │
├─────────────────────────────────────────────────────────────┤
│                  REPOSITORY LAYER                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repositories (Data Source Abstraction)               │  │
│  │  - ShopRepository.kt                                  │  │
│  │  - AuthRepository.kt                                  │  │
│  │  - UserFirebaseRepository.kt                          │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↕                                 │
├─────────────────────────────────────────────────────────────┤
│                   DATA LAYER                                │
│  ┌─────────────────────┬──────────────────────────────┐   │
│  │   Remote (API)      │   Local (Firebase)           │   │
│  │  ┌──────────────┐   │  ┌──────────────────────┐    │   │
│  │  │ ApiService   │   │  │ FirebaseAuth         │    │   │
│  │  │ - ShopApi    │   │  │ FirebaseFirestore    │    │   │
│  │  │ - AuthApi    │   │  │ GoogleSignInClient   │    │   │
│  │  └──────────────┘   │  └──────────────────────┘    │   │
│  └─────────────────────┴──────────────────────────────┘   │
│                           ↕                                 │
├─────────────────────────────────────────────────────────────┤
│                   NETWORK LAYER                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Retrofit + OkHttp                                    │  │
│  │  - ApiClient (Singleton)                              │  │
│  │  - Interceptors (Auth, Logging)                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↕                                 │
└─────────────────────────────────────────────────────────────┘
                    Backend API / Firebase
```

---

## 📁 Cấu trúc thư mục

```
app/src/main/java/com/example/foodapp/
│
├── 📂 authentication/              # Màn hình xác thực
│   ├── login/
│   │   ├── LoginScreen.kt         # UI đăng nhập
│   │   └── LoginViewModel.kt      # Logic đăng nhập
│   ├── register/
│   │   ├── RegisterScreen.kt
│   │   └── RegisterViewModel.kt
│   └── roleselection/
│       ├── RoleSelectionScreen.kt # Chọn vai trò
│       └── RoleSelectionViewModel.kt
│
├── 📂 pages/                       # Màn hình chính
│   ├── owner/                     # Màn hình cho Owner
│   │   ├── shopsetup/
│   │   │   ├── ShopSetupScreen.kt      # UI setup shop
│   │   │   ├── ShopSetupViewModel.kt   # Logic setup
│   │   │   ├── ShopSetupUiState.kt     # State definition
│   │   │   └── OwnerHomeWrapper.kt     # Wrapper check shop
│   │   └── dashboard/
│   │       └── DashboardViewModel.kt
│   ├── customer/                  # Màn hình cho Customer
│   └── shipper/                   # Màn hình cho Shipper
│
├── 📂 data/                        # Data Layer
│   ├── model/                     # Data Models
│   │   ├── owner/
│   │   │   ├── Shop.kt           # Shop model
│   │   │   ├── CreateShopRequest.kt
│   │   │   └── CreateShopResponse.kt
│   │   └── shared/
│   │       └── auth/
│   │           ├── ApiResult.kt   # Wrapper cho kết quả API
│   │           └── Auth.Models.kt # Auth models
│   │
│   ├── remote/                    # Remote Data Source
│   │   ├── api/
│   │   │   └── ApiClient.kt      # Retrofit singleton
│   │   ├── owner/
│   │   │   └── ShopApiService.kt # Shop API endpoints
│   │   └── shared/
│   │       └── AuthApiService.kt # Auth API endpoints
│   │
│   └── repository/                # Repository Pattern
│       ├── owner/
│       │   └── shop/
│       │       └── ShopRepository.kt  # Shop data operations
│       ├── shared/
│       │   └── AuthRepository.kt      # Auth operations
│       └── firebase/
│           └── UserFirebaseRepository.kt # Firebase operations
│
├── 📂 navigation/                  # Navigation
│   └── NavGraph.kt                # App navigation graph
│
└── 📂 ui/                          # UI Components
    └── theme/
        └── Theme.kt               # App theme
```

---

## 🔄 Luồng hoạt động

### 1️⃣ **Luồng tạo Shop (Owner Setup)**

```
┌──────────────┐
│   USER       │
│  Nhập form   │
│  Chọn ảnh    │
└──────┬───────┘
       │ onClick
       ↓
┌─────────────────────────────────────────────────────────┐
│  ShopSetupScreen.kt (UI Layer)                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │  @Composable fun ShopSetupScreen()                │  │
│  │  {                                                 │  │
│  │    val uiState by viewModel.uiState.collectAsState│  │
│  │                                                    │  │
│  │    TextField(value = uiState.shopName, ...)       │  │
│  │    ImagePickerCard(uri = uiState.coverImageUri)   │  │
│  │                                                    │  │
│  │    Button(onClick = {                             │  │
│  │      viewModel.createShop(onSuccess)  ────────┐   │  │
│  │    })                                         │   │  │
│  │  }                                            │   │  │
│  └───────────────────────────────────────────────┼───┘  │
└────────────────────────────────────────────────┼───────┘
                                                 │
                                                 ↓
┌─────────────────────────────────────────────────────────┐
│  ShopSetupViewModel.kt (ViewModel Layer)                │
│  ┌───────────────────────────────────────────────────┐  │
│  │  class ShopSetupViewModel(context: Context) {     │  │
│  │    private val repository = ShopRepository(ctx)   │  │
│  │    private val _uiState = MutableStateFlow(...)   │  │
│  │                                                    │  │
│  │    fun createShop(onSuccess: () -> Unit) {        │  │
│  │      if (!validateForm()) return                  │  │
│  │                                                    │  │
│  │      viewModelScope.launch {                      │  │
│  │        _uiState.update { isLoading = true }       │  │
│  │                                                    │  │
│  │        val result = repository.createShopWithImages(│ │
│  │          name, description, ...,                  │  │
│  │          coverImageUri, logoUri  ─────────────┐   │  │
│  │        )                                      │   │  │
│  │                                               │   │  │
│  │        result.onSuccess { ... }               │   │  │
│  │        result.onFailure { ... }               │   │  │
│  │      }                                        │   │  │
│  │    }                                          │   │  │
│  │  }                                            │   │  │
│  └───────────────────────────────────────────────┼───┘  │
└────────────────────────────────────────────────┼───────┘
                                                 │
                                                 ↓
┌─────────────────────────────────────────────────────────┐
│  ShopRepository.kt (Repository Layer)                   │
│  ┌───────────────────────────────────────────────────┐  │
│  │  class ShopRepository(private val context) {      │  │
│  │    private val apiService = ApiClient.create(...) │  │
│  │                                                    │  │
│  │    suspend fun createShopWithImages(              │  │
│  │      name, ..., coverUri, logoUri                 │  │
│  │    ): Result<Shop> {                              │  │
│  │      return withContext(Dispatchers.IO) {         │  │
│  │        // 1. Convert text to RequestBody          │  │
│  │        val namePart = name.toRequestBody(...)     │  │
│  │                                                    │  │
│  │        // 2. Convert URI to MultipartBody.Part    │  │
│  │        val coverPart = uriToMultipartPart(        │  │
│  │          coverUri, "coverImage"                   │  │
│  │        )                                          │  │
│  │        val logoPart = uriToMultipartPart(...)     │  │
│  │                                                    │  │
│  │        // 3. Call API                             │  │
│  │        val response = apiService                  │  │
│  │          .createShopWithImages(  ─────────────┐   │  │
│  │            namePart, ...,                     │   │  │
│  │            coverPart, logoPart                │   │  │
│  │          )                                    │   │  │
│  │                                               │   │  │
│  │        // 4. Handle response                  │   │  │
│  │        if (response.isSuccessful) {           │   │  │
│  │          Result.success(response.body().data) │   │  │
│  │        } else {                                │   │  │
│  │          Result.failure(...)                  │   │  │
│  │        }                                      │   │  │
│  │      }                                        │   │  │
│  │    }                                          │   │  │
│  │                                               │   │  │
│  │    private fun uriToMultipartPart(...) {      │   │  │
│  │      // Convert URI → File → MultipartBody    │   │  │
│  │    }                                          │   │  │
│  │  }                                            │   │  │
│  └───────────────────────────────────────────────┼───┘  │
└────────────────────────────────────────────────┼───────┘
                                                 │
                                                 ↓
┌─────────────────────────────────────────────────────────┐
│  ShopApiService.kt (API Interface)                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │  interface ShopApiService {                       │  │
│  │    @Multipart                                     │  │
│  │    @POST("owner/shop")                            │  │
│  │    suspend fun createShopWithImages(              │  │
│  │      @Part("name") name: RequestBody,             │  │
│  │      @Part("description") description: RequestBody│ │
│  │      @Part coverImage: MultipartBody.Part,        │  │
│  │      @Part logo: MultipartBody.Part               │  │
│  │    ): Response<CreateShopResponse>  ──────────┐   │  │
│  │  }                                            │   │  │
│  └───────────────────────────────────────────────┼───┘  │
└────────────────────────────────────────────────┼───────┘
                                                 │
                                                 ↓
┌─────────────────────────────────────────────────────────┐
│  ApiClient.kt (Retrofit Configuration)                  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  object ApiClient {                               │  │
│  │    private val retrofit = Retrofit.Builder()      │  │
│  │      .baseUrl("http://10.0.2.2:3000/api/")        │  │
│  │      .client(okHttpClient)  // + Interceptors     │  │
│  │      .addConverterFactory(GsonConverterFactory)   │  │
│  │      .build()                                     │  │
│  │                                                    │  │
│  │    fun <T> createService(service: Class<T>): T {  │  │
│  │      return retrofit.create(service)              │  │
│  │    }                                              │  │
│  │  }                                                │  │
│  │                                                    │  │
│  │  OkHttpClient with:                               │  │
│  │  - AuthInterceptor (add Bearer token)            │  │
│  │  - LoggingInterceptor (log requests)             │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                         │
                         ↓
                  ┌──────────────┐
                  │   BACKEND    │
                  │ POST /owner/ │
                  │     shop     │
                  └──────────────┘
```

---

### 2️⃣ **Luồng Authentication (Login)**

```
┌──────────────┐
│   USER       │
│ Nhập email   │
│ Nhập password│
└──────┬───────┘
       │ onClick Login
       ↓
┌─────────────────────────────────────────┐
│  LoginScreen.kt                         │
│  Button { viewModel.login(email, pwd) } │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────────────────┐
│  LoginViewModel.kt                                  │
│  fun login(email, password) {                       │
│    viewModelScope.launch {                          │
│      val result = authRepository.login(email, pwd)  │
│      result.onSuccess { response ->                 │
│        // Lưu token vào SharedPreferences           │
│        saveToken(response.data.idToken)             │
│        _uiState.update { success = true }           │
│      }                                              │
│    }                                                │
│  }                                                  │
└──────────────┬──────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────────┐
│  AuthRepository.kt                                  │
│  suspend fun login(email, pwd): ApiResult<...> {    │
│    withContext(Dispatchers.IO) {                    │
│      val request = LoginRequest(email, pwd)         │
│      val response = apiService.login(request)       │
│      if (response.isSuccessful) {                   │
│        ApiResult.Success(response.body())           │
│      } else {                                       │
│        ApiResult.Failure(...)                       │
│      }                                              │
│    }                                                │
│  }                                                  │
└──────────────┬──────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────────┐
│  AuthApiService.kt                                  │
│  @POST("auth/login")                                │
│  suspend fun login(                                 │
│    @Body request: LoginRequest                      │
│  ): Response<ApiResponse>                           │
└──────────────┬──────────────────────────────────────┘
               ↓
         Backend API
```

---

## 🔍 Chi tiết từng layer

### 📱 **UI Layer (View)**

**Trách nhiệm:**
- Hiển thị UI
- Nhận input từ user
- Observe state từ ViewModel
- Không chứa business logic

**Công nghệ:**
- Jetpack Compose (`@Composable`)
- Material3 components

**Ví dụ:**
```kotlin
@Composable
fun ShopSetupScreen(onSetupComplete: () -> Unit) {
    val viewModel: ShopSetupViewModel = viewModel(...)
    val uiState by viewModel.uiState.collectAsState()
    
    // UI reacts to state changes
    if (uiState.isLoading) {
        CircularProgressIndicator()
    }
    
    TextField(
        value = uiState.shopName,
        onValueChange = viewModel::updateShopName,
        isError = uiState.shopNameError != null
    )
    
    Button(onClick = { viewModel.createShop(onSetupComplete) }) {
        Text("Tạo shop")
    }
}
```

---

### 🧠 **ViewModel Layer**

**Trách nhiệm:**
- Quản lý UI state
- Xử lý business logic
- Gọi Repository để lấy/gửi data
- Survive configuration changes (screen rotation)

**Công nghệ:**
- `ViewModel` (Android Architecture Components)
- `StateFlow` / `MutableStateFlow` (Kotlin Coroutines)
- `viewModelScope` (Coroutine scope)

**Pattern:**
```kotlin
class ShopSetupViewModel(context: Context) : ViewModel() {
    private val repository = ShopRepository(context)
    
    // Private mutable state
    private val _uiState = MutableStateFlow(ShopSetupUiState())
    
    // Public immutable state
    val uiState: StateFlow<ShopSetupUiState> = _uiState.asStateFlow()
    
    // Update state
    fun updateShopName(name: String) {
        _uiState.update { it.copy(shopName = name) }
    }
    
    // Business logic
    fun createShop(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.createShopWithImages(...)
            
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, success = true) }
                onSuccess()
            }
        }
    }
}
```

**UiState Pattern:**
```kotlin
data class ShopSetupUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Form fields
    val shopName: String = "",
    val coverImageUri: Uri? = null,
    
    // Validation errors
    val shopNameError: String? = null,
    val coverImageError: String? = null
)
```

---

### 💾 **Repository Layer**

**Trách nhiệm:**
- Abstract data sources (API, Firebase, Database)
- Quyết định lấy data từ đâu (cache, network, local)
- Convert data models
- Handle errors

**Pattern:**
```kotlin
class ShopRepository(private val context: Context) {
    private val apiService = ApiClient.createService(ShopApiService::class.java)
    
    suspend fun createShopWithImages(
        name: String,
        coverImageUri: Uri,
        logoUri: Uri
    ): Result<Shop> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Prepare data
                val namePart = name.toRequestBody(...)
                val coverPart = uriToMultipartPart(coverImageUri, "coverImage")
                
                // 2. Call API
                val response = apiService.createShopWithImages(
                    name = namePart,
                    coverImage = coverPart,
                    logo = logoPart
                )
                
                // 3. Handle response
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

---

### 🌐 **Data Layer**

#### **API Service (Retrofit Interface)**

```kotlin
interface ShopApiService {
    @Multipart
    @POST("owner/shop")
    suspend fun createShopWithImages(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part coverImage: MultipartBody.Part,
        @Part logo: MultipartBody.Part
    ): Response<CreateShopResponse>
    
    @GET("owner/shop")
    suspend fun getMyShop(): Response<Shop>
}
```

#### **ApiClient (Retrofit Configuration)**

```kotlin
object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)  // Add token
        .addInterceptor(loggingInterceptor)  // Log requests
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
```

#### **Auth Interceptor**

```kotlin
private val authInterceptor = Interceptor { chain ->
    val token = sharedPref.getString("firebase_id_token", null)
    
    val request = if (token != null) {
        chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
    } else {
        chain.request()
    }
    
    chain.proceed(request)
}
```

---

## 📝 Ví dụ thực tế: Tạo Shop

### **Bước 1: User nhập form**
```kotlin
// ShopSetupScreen.kt
TextField(
    value = uiState.shopName,
    onValueChange = { viewModel.updateShopName(it) }
)

ImagePickerCard(
    imageUri = uiState.coverImageUri,
    onImageSelected = { viewModel.updateCoverImage(it) }
)
```

### **Bước 2: User click "Tạo shop"**
```kotlin
Button(onClick = { viewModel.createShop(onSuccess) })
```

### **Bước 3: ViewModel validate và gọi Repository**
```kotlin
// ShopSetupViewModel.kt
fun createShop(onSuccess: () -> Unit) {
    if (!validateForm()) return  // Validate trước
    
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        
        val result = repository.createShopWithImages(
            name = uiState.value.shopName,
            coverImageUri = uiState.value.coverImageUri!!,
            logoUri = uiState.value.logoUri!!
        )
        
        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, success = true) }
            onSuccess()
        }
    }
}
```

### **Bước 4: Repository chuẩn bị data và gọi API**
```kotlin
// ShopRepository.kt
suspend fun createShopWithImages(...): Result<Shop> {
    return withContext(Dispatchers.IO) {
        // Convert URI → MultipartBody.Part
        val coverPart = uriToMultipartPart(coverImageUri, "coverImage")
        
        // Call API
        val response = apiService.createShopWithImages(
            name = namePart,
            coverImage = coverPart,
            logo = logoPart
        )
        
        // Return result
        if (response.isSuccessful) {
            Result.success(response.body()!!.data)
        } else {
            Result.failure(Exception(...))
        }
    }
}
```

### **Bước 5: Retrofit gửi HTTP request**
```kotlin
// ShopApiService.kt
@Multipart
@POST("owner/shop")
suspend fun createShopWithImages(
    @Part("name") name: RequestBody,
    @Part coverImage: MultipartBody.Part,
    @Part logo: MultipartBody.Part
): Response<CreateShopResponse>
```

### **Bước 6: Backend xử lý và trả response**
```
POST http://10.0.2.2:3000/api/owner/shop
Headers: Authorization: Bearer <token>
Content-Type: multipart/form-data

Response:
{
  "success": true,
  "data": {
    "id": "shop_123",
    "name": "Quán Phở Việt",
    "coverImageUrl": "https://...",
    ...
  }
}
```

### **Bước 7: Repository parse response**
```kotlin
if (response.isSuccessful && response.body() != null) {
    val shop = response.body()!!.data
    Result.success(shop)
}
```

### **Bước 8: ViewModel update UI state**
```kotlin
result.onSuccess { shop ->
    _uiState.update { 
        it.copy(
            isLoading = false,
            successMessage = "Tạo shop thành công!"
        )
    }
    onSuccess()  // Navigate to next screen
}
```

### **Bước 9: UI react to state change**
```kotlin
// ShopSetupScreen.kt
val uiState by viewModel.uiState.collectAsState()

if (uiState.successMessage != null) {
    SuccessCard(message = uiState.successMessage!!)
}

LaunchedEffect(uiState.successMessage) {
    if (uiState.successMessage != null) {
        delay(1000)
        onSetupComplete()  // Navigate
    }
}
```

---

## 🔐 Authentication Flow

```
┌─────────────────────────────────────────────────────────┐
│  1. User Login                                          │
│     ↓                                                   │
│  2. AuthRepository.login(email, password)               │
│     ↓                                                   │
│  3. POST /auth/login                                    │
│     ↓                                                   │
│  4. Backend returns { idToken, refreshToken }           │
│     ↓                                                   │
│  5. Save tokens to SharedPreferences                    │
│     ↓                                                   │
│  6. AuthInterceptor adds "Authorization: Bearer token"  │
│     to all subsequent requests                          │
│     ↓                                                   │
│  7. Protected endpoints work (e.g., POST /owner/shop)   │
└─────────────────────────────────────────────────────────┘
```

---

## 🎨 State Management

### **StateFlow Pattern**

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Update state
_uiState.update { currentState ->
    currentState.copy(isLoading = true)
}

// UI observes state
@Composable
fun Screen() {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isLoading) {
        LoadingIndicator()
    }
}
```

---

## 🚀 Best Practices

### ✅ **DO**
- Sử dụng `suspend` functions cho network calls
- Wrap API calls trong `withContext(Dispatchers.IO)`
- Handle errors properly với `Result<T>` hoặc `ApiResult<T>`
- Validate input ở ViewModel trước khi gọi Repository
- Use `StateFlow` cho reactive UI
- Keep UI logic in Composables, business logic in ViewModels

### ❌ **DON'T**
- Gọi API trực tiếp từ UI
- Block main thread với network calls
- Lưu sensitive data (tokens) trong ViewModel
- Hardcode strings (use string resources)
- Ignore error handling

---

## 📚 Tài liệu tham khảo

- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Retrofit](https://square.github.io/retrofit/)
- [MVVM Pattern](https://developer.android.com/topic/architecture#recommended-app-arch)
