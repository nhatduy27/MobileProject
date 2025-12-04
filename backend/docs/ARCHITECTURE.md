# FoodApp Backend

Backend NestJS cho ứng dụng di động FoodApp, được xây dựng với TypeScript theo các nguyên tắc **Kiến trúc Nguyên khối + Phân tầng (Monolithic + Layered Architecture)** kết hợp với **Kiến trúc Sạch (Clean Architecture)** theo mô hình **Cổng & Bộ chuyển đổi (Ports & Adapters)**.

## 🏗️ Tổng quan Kiến trúc

Backend này tuân theo **Kiến trúc Phân tầng Nguyên khối (Monolithic Layered Architecture)** với **Đảo ngược Phụ thuộc (Dependency Inversion)** thông qua mô hình **Cổng & Bộ chuyển đổi (Ports & Adapters pattern)**:

### Các Tầng Kiến trúc

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│              (Controllers - HTTP/REST API)                  │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                        │
│                  (Services - Business Logic)                │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│         (Entities, Repository Interfaces/Ports)             │
├─────────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                       │
│      (Repository Implementations/Adapters, Firebase)        │
└─────────────────────────────────────────────────────────────┘

         ┌──────────────────────────────────────┐
         │       Shared Services (Global)       │
         │  Cache, Notifications, Event Bus     │
         │     (Ports & Adapters Pattern)       │
         └──────────────────────────────────────┘
```

### Cấu trúc Thư mục

```
backend/
├── src/
│   ├── main.ts                   # Application entry point
│   ├── app.module.ts             # Root module
│   │
│   ├── config/                   # Configuration
│   │   ├── environment.config.ts # Environment variables
│   │   └── firebase.config.ts    # Firebase initialization (stub)
│   │
│   ├── common/                   # Shared utilities
│   │   ├── decorators/           # Custom decorators (@CurrentUser, @Roles)
│   │   ├── filters/              # Exception filters
│   │   ├── guards/               # Auth guards
│   │   └── interceptors/         # Logging interceptors
│   │
│   ├── shared/                   # Technical services (Ports & Adapters)
│   │   ├── cache/
│   │   │   ├── cache.port.ts               # Abstract cache interface
│   │   │   └── in-memory-cache.adapter.ts  # In-memory implementation
│   │   ├── notifications/
│   │   │   ├── notification.port.ts         # Abstract notification interface
│   │   │   └── fcm-notification.adapter.ts  # Firebase FCM stub
│   │   ├── events/
│   │   │   ├── event-bus.port.ts           # Abstract event bus interface
│   │   │   └── in-memory-event-bus.adapter.ts
│   │   └── shared.module.ts      # Shared services module
│   │
│   └── modules/                  # Feature modules
│       ├── auth/                 # Authentication module
│       │   ├── domain/
│       │   │   ├── auth-user.entity.ts    # User entity
│       │   │   └── auth.repository.ts     # Repository interface (Port)
│       │   ├── infra/
│       │   │   └── firebase-auth.repository.ts  # Repository implementation (Adapter)
│       │   ├── dto/
│       │   │   └── auth.dto.ts
│       │   ├── auth.controller.ts
│       │   ├── auth.service.ts
│       │   └── auth.module.ts
│       │
│       └── orders/               # Orders module
│           ├── domain/
│           │   ├── order.entity.ts         # Order entity
│           │   └── order.repository.ts     # Repository interface (Port)
│           ├── infra/
│           │   └── firebase-order.repository.ts  # Repository implementation (Adapter)
│           ├── dto/
│           │   └── order.dto.ts
│           ├── orders.controller.ts
│           ├── orders.service.ts
│           └── orders.module.ts
│
├── test/                         # E2E tests
├── package.json
├── tsconfig.json
└── README.md
```

## 🎯 Các Nguyên tắc Thiết kế Chính

### 1. **Đảo ngược Phụ thuộc (Dependency Inversion) - Mô hình Cổng & Bộ chuyển đổi (Ports & Adapters)**
- **Cổng (Ports)** (abstract classes) định nghĩa các hợp đồng giao tiếp
- **Bộ chuyển đổi (Adapters)** (concrete classes) triển khai các hợp đồng đó
- Logic nghiệp vụ phụ thuộc vào các abstraction, không phụ thuộc vào implementation cụ thể
- Dễ dàng thay đổi implementation (ví dụ: in-memory cache → Redis)

### 2. **Kiến trúc Phân tầng (Layered Architecture)**
- **Controllers**: Chỉ xử lý HTTP requests/responses
- **Services**: Chứa logic nghiệp vụ
- **Domain**: Định nghĩa entities và repository interfaces
- **Infrastructure**: Triển khai repository adapters (Firebase, v.v.)

### 3. **Lợi ích của Kiến trúc Sạch (Clean Architecture)**
- ✅ Khả năng kiểm thử (Testability): Dễ dàng mock dependencies
- ✅ Khả năng bảo trì (Maintainability): Tách biệt rõ ràng các mối quan tâm
- ✅ Tính linh hoạt (Flexibility): Thay đổi implementation mà không ảnh hưởng logic nghiệp vụ
- ✅ Khả năng mở rộng (Scalability): Thêm tính năng mới bằng cách mở rộng modules

## 📦 Công nghệ Sử dụng

- **Framework**: NestJS
- **Language**: TypeScript
- **Database**: Firebase Firestore (triển khai tạm - stub implementation)
- **Authentication**: Firebase Auth (triển khai tạm - stub implementation)
- **Validation**: class-validator, class-transformer
- **Cache**: In-memory (triển khai tạm cho Redis)
- **Notifications**: Firebase Cloud Messaging (triển khai tạm)
- **Events**: In-memory event bus (triển khai tạm cho RabbitMQ/Kafka)

## 🚀 Bắt đầu

### Yêu cầu
- Node.js >= 18.x
- npm hoặc yarn

### Cài đặt

```bash
# Install dependencies
npm install
```

### Biến Môi trường

Tạo file `.env` trong thư mục backend (tùy chọn hiện tại):

```env
PORT=3000
NODE_ENV=development

