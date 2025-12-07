# ADR-002: Tại Sao Chúng Ta Sử Dụng Layered Architecture?

**Trạng thái:** Chấp nhận  
**Ngày quyết định:** Tháng 11, 2025  
**Người quyết định:** Backend Team  

---

## Bối Cảnh

Backend phát triển nhanh với nhiều tính năng:
- Đặt hàng (orders)
- Quản lý nhà hàng (restaurants)
- Khuyến mãi (promotions)
- Thông báo (notifications)
- Xác thực (authentication)

Team có nhiều developers sẽ contribute code. Cần một kiến trúc:
- Dễ hiểu cho người mới
- Dễ test (unit tests, integration tests)
- Dễ maintain khi có bugs
- Dễ scale khi thêm features

## Quyết Định

**Chúng tôi tổ chức code theo Layered Architecture với 5 lớp:**

```
TRIGGERS (Entry Points)
    ↓
SERVICES (Business Logic)
    ↓
REPOSITORIES (Data Access)
    ↓
MODELS (Type Definitions)
    ↓
UTILS (Helpers)
```

## Lợi Ích

### 1. **Separation of Concerns**

Mỗi lớp có trách nhiệm rõ ràng:

```typescript
// ❌ BAD - Tất cả logic ở 1 nơi
export const placeOrder = onCall(async (request) => {
  // Validate input
  if (!request.data.restaurantId) throw new Error("Missing restaurantId");
  
  // Get restaurant
  const restaurant = await db.collection("restaurants").doc(request.data.restaurantId).get();
  
  // Validate restaurant open
  if (!restaurant.data()?.isOpen) throw new Error("Restaurant closed");
  
  // Calculate total
  let total = 0;
  for (const item of request.data.items) {
    const menuItem = await db.collection("restaurants")
      .doc(request.data.restaurantId)
      .collection("menuItems")
      .doc(item.menuItemId)
      .get();
    total += menuItem.data().price * item.quantity;
  }
  
  // Apply promotion
  const promo = await db.collection("promotions")
    .where("code", "==", request.data.promotionCode)
    .get();
  if (!promo.empty) {
    total *= (1 - promo.docs[0].data().discountPercent / 100);
  }
  
  // Save order
  const order = await db.collection("orders").add({
    userId: request.auth.uid,
    restaurantId: request.data.restaurantId,
    items: request.data.items,
    totalAmount: total,
    status: "PENDING"
  });
  
  // Send notification
  await sendNotification(restaurant.data().sellerId, `New order: ${order.id}`);
  
  return { orderId: order.id, totalAmount: total };
});
// ❌ Quá phức tạp: validation + logic + database + notification tất cả ở 1 nơi
// ❌ Khó test: phải mock Firestore, auth, notification
// ❌ Khó maintain: bug ở đâu? validate? logic? database?
// ❌ Khó reuse: copy-paste code nếu cần logic này ở chỗ khác
```

```typescript
// ✅ GOOD - Layered Architecture
// TRIGGER - Chỉ validate input & gọi service
export const placeOrder = onCall<PlaceOrderRequest>(async (request) => {
  const { data, auth } = request;
  
  // Validate input format
  if (!data.restaurantId) throw new Error("restaurantId required");
  if (!data.items?.length) throw new Error("items empty");
  
  // Call service
  return await orderService.placeOrder(data, auth);
});

// SERVICE - Business logic
export class OrderService {
  async placeOrder(data: PlaceOrderRequest, auth: CallableRequestContext) {
    // Validate business rules
    const restaurant = await restaurantRepository.getById(data.restaurantId);
    if (!restaurant?.isOpen) throw new Error("Restaurant closed");
    
    // Calculate total
    const totalAmount = await this.calculateTotal(data.restaurantId, data.items);
    
    // Apply promotion
    const finalAmount = await promotionService.applyPromotion(totalAmount, data.promotionCode);
    
    // Save order
    const orderId = await orderRepository.create({
      userId: auth.uid,
      restaurantId: data.restaurantId,
      items: data.items,
      totalAmount: finalAmount,
      status: "PENDING"
    });
    
    // Send notification
    await notificationService.sendToSeller(restaurant.sellerId, orderId);
    
    return { orderId, totalAmount: finalAmount };
  }
  
  private async calculateTotal(restaurantId: string, items: OrderItem[]) {
    // Calculation logic
  }
}

// REPOSITORY - Data access only
export class OrderRepository {
  async create(data: Order): Promise<string> {
    const docRef = await this.db.collection("orders").add(data);
    return docRef.id;
  }
  
  async getById(orderId: string): Promise<Order | null> {
    // Firestore read
  }
}

// ✅ Rõ ràng: mỗi lớp có trách nhiệm riêng
// ✅ Dễ test: mock từng lớp riêng biệt
// ✅ Dễ maintain: bug ở đâu? check từng lớp
// ✅ Dễ reuse: services/repos dùng lại nhiều nơi
```

