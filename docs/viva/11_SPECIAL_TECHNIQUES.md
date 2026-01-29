# 11. Kỹ thuật đặc biệt đã sử dụng (Special Techniques)

> **Mục đích:** Tài liệu tổng hợp các kỹ thuật nâng cao mà team đã triển khai trong dự án KTX Delivery.  
> **Dành cho:** Bảo vệ đồ án – chứng minh năng lực kỹ thuật với giảng viên.  
> **Nguyên tắc:** Chỉ liệt kê những kỹ thuật có bằng chứng trong code thực tế.

---

## A. Frontend (Android Kotlin + Jetpack Compose)

### A.1 GPS Tracking với Kotlin Flow + FusedLocationProviderClient

- **Mô tả ngắn:** Hệ thống theo dõi vị trí shipper real-time sử dụng Google Play Services Location API, kết hợp với Kotlin `callbackFlow` để stream dữ liệu vị trí liên tục.

- **Vì sao "đặc biệt" / giá trị:**
  - Tối ưu pin: Chỉ request location mỗi 5 giây + khoảng cách tối thiểu 5 mét
  - Memory-safe: Sử dụng `awaitClose` để tự động cleanup khi Flow bị cancel
  - Reactive: Vị trí mới tự động đẩy qua Flow → ViewModel → UI

- **Cách triển khai trong project:**
  1. `LocationHelper` wrap `FusedLocationProviderClient` thành `Flow<Location>`
  2. `GpsViewModel` collect Flow và update `StateFlow<TripLocation>`
  3. UI (DeliveryMapScreen) observe state và cập nhật marker trên Google Maps

- **Bằng chứng trong code:**
  - [FoodApp/app/.../utils/LocationHelper.kt](FoodApp/app/src/main/java/com/example/foodapp/utils/LocationHelper.kt) — `LocationHelper.getLocationUpdates()` (lines 50-82)
  - [FoodApp/app/.../pages/shipper/gps/GpsViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/gps/GpsViewModel.kt) — `startLocationTracking()` (lines 565-598)

- **Gợi ý demo (30–60s):**
  1. Mở app shipper → GPS screen
  2. Start trip → thấy marker di chuyển theo vị trí thực
  3. Log location updates trong Logcat mỗi 5s

---

### A.2 Optimistic UI Pattern cho Chat

- **Mô tả ngắn:** Khi người dùng gửi tin nhắn, UI hiển thị tin nhắn ngay lập tức (với status "SENDING") mà không đợi server response. Sau khi server confirm, thay thế bằng message thật.

- **Vì sao "đặc biệt" / giá trị:**
  - UX mượt mà: User thấy tin nhắn ngay, không có cảm giác lag
  - Xử lý lỗi graceful: Nếu send fail, đánh dấu message FAILED hoặc remove khỏi list
  - Áp dụng pattern phổ biến trong các ứng dụng chat lớn (Facebook Messenger, WhatsApp)

- **Cách triển khai trong project:**
  1. Tạo `tempMessage` với ID tạm `temp_{timestamp}`
  2. Thêm vào `_uiState.messages` ngay lập tức
  3. Gửi API → onSuccess: replace temp với real message; onFailure: remove hoặc mark failed

- **Bằng chứng trong code:**
  - [FoodApp/app/.../pages/shipper/chat/ChatDetailViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/chat/ChatDetailViewModel.kt) — `sendMessage()` với optimistic pattern (lines 110-148)
  - [FoodApp/app/.../pages/shipper/chat/ChatUiState.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/chat/ChatUiState.kt) — `isSending` flag (line 26)

- **Gợi ý demo (30–60s):**
  1. Mở conversation giữa Shipper ↔ Customer
  2. Gửi tin nhắn → thấy hiện ngay với loading indicator
  3. Sau 1-2s, indicator biến mất = đã gửi thành công

---

### A.3 StateFlow + UiState Pattern (Unidirectional Data Flow)

- **Mô tả ngắn:** Toàn bộ ViewModel sử dụng `MutableStateFlow<UiState>` làm single source of truth. UI chỉ observe state và dispatch events, không trực tiếp modify data.

- **Vì sao "đặc biệt" / giá trị:**
  - Predictable: Mọi thay đổi UI đều đi qua state
  - Testable: ViewModel logic có thể unit test độc lập
  - Compose-friendly: `collectAsState()` tự động recompose khi state thay đổi
  - Tránh memory leak: StateFlow tự động lifecycle-aware

