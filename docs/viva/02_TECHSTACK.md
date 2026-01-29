# 🛠️ TECH STACK - KTX Delivery App
> **Tài liệu Bảo vệ - Giai đoạn 1**  
> **Cập nhật lần cuối:** 30 tháng 1, 2026

---

## 1. TỔNG QUAN CÔNG NGHỆ

```
┌─────────────────────────────────────────────────────────────────────┐
│                        KTX DELIVERY TECH STACK                       │
├─────────────────────────────────────────────────────────────────────┤
│  ANDROID APP           │  BACKEND              │  ADMIN PANEL       │
│  ─────────────         │  ───────              │  ───────────       │
│  Kotlin + Compose      │  NestJS + TypeScript  │  React + Vite      │
│  MVVM Architecture     │  Firebase Functions   │  TypeScript        │
│  Retrofit + OkHttp     │  Firestore Database   │  Context API       │
│  Coroutines + Flow     │  Firebase Auth        │  Firebase SDK      │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │ Firebase │  │ Firebase │  │  Google  │
              │   Auth   │  │ Firestore│  │   Maps   │
              └──────────┘  └──────────┘  └──────────┘
                    │              │              │
                    ▼              ▼              ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │   FCM    │  │ Storage  │  │ Gemini   │
              │  Push    │  │  Images  │  │    AI    │
              └──────────┘  └──────────┘  └──────────┘
```

---

## 2. ANDROID STACK

### 2.1 Công nghệ cốt lõi

| Công nghệ | Phiên bản | Mục đích | Tham khảo |
|------------|---------|---------|-----------|
| **Kotlin** | Latest | Ngôn ngữ chính | `build.gradle.kts` |
| **Jetpack Compose** | 1.7.0 | UI framework khai báo | `build.gradle.kts` |
| **Android SDK** | 36 (target) | Target API level | `compileSdk = 36` |
| **Min SDK** | 24 | Hỗ trợ tối thiểu (Android 7.0) | `minSdk = 24` |

**Tham khảo:** `FoodApp/app/build.gradle.kts`

### 2.2 Architecture Components

| Component | Phiên bản | Mục đích | Tham khảo |
|-----------|---------|---------|-----------|
| **ViewModel** | 2.6.2 | Quản lý state | `lifecycle-viewmodel-compose` |
| **LiveData** | 2.6.2 | Dữ liệu có thể quan sát | `lifecycle-livedata-ktx` |
| **StateFlow** | (Coroutines) | Reactive state | `MutableStateFlow`, `asStateFlow()` |
| **Navigation Compose** | 2.7.5 | Điều hướng màn hình | `navigation-compose` |

**Tại sao MVVM?**
- ✅ Tách biệt rõ ràng (UI ↔ Logic ↔ Data)
- ✅ Tồn tại qua configuration changes (xoay màn hình)
- ✅ Tích hợp native với Compose
- ✅ Business logic có thể test

**Tham khảo:** `FoodApp/ARCHITECTURE.md`, ViewModels trong `pages/*/` directories

### 2.3 Networking

| Thư viện | Phiên bản | Mục đích | Tham khảo |
|---------|---------|---------|-----------|
| **Retrofit 2** | 2.9.0 | REST API client | `data/remote/api/ApiClient.kt` |
| **Gson** | - | JSON serialization | `converter-gson` |
| **OkHttp 3** | 4.11.0 | HTTP client + interceptors | `logging-interceptor` |

**Tại sao Retrofit?**
- ✅ API calls type-safe với interface definitions
- ✅ Tích hợp dễ dàng với Kotlin coroutines
- ✅ Request/response logging tích hợp sẵn
- ✅ Tiêu chuẩn ngành cho Android

**Ví dụ code:**
```kotlin
// Reference: data/remote/api/ApiClient.kt
object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)  // With logging interceptor
        .build()
}
```

### 2.4 Asynchronous Programming (Lập trình bất đồng bộ)

