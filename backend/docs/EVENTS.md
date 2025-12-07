# Event-Driven Architecture

## 📋 Tổng Quan

**Event-Driven Architecture (Kiến trúc hướng sự kiện)** là pattern trong đó các service giao tiếp với nhau thông qua events thay vì gọi trực tiếp. Khi một action xảy ra (ví dụ: order được tạo), hệ thống phát ra một event, và các service khác có thể "lắng nghe" và phản ứng tự động.

### Lợi Ích

✅ **Decoupling** - Các service không phụ thuộc trực tiếp vào nhau  
✅ **Scalability** - Dễ dàng thêm listeners mới mà không ảnh hưởng code cũ  
✅ **Async Processing** - Các handlers chạy song song, không block nhau  
✅ **Real-time** - Events xảy ra tức thì, phản hồi nhanh  
✅ **Audit Trail** - Có record của mọi event để trace và debug  
✅ **Future Ready** - Dễ tích hợp analytics, messaging, monitoring sau này  

---

## 🔄 Mô Hình Event-Driven

```
┌─────────────────┐
│  Action Occurs  │  (User đặt hàng, seller xác nhận, etc.)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Event Fired    │  (OrderCreated, OrderStatusUpdated, UserCreated)
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────┐
│  Firestore/Auth Triggers Automatically      │
│  - onDocumentCreated                        │
│  - onDocumentUpdated                        │
│  - onUserCreated                            │
└────────┬────────────────────────────────────┘
         │
         ├─────────────────┬─────────────────┬──────────────────┐
         ▼                 ▼                 ▼                  ▼
┌──────────────┐  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐
│   Handler 1  │  │  Handler 2  │  │   Handler 3  │  │   Handler 4  │
│ Notification │  │  Analytics  │  │   Metrics    │  │   Logging    │
└──────────────┘  └─────────────┘  └──────────────┘  └──────────────┘
```

**Flow:**
1. **Action** - User thực hiện hành động (đặt hàng, đăng ký, etc.)
2. **Event Fired** - Firestore/Auth tự động phát event
3. **Trigger Listens** - Cloud Functions trigger tự động chạy
4. **Multiple Handlers** - Các service xử lý song song

---

## 🎯 Các Event Chính Trong Hệ Thống

### 1️⃣ Event: OrderCreated

**Khi nào xảy ra:**  
Khách hàng đặt hàng thành công → Document mới được tạo trong collection `orders`

**Trigger:** `onDocumentCreated("orders/{orderId}")`

**Flow chi tiết:**

```
Client gọi placeOrder()
        │
        ▼
┌────────────────────────────────────┐
│ Trigger: api.order.ts              │
│ • Validate input                   │
│ • Gọi orderService.placeOrder()    │
└───────────────┬────────────────────┘
                │
                ▼
┌────────────────────────────────────┐
│ Service: order.service.ts          │
│ • Validate restaurant & menu items │
│ • Calculate totalAmount            │
│ • Apply promotion                  │
│ • Gọi orderRepository.create()     │
└───────────────┬────────────────────┘
                │
                ▼
┌────────────────────────────────────┐
│ Repository: order.repository.ts    │
│ • Lưu order vào Firestore          │
│ • Return orderId                   │
└───────────────┬────────────────────┘
                │
                ▼
          FIRESTORE
      (Document created)
                │
                ├─ 🔥 EVENT: OrderCreated
                │
                ▼
┌────────────────────────────────────┐
│ Trigger: order.trigger.ts          │
│ onOrderCreated()                   │
│                                    │
│ Tự động chạy khi order được tạo    │
└───────────────┬────────────────────┘
                │
                ├─────────────────────────┬────────────────────┬───────────────────┐
                ▼                         ▼                    ▼                   ▼
        ┌──────────────┐         ┌──────────────┐    ┌──────────────┐   ┌──────────────┐
        │ Handler 1:   │         │ Handler 2:   │    │ Handler 3:   │   │ Handler 4:   │
        │ Notify       │         │ Notify       │    │ Update       │   │ Log Event    │
        │ Seller       │         │ Buyer        │    │ Restaurant   │   │ (Analytics)  │
        │              │         │              │    │ Stats        │   │              │
        └──────────────┘         └──────────────┘    └──────────────┘   └──────────────┘
```

