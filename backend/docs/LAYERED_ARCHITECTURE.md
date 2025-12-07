# Layered Architecture - Chi Tiết Implementation

> **Tài liệu này cung cấp chi tiết implementation của kiến trúc phân lớp trong Firebase Functions backend.**

---

## 📋 Mục Lục

1. [Tổng Quan Layered Architecture](#tổng-quan)
2. [Layer 1: Triggers (Entry Points)](#layer-1-triggers)
3. [Layer 2: Services (Business Logic)](#layer-2-services)
4. [Layer 3: Repositories (Data Access)](#layer-3-repositories)
5. [Layer 4: Models (Type Definitions)](#layer-4-models)
6. [Layer 5: Utils (Helpers)](#layer-5-utils)
7. [Data Flow Examples](#data-flow-examples)
8. [Best Practices & Conventions](#best-practices)
9. [Testing Strategy](#testing-strategy)

---

## 🏗️ Tổng Quan

Layered Architecture tổ chức code theo 5 lớp, mỗi lớp có trách nhiệm rõ ràng:

```
┌─────────────────────────────────────┐
│      TRIGGERS (Entry Points)        │  ← Client gọi hàm này
│  - onCall (HTTP Functions)          │     HTTP requests, events
│  - onDocumentCreated (Firestore)    │
│  - onUserCreated (Auth)             │
└──────────────┬──────────────────────┘
               │ validates input
               │ extracts auth
               ▼
┌─────────────────────────────────────┐
│       SERVICES (Business Logic)     │  ← Xử lý business rules
│  - Order Service                    │     calculations, validations
│  - Promotion Service                │     orchestration
│  - Notification Service             │
└──────────────┬──────────────────────┘
               │ calls repositories
               │ for data operations
               ▼
┌─────────────────────────────────────┐
│    REPOSITORIES (Data Access)       │  ← Tương tác Firestore
│  - Order Repository                 │     CRUD operations
│  - User Repository                  │     queries, filters
│  - Restaurant Repository            │
│  - Promotion Repository             │
└──────────────┬──────────────────────┘
               │ uses models for types
               │ returns typed data
               ▼
┌─────────────────────────────────────┐
│        MODELS (Type Definitions)    │  ← TypeScript types
│  - Order, User, Restaurant          │     interfaces, enums
│  - Request/Response types           │
└─────────────────────────────────────┘
               │
               │ utils provide helpers
               ▼
┌─────────────────────────────────────┐
│           UTILS (Helpers)           │  ← Hàm tiện ích
│  - Error handling                   │     validation, logging
│  - Validation                       │     error mapping
│  - Logging                          │
└─────────────────────────────────────┘
```

**Nguyên tắc chính:**
- **Separation of Concerns** - Mỗi lớp có trách nhiệm riêng biệt
- **Dependency Rule** - Lớp trên phụ thuộc lớp dưới, không ngược lại
- **No Skip** - Không được skip layers (Trigger phải gọi Service, không gọi trực tiếp Repository)

---

## Layer 1: TRIGGERS (Entry Points)

### Trách Nhiệm

Triggers là entry points - nơi nhận requests từ bên ngoài:

✅ **Nên làm:**
- Validate input format (không rỗng, đúng type)
- Extract auth context (userId từ `request.auth`)
- Call service layer với clean data
- Handle errors và return response

❌ **Không được làm:**
- Business logic (calculations, validations)
- Database queries (Firestore operations)
- Call repositories trực tiếp

### Loại Triggers

#### A) Callable Functions (HTTP API)

**Vị trí:** `src/triggers/api.*.ts`

```typescript
// api.order.ts
import { onCall, CallableRequest } from "firebase-functions/v2/https";
import { PlaceOrderRequest, PlaceOrderResponse } from "../models";
import { orderService } from "../services";
import { toHttpsError } from "../utils";

export const placeOrder = onCall<PlaceOrderRequest, Promise<PlaceOrderResponse>>(
  async (request: CallableRequest<PlaceOrderRequest>) => {
    try {
      const { data, auth } = request;
      
      // 1. Validate input format
      if (!data.restaurantId) {
        throw new Error("restaurantId is required");
      }
      
      if (!data.items || data.items.length === 0) {
        throw new Error("items array cannot be empty");
      }
      
      // 2. Extract auth
      if (!auth?.uid) {
        throw new Error("Unauthenticated");
      }
      
      // 3. Call service
      const result = await orderService.placeOrder(data, auth.uid);
      
      return result;
    } catch (error) {
      throw toHttpsError(error);
    }
  }
);

export const cancelOrder = onCall<CancelOrderRequest, Promise<CancelOrderResponse>>(
  async (request: CallableRequest<CancelOrderRequest>) => {
    try {
      const { data, auth } = request;
      
      if (!data.orderId) throw new Error("orderId is required");
      if (!auth?.uid) throw new Error("Unauthenticated");
      
      return await orderService.cancelOrder(data.orderId, auth.uid);
    } catch (error) {
      throw toHttpsError(error);
    }
  }
);
```

**Pattern:**
```typescript
export const functionName = onCall<RequestType, Promise<ResponseType>>(
  async (request) => {
    try {
      // 1. Validate input
      // 2. Extract auth
      // 3. Call service
      // 4. Return result
    } catch (error) {
      throw toHttpsError(error);
    }
  }
);
```

#### B) Firestore Triggers

**Vị trí:** `src/triggers/*.trigger.ts`

```typescript
// order.trigger.ts
import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { Order } from "../models";
import { notificationService, orderService } from "../services";
import { logError } from "../utils";

export const onOrderCreated = onDocumentCreated(
  "orders/{orderId}",
  async (event) => {
    try {
      const orderId = event.params.orderId;
      const orderData = event.data?.data() as Order;
      
      if (!orderData) {
        console.warn(`Order ${orderId} has no data`);
        return;
      }
      
      // Trigger các handlers bất đồng bộ
      await Promise.allSettled([
        notificationService.sendToSeller(orderData.restaurantId, orderId),
        notificationService.sendToBuyer(orderData.userId, orderId),
        orderService.updateRestaurantStats(orderData.restaurantId)
      ]);
      
      console.log(`Order ${orderId} created handlers completed`);
    } catch (error) {
      logError("onOrderCreated", error);
    }
  }
);

export const onOrderStatusUpdated = onDocumentUpdated(
  "orders/{orderId}",
  async (event) => {
    try {
      const orderId = event.params.orderId;
      const beforeData = event.data?.before.data() as Order;
      const afterData = event.data?.after.data() as Order;
      
      // Chỉ xử lý khi status thay đổi
      if (beforeData.status === afterData.status) {
        return;
      }
      
      console.log(`Order ${orderId} status: ${beforeData.status} → ${afterData.status}`);
      
      // Notify status change
      await notificationService.sendStatusUpdate(afterData);
    } catch (error) {
      logError("onOrderStatusUpdated", error);
    }
  }
);
```

**Pattern:**
```typescript
export const onDocumentCreated("collection/{docId}", async (event) => {
  try {
    const docId = event.params.docId;
    const data = event.data?.data();
    
    // Call services (independent operations)
    await Promise.allSettled([
      service1.method(),
      service2.method()
    ]);
  } catch (error) {
    logError("triggerName", error);
  }
});
```

#### C) Auth Triggers

**Vị trí:** `src/triggers/auth.trigger.ts`

```typescript
// auth.trigger.ts
import { onUserCreated } from "firebase-functions/v2/identity";
import { userRepository } from "../repositories";
import { notificationService } from "../services";
import { logError } from "../utils";

export const onUserCreatedTrigger = onUserCreated(async (event) => {
  try {
    const user = event.data;
    
    // Tạo user document trong Firestore
    await userRepository.createUserDocument(user.uid, {
      email: user.email || "",
      displayName: user.displayName || "",
      photoURL: user.photoURL || "",
      role: "BUYER", // Default role
      createdAt: new Date().toISOString()
    });
    
    // Send welcome notification
    await notificationService.sendWelcomeMessage(user.uid);
    
    console.log(`User ${user.uid} created successfully`);
  } catch (error) {
    logError("onUserCreated", error);
  }
});
```

### Naming Conventions

| File Type | Pattern | Example |
|-----------|---------|---------|
| Callable functions | `api.*.ts` | `api.order.ts`, `api.promotion.ts` |
| Firestore triggers | `*.trigger.ts` | `order.trigger.ts`, `user.trigger.ts` |
| Auth triggers | `auth.trigger.ts` | `auth.trigger.ts` |
| Function names | `camelCase` | `placeOrder`, `cancelOrder` |

---

## Layer 2: SERVICES (Business Logic)

### Trách Nhiệm

Services chứa tất cả business logic:

✅ **Nên làm:**
- Validate business rules (restaurant open, stock available)
- Calculations (total price, discount)
- Orchestrate multiple repositories
- Call other services nếu cần
- Return structured data

❌ **Không được làm:**
- Parse HTTP request (trigger làm)
- Direct Firestore operations (repository làm)
- Handle auth extraction (trigger làm)

### Implementation Pattern

**Vị trí:** `src/services/*.service.ts`

```typescript
// order.service.ts
import { Order, OrderStatus, PlaceOrderRequest, PlaceOrderResponse } from "../models";
import { orderRepository, restaurantRepository, promotionRepository } from "../repositories";
import { notificationService } from "./notification.service";

export class OrderService {
  /**
   * Place a new order
   */
  async placeOrder(
    data: PlaceOrderRequest,
    userId: string
  ): Promise<PlaceOrderResponse> {
    // 1. Validate restaurant exists and is open
    const restaurant = await restaurantRepository.getById(data.restaurantId);
    if (!restaurant) {
      throw new Error("Restaurant not found");
    }
    if (!restaurant.isOpen) {
      throw new Error("Restaurant is currently closed");
    }
    
    // 2. Validate and fetch menu items
    let totalAmount = 0;
    for (const item of data.items) {
      const menuItem = await restaurantRepository.getMenuItem(
        data.restaurantId,
        item.menuItemId
      );
      
      if (!menuItem) {
        throw new Error(`Menu item ${item.menuItemId} not found`);
      }
      
      if (!menuItem.isAvailable) {
        throw new Error(`Menu item ${menuItem.name} is not available`);
      }
      
      totalAmount += menuItem.price * item.quantity;
    }
    
    // 3. Apply promotion if provided
    if (data.promotionCode) {
      const promotion = await promotionRepository.getByCode(data.promotionCode);
      
      if (promotion && promotion.isActive) {
        // Validate promotion conditions
        if (totalAmount < promotion.minOrderAmount) {
          throw new Error(
            `Minimum order amount for this promotion is ${promotion.minOrderAmount}`
          );
        }
        
        // Apply discount
        const discount = (totalAmount * promotion.discountPercent) / 100;
        totalAmount = totalAmount - discount;
      }
    }
    
    // 4. Create order
    const orderId = await orderRepository.create({
      userId,
      restaurantId: data.restaurantId,
      items: data.items,
      deliveryAddress: data.deliveryAddress || "",
      notes: data.notes || "",
      status: "PENDING",
      totalAmount,
      createdAt: new Date().toISOString()
    });
    
    // 5. Send notifications (non-blocking)
    notificationService.sendToSeller(data.restaurantId, orderId).catch((error) => {
      console.error("Failed to send seller notification:", error);
    });
    
    return {
      orderId,
      status: "PENDING",
      totalAmount
    };
  }
  
  /**
   * Cancel an order
   */
  async cancelOrder(orderId: string, userId: string): Promise<{ success: boolean }> {
    // 1. Get order
    const order = await orderRepository.getById(orderId);
    if (!order) {
      throw new Error("Order not found");
    }
    
    // 2. Check permission
    if (order.userId !== userId) {
      throw new Error("You do not have permission to cancel this order");
    }
    
    // 3. Check if order can be cancelled
    if (order.status !== "PENDING" && order.status !== "CONFIRMED") {
      throw new Error(`Cannot cancel order with status ${order.status}`);
    }
    
    // 4. Update status
    await orderRepository.updateStatus(orderId, "CANCELLED");
    
    // 5. Notify seller
    await notificationService.sendCancellationToSeller(order.restaurantId, orderId);
    
    return { success: true };
  }
  
  /**
   * Confirm order (seller action)
   */
  async confirmOrder(orderId: string, sellerId: string): Promise<{ success: boolean }> {
    // 1. Get order
    const order = await orderRepository.getById(orderId);
    if (!order) {
      throw new Error("Order not found");
    }
    
    // 2. Verify seller owns this restaurant
    const restaurant = await restaurantRepository.getById(order.restaurantId);
    if (restaurant.sellerId !== sellerId) {
      throw new Error("Permission denied");
    }
    
    // 3. Check order status
    if (order.status !== "PENDING") {
      throw new Error(`Cannot confirm order with status ${order.status}`);
    }
    
    // 4. Update status
    await orderRepository.updateStatus(orderId, "CONFIRMED");
    
    // 5. Notify buyer
    await notificationService.sendConfirmationToBuyer(order.userId, orderId);
    
    return { success: true };
  }
  
  /**
   * Update restaurant order statistics
   */
  async updateRestaurantStats(restaurantId: string): Promise<void> {
    const orders = await orderRepository.getByRestaurantId(restaurantId);
    const totalOrders = orders.length;
    const totalRevenue = orders.reduce((sum, order) => sum + order.totalAmount, 0);
    
    await restaurantRepository.updateStats(restaurantId, {
      totalOrders,
      totalRevenue
    });
  }
}

// Singleton export
export const orderService = new OrderService();
```

### Service Patterns

#### Pattern 1: Validation-Heavy Service

```typescript
export class PromotionService {
  async applyPromotion(promotionCode: string, orderAmount: number): Promise<number> {
    // Validate promotion exists
    const promotion = await promotionRepository.getByCode(promotionCode);
    if (!promotion) {
      throw new Error("Promotion not found");
    }
    
    // Validate active
    if (!promotion.isActive) {
      throw new Error("Promotion is not active");
    }
    
    // Validate dates
    const now = new Date();
    if (now < new Date(promotion.startDate)) {
      throw new Error("Promotion has not started yet");
    }
    if (now > new Date(promotion.endDate)) {
      throw new Error("Promotion has expired");
    }
    
    // Validate min order amount
    if (orderAmount < promotion.minOrderAmount) {
      throw new Error(`Minimum order amount is ${promotion.minOrderAmount}`);
    }
    
    // Calculate discount
    const discount = (orderAmount * promotion.discountPercent) / 100;
    return orderAmount - discount;
  }
}
```

#### Pattern 2: Orchestration Service

```typescript
export class NotificationService {
  async sendToSeller(restaurantId: string, orderId: string): Promise<void> {
    // Get restaurant to get seller info
    const restaurant = await restaurantRepository.getById(restaurantId);
    if (!restaurant) return;
    
    // Get order details
    const order = await orderRepository.getById(orderId);
    if (!order) return;
    
    // Send notification
    await this.sendPushNotification(restaurant.sellerId, {
      title: "New Order",
      body: `You have a new order #${orderId.substring(0, 8)}`,
      data: { orderId, type: "NEW_ORDER" }
    });
  }
  
  private async sendPushNotification(userId: string, payload: NotificationPayload) {
    // FCM implementation
  }
}
```

---

## Layer 3: REPOSITORIES (Data Access)

### Trách Nhiệm

Repositories chịu trách nhiệm tất cả tương tác với Firestore:

✅ **Nên làm:**
- CRUD operations (Create, Read, Update, Delete)
- Query & filter data
- Batch operations
- Transaction operations
- Return typed data (using Models)

❌ **Không được làm:**
- Business logic (calculations, validations)
- Call other repositories (nếu cần orchestration, làm ở service)
- Handle errors domain-specific (throw generic errors)

### Implementation Pattern

**Vị trí:** `src/repositories/*.repository.ts`

```typescript
// order.repository.ts
import * as admin from "firebase-admin";
import { Order, OrderStatus } from "../models";

export class OrderRepository {
  private db = admin.firestore();
  private collection = "orders";
  
  /**
   * Create a new order
   */
  async create(data: Omit<Order, "id">): Promise<string> {
    const docRef = await this.db.collection(this.collection).add({
      ...data,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    return docRef.id;
  }
  
  /**
   * Get order by ID
   */
  async getById(orderId: string): Promise<Order | null> {
    const doc = await this.db.collection(this.collection).doc(orderId).get();
    
    if (!doc.exists) {
      return null;
    }
    
    return {
      id: doc.id,
      ...doc.data()
    } as Order;
  }
  
  /**
   * Get orders by user ID
   */
  async getByUserId(userId: string, limit = 20): Promise<Order[]> {
    const snapshot = await this.db
      .collection(this.collection)
      .where("userId", "==", userId)
      .orderBy("createdAt", "desc")
      .limit(limit)
      .get();
    
    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data()
    })) as Order[];
  }
  
  /**
   * Get orders by restaurant ID
   */
  async getByRestaurantId(restaurantId: string, limit = 50): Promise<Order[]> {
    const snapshot = await this.db
      .collection(this.collection)
      .where("restaurantId", "==", restaurantId)
      .orderBy("createdAt", "desc")
      .limit(limit)
      .get();
    
    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data()
    })) as Order[];
  }
  
  /**
   * Update order status
   */
  async updateStatus(orderId: string, status: OrderStatus): Promise<void> {
    await this.db.collection(this.collection).doc(orderId).update({
      status,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
  }
  
  /**
   * Update order (partial)
   */
  async update(orderId: string, data: Partial<Order>): Promise<void> {
    await this.db.collection(this.collection).doc(orderId).update({
      ...data,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
  }
  
  /**
   * Delete order
   */
  async delete(orderId: string): Promise<void> {
    await this.db.collection(this.collection).doc(orderId).delete();
  }
  
  /**
   * Get orders by status
   */
  async getByStatus(status: OrderStatus, limit = 100): Promise<Order[]> {
    const snapshot = await this.db
      .collection(this.collection)
      .where("status", "==", status)
      .orderBy("createdAt", "desc")
      .limit(limit)
      .get();
    
    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data()
    })) as Order[];
  }
  
  /**
   * Batch update orders
   */
  async batchUpdateStatus(orderIds: string[], status: OrderStatus): Promise<void> {
    const batch = this.db.batch();
    
    orderIds.forEach((orderId) => {
      const docRef = this.db.collection(this.collection).doc(orderId);
      batch.update(docRef, {
        status,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    });
    
    await batch.commit();
  }
}

// Singleton export
export const orderRepository = new OrderRepository();
```

### Repository Patterns

#### Pattern 1: Standard CRUD

```typescript
export class RestaurantRepository {
  private db = admin.firestore();
  private collection = "restaurants";
  
  async create(data: Omit<Restaurant, "id">): Promise<string> {
    const docRef = await this.db.collection(this.collection).add(data);
    return docRef.id;
  }
  
  async getById(id: string): Promise<Restaurant | null> {
    const doc = await this.db.collection(this.collection).doc(id).get();
    return doc.exists ? ({ id: doc.id, ...doc.data() } as Restaurant) : null;
  }
  
  async update(id: string, data: Partial<Restaurant>): Promise<void> {
    await this.db.collection(this.collection).doc(id).update(data);
  }
  
  async delete(id: string): Promise<void> {
    await this.db.collection(this.collection).doc(id).delete();
  }
}
```

#### Pattern 2: Complex Queries

```typescript
export class PromotionRepository {
  async getActivePromotions(): Promise<Promotion[]> {
    const now = admin.firestore.Timestamp.now();
    
    const snapshot = await this.db
      .collection(this.collection)
      .where("isActive", "==", true)
      .where("startDate", "<=", now)
      .where("endDate", ">=", now)
      .get();
    
    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data()
    })) as Promotion[];
  }
  
  async getByCode(code: string): Promise<Promotion | null> {
    const snapshot = await this.db
      .collection(this.collection)
      .where("code", "==", code.toUpperCase())
      .limit(1)
      .get();
    
    if (snapshot.empty) return null;
    
    const doc = snapshot.docs[0];
    return { id: doc.id, ...doc.data() } as Promotion;
  }
}
```

#### Pattern 3: Transactions

```typescript
export class OrderRepository {
  async transferOrderToShipper(orderId: string, shipperId: string): Promise<void> {
    await this.db.runTransaction(async (transaction) => {
      const orderRef = this.db.collection("orders").doc(orderId);
      const orderDoc = await transaction.get(orderRef);
      
      if (!orderDoc.exists) {
        throw new Error("Order not found");
      }
      
      const order = orderDoc.data() as Order;
      
      if (order.status !== "CONFIRMED") {
        throw new Error("Order must be confirmed before assigning shipper");
      }
      
      // Update order
      transaction.update(orderRef, {
        shipperId,
        status: "DELIVERING",
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      
      // Update shipper stats
      const shipperRef = this.db.collection("users").doc(shipperId);
      transaction.update(shipperRef, {
        activeOrders: admin.firestore.FieldValue.increment(1)
      });
    });
  }
}
```

---

## Layer 4: MODELS (Type Definitions)

### Trách Nhiệm

Models định nghĩa TypeScript types cho toàn bộ application:

✅ **Nên làm:**
- Define interfaces cho entities (Order, User, Restaurant)
- Define enums/types cho constants (OrderStatus, UserRole)
- Define Request/Response types cho APIs
- Document types với JSDoc comments

❌ **Không được làm:**
- Logic, functions, calculations
- Import services, repositories (models phải độc lập)

### Implementation Pattern

**Vị trí:** `src/models/*.model.ts`

```typescript
// order.model.ts

/**
 * Order status enum
 */
export type OrderStatus =
  | "PENDING"       // Order created, waiting for seller confirmation
  | "CONFIRMED"     // Seller confirmed order
  | "PREPARING"     // Restaurant preparing food
  | "READY"         // Food ready for pickup
  | "DELIVERING"    // Shipper is delivering
  | "COMPLETED"     // Order delivered successfully
  | "CANCELLED";    // Order cancelled

/**
 * Order item in cart
 */
export interface OrderItem {
  menuItemId: string;
  quantity: number;
  unitPrice: number;
  notes?: string;
}

/**
 * Order entity
 */
export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  items: OrderItem[];
  deliveryAddress: string;
  notes?: string;
  status: OrderStatus;
  totalAmount: number;
  shipperId?: string;
  createdAt: string;
  updatedAt?: string;
  confirmedAt?: string;
  completedAt?: string;
}

/**
 * Request to place a new order
 */
export interface PlaceOrderRequest {
  restaurantId: string;
  items: Array<{
    menuItemId: string;
    quantity: number;
    notes?: string;
  }>;
  deliveryAddress?: string;
  notes?: string;
  promotionCode?: string;
}

/**
 * Response after placing order
 */
export interface PlaceOrderResponse {
  orderId: string;
  status: OrderStatus;
  totalAmount: number;
}

/**
 * Request to cancel order
 */
export interface CancelOrderRequest {
  orderId: string;
  reason?: string;
}

/**
 * Response after cancelling order
 */
export interface CancelOrderResponse {
  success: boolean;
  message?: string;
}
```

```typescript
// user.model.ts

/**
 * User roles
 */
export type UserRole = "BUYER" | "SELLER" | "SHIPPER" | "ADMIN";

/**
 * User entity
 */
export interface User {
  id: string;
  email: string;
  displayName: string;
  photoURL?: string;
  phoneNumber?: string;
  role: UserRole;
  createdAt: string;
  updatedAt?: string;
}

/**
 * User profile (public info)
 */
export interface UserProfile {
  id: string;
  displayName: string;
  photoURL?: string;
  role: UserRole;
}
```

```typescript
// restaurant.model.ts

/**
 * Restaurant entity
 */
export interface Restaurant {
  id: string;
  sellerId: string;
  name: string;
  description: string;
  address: string;
  phoneNumber: string;
  imageURL: string;
  isOpen: boolean;
  rating: number;
  totalOrders: number;
  totalRevenue: number;
  createdAt: string;
  updatedAt?: string;
}

/**
 * Menu item
 */
export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  imageURL: string;
  category: string;
  isAvailable: boolean;
  preparationTime: number; // minutes
}
```

### Naming Conventions

| Type | Suffix | Example |
|------|--------|---------|
| Request types | `Request` | `PlaceOrderRequest` |
| Response types | `Response` | `PlaceOrderResponse` |
| Entity types | (none) | `Order`, `User`, `Restaurant` |
| Enum types | (none) | `OrderStatus`, `UserRole` |

---

## Layer 5: UTILS (Helpers)

### Trách Nhiệm

Utils cung cấp helper functions được dùng chung:

✅ **Nên làm:**
- Error handling & mapping
- Input validation functions
- Logging utilities
- Format/parse helpers
- Pure functions (no side effects)

❌ **Không được làm:**
- Business logic
- Database operations
- Depend on services/repositories

### Implementation Pattern

**Vị trí:** `src/utils/*.utils.ts`

```typescript
// error.utils.ts
import { HttpsError } from "firebase-functions/v2/https";

/**
 * Convert any error to HttpsError for consistent API responses
 */
