## Khởi tạo Backend - Hướng dẫn Nhanh

### ✅ Đã Tạo Thành công

Backend NestJS của bạn đã được khởi tạo hoàn chỉnh với cấu trúc sau:

#### 📁 Cấu trúc Dự án
```
backend/
├── src/
│   ├── main.ts                    ✅ Enhanced with validation, CORS, global filters
│   ├── app.module.ts              ✅ Imports all feature modules
│   │
│   ├── config/                    ✅ Environment & Firebase config (stubbed)
│   │   ├── environment.config.ts
│   │   └── firebase.config.ts
│   │
│   ├── common/                    ✅ Shared utilities
│   │   ├── decorators/           
│   │   │   ├── roles.decorator.ts
│   │   │   └── current-user.decorator.ts
│   │   ├── filters/
│   │   │   └── http-exception.filter.ts
│   │   ├── guards/
│   │   │   └── auth.guard.ts
│   │   └── interceptors/
│   │       └── logging.interceptor.ts
│   │
│   ├── shared/                    ✅ Technical services (Ports & Adapters)
│   │   ├── cache/
│   │   │   ├── cache.port.ts
│   │   │   └── in-memory-cache.adapter.ts
│   │   ├── notifications/
│   │   │   ├── notification.port.ts
│   │   │   └── fcm-notification.adapter.ts
│   │   ├── events/
│   │   │   ├── event-bus.port.ts
│   │   │   └── in-memory-event-bus.adapter.ts
│   │   └── shared.module.ts
│   │
│   └── modules/                   ✅ Feature modules
│       ├── auth/                 ✅ Authentication module
│       │   ├── domain/
│       │   │   ├── auth-user.entity.ts
│       │   │   └── auth.repository.ts
│       │   ├── infra/
│       │   │   └── firebase-auth.repository.ts
│       │   ├── dto/
│       │   │   └── auth.dto.ts
│       │   ├── auth.controller.ts
│       │   ├── auth.service.ts
│       │   └── auth.module.ts
│       │
│       └── orders/               ✅ Orders module with full integration
│           ├── domain/
│           │   ├── order.entity.ts
│           │   └── order.repository.ts
│           ├── infra/
│           │   └── firebase-order.repository.ts
│           ├── dto/
│           │   └── order.dto.ts
│           ├── orders.controller.ts
│           ├── orders.service.ts
│           └── orders.module.ts
```

#### 🎯 Điểm nổi bật về Kiến trúc

**1. Kiến trúc Nguyên khối + Phân tầng (Monolithic + Layered Architecture)**
- ✅ Tầng Trình bày (Presentation Layer) - Controllers
- ✅ Tầng Ứng dụng (Application Layer) - Services
- ✅ Tầng Miền (Domain Layer) - Entities, Repository Interfaces
- ✅ Tầng Hạ tầng (Infrastructure Layer) - Repository Implementations

**2. Đảo ngược Phụ thuộc (Dependency Inversion) - Mô hình Cổng & Bộ chuyển đổi (Ports & Adapters)**
- ✅ Các Cổng trừu tượng (Abstract Ports): `CachePort`, `NotificationPort`, `EventBusPort`, `AuthRepository`, `OrderRepository`
- ✅ Các Bộ chuyển đổi cụ thể (Concrete Adapters): Triển khai in-memory (sẵn sàng để thay thế)

**3. Lợi ích của Kiến trúc Sạch (Clean Architecture)**
- ✅ Logic nghiệp vụ độc lập với framework
- ✅ Dễ dàng kiểm thử với mock
- ✅ Dễ dàng thay đổi implementation
- ✅ Tách biệt rõ ràng các mối quan tâm

### 🚀 Server đang Chạy

```
http://localhost:3000/api
```

#### Các Endpoint có sẵn:

**Xác thực (Authentication)**
- `POST /api/auth/register` - Đăng ký người dùng mới
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/users/:id` - Lấy thông tin người dùng theo ID

**Đơn hàng (Orders)**
- `POST /api/orders` - Tạo đơn hàng mới
- `GET /api/orders/:id` - Lấy đơn hàng theo ID
- `GET /api/orders/customer/:customerId` - Lấy đơn hàng của khách hàng
- `GET /api/orders/seller/:sellerId` - Lấy đơn hàng của người bán
- `PATCH /api/orders/:id/status` - Cập nhật trạng thái đơn hàng
- `DELETE /api/orders/:id` - Hủy đơn hàng

### 🧪 Kiểm thử API

#### Đăng ký Người dùng
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "displayName": "Test User"
  }'
```

#### Tạo Đơn hàng
```bash
curl -X POST http://localhost:3000/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "user_1",
    "sellerId": "seller_1",
    "items": [
      {
        "productId": "prod_1",
        "productName": "Pizza Margherita",
        "quantity": 2,
        "unitPrice": 12.99
      }
    ],
    "deliveryAddress": "123 Main St",
    "notes": "Extra cheese please"
  }'
```

### 📝 Các Phần đang Triển khai Tạm (TODO)

Tất cả các triển khai tạm đều được đánh dấu rõ ràng bằng comment `TODO` và log messages:

1. **Firebase Auth** - Thay thế `FirebaseAuthRepository` bằng Firebase Admin SDK
2. **Firebase Firestore** - Thay thế `FirebaseOrderRepository` bằng các thao tác Firestore
3. **Cache** - Thay thế `InMemoryCacheAdapter` bằng Redis
4. **Notifications** - Thay thế `FcmNotificationAdapter` bằng Firebase Cloud Messaging
5. **Events** - Thay thế `InMemoryEventBusAdapter` bằng RabbitMQ/Kafka
6. **JWT** - Thêm `@nestjs/jwt` và triển khai tạo/xác thực token

### 📚 Các Bước Tiếp theo

1. **Thêm Tích hợp Firebase**
   ```bash
   npm install firebase-admin
   ```
   - Cập nhật `firebase.config.ts` với thông tin xác thực của bạn
   - Thay thế các stub repository bằng triển khai thực

2. **Thêm Xác thực JWT**
   ```bash
   npm install @nestjs/jwt @nestjs/passport passport passport-jwt
   npm install -D @types/passport-jwt
   ```
   - Triển khai JWT strategy
   - Cập nhật AuthGuard với xác thực thực tế

3. **Thêm Redis Cache**
   ```bash
   npm install @nestjs/cache-manager cache-manager-redis-store redis
   ```
   - Tạo `RedisCacheAdapter`
   - Cập nhật SharedModule để sử dụng Redis

4. **Thêm Tài liệu Swagger**
   ```bash
   npm install @nestjs/swagger
   ```
   - Thêm Swagger decorators vào DTOs và controllers

5. **Thêm Các Module Tính năng Khác**
   - Module sản phẩm (Products)
   - Module người dùng/người bán (Users/Sellers)
   - Module đánh giá (Reviews)
   - Module thanh toán (Payments)

### 🎉 Tổng kết

Bây giờ bạn đã có một backend NestJS hoàn chỉnh với:
- ✅ Kiến trúc sạch, phân tầng rõ ràng
- ✅ Đảo ngược Phụ thuộc (Dependency Inversion) theo mô hình Cổng & Bộ chuyển đổi (Ports & Adapters)
- ✅ Hai module tính năng (Auth & Orders)
- ✅ Validation toàn cục, xử lý lỗi, logging
- ✅ Các triển khai tạm sẵn sàng để thay thế
- ✅ Biên dịch và chạy thành công
- ✅ Sẵn sàng cho phát triển nhóm

Xem `ARCHITECTURE.md` để biết tài liệu chi tiết!