- **Cách triển khai trong project:**
  ```kotlin
  data class XxxUiState(
      val isLoading: Boolean = false,
      val data: List<Item> = emptyList(),
      val error: String? = null
  )
  
  class XxxViewModel : ViewModel() {
      private val _uiState = MutableStateFlow(XxxUiState())
      val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
      
      fun loadData() {
          viewModelScope.launch {
              _uiState.update { it.copy(isLoading = true) }
              // ... fetch data
              _uiState.update { it.copy(data = result, isLoading = false) }
          }
      }
  }
  ```

- **Bằng chứng trong code:**
  - [FoodApp/app/.../pages/shipper/settings/ShipperSettingsViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/settings/ShipperSettingsViewModel.kt) — `ShipperSettingsUiState` + `_uiState` (lines 15-28)
  - [FoodApp/app/.../pages/shipper/gps/GpsViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/gps/GpsViewModel.kt) — `GpsUiState` + state management
  - [FoodApp/app/.../pages/owner/customer/CustomerViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/owner/customer/CustomerViewModel.kt) — Debounce search với coroutine (lines 102-110)

- **Gợi ý demo (30–60s):**
  1. Mở bất kỳ màn hình nào (VD: Shipper GPS)
  2. Pull to refresh → loading state → data state
  3. Disconnect network → error state hiện

---

### A.4 Sealed Class Navigation với Type-Safe Arguments

- **Mô tả ngắn:** Navigation Graph sử dụng `sealed class Screen` để định nghĩa tất cả routes, với type-safe argument encoding (URL encode JSON).

- **Vì sao "đặc biệt" / giá trị:**
  - Compile-time safety: Không thể navigate đến route không tồn tại
  - Centralized: Tất cả routes định nghĩa ở một file
  - Complex data passing: Hỗ trợ truyền object phức tạp qua JSON encoding

- **Cách triển khai trong project:**
  ```kotlin
  sealed class Screen(val route: String) {
      object UserHome : Screen("user_home")
      object OrderSuccess : Screen("order_success/{orderJson}") {
          fun createRoute(orderJson: String): String {
              val encoded = URLEncoder.encode(orderJson, "UTF-8")
              return "order_success/$encoded"
          }
      }
  }
  ```

- **Bằng chứng trong code:**
  - [FoodApp/app/.../navigation/NavGraph.kt](FoodApp/app/src/main/java/com/example/foodapp/navigation/NavGraph.kt) — `sealed class Screen` (lines 56-115)
  - [FoodApp/app/.../pages/shipper/dashboard/ShipperDashboardRootScreen.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/dashboard/ShipperDashboardRootScreen.kt) — Nested NavHost (line 85)

- **Gợi ý demo (30–60s):**
  1. Giải thích cấu trúc sealed class
  2. Show navigation flow: Login → RoleSelection → Home theo role

---

### A.5 Debounce Search với Coroutine Delay

- **Mô tả ngắn:** Khi user typing trong search box, không gọi API ngay mà đợi 300ms sau keystroke cuối cùng mới gọi.

- **Vì sao "đặc biệt" / giá trị:**
  - Giảm tải server: Không gọi API mỗi ký tự
  - Better UX: Không bị flickering kết quả liên tục
  - Simple implementation: Chỉ cần `delay()` trong coroutine

- **Cách triển khai trong project:**
  ```kotlin
  fun onSearchQueryChanged(query: String) {
      searchJob?.cancel()
      searchJob = viewModelScope.launch {
          delay(300) // Debounce 300ms
          performSearch(query)
      }
  }
  ```

- **Bằng chứng trong code:**
  - [FoodApp/app/.../pages/client/home/HomeViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/client/home/HomeViewModel.kt) — Debounce 300ms (line 283)
  - [FoodApp/app/.../pages/owner/customer/CustomerViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/owner/customer/CustomerViewModel.kt) — Search với debounce (lines 102-110)

- **Gợi ý demo (30–60s):**
  1. Mở User Home → Search bar
  2. Type nhanh "bún" → thấy không gọi API liên tục
  3. Dừng lại → 300ms sau mới thấy kết quả

---