export function toHttpsError(error: any): HttpsError {
  // Already HttpsError
  if (error instanceof HttpsError) {
    return error;
  }
  
  const message = error.message || "An unexpected error occurred";
  
  // Map common error patterns to HttpsError codes
  if (message.includes("not found")) {
    return new HttpsError("not-found", message);
  }
  
  if (message.includes("Unauthenticated") || message.includes("auth")) {
    return new HttpsError("unauthenticated", message);
  }
  
  if (message.includes("Permission denied") || message.includes("permission")) {
    return new HttpsError("permission-denied", message);
  }
  
  if (message.includes("required") || message.includes("invalid") || message.includes("empty")) {
    return new HttpsError("invalid-argument", message);
  }
  
  if (message.includes("already exists") || message.includes("duplicate")) {
    return new HttpsError("already-exists", message);
  }
  
  if (message.includes("unavailable") || message.includes("closed")) {
    return new HttpsError("unavailable", message);
  }
  
  // Default to internal error
  return new HttpsError("internal", message);
}

/**
 * Log error with context
 */
export function logError(context: string, error: any): void {
  console.error(`[${context}] Error:`, {
    message: error.message || error,
    stack: error.stack,
    timestamp: new Date().toISOString()
  });
}
```

```typescript
// validation.utils.ts

