ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts
ts

# API_REFERENCE_MVP.md
_Mobile Food Delivery – Firebase Functions Backend_

Tài liệu này mô tả **API Contract cho phiên bản MVP** của hệ thống giao đồ ăn, dùng Firebase Functions (onCall) + Firestore.

## Mục tiêu MVP

- Đăng ký / đăng nhập / quản lý profile
- Buyer: xem quán, xem menu, đặt đơn, xem + hủy đơn, áp mã giảm giá
- Seller: xem & xử lý đơn của quán
- Shipper: (optional) nhận đơn & cập nhật trạng thái
- Các trigger backend chính & luồng trạng thái đơn

---

## 1. Authentication APIs

### 1.1. `signUp`

**Mô tả:** Đăng ký tài khoản mới.

- **Type:** Callable (`onCall`)
- **Auth:** ❌
- **Role:** Public

**Request**
```typescript
interface SignUpRequest {
  email: string;
  password: string;        // >= 6 ký tự
  displayName: string;
  phoneNumber?: string;
  role?: "BUYER" | "SELLER" | "SHIPPER"; // default BUYER
}
```

**Response**
```typescript
interface SignUpResponse {
  userId: string;
  email: string;
  displayName: string;
  role: string;
  idToken: string;
  message: string;
}
```

**Ghi chú**
- Tạo user trong Firebase Auth
- Trigger onUserCreated tạo profile Firestore + set custom claims

---

### 1.2. `signIn`

**Mô tả:** Đăng nhập bằng email & password.

- **Type:** Callable
- **Auth:** ❌

**Request**
```typescript
interface SignInRequest {
  email: string;
  password: string;
}
```

**Response**
```typescript
interface SignInResponse {
  userId: string;
  email: string;
  displayName: string;
  role: string;
  idToken: string;
  refreshToken: string;
  expiresIn: number;
}
```

---

### 1.3. `getUserProfile`

**Mô tả:** Lấy profile của chính user đang đăng nhập.

- **Type:** Callable
- **Auth:** ✅
- **Role:** mọi role

**Request:** không có body, dùng `context.auth.uid`.

**Response:** object UserProfile (email, displayName, role, phoneNumber, photoURL, isActive, isVerified…).

---

### 1.4. `updateUserProfile`

**Mô tả:** Cập nhật profile của chính mình.

- **Type:** Callable
- **Auth:** ✅

**Request (ví dụ)**
```typescript
interface UpdateUserProfileRequest {
  displayName?: string;
  phoneNumber?: string;
  photoURL?: string;
  deliveryAddresses?: Address[];
  vehicleInfo?: VehicleInfo; // cho shipper
}
```
> Không cho phép đổi role, isActive, isVerified.

---

## 2. BUYER APIs

### 2.1. `getRestaurants`

**Mô tả:** Lấy danh sách quán đang hoạt động.

- **Type:** Callable
- **Auth:** ✅
- **Role:** BUYER

**Request (optional filter)**
```typescript
interface GetRestaurantsRequest {
  city?: string;
  category?: string;
  minRating?: number;
  isOpen?: boolean;
  limit?: number;
  startAfter?: string; // cursor
}
```

**Response**
```typescript
interface RestaurantList {
  restaurants: Restaurant[];
  hasMore: boolean;
  nextCursor?: string;
}
```

---

### 2.2. `getRestaurantMenu`

**Mô tả:** Xem menu của 1 quán.

- **Type:** Callable
- **Auth:** ✅
- **Role:** BUYER

**Request**
```typescript
interface GetRestaurantMenuRequest {
  restaurantId: string;
  category?: string;
  availableOnly?: boolean;
}
```

**Response**
```typescript
interface MenuItemList {
  restaurantId: string;
  restaurantName: string;
  menuItems: MenuItem[];
}
```

---

### 2.3. `placeOrder`

**Mô tả:** Buyer tạo đơn mới (status ban đầu: PENDING).

- **Type:** Callable
- **Auth:** ✅
- **Role:** BUYER (isVerified = true)

**Request**
```typescript
interface PlaceOrderRequest {
  restaurantId: string;
  items: OrderItem[];
  deliveryAddress: Address;
  paymentMethod: "CASH" | "CARD" | "WALLET";
  note?: string;
  promotionCode?: string;
}
```

**Response (rút gọn)**
```typescript
interface PlaceOrderResponse {
  orderId: string;
  status: "PENDING";
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  deliveryFee: number;
  estimatedDeliveryTime: string;
  createdAt: string;
}
```

---

### 2.4. `cancelOrder`

**Mô tả:** Buyer hủy đơn khi còn PENDING.

- **Type:** Callable
- **Auth:** ✅
- **Role:** BUYER (chủ đơn)

**Request**
```typescript
interface CancelOrderRequest {
  orderId: string;
  reason?: string;
}
```

**Response**
```typescript
interface CancelOrderResponse {
  success: boolean;
  orderId: string;
  previousStatus: string;
  newStatus: "CANCELLED";
  cancelledAt: string;
  refundAmount?: number;
}
```

---

### 2.5. `getMyOrders`

**Mô tả:** Lịch sử đơn của Buyer.

- **Type:** Callable
- **Auth:** ✅
- **Role:** BUYER

**Request**
```typescript
interface GetMyOrdersRequest {
  status?: string;
  limit?: number;
  startAfter?: string;
}
```