**Implementation:**

```typescript
// order.trigger.ts
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { notificationService } from "../services/notification.service";
import { restaurantRepository } from "../repositories/restaurant.repository";
import { Order } from "../models/order.model";

/**
 * Trigger tự động khi order được tạo
 * Xử lý các tác vụ post-order: notifications, stats, logging
 */
export const onOrderCreated = onDocumentCreated(
  "orders/{orderId}",
  async (event) => {
    const orderId = event.params.orderId;
    const orderData = event.data?.data() as Order;

    if (!orderData) {
      console.error(`[onOrderCreated] No data for order ${orderId}`);
      return;
    }

    console.log(`[EVENT] OrderCreated: ${orderId}`);

    try {
      // Handler 1: Gửi notification cho seller
      await notificationService.sendToSeller({
        restaurantId: orderData.restaurantId,
        title: "Đơn hàng mới",
        message: `Bạn có đơn hàng mới #${orderId}`,
        data: {
          orderId,
          totalAmount: orderData.totalAmount.toString(),
          itemCount: orderData.items.length.toString(),
        },
      });

      console.log(`[Handler 1] Notification sent to seller`);
    } catch (error) {
      console.error(`[Handler 1] Error sending seller notification:`, error);
    }

    try {
      // Handler 2: Gửi confirmation cho buyer
      await notificationService.sendToBuyer({
        userId: orderData.userId,
        title: "Đặt hàng thành công",
        message: `Đơn hàng #${orderId} đã được tạo thành công`,
        data: {
          orderId,
          status: orderData.status,
        },
      });

      console.log(`[Handler 2] Confirmation sent to buyer`);
    } catch (error) {
      console.error(`[Handler 2] Error sending buyer confirmation:`, error);
    }

    try {
      // Handler 3: Update restaurant statistics
      await restaurantRepository.incrementOrderCount(
        orderData.restaurantId
      );

      console.log(`[Handler 3] Restaurant stats updated`);
    } catch (error) {
      console.error(`[Handler 3] Error updating restaurant stats:`, error);
    }

    // Handler 4: Log event cho analytics
    console.info("[EVENT_LOG]", {
      event: "order_created",
      orderId,
      restaurantId: orderData.restaurantId,
      userId: orderData.userId,
      totalAmount: orderData.totalAmount,
      itemCount: orderData.items.length,
      timestamp: new Date().toISOString(),
    });
  }
);
```

**Listeners (Consumers):**

| Handler | Service | Action |
|---------|---------|--------|
| Handler 1 | NotificationService | Gửi FCM notification cho seller |
| Handler 2 | NotificationService | Gửi confirmation cho buyer |
| Handler 3 | RestaurantRepository | Tăng orderCount của restaurant |
| Handler 4 | Analytics | Log event để reporting |

**Data Flow:**

```typescript
// Input (Firestore event)
{
  orderId: "order_abc123",
  data: {
    userId: "user_456",
    restaurantId: "rest_789",
    items: [...],
    status: "PENDING",
    totalAmount: 125000,
    createdAt: "2025-12-07T10:30:00Z"
  }
}

// Output (Multiple async actions)
✅ FCM notification → Seller app
✅ FCM notification → Buyer app
✅ Firestore update → Restaurant stats
✅ Console log → Analytics system
```

---

### 2️⃣ Event: OrderStatusUpdated

**Khi nào xảy ra:**  
Trạng thái order thay đổi (PENDING → CONFIRMED → DELIVERING → COMPLETED/CANCELLED)

**Trigger:** `onDocumentUpdated("orders/{orderId}")`

**Flow chi tiết:**

```
Seller/Shipper update order status
        │
        ▼
┌────────────────────────────────────┐
│ Trigger: api.order.ts              │
│ updateOrderStatus()                │
└───────────────┬────────────────────┘
                │
                ▼
┌────────────────────────────────────┐
│ Service: order.service.ts          │
│ • Validate status transition       │
│ • Check permissions                │
│ • Gọi orderRepository.update()     │
└───────────────┬────────────────────┘
                │
                ▼
┌────────────────────────────────────┐
│ Repository: order.repository.ts    │
│ • Update status trong Firestore    │
└───────────────┬────────────────────┘
                │
                ▼
          FIRESTORE
      (Document updated)
                │
                ├─ 🔥 EVENT: OrderStatusUpdated
                │
                ▼
