# OpenAPI Specification - KTX Delivery API

> **Version:** 1.0.0  
> **Updated:** 2026-01-07  
> **Base URL:** `http://localhost:3000/api`  
> **Swagger UI:** `http://localhost:3000/api/docs`

---

## 📋 Implementation Status

| Symbol | Meaning              |
| ------ | -------------------- |
| ✅     | Implemented & Tested |
| 🔲     | Not Started          |
| 🚧     | In Progress          |

---

## 🔐 Authentication

Tất cả endpoints yêu cầu header (trừ đánh dấu 🔓 Public):

```
Authorization: Bearer <firebase-id-token>
```

---

# API Endpoints

## 1. AUTH ✅ DONE

| Status | Method | Endpoint             | Description              |
| ------ | ------ | -------------------- | ------------------------ |
| ✅     | POST   | `/auth/register`     | 🔓 Đăng ký tài khoản mới |
| ✅     | POST   | `/auth/login`        | 🔓 Hướng dẫn đăng nhập   |
| ✅     | POST   | `/auth/verify-token` | 🔓 Xác thực ID Token     |
| ✅     | POST   | `/auth/google`       | 🔓 Google Sign-In        |
| ✅     | GET    | `/auth/profile`      | Lấy thông tin profile    |
| ✅     | PUT    | `/auth/profile`      | Cập nhật profile         |
| ✅     | PUT    | `/auth/role`         | Cập nhật vai trò         |
| ✅     | DELETE | `/auth/account`      | Xóa tài khoản            |

### POST /auth/register ✅

Đăng ký tài khoản mới với email/password.

**Request:**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "password": "Password123",
  "phone": "0901234567" // optional
}
```

**Response:** `201 Created`

```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "user": {
      "id": "abc123",
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "isVerify": false,
      "phone": "",
      "role": "user",
      "imageAvatar": "",
      "createdAt": 1704700000000,
      "updatedAt": 1704700000000
    },
    "uid": "abc123"
  }
}
```

### POST /auth/verify-token ✅

Xác thực Firebase ID Token và trả về user profile.

**Request:**

```json
{
  "idToken": "eyJhbGciOiJS..."
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Xác thực thành công",
  "data": {
    "user": { ... }
  }
}
```

### POST /auth/google ✅

Đăng nhập bằng Google.

**Request:**

```json
{
  "idToken": "firebase-id-token-from-google-signin"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "user": { ... },
    "isNewUser": true
  }
}
```

### GET /auth/profile ✅

Lấy thông tin profile user hiện tại.

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "abc123",
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "isVerify": true,
    "phone": "0901234567",
    "role": "user",
    "imageAvatar": "",
    "createdAt": 1704700000000,
    "updatedAt": 1704700000000
  }
}
```

### PUT /auth/profile ✅

Cập nhật thông tin profile.

**Headers:** `Authorization: Bearer <token>`

**Request:**

```json
{
  "fullName": "Nguyễn Văn B",
  "phone": "0909876543",
  "imageAvatar": "https://example.com/avatar.jpg"
}
```

### PUT /auth/role ✅

Cập nhật vai trò (Role Selection screen).

**Headers:** `Authorization: Bearer <token>`

**Request:**

```json
{
  "role": "seller" // user | seller | delivery
}
```

### DELETE /auth/account ✅

Xóa tài khoản (Firebase Auth + Firestore).

**Headers:** `Authorization: Bearer <token>`

---

## 2. CATEGORIES 🔲

| Status | Method | Endpoint                 | Description           |
| ------ | ------ | ------------------------ | --------------------- |
| 🔲     | GET    | `/categories`            | 🔓 Danh sách danh mục |
| 🔲     | POST   | `/admin/categories`      | [Admin] Tạo danh mục  |
| 🔲     | PUT    | `/admin/categories/{id}` | [Admin] Sửa danh mục  |
| 🔲     | DELETE | `/admin/categories/{id}` | [Admin] Xóa danh mục  |

---

## 3. SHOPS (Customer) 🔲

| Status | Method | Endpoint                   | Description               |
| ------ | ------ | -------------------------- | ------------------------- |
| 🔲     | GET    | `/shops`                   | 🔓 Danh sách shop đang mở |
| 🔲     | GET    | `/shops/{shopId}`          | 🔓 Chi tiết shop          |
| 🔲     | GET    | `/shops/{shopId}/products` | 🔓 Menu của shop          |

---

## 4. CART 🔲

