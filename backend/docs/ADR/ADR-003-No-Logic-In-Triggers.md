# ADR-003: Tại Sao Business Logic KHÔNG Được Viết Trong Triggers?

**Trạng thái:** Chấp nhận  
**Ngày quyết định:** Tháng 11, 2025  
**Người quyết định:** Backend Team  

---

## Bối Cảnh

Firebase Cloud Functions có nhiều loại triggers:
- Callable Functions (HTTP API)
- Firestore Triggers (onDocumentCreated, onDocumentUpdated)
- Auth Triggers (onUserCreated)
- Scheduled Functions (cron jobs)
- Storage Triggers, PubSub, etc.

Developer dễ bị cám dỗ viết business logic trực tiếp trong trigger vì:
- "Nó chỉ là vài dòng code"
- "Nhanh hơn là tạo service riêng"
- "Logic này chỉ dùng ở 1 chỗ"

**Nhưng sau này:**
- Logic cần dùng lại ở trigger khác → Copy-paste
- Bug trong logic → Phải sửa nhiều nơi
- Test khó → Phải mock Firebase context

## Quyết Định

**Triggers chỉ là entry points. Tất cả business logic phải ở Services.**

```typescript
// ❌ BAD
export const placeOrder = onCall(async (request) => {
  // Business logic ở đây - 100 dòng code
});

// ✅ GOOD
export const placeOrder = onCall(async (request) => {
  return await orderService.placeOrder(request.data, request.auth);
});
```

## Lý Do

### 1. **Reusability - Logic Có Thể Được Gọi Từ Nhiều Triggers**

#### ❌ Problem: Logic trong trigger không reuse được

```typescript
// Trigger 1: Client đặt hàng
export const placeOrder = onCall(async (request) => {
  const { data } = request;
  
  // Validate promotion
  const promo = await db.collection("promotions")
    .where("code", "==", data.promotionCode)
    .get();
  
  if (promo.empty) throw new Error("Invalid promo");
  
  let total = calculatePrice(data.items);
  
  if (promo.docs[0].data().discountPercent) {
    total *= (1 - promo.docs[0].data().discountPercent / 100);
  }
  
  // Save order...
});

// Trigger 2: Scheduled function đặt hàng tự động
export const placeScheduledOrder = onSchedule("every day 10:00", async () => {
  // ❌ Copy-paste logic từ trigger 1?
  // ❌ Hoặc gọi placeOrder từ đây? (trigger gọi trigger - weird!)
});

// Trigger 3: Admin đặt hàng thay user
export const placeOrderAsAdmin = onCall(async (request) => {
  // ❌ Copy-paste logic lần nữa?
});
```

#### ✅ Solution: Logic trong service, reuse nhiều nơi

```typescript
// SERVICE - Logic viết 1 lần
export class OrderService {
  async placeOrder(data: PlaceOrderRequest, userId: string) {
    // Validate promotion
    const promotion = await promotionRepository.getByCode(data.promotionCode);
    if (!promotion?.isActive) throw new Error("Invalid promo");
    
    // Calculate total
    let totalAmount = this.calculateTotal(data.items);
    
    // Apply discount
    if (promotion.discountPercent) {
      totalAmount *= (1 - promotion.discountPercent / 100);
    }
    
    // Save order
    return await orderRepository.create({ ...data, totalAmount, userId });
  }
  
  private calculateTotal(items: OrderItem[]): number {
    // Calculation logic
  }
}

// TRIGGER 1 - Client
export const placeOrder = onCall(async (request) => {
  return await orderService.placeOrder(request.data, request.auth.uid);
});

// TRIGGER 2 - Scheduled
export const placeScheduledOrder = onSchedule("every day 10:00", async () => {
  return await orderService.placeOrder(scheduledData, "SYSTEM");
});

// TRIGGER 3 - Admin
export const placeOrderAsAdmin = onCall(async (request) => {
  // Admin có quyền đặt hàng thay user
  return await orderService.placeOrder(request.data, request.data.userId);
});

// ✅ Logic chỉ viết 1 lần
// ✅ Bug fix 1 nơi, 3 triggers đều được fix
// ✅ Test logic 1 lần (test service)
```