| Công nghệ | Phiên bản | Mục đích | Tham khảo |
|------------|---------|---------|-----------|
| **Coroutines** | 1.8.0 | Async operations | `viewModelScope.launch {}` |
| **Flow** | (Coroutines) | Reactive streams | `StateFlow`, `SharedFlow` |
| **Lifecycle-aware** | 2.6.2 | Tự động hủy theo lifecycle | `lifecycle-runtime-compose` |

**Tại sao Coroutines?**
- ✅ Giải pháp native của Kotlin (không phức tạp như RxJava)
- ✅ Structured concurrency (tự động hủy)
- ✅ Suspend functions cho async code tuần tự
- ✅ Flow cho reactive streams

**Code Pattern:**
```kotlin
// Reference: LoginViewModel.kt
viewModelScope.launch {
    _logInState.value = LogInState.Loading
    val result = authRepository.login(email, password)
    _logInState.value = when (result) {
        is ApiResult.Success -> LogInState.Success(result.data)
        is ApiResult.Error -> LogInState.Error(result.message)
    }
}
```

### 2.5 Image Loading (Tải hình ảnh)

| Thư viện | Phiên bản | Mục đích | Tham khảo |
|---------|---------|---------|-----------|
| **Coil** | 2.6.0 | Image loading + caching | `coil-compose` |

**Tại sao Coil?**
- ✅ Kotlin-first (suspend functions)
- ✅ Tích hợp native với Compose (`AsyncImage`)
- ✅ Tự động memory/disk caching
- ✅ Nhẹ (so với Glide/Picasso)

### 2.6 Dependency Injection (Tiêm phụ thuộc)

| Cách tiếp cận | Cách triển khai | Tham khảo |
|----------|---------------|-----------|
| **Manual Factory** | ViewModelProvider.Factory | `LoginViewModel.factory(context)` |

**Tại sao Manual DI (thay vì Hilt/Koin)?**
- ✅ Không thêm độ phức tạp cho việc học
- ✅ Dependencies rõ ràng (dễ hiểu)
- ⚠️ Code boilerplate nhiều hơn
- ⚠️ Được liệt kê là technical debt cho tương lai

**Ví dụ code:**
```kotlin
// Reference: LoginViewModel.kt
companion object {
    fun factory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(
                    repository = UserFirebaseRepository(context),
                    authRepository = AuthRepository(),
                    notificationRepository = NotificationRepository(),
                    context = context
                ) as T
            }
        }
    }
}
```

---

## 3. FIREBASE STACK

### 3.1 Tổng quan Firebase Services

```
┌─────────────────────────────────────────────────────────────┐
│                    FIREBASE PROJECT                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Firebase  │  │  Firestore  │  │  Cloud Functions    │ │
│  │     Auth    │  │  Database   │  │  (NestJS Backend)   │ │
│  │             │  │             │  │                     │ │
│  │ • Email/Pwd │  │ • NoSQL     │  │ • REST API          │ │
│  │ • Google    │  │ • Realtime  │  │ • Business Logic    │ │
│  │ • Custom    │  │ • Offline   │  │ • Triggers          │ │
│  │   Tokens    │  │   sync      │  │                     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Firebase  │  │  Firebase   │  │  Firebase           │ │
│  │   Storage   │  │     FCM     │  │  Analytics          │ │
│  │             │  │             │  │                     │ │
│  │ • Images    │  │ • Push      │  │ • Events            │ │
│  │ • Files     │  │ • Topics    │  │ • Funnels           │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Firebase Authentication

| Tính năng | Cách triển khai | Tham khảo |
|---------|---------------|-----------|
| **Email/Password** | Firebase Auth REST API | `AuthService.login()` |
| **Google Sign-In** | Google Sign-In SDK | `LoginViewModel.handleGoogleSignIn()` |
| **Custom Tokens** | Admin SDK | `AuthService.generateCustomToken()` |
| **OTP Verification** | Email via SendGrid | `EmailService`, `otps` collection |

**Android SDK:**
```kotlin
implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
implementation("com.google.android.gms:play-services-auth:20.7.0")
```

**Tại sao Firebase Auth?**
- ✅ Dịch vụ quản lý danh tính (không cần tự triển khai)
- ✅ Nhiều providers (Email, Google, Phone)
- ✅ Quản lý token bảo mật
- ✅ Phân quyền theo vai trò dễ dàng qua custom claims

**Luồng Authentication:**
```
[Android]                    [Backend]                    [Firebase]
    │                            │                            │
    │─── POST /auth/login ──────►│                            │
    │    {email, password}       │                            │
    │                            │─── Verify credentials ────►│
    │                            │◄── User record ────────────│
    │                            │                            │
    │                            │─── Create custom token ───►│
    │                            │◄── Custom token ───────────│
    │◄── {customToken, user} ────│                            │
    │                            │                            │
    │─── signInWithCustomToken() ────────────────────────────►│
    │◄── Firebase ID token ───────────────────────────────────│
