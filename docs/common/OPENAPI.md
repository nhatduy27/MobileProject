# OpenAPI Specification - KTX Delivery API

> **Version:** 2.2  
> **Updated:** 2026-01-08  
> **Base URL:** `http://localhost:3000/api`  
> **Swagger UI:** `http://localhost:3000/api/docs`  
> ⚠️ **Synced with:** `docs-god/api/*` and actual backend implementation

---

## 📋 Implementation Status

| Symbol | Meaning                     |
| ------ | --------------------------- |
| ✅     | Implemented & Working       |
| 🧪     | Tested (Postman/Emulator)   |
| ⛔     | BLOCKED (needs dependencies)|
| 🔲     | Not Started                 |
| 🚧     | In Progress                 |

---

## 🔐 Authentication

Tất cả endpoints yêu cầu header (trừ đánh dấu 🔓 Public):

```
Authorization: Bearer <firebase-id-token>
```

---

# API Endpoints

## 1. AUTH ✅

> 🔐 **Module thuần xác thực** - KHÔNG chứa /me endpoints

| Status | Method | Endpoint                | Description              |
| ------ | ------ | ----------------------- | ------------------------ |
| ✅     | POST   | `/auth/register`        | 🔓 Đăng ký tài khoản mới |
| ✅     | POST   | `/auth/google`          | 🔓 Google Sign-In        |
| ✅     | POST   | `/auth/verify-email`    | 🔓 Xác thực OTP email    |
| ✅     | POST   | `/auth/resend-otp`      | 🔓 Gửi lại OTP           |
| ✅     | POST   | `/auth/forgot-password` | 🔓 Quên mật khẩu         |
| ✅     | POST   | `/auth/reset-password`  | 🔓 Đặt lại mật khẩu      |
| ✅     | PUT    | `/auth/change-password` | Đổi mật khẩu             |
| ✅     | POST   | `/auth/logout`          | Đăng xuất (xóa FCM)      |

### POST /auth/register

Đăng ký tài khoản mới với email/password.

**Request:**

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "displayName": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "CUSTOMER"
}
```

| Field       | Type   | Required | Options                  |
| ----------- | ------ | -------- | ------------------------ |
| email       | string | ✅       | Valid email              |
| password    | string | ✅       | Min 6 chars              |
| displayName | string | ✅       | Min 2 chars              |
| phone       | string | ❌       | VN phone (10 digits)     |
| role        | string | ✅       | CUSTOMER, OWNER, SHIPPER |

**Response:** `201 Created`

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uid_abc123",
      "email": "user@example.com",
      "displayName": "Nguyễn Văn A",
      "role": "CUSTOMER",
      "status": "ACTIVE",
      "createdAt": "2026-01-05T10:00:00Z"
    },
    "customToken": "firebase_custom_token..."
  },
  "message": "Đăng ký thành công"
}
```

---

## 2. USER ✅ (Hiệp)

> 👤 **Profile & Settings** - Tất cả /me endpoints

| Status | Method | Endpoint                    | Description           |
| ------ | ------ | --------------------------- | --------------------- |
| ✅     | GET    | `/me`                       | Lấy thông tin profile |
| ✅     | PUT    | `/me`                       | Cập nhật profile      |
| ✅     | POST   | `/me/avatar`                | Upload avatar         |
| ✅     | PUT    | `/me/fcm-token`             | Cập nhật FCM token    |
| ✅     | GET    | `/me/addresses`             | Danh sách địa chỉ     |
| ✅     | POST   | `/me/addresses`             | Thêm địa chỉ          |
| ✅     | PUT    | `/me/addresses/:id`         | Sửa địa chỉ           |
| ✅     | DELETE | `/me/addresses/:id`         | Xóa địa chỉ           |
| ✅     | PUT    | `/me/addresses/:id/default` | Set địa chỉ mặc định  |
| ✅     | GET    | `/me/favorites/products`             | Danh sách yêu thích   |
| ✅     | POST   | `/me/favorites/products/:productId`  | Thêm vào yêu thích    |
| ✅     | DELETE | `/me/favorites/products/:productId`  | Xóa khỏi yêu thích    |
| ✅     | GET    | `/me/settings`              | Lấy settings          |
| ✅     | PUT    | `/me/settings`              | Cập nhật settings     |
| ✅     | DELETE | `/me`                       | Xóa tài khoản         |