### 2. **Testability - Services Dễ Test, Triggers Khó Test**

#### ❌ Problem: Test trigger phức tạp

```typescript
// Trigger với business logic
export const placeOrder = onCall(async (request) => {
  const restaurant = await db.collection("restaurants")
    .doc(request.data.restaurantId)
    .get();
  
  if (!restaurant.data()?.isOpen) {
    throw new Error("Restaurant closed");
  }
  
  // ... 50 dòng business logic
});

// Test trigger - RẤT PHỨC TẠP
describe('placeOrder trigger', () => {
  it('should throw error if restaurant closed', async () => {
    // ❌ Phải setup Firebase emulator
    // ❌ Phải mock Firestore
    // ❌ Phải mock auth context
    // ❌ Phải mock CallableRequest object
    
    const mockRequest = {
      data: { restaurantId: 'rest_1' },
      auth: { uid: 'user_1' },
      rawRequest: { /* ... */ }
    } as CallableRequest;
    
    // Test...
  });
});
```

#### ✅ Solution: Test service đơn giản

```typescript
// Service với business logic
export class OrderService {
  async placeOrder(data: PlaceOrderRequest, userId: string) {
    const restaurant = await restaurantRepository.getById(data.restaurantId);
    
    if (!restaurant?.isOpen) {
      throw new Error("Restaurant closed");
    }
    
    // ... business logic
  }
}

// Test service - ĐƠN GIẢN
describe('OrderService.placeOrder', () => {
  it('should throw error if restaurant closed', async () => {
    // ✅ Chỉ mock repository
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue({
      id: 'rest_1',
      isOpen: false
    });
    
    // ✅ Test logic
    await expect(
      orderService.placeOrder({ restaurantId: 'rest_1' }, 'user_1')
    ).rejects.toThrow('Restaurant closed');
  });
  
  it('should calculate total correctly with discount', async () => {
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue(mockRestaurant);
    jest.spyOn(promotionRepository, 'getByCode').mockResolvedValue({
      discountPercent: 20
    });
    
    const result = await orderService.placeOrder(mockData, 'user_1');
    
    expect(result.totalAmount).toBe(80000); // 100k - 20% = 80k
  });
});

// Trigger - Test đơn giản (chỉ test integration)
describe('placeOrder trigger', () => {
  it('should call orderService.placeOrder', async () => {
    const spy = jest.spyOn(orderService, 'placeOrder');
    
    await placeOrder({ data: mockData, auth: { uid: 'user_1' } });
    
    expect(spy).toHaveBeenCalledWith(mockData, 'user_1');
  });
});
```

### 3. **Maintainability - Dễ Sửa Bug**

#### ❌ Problem: Bug trong trigger phức tạp

```
Bug Report: "Promotion không được apply đúng"

Code:
- placeOrder trigger (150 dòng logic)
- placeScheduledOrder trigger (120 dòng logic)
- placeOrderAsAdmin trigger (130 dòng logic)

Developer:
1. Đọc placeOrder trigger → Tìm bug ở dòng 73
2. Sửa placeOrder trigger
3. Ah, placeScheduledOrder cũng có bug tương tự → Sửa
4. placeOrderAsAdmin cũng có bug → Sửa
5. ❌ Quên sửa placeOrderFromWebhook → Bug vẫn còn

→ 4 triggers, 4 nơi phải sửa
→ Dễ miss một vài triggers
→ Code review khó (4 files changes)
```

#### ✅ Solution: Bug trong service sửa 1 lần

```
Bug Report: "Promotion không được apply đúng"

Code:
- OrderService.placeOrder (business logic)
- 4 triggers gọi service

Developer:
1. Đọc OrderService.placeOrder → Tìm bug
2. Sửa OrderService.applyPromotion()
3. ✅ Done - tất cả triggers tự động được fix

→ 1 service, 1 nơi sửa
→ Không miss nơi nào
→ Code review dễ (1 file change)
```