┌────────────────────────────────────┐
│ Trigger: order.trigger.ts          │
│ onOrderUpdated()                   │
└───────────────┬────────────────────┘
                │
                ├─────────────────┬──────────────────┬──────────────────┐
                ▼                 ▼                  ▼                  ▼
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │ Handler 1:   │  │ Handler 2:   │  │ Handler 3:   │  │ Handler 4:   │
        │ Notify       │  │ Log Status   │  │ Update       │  │ Send         │
        │ Buyer        │  │ Transition   │  │ Timeline     │  │ Webhook      │
        │              │  │ (Analytics)  │  │              │  │ (Future)     │
        └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
```

**Implementation:**

```typescript
// order.trigger.ts
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { notificationService } from "../services/notification.service";
import { Order } from "../models/order.model";

/**
 * Trigger tự động khi order được update
 * Chỉ xử lý khi status thay đổi
 */
export const onOrderUpdated = onDocumentUpdated(
  "orders/{orderId}",
  async (event) => {
    const orderId = event.params.orderId;

    // Lấy data before & after
    const before = event.data?.before.data() as Order;
    const after = event.data?.after.data() as Order;

    if (!before || !after) {
      console.error(`[onOrderUpdated] Missing data for order ${orderId}`);
      return;
    }

    // Chỉ xử lý khi status thay đổi
    if (before.status === after.status) {
      console.log(`[onOrderUpdated] Status unchanged, skipping`);
      return;
    }

    console.log(
      `[EVENT] OrderStatusUpdated: ${before.status} → ${after.status}`
    );

    try {
      // Handler 1: Notify buyer về status change
      const statusMessages = {
        CONFIRMED: "Đơn hàng đã được xác nhận",
        PREPARING: "Nhà hàng đang chuẩn bị món",
        READY_FOR_DELIVERY: "Đơn hàng sẵn sàng giao",
        DELIVERING: "Đơn hàng đang được giao",
        COMPLETED: "Đơn hàng đã hoàn thành",
        CANCELLED: "Đơn hàng đã bị hủy",
      };

      await notificationService.sendToBuyer({
        userId: after.userId,
        title: "Cập nhật đơn hàng",
        message: statusMessages[after.status] || `Trạng thái: ${after.status}`,
        data: {
          orderId,
          status: after.status,
          previousStatus: before.status,
        },
      });

      console.log(`[Handler 1] Status notification sent to buyer`);
    } catch (error) {
      console.error(`[Handler 1] Error sending notification:`, error);
    }

    // Handler 2: Log status transition cho analytics
    console.info("[EVENT_LOG]", {
      event: "order_status_updated",
      orderId,
      previousStatus: before.status,
      newStatus: after.status,
      userId: after.userId,
      restaurantId: after.restaurantId,
      timestamp: new Date().toISOString(),
    });

    try {
      // Handler 3: Update order timeline (subcollection)
      await event.data?.after.ref
        .collection("timeline")
        .add({
          status: after.status,
          previousStatus: before.status,
          timestamp: new Date().toISOString(),
          updatedBy: after.updatedBy || "system",
        });

      console.log(`[Handler 3] Timeline updated`);
    } catch (error) {
      console.error(`[Handler 3] Error updating timeline:`, error);
    }

    // Handler 4: Future - Send webhook to external system
    // await webhookService.sendOrderUpdate(orderId, after.status);
  }
);
```

**Status Transitions:**

```
PENDING ──────────► CONFIRMED ──────────► PREPARING
                        │                      │
                        │                      ▼
                        │              READY_FOR_DELIVERY
                        │                      │
                        ▼                      ▼
                    CANCELLED             DELIVERING
                                               │
                                               ▼
                                          COMPLETED
```

**Listeners (Consumers):**

| Handler | Service | Action |
|---------|---------|--------|
| Handler 1 | NotificationService | Gửi notification về status mới |
| Handler 2 | Analytics | Log transition time & patterns |
| Handler 3 | OrderRepository | Update timeline subcollection |
| Handler 4 | WebhookService | Send update to external systems (future) |

**Use Cases:**

```typescript
// PENDING → CONFIRMED (Seller xác nhận)
{
  previousStatus: "PENDING",
  newStatus: "CONFIRMED",
  notification: "Đơn hàng đã được xác nhận"
}