## B. Backend (NestJS + Firebase Cloud Functions + Firestore)

### B.1 Fuzzy Search với Fuse.js + Vietnamese Normalization

- **Mô tả ngắn:** Tìm kiếm sản phẩm hỗ trợ typo tolerance và Vietnamese diacritics. User gõ "bun bo" có thể tìm thấy "Bún bò Huế".

- **Vì sao "đặc biệt" / giá trị:**
  - Typo tolerance: threshold 0.4 cho phép sai vài ký tự
  - Vietnamese support: Normalize "Bún" → "bun" để matching
  - Weighted search: name (40%) > description (10%)
  - In-memory với caching: Nhanh, không query Firestore mỗi request

- **Cách triển khai trong project:**
  1. Build search index từ Firestore (cached 2 phút)
  2. Normalize Vietnamese diacritics
  3. Fuse.js search với weighted keys
  4. Return sorted by score

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/products/services/product-search.service.ts](Backend/functions/src/modules/products/services/product-search.service.ts) — `ProductSearchService` với Fuse.js (lines 1-100)
  - [Backend/functions/src/modules/products/controllers/search.controller.ts](Backend/functions/src/modules/products/controllers/search.controller.ts) — API endpoint `/products/search` (lines 8-24)

- **Gợi ý demo (30–60s):**
  1. Swagger UI → GET /products/search?q=bun
  2. Thử search "pho" → tìm thấy "Phở bò"
  3. Thử typo "phs" → vẫn match được

---

### B.2 In-Memory TTL Cache với Auto-Invalidation

- **Mô tả ngắn:** Simple cache utility lưu data trong memory với TTL expiration, tự động invalidate khi data thay đổi.

- **Vì sao "đặc biệt" / giá trị:**
  - Giảm Firestore reads: Menu/FAQ không cần query mỗi request
  - Configurable TTL: Menu 2 phút, FAQ 5 phút
  - Pattern-based invalidation: `invalidateByPrefix("shop:123:")` xóa tất cả cache của shop
  - Zero dependencies: Không cần Redis, chạy trong Cloud Function memory

- **Cách triển khai trong project:**
  ```typescript
  class SimpleCache {
      private cache = new Map<string, CacheEntry>();
      
      get<T>(key: string): T | null { /* check expiry */ }
      set<T>(key: string, value: T, ttlMs: number): void { /* store with expiry */ }
      invalidateByPrefix(prefix: string): number { /* bulk delete */ }
  }
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/shared/utils/simple-cache.util.ts](Backend/functions/src/shared/utils/simple-cache.util.ts) — `SimpleCache` class (lines 1-100)
  - [Backend/functions/src/modules/products/services/product-search.service.ts](Backend/functions/src/modules/products/services/product-search.service.ts) — Sử dụng `globalCache` (line 4)
  - [Backend/functions/src/modules/chatbot/services/chatbot.service.ts](Backend/functions/src/modules/chatbot/services/chatbot.service.ts) — FAQ context cache (line 159-175)

- **Gợi ý demo (30–60s):**
  1. Gọi API 2 lần liên tiếp
  2. Logs hiện "Cache HIT" lần 2
  3. Đợi hết TTL → "Cache EXPIRED"

---

### B.3 Chatbot AI với Gemini + Rate Limiting + FAQ Context

- **Mô tả ngắn:** Chatbot hỗ trợ khách hàng sử dụng Google Gemini AI, với FAQ context injection và rate limiting để tránh abuse.

- **Vì sao "đặc biệt" / giá trị:**
  - AI-powered: Trả lời câu hỏi bằng ngôn ngữ tự nhiên
  - Context-aware: Inject FAQ vào prompt để trả lời chính xác hơn
  - Rate limiting: Max 10 requests/phút/user (tránh abuse + tiết kiệm API cost)
  - Graceful degradation: Nếu Gemini fail, trả lời fallback

- **Cách triển khai trong project:**
  1. Load FAQ từ Firestore (cached 5 phút)
  2. Check rate limit (min 6s giữa requests)
  3. Build system prompt với FAQ context
  4. Call Gemini API → return response

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/chatbot/services/chatbot.service.ts](Backend/functions/src/modules/chatbot/services/chatbot.service.ts) — Full chatbot service (lines 1-210)
    - Rate limiting: `checkRateLimit()` (lines 82-93)
    - Gemini integration: `chat()` (lines 98-148)
    - FAQ context: `getFaqContext()` (lines 155-175)

- **Gợi ý demo (30–60s):**
  1. Mở Owner Chatbot screen
  2. Hỏi "Cách thêm sản phẩm mới?" → AI trả lời từ FAQ
  3. Spam nhiều message → bị rate limit

---

### B.4 Atomic Transactions cho Order + Voucher + SoldCount

- **Mô tả ngắn:** Khi tạo order, sử dụng Firestore Transaction để đảm bảo: tạo order + xóa cart + apply voucher + update soldCount diễn ra atomically.

- **Vì sao "đặc biệt" / giá trị:**
  - Data integrity: Không thể có order mà cart chưa xóa, hoặc voucher đã dùng mà order fail
  - Race condition prevention: 2 shipper accept cùng order → chỉ 1 thành công
  - Atomic counters: `FieldValue.increment()` đảm bảo soldCount chính xác dù concurrent updates

- **Cách triển khai trong project:**
  ```typescript
  await this.firestore.runTransaction(async (transaction) => {
      // READ phase
      const order = await transaction.get(orderRef);
      // WRITE phase
      transaction.update(orderRef, { ... });
      transaction.update(productRef, { soldCount: FieldValue.increment(qty) });
  });
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/orders/repositories/firestore-orders.repository.ts](Backend/functions/src/modules/orders/repositories/firestore-orders.repository.ts) — `acceptOrderAtomically()` (lines 215-290)
  - [Backend/functions/src/modules/products/repositories/firestore-products.repository.ts](Backend/functions/src/modules/products/repositories/firestore-products.repository.ts) — `incrementSoldCount()` với batch (lines 285-300)
  - [Backend/functions/src/modules/wallets/wallets.service.ts](Backend/functions/src/modules/wallets/wallets.service.ts) — Wallet payout transaction (lines 143-250)

