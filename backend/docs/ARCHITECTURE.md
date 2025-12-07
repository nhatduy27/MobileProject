# Kiến Trúc Backend

> **Tài liệu tổng quan về kiến trúc backend của dự án Mobile Food Delivery App sử dụng Firebase Cloud Functions.**

---

## 📐 Tổng Quan

Backend được xây dựng trên **Firebase Cloud Functions v2** với **Layered Architecture (Kiến trúc phân lớp)** để đảm bảo:

- **Separation of Concerns** - Mỗi lớp có trách nhiệm riêng biệt
- **Maintainability** - Dễ bảo trì và mở rộng
- **Testability** - Dễ viết unit tests và integration tests
- **Scalability** - Dễ thêm features mới

### Stack Công Nghệ

- **Runtime:** Firebase Cloud Functions v2 (Node.js 20)
- **Language:** TypeScript 5.x
- **Database:** Cloud Firestore
- **Authentication:** Firebase Auth
- **Storage:** Cloud Storage
- **Notifications:** Firebase Cloud Messaging (FCM)

---

## 🏗️ Kiến Trúc Phân Lớp

Backend được tổ chức theo 5 lớp, mỗi lớp giao tiếp với lớp liền kề:

```
┌─────────────────────────────────────┐
│      TRIGGERS (Entry Points)        │  ← HTTP requests, Firestore events
│  - Callable Functions               │
│  - Firestore Triggers               │
│  - Auth Triggers                    │
└──────────────┬──────────────────────┘
               │ validates & delegates
               ▼
┌─────────────────────────────────────┐
│       SERVICES (Business Logic)     │  ← Business rules & calculations
│  - Order Service                    │
│  - Promotion Service                │
│  - Notification Service             │
└──────────────┬──────────────────────┘
               │ calls repositories
               ▼
┌─────────────────────────────────────┐
│    REPOSITORIES (Data Access)       │  ← Firestore operations
│  - Order Repository                 │
│  - User Repository                  │
│  - Restaurant Repository            │
└──────────────┬──────────────────────┘
               │ uses models
               ▼
┌─────────────────────────────────────┐
│        MODELS (Type Definitions)    │  ← TypeScript interfaces
│  - Order, User, Restaurant          │
└─────────────────────────────────────┘
               │
┌─────────────────────────────────────┐
│           UTILS (Helpers)           │  ← Reusable functions
│  - Error handling & validation      │
└─────────────────────────────────────┘
```

### Trách Nhiệm Từng Lớp

| Lớp | Trách Nhiệm | Ví Dụ |
|-----|-------------|-------|
| **Triggers** | Validate input format, extract auth, gọi service | Check `restaurantId` not empty |
| **Services** | Business logic, calculations, orchestration | Validate restaurant open, calculate total |
| **Repositories** | Database operations (CRUD, queries) | Save order to Firestore |
| **Models** | Type definitions (interfaces, enums) | `interface Order`, `type OrderStatus` |
| **Utils** | Reusable helpers (error, validation) | `toHttpsError()`, `isValidEmail()` |

---

## 🔄 Dòng Chảy Dữ Liệu

### Ví Dụ: Đặt Hàng (Place Order)

```
CLIENT (Mobile App)
       │
       ├─ Gọi placeOrder({ restaurantId, items })
       │
       ▼
TRIGGER (api.order.ts)
       │
       ├─ Validate input format
       ├─ Extract userId từ auth
       │
       ▼
SERVICE (order.service.ts)
       │
       ├─ Validate restaurant open
       ├─ Validate menu items available
       ├─ Calculate total amount
       ├─ Apply promotion if provided
       │
       ▼
REPOSITORY (order.repository.ts)
       │
       ├─ Save order to Firestore
       │
       ▼
FIRESTORE (Database)
       │
       ├─ Order document created
       │
       ▼
TRIGGER (order.trigger.ts - onOrderCreated)
       │
       ├─ Send notification to seller
       ├─ Update restaurant stats
       ├─ Log analytics event
       │
       ▼
Response → CLIENT
{ orderId, status, totalAmount }
```

**Chi tiết implementation:** Xem [LAYERED_ARCHITECTURE.md](./LAYERED_ARCHITECTURE.md)

---