/**
 * Check if value is not empty
 */
export function isNotEmpty(value: any): boolean {
  return value !== null && value !== undefined && value !== "";
}

/**
 * Check if array is not empty
 */
export function isNonEmptyArray(value: any): boolean {
  return Array.isArray(value) && value.length > 0;
}

/**
 * Validate email format
 */
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate Vietnamese phone number
 */
export function isValidPhoneNumber(phone: string): boolean {
  const phoneRegex = /^(\+84|0)\d{9,10}$/;
  return phoneRegex.test(phone);
}

/**
 * Validate positive number
 */
export function isPositiveNumber(value: number): boolean {
  return typeof value === "number" && value > 0 && !isNaN(value);
}

/**
 * Validate order amount (min/max)
 */
export function isValidOrderAmount(amount: number): boolean {
  const MIN_ORDER = 10000;  // 10,000 VND
  const MAX_ORDER = 10000000; // 10,000,000 VND
  return isPositiveNumber(amount) && amount >= MIN_ORDER && amount <= MAX_ORDER;
}
```

---

## Data Flow Examples

### Example 1: Đặt Hàng (Place Order)

```
┌─────────────┐
│   CLIENT    │
│  (Mobile)   │
└──────┬──────┘
       │ placeOrder({ restaurantId, items })
       ▼
