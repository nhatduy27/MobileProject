# TÀI LIỆU MÔ TẢ DATABASE - KTX DELIVERY

- **Version**: 3.0
- **Last Updated**: 2026-01-07
- **Database**: Firebase Firestore (NoSQL)
- **Project**: KTX Delivery App

---

## Tổng quan

Hệ thống sử dụng **Firestore** làm database chính với cấu trúc document-based. Authentication được quản lý bởi **Firebase Auth**.

### Collections Overview

| Collection      | Status     | Mô tả                     |
| --------------- | ---------- | ------------------------- |
| `users`         | ✅ Done    | Thông tin người dùng      |
| `categories`    | 🔲 Planned | Danh mục sản phẩm (Admin) |
| `shops`         | 🔲 Planned | Cửa hàng/Quán ăn          |
| `products`      | 🔲 Planned | Sản phẩm/Món ăn           |
| `carts`         | 🔲 Planned | Giỏ hàng                  |
| `orders`        | 🔲 Planned | Đơn hàng                  |
| `vouchers`      | 🔲 Planned | Mã giảm giá               |
| `wallets`       | 🔲 Planned | Ví tiền                   |
| `transactions`  | 🔲 Planned | Giao dịch ví              |
| `notifications` | 🔲 Planned | Thông báo                 |
| `subscriptions` | 🔲 Planned | Gói dịch vụ shop          |

---

## USERS ✅ DONE

> Collection: `users/{userId}`

Thông tin người dùng, đồng bộ với Firebase Auth.

| Field         | Type    | Required | Description                           |
| ------------- | ------- | -------- | ------------------------------------- |
| `id`          | string  | ✅       | Primary Key = Firebase UID            |
| `fullName`    | string  | ✅       | Họ tên đầy đủ                         |
| `email`       | string  | ✅       | Email đăng nhập                       |
| `isVerify`    | boolean | ✅       | Trạng thái xác thực email/phone       |
| `phone`       | string  | ❌       | Số điện thoại (VN format: 09xxxxxxxx) |
| `role`        | enum    | ✅       | Vai trò: `user`, `seller`, `delivery` |
| `imageAvatar` | string  | ❌       | URL ảnh đại diện                      |
| `createdAt`   | number  | ✅       | Timestamp tạo (milliseconds)          |
| `updatedAt`   | number  | ✅       | Timestamp cập nhật cuối               |

### Roles

| Role       | Mô tả      | Quyền hạn                        |
| ---------- | ---------- | -------------------------------- |
| `user`     | Khách hàng | Đặt hàng, xem shop, giỏ hàng     |
| `seller`   | Chủ shop   | Quản lý shop, sản phẩm, đơn hàng |
| `delivery` | Shipper    | Nhận đơn, giao hàng              |

### Example Document

```json
{
  "id": "abc123xyz",
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "isVerify": true,
  "phone": "0901234567",
  "role": "user",
  "imageAvatar": "https://storage.googleapis.com/...",
  "createdAt": 1704700000000,
  "updatedAt": 1704700000000
}
```

---

## CATEGORIES 🔲

> Collection: `categories/{categoryId}`

Danh mục sản phẩm (Admin quản lý).

| Field          | Type    | Required | Description        |
| -------------- | ------- | -------- | ------------------ |
| `id`           | string  | ✅       | Primary Key        |
| `name`         | string  | ✅       | Tên danh mục       |
| `description`  | string  | ❌       | Mô tả              |
| `imageUrl`     | string  | ❌       | Ảnh danh mục       |
| `displayOrder` | number  | ✅       | Thứ tự hiển thị    |
| `isActive`     | boolean | ✅       | Hiển thị hay không |
| `createdAt`    | number  | ✅       | Timestamp tạo      |
| `updatedAt`    | number  | ✅       | Timestamp cập nhật |

---

## SHOPS 🔲

> Collection: `shops/{shopId}`

Thông tin cửa hàng của Seller.