- **Gợi ý demo (30–60s):**
  1. Giải thích flow: Order create → voucher applied → cart cleared
  2. Nếu voucher fail → toàn bộ rollback
  3. Show soldCount increment sau khi giao hàng

---

### B.5 Order State Machine Pattern

- **Mô tả ngắn:** Trạng thái đơn hàng được quản lý bằng State Machine, chỉ cho phép transitions hợp lệ (VD: PENDING → CONFIRMED, không thể PENDING → DELIVERED).

- **Vì sao "đặc biệt" / giá trị:**
  - Business logic protection: Không thể skip trạng thái
  - Clear documentation: Transition rules định nghĩa rõ ràng
  - Testable: Unit test từng transition

- **Cách triển khai trong project:**
  ```typescript
  const transitions = new Map([
      [PENDING, [CONFIRMED, CANCELLED]],
      [CONFIRMED, [PREPARING, CANCELLED]],
      [PREPARING, [READY, CANCELLED]],
      [READY, [SHIPPING]],
      [SHIPPING, [DELIVERED]],
      [DELIVERED, []],
      [CANCELLED, []],
  ]);
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/orders/services/order-state-machine.service.ts](Backend/functions/src/modules/orders/services/order-state-machine.service.ts) — Full state machine (lines 1-55)
  - [Backend/functions/src/modules/gps/services/gps.service.ts](Backend/functions/src/modules/gps/services/gps.service.ts) — Trip state validation (lines 535, 579)

- **Gợi ý demo (30–60s):**
  1. Tạo order → PENDING
  2. Owner confirm → CONFIRMED
  3. Thử gọi API mark as DELIVERED → Lỗi "Invalid transition"

---

### B.6 FCM Push Notifications với Best-Effort + History Persistence

- **Mô tả ngắn:** Notification system đảm bảo: (1) FCM push best-effort (nếu fail vẫn tiếp tục), (2) LUÔN lưu history vào Firestore dù push thành công hay thất bại.

- **Vì sao "đặc biệt" / giá trị:**
  - Reliability: User không bỏ lỡ thông báo quan trọng (có thể xem trong history)
  - Best-effort delivery: FCM failure không block business logic
  - Debugging: Lưu `deliveryStatus`, `deliveryErrorCode` để troubleshoot

- **Cách triển khai trong project:**
  ```typescript
  async send(options): Promise<NotificationEntity> {
      try {
          await this.fcmService.sendToTokens(tokens, payload);
      } catch (error) {
          deliveryStatus = 'FAILED';
          // Continue - don't throw
      }
      // ALWAYS save to history
      return this.notificationsRepository.create(userId, { ..., deliveryStatus });
  }
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/notifications/services/notifications.service.ts](Backend/functions/src/modules/notifications/services/notifications.service.ts) — `send()` method (lines 44-117)
  - [Backend/functions/src/modules/notifications/services/fcm.service.ts](Backend/functions/src/modules/notifications/services/fcm.service.ts) — FCM integration (lines 1-80)