// CONFIRMED → PREPARING (Seller bắt đầu làm)
{
  previousStatus: "CONFIRMED",
  newStatus: "PREPARING",
  notification: "Nhà hàng đang chuẩn bị món"
}

// READY_FOR_DELIVERY → DELIVERING (Shipper nhận hàng)
{
  previousStatus: "READY_FOR_DELIVERY",
  newStatus: "DELIVERING",
  notification: "Đơn hàng đang được giao"
}

// DELIVERING → COMPLETED (Giao thành công)
{
  previousStatus: "DELIVERING",
  newStatus: "COMPLETED",
  notification: "Đơn hàng đã hoàn thành"
}

// PENDING → CANCELLED (Buyer/Seller hủy)
{
  previousStatus: "PENDING",
  newStatus: "CANCELLED",
  notification: "Đơn hàng đã bị hủy"
}
```

---

### 3️⃣ Event: UserCreated

**Khi nào xảy ra:**  
User đăng ký account mới → Firebase Auth tạo user mới

**Trigger:** `onUserCreated()`

**Flow chi tiết:**

```
User đăng ký (signUp)
        │
        ▼
    FIREBASE AUTH
    (User created)
        │
        ├─ 🔥 EVENT: UserCreated
        │
        ▼
┌────────────────────────────────────┐
│ Trigger: auth.trigger.ts           │
│ onUserCreated()                    │
│                                    │
│ Tự động chạy khi user register     │
└───────────────┬────────────────────┘
                │
                ├─────────────────────┬──────────────────┬──────────────────┐
                ▼                     ▼                  ▼                  ▼
        ┌──────────────┐      ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │ Handler 1:   │      │ Handler 2:   │  │ Handler 3:   │  │ Handler 4:   │
        │ Create User  │      │ Send Welcome │  │ Initialize   │  │ Log Event    │
        │ Document     │      │ Notification │  │ Settings     │  │ (Analytics)  │
        │ (Firestore)  │      │              │  │              │  │              │
        └──────────────┘      └──────────────┘  └──────────────┘  └──────────────┘
```

**Implementation:**

```typescript
// auth.trigger.ts
import { onUserCreated } from "firebase-functions/v2/identity";
import { userRepository } from "../repositories/user.repository";
import { notificationService } from "../services/notification.service";

/**
 * Trigger tự động khi user đăng ký
 * Initialize user profile và send welcome message
 */