# Firebase Configuration (TODO: Add real credentials)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=your-client-email
FIREBASE_PRIVATE_KEY=your-private-key

# JWT Configuration (TODO)
JWT_SECRET=your-secret-key
JWT_EXPIRES_IN=1d
```

### Chạy Ứng dụng

```bash
# Development mode with hot-reload
npm run start:dev

# Production mode
npm run start:prod

# Debug mode
npm run start:debug
```

API sẽ chạy tại: `http://localhost:3000/api`

### Kiểm thử

```bash
# Unit tests
npm test

# E2E tests
npm run test:e2e

# Test coverage
npm run test:cov
```

## 📚 Các API Endpoint

### Xác thực (Authentication)

```
POST   /api/auth/register        # Register new user
POST   /api/auth/login           # User login
GET    /api/auth/users/:id       # Get user by ID
```

### Đơn hàng (Orders)

```
POST   /api/orders                      # Create new order
GET    /api/orders/:id                  # Get order by ID
GET    /api/orders/customer/:customerId # Get customer's orders
GET    /api/orders/seller/:sellerId     # Get seller's orders
PATCH  /api/orders/:id/status           # Update order status
DELETE /api/orders/:id                  # Cancel order
```

## 🔧 Các Triển khai Tạm (Stub Implementations)

Các tính năng sau hiện đang được **triển khai tạm (stubbed)** và cần tích hợp Firebase:

### 1. **Xác thực (Authentication)** (`FirebaseAuthRepository`)
- Hiện tại sử dụng bộ nhớ in-memory
- TODO: Tích hợp Firebase Admin SDK để quản lý người dùng

### 2. **Đơn hàng (Orders)** (`FirebaseOrderRepository`)
- Hiện tại sử dụng bộ nhớ in-memory
- TODO: Tích hợp Firebase Firestore để lưu trữ dữ liệu

### 3. **Bộ nhớ đệm (Cache)** (`InMemoryCacheAdapter`)
- Hiện tại sử dụng JavaScript Map
- TODO: Tích hợp Redis cho distributed caching

### 4. **Thông báo (Notifications)** (`FcmNotificationAdapter`)
- Hiện tại chỉ log ra console
- TODO: Tích hợp Firebase Cloud Messaging cho push notifications

### 5. **Sự kiện (Events)** (`InMemoryEventBusAdapter`)
- Hiện tại chỉ log ra console
- TODO: Tích hợp RabbitMQ, Kafka, hoặc AWS SQS cho kiến trúc hướng sự kiện

## 🛠️ Các Bước Tiếp theo

1. **Tích hợp Firebase Admin SDK**
   - Thêm thông tin xác thực Firebase
   - Thay thế các stub repository bằng các thao tác Firestore thực
   - Triển khai Firebase Auth

2. **Thêm Xác thực JWT**
   - Cài đặt `@nestjs/jwt` và `@nestjs/passport`
   - Triển khai tạo và xác thực JWT token
   - Bảo vệ các route với AuthGuard

3. **Thêm Các Module Tính năng Khác**
   - Module sản phẩm (Products)
   - Module người dùng/người bán (Users/Sellers)
   - Module đánh giá (Reviews)
   - Module thanh toán (Payments)

4. **Thêm Redis Cache**
   - Cài đặt `@nestjs/cache-manager` và `cache-manager-redis-store`
   - Thay thế `InMemoryCacheAdapter` bằng `RedisCacheAdapter`

5. **Thêm Message Queue**
   - Cài đặt `@nestjs/microservices` với RabbitMQ hoặc Kafka
   - Thay thế `InMemoryEventBusAdapter` bằng event bus thực

6. **Thêm Tài liệu API**
   - Cài đặt `@nestjs/swagger`
   - Thêm Swagger decorators vào controllers

## 📖 Tài liệu Tham khảo

- [Tài liệu NestJS](https://docs.nestjs.com)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture (Ports & Adapters)](https://alistair.cockburn.us/hexagonal-architecture/)

## 📝 Giấy phép

Dự án này là một phần của ứng dụng di động FoodApp.