| Field              | Type    | Required | Description                        |
| ------------------ | ------- | -------- | ---------------------------------- |
| `id`               | string  | ✅       | Primary Key (auto-generated)       |
| `ownerId`          | string  | ✅       | FK → users.id (Seller)             |
| `name`             | string  | ✅       | Tên cửa hàng                       |
| `description`      | string  | ❌       | Mô tả                              |
| `phone`            | string  | ✅       | SĐT liên hệ                        |
| `address`          | string  | ✅       | Địa chỉ                            |
| `imageUrl`         | string  | ❌       | Ảnh đại diện shop                  |
| `coverUrl`         | string  | ❌       | Ảnh bìa                            |
| `isOpen`           | boolean | ✅       | Trạng thái mở/đóng                 |
| `rating`           | number  | ❌       | Đánh giá trung bình (1-5)          |
| `totalOrders`      | number  | ✅       | Tổng số đơn                        |
| `status`           | enum    | ✅       | `PENDING`, `APPROVED`, `SUSPENDED` |
| `priceLockedSince` | number  | ❌       | Timestamp khóa giá (khi shop mở)   |
| `openingHours`     | object  | ❌       | Giờ mở cửa                         |
| `createdAt`        | number  | ✅       | Timestamp tạo                      |
| `updatedAt`        | number  | ✅       | Timestamp cập nhật                 |

---

## PRODUCTS 🔲

> Collection: `products/{productId}`

Sản phẩm/Món ăn của shop.

| Field          | Type    | Required | Description        |
| -------------- | ------- | -------- | ------------------ |
| `id`           | string  | ✅       | Primary Key        |
| `shopId`       | string  | ✅       | FK → shops.id      |
| `categoryId`   | string  | ✅       | FK → categories.id |
| `name`         | string  | ✅       | Tên sản phẩm       |
| `description`  | string  | ❌       | Mô tả              |
| `price`        | number  | ✅       | Giá (VND)          |
| `imageUrl`     | string  | ❌       | Ảnh sản phẩm       |
| `isAvailable`  | boolean | ✅       | Còn hàng           |
| `isActive`     | boolean | ✅       | Hiển thị           |
| `displayOrder` | number  | ✅       | Thứ tự             |
| `createdAt`    | number  | ✅       | Timestamp tạo      |
| `updatedAt`    | number  | ✅       | Timestamp cập nhật |

---

## CARTS 🔲

> Collection: `carts/{userId}`

Giỏ hàng (1 user = 1 cart).

| Field       | Type   | Required | Description             |
| ----------- | ------ | -------- | ----------------------- |
| `id`        | string | ✅       | = userId                |
| `shopId`    | string | ❌       | Shop hiện tại trong giỏ |
| `items`     | array  | ✅       | Danh sách sản phẩm      |
| `updatedAt` | number | ✅       | Timestamp cập nhật      |

### Cart Item Structure

```json
{
  "productId": "prod_001",
  "name": "Phở bò",
  "price": 45000,
  "quantity": 2,
  "note": "Ít hành"
}
```

---

## ORDERS 🔲

> Collection: `orders/{orderId}`

Đơn hàng.

| Field             | Type   | Required | Description                             |
| ----------------- | ------ | -------- | --------------------------------------- |
| `id`              | string | ✅       | Primary Key                             |
| `orderNumber`     | string | ✅       | Mã đơn (KTX-YYYYMMDD-XXXX)              |
| `customerId`      | string | ✅       | FK → users.id                           |
| `shopId`          | string | ✅       | FK → shops.id                           |
| `shipperId`       | string | ❌       | FK → users.id (Shipper)                 |
| `items`           | array  | ✅       | Danh sách sản phẩm                      |
| `subtotal`        | number | ✅       | Tổng tiền hàng                          |
| `shippingFee`     | number | ✅       | Phí ship                                |
| `discount`        | number | ✅       | Giảm giá                                |
| `total`           | number | ✅       | Tổng thanh toán                         |
| `paymentMethod`   | enum   | ✅       | `COD`, `ZALOPAY`, `MOMO`, `SEPAY`       |
| `paymentStatus`   | enum   | ✅       | `PENDING`, `PAID`, `FAILED`, `REFUNDED` |
| `status`          | enum   | ✅       | Trạng thái đơn (xem State Machine)      |
| `deliveryAddress` | string | ✅       | Địa chỉ giao                            |
| `note`            | string | ❌       | Ghi chú                                 |
| `voucherId`       | string | ❌       | FK → vouchers.id                        |
| `cancelReason`    | string | ❌       | Lý do hủy                               |
| `cancelledBy`     | string | ❌       | Ai hủy                                  |
| `createdAt`       | number | ✅       | Timestamp tạo                           |
| `updatedAt`       | number | ✅       | Timestamp cập nhật                      |
| `confirmedAt`     | number | ❌       | Timestamp xác nhận                      |
| `pickedAt`        | number | ❌       | Timestamp shipper lấy hàng              |
| `deliveredAt`     | number | ❌       | Timestamp giao xong                     |