export const onUserCreated = onUserCreated(async (event) => {
  const user = event.data;
  const uid = user.uid;
  const email = user.email || "";
  const displayName = user.displayName || "";

  console.log(`[EVENT] UserCreated: ${uid}`);

  try {
    // Handler 1: Tạo user document trong Firestore
    await userRepository.createUserDocument(uid, {
      uid,
      email,
      displayName,
      role: "BUYER", // Default role
      status: "ACTIVE",
      isVerified: false,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      profileComplete: false,
    });

    console.log(`[Handler 1] User document created in Firestore`);
  } catch (error) {
    console.error(`[Handler 1] Error creating user document:`, error);
  }

  try {
    // Handler 2: Gửi welcome notification
    // Note: Cần delay một chút để FCM token được register
    setTimeout(async () => {
      await notificationService.sendToUser({
        userId: uid,
        title: "Chào mừng đến với Food App!",
        message: "Cảm ơn bạn đã đăng ký. Hãy khám phá các nhà hàng ngon nhé!",
        data: {
          type: "welcome",
        },
      });

      console.log(`[Handler 2] Welcome notification sent`);
    }, 5000); // Delay 5 seconds
  } catch (error) {
    console.error(`[Handler 2] Error sending welcome notification:`, error);
  }

  try {
    // Handler 3: Initialize user settings & preferences
    await userRepository.initializeUserSettings(uid, {
      notifications: {
        orderUpdates: true,
        promotions: true,
        newsletter: true,
      },
      preferences: {
        language: "vi",
        currency: "VND",
        theme: "light",
      },
    });

    console.log(`[Handler 3] User settings initialized`);
  } catch (error) {
    console.error(`[Handler 3] Error initializing settings:`, error);
  }

  // Handler 4: Log event cho analytics
  console.info("[EVENT_LOG]", {
    event: "user_created",
    userId: uid,
    email,
    displayName,
    timestamp: new Date().toISOString(),
  });

  // Handler 5: Send welcome email (future)
  // await emailService.sendWelcomeEmail(email, displayName);
});
```

**Listeners (Consumers):**

| Handler | Service | Action |
|---------|---------|--------|
| Handler 1 | UserRepository | Tạo user document trong Firestore |
| Handler 2 | NotificationService | Gửi welcome notification |
| Handler 3 | UserRepository | Initialize settings & preferences |
| Handler 4 | Analytics | Log signup event |
| Handler 5 | EmailService | Send welcome email (future) |

**Data Created:**

```typescript
// Firestore: /users/{uid}
{
  uid: "user_abc123",
  email: "john@example.com",
  displayName: "John Doe",
  role: "BUYER",
  status: "ACTIVE",
  isVerified: false,
  createdAt: "2025-12-07T10:30:00Z",
  updatedAt: "2025-12-07T10:30:00Z",
  profileComplete: false,
  
  // Subcollection: /users/{uid}/settings
  settings: {
    notifications: {
      orderUpdates: true,
      promotions: true,
      newsletter: true
    },
    preferences: {
      language: "vi",
      currency: "VND",
      theme: "light"
    }
  }
}
```

---

## 🔧 Hướng Dẫn Thêm Event Mới

### Step 1: Xác Định Event

**Câu hỏi cần trả lời:**
- Event gì? (ví dụ: PromotionUsed, RestaurantRated)
- Khi nào event xảy ra?
- Data nào cần truyền?
- Ai cần lắng nghe event này?

**Ví dụ:** Thêm event `PromotionUsed`

```
Event: PromotionUsed
Trigger: Khi user apply promotion code thành công
Data: promotionId, userId, orderId, discountAmount
Listeners: 
  - PromotionRepository (update usageCount)
  - Analytics (track promotion effectiveness)
  - NotificationService (notify seller about promotion usage)
```

### Step 2: Tạo Trigger File

**File: `src/triggers/promotion.trigger.ts`**

```typescript
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { promotionRepository } from "../repositories/promotion.repository";
import { notificationService } from "../services/notification.service";

/**
 * Event: PromotionUsed
 * Trigger khi order được update với promotionCode
 */
export const onPromotionUsed = onDocumentUpdated(
  "orders/{orderId}",
  async (event) => {
    const orderId = event.params.orderId;
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    // Chỉ xử lý khi promotionCode được thêm vào
    if (
      !before?.promotionCode &&
      after?.promotionCode &&
      after?.promotionId
    ) {
      console.log(`[EVENT] PromotionUsed: ${after.promotionId}`);

      // Handler 1: Update promotion usage count
      try {
        await promotionRepository.incrementUsageCount(after.promotionId);
        console.log(`[Handler 1] Usage count updated`);
      } catch (error) {
        console.error(`[Handler 1] Error:`, error);
      }

      // Handler 2: Log analytics
      console.info("[EVENT_LOG]", {
        event: "promotion_used",
        promotionId: after.promotionId,
        promotionCode: after.promotionCode,
        orderId,
        userId: after.userId,
        discountAmount: after.discountAmount,
        timestamp: new Date().toISOString(),
      });

      // Handler 3: Notify seller (optional)
      try {
        await notificationService.sendToSeller({
          restaurantId: after.restaurantId,
          title: "Khuyến mãi được sử dụng",
          message: `Mã ${after.promotionCode} đã được áp dụng cho đơn ${orderId}`,
          data: {
            orderId,
            promotionCode: after.promotionCode,
          },
        });
        console.log(`[Handler 3] Seller notified`);
      } catch (error) {
        console.error(`[Handler 3] Error:`, error);
      }
    }
  }
);
```

### Step 3: Export Trigger

**File: `src/index.ts`**

```typescript
// Existing exports
export * from "./triggers/api.order";
export * from "./triggers/api.promotion";
export * from "./triggers/auth.trigger";
export * from "./triggers/order.trigger";

// New export
export * from "./triggers/promotion.trigger";
```

### Step 4: Deploy & Test

```bash
# Build
npm run build

