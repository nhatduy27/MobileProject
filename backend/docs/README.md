# Tài liệu Backend - Firebase Functions

## 📋 Tổng Quan

Đây là backend của ứng dụng đặt thức ăn trực tuyến (Food Delivery App), được xây dựng bằng **Firebase Cloud Functions** với **TypeScript** và cơ sở dữ liệu **Firestore**.

Backend cung cấp các API thông qua Cloud Functions để xử lý:
- ✅ Xác thực người dùng (Authentication)
- ✅ Quản lý đơn hàng (Orders)
- ✅ Quản lý nhà hàng (Restaurants)
- ✅ Quản lý khuyến mãi (Promotions)
- ✅ Xử lý thông báo (Notifications)

---

## 🛠️ Công Nghệ Sử Dụng

| Công nghệ | Phiên bản | Mục đích |
|-----------|----------|---------|
| **Firebase Cloud Functions** | v7.0.0 | Serverless backend |
| **Firebase Admin SDK** | v13.6.0 | Tương tác với Firebase services |
| **Firestore** | (integrated) | NoSQL Database |
| **Firebase Authentication** | (integrated) | Quản lý user & authentication |
| **Cloud Storage** | (integrated) | Lưu trữ hình ảnh, files |
| **TypeScript** | v5.7.3 | Ngôn ngữ lập trình |
| **Node.js** | 24 | Runtime environment |

---

## 💡 Tại Sao Chúng Ta Dùng Firebase Functions?

Thay vì xây dựng backend truyền thống bằng **NestJS** hoặc **Node.js servers**, chúng tôi chọn **Firebase Cloud Functions** vì những lợi ích sau:

### 🚀 Lợi Ích Chính

| Tiêu Chí | Firebase Functions | NestJS / Node.js Server |
|---------|-------------------|------------------------|
| **Khả năng mở rộng** | Tự động scale (0 → millions) | Phải quản lý infrastructure |
| **Chi phí** | Pay-per-use, rẻ hơn 50% | Chi phí server cố định, đắt |
| **Bảo trì** | Zero server maintenance | Phải quản lý, update, patch |
| **Tích hợp Firebase** | Native integration | Phải config thêm packages |
| **Hỗ trợ Events** | Built-in Firestore/Auth triggers | Phải setup message queues |
| **Deployment** | 1 lệnh, tự động | Phải setup CI/CD, Docker |
| **Monitoring** | Firebase console sẵn có | Phải setup logging, monitoring |

### 📱 Tại Sao Phù Hợp Cho Mobile Apps

1. **Serverless** - Không phải quản lý servers
   - Tự động scale khi có spike traffic
   - Tiết kiệm chi phí khi traffic thấp
   - Zero downtime deployments

2. **Event-Driven** - Phản ứng ngay khi dữ liệu thay đổi
   - User tạo order → Function tự động trigger
   - Firestore update → Function xử lý
   - Auth event → Function tạo user document

3. **Tích Hợp Sâu Firebase**
   - Không cần API layer riêng, mọi dữ liệu đều qua Firebase
   - Authentication tự động (user context)
   - Real-time database (Firestore)
   - Cloud Storage cho files

4. **Bảo Mật**
   - Security Rules trực tiếp trong Firestore
   - Firebase Auth để verify user
   - Zero trust model (verify mọi request)

5. **Phát Triển Nhanh**
   - Ít boilerplate code hơn NestJS
   - TypeScript + Firebase SDK = Productivity tối đa
   - Emulator local cho development

### 📊 So Sánh Chi Phí (Monthly)

**Scenario: 100K monthly active users**

| Chi Phí | Firebase | NestJS Server (AWS) |
|---------|----------|-------------------|
| **Compute** | $50 | $500+ |
| **Database** | $100 | $200 |
| **Storage** | $10 | $50 |
| **Total** | **$160** | **$750+** |
| **Savings** | - | **Firebase 80% cheaper** |

---

## 📡 Danh Sách Cloud Functions (API Overview)

Tất cả các Cloud Functions được cung cấp bởi backend:

### Callable Functions (HTTP - Client gọi trực tiếp)