| Status | Method | Endpoint                  | Description       |
| ------ | ------ | ------------------------- | ----------------- |
| 🔲     | GET    | `/cart`                   | Lấy giỏ hàng      |
| 🔲     | POST   | `/cart/items`             | Thêm sản phẩm     |
| 🔲     | PATCH  | `/cart/items/{productId}` | Cập nhật số lượng |
| 🔲     | DELETE | `/cart/items/{productId}` | Xóa sản phẩm      |
| 🔲     | DELETE | `/cart`                   | Xóa toàn bộ giỏ   |

---

## 5. ORDERS (Customer) 🔲

| Status | Method | Endpoint                   | Description           |
| ------ | ------ | -------------------------- | --------------------- |
| 🔲     | POST   | `/orders`                  | Tạo đơn hàng          |
| 🔲     | GET    | `/orders`                  | Danh sách đơn của tôi |
| 🔲     | GET    | `/orders/{orderId}`        | Chi tiết đơn          |
| 🔲     | POST   | `/orders/{orderId}/cancel` | Hủy đơn               |

---

## 6. VOUCHERS (Customer) 🔲

| Status | Method | Endpoint          | Description               |
| ------ | ------ | ----------------- | ------------------------- |
| 🔲     | GET    | `/vouchers/my`    | Voucher của tôi           |
| 🔲     | POST   | `/vouchers/apply` | Áp dụng voucher (preview) |
| 🔲     | POST   | `/vouchers/claim` | Nhận voucher bằng code    |

---

## 7. SELLER - SHOP 🔲

| Status | Method | Endpoint             | Description            |
| ------ | ------ | -------------------- | ---------------------- |
| 🔲     | GET    | `/seller/shop`       | Lấy thông tin shop     |
| 🔲     | POST   | `/seller/shop`       | Tạo shop               |
| 🔲     | PATCH  | `/seller/shop`       | Cập nhật shop          |
| 🔲     | POST   | `/seller/shop/open`  | Mở shop (lock giá)     |
| 🔲     | POST   | `/seller/shop/close` | Đóng shop (unlock giá) |

---

## 8. SELLER - PRODUCTS 🔲

| Status | Method | Endpoint                | Description        |
| ------ | ------ | ----------------------- | ------------------ |
| 🔲     | GET    | `/seller/products`      | Danh sách sản phẩm |
| 🔲     | POST   | `/seller/products`      | Tạo sản phẩm       |
| 🔲     | PUT    | `/seller/products/{id}` | Sửa sản phẩm       |
| 🔲     | DELETE | `/seller/products/{id}` | Xóa sản phẩm       |

---

## 9. SELLER - ORDERS 🔲

| Status | Method | Endpoint                      | Description            |
| ------ | ------ | ----------------------------- | ---------------------- |
| 🔲     | GET    | `/seller/orders`              | Danh sách đơn của shop |
| 🔲     | POST   | `/seller/orders/{id}/confirm` | Xác nhận đơn           |
| 🔲     | POST   | `/seller/orders/{id}/ready`   | Đánh dấu sẵn sàng      |
| 🔲     | POST   | `/seller/orders/{id}/cancel`  | Hủy đơn                |

---

## 10. SELLER - SHIPPER POOL 🔲

| Status | Method | Endpoint                                 | Description                |
| ------ | ------ | ---------------------------------------- | -------------------------- |
| 🔲     | GET    | `/seller/shippers`                       | Danh sách shipper của shop |
| 🔲     | POST   | `/seller/shippers/invite`                | Mời shipper                |
| 🔲     | DELETE | `/seller/shippers/{id}`                  | Xóa shipper                |
| 🔲     | GET    | `/seller/shippers/requests`              | Yêu cầu tham gia           |
| 🔲     | POST   | `/seller/shippers/requests/{id}/approve` | Duyệt yêu cầu              |
| 🔲     | POST   | `/seller/shippers/requests/{id}/reject`  | Từ chối yêu cầu            |

---

## 11. SELLER - VOUCHERS 🔲

| Status | Method | Endpoint                | Description            |
| ------ | ------ | ----------------------- | ---------------------- |
| 🔲     | GET    | `/seller/vouchers`      | Danh sách voucher shop |
| 🔲     | POST   | `/seller/vouchers`      | Tạo voucher            |
| 🔲     | PUT    | `/seller/vouchers/{id}` | Sửa voucher            |
| 🔲     | DELETE | `/seller/vouchers/{id}` | Xóa voucher            |

---

## 12. SELLER - SUBSCRIPTION 🔲

| Status | Method | Endpoint                       | Description   |
| ------ | ------ | ------------------------------ | ------------- |
| 🔲     | GET    | `/seller/subscription`         | Gói hiện tại  |
| 🔲     | GET    | `/seller/subscription/plans`   | Danh sách gói |
| 🔲     | POST   | `/seller/subscription/upgrade` | Nâng cấp gói  |
| 🔲     | POST   | `/seller/subscription/cancel`  | Hủy gói       |

