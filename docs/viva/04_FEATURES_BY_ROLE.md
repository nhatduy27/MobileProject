# 👥 TÍNH NĂNG THEO VAI TRÒ - KTX Delivery App
> **Tài liệu Viva - Phase 2**  
> **Cập nhật lần cuối:** 30 tháng 1, 2026

---

## MỤC LỤC

1. [Tổng quan vai trò](#1-role-overview)
2. [Tính năng BUYER (Khách hàng)](#2-buyer-customer-features)
3. [Tính năng SELLER (Chủ cửa hàng)](#3-seller-owner-features)
4. [Tính năng SHIPPER](#4-shipper-features)
5. [Trạng thái triển khai tính năng](#5-feature-implementation-status)

---

## 1. TỔNG QUAN VAI TRÒ

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CÁC VAI TRÒ NGƯỜI DÙNG                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                │
│   │   BUYER     │    │   SELLER    │    │   SHIPPER   │                │
│   │  (CUSTOMER) │    │   (OWNER)   │    │             │                │
│   ├─────────────┤    ├─────────────┤    ├─────────────┤                │
│   │ • Duyệt     │    │ • Quản lý   │    │ • Nhận      │                │
│   │ • Đặt hàng  │    │   cửa hàng  │    │   đơn hàng  │                │
│   │ • Thanh toán│    │ • Sản phẩm  │    │ • GPS track │                │
│   │ • Theo dõi  │    │ • Đơn hàng  │    │ • Giao hàng │                │
│   │ • Đánh giá  │    │ • Shipper   │    │ • Thu nhập  │                │
│   │ • Chat      │    │ • Doanh thu │    │ • Chat      │                │
│   └─────────────┘    └─────────────┘    └─────────────┘                │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

| Vai trò | Tên nội bộ | Mô tả | Màn hình đầu vào |
|------|---------------|-------------|--------------|
| **BUYER** | `CUSTOMER` | Người dùng cuối đặt món ăn | `UserHomeScreen` |
| **SELLER** | `OWNER` | Chủ cửa hàng quản lý shop của mình | `DashBoardRootScreen` |
| **SHIPPER** | `SHIPPER` | Tài xế giao hàng | `ShipperDashboardRootScreen` |
| **ADMIN** | `ADMIN` | Quản trị viên hệ thống (web panel) | Admin Dashboard (React) |

**Bằng chứng:** Các hàm helper trong `firestore.rules`: `hasRole()`, `hasOwnerRole()`, `hasShipperRole()`

---

## 2. Tính năng BUYER (Khách hàng)

### 2.1 Tóm tắt tính năng

| Tính năng | Màn hình | ViewModel | Trạng thái |
|---------|--------|-----------|--------|
| Duyệt cửa hàng | `UserHomeScreen` | `UserHomeViewModel` | ✅ Hoàn thành |
| Xem chi tiết cửa hàng | `ShopDetailScreen` | `ShopDetailViewModel` | ✅ Hoàn thành |
| Duyệt sản phẩm | `UserHomeScreen`, `ProductDetailScreen` | `UserHomeViewModel` | ✅ Hoàn thành |
| Tìm kiếm sản phẩm | `UserHomeScreen` | `UserHomeViewModel.searchProducts()` | ✅ Hoàn thành |
| Giỏ hàng | `CartScreen` | `CartViewModel` | ✅ Hoàn thành |
| Thanh toán | `PaymentScreen` | `PaymentViewModel` | ✅ Hoàn thành |
| Áp dụng voucher | `PaymentScreen` | `PaymentViewModel` | ✅ Hoàn thành |
| Theo dõi đơn hàng | `OrderScreen`, `OrderDetailScreen` | `OrderViewModel` | ✅ Hoàn thành |
| Viết đánh giá | `ReviewScreen` | `ReviewViewModel` | ✅ Hoàn thành |
| Chat với cửa hàng | `ChatScreen` | `ChatViewModel` | ✅ Hoàn thành |
| Thông báo | `NotificationsScreen` | `NotificationsViewModel` | ✅ Hoàn thành |
| Yêu thích | `FavoritesScreen` | `FavoritesViewModel` | ✅ Hoàn thành |
| AI Chatbot | `ChatBotScreen` | `ChatBotViewModel` | ✅ Hoàn thành |
| Hồ sơ/Cài đặt | `SettingsScreen` | `SettingsViewModel` | ✅ Hoàn thành |

### 2.2 Chi tiết tính năng

#### 2.2.1 Duyệt cửa hàng

**Mô tả:** Xem danh sách các cửa hàng/nhà hàng có sẵn

**Điểm vào:**
- Màn hình: `pages/client/home/UserHomeScreen.kt`
- ViewModel: `pages/client/home/UserHomeViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `shops` | `id`, `name`, `imageUrl`, `status`, `rating` |
| `products` | `id`, `name`, `price`, `shopId`, `imageUrl` |

**Các phương thức chính:**
```kotlin
// UserHomeViewModel.kt
fun loadShops()
fun searchShops(query: String)
```

**Backend Endpoint:** `GET /api/shops`

---

#### 2.2.2 Giỏ hàng

**Mô tả:** Quản lý các mặt hàng trước khi thanh toán (thêm, cập nhật số lượng, xóa)

**Điểm vào:**
- Màn hình: `pages/client/cart/CartScreen.kt`
- ViewModel: `pages/client/cart/CartViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `carts/{userId}/items` | `productId`, `quantity`, `price`, `shopId` |
| `products` | `name`, `price`, `imageUrl` |
| `shops` | `name`, `imageUrl` |

**Các phương thức chính:**
```kotlin
// CartViewModel.kt
fun loadCart()
fun updateQuantity(productId: String, quantity: Int)
fun removeItem(productId: String)
```

**Backend Endpoints:**
- `GET /api/cart` - Lấy các mặt hàng trong giỏ (nhóm theo cửa hàng)
- `PATCH /api/cart/:productId` - Cập nhật số lượng
- `DELETE /api/cart/:productId` - Xóa mặt hàng

**Logic đặc biệt:** Các mặt hàng trong giỏ được nhóm theo cửa hàng; mỗi nhóm cửa hàng trở thành một đơn hàng riêng biệt.

---

#### 2.2.3 Thanh toán

**Mô tả:** Tạo đơn hàng từ giỏ hàng, chọn phương thức thanh toán

**Điểm vào:**
- Màn hình: `pages/client/payment/PaymentScreen.kt`
- ViewModel: `pages/client/payment/PaymentViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `customerId`, `shopId`, `items[]`, `totalAmount`, `status`, `paymentMethod`, `paymentStatus` |
| `carts/{userId}/items` | (xóa sau khi đặt hàng) |
| `vouchers` | `code`, `discountPercent`, `discountAmount` |

**Phương thức thanh toán:**
| Phương thức | Mã | Mô tả |
|--------|------|-------------|
| Thanh toán khi nhận hàng | `COD` | Trả tiền mặt cho shipper |
| Chuyển khoản | `BANK_TRANSFER` | Thanh toán qua mã QR SePay |

**Các phương thức chính:**
```kotlin
// PaymentViewModel.kt
fun createOrder(dto: CreateOrderRequest)
fun applyVoucher(code: String)
fun pollPaymentStatus(orderId: String)
```

**Backend Endpoints:**
- `POST /api/orders` - Tạo đơn hàng (giao dịch)
- `POST /api/vouchers/validate` - Xác thực voucher
- `GET /api/payments/:orderId/status` - Kiểm tra trạng thái thanh toán

**Thông báo được kích hoạt:**
- `NEW_ORDER` → Chủ cửa hàng

---

#### 2.2.4 Theo dõi đơn hàng

**Mô tả:** Xem lịch sử đơn hàng, theo dõi trạng thái đơn hàng hiện tại

**Điểm vào:**
- Màn hình: `pages/client/order/OrderScreen.kt`
- ViewModel: `pages/client/order/OrderViewModel.kt`

**Luồng trạng thái đơn hàng (góc nhìn khách hàng):**
```
PENDING → CONFIRMED → PREPARING → READY → SHIPPING → DELIVERED
                                                    ↓
                                              [Có thể đánh giá]
```

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `id`, `status`, `items[]`, `totalAmount`, `createdAt`, `shipperId` |
| `trips` | `currentLocation`, `route` (cho theo dõi trực tiếp) |

**Các phương thức chính:**
```kotlin
// OrderViewModel.kt
fun loadOrders()
fun getOrderById(orderId: String)
```

**Backend Endpoints:**
- `GET /api/orders` - Lịch sử đơn hàng của khách
- `GET /api/orders/:id` - Chi tiết đơn hàng
- `GET /api/gps/trips/order/:orderId` - Lấy chuyến đi để theo dõi

---

#### 2.2.5 Đánh giá & Xếp hạng

**Mô tả:** Đánh giá và xếp hạng đơn hàng sau khi giao

**Điểm vào:**
- Màn hình: `pages/client/review/ReviewScreen.kt`
- ViewModel: `pages/client/review/ReviewViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `review` (nhúng), `hasReview` |

**Các phương thức chính:**
```kotlin
// ReviewViewModel.kt
fun submitReview(orderId: String, rating: Int, comment: String)
```

**Backend Endpoint:** `POST /api/orders/:id/reviews`

**Hạn chế đã biết:**
- ⚠️ **TODO trong code:** Chưa triển khai sửa đánh giá (`ReviewScreen.kt:236`)
- ⚠️ **TODO trong code:** Chưa triển khai xóa đánh giá (`ReviewViewModel.kt:144`)

---

#### 2.2.6 Chat với chủ cửa hàng

**Mô tả:** Nhắn tin 1-1 với chủ cửa hàng

**Điểm vào:**
- Màn hình: `pages/client/listchat/ListChatScreen.kt` → `pages/client/chat/ChatScreen.kt`
- ViewModel: `ChatViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `conversations` | `participants[]`, `lastMessage`, `updatedAt` |
| `conversations/{id}/messages` | `senderId`, `text`, `timestamp`, `read` |

**Các phương thức chính:**
```kotlin
// ChatViewModel.kt
fun loadConversations()
fun sendMessage(conversationId: String, text: String)
```

**Backend Endpoints:**
- `POST /api/chat/conversations` - Tạo/lấy cuộc hội thoại
- `POST /api/chat/conversations/:id/messages` - Gửi tin nhắn
- `GET /api/chat/conversations/:id/messages` - Lấy tin nhắn

**Hạn chế đã biết:**
- ⚠️ **TODO trong code:** Logic đếm tin nhắn chưa đọc chưa hoàn thiện (`ListChatScreen.kt:313`)

---

## 3. Tính năng SELLER (Chủ cửa hàng)

### 3.1 Tóm tắt tính năng

| Tính năng | Màn hình | ViewModel | Trạng thái |
|---------|--------|-----------|--------|
| Thiết lập cửa hàng | `ShopSetupScreen` | `ShopSetupViewModel` | ✅ Hoàn thành |
| Bảng điều khiển | `DashBoardRootScreen` | - | ✅ Hoàn thành |
| Quản lý sản phẩm | `FoodsScreen` | `FoodsViewModel` | ✅ Hoàn thành |
| Quản lý đơn hàng | `OrdersScreen` | `OrdersViewModel` | ✅ Hoàn thành |
| Quản lý Shipper | `ShippersScreen` | `ShippersViewModel` | ✅ Hoàn thành |
| Quản lý Voucher | `VouchersScreen` | `VouchersViewModel` | ✅ Hoàn thành |
| Phân tích doanh thu | `RevenueScreen` | `RevenueViewModel` | ✅ Hoàn thành |
| Thống kê khách hàng | `CustomerScreen` | `CustomerViewModel` | ✅ Hoàn thành |
| Đánh giá | `ReviewsScreen` | `ReviewsViewModel` | ✅ Hoàn thành |
| Chat với khách | `OwnerChatDetailScreen` | `OwnerChatViewModel` | ✅ Hoàn thành |
| AI Chatbot | `ChatbotScreen` | `ChatbotViewModel` | ✅ Hoàn thành |
| Cài đặt | `SettingsScreen`, `StoreInfoScreen` | `SettingsViewModel` | ✅ Hoàn thành |

### 3.2 Chi tiết tính năng

#### 3.2.1 Thiết lập cửa hàng (Luồng lần đầu)

**Mô tả:** Tạo và cấu hình cửa hàng cho chủ mới

**Điểm vào:**
- Màn hình: `pages/owner/shopsetup/ShopSetupScreen.kt`
- ViewModel: `pages/owner/shopsetup/ShopSetupViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `shops` | `ownerId`, `name`, `description`, `address`, `phone`, `logo`, `coverImage`, `openTime`, `closeTime`, `status` |

**Trạng thái cửa hàng:**
| Trạng thái | Mô tả |
|--------|-------------|
| `PENDING_REVIEW` | Chờ admin phê duyệt |
| `OPEN` | Đang hoạt động và nhận đơn |
| `CLOSED` | Tạm đóng cửa |

**Backend Endpoints:**
- `POST /api/shops` - Tạo cửa hàng
- `GET /api/shops/my-shop` - Lấy cửa hàng của chủ
- `PATCH /api/shops/:id` - Cập nhật thông tin cửa hàng
- `POST /api/shops/:id/images` - Tải lên logo/ảnh bìa

---

#### 3.2.2 Quản lý sản phẩm (CRUD)

**Mô tả:** Tạo, đọc, cập nhật, xóa sản phẩm

**Điểm vào:**
- Màn hình: `pages/owner/foods/FoodsScreen.kt`
- ViewModel: `pages/owner/foods/FoodsViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `products` | `id`, `shopId`, `name`, `description`, `price`, `imageUrl`, `categoryId`, `isAvailable`, `soldCount` |
| `categories` | `id`, `name` |

**Các phương thức chính:**
```kotlin
// FoodsViewModel.kt
fun loadProducts()
fun createProduct(dto: CreateProductRequest)
fun updateProduct(productId: String, dto: UpdateProductRequest)
fun deleteProduct(productId: String)
fun toggleAvailability(productId: String)
```

**Backend Endpoints:**
- `POST /api/products` - Tạo sản phẩm
- `GET /api/products?shopId=xxx` - Danh sách sản phẩm
- `PATCH /api/products/:id` - Cập nhật sản phẩm
- `DELETE /api/products/:id` - Xóa mềm

**Tính năng SoldCount:**
- Tự động tăng khi đơn hàng được đánh dấu DELIVERED
- Tham khảo: `ProductsService.incrementSoldCount()`

---

#### 3.2.3 Quản lý đơn hàng

**Mô tả:** Xem và quản lý các đơn hàng đến

**Điểm vào:**
- Màn hình: `pages/owner/orders/OrdersScreen.kt`
- ViewModel: `pages/owner/orders/OrdersViewModel.kt`

**Máy trạng thái đơn hàng (Hành động của chủ cửa hàng):**
```
[Khách hàng tạo đơn]
        │
        ▼
    PENDING ──────── confirmOrder() ──────► CONFIRMED
                                                │
                                                │ markAsPreparing()
                                                ▼
                                           PREPARING
                                                │
                                                │ markAsReady()
                                                ▼
                                             READY ────────► [Shipper lấy đơn]
                                                │
                                                │ (tự động cập nhật)
                                                ▼
                                           SHIPPING ───────► DELIVERED
```

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `id`, `customerId`, `shopId`, `items[]`, `status`, `totalAmount`, `createdAt` |

**Các phương thức chính:**
```kotlin
// OrdersViewModel.kt
fun loadOrders(status: OrderStatus)
fun confirmOrder(orderId: String)
fun markAsPreparing(orderId: String)
fun markAsReady(orderId: String)
fun cancelOrder(orderId: String, reason: String)
```

**Backend Endpoints:**
- `GET /api/orders/owner` - Đơn hàng đến của chủ cửa hàng
- `PATCH /api/orders/:id/confirm` - Xác nhận đơn
- `PATCH /api/orders/:id/preparing` - Đánh dấu đang chuẩn bị
- `PATCH /api/orders/:id/ready` - Đánh dấu sẵn sàng
- `PATCH /api/orders/:id/cancel` - Hủy đơn

**Thông báo được kích hoạt:**
| Hành động | Thông báo | Người nhận |
|--------|-------------|-----------|
| `confirmOrder()` | `ORDER_CONFIRMED` | Khách hàng |
| `markAsReady()` | `ORDER_READY` | Khách hàng, Shipper topic |
| `cancelOrder()` | `ORDER_CANCELLED` | Khách hàng |

---

#### 3.2.4 Quản lý Shipper

**Mô tả:** Phê duyệt/từ chối đơn ứng tuyển shipper, quản lý shipper đang hoạt động

**Điểm vào:**
- Màn hình: `pages/owner/shippers/ShippersScreen.kt`
- ViewModel: `pages/owner/shippers/ShippersViewModel.kt`

**Các tab:**
1. **Đơn ứng tuyển** - Đơn ứng tuyển shipper đang chờ
2. **Shipper hoạt động** - Shipper hiện đang được phân công
3. **Yêu cầu rời đi** - Shipper yêu cầu nghỉ việc

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `shipper_applications` | `shipperId`, `shopId`, `status`, `createdAt` |
| `shippers` | `shipperId`, `shopId`, `status` |
| `shipper_removal_requests` | `shipperId`, `shopId`, `reason`, `status` |

**Các phương thức chính:**
```kotlin
// ShippersViewModel.kt
fun loadApplications()
fun approveApplication(applicationId: String)
fun rejectApplication(applicationId: String)
fun handleRemovalRequest(requestId: String, approved: Boolean)
```

**Backend Endpoints:**
- `GET /api/shippers/applications?shopId=xxx` - Danh sách đơn ứng tuyển
- `PATCH /api/shippers/applications/:id/approve` - Phê duyệt
- `PATCH /api/shippers/applications/:id/reject` - Từ chối
- `PATCH /api/shipper-removal-requests/:id/approve` - Chấp nhận rời đi
- `PATCH /api/shipper-removal-requests/:id/reject` - Từ chối rời đi

**Thông báo được kích hoạt:**
| Hành động | Thông báo | Người nhận |
|--------|-------------|-----------|
| `approveApplication()` | `APPLICATION_APPROVED` | Shipper |
| `rejectApplication()` | `APPLICATION_REJECTED` | Shipper |

---

#### 3.2.5 Quản lý Voucher

**Mô tả:** Tạo và quản lý voucher giảm giá

**Điểm vào:**
- Màn hình: `pages/owner/vouchers/VouchersScreen.kt`
- ViewModel: `pages/owner/vouchers/VouchersViewModel.kt`

**Loại Voucher:**
| Loại | Mô tả | Ví dụ |
|------|-------------|---------|
| `PERCENTAGE` | Giảm theo phần trăm | Giảm 20% |
| `FIXED_AMOUNT` | Giảm theo số tiền cố định | Giảm 50.000đ |

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `vouchers` | `id`, `shopId`, `code`, `type`, `value`, `minOrderAmount`, `maxDiscount`, `usageLimit`, `usedCount`, `expiresAt`, `isActive` |
| `voucherUsage` | `voucherId`, `userId`, `orderId`, `usedAt` |

**Các phương thức chính:**
```kotlin
// VouchersViewModel.kt
fun loadVouchers()
fun createVoucher(dto: CreateVoucherRequest)
fun updateVoucher(voucherId: String, dto: UpdateVoucherRequest)
fun deactivateVoucher(voucherId: String)
```

**Backend Endpoints:**
- `GET /api/vouchers?shopId=xxx` - Danh sách voucher cửa hàng
- `POST /api/vouchers` - Tạo voucher
- `PATCH /api/vouchers/:id` - Cập nhật voucher
- `DELETE /api/vouchers/:id` - Vô hiệu hóa voucher

---

#### 3.2.6 Phân tích doanh thu

**Mô tả:** Xem thống kê bán hàng và báo cáo doanh thu

**Điểm vào:**
- Màn hình: `pages/owner/revenue/RevenueScreen.kt`
- ViewModel: `pages/owner/revenue/RevenueViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `totalAmount`, `status`, `createdAt` (tổng hợp) |
| `wallets` | `balance` |

**Các chỉ số hiển thị:**
- Doanh thu ngày/tuần/tháng
- Tổng số đơn hàng
- Giá trị đơn hàng trung bình
- Sản phẩm bán chạy nhất

**Backend Endpoint:** `GET /api/revenue?startDate=xxx&endDate=xxx`

---

#### 3.2.7 Thống kê khách hàng (Phân hạng người mua)

**Mô tả:** Xem thống kê mua hàng của khách và hạng thành viên

**Điểm vào:**
- Màn hình: `pages/owner/customer/CustomerScreen.kt`
- ViewModel: `pages/owner/customer/CustomerViewModel.kt`

**Hệ thống phân hạng:**
| Hạng | Số đơn hàng | Huy hiệu |
|------|-------------|-------|
| BRONZE | 0-5 đơn | 🥉 |
| SILVER | 6-15 đơn | 🥈 |
| GOLD | 16+ đơn | 🥇 |

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `customerId`, `totalAmount` (tổng hợp) |
| `users` | `name`, `email` |

**Backend Endpoint:** `GET /api/buyers/stats?shopId=xxx`

---

## 4. Tính năng SHIPPER

### 4.1 Tóm tắt tính năng

| Tính năng | Màn hình | ViewModel | Trạng thái |
|---------|--------|-----------|--------|
| Bảng điều khiển | `ShipperDashboardRootScreen` | - | ✅ Hoàn thành |
| Ứng tuyển vào cửa hàng | `ShopSelectionScreen` | `ShopSelectionViewModel` | ✅ Hoàn thành |
| Đơn ứng tuyển của tôi | `MyApplicationsScreen` | `MyApplicationsViewModel` | ✅ Hoàn thành |
| Đơn hàng khả dụng | `ShipperHomeScreen` | `ShipperHomeViewModel` | ✅ Hoàn thành |
| Nhận đơn hàng | `ShipperHomeScreen` | `ShipperHomeViewModel` | ✅ Hoàn thành |
| Giao hàng GPS | `GpsScreen`, `DeliveryMapScreen` | `GpsViewModel` | ✅ Hoàn thành |
| Quản lý chuyến đi | `TripDetailScreen` | `GpsViewModel` | ✅ Hoàn thành |
| Lịch sử đơn hàng | `HistoryScreen` | `HistoryViewModel` | ✅ Hoàn thành |
| Thu nhập/Ví | `EarningsScreen` | `EarningsViewModel` | ✅ Hoàn thành |
| Yêu cầu rời đi | `RemovalRequestScreen` | `RemovalRequestViewModel` | ✅ Hoàn thành |
| Chat | `ChatDetailScreen` | `ChatViewModel` | ✅ Hoàn thành |
| AI Chatbot | `ShipperChatbotScreen` | `ChatbotViewModel` | ✅ Hoàn thành |
| Thông báo | `NotificationsScreen` | `NotificationsViewModel` | ✅ Hoàn thành |

### 4.2 Chi tiết tính năng

#### 4.2.1 Ứng tuyển vào cửa hàng

**Mô tả:** Ứng tuyển làm tài xế giao hàng cho một cửa hàng

**Điểm vào:**
- Màn hình: `pages/shipper/application/ShopSelectionScreen.kt`
- ViewModel: `pages/shipper/application/ShopSelectionViewModel.kt`

**Luồng ứng tuyển:**
```
[Shipper] ──► Chọn cửa hàng ──► Gửi đơn ──► [Chủ cửa hàng duyệt]
                                                   │
                                                   ├─► Phê duyệt ──► Được phân công vào cửa hàng
                                                   │
                                                   └─► Từ chối ──► Có thể ứng tuyển lại
```

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `shops` | `id`, `name`, `address` (để chọn) |
| `shipper_applications` | `shipperId`, `shopId`, `status`, `createdAt` |

**Backend Endpoint:** `POST /api/shippers/applications`

---

#### 4.2.2 Nhận & Giao đơn hàng

**Mô tả:** Xem đơn hàng khả dụng và nhận để giao

**Điểm vào:**
- Màn hình: `pages/shipper/home/ShipperHomeScreen.kt`
- ViewModel: `pages/shipper/home/ShipperHomeViewModel.kt`

**Luồng nhận đơn:**
```
[Chủ cửa hàng đánh dấu READY]
        │
        ▼
Đơn hàng khả dụng ──► Shipper nhận ──► Đơn được gán
        │                                      │
        │                                      ▼
        │                              Tạo/Tham gia chuyến đi
        │                                      │
        │                                      ▼
        │                              Bắt đầu chuyến (SHIPPING)
        │                                      │
        │                                      ▼
        │                              Hoàn thành chuyến (DELIVERED)
        │
        └──► [Shipper khác thấy đơn đã biến mất]
```

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `orders` | `id`, `status`, `shopId`, `deliveryAddress`, `items[]` |
| `shippers` | `shopId` (để lọc đơn theo cửa hàng được gán) |

**Các phương thức chính:**
```kotlin
// ShipperHomeViewModel.kt
fun loadAvailableOrders()
fun acceptOrder(orderId: String)
fun toggleOnlineStatus()
```

**Backend Endpoints:**
- `GET /api/orders/shipper/available` - Đơn hàng khả dụng (trạng thái READY)
- `PATCH /api/orders/:id/accept` - Nhận đơn (atomic, ngăn race condition)

**Thông báo được kích hoạt:**
| Hành động | Thông báo | Người nhận |
|--------|-------------|-----------|
| `acceptOrder()` | `ORDER_SHIPPING` | Khách hàng |

---

#### 4.2.3 Giao hàng GPS & Quản lý chuyến đi

**Mô tả:** Theo dõi GPS theo thời gian thực trong quá trình giao hàng

**Điểm vào:**
- Màn hình: `pages/shipper/gps/GpsScreen.kt`, `pages/shipper/gps/DeliveryMapScreen.kt`
- ViewModel: `pages/shipper/gps/GpsViewModel.kt`

**Vòng đời chuyến đi:**
```
PENDING ──► startTrip() ──► STARTED ──► finishTrip() ──► COMPLETED
                              │
                              │ updateLocation() (liên tục)
                              ▼
                        Theo dõi thời gian thực
```

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `trips` | `id`, `shipperId`, `shopId`, `orderIds[]`, `status`, `route[]`, `currentLocation` |
| `orders` | `status` (cập nhật thành SHIPPING/DELIVERED) |

**Các phương thức chính:**
```kotlin
// GpsViewModel.kt
fun createTrip(orderIds: List<String>)
fun startTrip(tripId: String)
fun updateLocation(tripId: String, lat: Double, lng: Double)
fun finishTrip(tripId: String)
fun loadTripByOrderId(orderId: String)
```

**Backend Endpoints:**
- `POST /api/gps/trips` - Tạo chuyến đi
- `PATCH /api/gps/trips/:id/start` - Bắt đầu chuyến
- `PATCH /api/gps/trips/:id/location` - Cập nhật vị trí
- `POST /api/gps/trips/:id/finish` - Hoàn thành chuyến

**Android Permissions:**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

**Thông báo được kích hoạt:**
| Hành động | Thông báo | Người nhận |
|--------|-------------|-----------|
| `startTrip()` | `ORDER_SHIPPING` | Khách hàng |
| `finishTrip()` | `ORDER_DELIVERED` | Khách hàng |

**Hạn chế đã biết:**
- ⚠️ **TODO trong code:** Chưa triển khai điều hướng Google Maps (`DeliveryMapScreen.kt:541`)

---

#### 4.2.4 Thu nhập & Ví

**Mô tả:** Xem thu nhập, lịch sử giao dịch, yêu cầu rút tiền

**Điểm vào:**
- Màn hình: `pages/shipper/earnings/EarningsScreen.kt`
- ViewModel: `pages/shipper/earnings/EarningsViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `wallets` | `balance` |
| `wallets/{id}/transactions` | `type`, `amount`, `createdAt`, `orderId` |
| `payoutRequests` | `userId`, `amount`, `status`, `createdAt` |

**Các phương thức chính:**
```kotlin
// EarningsViewModel.kt
fun loadBalance()
fun loadTransactions()
fun requestPayout(amount: Double)
```

**Backend Endpoints:**
- `GET /api/wallets/balance` - Số dư hiện tại
- `GET /api/wallets/transactions` - Lịch sử giao dịch
- `POST /api/wallets/payouts` - Yêu cầu rút tiền

---

#### 4.2.5 Yêu cầu rời đi

**Mô tả:** Yêu cầu rời khỏi phân công cửa hàng

**Điểm vào:**
- Màn hình: `pages/shipper/removal/RemovalRequestScreen.kt`
- ViewModel: `pages/shipper/removal/RemovalRequestViewModel.kt`

**Thực thể dữ liệu:**
| Collection | Các trường sử dụng |
|------------|-------------|
| `shipper_removal_requests` | `shipperId`, `shopId`, `reason`, `status`, `createdAt` |

**Backend Endpoint:** `POST /api/shipper-removal-requests`

---

## 5. TRẠNG THÁI TRIỂN KHAI TÍNH NĂNG

### ✅ Đã triển khai đầy đủ

| Tính năng | BUYER | SELLER | SHIPPER |
|---------|:-----:|:------:|:-------:|
| Xác thực | ✅ | ✅ | ✅ |
| Quản lý hồ sơ | ✅ | ✅ | ✅ |
| Duyệt cửa hàng/Sản phẩm | ✅ | - | - |
| Giỏ hàng | ✅ | - | - |
| Thanh toán | ✅ | - | - |
| Theo dõi đơn hàng | ✅ | ✅ | ✅ |
| Quản lý cửa hàng | - | ✅ | - |
| CRUD sản phẩm | - | ✅ | - |
| Máy trạng thái đơn hàng | - | ✅ | ✅ |
| Ứng tuyển Shipper | - | ✅ | ✅ |
| Giao hàng GPS | - | - | ✅ |
| Hệ thống Voucher | ✅ | ✅ | - |
| Ví/Thu nhập | - | ✅ | ✅ |
| Chat 1-1 | ✅ | ✅ | ✅ |
| AI Chatbot | ✅ | ✅ | ✅ |
| Thông báo đẩy | ✅ | ✅ | ✅ |
| Đánh giá | ✅ | ✅ | - |

### ⚠️ Triển khai một phần / Có TODO

| Tính năng | Vấn đề | Tham khảo |
|---------|-------|-----------|
| Sửa đánh giá | Chưa triển khai | `ReviewScreen.kt:236` |
| Xóa đánh giá | Chưa triển khai | `ReviewViewModel.kt:144` |
| Điều hướng Google Maps | TODO placeholder | `DeliveryMapScreen.kt:541` |
| Đếm tin nhắn chưa đọc | Logic chưa hoàn thiện | `ListChatScreen.kt:313` |
| Kiểm tra đánh giá tồn tại | API không được gọi | `OrderDetailViewModel.kt:129` |

### ❌ Chưa triển khai

| Tính năng | Ghi chú |
|---------|-------|
| Nhiều cửa hàng cho mỗi chủ | Một chủ = một cửa hàng |
| Hệ thống hoàn tiền | Không có luồng hoàn tiền |
| Đơn hàng đặt lịch | Không có đặt trước |
| Chế độ offline | Không có cache cục bộ |
| Dark mode | Chưa triển khai |

---

## TÓM TẮT THAM CHIẾU FILE

| Vai trò | Các file chính |
|------|-----------|
| **BUYER** | `pages/client/home/`, `pages/client/cart/`, `pages/client/payment/`, `pages/client/order/` |
| **SELLER** | `pages/owner/dashboard/`, `pages/owner/orders/`, `pages/owner/foods/`, `pages/owner/shippers/` |
| **SHIPPER** | `pages/shipper/home/`, `pages/shipper/gps/`, `pages/shipper/earnings/`, `pages/shipper/application/` |

---

**KẾT THÚC TÀI LIỆU TÍNH NĂNG THEO VAI TRÒ**
