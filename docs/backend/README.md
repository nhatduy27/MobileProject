# Backend API Documentation - KTX Delivery

> **Version:** 1.0.0  
> **Updated:** 2026-01-07

## 1. Giới thiệu

Backend API cho **KTX Delivery** - ứng dụng đặt đồ ăn dành cho sinh viên KTX.

| Stack     | Technology               |
| --------- | ------------------------ |
| Framework | NestJS 10.x              |
| Runtime   | Firebase Functions Gen 2 |
| Database  | Firestore (NoSQL)        |
| Auth      | Firebase Authentication  |
| Language  | TypeScript               |

## 2. Quick Start

### Prerequisites

- Node.js >= 22
- Java JDK >= 17 (cho Firebase Emulators)
- Firebase CLI: `npm install -g firebase-tools`

### Chạy Development

```bash
cd MobileProject/Backend/functions

# Install dependencies
npm install

# Build TypeScript
npm run build

# Option 1: NestJS standalone (port 3000) - connects to PRODUCTION Firebase
npm start

# Option 2: Firebase Emulators (recommended for development)
npm run emulators
```

### URLs

| Service          | URL                            |
| ---------------- | ------------------------------ |
| API (standalone) | http://localhost:3000/api      |
| Swagger Docs     | http://localhost:3000/api/docs |
| Emulator UI      | http://localhost:4000          |

## 3. Project Structure

```
Backend/
├── firebase.json          # Firebase config
├── functions/
│   ├── package.json
│   ├── tsconfig.json
│   ├── service-account.json  # ⚠️ Git ignored
│   ├── src/
│   │   ├── index.ts          # Firebase entry point
│   │   ├── main.ts           # NestJS standalone entry
│   │   ├── app.module.ts     # Root module
│   │   ├── core/             # Core services
│   │   │   └── firebase/     # Firebase Admin SDK
│   │   ├── shared/           # Shared utilities
│   │   │   ├── constants/    # Error codes, enums
│   │   │   ├── decorators/   # Custom decorators
│   │   │   ├── filters/      # Exception filters
│   │   │   └── guards/       # Auth guards
│   │   └── modules/          # Feature modules
│   │       ├── auth/         # ✅ Done
│   │       ├── categories/   # 🔲 Planned
│   │       ├── shops/        # 🔲 Planned
│   │       ├── products/     # 🔲 Planned
│   │       ├── cart/         # 🔲 Planned
│   │       ├── orders/       # 🔲 Planned
│   │       └── ...
│   └── lib/                  # Compiled JS output
```

## 4. Modules

### ✅ Implemented

#### `AuthModule` - **COMPLETED**

**9 Authentication APIs:**

- ✅ Register (email/password)
- ✅ Login (email/password)
- ✅ Google Sign-In
- ✅ Send OTP (email verification)
- ✅ Verify OTP
- ✅ Forgot Password
- ✅ Reset Password
- ✅ Change Password (protected)
- ✅ Logout (protected)

📖 **[Authentication Guide](AUTH_GUIDE.md)**

---

#### `UsersModule` - **COMPLETED**

**16 User Profile APIs:**

- ✅ GET/PUT /me (profile)
- ✅ POST /me/avatar (upload)
- ✅ DELETE /me (delete account)
- ✅ CRUD /me/addresses
- ✅ GET/PUT /me/settings
- ✅ PUT /me/fcm-token
- ✅ CRUD /me/favorites/products

📖 **[User Guide](USER_GUIDE.md)**

---

#### `AdminModule` - **PARTIAL**

- ✅ Users management (list, ban/unban)
- ✅ Categories management (CRUD)
- ⛔ Shops management (blocked by ShopModule)
- ⛔ Payouts management (blocked by WalletModule)

📖 **[Admin Guide](ADMIN_GUIDE.md)**

---

### 🔲 Planned

| Module               | Status         | Description                               |
| -------------------- | -------------- | ----------------------------------------- |
| `CategoriesModule`   | ✅ Done        | Quản lý danh mục (Admin + Public)         |
| `UsersModule`        | ✅ Done        | Profile, addresses, settings              |
| `FavoritesModule`    | ✅ Done        | Favorite products                         |
| `AdminModule`        | 🟡 Partial     | Categories ✅, Users ✅, Shops/Payouts ⛔ |
| `ShopsModule`        | 🔴 Not Started | CRUD shop, trạng thái mở/đóng             |
| `ProductsModule`     | 🔴 Not Started | CRUD sản phẩm của shop                    |
| `CartModule`         | 🔴 Not Started | Giỏ hàng khách hàng                       |
| `OrdersModule`       | 🔴 Not Started | Luồng đơn hàng                            |
| `VouchersModule`     | 🔴 Not Started | Mã giảm giá                               |
| `WalletModule`       | 🔴 Not Started | Ví tiền Seller/Shipper                    |
| `ShipperModule`      | 🔴 Not Started | Nhận và giao đơn                          |
| `NotificationModule` | 🔴 Not Started | Push notifications                        |

## 5. Database

- **Type:** Firestore (NoSQL Document Database)
- **Collections:** users, shops, products, orders, carts, vouchers, wallets, transactions, notifications

Xem chi tiết:

- [Database Description](database/description.md)
- [ER Diagram](database/er_diagram.md)

## 6. API Reference

| Document                                                    | Description                          |
| ----------------------------------------------------------- | ------------------------------------ |
| [Authentication Guide](AUTH_GUIDE.md)                       | Auth flow chi tiết                   |
| [User Guide](USER_GUIDE.md)                                 | User profile, addresses, favorites   |
| [Admin Guide](ADMIN_GUIDE.md)                               | Admin management APIs                |
| **[Payment & Payout QR Guide](PAYMENT_WALLET_QR_GUIDE.md)** | **🆕 SePay QR flows - Step by step** |
| [Test Accounts](TEST_ACCOUNTS.md)                           | Accounts có sẵn để test              |
| [OpenAPI Specification](../common/OPENAPI.md)               | Danh sách endpoints với status       |
| Swagger UI                                                  | http://localhost:3000/api/docs       |

### Authentication

Hệ thống sử dụng **Firebase ID Token** cho protected APIs.

**Flow:**

1. Client call `POST /auth/register` hoặc `POST /auth/login`
2. Backend trả về `customToken`
3. Client sign in Firebase: `signInWithCustomToken(customToken)`
4. Client lấy ID token: `user.getIdToken()`
5. Client dùng ID token cho protected APIs:

```http
Authorization: Bearer <firebase-id-token>
```

**Testing trên Swagger:**

```bash
cd Backend/functions
node get-id-token.js your-email@example.com
# Copy token và paste vào Swagger Authorize button
```

📖 Xem chi tiết: [AUTH_GUIDE.md](AUTH_GUIDE.md)