| Tên Function | Loại | Mô Tả | Người Dùng |
|---|---|---|---|
| **placeOrder** | Callable | Khách hàng đặt hàng mới. Validate items, tính toán tổng tiền, áp dụng khuyến mãi, lưu vào Firestore | BUYER |
| **cancelOrder** | Callable | Hủy đơn hàng (chỉ khi trạng thái = PENDING). Cập nhật status thành CANCELLED | BUYER |
| **applyPromotion** | Callable | Áp dụng mã khuyến mãi cho đơn hàng. Validate mã, kiểm tra điều kiện, tính giảm giá | BUYER |

### Firestore Triggers (Tự động kích hoạt khi document thay đổi)

| Tên Function | Loại | Kích Hoạt | Mô Tả |
|---|---|---|---|
| **onOrderCreated** | Firestore Trigger | Khi order được tạo trong `orders` collection | Gửi notification cho seller, cập nhật restaurant stats, ghi log |
| **onOrderUpdated** | Firestore Trigger | Khi order được cập nhật | Cập nhật statistics, gửi notification cho buyer/shipper |

### Auth Triggers (Tự động kích hoạt khi user account thay đổi)

| Tên Function | Loại | Kích Hoạt | Mô Tả |
|---|---|---|---|
| **onUserCreated** | Auth Trigger | Khi user đăng ký tài khoản mới | Tạo user document trong Firestore, set role mặc định = BUYER |

### Gọi Callable Function Từ App (Ví Dụ)

```typescript
// Flutter/Dart example
import 'package:cloud_functions/cloud_functions.dart';

// Gọi placeOrder function
try {
  final result = await FirebaseFunctions.instance
    .httpsCallable('placeOrder')
    .call({
      'restaurantId': 'rest_001',
      'items': [
        {
          'menuItemId': 'item_1',
          'quantity': 2
        }
      ],
      'deliveryAddress': '123 Lê Lợi, Q.1, TP.HCM',
      'promotionCode': 'WELCOME10'
    });

  final orderId = result.data['orderId'];
  final totalAmount = result.data['totalAmount'];
  print('Order created: $orderId, Total: $totalAmount');
} on FirebaseFunctionsException catch (e) {
  print('Error: ${e.message}');
}
```

---

## 👥 Backend Team Workflow

Quy trình làm việc nhóm để đảm bảo code quality và consistency.

### 🔀 Quy Ước Tên Branch

```
<type>/<feature-name>

Loại (type):
- feature/   → Tính năng mới (feature/payment-integration)
- bugfix/    → Fix lỗi (bugfix/order-status-update)
- hotfix/    → Fix khẩn cấp (hotfix/critical-payment-error)
- refactor/  → Cải thiện code (refactor/service-optimization)
- docs/      → Cập nhật docs (docs/api-documentation)

Ví dụ:
✅ feature/order-cancellation
✅ bugfix/firestore-rules-permission
❌ feature (quá chung chung)
❌ my-feature (không có type prefix)
```

### 💬 Quy Ước Commit Message

**Format:** `<type>(<scope>): <subject>`

```
Loại (type):
- feat     → Tính năng mới
- fix      → Fix lỗi
- docs     → Thay đổi docs
- refactor → Cải thiện code (không thay đổi functionality)
- test     → Thêm/cập nhật tests
- chore    → Update dependencies, config

Scope:
- order        → Liên quan Order
- restaurant   → Liên quan Restaurant
- promotion    → Liên quan Promotion
- user         → Liên quan User
- rules        → Firestore Security Rules
- (optional)   → Có thể bỏ qua nếu ảnh hưởng nhiều module

Subject:
- Mô tả ngắn gọn, imperative mood
- Không dùng past tense
- Bắt đầu bằng chữ thường
- Không kết thúc bằng dấu chấm

Ví dụ commit messages:
✅ feat(order): add order cancellation endpoint
✅ fix(rules): fix permission denied for seller orders
✅ docs(readme): update installation guide
✅ refactor(services): simplify validation logic
✅ test(order-service): add unit tests for placeOrder

❌ feat: fixed order stuff
❌ FEAT(ORDER): Add Order Cancellation
❌ Updated the order service
```

### 📋 Pull Request (Quy Tắc)

**Khi tạo PR, phải:**