# Deploy
firebase deploy --only functions:onPromotionUsed

# Test
# 1. Tạo order với promotionCode
# 2. Check logs: npm run logs -- --only onPromotionUsed
# 3. Verify: promotion usageCount tăng, notification gửi đi
```

### Step 5: Document Event

**Thêm vào file này (EVENTS.md):**

```markdown
### 4️⃣ Event: PromotionUsed

**Khi nào xảy ra:** User áp dụng promotion code thành công

**Trigger:** `onDocumentUpdated("orders/{orderId}")`

**Listeners:**
1. PromotionRepository - Update usageCount
2. Analytics - Track promotion effectiveness
3. NotificationService - Notify seller

**Data Flow:**
...
```

---

## 📊 Event Flow Summary

**Tổng hợp các events trong hệ thống:**

| Event | Trigger Type | When | Handlers |
|-------|--------------|------|----------|
| **OrderCreated** | onDocumentCreated | Order mới được tạo | Notify seller, notify buyer, update stats, log |
| **OrderStatusUpdated** | onDocumentUpdated | Status thay đổi | Notify buyer, log transition, update timeline |
| **UserCreated** | onUserCreated | User đăng ký | Create profile, welcome message, init settings |
| **PromotionUsed** | onDocumentUpdated | Promotion applied | Update usage count, analytics, notify seller |

---

## 🎯 Best Practices

### DO ✅

```typescript
// ✅ GOOD - Handlers độc lập, có try-catch riêng
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  // Handler 1
  try {
    await notificationService.sendToSeller(...);
  } catch (error) {
    console.error("Handler 1 error:", error);
    // Không throw, để handlers khác chạy tiếp
  }

  // Handler 2
  try {
    await restaurantRepository.updateStats(...);
  } catch (error) {
    console.error("Handler 2 error:", error);
  }
});

// ✅ GOOD - Log event cho analytics
console.info("[EVENT_LOG]", {
  event: "order_created",
  orderId,
  timestamp: new Date().toISOString()
});

// ✅ GOOD - Check data exists
if (!orderData) {
  console.error("No data for order");
  return;
}
```

### DON'T ❌

```typescript
// ❌ BAD - Một handler fail làm toàn bộ fail
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  await notificationService.sendToSeller(...);  // Nếu fail → toàn bộ fail
  await restaurantRepository.updateStats(...);   // Không chạy được
});

// ❌ BAD - Không log event
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  // Xử lý nhưng không log → khó track & debug
  await notificationService.sendToSeller(...);
});

// ❌ BAD - Không validate data
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  const orderData = event.data?.data();
  // Không check orderData có tồn tại không
  await notificationService.sendToSeller({
    restaurantId: orderData.restaurantId  // Có thể undefined → crash
  });
});
```

---

## 🐛 Debugging Events

### 1. Logs Trong Firebase Console

```bash
# View logs của specific trigger
firebase functions:log --only onOrderCreated

# Tail logs real-time
firebase functions:log --only onOrderCreated --tail
```

### 2. Test Locally với Emulator

```bash
# Start emulator
firebase emulators:start

# Trigger event bằng cách tạo document
# Emulator sẽ tự động fire trigger
```

### 3. Check Event Đã Fire Chưa

```typescript
// Add logging ở đầu trigger
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  console.log("[TRIGGER FIRED] onOrderCreated");
  console.log("Order ID:", event.params.orderId);
  console.log("Order data:", event.data?.data());
  
  // ... handlers
});
```

### 4. Monitor Handler Success/Failure

```typescript
export const onOrderCreated = onDocumentCreated("orders/{orderId}", async (event) => {
  const startTime = Date.now();
  
  // Handler with timing
  try {
    await notificationService.sendToSeller(...);
    console.log(`[Handler 1] Success (${Date.now() - startTime}ms)`);
  } catch (error) {
    console.error(`[Handler 1] Failed (${Date.now() - startTime}ms):`, error);
  }
});
```

---

## 🔗 Tài Liệu Liên Quan

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc layered
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Cách xử lý lỗi trong system
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Hướng dẫn phát triển
- [Firebase Triggers Documentation](https://firebase.google.com/docs/functions/firestore-events) - Official docs

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