- **Gợi ý demo (30–60s):**
  1. Tạo order → Owner nhận push notification
  2. Mở Notification screen → thấy trong history
  3. Giải thích: Nếu FCM fail, vẫn thấy trong history

---

### B.7 Role-Based Access Control (RBAC) với Guards

- **Mô tả ngắn:** Backend sử dụng NestJS Guards để kiểm tra: (1) AuthGuard - user đã login, (2) RolesGuard - user có role phù hợp.

- **Vì sao "đặc biệt" / giá trị:**
  - Security layer: Không thể access API không được phép
  - Declarative: Chỉ cần decorator `@Roles(UserRole.OWNER)`
  - Centralized: Logic auth/authz tập trung, không scattered

- **Cách triển khai trong project:**
  ```typescript
  @Controller('owner/orders')
  @UseGuards(AuthGuard, RolesGuard)
  @Roles(UserRole.OWNER)
  export class OwnerOrdersController { ... }
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/core/guards/roles.guard.ts](Backend/functions/src/core/guards/roles.guard.ts) — `RolesGuard` implementation (lines 1-46)
  - [Backend/functions/src/modules/vouchers/controllers/owner-vouchers.controller.ts](Backend/functions/src/modules/vouchers/controllers/owner-vouchers.controller.ts) — `@Roles(UserRole.OWNER)` (lines 40-41)
  - [Backend/functions/src/modules/wallets/controllers/wallets.controller.ts](Backend/functions/src/modules/wallets/controllers/wallets.controller.ts) — `@Roles(UserRole.OWNER, UserRole.SHIPPER)` (line 32)

- **Gợi ý demo (30–60s):**
  1. Swagger → Gọi API owner với token customer → 403 Forbidden
  2. Giải thích guard flow: Token → AuthGuard → RolesGuard → Controller

---

### B.8 SePay Payment Integration với QR Code Generation

- **Mô tả ngắn:** Tích hợp thanh toán qua SePay: Generate QR code chứa thông tin chuyển khoản, customer scan và thanh toán.

- **Vì sao "đặc biệt" / giá trị:**
  - Real payment flow: Không chỉ demo, có thể nhận tiền thật
  - Standardized: Theo chuẩn VietQR
  - Secure: Content có format deterministic, dễ verify

- **Cách triển khai trong project:**
  1. Validate SePay config từ environment
  2. Generate payment content: `KTX{orderNumber}`
  3. Build QR URL với bank code, account, amount
  4. Return QR URL cho client hiển thị

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/payments/payments.service.ts](Backend/functions/src/modules/payments/payments.service.ts) — SePay integration (lines 89-200)
  - [Backend/functions/src/shared/constants/payment-methods.ts](Backend/functions/src/shared/constants/payment-methods.ts) — Payment method enum (lines 14-15)

- **Gợi ý demo (30–60s):**
  1. Customer checkout → chọn Bank Transfer
  2. Hiển thị QR code
  3. Scan bằng app ngân hàng → thấy thông tin chính xác

---

## C. Cross-cutting (Security / Reliability / Performance / Architecture)

### C.1 Kiến trúc MVVM + Repository Pattern

- **Mô tả ngắn:** Android app theo kiến trúc MVVM: View (Compose) → ViewModel (State + Logic) → Repository (Data) → API/Firestore.

- **Vì sao "đặc biệt" / giá trị:**
  - Separation of Concerns: UI không biết data từ đâu
  - Testability: ViewModel test được với mock repository
  - Scalability: Dễ thay đổi data source (VD: cache layer)

- **Cách triển khai trong project:**
  ```
  UI (Screen) 
    ↓ observe StateFlow
  ViewModel 
    ↓ call repository methods
  Repository 
    ↓ call API / Firestore
  Remote Data Source
  ```

- **Bằng chứng trong code:**
  - [FoodApp/app/.../data/repository/](FoodApp/app/src/main/java/com/example/foodapp/data/repository/) — Repository implementations
  - [FoodApp/app/.../pages/shipper/gps/GpsViewModel.kt](FoodApp/app/src/main/java/com/example/foodapp/pages/shipper/gps/GpsViewModel.kt) — ViewModel sử dụng repository
  - [FoodApp/app/.../data/remote/](FoodApp/app/src/main/java/com/example/foodapp/data/remote/) — API service interfaces

- **Gợi ý demo (30–60s):**
  1. Vẽ diagram: Screen → ViewModel → Repository → API
  2. Show code: ViewModel inject Repository, Repository inject ApiService

---

### C.2 OTP Verification với Rate Limiting + Attempt Counter

- **Mô tả ngắn:** Hệ thống OTP email với: (1) Rate limit gửi OTP, (2) Max 3 lần nhập sai → OTP hết hiệu lực.

- **Vì sao "đặc biệt" / giá trị:**
  - Security: Chống brute force
  - UX: User biết còn bao nhiêu lần thử
  - Atomic: `FieldValue.increment()` đếm attempts chính xác

- **Cách triển khai trong project:**
  ```typescript
  if (otp.attempts >= MAX_ATTEMPTS) {
      throw new Error('OTP expired due to too many attempts');
  }
  if (otp.code !== providedCode) {
      await this.otpRepository.incrementAttempts(otp.id);
      throw new Error('Invalid OTP');
  }
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/modules/auth/auth.service.ts](Backend/functions/src/modules/auth/auth.service.ts) — `verifyOTP()` với attempt check (lines 369-395)
  - [Backend/functions/src/modules/auth/repositories/firestore-otp.repository.ts](Backend/functions/src/modules/auth/repositories/firestore-otp.repository.ts) — `incrementAttempts()` (lines 54-56)