1. ✅ **Title rõ ràng** theo convention trên
   ```
   feat(order): implement order cancellation
   ```

2. ✅ **Description chi tiết:**
   ```
   ## Description
   Allows buyers to cancel orders that are still in PENDING status.

   ## Changes
   - Add cancelOrder callable function
   - Update order status validation
   - Send notification to seller when order cancelled

   ## Testing
   - Tested locally with emulator
   - Validated Firestore rules
   - Checked error handling
   ```

3. ✅ **Linked issue** (nếu có)
   ```
   Fixes #123
   Related to #456
   ```

4. ✅ **Self-review** trước submit
   - [ ] Code follows conventions
   - [ ] No console.log() left
   - [ ] Error handling complete
   - [ ] Lint passes: `npm run lint`
   - [ ] Build passes: `npm run build`
   - [ ] Tested in emulator

### 👀 Code Review Guidelines

**Khi review PR, kiểm tra:**

| Tiêu Chí | Câu Hỏi |
|---------|---------|
| **Functionality** | Đây có implement đúng requirement không? |
| **Architecture** | Có follow layered architecture (Triggers → Services → Repos)? |
| **Code Quality** | Code có clear, readable, maintainable không? |
| **Error Handling** | Có xử lý tất cả edge cases? Errors có descriptive? |
| **Types** | TypeScript types có correct? Không có `any`? |
| **Security** | Có check authentication? Authorization? Validate input? |
| **Tests** | Có test trong emulator? |
| **Documentation** | Có comment/docs cho logic phức tạp? |
| **Performance** | Có N+1 queries? Có batch operations nếu cần? |

**Approval criteria:** Ít nhất 1 approval từ senior dev trước merge.

### 🛠️ Cách Làm Việc Theo Module

#### **Services** (`src/services/`)
- **Trách nhiệm:** Business logic
- **Quy tắc:**
  - ✅ Implement use cases (placeOrder, cancelOrder, etc.)
  - ✅ Gọi repositories để access dữ liệu
  - ✅ Validate business rules
  - ❌ Không trực tiếp tương tác Firestore
  - ❌ Không HTTP logic

**Ví dụ:**
```typescript
// ✅ GOOD - Service logic
async placeOrder(data, userId) {
  // 1. Validate
  if (!userId) throw error("Unauthenticated");
  
  // 2. Fetch related data via repos
  const restaurant = await restaurantRepo.getById(data.restaurantId);
  const menuItems = await restaurantRepo.getMenuItems(...);
  
  // 3. Business logic
  const totalAmount = this.calculateTotal(menuItems);
  
  // 4. Save via repo
  const orderId = await orderRepo.create({...});
  
  // 5. Side effects
  await notificationService.sendToSeller(...);
  
  return { orderId };
}
```

#### **Repositories** (`src/repositories/`)
- **Trách nhiệm:** Data access (CRUD operations)
- **Quy tắc:**
  - ✅ Tất cả Firestore operations
  - ✅ Queries, filtering, ordering
  - ✅ Batch operations
  - ✅ Timestamp handling
  - ❌ Không business logic
  - ❌ Không validation

**Ví dụ:**
```typescript
// ✅ GOOD - Repository (chỉ data operations)
async getByRestaurantAndStatus(restaurantId, status) {
  return this.db
    .collection('orders')
    .where('restaurantId', '==', restaurantId)
    .where('status', '==', status)
    .orderBy('createdAt', 'desc')
    .get();
}
```

#### **Models** (`src/models/`)
- **Trách nhiệm:** Type definitions
- **Quy tắc:**
  - ✅ TypeScript interfaces
  - ✅ Enums/union types
  - ✅ Request/Response types
  - ❌ Không logic
  - ❌ Không async methods

**Ví dụ:**
```typescript
// ✅ GOOD - Model (chỉ types)
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface Order {
  id: string;
  userId: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
}

export interface PlaceOrderRequest {
  restaurantId: string;
  items: Array<{menuItemId: string; quantity: number}>;
}
```

#### **Triggers** (`src/triggers/`)
- **Trách nhiệm:** Entry points, input validation
- **Quy tắc:**
  - ✅ Validate input
  - ✅ Extract auth context
  - ✅ Gọi service layer
  - ✅ Error handling & conversion
  - ❌ Không business logic trực tiếp
  - ❌ Không Firestore access trực tiếp