┌────────────────────────────────────────────┐
│ TRIGGER: api.order.ts - placeOrder()       │
│ • Validate restaurantId not empty          │
│ • Validate items array not empty           │
│ • Extract userId from auth.uid             │
│ • Call orderService.placeOrder()           │
└────────────────┬───────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────┐
│ SERVICE: order.service.ts                  │
│ • Get restaurant (via restaurantRepo)      │
│ • Validate restaurant.isOpen               │
│ • Get menu items (via restaurantRepo)      │
│ • Calculate totalAmount                    │
│ • Apply promotion (via promotionRepo)      │
│ • Create order (via orderRepo)             │
│ • Send notification (via notificationSvc)  │
└────────────────┬───────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────┐
│ REPOSITORY: order.repository.ts            │
│ • db.collection("orders").add(data)        │
│ • Return orderId                           │
└────────────────┬───────────────────────────┘
                 │
                 ▼
             FIRESTORE
           (orders/{orderId})
                 │
                 │ Document created event
                 ▼
┌────────────────────────────────────────────┐
│ TRIGGER: order.trigger.ts                  │
│ • onOrderCreated fires automatically       │
│ • Send notifications                       │
│ • Update restaurant stats                  │
│ • Log analytics                            │
└────────────────────────────────────────────┘
```

### Example 2: Xác Nhận Đơn Hàng (Confirm Order)

```
┌─────────────┐
│   SELLER    │
│ (Mobile App)│
└──────┬──────┘
       │ confirmOrder({ orderId })
       ▼