```

**Tham khảo:** `modules/auth/auth.service.ts`, `LoginViewModel.kt`

### 3.3 Cloud Firestore

| Khía cạnh | Chi tiết | Tham khảo |
|--------|---------|-----------|
| **Loại** | Cơ sở dữ liệu NoSQL document | Tất cả `Firestore*Repository` classes |
| **Cấu trúc** | Collections → Documents → Subcollections | `firestore.indexes.json` |
| **Transactions** | Atomic multi-document operations | `OrdersService.createOrder()` |
| **Security** | Kiểm soát truy cập dựa trên rules | `firestore.rules` |

**Collections chính:**
| Collection | Mục đích | Trường chính |
|------------|---------|------------|
| `users` | Tài khoản người dùng | `id`, `email`, `role`, `status` |
| `shops` | Dữ liệu nhà hàng | `id`, `ownerId`, `name`, `status` |
| `products` | Danh mục sản phẩm | `id`, `shopId`, `name`, `price`, `soldCount` |
| `orders` | Documents đơn hàng | `id`, `customerId`, `shopId`, `status` |
| `trips` | Chuyến giao hàng | `id`, `shipperId`, `orderIds[]`, `status` |

**Tại sao Firestore?**
- ✅ Cập nhật thời gian thực (listeners)
- ✅ Hỗ trợ offline (tự động sync)
- ✅ Có thể mở rộng (không cần quản lý server)
- ✅ Tích hợp với các dịch vụ Firebase khác
- ✅ Truy vấn linh hoạt

**Tham khảo:** `Backend/firestore.indexes.json`, `firestore.rules`

### 3.4 Cloud Functions

| Khía cạnh | Chi tiết | Tham khảo |
|--------|---------|-----------|
| **Runtime** | Node.js 24 | `package.json` engines |
| **Framework** | NestJS 11.1.11 | Kiến trúc modular |
| **Memory** | 2GB | `runWith({ memory: '2GB' })` |
| **Timeout** | 300 seconds | `runWith({ timeoutSeconds: 300 })` |

**Deployment:**
```bash
# Reference: package.json scripts
npm run deploy  # firebase deploy --only functions
```

**Tại sao NestJS trên Cloud Functions?**
- ✅ TypeScript-first (type safety)
- ✅ Kiến trúc modular (có thể mở rộng)
- ✅ Validation tích hợp sẵn (class-validator)
- ✅ Swagger documentation
- ✅ Express patterns quen thuộc

**Tham khảo:** `Backend/functions/src/index.ts`, `Backend/functions/package.json`

### 3.5 Firebase Cloud Messaging (FCM)

| Tính năng | Cách triển khai | Tham khảo |
|---------|---------------|-----------|
| **Token Registration** | On login/signup | `LoginViewModel`, `SignUpViewModel` |
| **Token Storage** | `users/{userId}/fcmTokens` subcollection | `NotificationsService` |
| **Push Sending** | Firebase Admin SDK | `FirebaseService.sendNotification()` |
| **Topics** | `shipper_available`, `admin_announcements` | `sendToTopic()` |

**Android SDK:**
```kotlin
implementation("com.google.firebase:firebase-messaging:23.3.1")
```

**Tại sao FCM?**
- ✅ Tích hợp native với Android
- ✅ Topic-based broadcasting
- ✅ Free tier hào phóng
- ✅ Giao hàng đáng tin cậy

**Tham khảo:** `modules/notifications/`, `NotificationsService.send()`

### 3.6 Firebase Storage

| Tính năng | Cách triển khai | Tham khảo |
|---------|---------------|-----------|
| **Product Images** | Upload via backend | `ProductsController` |
| **Shop Images** | Logo + Cover image | `ShopsController` |
| **File Types** | JPEG, PNG | Multer middleware |

**Tại sao Firebase Storage?**
- ✅ Tích hợp với Firebase Auth (security rules)
- ✅ CDN-backed (giao hàng nhanh)
- ✅ Tạo URL dễ dàng

**Tham khảo:** `Backend/storage.rules`, `modules/products/`, `shared/services/storage.service.ts`

---

## 4. GOOGLE INTEGRATIONS

### 4.1 Google Maps

| Thư viện | Phiên bản | Mục đích | Tham khảo |
|---------|---------|---------|-----------|
| **Maps Compose** | 2.15.0 | Map UI trong Compose | `DeliveryMapScreen.kt` |
| **Play Services Maps** | 18.2.0 | Core Maps SDK | `build.gradle.kts` |
| **Android Maps Utils** | 3.5.3 | Polyline decoding | Route rendering |

**Tính năng sử dụng:**
- 📍 Hiển thị bản đồ với markers
- 🛤️ Render polyline route
- 📌 Cập nhật vị trí shipper thời gian thực
- 🎯 Destination markers

**Tại sao Google Maps?**
- ✅ Tiêu chuẩn ngành cho bản đồ
- ✅ Hỗ trợ native Compose
- ✅ Đáng tin cậy ở Việt Nam
- ✅ Tích hợp dễ dàng với location services

**Tham khảo:** `pages/shipper/gps/DeliveryMapScreen.kt`, `AndroidManifest.xml` (API key)

### 4.2 Google Location Services

| Thư viện | Phiên bản | Mục đích | Tham khảo |
|---------|---------|---------|-----------|
| **Play Services Location** | 21.0.1 | GPS tracking | `GpsViewModel.kt` |
| **FusedLocationProvider** | - | Location hiệu quả | `startLocationTracking()` |

**Permissions:**
```xml
<!-- Reference: AndroidManifest.xml -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