## 🎯 Nguyên Tắc Kiến Trúc

### 1. Không Logic Trong Triggers

Triggers chỉ là entry points - **không được chứa business logic**.

```typescript
// ❌ BAD - Logic trong trigger
export const placeOrder = onCall(async (request) => {
  const restaurant = await db.collection("restaurants").doc(...).get();
  if (!restaurant.data()?.isOpen) throw new Error(...);
  // ... 50 dòng business logic
});

// ✅ GOOD - Logic trong service
export const placeOrder = onCall(async (request) => {
  return await orderService.placeOrder(request.data, request.auth.uid);
});
```

**Lý do:** Reusability, testability, maintainability. Chi tiết: [ADR-003](./ADR/ADR-003-No-Logic-In-Triggers.md)

### 2. Services Là Pure Business Logic

Services chứa tất cả business rules, calculations, validations - **không phụ thuộc vào HTTP request format**.

```typescript
// ✅ Service không biết về HTTP request
export class OrderService {
  async placeOrder(data: PlaceOrderRequest, userId: string) {
    // Validate business rules
    // Calculate totals
    // Call repositories
  }
}
```

### 3. Repositories Chỉ Data Operations

Repositories chỉ tương tác với Firestore - **không có business logic**.

```typescript
// ✅ Repository chỉ CRUD operations
export class OrderRepository {
  async create(data: Order): Promise<string> {
    const docRef = await this.db.collection("orders").add(data);
    return docRef.id;
  }
}
```

### 4. Models Độc Lập

Models định nghĩa types - **không import services/repositories**.

```typescript
// ✅ Pure type definitions
export interface Order {
  id: string;
  userId: string;
  status: OrderStatus;
  // ...
}
```

---

## 📚 Architecture Decision Records (ADRs)

Các quyết định kiến trúc quan trọng được ghi lại trong thư mục `ADR/`:

### ADR-001: Tại Sao Chọn Firebase Functions?

Firebase Functions được chọn vì phù hợp với team nhỏ, không cần DevOps, và tích hợp seamless với Firebase ecosystem (Auth, Firestore, Storage).

**Chi tiết:** [ADR-001-Why-Firebase-Functions.md](./ADR/ADR-001-Why-Firebase-Functions.md)

### ADR-002: Tại Sao Chọn Layered Architecture?

Layered Architecture giúp tách biệt trách nhiệm, dễ test, maintain, và scale. Mỗi lớp có vai trò rõ ràng và chỉ phụ thuộc vào lớp liền kề.

**Chi tiết:** [ADR-002-Layered-Architecture.md](./ADR/ADR-002-Layered-Architecture.md)

### ADR-003: Tại Sao Không Logic Trong Triggers?

Triggers chỉ là entry points. Business logic phải ở Services để đảm bảo reusability, testability, và maintainability.

**Chi tiết:** [ADR-003-No-Logic-In-Triggers.md](./ADR/ADR-003-No-Logic-In-Triggers.md)

---

## 🔗 Cấu Trúc Thư Mục

```
backend/
├── functions/
│   ├── src/
│   │   ├── triggers/          # Entry points (HTTP, Firestore, Auth)
│   │   │   ├── api.order.ts
│   │   │   ├── api.promotion.ts
│   │   │   ├── order.trigger.ts
│   │   │   └── auth.trigger.ts
│   │   ├── services/          # Business logic
│   │   │   ├── order.service.ts
│   │   │   ├── promotion.service.ts
│   │   │   └── notification.service.ts
│   │   ├── repositories/      # Data access layer
│   │   │   ├── order.repository.ts
│   │   │   ├── user.repository.ts
│   │   │   └── restaurant.repository.ts
│   │   ├── models/            # TypeScript types
│   │   │   ├── order.model.ts
│   │   │   ├── user.model.ts
│   │   │   └── restaurant.model.ts
│   │   ├── utils/             # Helper functions
│   │   │   ├── error.utils.ts
│   │   │   └── validation.utils.ts
│   │   └── index.ts           # Export all functions
│   └── package.json
├── docs/
│   ├── ARCHITECTURE.md               # (Tài liệu này)
│   ├── LAYERED_ARCHITECTURE.md       # Chi tiết implementation
│   ├── EVENTS.md                     # Event-driven architecture
│   ├── ERROR_HANDLING.md             # Error handling patterns
│   ├── DEVELOPMENT_GUIDE.md          # Hướng dẫn phát triển
│   ├── FIRESTORE_SCHEMA.md           # Database schema
│   ├── RULES.md                      # Firestore Security Rules
│   └── ADR/                          # Architecture decisions
│       ├── ADR-001-Why-Firebase-Functions.md
│       ├── ADR-002-Layered-Architecture.md
│       └── ADR-003-No-Logic-In-Triggers.md
└── firebase.json
```