- **Gợi ý demo (30–60s):**
  1. Sign up → nhận OTP email
  2. Nhập sai 3 lần → báo hết lượt
  3. Phải request OTP mới

---

### C.3 Lazy Initialization + Caching cho Cloud Functions

- **Mô tả ngắn:** NestJS app được cache trong memory để tránh cold start overhead. Mỗi request sau lần đầu reuse cached instance.

- **Vì sao "đặc biệt" / giá trị:**
  - Performance: Cold start 3-5s, warm start <100ms
  - Cost: Giảm số lần khởi tạo
  - Standard pattern cho serverless

- **Cách triển khai trong project:**
  ```typescript
  let cachedServer: INestApplication | null = null;
  
  export const api = functions.https.onRequest(async (req, res) => {
      if (!cachedServer) {
          cachedServer = await createNestApp();
      }
      cachedServer.handle(req, res);
  });
  ```

- **Bằng chứng trong code:**
  - [Backend/functions/src/index.ts](Backend/functions/src/index.ts) — `cachedServer` pattern (lines 92-116)

- **Gợi ý demo (30–60s):**
  1. Gọi API lần 1 → chậm (cold start)
  2. Gọi API lần 2 ngay → nhanh (warm)
  3. Giải thích caching mechanism

---

### C.4 Error Parser với Vietnamese Translation

- **Mô tả ngắn:** Frontend có utility parse error response từ API và translate thành message tiếng Việt thân thiện với user.

- **Vì sao "đặc biệt" / giá trị:**
  - UX: User thấy message tiếng Việt, không phải technical error
  - Centralized: Một nơi xử lý tất cả API errors
  - Graceful: Fallback message nếu không parse được

- **Cách triển khai trong project:**
  ```kotlin
  object ErrorParser {
      fun parseError(errorBody: String?): String {
          // Parse JSON → extract message → translate
          return translateErrorMessage(message)
      }
  }
  ```

- **Bằng chứng trong code:**
  - [FoodApp/app/.../utils/ErrorParser.kt](FoodApp/app/src/main/java/com/example/foodapp/utils/ErrorParser.kt) — `ErrorParser` object (lines 7-50)

- **Gợi ý demo (30–60s):**
  1. Trigger một lỗi (VD: shop closed)
  2. Thấy message tiếng Việt: "Cửa hàng đang đóng cửa"
  3. Không phải technical: "SHOP_CLOSED_409"