### Order Status Flow

```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → PICKED_UP → DELIVERING → DELIVERED
    ↓         ↓           ↓
 CANCELLED CANCELLED  CANCELLED
```

---

## VOUCHERS 🔲

> Collection: `vouchers/{voucherId}`

Mã giảm giá.

| Field           | Type    | Required | Description                             |
| --------------- | ------- | -------- | --------------------------------------- |
| `id`            | string  | ✅       | Primary Key                             |
| `code`          | string  | ✅       | Mã voucher (unique)                     |
| `shopId`        | string  | ❌       | null = Admin voucher, có = Shop voucher |
| `type`          | enum    | ✅       | `PERCENT`, `FIXED`                      |
| `value`         | number  | ✅       | Giá trị giảm                            |
| `minOrderValue` | number  | ✅       | Đơn tối thiểu                           |
| `maxDiscount`   | number  | ❌       | Giảm tối đa (cho PERCENT)               |
| `totalQuantity` | number  | ✅       | Tổng số lượng                           |
| `usedQuantity`  | number  | ✅       | Đã sử dụng                              |
| `startDate`     | number  | ✅       | Ngày bắt đầu                            |
| `endDate`       | number  | ✅       | Ngày kết thúc                           |
| `isActive`      | boolean | ✅       | Đang hoạt động                          |
| `createdAt`     | number  | ✅       | Timestamp tạo                           |

---

## WALLETS 🔲

> Collection: `wallets/{userId}`

Ví tiền (cho Seller & Shipper).

| Field            | Type   | Required | Description        |
| ---------------- | ------ | -------- | ------------------ |
| `id`             | string | ✅       | = userId           |
| `balance`        | number | ✅       | Số dư khả dụng     |
| `pendingBalance` | number | ✅       | Số dư chờ xử lý    |
| `totalEarnings`  | number | ✅       | Tổng thu nhập      |
| `totalWithdrawn` | number | ✅       | Tổng đã rút        |
| `updatedAt`      | number | ✅       | Timestamp cập nhật |

---

## TRANSACTIONS 🔲

> Collection: `transactions/{transactionId}`

Lịch sử giao dịch ví.

| Field           | Type   | Required | Description                 |
| --------------- | ------ | -------- | --------------------------- |
| `id`            | string | ✅       | Primary Key                 |
| `walletId`      | string | ✅       | FK → wallets.id             |
| `type`          | enum   | ✅       | `CREDIT`, `DEBIT`           |
| `amount`        | number | ✅       | Số tiền                     |
| `balanceAfter`  | number | ✅       | Số dư sau giao dịch         |
| `description`   | string | ✅       | Mô tả                       |
| `referenceType` | enum   | ❌       | `ORDER`, `PAYOUT`, `REFUND` |
| `referenceId`   | string | ❌       | ID tham chiếu               |
| `createdAt`     | number | ✅       | Timestamp tạo               |

---

## NOTIFICATIONS 🔲

> Collection: `notifications/{notificationId}`

Thông báo.

| Field       | Type    | Required | Description      |
| ----------- | ------- | -------- | ---------------- |
| `id`        | string  | ✅       | Primary Key      |
| `userId`    | string  | ✅       | FK → users.id    |
| `type`      | enum    | ✅       | Loại thông báo   |
| `title`     | string  | ✅       | Tiêu đề          |
| `body`      | string  | ✅       | Nội dung         |
| `data`      | object  | ❌       | Dữ liệu đính kèm |
| `isRead`    | boolean | ✅       | Đã đọc           |
| `createdAt` | number  | ✅       | Timestamp tạo    |

---

## SUBSCRIPTIONS 🔲

> Collection: `subscriptions/{subscriptionId}`

Gói dịch vụ shop.

| Field       | Type   | Required | Description                      |
| ----------- | ------ | -------- | -------------------------------- |
| `id`        | string | ✅       | Primary Key                      |
| `shopId`    | string | ✅       | FK → shops.id                    |
| `plan`      | enum   | ✅       | `FREE`, `BASIC`, `PREMIUM`       |
| `status`    | enum   | ✅       | `ACTIVE`, `EXPIRED`, `CANCELLED` |
| `startDate` | number | ✅       | Ngày bắt đầu                     |
| `endDate`   | number | ✅       | Ngày kết thúc                    |
| `createdAt` | number | ✅       | Timestamp tạo                    |