**Response**
```typescript
interface BuyerOrderList {
  orders: OrderSummary[];
  hasMore: boolean;
  nextCursor?: string;
}
```

---

### 2.6. `applyPromotion`

**Mô tả:** Áp mã khuyến mãi cho một đơn PENDING.

- **Type:** Callable
- **Auth:** ✅
- **Role:** BUYER (chủ đơn)

**Request**
```typescript
interface ApplyPromotionRequest {
  orderId: string;
  promotionCode: string;
}
```

**Response**
```typescript
interface ApplyPromotionResponse {
  orderId: string;
  promotionId: string;
  code: string;
  discountAmount: number;
  originalAmount: number;
  finalAmount: number;
  type: "PERCENT" | "FIXED_AMOUNT" | "FREE_DELIVERY";
  expiresAt: string;
}
```

---

## 3. SELLER APIs

### 3.1. `getRestaurantOrders`

**Mô tả:** Seller xem danh sách đơn của quán.

- **Type:** Callable
- **Auth:** ✅
- **Role:** SELLER (owner của restaurant)

**Request**
```typescript
interface GetRestaurantOrdersRequest {
  restaurantId: string;
  status?: string;
  limit?: number;
  startAfter?: string;
}
```

**Response:** danh sách Order với thông tin buyer, items, amounts, status, createdAt…

---

### 3.2. `acceptOrder`

**Mô tả:** Chấp nhận đơn → PENDING → ACCEPTED.

- **Type:** Callable
- **Role:** SELLER

**Request**
```typescript
interface AcceptOrderRequest {
  orderId: string;
  estimatedPreparationTime?: number; // phút
}
```

**Response:** chứa orderId, previousStatus, newStatus: "ACCEPTED", estimatedReadyTime, updatedAt.

---

### 3.3. `rejectOrder`

**Mô tả:** Từ chối đơn → PENDING → REJECTED.

- **Type:** Callable
- **Role:** SELLER

**Request**
```typescript
interface RejectOrderRequest {
  orderId: string;
  reason: string;
}
```

---

### 3.4. `updateOrderToPreparing`

**Mô tả:** Đang chuẩn bị món → ACCEPTED → PREPARING.

- **Type:** Callable
- **Role:** SELLER

**Request**
```typescript
interface UpdateOrderToPreparingRequest {
  orderId: string;
}
```

---

### 3.5. `updateOrderToReady`

**Mô tả:** Món đã sẵn sàng → PREPARING → READY.

- **Type:** Callable
- **Role:** SELLER

**Request**
```typescript
interface UpdateOrderToReadyRequest {
  orderId: string;
}
```

---

## 4. SHIPPER APIs (Optional trong MVP)

Nếu muốn tối giản, có thể lùi nhóm API này sang phase sau. Contract gợi ý:

- `getAvailableOrders` – SHIPPER xem đơn READY chưa có shipper
- `acceptDelivery` – nhận giao; READY → ASSIGNED
- `updateOrderToPickedUp` – ASSIGNED → PICKED_UP
- `updateOrderToDelivering` – PICKED_UP → DELIVERING
- `completeOrder` – DELIVERING → COMPLETED

Schema có thể reuse từ file chi tiết nếu team cần.

---

## 5. Backend Triggers & Order Workflow

### 5.1. `onUserCreated` (Auth Trigger)

- Tự động tạo `users/{userId}` với thông tin cơ bản.
- Set custom claims role.
- Khởi tạo thống kê `userStats/{userId}`.

### 5.2. `onOrderCreated` (Firestore Trigger)

- Chạy khi tạo `orders/{orderId}`.
- Gửi notification cho buyer + seller.
- Cập nhật thống kê `restaurantStats`, `userStats`.

### 5.3. Luồng trạng thái đơn (MVP)

Các status chính:

- `PENDING` – Buyer mới tạo
- `ACCEPTED` – Seller chấp nhận
- `REJECTED` – Seller từ chối
- `PREPARING` – Quán đang làm món
- `READY` – Món xong, chờ shipper
- `ASSIGNED` – Đã có shipper nhận
- `PICKED_UP` – Shipper đã lấy món
- `DELIVERING` – Đang giao
- `COMPLETED` – Hoàn tất
- `CANCELLED` – Buyer hủy khi pending

**Transition hợp lệ (MVP):**

**Buyer:**
- `PENDING` → `CANCELLED`

**Seller:**
- `PENDING` → `ACCEPTED`
- `PENDING` → `REJECTED`
- `ACCEPTED` → `PREPARING`
- `PREPARING` → `READY`

**Shipper:**
- `READY` → `ASSIGNED`
- `ASSIGNED` → `PICKED_UP`
- `PICKED_UP` → `DELIVERING`
- `DELIVERING` → `COMPLETED`

Trigger `onOrderStatusChanged` (nếu implement sau) sẽ:

- Log thay đổi trạng thái
- Gửi notification tương ứng
- Cập nhật thống kê

---

## 6. Ghi chú triển khai

- Bắt buộc cho MVP: Auth + Buyer + Seller + 2 trigger đầu.
- Tùy chọn: API Shipper + trigger status nâng cao.

> Nếu cần chỉnh API, cập nhật file này trước rồi mới sửa code BE/FE.