┌────────────────────────────────────────────┐
│ TRIGGER: api.order.ts - confirmOrder()     │
│ • Validate orderId                         │
│ • Extract sellerId from auth.uid           │
│ • Call orderService.confirmOrder()         │
└────────────────┬───────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────┐
│ SERVICE: order.service.ts                  │
│ • Get order (via orderRepo)                │
│ • Get restaurant (via restaurantRepo)      │
│ • Validate restaurant.sellerId == sellerId │
│ • Validate order.status == "PENDING"       │
│ • Update status = "CONFIRMED"              │
│ • Notify buyer (via notificationService)   │
└────────────────┬───────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────┐
│ REPOSITORY: order.repository.ts            │
│ • db.collection("orders")                  │
│     .doc(orderId)                          │
│     .update({ status: "CONFIRMED" })       │
└────────────────┬───────────────────────────┘
                 │
                 ▼
             FIRESTORE
         (orders/{orderId})
         status: PENDING → CONFIRMED
                 │
                 │ Document updated event
                 ▼
┌────────────────────────────────────────────┐
│ TRIGGER: order.trigger.ts                  │
│ • onOrderStatusUpdated fires               │
│ • Detect status change                     │
│ • Send status update notification          │
│ • Log event to analytics                   │
└────────────────────────────────────────────┘
```

---

## Best Practices & Conventions

### 1. Naming Conventions

```typescript
// Files
api.order.ts           // ✅ Callable functions
order.trigger.ts       // ✅ Firestore triggers
order.service.ts       // ✅ Services
order.repository.ts    // ✅ Repositories
order.model.ts         // ✅ Models