---

## D. Tóm tắt 1 slide (bullet)

**Frontend (Android):**
- 🗺️ GPS Tracking: FusedLocationProviderClient + Kotlin Flow + 5s interval
- 💬 Optimistic UI: Chat messages hiển thị ngay, sync sau
- 🔄 StateFlow + UiState: Unidirectional data flow cho Compose
- 🔀 Sealed Class Navigation: Type-safe routes + argument encoding
- ⏱️ Debounce Search: 300ms delay giảm API calls

**Backend (NestJS):**
- 🔍 Fuzzy Search: Fuse.js + Vietnamese normalization + caching
- 💾 TTL Cache: In-memory, auto-invalidation, zero Redis dependency
- 🤖 AI Chatbot: Gemini + rate limiting + FAQ context injection
- ⚡ Atomic Transactions: Order + Voucher + SoldCount atomicity
- 🚦 State Machine: Order lifecycle với valid transitions only
- 🔔 FCM + History: Best-effort push, always persist history
- 🔐 RBAC Guards: AuthGuard + RolesGuard declarative security
- 💳 SePay Integration: QR code payment generation

**Cross-cutting:**
- 🏗️ MVVM + Repository Pattern
- 🔒 OTP với rate limit + attempt counter
- ⚡ Cloud Function caching (warm start)
- 🇻🇳 Error message translation

---

## E. Script nói 2 phút (đọc được)

> **Thầy/Cô hỏi:** "Em giới thiệu các kỹ thuật đặc biệt trong project?"

---

Dạ thưa thầy/cô, trong dự án KTX Delivery, nhóm em đã áp dụng một số kỹ thuật nâng cao để giải quyết các vấn đề thực tế.

**Về phía Android**, nhóm sử dụng **Kotlin Flow kết hợp FusedLocationProviderClient** để theo dõi vị trí shipper real-time. Mỗi 5 giây, vị trí được stream qua Flow và cập nhật lên server. Điểm đặc biệt là sử dụng `callbackFlow` với `awaitClose` để đảm bảo không bị memory leak khi user thoát khỏi màn hình.

Với tính năng chat, nhóm implement **Optimistic UI Pattern**: khi gửi tin nhắn, UI hiển thị ngay với status "đang gửi", không đợi server response. Nếu thành công thì thay bằng message thật, nếu fail thì đánh dấu lỗi. Pattern này giúp UX mượt mà như các ứng dụng chat chuyên nghiệp.

**Về phía Backend**, điểm nhấn là **tích hợp AI Chatbot với Google Gemini**. Chatbot không chỉ trả lời bằng AI mà còn inject FAQ context vào prompt để trả lời chính xác hơn về dịch vụ. Đồng thời có **rate limiting** mỗi 6 giây để tránh abuse.

Với **Fuse.js Fuzzy Search**, user có thể tìm "bun bo" mà vẫn ra "Bún bò Huế" nhờ typo tolerance và Vietnamese normalization.

Về data integrity, nhóm sử dụng **Firestore Transactions** để đảm bảo atomicity: khi tạo order, việc tạo order + xóa cart + apply voucher + update soldCount phải thành công cùng lúc hoặc rollback toàn bộ. Tương tự, khi shipper accept order, transaction đảm bảo không có 2 shipper cùng nhận 1 đơn.

Cuối cùng, **Order State Machine** đảm bảo đơn hàng chỉ có thể chuyển trạng thái hợp lệ: PENDING → CONFIRMED → PREPARING → READY → SHIPPING → DELIVERED. Không thể skip bước, giúp business logic an toàn.

Các kỹ thuật này không chỉ giải quyết bài toán kỹ thuật mà còn hướng đến trải nghiệm người dùng tốt và data integrity cao. Dạ em xin hết ạ.

---

## F. Các kỹ thuật dự kiến nhưng chưa có bằng chứng trong codebase

| Kỹ thuật | Lý do chưa implement |
|----------|---------------------|
| Background Location Service | Chỉ track foreground (MVP scope) |
| Offline-first với local database | MVP không yêu cầu offline support |
| Push notification với Firebase Topics | Chỉ dùng direct token send |
| Image compression trước upload | Upload nguyên ảnh (MVP scope) |
| WebSocket cho real-time chat | Dùng Firestore listeners thay thế |
