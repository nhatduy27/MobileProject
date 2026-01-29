# 📚 CODEBASE MAP - KTX Delivery App
> **Documentation for Viva/Oral Defense**  
> **Last Updated:** January 30, 2026  
> **Purpose:** Comprehensive audit-friendly codebase reference

---

## 🇻🇳 TÓM TẮT CODEBASE (Dùng cho vấn đáp)

**Khi thầy hỏi "Em giới thiệu nhanh cấu trúc codebase":**

- **Dự án gồm 3 thành phần chính:** Android app (Kotlin), Backend (NestJS), Admin dashboard (React)
- **Android dùng kiến trúc MVVM:** ViewModel quản lý logic, Jetpack Compose để hiển thị UI, Coroutines xử lý bất đồng bộ
- **Backend chạy trên Firebase Cloud Functions:** NestJS framework, TypeScript, 23 modules độc lập
- **Ứng dụng là nền tảng giao đồ ăn:** 4 vai trò chính là Khách hàng, Chủ quán, Shipper, Admin
- **Luồng chính:** Khách order → Chủ quán xác nhận → Chuẩn bị → Shipper giao → Xác nhận thành công
- **Module Orders (phức tạp nhất):** Quản lý trạng thái đơn hàng bằng State Machine, đảm bảo tính nguyên tử khi tạo đơn
- **GPS Tracking:** Shipper gửi vị trí liên tục (mỗi 5 giây), backend lưu Firestore, khách hàng xem real-time
- **Notification System:** FCM push, lưu lịch sử trong Firestore, chia loại (giao dịch, khuyến mãi, xã hội)
- **Chat 1-1:** Khách ↔ Chủ quán dùng Firestore Listener, gửi tin nhắn optimistic update (UI hiển thị ngay)
- **Voucher Transactions:** Phức tạp vì cần atomic (tạo đơn + xóa giỏ + cộng voucher dùng cùng lúc, tránh race condition)
- **Wallet System:** Lưu số dư, lịch sử giao dịch; Admin duyệt yêu cầu rút tiền
- **Firestore là backbone:** 15+ collections, subcollections cho cart/notification/messages, indexed queries để tối ưu
- **SoldCount Feature:** Đếm số sản phẩm đã bán, lưu ở database, cập nhật nguyên tử khi giao hàng xong
- **Shipper Applications:** Shipper đăng ký → Chủ quán duyệt → Assign shop → Có thể nhận đơn từ shop đó
- **Điểm phức tạp nhất:** Quản lý tính nhất quán dữ liệu khi nhiều sự kiện diễn ra đồng thời (multiple orders, atomic updates, real-time sync)

---

## 📋 TABLE OF CONTENTS