### 2. **Testability**

Dễ viết unit tests cho từng lớp:

```typescript
// Test Service (mock repository)
describe('OrderService.placeOrder', () => {
  it('should throw error if restaurant is closed', async () => {
    // Mock repository
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue({
      id: 'rest_1',
      isOpen: false  // ✅ Mock data
    });
    
    // Test service
    await expect(
      orderService.placeOrder(mockData, mockAuth)
    ).rejects.toThrow('Restaurant closed');
  });
  
  it('should calculate total correctly', async () => {
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue(mockRestaurant);
    jest.spyOn(orderRepository, 'create').mockResolvedValue('order_123');
    
    const result = await orderService.placeOrder(mockData, mockAuth);
    
    expect(result.totalAmount).toBe(150000);  // ✅ Test logic
  });
});

// Test Repository (mock Firestore)
describe('OrderRepository.create', () => {
  it('should save order to Firestore', async () => {
    const mockAdd = jest.fn().mockResolvedValue({ id: 'order_123' });
    jest.spyOn(db.collection('orders'), 'add', 'get').mockReturnValue(mockAdd);
    
    const orderId = await orderRepository.create(mockOrderData);
    
    expect(orderId).toBe('order_123');
    expect(mockAdd).toHaveBeenCalledWith(mockOrderData);
  });
});
```

### 3. **Maintainability**

Khi có bug, dễ xác định vấn đề ở lớp nào:

```
Bug Report: "Promotion không được apply đúng"

Kiểm tra từng lớp:
1. TRIGGER - Input có đúng không? ✅ OK
2. SERVICE - Logic apply promotion sai? ❌ BUG HERE
   → Sửa OrderService.applyPromotion()
3. REPOSITORY - Database lưu sai? ✅ OK

✅ Fix 1 nơi, tất cả triggers dùng service này đều được fix
```

### 4. **Reusability**

Services và repositories có thể dùng lại nhiều nơi:

```typescript
// OrderService được dùng bởi nhiều triggers
export const placeOrder = onCall(async (request) => {
  return await orderService.placeOrder(request.data, request.auth);
});

export const placeOrderFromSchedule = onSchedule('every day 10:00', async () => {
  // Reuse service
  await orderService.placeOrder(scheduledOrderData, systemAuth);
});

export const placeOrderFromWebhook = onRequest(async (req, res) => {
  // Reuse service
  const result = await orderService.placeOrder(req.body, webhookAuth);
  res.json(result);
});

// ✅ Logic chỉ viết 1 lần, dùng nhiều nơi
```

### 5. **Scalability**

Dễ thêm features mới:

```typescript
// Thêm feature mới: Đặt hàng định kỳ
// 1. Thêm service method
export class OrderService {
  async placeRecurringOrder(data: RecurringOrderData) {
    // Reuse existing methods
    const order = await this.placeOrder(data.orderData, data.auth);
    await this.scheduleNextOrder(data.schedule);
    return order;
  }
}

// 2. Thêm trigger
export const placeRecurringOrder = onCall(async (request) => {
  return await orderService.placeRecurringOrder(request.data);
});

// ✅ Không cần sửa code cũ
// ✅ Reuse logic hiện tại
// ✅ Test riêng feature mới
```

### 6. **Onboarding**

Developer mới dễ hiểu code structure:

```
Developer mới: "Tôi cần sửa logic tính giá promotion"

Lead: "Check OrderService.applyPromotion()"

Developer: Đọc OrderService → Hiểu ngay logic → Sửa
           ✅ Không cần đọc trigger, repository, utils

// vs.

Developer mới: "Tôi cần sửa logic tính giá promotion"

Lead: "Ở trong trigger placeOrder, dòng 37-85"

Developer: Đọc 200 dòng trigger → Tìm logic → Sửa
           ❌ Khó hiểu, dễ sửa sai chỗ
```