---

## 🎯 Tóm Tắt

### Kiến Trúc Đảm Bảo

✅ **Separation of Concerns** - Mỗi lớp có trách nhiệm riêng  
✅ **Testability** - Dễ viết unit tests  
✅ **Maintainability** - Dễ bảo trì & sửa lỗi  
✅ **Scalability** - Dễ thêm features mới  
✅ **Type Safety** - TypeScript catches errors sớm  
✅ **Reusability** - Services/repos dùng lại nhiều nơi  

### Quy Tắc Cần Nhớ

- **Triggers** chỉ validate input & gọi service
- **Services** chứa toàn bộ business logic
- **Repositories** chỉ thực hiện data operations
- **Models** định nghĩa types & interfaces
- **Utils** cung cấp helper functions

### Chi Tiết Implementation

Để hiểu rõ cách implement từng lớp với code examples, patterns, và best practices, xem:

📖 **[LAYERED_ARCHITECTURE.md](./LAYERED_ARCHITECTURE.md)** - Tài liệu chi tiết về implementation của từng layer

---

## 🏗️ Các Lớp - Tổng Quan Ngắn Gọn

### 1️⃣ TRIGGERS (Entry Points)

**Vị trí:** `src/triggers/`

**Trách nhiệm:** Validate input, extract auth, gọi service

**Loại:** Callable Functions (HTTP), Firestore Triggers, Auth Triggers

### 2️⃣ SERVICES (Business Logic)

**Vị trí:** `src/services/`

**Trách nhiệm:** Business logic, calculations, validations, orchestration

**Ví dụ:** Validate restaurant open, calculate total, apply promotions

### 3️⃣ REPOSITORIES (Data Access)

**Vị trí:** `src/repositories/`

**Trách nhiệm:** CRUD operations, queries, batch operations với Firestore

**Ví dụ:** `create()`, `getById()`, `update()`, `delete()`

### 4️⃣ MODELS (Type Definitions)

**Vị trí:** `src/models/`

**Trách nhiệm:** TypeScript interfaces, enums, Request/Response types

**Ví dụ:** `interface Order`, `type OrderStatus`, `PlaceOrderRequest`

### 5️⃣ UTILS (Helpers)

**Vị trí:** `src/utils/`

**Trách nhiệm:** Error handling, validation, logging, helper functions

**Ví dụ:** `toHttpsError()`, `isValidEmail()`, `logError()`

---

## 📚 Tài Liệu Liên Quan

### 🏗️ Architecture Details
- **[LAYERED_ARCHITECTURE.md](./LAYERED_ARCHITECTURE.md)** - Chi tiết implementation của từng layer với code examples, patterns, và best practices
- **[ADR/](./ADR/)** - Architecture Decision Records
  - [ADR-001: Why Firebase Functions?](./ADR/ADR-001-Why-Firebase-Functions.md)
  - [ADR-002: Layered Architecture](./ADR/ADR-002-Layered-Architecture.md)
  - [ADR-003: No Logic In Triggers](./ADR/ADR-003-No-Logic-In-Triggers.md)

### 📊 Patterns & Practices
- **[EVENTS.md](./EVENTS.md)** - Event-driven architecture, triggers, handlers
- **[ERROR_HANDLING.md](./ERROR_HANDLING.md)** - Error handling patterns, HttpsError mapping

### 🔧 Development
- **[DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)** - Setup, emulator, debugging, CI/CD
- **[FIRESTORE_SCHEMA.md](./FIRESTORE_SCHEMA.md)** - Database schema, indexes, relationships
- **[RULES.md](./RULES.md)** - Firestore Security Rules, role-based access control

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