### 4. **Separation of Concerns - Trigger Chỉ Giải Quyết "Entry Point"**

#### ❌ Problem: Trigger làm quá nhiều việc

```typescript
export const placeOrder = onCall(async (request) => {
  // 1. Parse & validate input format
  if (!request.data.restaurantId) throw new Error("Missing restaurantId");
  
  // 2. Extract auth
  const userId = request.auth?.uid;
  if (!userId) throw new Error("Unauthenticated");
  
  // 3. Validate business rules
  const restaurant = await db.collection("restaurants").doc(request.data.restaurantId).get();
  if (!restaurant.data()?.isOpen) throw new Error("Restaurant closed");
  
  // 4. Calculate total
  let total = 0;
  for (const item of request.data.items) {
    const menuItem = await db.collection("restaurants")
      .doc(request.data.restaurantId)
      .collection("menuItems")
      .doc(item.menuItemId)
      .get();
    total += menuItem.data().price * item.quantity;
  }
  
  // 5. Apply promotion
  const promo = await db.collection("promotions").where("code", "==", request.data.promotionCode).get();
  if (!promo.empty) {
    total *= (1 - promo.docs[0].data().discountPercent / 100);
  }
  
  // 6. Save to database
  const order = await db.collection("orders").add({ ...data, total });
  
  // 7. Send notification
  await sendNotification(restaurant.data().sellerId, `New order: ${order.id}`);
  
  return { orderId: order.id, totalAmount: total };
});

// ❌ Trigger làm 7 việc khác nhau
// ❌ Không rõ phần nào là "entry point logic" vs "business logic"
// ❌ Khó test, maintain, reuse
```

#### ✅ Solution: Phân tách trách nhiệm rõ ràng

```typescript
// TRIGGER - Chỉ xử lý "entry point"
export const placeOrder = onCall<PlaceOrderRequest>(async (request) => {
  const { data, auth } = request;
  
  // 1. Validate input format (entry point responsibility)
  if (!data.restaurantId) throw new Error("restaurantId required");
  if (!data.items?.length) throw new Error("items empty");
  
  // 2. Extract auth (entry point responsibility)
  if (!auth?.uid) throw new Error("Unauthenticated");
  
  // 3. Delegate to service (business logic)
  return await orderService.placeOrder(data, auth.uid);
});

// SERVICE - Business logic
export class OrderService {
  async placeOrder(data: PlaceOrderRequest, userId: string) {
    // Validate business rules
    const restaurant = await restaurantRepository.getById(data.restaurantId);
    if (!restaurant?.isOpen) throw new Error("Restaurant closed");
    
    // Calculate total
    const totalAmount = await this.calculateTotal(data.restaurantId, data.items);
    
    // Apply promotion
    const finalAmount = await promotionService.applyPromotion(totalAmount, data.promotionCode);
    
    // Save order
    const orderId = await orderRepository.create({
      userId,
      restaurantId: data.restaurantId,
      items: data.items,
      totalAmount: finalAmount,
      status: "PENDING"
    });
    
    // Send notification
    await notificationService.sendToSeller(restaurant.sellerId, orderId);
    
    return { orderId, totalAmount: finalAmount };
  }
}

// REPOSITORY - Data access
export class OrderRepository {
  async create(data: Order): Promise<string> {
    const docRef = await this.db.collection("orders").add(data);
    return docRef.id;
  }
}

// ✅ Rõ ràng: Trigger = entry point, Service = business logic, Repository = data
```

## Pattern Chuẩn

| Lớp | Trách Nhiệm | Không Được Làm |
|-----|------------|----------------|
| **Trigger** | • Validate request format<br>• Extract auth context<br>• Call service | • Business logic<br>• Database queries<br>• Calculations |
| **Service** | • Business rules validation<br>• Calculations<br>• Orchestrate repositories | • Parse request format<br>• Handle auth extraction |
| **Repository** | • Database operations<br>• Data queries | • Business logic<br>• Calculations |