## Trade-offs (Nhược Điểm)

### 1. **Boilerplate Code**

Phải viết nhiều files, classes:

```
❌ Without Layers (1 file):
src/triggers/place-order.ts (100 lines)

✅ With Layers (4 files):
src/triggers/api.order.ts       (20 lines)
src/services/order.service.ts   (60 lines)
src/repositories/order.repo.ts  (40 lines)
src/models/order.model.ts       (20 lines)
Total: 140 lines (40% more code)
```

**Trade-off acceptable vì:**
- Code dễ maintain hơn nhiều
- Dễ test từng lớp riêng
- Long-term benefit > short-term cost

### 2. **Learning Curve**

Developer mới phải học pattern:

```
Junior Dev:
"Tại sao phải chia ra nhiều file? 
 Để tất cả ở 1 nơi không dễ hơn sao?"

Lead:
"Ban đầu tưởng phức tạp, nhưng khi project lớn,
 layered architecture giúp maintain dễ hơn rất nhiều.
 Hãy thử follow pattern 1-2 tuần, sẽ thấy benefit."
```

### 3. **Over-engineering Cho Features Nhỏ**

```typescript
// Feature đơn giản: Log user login
export const logUserLogin = onUserCreated(async (user) => {
  console.log(`User ${user.uid} logged in`);
});

// Có cần layered không?
// ❌ Không - quá đơn giản, 1 trigger đủ
// ✅ Nhưng nếu sau này mở rộng (send welcome email, create profile),
//    thì layered sẽ dễ maintain hơn
```

## Pattern Mapping: Trách Nhiệm Từng Lớp

| Lớp | Trách Nhiệm | Ví Dụ |
|-----|------------|-------|
| **Trigger** | Validate request format, extract auth | Check `restaurantId` not empty |
| **Service** | Business logic, calculations, validations | Check restaurant open, calculate total |
| **Repository** | Database operations | Save to Firestore, query data |
| **Model** | Type definitions | `interface Order`, `type OrderStatus` |
| **Utils** | Reusable helpers | `toHttpsError()`, `validateEmail()` |

## Khi Nào KHÔNG NÊN Dùng Layered Architecture?

1. **Prototypes/MVPs cực nhỏ (1-2 functions)**
   ```typescript
   // OK - Không cần layered
   export const hello = onCall(async () => {
     return { message: "Hello World" };
   });
   ```

2. **Simple CRUD APIs (không có business logic)**
   ```typescript
   // OK - Repository trong trigger
   export const getUser = onCall(async (request) => {
     const doc = await db.collection('users').doc(request.data.userId).get();
     return doc.data();
   });
   ```

3. **One-time scripts/migrations**
   ```typescript
   // OK - Script chạy 1 lần
   export const migrateData = onRequest(async (req, res) => {
     // Logic ở đây không cần tái sử dụng
   });
   ```

## Kết Luận

**Layered Architecture là lựa chọn đúng đắn cho dự án này vì:**

✅ Backend có nhiều features phức tạp (orders, promotions, notifications)  
✅ Team nhiều developers  
✅ Cần test, maintain, scale dễ dàng  
✅ Long-term project (không phải prototype)  

**Trade-offs acceptable:**
- Boilerplate code nhiều → Nhưng dễ maintain
- Learning curve → Developer quen sau 1-2 tuần
- Over-engineering cho features nhỏ → Benefit khi scale

**Pattern này áp dụng cho:**
- Tất cả callable functions (API endpoints)
- Tất cả Firestore triggers có business logic
- Scheduled functions phức tạp

**Pattern này KHÔNG áp dụng cho:**
- Simple CRUD không có logic
- One-time scripts
- Prototypes/MVPs cực nhỏ

---

**Tham khảo:**
- [Martin Fowler - Layered Architecture](https://martinfowler.com/bliki/PresentationDomainDataLayering.html)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Firebase Functions Best Practices](https://firebase.google.com/docs/functions/best-practices)

**Cập nhật lần cuối:** 7 Tháng 12, 2025