// Classes
export class OrderService { }        // ✅ PascalCase
export class OrderRepository { }     // ✅ PascalCase

// Functions
export const placeOrder = ...        // ✅ camelCase
export const cancelOrder = ...       // ✅ camelCase

// Interfaces
export interface Order { }           // ✅ PascalCase
export interface PlaceOrderRequest { } // ✅ PascalCase with suffix

// Types
export type OrderStatus = ...        // ✅ PascalCase
export type UserRole = ...           // ✅ PascalCase

// Constants
const MAX_ITEMS = 100;               // ✅ UPPER_CASE
const MIN_ORDER_AMOUNT = 10000;      // ✅ UPPER_CASE

// Singleton exports
export const orderService = new OrderService();  // ✅ camelCase instance
```

### 2. Error Handling

```typescript
// ✅ GOOD - Specific errors
if (!restaurant) {
  throw new Error("Restaurant not found");
}

if (!restaurant.isOpen) {
  throw new Error("Restaurant is currently closed");
}

// ❌ BAD - Generic errors
if (!restaurant || !restaurant.isOpen) {
  throw new Error("Error");
}
```

### 3. Validation

```typescript
// ✅ GOOD - Validate early in trigger
export const placeOrder = onCall(async (request) => {
  if (!request.data.restaurantId) throw new Error("restaurantId required");
  if (!request.data.items?.length) throw new Error("items empty");
  
  return await orderService.placeOrder(request.data, request.auth.uid);
});