**Ví dụ:**
```typescript
// ✅ GOOD - Trigger (chỉ validation + gọi service)
export const placeOrder = onCall(async (request) => {
  const { data, auth } = request;
  
  // 1. Validate
  if (!isNotEmpty(data.restaurantId)) throw error("restaurantId required");
  if (!isNonEmptyArray(data.items)) throw error("items empty");
  
  // 2. Call service
  const result = await orderService.placeOrder(data, auth);
  
  // 3. Return
  return result;
});
```

---

## 📂 Cấu Trúc Thư Mục
```
backend/
├── functions/
│   ├── src/                    # Mã nguồn TypeScript
│   │   ├── index.ts           # Entry point chính
│   │   ├── params.ts          # Cấu hình parameters
│   │   ├── models/            # Data models & types
│   │   │   ├── user.model.ts
│   │   │   ├── order.model.ts
│   │   │   ├── restaurant.model.ts
│   │   │   └── promotion.model.ts
│   │   ├── repositories/      # Data access layer
│   │   │   ├── user.repository.ts
│   │   │   ├── order.repository.ts
│   │   │   ├── restaurant.repository.ts
│   │   │   └── promotion.repository.ts
│   │   ├── services/          # Business logic layer
│   │   │   ├── order.service.ts
│   │   │   ├── promotion.service.ts
│   │   │   └── notification.service.ts
│   │   ├── triggers/          # Cloud Functions entry points
│   │   │   ├── api.order.ts           # Callable functions cho Orders
│   │   │   ├── api.promotion.ts       # Callable functions cho Promotions
│   │   │   ├── auth.trigger.ts        # Firebase Auth triggers
│   │   │   └── order.trigger.ts       # Firestore triggers
│   │   └── utils/             # Utility functions
│   │       ├── error.utils.ts
│   │       └── validation.utils.ts
│   ├── lib/                   # Mã JavaScript sau compile
│   ├── package.json           # Dependencies
│   ├── tsconfig.json          # TypeScript config
│   └── .eslintrc.js          # Linting rules
├── docs/                       # Tài liệu
│   ├── README.md              # File này
│   ├── ARCHITECTURE.md        # Kiến trúc layered
│   ├── FIRESTORE_SCHEMA.md    # Thiết kế Firestore
│   ├── RULES.md               # Security rules
│   └── DEVELOPMENT_GUIDE.md   # Hướng dẫn phát triển
├── firebase.json              # Firebase configuration
└── .firebaserc                # Firebase project config
```

### Giải Thích Các Thư Mục:

- **`models/`**: Định nghĩa TypeScript interfaces và types cho mỗi entity (User, Order, etc.)
- **`repositories/`**: Tầng truy cập dữ liệu - tất cả thao tác với Firestore đều qua đây
- **`services/`**: Tầng logic kinh doanh - xử lý business rules và workflows
- **`triggers/`**: Cloud Functions entry points - người dùng gọi hàm này từ client
- **`utils/`**: Các hàm tiện ích chung (validation, error handling, logging)
- **`lib/`**: Thư mục output khi compile TypeScript → JavaScript

---

## 🚀 Cài Đặt & Khởi Tạo

### 1. Điều Kiện Tiên Quyết

Cài đặt các công cụ sau trước tiên:

```bash
# Node.js (v24 hoặc cao hơn)
node --version          # v24.x.x

# npm (thường kèm theo Node.js)
npm --version           # v10.x.x hoặc cao hơn

# Firebase CLI (global)
npm install -g firebase-tools
firebase --version
```

### 2. Cài Đặt Dependencies

```bash
# Vào thư mục functions
cd backend/functions

# Cài các package từ package.json
npm install
```

Lệnh này sẽ cài đặt:
- `firebase-admin`: Admin SDK để tương tác Firebase
- `firebase-functions`: Framework cho Cloud Functions
- `typescript`: Compiler TypeScript
- `eslint`: Linter để kiểm tra code quality
- `concurrently`: Chạy nhiều lệnh song song

### 3. Kích Hoạt Firebase