## Ví Dụ Thực Tế: Seller Xác Nhận Đơn

```typescript
// ❌ BAD - Logic trong trigger
export const confirmOrder = onCall(async (request) => {
  const { orderId } = request.data;
  const userId = request.auth.uid;
  
  // Get order
  const order = await db.collection("orders").doc(orderId).get();
  if (!order.exists) throw new Error("Order not found");
  
  // Get restaurant
  const restaurant = await db.collection("restaurants").doc(order.data().restaurantId).get();
  
  // Check permission
  if (restaurant.data().sellerId !== userId) {
    throw new Error("Permission denied");
  }
  
  // Update order
  await db.collection("orders").doc(orderId).update({
    status: "CONFIRMED",
    updatedAt: new Date()
  });
  
  // Send notification
  await sendNotification(order.data().userId, "Your order is confirmed");
  
  return { success: true };
});

// ✅ GOOD - Logic trong service
export const confirmOrder = onCall(async (request) => {
  return await orderService.confirmOrder(request.data.orderId, request.auth.uid);
});

export class OrderService {
  async confirmOrder(orderId: string, userId: string) {
    // Get order
    const order = await orderRepository.getById(orderId);
    if (!order) throw new Error("Order not found");
    
    // Check permission
    const restaurant = await restaurantRepository.getById(order.restaurantId);
    if (restaurant.sellerId !== userId) {
      throw new Error("Permission denied");
    }
    
    // Update status
    await orderRepository.updateStatus(orderId, "CONFIRMED");
    
    // Notify buyer
    await notificationService.sendToBuyer(order.userId, {
      title: "Order Confirmed",
      body: `Your order #${orderId} is confirmed`
    });
    
    return { success: true };
  }
}
```

## Exceptions: Khi Nào Logic Trong Trigger OK?

### 1. **Firestore Triggers Đơn Giản**

```typescript
// ✅ OK - Chỉ log, không có business logic
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  console.log(`Order created: ${event.params.orderId}`);
});

// ✅ OK - Analytics logging
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  await analytics.logEvent("order_created", {
    orderId: event.params.orderId,
    totalAmount: event.data.data().totalAmount
  });
});
```

### 2. **Auth Triggers Đơn Giản**

```typescript
// ✅ OK - Chỉ tạo user document
export const onUserCreated = onUserCreated(async (user) => {
  await db.collection("users").doc(user.uid).set({
    email: user.email,
    createdAt: new Date()
  });
});
```

### 3. **Scheduled Functions Không Có Logic Phức Tạp**

```typescript
// ✅ OK - Chỉ cleanup
export const cleanupExpiredSessions = onSchedule("every day 00:00", async () => {
  const expiredSessions = await db.collection("sessions")
    .where("expiresAt", "<", new Date())
    .get();
  
  const batch = db.batch();
  expiredSessions.forEach(doc => batch.delete(doc.ref));
  await batch.commit();
});
```

## Kết Luận

**Business logic PHẢI ở Services vì:**

✅ **Reusability** - Logic dùng lại nhiều triggers  
✅ **Testability** - Dễ test service (mock repositories)  
✅ **Maintainability** - Bug fix 1 nơi, tất cả triggers được fix  
✅ **Separation of Concerns** - Trigger = entry, Service = logic, Repository = data  

**Trigger chỉ làm:**
1. Validate input format
2. Extract auth context
3. Call service
4. Return response

**Exceptions:**
- Firestore triggers đơn giản (log, analytics)
- Auth triggers đơn giản (create user document)
- Scheduled functions không có logic phức tạp

---

**Tham khảo:**
- [Clean Code by Robert C. Martin](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
- [Firebase Functions Best Practices](https://firebase.google.com/docs/functions/best-practices)
- [Separation of Concerns - Wikipedia](https://en.wikipedia.org/wiki/Separation_of_concerns)

**Cập nhật lần cuối:** 7 Tháng 12, 2025