// ❌ BAD - Validate sau
export const placeOrder = onCall(async (request) => {
  return await orderService.placeOrder(request.data, request.auth.uid);
  // Service phải validate → logic không rõ ràng
});
```

### 4. Comments & Documentation

```typescript
// ✅ GOOD - JSDoc for public methods
/**
 * Place a new order
 * @param data - Order details
 * @param userId - User placing the order
 * @returns Order ID and status
 * @throws Error if restaurant not found or closed
 */
async placeOrder(data: PlaceOrderRequest, userId: string): Promise<PlaceOrderResponse> {
  // ...
}

// ✅ GOOD - Inline comments for complex logic
// Apply discount based on promotion type
if (promotion.type === "PERCENT") {
  discount = (totalAmount * promotion.value) / 100;
} else {
  discount = promotion.value; // Fixed amount
}

// ❌ BAD - No comments
async placeOrder(data, userId) {
  const r = await this.repo.get(data.rid);
  if (!r.o) throw new Error("e1");
  // WTF is r.o?
}
```

### 5. Async/Await Best Practices

```typescript
// ✅ GOOD - Parallel independent operations
const [restaurant, promotion] = await Promise.all([
  restaurantRepository.getById(data.restaurantId),
  promotionRepository.getByCode(data.promotionCode)
]);