```bash
# Login vào Firebase account
firebase login

# List các project đã liên kết
firebase projects:list

# Chọn project (nếu có nhiều)
firebase use <project-id>
```

Kiểm tra file `.firebaserc` để xem project nào được chọn:

```json
{
  "projects": {
    "default": "your-project-id"
  }
}
```

---

## 🔧 Chạy Backend trong Chế Độ Development

### Development Mode với Hot Reload

Chế độ này cho phép code tự động compile và reload khi có thay đổi:

```bash
cd backend/functions

# Chạy cả TypeScript compiler (watch mode) và emulator
npm run dev
```

Điều này sẽ:
1. ✅ Khởi động TypeScript compiler trong watch mode
2. ✅ Khởi động Firebase Emulator Suite (chỉ Functions)
3. ✅ Tự động compile code khi có file .ts thay đổi
4. ✅ Tự động reload functions trong emulator

**Output mong đợi:**

```
firebase notice functions: If you are not already running a local emulator suite, start one by running firebase emulators:start

typescript notice cts: Watching for file changes...
⚠️  emulator notice functions: The following emulators are not running: auth, firestore, storage. Only Cloud Functions emulator will run.
ℹ️  functions: Listening on 5001
```

### Giải Thích Các Lệnh npm

```bash
npm run build              # Compile TypeScript → JavaScript
npm run build:watch        # Compile & watch cho changes
npm run serve              # Build + start emulator
npm run dev                # Build:watch + emulator (HOT RELOAD)
npm run shell              # Chạy functions shell (interactive)
npm run lint               # Kiểm tra code quality
npm run deploy             # Deploy lên Firebase
npm run logs               # Xem logs từ Firebase
```

### Chạy Emulator Đầy Đủ (Tùy Chọn)

Nếu muốn test với Firestore, Auth, Storage emulator cùng lúc:

```bash
# Khởi động full emulator suite
firebase emulators:start

# Hoặc chỉ Functions
firebase emulators:start --only functions
```

---

## 📤 Deploy Lên Firebase

### 1. Build Trước Khi Deploy

```bash
cd backend/functions
npm run lint    # Check code quality
npm run build   # Compile TypeScript
```

### 2. Deploy Functions

```bash
npm run deploy
```

Hoặc deploy toàn bộ project:

```bash
firebase deploy
```

**Firebase sẽ tự động:**
- ✅ Chạy linting
- ✅ Compile TypeScript
- ✅ Upload code lên Firebase
- ✅ Deploy functions

### 3. Kiểm Tra Logs Sau Deploy

```bash
npm run logs

# Hoặc xem real-time trên console
# https://console.firebase.google.com/project/<project-id>/functions
```

---

## 📚 Tài Liệu Liên Quan

Để hiểu rõ hơn về dự án, tham khảo:

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Kiến trúc layered, quy ước, flows
- **[FIRESTORE_SCHEMA.md](./FIRESTORE_SCHEMA.md)** - Thiết kế cấu trúc Firestore
- **[RULES.md](./RULES.md)** - Security rules cho Firestore
- **[DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)** - Hướng dẫn phát triển chi tiết

---

## ⚠️ Các Vấn Đề Thường Gặp

### 1. "Cannot find module" error

```bash
# Xóa node_modules và cài lại
rm -rf node_modules
npm install

# Hoặc rebuild
npm run build
```

### 2. Emulator không khởi động

```bash
# Kiểm tra Java đã cài đúng không
java -version

# Kiểm tra port 5001 không bị chiếm
lsof -i :5001

# Khởi động lại emulator
firebase emulators:start --clear-on-exit
```

### 3. Linting errors

```bash
# Xem lỗi chi tiết
npm run lint

# Fix automatically nếu có thể
npm run lint -- --fix
```

### 4. TypeScript errors

```bash
# Rebuild
npm run build

# Hoặc check type errors
npx tsc --noEmit
```

---

## 🆘 Hỗ Trợ & Liên Hệ

Nếu gặp vấn đề:

1. Kiểm tra logs: `npm run logs`
2. Xem Firebase console: https://console.firebase.google.com
3. Tham khảo [Firebase Functions documentation](https://firebase.google.com/docs/functions)
4. Liên hệ team lead

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
