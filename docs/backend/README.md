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

#### `AuthModule`

- Đăng ký email/password
- Đăng nhập / Xác thực token
- Google Sign-In
- Profile CRUD
- Role selection (user/seller/delivery)

### 🔲 Planned

| Module               | Description                   |
| -------------------- | ----------------------------- |
| `CategoriesModule`   | Quản lý danh mục (Admin)      |
| `ShopsModule`        | CRUD shop, trạng thái mở/đóng |
| `ProductsModule`     | CRUD sản phẩm của shop        |
| `CartModule`         | Giỏ hàng khách hàng           |
| `OrdersModule`       | Luồng đơn hàng                |
| `VouchersModule`     | Mã giảm giá                   |
| `WalletModule`       | Ví tiền Seller/Shipper        |
| `ShipperModule`      | Nhận và giao đơn              |
| `NotificationModule` | Push notifications            |

## 5. Database

- **Type:** Firestore (NoSQL Document Database)
- **Collections:** users, shops, products, orders, carts, vouchers, wallets, transactions, notifications

Xem chi tiết:

- [Database Description](database/description.md)
- [ER Diagram](database/er_diagram.md)

## 6. API Reference

- [OpenAPI Specification](../common/OPENAPI.md) - Danh sách endpoints với status
- Swagger UI: http://localhost:3000/api/docs

### Authentication

Tất cả API (trừ public) yêu cầu header:

```
Authorization: Bearer <firebase-id-token>
```