**Tại sao FusedLocationProvider?**
- ✅ Tiết kiệm pin (sử dụng nhiều nguồn)
- ✅ Độ chính xác cao khi cần
- ✅ Tự động chuyển đổi provider

**Tham khảo:** `GpsViewModel.updateLocation()`, `utils/LocationHelper.kt`

### 4.3 Google Gemini AI

| Thư viện | Phiên bản | Mục đích | Tham khảo |
|---------|---------|---------|-----------|
| **@google/genai** | 1.38.0 | AI chatbot | `modules/chatbot/` |

**Tính năng:**
- 🤖 AI đàm thoại hỗ trợ khách hàng
- 📝 Gợi ý sản phẩm
- ❓ Trả lời FAQ

**Tại sao Gemini?**
- ✅ Language model tiên tiến
- ✅ Tích hợp API dễ dàng
- ✅ Hiểu ngữ cảnh

**Tham khảo:** `Backend/functions/src/modules/chatbot/`

---

## 5. BACKEND DEPENDENCIES

### 5.1 NestJS Ecosystem

| Package | Phiên bản | Mục đích |
|---------|---------|---------|
| `@nestjs/core` | 11.1.11 | Core framework |
| `@nestjs/common` | 11.1.11 | Common utilities |
| `@nestjs/platform-express` | 11.1.12 | Express adapter |
| `@nestjs/swagger` | 11.2.5 | API documentation |

### 5.2 Validation & Transformation

| Package | Phiên bản | Mục đích |
|---------|---------|---------|
| `class-validator` | 0.14.3 | DTO validation |
| `class-transformer` | 0.5.1 | Object transformation |