---

## 13. SHIPPER 🔲

| Status | Method | Endpoint                         | Description        |
| ------ | ------ | -------------------------------- | ------------------ |
| 🔲     | GET    | `/shipper/available-orders`      | Đơn có thể nhận    |
| 🔲     | POST   | `/shipper/orders/{id}/accept`    | Nhận đơn           |
| 🔲     | GET    | `/shipper/orders`                | Đơn đang giao      |
| 🔲     | POST   | `/shipper/orders/{id}/picked`    | Đã lấy hàng        |
| 🔲     | POST   | `/shipper/orders/{id}/delivered` | Đã giao xong       |
| 🔲     | POST   | `/shipper/orders/{id}/failed`    | Giao thất bại      |
| 🔲     | GET    | `/shipper/earnings`              | Thu nhập           |
| 🔲     | GET    | `/shipper/history`               | Lịch sử giao hàng  |
| 🔲     | GET    | `/shipper/shops`                 | Shop đang tham gia |
| 🔲     | POST   | `/shipper/shops/{id}/request`    | Xin vào shop       |

---

## 14. WALLET 🔲

| Status | Method | Endpoint               | Description       |
| ------ | ------ | ---------------------- | ----------------- |
| 🔲     | GET    | `/wallet`              | Thông tin ví      |
| 🔲     | GET    | `/wallet/transactions` | Lịch sử giao dịch |
| 🔲     | POST   | `/wallet/payout`       | Yêu cầu rút tiền  |

---

## 15. WEBHOOKS 🔲

| Status | Method | Endpoint            | Description      |
| ------ | ------ | ------------------- | ---------------- |
| 🔲     | POST   | `/webhooks/zalopay` | ZaloPay callback |
| 🔲     | POST   | `/webhooks/momo`    | MoMo callback    |
| 🔲     | POST   | `/webhooks/sepay`   | SePay callback   |

---

## 16. ADMIN 🔲

| Status | Method | Endpoint                    | Description     |
| ------ | ------ | --------------------------- | --------------- |
| 🔲     | GET    | `/admin/users`              | Danh sách users |
| 🔲     | GET    | `/admin/shops`              | Danh sách shops |
| 🔲     | POST   | `/admin/shops/{id}/approve` | Duyệt shop      |
| 🔲     | POST   | `/admin/shops/{id}/suspend` | Khóa shop       |

---

## 📊 Progress Summary

| Module              | Endpoints | Done  | Progress |
| ------------------- | --------- | ----- | -------- |
| Auth                | 8         | 8     | ✅ 100%  |
| Categories          | 4         | 0     | 🔲 0%    |
| Shops (Customer)    | 3         | 0     | 🔲 0%    |
| Cart                | 5         | 0     | 🔲 0%    |
| Orders (Customer)   | 4         | 0     | 🔲 0%    |
| Vouchers (Customer) | 3         | 0     | 🔲 0%    |
| Seller Shop         | 5         | 0     | 🔲 0%    |
| Seller Products     | 4         | 0     | 🔲 0%    |
| Seller Orders       | 4         | 0     | 🔲 0%    |
| Seller Shippers     | 6         | 0     | 🔲 0%    |
| Seller Vouchers     | 4         | 0     | 🔲 0%    |
| Seller Subscription | 4         | 0     | 🔲 0%    |
| Shipper             | 10        | 0     | 🔲 0%    |
| Wallet              | 3         | 0     | 🔲 0%    |
| Webhooks            | 3         | 0     | 🔲 0%    |
| Admin               | 4         | 0     | 🔲 0%    |
| **TOTAL**           | **74**    | **8** | **11%**  |

---

## 📦 Error Response Format

```json
{
  "statusCode": 400,
  "code": "AUTH_1011",
  "message": "Email đã được sử dụng",
  "timestamp": "2026-01-07T10:00:00Z"
}
```

### Error Codes

| Code      | Description             |
| --------- | ----------------------- |
| AUTH_1001 | Token không hợp lệ      |
| AUTH_1002 | Token hết hạn           |
| AUTH_1005 | Không tìm thấy user     |
| AUTH_1011 | Email đã tồn tại        |
| AUTH_1012 | Email không hợp lệ      |
| AUTH_1013 | Mật khẩu quá yếu        |
| AUTH_1014 | Đăng ký thất bại        |
| AUTH_1016 | Google Sign-In thất bại |

Xem đầy đủ tại `shared/constants/error-codes.ts`
