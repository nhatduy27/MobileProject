# Android Clean Architecture Backend Layer – Implementation Summary

## Overview
Complete skeleton for Clean Architecture (Domain → Data → DI) using Firebase BaaS (Auth, Firestore) with Hilt dependency injection. No Room, no Retrofit.

## Structure

### 📦 DOMAIN LAYER (Pure Kotlin, No Android/Firebase)

**Entities** (`domain/entities/`):
- `User` – User profile with role (BUYER/SELLER/SHIPPER)
- `UserRole` – Enum for role types
- `Restaurant` – Restaurant details, rating, fees
- `MenuItem` – Menu item with price, availability
- `OrderStatus` – Enum (PENDING, CONFIRMED, PREPARING, DELIVERING, COMPLETED, CANCELLED)
- `OrderItem` – Item in order (quantity, price)
- `Order` – Order with items, status, amounts

**Repositories** (`domain/repositories/`):
- `AuthRepository` – Interface for login/register/logout
- `RestaurantRepository` – Interface for fetching restaurants & menus
- `OrderRepository` – Interface for creating & fetching orders

**Use Cases** (`domain/usecase/`):
- `auth/`: LoginUseCase, RegisterUseCase, GetCurrentUserUseCase, LogoutUseCase
- `restaurant/`: GetRestaurantsUseCase, GetRestaurantDetailUseCase, GetMenuItemsUseCase
- `order/`: PlaceOrderUseCase, GetUserOrdersUseCase

Each use case has `suspend operator fun invoke(...)` calling the repository.

---

### 🔌 DATA LAYER (Firebase SDK, Mappers, Repository Implementations)

**Firebase Data Sources** (`data/remote/`):
- `FirebaseAuthDataSource` – Wraps FirebaseAuth with suspend functions (register, login, logout, getCurrentUser)
- `FirestoreUserDataSource` – Create & fetch user profiles from Firestore
- `FirestoreRestaurantDataSource` – Fetch restaurants & menu items from Firestore
- `FirestoreOrderDataSource` – Create orders & fetch user orders from Firestore

**Mappers** (`data/mapper/`):
- `UserMapper` – Firebase → Domain User, Domain → Firestore Map
- `RestaurantMapper` – Firestore → Restaurant, Restaurant → Firestore
- `MenuItemMapper` – Firestore → MenuItem, MenuItem → Firestore
- `OrderMapper` – Firestore → Order, Order → Firestore (handles nested OrderItems)

**Repository Implementations** (`data/repositories/`):
- `AuthRepositoryImpl` – Implements AuthRepository using FirebaseAuth + Firestore
- `RestaurantRepositoryImpl` – Implements RestaurantRepository using Firestore
- `OrderRepositoryImpl` – Implements OrderRepository using Firestore & AuthRepository

---

### 💉 DI LAYER (Hilt Modules)

**FirebaseModule** (`di/module/FirebaseModule.kt`):
- Provides: FirebaseAuth, FirebaseFirestore, FirebaseFunctions (singletons)
- Provides: All Firebase data sources (singletons)

**RepositoryModule** (`di/module/RepositoryModule.kt`):
- Provides: AuthRepository (as AuthRepositoryImpl), RestaurantRepository, OrderRepository

**UseCaseModule** (`di/module/UseCaseModule.kt`):
- Provides: All 9 use cases (LoginUseCase, RegisterUseCase, etc.)

All modules use `@Module`, `@InstallIn(SingletonComponent::class)`, `@Provides`, `@Singleton`.

---

## Key Features

✅ Clean Architecture – Domain isolated from implementation details
✅ Suspend Functions – Coroutine-based async operations
✅ Hilt Dependency Injection – Automatic constructor injection
✅ Firebase SDK Only – No Room (SQL) or Retrofit (HTTP)
✅ Mappers – Resilient conversion between Firebase & Domain models
✅ Ready for ViewModels – UI layer can inject use cases directly

## Dependencies Required

Add to `build.gradle.kts`:
```gradle
// Firebase
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-functions-ktx")

// Hilt
implementation("com.google.dagger:hilt-android:2.46")
kapt("com.google.dagger:hilt-compiler:2.46")

// Coroutines + Firebase Tasks
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")
```

## How to Use in ViewModels

Example:
```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = loginUseCase(email, password)
                // Update UI state
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

## Next Steps

1. Ensure all dependencies are added to `build.gradle.kts`
2. Create Application class with `@HiltAndroidApp` annotation
3. Create ViewModels that inject use cases
4. Build UI layer (Activities, Fragments, Composables)
5. Test each layer independently