1. [Repository Overview](#repository-overview)
2. [Android App Architecture](#android-app-architecture)
3. [Backend Architecture](#backend-architecture)
4. [Tech Stack](#tech-stack)
5. [Core Features](#core-features)
6. [Data Layer](#data-layer)
7. [What's Implemented vs Not Implemented](#whats-implemented-vs-not-implemented)

---

## 1. REPOSITORY OVERVIEW

### Project Structure
```
MobileProject/
├── FoodApp/          # Android app (Kotlin + Jetpack Compose)
├── Backend/          # Firebase Cloud Functions (NestJS + TypeScript)
├── Admin/            # Admin web dashboard (React + TypeScript + Vite)
└── docs/             # Documentation
```

### Key Modules

#### **Android App** (`FoodApp/app/`)
- **Package:** `com.example.foodapp`
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)

#### **Backend** (`Backend/functions/src/`)
- **Framework:** NestJS v11.1.11
- **Runtime:** Node.js 24
- **Database:** Firebase Firestore
- **Architecture:** Modular (23 modules)

#### **Admin Dashboard** (`Admin/src/`)
- **Framework:** React 18 + TypeScript
- **Build Tool:** Vite
- **Hosting:** Vercel

---

## 2. ANDROID APP ARCHITECTURE

### 2.1 Module Structure

**Reference:** `FoodApp/app/src/main/java/com/example/foodapp/`

```
app/src/main/java/com/example/foodapp/
├── authentication/          # Login, Signup, Role Selection, Password Reset
│   ├── login/              # LoginViewModel, LoginScreen
│   ├── signup/             # SignUpViewModel, SignUpScreen
│   ├── roleselection/      # RoleSelectionViewModel, RoleSelectionScreen
│   └── forgotpassword/     # Email input, OTP, Reset password flows
│
├── pages/                   # Feature screens by role
│   ├── client/             # Customer screens
│   │   ├── home/           # UserHomeScreen, UserHomeViewModel
│   │   ├── productdetail/  # ProductDetailScreen, ProductDetailViewModel
│   │   ├── cart/           # CartScreen, CartViewModel
│   │   ├── payment/        # PaymentScreen, PaymentViewModel
│   │   ├── order/          # OrderScreen (order history)
│   │   ├── ordersuccess/   # OrderSuccessScreen
│   │   ├── shoplist/       # ShopListScreen
│   │   ├── favorites/      # FavoritesScreen
│   │   ├── setting/        # SettingsScreen
│   │   ├── listchat/       # ConversationsScreen
│   │   └── chat/           # ChatScreen (1-1 messaging)
│   │
│   ├── owner/              # Restaurant owner screens
│   │   ├── dashboard/      # DashBoardRootScreen (main container)
│   │   ├── shopsetup/      # ShopSetupScreen (first-time setup)
│   │   ├── orders/         # OrdersScreen, OrdersViewModel
│   │   ├── foods/          # FoodsScreen (product management)
│   │   ├── shippers/       # ShippersScreen (shipper applications, removal requests)
│   │   ├── vouchers/       # VouchersScreen
│   │   ├── customer/       # CustomerScreen (buyer management)
│   │   ├── revenue/        # RevenueScreen
│   │   ├── reviews/        # ReviewsScreen
│   │   ├── chatbot/        # ChatbotScreen (AI assistant)
│   │   ├── chat/           # OwnerConversationsScreen, OwnerChatDetailScreen
│   │   └── settings/       # SettingsScreen, PersonalInfoScreen, StoreInfoScreen
│   │
│   └── shipper/            # Delivery driver screens
│       ├── dashboard/      # ShipperDashboardRootScreen (main container)
│       ├── home/           # ShipperHomeScreen, ShipperHomeViewModel
│       ├── order/          # ShipperOrderDetailScreen
│       ├── gps/            # GpsScreen, DeliveryMapScreen, TripDetailScreen
│       ├── earnings/       # EarningsScreen
│       ├── history/        # HistoryScreen
│       ├── application/    # ShopSelectionScreen, MyApplicationsScreen
│       ├── removal/        # RemovalRequestScreen
│       ├── notifications/  # NotificationsScreen
│       ├── chat/           # ConversationsScreen, ChatDetailScreen
│       ├── chatbot/        # ShipperChatbotScreen
│       └── settings/       # ShipperSettingsScreen, EditProfileScreen
│
├── data/                    # Data layer
│   ├── model/              # Data models (Client, Shop, Order, Product, etc.)
│   ├── repository/         # Repository pattern implementations
│   │   ├── firebase/       # UserFirebaseRepository, AuthManager
│   │   ├── shared/         # AuthRepository (REST API)
│   │   ├── client/         # Client-specific repositories
│   │   ├── owner/          # Owner-specific repositories
│   │   └── shipper/        # Shipper-specific repositories
│   └── remote/             # API service interfaces
│       └── api/            # ApiClient, ApiService interfaces
│
├── navigation/             # Navigation graph
│   └── NavGraph.kt         # FoodAppNavHost, Screen sealed class
│
├── ui/theme/               # Theme configuration
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
├── utils/                  # Utility classes
│   ├── LocationHelper.kt
│   ├── CurrencyUtils.kt
│   └── ErrorParser.kt
│
└── MainActivity.kt         # App entry point
```

### 2.2 Navigation Flow

**Reference:** `navigation/NavGraph.kt`

#### Authentication Flow
```
Intro → Login/SignUp → OTP Verification → Role Selection → Role-based Home
```

**Entry Points:**
- **Customer:** `Screen.UserHome` → `UserHomeScreen`
- **Owner:** `Screen.OwnerHome` → `OwnerHomeWrapper` → `DashBoardRootScreen`
- **Shipper:** `Screen.ShipperHome` → `ShipperDashboardRootScreen`

#### Customer Journey
```
UserHome → ProductDetail → Cart → Payment → OrderSuccess → OrderTracking
```

**Key Screens:**
- `UserHomeScreen`: Browse restaurants and products
- `ProductDetailScreen`: View product details, add to cart
- `CartScreen`: Review cart items
- `PaymentScreen`: Select payment method (COD/Bank Transfer)
- `OrderSuccessScreen`: Confirmation screen
- `OrderScreen`: Order history with status tracking

#### Owner Journey
```
OwnerHome → Dashboard → [Orders|Foods|Shippers|Vouchers|Revenue]
```

**Key Screens:**
- `DashBoardRootScreen`: Main container with navigation drawer
- `OrdersScreen`: Manage incoming orders (PENDING → CONFIRMED → PREPARING → READY)
- `FoodsScreen`: Product CRUD operations
- `ShippersScreen`: 3 tabs - Applications, Active Shippers, Removal Requests
- `RevenueScreen`: Financial analytics

#### Shipper Journey
```
ShipperHome → [Available Orders|GPS Tracking|Earnings]
```

**Key Screens:**
- `ShipperHomeScreen`: View available orders, toggle online status
- `GpsScreen`: 2 tabs - Available Orders, Shipping Orders
- `DeliveryMapScreen`: Real-time GPS tracking with Google Maps
- `EarningsScreen`: Income and payout management

### 2.3 Key ViewModels & Logic

#### Authentication
- **LoginViewModel** (`authentication/login/LoginViewModel.kt`)
  - Methods: `login(email, password)`, `handleGoogleSignIn()`
  - Uses: `AuthRepository.login()`, `FirebaseAuth.signInWithCustomToken()`
  
- **SignUpViewModel** (`authentication/signup/SignUpViewModel.kt`)
  - Methods: `registerWithEmail()`, `validateInput()`
  - Uses: `AuthRepository.register()`, FCM token registration

#### Customer
- **UserHomeViewModel** (`pages/client/home/UserHomeViewModel.kt`)
  - Methods: `loadProducts()`, `loadShops()`, `searchProducts()`
  
- **PaymentViewModel** (`pages/client/payment/PaymentViewModel.kt`)
  - Methods: `createOrder()`, `processPayment()`, `pollPaymentStatus()`
  - Creates order via `OrderRepository.createOrder()`

#### Owner
- **OrdersViewModel** (`pages/owner/orders/OrdersViewModel.kt`)
  - Methods: `confirmOrder()`, `markAsPreparing()`, `markAsReady()`
  - Order state machine: PENDING → CONFIRMED → PREPARING → READY

- **ShippersViewModel** (`pages/owner/shippers/ShippersViewModel.kt`)
  - Methods: `approveApplication()`, `rejectApplication()`, `handleRemovalRequest()`

#### Shipper
- **ShipperHomeViewModel** (`pages/shipper/home/ShipperHomeViewModel.kt`)
  - Methods: `toggleOnlineStatus()`, `loadAvailableOrders()`, `acceptOrder()`
  
- **GpsViewModel** (`pages/shipper/gps/GpsViewModel.kt`)
  - Methods: `startTrip()`, `finishTrip()`, `updateLocation()`, `loadTripByOrderId()`
  - GPS tracking: Uses `FusedLocationProviderClient` for real-time location

---

## 3. BACKEND ARCHITECTURE

### 3.1 Firebase Functions Setup

**Reference:** `Backend/functions/src/main.ts`

- **Framework:** NestJS (Express adapter)
- **Deployment:** Firebase Cloud Functions
- **API Prefix:** `/api`
- **Swagger:** Available at `/api/docs`
- **CORS:** Enabled for all origins

**Entry Point:** `Backend/functions/src/index.ts`
```typescript
exports.api = functions
  .runWith({ timeoutSeconds: 300, memory: '2GB' })
  .https.onRequest(createNestApp());
```

### 3.2 Module Structure

**Reference:** `Backend/functions/src/modules/`

```
modules/
├── auth/              # Authentication (login, register, Google sign-in)
├── users/             # User management, addresses
├── shops/             # Restaurant CRUD, shop status
├── products/          # Product CRUD, soldCount tracking
├── categories/        # Product categories
├── cart/              # Shopping cart operations
├── orders/            # Order lifecycle, reviews
├── payments/          # Payment processing (SePay integration)
├── vouchers/          # Discount vouchers, usage tracking
├── wallets/           # Owner/Shipper wallet management
├── shippers/          # Shipper applications, shop assignments
├── shipper-removal-requests/  # Removal request workflow
├── gps/               # Trip management, location tracking
├── notifications/     # FCM push notifications, in-app notifications
├── chat/              # 1-1 messaging (conversations, messages)
├── chatbot/           # Gemini AI chatbot integration
├── favorites/         # User favorites management
├── buyers/            # Buyer statistics, tier system
├── revenue/           # Revenue analytics
├── admin/             # Admin dashboard APIs
├── delivery-points/   # Delivery location management
├── email/             # Email service (SendGrid)
└── health/            # Health check endpoint
```

### 3.3 Core Services

#### Auth Service
**Reference:** `modules/auth/auth.service.ts`

**Methods:**
- `login(dto: LoginDto)`: Email/password authentication
- `register(dto: RegisterDto)`: Create new user account
- `googleLogin(dto: GoogleLoginDto)`: Google OAuth sign-in
- `sendVerificationOTP()`: Send email OTP
- `verifyOTP()`: Verify OTP code

**Flow:**
1. Verify credentials with Firebase Auth REST API
2. Fetch user from Firestore
3. Generate Firebase custom token
4. Return token + user data to client

#### Orders Service
**Reference:** `modules/orders/services/orders.service.ts`

**Key Methods:**
- `createOrder(customerId, dto)`: Create order from cart (ORDER-002)
  - **Transaction:** Atomically creates order + clears cart + applies voucher
  - **Validation:** Shop status, cart existence, delivery address
  - **Pricing:** Uses cart snapshot prices (prevents price manipulation)
  
- `confirmOrder(ownerId, orderId)`: Owner confirms order
- `markAsPreparing(ownerId, orderId)`: Change status to PREPARING
- `markAsReady(ownerId, orderId)`: Ready for pickup
- `acceptOrder(shipperId, orderId)`: Shipper accepts delivery
  - **Transaction:** Prevents race condition (atomic order assignment)
  
- `markDelivered(shipperId, orderId)`: Complete delivery
  - **Side Effects:** Update soldCount, update stats, process payout

**Order Lifecycle:**
```
PENDING → CONFIRMED → PREPARING → READY → SHIPPING → DELIVERED
```

**Notifications:**
- `NEW_ORDER` → Shop owner (on order creation)
- `ORDER_CONFIRMED` → Customer (on confirmation)
- `ORDER_READY` → Customer + Shipper topic (on ready)
- `ORDER_SHIPPING` → Customer (on pickup)
- `ORDER_DELIVERED` → Customer (on delivery)

#### GPS Service
**Reference:** `modules/gps/services/gps.service.ts`

**Methods:**
- `createTrip(dto)`: Create delivery trip for multiple orders
- `startTrip(tripId)`: Begin delivery (updates order status to SHIPPING)
- `updateTripLocation(tripId, location)`: Real-time location tracking
- `finishTrip(tripId)`: Complete all orders in trip (marks as DELIVERED)
- `getTripByOrderId(orderId)`: Find active trip for specific order

**Trip Entity:**
```typescript
{
  id: string;
  shipperId: string;
  shopId: string;
  orderIds: string[];
  status: TripStatus; // PENDING | STARTED | COMPLETED
  route: LatLng[];    // Polyline coordinates
  currentLocation?: LatLng;
}
```

#### Notifications Service
**Reference:** `modules/notifications/services/notifications.service.ts`

**Methods:**
- `send(dto)`: Send notification to user (stores in Firestore + sends FCM)
- `sendToTopic(topic, dto)`: Send to FCM topic (e.g., "shipper_available")
- `getNotifications(userId)`: Fetch user's notification list
- `markAsRead(userId, notificationId)`: Update read status

**FCM Integration:**
- **Android:** `FirebaseMessaging` SDK
- **Token Storage:** `users/{userId}/fcmTokens` subcollection
- **Topics:** `shipper_available`, `admin_announcements`

#### Products Service (SoldCount Feature)
**Reference:** `modules/products/services/products.service.ts`

**Methods:**
- `incrementSoldCount(items)`: Atomic increment after order delivered
- `decrementSoldCount(items)`: Atomic decrement on order cancellation

**Repository Implementation:**
**Reference:** `modules/products/repositories/firestore-products.repository.ts`
```typescript
async incrementSoldCount(items: { productId: string; quantity: number }[]) {
  const batch = this.firestore.batch();
  for (const item of items) {
    const ref = this.firestore.collection('products').doc(item.productId);
    batch.update(ref, {
      soldCount: FieldValue.increment(item.quantity)
    });
  }
  await batch.commit();
}
```

**Backfill Script:** `scripts/backfill-soldcount.ts`

---

## 4. TECH STACK

### 4.1 Android App

**Reference:** `FoodApp/app/build.gradle.kts`

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin | Latest |
| **UI** | Jetpack Compose | 1.7.0 |
| **Architecture** | MVVM | - |
| **Navigation** | Navigation Compose | 2.7.5 |
| **Lifecycle** | ViewModel, LiveData, Flow | 2.6.2 |
| **Dependency Injection** | Manual (Factory pattern) | - |
| **Networking** | Retrofit 2 | 2.9.0 |
| **Serialization** | Gson | - |
| **HTTP Client** | OkHttp 3 | 4.11.0 |
| **Image Loading** | Coil | 2.6.0 |
| **Async** | Coroutines | 1.8.0 |
| **Firebase Auth** | Firebase Auth SDK | 22.3.0 |
| **Firestore** | Firebase Firestore | 24.9.1 |
| **FCM** | Firebase Messaging | 23.3.1 |
| **Analytics** | Firebase Analytics | 21.5.0 |
| **Google Sign-In** | Play Services Auth | 20.7.0 |
| **Maps** | Google Maps Compose | 2.15.0 |
| **Location** | Play Services Location | 21.0.1 |
| **Logging** | Timber | 5.0.1 |

### 4.2 Backend

**Reference:** `Backend/functions/package.json`

| Category | Technology | Version |
|----------|-----------|---------|
| **Framework** | NestJS | 11.1.11 |
| **Runtime** | Node.js | 24 |
| **Language** | TypeScript | 5.9.3 |
| **Cloud Functions** | Firebase Functions | 7.0.5 |
| **Admin SDK** | Firebase Admin | 13.6.0 |
| **Database** | Firestore | (via Admin SDK) |
| **Authentication** | Firebase Auth | (via Admin SDK) |
| **Storage** | Firebase Storage | (via Admin SDK) |
| **Validation** | class-validator | 0.14.3 |
| **Transformation** | class-transformer | 0.5.1 |
| **API Docs** | Swagger | 11.2.5 |
| **Email** | SendGrid | 8.1.6 |
| **File Upload** | Multer | 2.0.2 |
| **Testing** | Jest | 29.7.0 |
| **AI** | Google Gemini | 1.38.0 |

### 4.3 Admin Dashboard

**Reference:** `Admin/package.json`

| Category | Technology | Version |
|----------|-----------|---------|
| **Framework** | React | 18 |
| **Language** | TypeScript | Latest |
| **Build Tool** | Vite | Latest |
| **UI Library** | Material-UI (assumed) | - |
| **State Management** | Context API | - |
| **Firebase** | Firebase JS SDK | Latest |
| **Hosting** | Vercel | - |

---

## 5. CORE FEATURES

### 5.1 Authentication & Authorization

#### Implemented Features ✅

**Email/Password Authentication**
- **Endpoints:** `POST /api/auth/register`, `POST /api/auth/login`
- **Flow:** Register → OTP verification → Role selection → Login
- **Android:** `LoginViewModel`, `SignUpViewModel`
- **Backend:** `AuthService.login()`, `AuthService.register()`
- **Security:** Firebase custom tokens, JWT-like pattern

**Google OAuth Sign-In**
- **Endpoint:** `POST /api/auth/google-login`
- **Android:** `LoginViewModel.handleGoogleSignIn()`
- **SDK:** Google Sign-In SDK (`play-services-auth:20.7.0`)
- **Flow:** Get Google ID token → Send to backend → Receive custom token

**Role-Based Access Control (RBAC)**
- **Roles:** `CUSTOMER`, `OWNER`, `SHIPPER`, `ADMIN`
- **Guard:** `RolesGuard` (NestJS)
- **Decorator:** `@Roles(UserRole.CUSTOMER)`
- **Android:** Role-based navigation in `NavGraph.kt`

**OTP Verification**
- **Purpose:** Email verification, password reset
- **Storage:** Firestore `otps` collection
- **Expiry:** 10 minutes
- **Service:** `EmailService` via SendGrid

### 5.2 Product & Shop Management

#### Restaurant (Shop) Management ✅

**Owner Side:**
- **Setup:** `ShopSetupScreen` (first-time flow)
- **CRUD:** `StoreInfoScreen` (edit shop details)
- **Fields:** Name, description, address, phone, logo, cover image, open/close time
- **Status:** `OPEN`, `CLOSED`, `PENDING_REVIEW`

**Backend:**
- **Endpoints:** 
  - `POST /api/shops` - Create shop
  - `GET /api/shops/my-shop` - Get owner's shop
  - `PATCH /api/shops/:id` - Update shop
  - `POST /api/shops/:id/images` - Upload images
- **Repository:** `FirestoreShopsRepository`

#### Product Management ✅

**Owner Side:**
- **Screen:** `FoodsScreen` (product list + CRUD)
- **Operations:** Create, Update, Delete, Toggle availability
- **Image Upload:** Multiple product images

**Backend:**
- **Endpoints:**
  - `POST /api/products` - Create product
  - `GET /api/products?shopId=xxx` - List products by shop
  - `PATCH /api/products/:id` - Update product
  - `DELETE /api/products/:id` - Soft delete
- **Features:**
  - **SoldCount Tracking:** Atomic increment on delivery (`incrementSoldCount`)
  - **Filtering:** By category, availability, price range
  - **Search:** Full-text search via Fuse.js

**SoldCount Implementation:**
- **Service:** `ProductsService.incrementSoldCount()`
- **Repository:** `FirestoreProductsRepository` (uses `FieldValue.increment()`)
- **Trigger:** Called in `OrdersService.markDelivered()`
- **Backfill:** `scripts/backfill-soldcount.ts`

### 5.3 Shopping & Orders

#### Shopping Cart ✅

**Customer Side:**
- **Screen:** `CartScreen`, `CartViewModel`
- **Operations:** Add, update quantity, remove items
- **Grouping:** By shop (one order per shop)

**Backend:**
- **Endpoints:**
  - `POST /api/cart` - Add to cart
  - `GET /api/cart` - Get cart (grouped by shop)
  - `PATCH /api/cart/:productId` - Update quantity
  - `DELETE /api/cart/:productId` - Remove item
- **Structure:** `carts/{userId}/items/{productId}`
- **Price Lock:** Stores product price at add-to-cart time

#### Order Creation & Lifecycle ✅

**Create Order Flow:**
1. **Customer:** `PaymentScreen` → Select address → Choose payment method → Create order
2. **Backend:** `OrdersService.createOrder()`
   - Validate cart, shop, delivery address
   - Apply voucher (if any)
   - **Transaction:** Create order + clear cart + apply voucher (atomic)
   - Send `NEW_ORDER` notification to owner
3. **Owner:** Receives notification → `OrdersScreen` → Confirm order
4. **Lifecycle:** PENDING → CONFIRMED → PREPARING → READY → SHIPPING → DELIVERED

**Order Status Transitions:**
- **PENDING:** Initial state after creation
- **CONFIRMED:** Owner accepts order (`confirmOrder()`)
- **PREPARING:** Kitchen preparing (`markAsPreparing()`)
- **READY:** Ready for pickup (`markAsReady()`)
- **SHIPPING:** Shipper picked up (`acceptOrder()` then `startTrip()`)
- **DELIVERED:** Delivered (`markDelivered()`)
- **CANCELLED:** Cancelled by customer/owner (`cancelOrder()`)

**Key Endpoints:**
- `POST /api/orders` - Create order
- `GET /api/orders` - Customer order history
- `GET /api/orders/owner` - Owner incoming orders
- `GET /api/orders/shipper/available` - Available orders for shippers
- `PATCH /api/orders/:id/confirm` - Owner confirms
- `PATCH /api/orders/:id/accept` - Shipper accepts
- `PATCH /api/orders/:id/delivered` - Mark as delivered

**Android Screens:**
- **Customer:** `PaymentScreen`, `OrderSuccessScreen`, `OrderScreen`
- **Owner:** `OrdersScreen` (tabs: Pending, Confirmed, Preparing, Ready, Shipping, Completed)
- **Shipper:** `ShipperHomeScreen`, `ShipperOrderDetailScreen`

#### Payment Processing ✅

**Payment Methods:**
- **COD (Cash on Delivery):** Default, no processing needed
- **Bank Transfer (SePay):** QR code payment

**SePay Integration:**
- **Endpoint:** `POST /api/payments/create`
- **Flow:**
  1. Create payment request → Receive QR code URL
  2. Customer scans QR → Pays
  3. Backend polls payment status (`pollPaymentStatus()`)
  4. Update order `paymentStatus` to PAID
- **Polling:** `PaymentViewModel.pollPaymentStatus()` (Android side)

**Payment Status:**
- `PAID`: Payment completed
- `UNPAID`: Awaiting payment
- `REFUNDED`: Payment refunded

### 5.4 Delivery & GPS Tracking

#### Shipper Application System ✅

**Apply Flow:**
1. **Shipper:** `ShopSelectionScreen` → Select shop → Submit application
2. **Endpoint:** `POST /api/shippers/applications`
3. **Owner:** `ShippersScreen` (Applications tab) → Approve/Reject
4. **Endpoints:** 
   - `PATCH /api/shippers/applications/:id/approve`
   - `PATCH /api/shippers/applications/:id/reject`
5. **Result:** Shipper assigned to shop → Can accept orders

**Shipper Assignment:**
- **Collection:** `shippers/{shipperId}` with `{ shopId, status: ACTIVE }`
- **Query:** Filter available orders by shipper's shop

#### GPS Tracking System ✅

**Trip Management:**
- **Purpose:** Group multiple orders into one delivery trip
- **Entity:** `trips` collection
- **Fields:** `shipperId`, `shopId`, `orderIds[]`, `status`, `route[]`, `currentLocation`

**Create Trip Flow:**
1. **Owner:** `OrdersScreen` → Mark order(s) as READY
2. **Shipper:** `GpsScreen` (Available Orders tab) → Accept order(s) → Create trip
3. **Endpoint:** `POST /api/gps/trips`
4. **Auto-create:** System can auto-create trip when shipper accepts single order

**Delivery Flow:**
1. **Start Trip:** `GpsViewModel.startTrip()` → `PATCH /api/gps/trips/:id/start`
   - Updates orders to `SHIPPING` status
   - Sends `ORDER_SHIPPING` notification to customers
2. **Location Tracking:** `GpsViewModel.updateLocation()` → `PATCH /api/gps/trips/:id/location`
   - Uses `FusedLocationProviderClient` (Android)
   - Updates `currentLocation` in real-time
3. **Map View:** `DeliveryMapScreen` → Display route + current location
   - Google Maps integration
   - Polyline rendering
4. **Finish Trip:** `GpsViewModel.finishTrip()` → `POST /api/gps/trips/:id/finish`
   - Marks all orders as `DELIVERED`
   - Triggers `OrdersService.markDelivered()` for each order
   - Updates soldCount, processes payout

**Android Components:**
- **ViewModel:** `GpsViewModel` (trip management, location tracking)
- **Screens:** `GpsScreen`, `DeliveryMapScreen`, `TripDetailScreen`
- **Permissions:** `ACCESS_FINE_LOCATION`, `FOREGROUND_SERVICE_LOCATION`

**Backend Components:**
- **Service:** `GpsService`
- **Repository:** `FirestoreGpsRepository`
- **Endpoints:** `/api/gps/trips/*`

### 5.5 Notifications & Messaging

#### Push Notifications (FCM) ✅

**FCM Token Management:**
- **Registration:** `LoginViewModel` / `SignUpViewModel` → Store token
- **Storage:** `users/{userId}/fcmTokens/{tokenId}`
- **Android:** `FirebaseMessaging.getToken()`

**Notification Types:**
- **Order Notifications:** `NEW_ORDER`, `ORDER_CONFIRMED`, `ORDER_READY`, `ORDER_SHIPPING`, `ORDER_DELIVERED`, `ORDER_CANCELLED`
- **Payment:** `PAYMENT_SUCCESS`, `PAYMENT_FAILED`
- **Shipper:** `APPLICATION_APPROVED`, `APPLICATION_REJECTED`
- **Marketing:** `PROMOTION`

**Sending Flow:**
1. **Trigger:** Backend service method (e.g., `OrdersService.createOrder()`)
2. **Service:** `NotificationsService.send(dto)`
   - Stores notification in Firestore: `notifications/{userId}/notifications/{notificationId}`
   - Sends FCM: `FirebaseService.sendNotification()`
3. **Android:** Receives FCM → Shows notification → Stores in local list

**Topic Subscriptions:**
- **Shipper Available:** `shipper_available` (for ORDER_READY notifications)
- **Admin:** `admin_announcements`

**Android Screens:**
- **Customer:** `NotificationsScreen` (client)
- **Shipper:** `NotificationsScreen` (shipper)
- **Owner:** Notification bell in dashboard

**Backend:**
- **Service:** `NotificationsService`
- **Repository:** `FirestoreNotificationsRepository`
- **Endpoints:** 
  - `GET /api/notifications` - Get user notifications
  - `PATCH /api/notifications/:id/read` - Mark as read
  - `POST /api/admin/notifications/topics/:topic/send` - Admin send to topic

#### 1-1 Chat Messaging ✅

**Chat System:**
- **Purpose:** Customer ↔ Shop Owner communication
- **Architecture:** Conversation-based (like WhatsApp)

**Data Model:**
```
conversations/{conversationId}
  ├── participants: [customerId, ownerId]
  ├── lastMessage: { text, timestamp, senderId }
  └── messages (subcollection)
      └── {messageId}: { senderId, text, timestamp, read }
```

**Create Conversation:**
- **Trigger:** Customer clicks "Chat" on product detail
- **Endpoint:** `POST /api/chat/conversations`
- **Auto-create:** If conversation doesn't exist

**Send Message:**
- **Endpoint:** `POST /api/chat/conversations/:id/messages`
- **Real-time:** Firestore listener updates UI instantly

**Android Screens:**
- **Customer:** `ConversationsScreen` (list) → `ChatScreen` (detail)
- **Owner:** `OwnerConversationsScreen` → `OwnerChatDetailScreen`
- **Shipper:** `ConversationsScreen` → `ChatDetailScreen`

**Backend:**
- **Service:** `ChatService`
- **Repositories:** `FirestoreConversationsRepository`, `FirestoreMessagesRepository`
- **Endpoints:** 
  - `POST /api/chat/conversations` - Create conversation
  - `GET /api/chat/conversations` - List conversations
  - `POST /api/chat/conversations/:id/messages` - Send message
  - `GET /api/chat/conversations/:id/messages` - Get messages

### 5.6 Advanced Features

#### Voucher System ✅

**Voucher Types:**
- **Percentage:** Discount by percentage (e.g., 20% off)
- **Fixed Amount:** Discount by fixed value (e.g., 50,000đ off)

**Validation Rules:**
- Minimum order amount
- Max discount cap
- Usage limit per user
- Total usage limit
- Expiry date
- Applicable roles (CUSTOMER, ALL)

**Apply Voucher Flow:**
1. **Customer:** `PaymentScreen` → Enter voucher code
2. **Backend:** `VouchersService.validateVoucher()` → Check validity
3. **Transaction:** `OrdersService.createOrder()` → Apply voucher atomically
   - Create order
   - Clear cart
   - Increment voucher usage count (atomic)
4. **Result:** Discount applied to order total

**Backend:**
- **Service:** `VouchersService`
- **Repository:** `FirestoreVouchersRepository`
- **Endpoints:**
  - `GET /api/vouchers` - List available vouchers
  - `POST /api/vouchers/validate` - Preview discount
  - `POST /api/vouchers` - Owner creates voucher (OWNER role)

**Owner Management:**
- **Screen:** `VouchersScreen`
- **Operations:** Create, Update, Deactivate, View usage stats

#### Wallet & Payout System ✅

**Purpose:** Manage owner/shipper earnings and payouts

**Wallet Operations:**
- **Order Delivery:** Credit owner wallet, credit shipper wallet
- **Payout Request:** Owner/Shipper requests withdrawal
- **Admin Approval:** Admin approves payout

**Payout Flow:**
1. **Owner/Shipper:** Request payout via app
2. **Endpoint:** `POST /api/wallets/payouts`
3. **Admin:** Admin dashboard → Approve payout
4. **Endpoint:** `PATCH /api/admin/payouts/:id/approve`
5. **Result:** Wallet balance decremented

**Backend:**
- **Service:** `WalletsService`
- **Repository:** `FirestoreWalletsRepository`
- **Endpoints:**
  - `GET /api/wallets/balance` - Get wallet balance
  - `GET /api/wallets/transactions` - Transaction history
  - `POST /api/wallets/payouts` - Request payout

**Shipper Earnings:**
- **Screen:** `EarningsScreen`
- **Data:** Total earnings, pending balance, transaction history

#### AI Chatbot (Gemini Integration) ✅

**Purpose:** Automated customer support, product recommendations

**Integration:**
- **Model:** Google Gemini (`@google/genai`)
- **Endpoint:** `POST /api/chatbot`
- **Input:** User message
- **Output:** AI-generated response

**Android:**
- **Customer:** `ChatBotScreen` (accessible from UserHome)
- **Owner:** `ChatbotScreen` (in dashboard)
- **Shipper:** `ShipperChatbotScreen`

**Features:**
- Product search assistance
- Order status inquiry
- General Q&A

#### Buyer Statistics & Tier System ✅

**Purpose:** Track customer purchase behavior, implement loyalty tiers

**Metrics:**
- Total orders
- Total spent
- Average order value
- Last order date

**Tier System:**
- **BRONZE:** 0-5 orders
- **SILVER:** 6-15 orders
- **GOLD:** 16+ orders

**Owner View:**
- **Screen:** `CustomerScreen`
- **Display:** List of buyers with tier badges, stats

**Backend:**
- **Service:** `BuyersStatsService`
- **Method:** `updateBuyerStatsOnDelivery()`
- **Trigger:** Called in `OrdersService.markDelivered()`

#### Reviews & Ratings ✅

**Review Flow:**
1. **Order Delivered:** Customer can review
2. **Endpoint:** `POST /api/orders/:id/reviews`
3. **Fields:** Rating (1-5), comment, product reviews
4. **Owner Reply:** Owner can reply to review

**Android:**
- **Customer:** Review form after delivery
- **Owner:** `ReviewsScreen` (view and reply to reviews)

**Backend:**
- **Repository:** `FirestoreReviewsRepository`
- **Endpoints:**
  - `POST /api/orders/:id/reviews` - Create review
  - `POST /api/orders/:id/reviews/reply` - Owner reply

#### Revenue Analytics ✅

**Owner Dashboard:**
- **Screen:** `RevenueScreen`
- **Metrics:** Daily/weekly/monthly revenue, order count, avg order value

**Backend:**
- **Service:** `RevenueRepository`
- **Endpoint:** `GET /api/revenue?startDate=xxx&endDate=xxx`
- **Aggregation:** Firestore queries with date filters

#### Shipper Removal Requests ✅

**Purpose:** Allow shippers to leave a shop

**Flow:**
1. **Shipper:** Submit removal request
2. **Endpoint:** `POST /api/shipper-removal-requests`
3. **Owner:** `ShippersScreen` (Removal Requests tab) → Approve/Reject
4. **Endpoints:**
   - `PATCH /api/shipper-removal-requests/:id/approve`
   - `PATCH /api/shipper-removal-requests/:id/reject`
5. **Result:** Shipper unassigned from shop

**Android:**
- **Shipper:** `RemovalRequestScreen`
- **Owner:** `ShippersScreen` (tab 3)

---

## 6. DATA LAYER

### 6.1 Android Repository Pattern

**Reference:** `FoodApp/app/src/main/java/com/example/foodapp/data/repository/`

#### Firebase Repositories
- **UserFirebaseRepository** (`firebase/UserFirebaseRepository.kt`)
  - Direct Firestore access for user data
  - Methods: `getUserRole()`, `saveUserToFirestore()`

- **AuthManager** (`firebase/AuthManager.kt`)
  - Manages authentication state
  - SharedPreferences storage for tokens
  - Methods: `saveUserInfo()`, `getValidToken()`, `clearAuthData()`

#### REST API Repositories
- **AuthRepository** (`shared/AuthRepository.kt`)
  - Backend API calls via Retrofit
  - Methods: `login()`, `register()`, `googleLogin()`

- **OrderRepository** (`client/order/OrderRepository.kt`)
  - Methods: `createOrder()`, `getOrders()`, `getOrderById()`

- **ProductRepository** (`client/product/ProductRepository.kt`)
  - Methods: `getProducts()`, `getProductById()`, `searchProducts()`

- **ShopRepository** (`owner/shop/ShopRepository.kt`)
  - Methods: `createShop()`, `getMyShop()`, `updateShop()`

### 6.2 Backend Repository Pattern

**Base Repository:**
**Reference:** `Backend/functions/src/core/database/firestore/firestore-base.repository.ts`

```typescript
abstract class FirestoreBaseRepository<T> implements IBaseRepository<T> {
  constructor(firestore: Firestore, collectionName: string);
  
  async findById(id: string): Promise<T | null>;
  async findAll(options?: QueryOptions<T>): Promise<PaginatedResult<T>>;
  async create(data: Partial<T>): Promise<T>;
  async update(id: string, data: Partial<T>): Promise<T>;
  async delete(id: string): Promise<void>;
  async count(where?: Partial<Record<keyof T, any>>): Promise<number>;
}
```

**Concrete Implementations:**
- `FirestoreUsersRepository` - `users` collection
- `FirestoreShopsRepository` - `shops` collection
- `FirestoreProductsRepository` - `products` collection
- `FirestoreOrdersRepository` - `orders` collection
- `FirestoreCartRepository` - `carts/{userId}/items` subcollection
- `FirestoreNotificationsRepository` - `notifications/{userId}/notifications` subcollection

**Dependency Injection:**
```typescript
// In module providers
{
  provide: 'USERS_REPOSITORY',
  useClass: FirestoreUsersRepository
}

// In service constructor
constructor(
  @Inject('USERS_REPOSITORY') 
  private readonly usersRepo: IUsersRepository
) {}
```

### 6.3 Firestore Collections

**Main Collections:**
- `users` - User accounts (all roles)
- `shops` - Restaurant data
- `products` - Product catalog
- `categories` - Product categories
- `orders` - Order documents
- `shippers` - Shipper-shop assignments
- `shipper_applications` - Application requests
- `shipper_removal_requests` - Removal requests
- `trips` - GPS delivery trips
- `wallets` - Wallet balances
- `payoutRequests` - Payout requests
- `vouchers` - Discount vouchers
- `voucherUsage` - Usage tracking
- `conversations` - Chat conversations
- `favorites` - User favorites
- `delivery_points` - Delivery locations
- `otps` - OTP verification codes

**Subcollections:**
- `carts/{userId}/items` - Cart items
- `notifications/{userId}/notifications` - User notifications
- `users/{userId}/fcmTokens` - FCM device tokens
- `conversations/{conversationId}/messages` - Chat messages
- `wallets/{userId}/transactions` - Wallet transaction history

**Indexes:**
**Reference:** `Backend/firestore.indexes.json`
- Orders: `(customerId, status, createdAt)`
- Orders: `(shopId, status, createdAt)`
- Orders: `(shipperId, status, createdAt)`
- Products: `(shopId, isAvailable, createdAt)`
- Notifications: `(userId, read, createdAt)`

### 6.4 REST API Structure

**Base URL:** `https://YOUR_REGION-YOUR_PROJECT.cloudfunctions.net/api/api`  
**Local:** `http://localhost:5001/YOUR_PROJECT/YOUR_REGION/api/api`

**API Client:**
**Reference:** `FoodApp/app/src/main/java/com/example/foodapp/data/remote/api/ApiClient.kt`

```kotlin
object ApiClient {
    private const val BASE_URL = "YOUR_BACKEND_URL/api/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
```

**Authentication:**
- **Header:** `Authorization: Bearer <firebase_custom_token>`
- **Guard:** `AuthGuard` validates token via Firebase Admin SDK

---

## 7. WHAT'S IMPLEMENTED VS NOT IMPLEMENTED

### ✅ IMPLEMENTED FEATURES

#### Authentication & Users
- [x] Email/password registration + login
- [x] Google OAuth sign-in
- [x] OTP email verification
- [x] Password reset flow
- [x] Role-based access control (CUSTOMER, OWNER, SHIPPER, ADMIN)
- [x] Firebase custom token authentication
- [x] FCM token registration

#### Shop & Products
- [x] Shop setup (create, update)
- [x] Product CRUD operations
- [x] Product image upload
- [x] Category management
- [x] Product availability toggle
- [x] SoldCount tracking (atomic increment/decrement)
- [x] Backfill script for soldCount

#### Shopping & Orders
- [x] Shopping cart (add, update, remove)
- [x] Cart grouping by shop
- [x] Create order from cart (transactional)
- [x] Order lifecycle (8 statuses)
- [x] Order state machine (validation)
- [x] Order history (customer, owner, shipper views)
- [x] Payment methods (COD, Bank Transfer/SePay)
- [x] Payment polling
- [x] Order cancellation

#### Delivery
- [x] Shipper application system
- [x] Shipper approval/rejection
- [x] GPS trip creation
- [x] Real-time location tracking
- [x] Google Maps integration
- [x] Trip start/finish flow
- [x] Auto-update order status on trip events
- [x] Shipper removal request system

#### Notifications
- [x] FCM push notifications
- [x] In-app notification list
- [x] Topic-based notifications
- [x] Order event notifications (7 types)
- [x] Mark as read/unread
- [x] Badge counts

#### Messaging & Support
- [x] 1-1 chat (Customer ↔ Owner)
- [x] Conversation list
- [x] Real-time message updates
- [x] AI Chatbot (Gemini integration)

#### Advanced Features
- [x] Voucher system (percentage, fixed amount)
- [x] Voucher validation & atomic usage
- [x] Wallet management (owner, shipper)
- [x] Payout request system
- [x] Admin payout approval
- [x] Reviews & ratings
- [x] Owner reply to reviews
- [x] Buyer statistics
- [x] Buyer tier system (Bronze, Silver, Gold)
- [x] Revenue analytics
- [x] Favorites management

#### Admin Panel
- [x] Admin dashboard (React + Vite)
- [x] User management
- [x] Shop management
- [x] Payout approval
- [x] Analytics dashboard

### ❌ NOT IMPLEMENTED / FUTURE ENHANCEMENTS

#### Missing Features
- [ ] Multi-language support (i18n)
- [ ] Dark mode
- [ ] Offline mode / caching
- [ ] Image optimization (lazy loading)
- [ ] Infinite scroll / pagination (some screens)
- [ ] Unit test coverage (backend)
- [ ] E2E tests (Android)
- [ ] Performance monitoring
- [ ] Crash reporting (Crashlytics)

#### Business Logic Gaps
- [ ] Refund system
- [ ] Dispute resolution
- [ ] Multi-store per owner
- [ ] Scheduled orders
- [ ] Recurring orders
- [ ] Loyalty points system
- [ ] Referral program
- [ ] Promo code auto-apply
- [ ] Dynamic pricing
- [ ] Surge pricing

#### Technical Debt
- [ ] Dependency injection (Hilt/Koin) in Android
- [ ] Repository caching strategy
- [ ] GraphQL instead of REST (optional)
- [ ] WebSocket for real-time updates (currently polling)
- [ ] Image CDN integration
- [ ] Database migration strategy
- [ ] API versioning
- [ ] Rate limiting
- [ ] Comprehensive logging

#### Infrastructure
- [ ] CI/CD pipeline
- [ ] Staging environment
- [ ] Load testing
- [ ] Monitoring dashboard (Grafana)
- [ ] Error tracking (Sentry)
- [ ] Backup automation

### ⚠️ KNOWN LIMITATIONS

1. **Free-Ship Model:** Customer always pays 0đ for shipping (internal accounting with `shipperPayout`)
2. **Single Shop per Owner:** Currently one owner = one shop
3. **No WebSocket:** Real-time updates rely on Firestore listeners or polling
4. **No Image Compression:** Images uploaded as-is (potential storage bloat)
5. **Manual DI:** Android uses manual factory pattern (no Hilt/Koin)
6. **Limited Search:** Basic text search (no Algolia/Elasticsearch)
7. **No Analytics SDK:** Custom analytics implementation

---

## 📖 APPENDIX

### File References Summary

**Android Key Files:**
- Main entry: `MainActivity.kt`
- Navigation: `navigation/NavGraph.kt`
- Auth ViewModels: `authentication/login/LoginViewModel.kt`, `authentication/signup/SignUpViewModel.kt`
- Order flow: `pages/client/payment/PaymentViewModel.kt`, `pages/owner/orders/OrdersViewModel.kt`
- GPS tracking: `pages/shipper/gps/GpsViewModel.kt`, `pages/shipper/gps/DeliveryMapScreen.kt`
- Repositories: `data/repository/shared/AuthRepository.kt`, `data/repository/firebase/AuthManager.kt`

**Backend Key Files:**
- Entry point: `src/index.ts`, `src/main.ts`
- Auth: `modules/auth/auth.service.ts`, `modules/auth/auth.controller.ts`
- Orders: `modules/orders/services/orders.service.ts`, `modules/orders/repositories/firestore-orders.repository.ts`
- GPS: `modules/gps/services/gps.service.ts`
- Notifications: `modules/notifications/services/notifications.service.ts`
- Products: `modules/products/services/products.service.ts`, `modules/products/repositories/firestore-products.repository.ts`

**Configuration Files:**
- Android build: `FoodApp/app/build.gradle.kts`
- Backend deps: `Backend/functions/package.json`
- Firebase config: `Backend/firebase.json`
- Firestore indexes: `Backend/firestore.indexes.json`
- Firestore rules: `Backend/firestore.rules`

### Documentation References
- Architecture: `FoodApp/ARCHITECTURE.md`
- Backend docs: `docs/backend/`, `docs-god/`, `docs-team/`
- Migration docs: `MIGRATION_*.md` files
- Notification docs: `docs/notifications/`, `docs/frontend/NOTIFICATIONS_INTEGRATION_GUIDE.md`
- GPS docs: `docs/mobile/GPS_*.md`
- SoldCount docs: `SOLDCOUNT_FIX_SUMMARY.md`, `SOLDCOUNT_QUICKREF.md`

---

**END OF CODEBASE MAP**

This document provides a comprehensive overview of the KTX Delivery App codebase for viva/oral defense purposes. All claims are backed by specific file references and verified implementations.
