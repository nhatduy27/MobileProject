# Quy Ước Coding cho Backend

> **Tài liệu này định nghĩa các quy ước coding, best practices, và conventions cho Firebase Functions backend.**

---

## 📖 Mục Lục

- [Service Template](#-service-template)
- [Repository Template](#-repository-template)
- [Best Practices](#-best-practices)
- [Logging Conventions](#-logging-conventions)
- [Commit Message Convention](#-commit-message-convention)
- [Code Review Checklist](#-code-review-checklist)
- [Workflow Làm Việc Nhóm](#-workflow-làm-việc-nhóm)

---

## 📝 Service Template

### Template cho Service mới

```typescript
/**
 * [Entity] Service
 *
 * Business logic cho [Entity]
 */

import { CallableRequest } from "firebase-functions/v2/https";
import { [Entity], [EntityRequest], [EntityResponse] } from "../models/[entity].model";
import { [entity]Repository } from "../repositories/[entity].repository";

type CallableRequestContext = CallableRequest["auth"];

export class [Entity]Service {
  /**
   * [Action]
   *
   * @param data Request payload
   * @param context Auth context
   */
  async [action](
    data: [EntityRequest],
    context: CallableRequestContext
  ): Promise<[EntityResponse]> {
    // 1. Validate auth
    if (!context?.uid) {
      throw new Error("Unauthenticated");
    }

    // 2. Validate input
    // 3. Fetch related data
    // 4. Apply business logic
    // 5. Call repository to save
    // 6. Return response
  }
}

export const [entity]Service = new [Entity]Service();
```

### Service Best Practices

✅ **DO:**
- Validate input đầu tiên
- Check authentication & authorization
- Use repositories để access data
- Throw descriptive errors
- Return type-safe responses
- Keep functions focused (single responsibility)
- Document complex logic with comments

❌ **DON'T:**
- Direct Firestore access (dùng repositories)
- Mix multiple concerns (e.g., payment + shipping)
- Ignore errors
- Return raw Firebase objects
- Put too much logic in one function

**Example - Good Service:**

```typescript
export class OrderService {
  /**
   * Place a new order
   */
  async placeOrder(
    data: PlaceOrderRequest,
    context: CallableRequestContext
  ): Promise<PlaceOrderResponse> {
    // 1. Validate auth
    if (!context?.uid) {
      throw new Error("Unauthenticated");
    }

    // 2. Validate input
    if (!data.restaurantId || !data.items || data.items.length === 0) {
      throw new Error("Invalid order data");
    }

    // 3. Fetch restaurant
    const restaurant = await restaurantRepository.getById(data.restaurantId);
    if (!restaurant || !restaurant.isActive) {
      throw new Error("Restaurant not available");
    }

    // 4. Calculate total
    const totalAmount = this.calculateTotal(data.items);

    // 5. Create order
    const orderId = await orderRepository.create({
      userId: context.uid,
      restaurantId: data.restaurantId,
      items: data.items,
      totalAmount,
      status: "PENDING",
    });

    // 6. Send notification
    await notificationService.sendOrderConfirmation(context.uid, orderId);

    return { orderId, totalAmount };
  }

  private calculateTotal(items: OrderItem[]): number {
    return items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }
}
```

---

## 📝 Repository Template

### Template cho Repository mới

```typescript
/**
 * [Entity] Repository
 *
 * Data access layer cho [Entity]
 */

import * as admin from "firebase-admin";
import { [Entity] } from "../models/[entity].model";

export class [Entity]Repository {
  private db = admin.firestore();
  private collection = "[entities]";

  async create(data: Omit<[Entity], "id">): Promise<string> {
    const docRef = await this.db.collection(this.collection).add({
      ...data,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return docRef.id;
  }

  async getById(id: string): Promise<[Entity] | null> {
    const doc = await this.db.collection(this.collection).doc(id).get();
    if (!doc.exists) return null;
    return { id: doc.id, ...doc.data() } as [Entity];
  }

  async getBy[Field](value: any): Promise<[Entity][]> {
    const snapshot = await this.db
      .collection(this.collection)
      .where("[field]", "==", value)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as [Entity][];
  }

  async update(id: string, data: Partial<[Entity]>): Promise<void> {
    await this.db.collection(this.collection).doc(id).update({
      ...data,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }

  async delete(id: string): Promise<void> {
    await this.db.collection(this.collection).doc(id).delete();
  }
}

export const [entity]Repository = new [Entity]Repository();
```

### Repository Best Practices

✅ **DO:**
- Use admin SDK methods
- Always handle timestamps with `serverTimestamp()`
- Validate document exists before operations
- Use proper queries with indexes
- Return Promise<type> or null
- Use transactions for multi-document updates
- Cache frequently accessed data

❌ **DON'T:**
- Business logic in repository
- Direct document manipulation
- Client timestamp usage
- Unindexed queries
- Ignore null/undefined checks

**Example - Good Repository:**

```typescript
export class OrderRepository {
  private db = admin.firestore();
  private collection = "orders";

  /**
   * Create new order
   */
  async create(data: Omit<Order, "id">): Promise<string> {
    const docRef = await this.db.collection(this.collection).add({
      ...data,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return docRef.id;
  }

  /**
   * Get order by ID
   */
  async getById(orderId: string): Promise<Order | null> {
    const doc = await this.db.collection(this.collection).doc(orderId).get();
    if (!doc.exists) return null;
    return { id: doc.id, ...doc.data() } as Order;
  }

  /**
   * Get user orders with pagination
   */
  async getUserOrders(
    userId: string,
    limit: number = 20,
    startAfter?: string
  ): Promise<Order[]> {
    let query = this.db
      .collection(this.collection)
      .where("userId", "==", userId)
      .orderBy("createdAt", "desc")
      .limit(limit);

    if (startAfter) {
      const startDoc = await this.db
        .collection(this.collection)
        .doc(startAfter)
        .get();
      query = query.startAfter(startDoc);
    }

    const snapshot = await query.get();
    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as Order[];
  }

  /**
   * Update order status (transactional)
   */
  async updateStatus(orderId: string, status: OrderStatus): Promise<void> {
    const docRef = this.db.collection(this.collection).doc(orderId);
    
    await this.db.runTransaction(async (transaction) => {
      const doc = await transaction.get(docRef);
      if (!doc.exists) {
        throw new Error("Order not found");
      }
      
      transaction.update(docRef, {
        status,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    });
  }
}
```

---

## ✅ Best Practices

### Naming Conventions

**Files:**
```
✅ GOOD:
order.service.ts
user.repository.ts
payment.model.ts
api.order.ts

❌ BAD:
OrderService.ts
UserRepo.ts
PaymentModel.ts
apiOrder.ts
```

**Classes:**
```typescript
✅ GOOD:
class OrderService { }
class UserRepository { }
interface Payment { }

❌ BAD:
class orderService { }
class user_repository { }
interface payment { }
```

**Functions:**
```typescript
✅ GOOD:
async placeOrder() { }
async getUserProfile() { }
calculateTotalAmount()

❌ BAD:
async PlaceOrder() { }
async get_user_profile() { }
CalcTotalAmount()
```

**Variables:**
```typescript
✅ GOOD:
const userId = "123";
const orderItems = [];
const isActive = true;

❌ BAD:
const UserId = "123";
const order_items = [];
const is_active = true;
```

### File Structure

```typescript
// ✅ GOOD - Organized imports
import * as admin from "firebase-admin";
import { onCall } from "firebase-functions/v2/https";
import { logger } from "firebase-functions/v2";

// Models
import { Order, PlaceOrderRequest } from "../models/order.model";

// Repositories
import { orderRepository } from "../repositories/order.repository";

// Services
import { orderService } from "../services/order.service";

// Utils
import { toHttpsError, logError } from "../utils/error.utils";
import { isNotEmpty } from "../utils/validation.utils";

// ❌ BAD - Random order, mixed imports
import { orderService } from "../services/order.service";
import * as admin from "firebase-admin";
import { isNotEmpty } from "../utils/validation.utils";
import { Order } from "../models/order.model";
```

### TypeScript Types

```typescript
// ✅ GOOD - Explicit types
async function getOrder(orderId: string): Promise<Order | null> {
  // ...
}

const calculateTotal = (items: OrderItem[]): number => {
  return items.reduce((sum, item) => sum + item.price * item.quantity, 0);
};

// ❌ BAD - Any types
async function getOrder(orderId: any): Promise<any> {
  // ...
}

const calculateTotal = (items: any) => {
  return items.reduce((sum: any, item: any) => sum + item.price * item.quantity, 0);
};
```

### Error Handling

```typescript
// ✅ GOOD - Specific errors with context
if (!user) {
  throw new Error(`User not found with id: ${userId}`);
}

if (order.status !== "PENDING") {
  throw new Error(`Cannot cancel order with status: ${order.status}`);
}

// ❌ BAD - Generic errors
if (!user) {
  throw new Error("Error");
}

if (order.status !== "PENDING") {
  throw new Error("Invalid status");
}
```

### Comments

```typescript
// ✅ GOOD - Meaningful comments
/**
 * Calculate order total including tax and delivery fee
 * 
 * @param items - Order items
 * @param deliveryFee - Delivery fee in VND
 * @returns Total amount in VND
 */
function calculateTotal(items: OrderItem[], deliveryFee: number): number {
  const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const tax = subtotal * 0.1; // 10% VAT
  return subtotal + tax + deliveryFee;
}

// ❌ BAD - Obvious comments
// Get user
const user = await userRepository.getById(userId);

// Add 1 to count
count = count + 1;
```

---

## 📊 Logging Conventions

### Log Levels

```typescript
import { logger } from "firebase-functions/v2";

// INFO - Thông tin quan trọng
logger.info("User created", { userId });

// WARN - Cảnh báo
logger.warn("Order processing slow", { processingTime: "5000ms" });

// ERROR - Lỗi
logger.error("Payment failed", { error: error.message, orderId });

// DEBUG - Chi tiết (chỉ development)
logger.debug("Processing items", { items });
```

### Structured Logging

```typescript
// ✅ GOOD - Structured, searchable logs
logger.info("Order placed", {
  orderId: order.id,
  userId: context.uid,
  restaurantId: order.restaurantId,
  totalAmount: order.totalAmount,
  itemCount: order.items.length,
  timestamp: Date.now(),
});

// ❌ BAD - Unstructured text
console.log("Order placed: " + order.id);
console.log("User: " + context.uid);
```

### Log Prefixes

```typescript
// ✅ GOOD - Clear function context
logger.info("[placeOrder] Validating order", { orderId });
logger.info("[placeOrder] Processing payment", { amount });
logger.info("[placeOrder] Order completed", { orderId });

// ❌ BAD - No context
logger.info("Validating order");
logger.info("Processing payment");
logger.info("Order completed");
```

---

## 📝 Commit Message Convention

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, missing semicolons, etc.)
- `refactor` - Code refactoring
- `test` - Adding tests
- `chore` - Maintenance tasks (deps, build, etc.)

### Examples

```bash
# Feature
git commit -m "feat(order): Add order cancellation feature"

# Bug fix
git commit -m "fix(payment): Fix payment status update race condition"

# Documentation
git commit -m "docs(readme): Update installation guide"

# Refactor
git commit -m "refactor(service): Simplify order validation logic"

# Test
git commit -m "test(order): Add unit tests for order service"

# Chore
git commit -m "chore(deps): Update Firebase SDK to v10.7.0"
```

### Best Practices

✅ **DO:**
- Use present tense ("Add feature" not "Added feature")
- Keep subject line under 50 characters
- Capitalize first letter
- No period at the end
- Add body for complex changes

❌ **DON'T:**
- Use vague messages ("Fix bug", "Update code")
- Mix multiple unrelated changes
- Include WIP commits in main branch

---

## 👥 Code Review Checklist

### For Reviewers

```markdown
#### Code Quality
- [ ] Code follows naming conventions
- [ ] TypeScript types are correct and specific
- [ ] No `any` types unless absolutely necessary
- [ ] Functions have clear single responsibility
- [ ] Complex logic has comments

#### Error Handling
- [ ] All errors are handled properly
- [ ] Error messages are descriptive
- [ ] No console.log() left in production code
- [ ] Uses logger with appropriate levels

#### Testing
- [ ] Code has been tested locally
- [ ] Functions tested in emulator
- [ ] Edge cases considered
- [ ] No regression in existing features

#### Security
- [ ] Authentication checks present
- [ ] Authorization rules correct
- [ ] No sensitive data in logs
- [ ] Input validation present

#### Performance
- [ ] No unnecessary database calls
- [ ] Proper use of indexes
- [ ] Efficient queries (no N+1 problems)
- [ ] Appropriate use of pagination

#### Documentation
- [ ] Functions have JSDoc comments
- [ ] Complex logic explained
- [ ] README updated if needed
- [ ] API documentation updated
```

---

## 👥 Workflow Làm Việc Nhóm

### 1. Feature Branch Workflow

```bash
# Tạo branch từ test
git checkout test
git pull origin test

# Tạo feature branch
git checkout -b feature/payment-integration

# Code & commit
git add .
git commit -m "feat(payment): Add payment service and triggers"

# Push
git push origin feature/payment-integration
```

### 2. Pull Request Process

1. **Tạo PR trên GitHub/GitLab**
   - Clear title và description
   - Link related issues
   - Add screenshots/videos nếu cần

2. **Code Review**
   - Assign reviewer
   - Address comments
   - Push changes

3. **Testing**
   - Ensure CI/CD passes
   - Test manually if needed

4. **Merge**
   - Merge vào `test` branch
   - Delete feature branch

### 3. Testing trước Deploy

```bash
# 1. Build
npm run build

# 2. Lint
npm run lint

# 3. Test trong emulator
npm run dev
# Test functions manually

# 4. Deploy to staging
firebase use staging
firebase deploy --only functions
```

---

## 📚 Tài Liệu Liên Quan

- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Hướng dẫn phát triển chính
- [LAYERED_ARCHITECTURE.md](./LAYERED_ARCHITECTURE.md) - Chi tiết kiến trúc phân lớp
- [TypeScript Style Guide](https://google.github.io/styleguide/tsguide.html)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