### GET /me

Lấy thông tin user hiện tại.

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uid_abc123",
      "email": "user@example.com",
      "displayName": "Nguyễn Văn A",
      "role": "CUSTOMER",
      "phone": "0901234567",
      "avatarUrl": "https://...",
      "addresses": [...],
      "createdAt": "2026-01-01T00:00:00Z"
    },
    "context": null
  }
}
```

---

## 3. CATEGORIES ✅

> 📁 **Admin-managed** - Chỉ Admin CRUD, Owner/Customer chỉ đọc

| Status | Method | Endpoint                | Description                      |
| ------ | ------ | ----------------------- | -------------------------------- |
| ✅     | GET    | `/categories`           | 🔓 Danh sách categories (active) |
| ✅     | GET    | `/categories/:idOrSlug` | 🔓 Chi tiết category             |
| ✅     | GET    | `/admin/categories`     | [Admin] Tất cả categories        |
| ✅     | GET    | `/admin/categories/:id` | [Admin] Chi tiết category        |
| ✅     | POST   | `/admin/categories`     | [Admin] Tạo category mới         |
| ✅     | PUT    | `/admin/categories/:id` | [Admin] Cập nhật category        |
| ✅     | DELETE | `/admin/categories/:id` | [Admin] Xóa category             |

---

## 4. SHOPS (Customer) 🔲 (Ninh)

| Status | Method | Endpoint                  | Description               |
| ------ | ------ | ------------------------- | ------------------------- |
| 🔲     | GET    | `/shops`                  | 🔓 Danh sách shop đang mở |
| 🔲     | GET    | `/shops/:shopId`          | 🔓 Chi tiết shop          |
| 🔲     | GET    | `/shops/:shopId/products` | 🔓 Menu của shop          |
| 🔲     | GET    | `/shops/:shopId/reviews`  | 🔓 Reviews của shop       |
| 🔲     | GET    | `/shops/search`           | 🔓 Tìm kiếm shop          |
| 🔲     | GET    | `/customer/shop-feed`     | Discovery feed            |

---

## 5. PRODUCTS (Customer) 🔲 (Ninh)

| Status | Method | Endpoint           | Description          |
| ------ | ------ | ------------------ | -------------------- |
| 🔲     | GET    | `/products`        | 🔓 Product Feed      |
| 🔲     | GET    | `/products/:id`    | 🔓 Chi tiết sản phẩm |
| 🔲     | GET    | `/products/search` | 🔓 Tìm kiếm sản phẩm |

---

## 6. CART 🔲 (Hiệp)

| Status | Method | Endpoint              | Description             |
| ------ | ------ | --------------------- | ----------------------- |
| 🔲     | GET    | `/cart`               | Lấy giỏ hàng            |
| 🔲     | POST   | `/cart/items`         | Thêm sản phẩm           |
| 🔲     | PUT    | `/cart/items/:itemId` | Cập nhật số lượng       |
| 🔲     | DELETE | `/cart/items/:itemId` | Xóa sản phẩm            |
| 🔲     | DELETE | `/cart`               | Xóa toàn bộ giỏ         |
| 🔲     | POST   | `/cart/validate`      | Validate trước checkout |

---

## 7. ORDERS (Customer) 🔲 

| Status | Method | Endpoint                   | Description           |
| ------ | ------ | -------------------------- | --------------------- |
| 🔲     | POST   | `/orders`                  | Tạo đơn hàng          |
| 🔲     | GET    | `/orders`                  | Danh sách đơn của tôi |
| 🔲     | GET    | `/orders/:orderId`         | Chi tiết đơn          |
| 🔲     | POST   | `/orders/:orderId/payment` | Thanh toán (2-step)   |
| 🔲     | POST   | `/orders/:orderId/cancel`  | Hủy đơn               |
| 🔲     | POST   | `/orders/:orderId/review`  | Đánh giá              |

---

## 8. VOUCHERS (Customer) 🔲 

| Status | Method | Endpoint          | Description               |
| ------ | ------ | ----------------- | ------------------------- |
| 🔲     | GET    | `/vouchers/my`    | Voucher của tôi           |
| 🔲     | POST   | `/vouchers/apply` | Áp dụng voucher (preview) |
| 🔲     | POST   | `/vouchers/claim` | Nhận voucher bằng code    |

---

## 9. OWNER - SHOP 🔲 (Ninh)

| Status | Method | Endpoint             | Description         |
| ------ | ------ | -------------------- | ------------------- |
| 🔲     | GET    | `/owner/shop`        | Lấy thông tin shop  |
| 🔲     | POST   | `/owner/shop`        | Tạo shop            |
| 🔲     | PUT    | `/owner/shop`        | Cập nhật shop       |
| 🔲     | PUT    | `/owner/shop/status` | Mở/đóng shop        |
| 🔲     | GET    | `/owner/dashboard`   | Dashboard analytics |

---

## 10. OWNER - PRODUCTS 🔲 (Ninh)

| Status | Method | Endpoint              | Description        |
| ------ | ------ | --------------------- | ------------------ |
| 🔲     | GET    | `/owner/products`     | Danh sách sản phẩm |
| 🔲     | POST   | `/owner/products`     | Tạo sản phẩm       |
| 🔲     | PUT    | `/owner/products/:id` | Sửa sản phẩm       |
| 🔲     | PATCH  | `/owner/products/:id` | Toggle available   |
| 🔲     | DELETE | `/owner/products/:id` | Xóa sản phẩm       |

---

## 11. OWNER - ORDERS 🔲 

| Status | Method | Endpoint                      | Description            |
| ------ | ------ | ----------------------------- | ---------------------- |
| 🔲     | GET    | `/owner/orders`               | Danh sách đơn của shop |
| 🔲     | POST   | `/owner/orders/:id/confirm`   | Xác nhận đơn           |
| 🔲     | POST   | `/owner/orders/:id/preparing` | Bắt đầu chuẩn bị       |
| 🔲     | POST   | `/owner/orders/:id/ready`     | Sẵn sàng giao          |
| 🔲     | POST   | `/owner/orders/:id/cancel`    | Hủy đơn                |

---

## 12. OWNER - SHIPPERS 🔲 (Ninh)

| Status | Method | Endpoint                                   | Description |
| ------ | ------ | ------------------------------------------ | ----------- |
| 🔲     | GET    | `/owner/shippers`                          | DS shipper  |
| 🔲     | GET    | `/owner/shippers/applications`             | DS đơn xin  |
| 🔲     | POST   | `/owner/shippers/applications/:id/approve` | Duyệt       |
| 🔲     | POST   | `/owner/shippers/applications/:id/reject`  | Từ chối     |
| 🔲     | DELETE | `/owner/shippers/:id`                      | Xóa shipper |

---

## 13. SHIPPER 🔲 (Ninh)

| Status | Method | Endpoint                         | Description        |
| ------ | ------ | -------------------------------- | ------------------ |
| 🔲     | POST   | `/shipper/apply`                 | Xin vào shop       |
| 🔲     | GET    | `/shipper/applications`          | DS đơn xin của tôi |
| 🔲     | PUT    | `/shipper/status`                | Toggle online      |
| 🔲     | GET    | `/shipper/orders/available`      | Đơn có thể nhận    |
| 🔲     | POST   | `/shipper/orders/:id/claim`      | Nhận đơn           |
| 🔲     | PUT    | `/shipper/orders/:id/pickup`     | Đã lấy hàng        |
| 🔲     | PUT    | `/shipper/orders/:id/delivering` | Đang giao          |
| 🔲     | PUT    | `/shipper/orders/:id/delivered`  | Đã giao xong       |
| 🔲     | GET    | `/shipper/stats`                 | Thống kê           |

---

## 14. NOTIFY 🔲 (Hiệp)

| Status | Method | Endpoint                      | Description            |
| ------ | ------ | ----------------------------- | ---------------------- |
| 🔲     | GET    | `/notifications`              | Danh sách thông báo    |
| 🔲     | PUT    | `/notifications/:id/read`     | Đánh dấu đã đọc        |
| 🔲     | PUT    | `/notifications/read-all`     | Đánh dấu tất cả đã đọc |
| 🔲     | GET    | `/notifications/unread-count` | Số chưa đọc            |

---

## 15. WALLET 🔲 

| Status | Method | Endpoint               | Description       |
| ------ | ------ | ---------------------- | ----------------- |
| 🔲     | GET    | `/wallet`              | Thông tin ví      |
| 🔲     | GET    | `/wallet/transactions` | Lịch sử giao dịch |
| 🔲     | POST   | `/wallet/payout`       | Yêu cầu rút tiền  |

---

## 16. ADMIN ✅ (Partial)

> ⚠️ **Note:** Categories + Users APIs ✅ DONE. Shops/Payouts/Dashboard ⛔ BLOCKED.

| Status | Method | Endpoint                       | Description               |
| ------ | ------ | ------------------------------ | ------------------------- |
| ✅      | GET    | `/admin/users`                 | Danh sách users           |
| ✅      | GET    | `/admin/users/:userId`         | Chi tiết user             |
| ✅      | PUT    | `/admin/users/:userId/status`  | Ban/Unban user            |
| ✅      | GET    | `/admin/categories`            | Tất cả categories         |
| ✅      | GET    | `/admin/categories/:id`        | Chi tiết category         |
| ✅      | POST   | `/admin/categories`            | Tạo category              |
| ✅      | PUT    | `/admin/categories/:id`        | Sửa category              |
| ✅      | DELETE | `/admin/categories/:id`        | Xóa category              |
| ⛔      | GET    | `/admin/shops`                 | (BLOCKED - ShopModule)    |
| ⛔      | GET    | `/admin/shops/:shopId`         | (BLOCKED - ShopModule)    |
| ⛔      | PUT    | `/admin/shops/:shopId/status`  | (BLOCKED - ShopModule)    |
| ⛔      | GET    | `/admin/payouts`               | (BLOCKED - WalletModule)  |
| ⛔      | GET    | `/admin/payouts/:id`           | (BLOCKED - WalletModule)  |
| ⛔      | POST   | `/admin/payouts/:id/approve`   | (BLOCKED - WalletModule)  |
| ⛔      | POST   | `/admin/payouts/:id/reject`    | (BLOCKED - WalletModule)  |
| ⛔      | POST   | `/admin/payouts/:id/transferred` | (BLOCKED - WalletModule) |
| ⛔      | GET    | `/admin/dashboard`             | (BLOCKED - OrderModule)   |
| ✅      | GET    | `/admin/dashboard/users`       | User stats                |
| ⛔      | GET    | `/admin/dashboard/orders`      | (BLOCKED - OrderModule)   |
| ⛔      | GET    | `/admin/dashboard/revenue`     | (BLOCKED - OrderModule)   |

### Latest Test Results (2026-01-08)

#### ✅ USER Epic - Fully Tested

- All profile & address management endpoints working
- FCM token registration working
- Favorites system working (`/me/favorites/products/*`)
- Settings management working
- **✅ Avatar upload endpoint: IMPLEMENTED** (line 124-160 me.controller.ts)
- **✅ Delete account endpoint: IMPLEMENTED** (line 328-342 me.controller.ts)

#### ⚠️ ADMIN Epic - Blocked

- Endpoints implemented but cannot test
- **Issue:** Current test user doesn't have ADMIN role
- **Need:** `service-account.json` to grant admin privileges
- All admin endpoints are code-complete and ready for testing

## 📊 Progress Summary by Owner

| Owner     | Modules                                      | Done  | Total    |
| --------- | -------------------------------------------- | ----- | -------- |
| Hòa       | AUTH, ORDER, PAYMENT, WALLET, VOUCHER, ADMIN | 8     | ~70      |
| Hiệp      | USER, CART, NOTIFY                           | 16    | ~27      |
| Ninh      | SHOP, PRODUCT, SHIPPER                       | 0     | ~38      |
| **TOTAL** |                                              | **24**| **~135** |

---

## 📦 Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "AUTH_001",
    "message": "Token không hợp lệ",
    "details": {}
  }
}
```

### Error Codes

| Code        | Status | Description        |
| ----------- | ------ | ------------------ |
| AUTH_001    | 401    | Token không hợp lệ |
| AUTH_002    | 401    | Token hết hạn      |
| AUTH_004    | 409    | Email đã tồn tại   |
| AUTH_005    | 400    | Mật khẩu quá yếu   |
| USER_001    | 404    | User không tồn tại |
| SHOP_001    | 404    | Shop không tồn tại |
| PRODUCT_003 | 409    | Price locked       |
| CART_001    | 400    | Khác shop          |
| ORDER_002   | 400    | Invalid status     |