// ❌ BAD - Sequential when not needed
const restaurant = await restaurantRepository.getById(data.restaurantId);
const promotion = await promotionRepository.getByCode(data.promotionCode);
// Chậm gấp đôi!

// ✅ GOOD - Promise.allSettled for non-critical operations
await Promise.allSettled([
  notificationService.sendToSeller(orderId),
  notificationService.sendToBuyer(orderId),
  analyticsService.logEvent("order_created")
]);
// Notification fails không ảnh hưởng main flow

// ❌ BAD - Blocking on non-critical operations
await notificationService.sendToSeller(orderId);
await notificationService.sendToBuyer(orderId);
// Nếu notification fail → toàn bộ flow fail
```

---

## Testing Strategy

### Unit Tests

```typescript
// Test Service (mock repositories)
describe('OrderService.placeOrder', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  it('should throw error if restaurant not found', async () => {
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue(null);
    
    await expect(
      orderService.placeOrder(mockData, 'user_1')
    ).rejects.toThrow('Restaurant not found');
  });
  
  it('should throw error if restaurant is closed', async () => {
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue({
      id: 'rest_1',
      isOpen: false
    });
    
    await expect(
      orderService.placeOrder(mockData, 'user_1')
    ).rejects.toThrow('Restaurant is currently closed');
  });
  
  it('should calculate total correctly', async () => {
    jest.spyOn(restaurantRepository, 'getById').mockResolvedValue(mockRestaurant);
    jest.spyOn(restaurantRepository, 'getMenuItem').mockResolvedValue({
      id: 'item_1',
      price: 50000
    });
    jest.spyOn(orderRepository, 'create').mockResolvedValue('order_123');
    
    const result = await orderService.placeOrder({
      restaurantId: 'rest_1',
      items: [{ menuItemId: 'item_1', quantity: 2 }]
    }, 'user_1');
    
    expect(result.totalAmount).toBe(100000);
  });
});
```

### Integration Tests

```typescript
// Test with Firebase Emulator
describe('Order Flow Integration', () => {
  it('should create order and trigger notifications', async () => {
    // Call trigger
    const result = await placeOrder({
      data: {
        restaurantId: 'rest_1',
        items: [{ menuItemId: 'item_1', quantity: 2 }]
      },
      auth: { uid: 'user_1' }
    });
    
    // Verify order created
    const order = await db.collection('orders').doc(result.orderId).get();
    expect(order.exists).toBe(true);
    expect(order.data().status).toBe('PENDING');
    
    // Wait for trigger to fire
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Verify notification sent
    // ...
  });
});
```

---

## Tài Liệu Liên Quan

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc backend
- [ADR/](./ADR/) - Architecture Decision Records
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Error handling chi tiết
- [EVENTS.md](./EVENTS.md) - Event-driven architecture
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Hướng dẫn phát triển

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