**Ví dụ code:**
```typescript
// Reference: modules/auth/dto/login.dto.ts
export class LoginDto {
    @IsEmail()
    @IsNotEmpty()
    email: string;
    
    @IsString()
    @MinLength(6)
    password: string;
}
```

### 5.3 External Services (Dịch vụ bên ngoài)

| Package | Phiên bản | Mục đích |
|---------|---------|---------|
| `@sendgrid/mail` | 8.1.6 | Giao email (OTP) |
| `fuse.js` | 7.1.0 | Fuzzy text search |
| `multer` | 2.0.2 | Xử lý file upload |

### 5.4 Testing

| Package | Phiên bản | Mục đích |
|---------|---------|---------|
| `jest` | 29.7.0 | Unit testing |
| `@nestjs/testing` | 11.1.12 | Test utilities |
| `ts-jest` | 29.4.6 | TypeScript support |

**Tham khảo:** `Backend/functions/package.json`

---

## 6. MÔ HÌNH BẢO MẬT

### 6.1 Luồng Authentication

```
[User] → [Android App] → [Backend API] → [Firebase Auth]
                              │
                              ▼
                    Token Validation (AuthGuard)
                              │
                              ▼
                    Role Check (RolesGuard)
                              │
                              ▼
                    Business Logic
```

### 6.2 Firestore Security Rules

**Rules chính:**
```javascript
// Reference: firestore.rules
function isAuthenticated() {
  return request.auth != null;
}

function isCurrentUser(userId) {
  return request.auth.uid == userId;
}

function hasRole(role) {
  return isAuthenticated() && 
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == role;
}

// Example: Shops - owner can update own shop
match /shops/{shopId} {
  allow read: if true;
  allow create: if isAuthenticated();
  allow update, delete: if resource.data.ownerId == request.auth.uid || isAdmin();
}
```

### 6.3 Backend Guards

| Guard | Mục đích | Tham khảo |
|-------|---------|-----------|
| `AuthGuard` | Validate Firebase token | Áp dụng cho tất cả protected routes |
| `RolesGuard` | Kiểm tra user role | `@Roles(UserRole.CUSTOMER)` decorator |

**Tham khảo:** `firestore.rules`, `AuthGuard`, `RolesGuard`

---

## 7. TÓM TẮT: TẠI SAO CHỌN TECH STACK NÀY?

| Lựa chọn | Các lựa chọn thay thế đã xem xét | Tại sao được chọn |
|--------|------------------------|------------|
| **Kotlin + Compose** | Java + XML, Flutter | Native Android, UI hiện đại, Google-backed |
| **MVVM** | MVI, Clean Architecture | Đơn giản hơn, tích hợp Compose, team quen thuộc |
| **Firebase** | AWS Amplify, Supabase | All-in-one, free tier, documentation tốt |
| **NestJS** | Express, Fastify | TypeScript, modular, Swagger tích hợp sẵn |
| **Firestore** | Realtime DB, MongoDB | Truy vấn linh hoạt, offline sync, security rules |
| **FCM** | OneSignal, Pusher | Tích hợp native, miễn phí, đáng tin cậy |
| **Retrofit** | Ktor, Volley | Tiêu chuẩn ngành, hỗ trợ coroutines |
| **Coroutines** | RxJava, Callbacks | Native Kotlin, đơn giản hơn, structured |
| **Google Maps** | Mapbox, OpenStreetMap | Phủ sóng tốt ở Việt Nam, hỗ trợ Compose |

---

## 8. THAM CHIẾU FILE

| Công nghệ | File chính |
|------------|-----------|
| **Android Build** | `FoodApp/app/build.gradle.kts` |
| **Backend Deps** | `Backend/functions/package.json` |
| **Firebase Config** | `Backend/firebase.json`, `firestore.rules` |
| **Architecture** | `FoodApp/ARCHITECTURE.md` |
| **API Client** | `data/remote/api/ApiClient.kt` |
| **ViewModels** | `authentication/*/`, `pages/*/*ViewModel.kt` |

---

**KẾT THÚC TECH STACK**
