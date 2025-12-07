# Firestore Composite Indexes

> **Tài liệu này mô tả các composite indexes bắt buộc cho hệ thống Food Delivery backend.**

---

## 📖 Mục Lục

- [Tại Sao Cần Composite Indexes](#-tại-sao-cần-composite-indexes)
- [Danh Sách Indexes Bắt Buộc](#-danh-sách-indexes-bắt-buộc)
- [Index Configuration JSON](#-index-configuration-json)
- [Cách Tạo Indexes](#-cách-tạo-indexes)
- [Troubleshooting](#-troubleshooting)
- [Best Practices](#-best-practices)

---

## 🔍 Tại Sao Cần Composite Indexes

### Firestore Query Limitations

Firestore **KHÔNG hỗ trợ** queries với nhiều fields hoặc kết hợp order by mà không có composite index.

**Ví dụ queries cần index:**

❌ **Query này SẼ FAIL nếu không có index:**
```typescript
// Query: Lấy orders của user, sort theo createdAt
ordersRef
  .where("userId", "==", "user_123")
  .orderBy("createdAt", "desc")
  .limit(20)

// Error: FAILED_PRECONDITION
// The query requires an index
```

✅ **Query này OK (không cần index):**
```typescript
// Chỉ filter 1 field, không order by
ordersRef
  .where("userId", "==", "user_123")
  .limit(20)
```

### Khi Nào Cần Composite Index

Bạn cần composite index khi query có:

1. **Multiple where clauses** trên fields khác nhau
   ```
   .where("restaurantId", "==", "rest_001")
   .where("status", "==", "PENDING")
   ```

2. **Where + OrderBy** trên fields khác nhau
   ```
   .where("userId", "==", "user_123")
   .orderBy("createdAt", "desc")
   ```

3. **Multiple orderBy** clauses
   ```
   .orderBy("category", "asc")
   .orderBy("price", "desc")
   ```

4. **Where + Range filters**
   ```
   .where("restaurantId", "==", "rest_001")
   .where("price", ">", 50000)
   ```

### Tác Động Khi Thiếu Index

⚠️ **FAILED_PRECONDITION Error:**

```
FirebaseError: 9 FAILED_PRECONDITION: 
The query requires an index. 
You can create it here: https://console.firebase.google.com/...
```

**Khi lỗi này xảy ra:**
- ❌ User không xem được orders
- ❌ Seller không load được đơn hàng
- ❌ Queries trong backend functions fail
- ❌ App UI bị broken

**Giải pháp:**
- ✅ Tạo indexes TRƯỚC khi deploy production
- ✅ Test queries trong emulator với indexes
- ✅ Document tất cả queries cần indexes

---

## 📋 Danh Sách Indexes Bắt Buộc

### Tổng Quan

| Collection | Fields | Order | Purpose |
|-----------|---------|-------|---------|
| `orders` | `userId` + `createdAt` | ASC + DESC | Lấy lịch sử đơn hàng theo user, sort mới nhất |
| `orders` | `restaurantId` + `createdAt` | ASC + DESC | Lấy đơn hàng theo restaurant, sort mới nhất |
| `orders` | `shipperId` + `createdAt` | ASC + DESC | Lấy đơn hàng theo shipper, sort mới nhất |
| `orders` | `userId` + `status` + `createdAt` | ASC + ASC + DESC | Lọc đơn theo user và status |
| `orders` | `restaurantId` + `status` + `createdAt` | ASC + ASC + DESC | Lọc đơn theo restaurant và status |
| `menuItems` | `restaurantId` + `category` | ASC + ASC | Lọc món theo restaurant và category |
| `menuItems` | `restaurantId` + `available` + `category` | ASC + ASC + ASC | Lọc món available theo restaurant |
| `promotions` | `code` + `isActive` + `expiresAt` | ASC + ASC + DESC | Tìm promotion code còn hạn |
| `promotions` | `restaurantId` + `isActive` + `expiresAt` | ASC + ASC + DESC | Lấy promotions của restaurant |
| `reviews` | `restaurantId` + `createdAt` | ASC + DESC | Lấy reviews theo restaurant, sort mới nhất |
| `reviews` | `userId` + `createdAt` | ASC + DESC | Lấy reviews theo user |

---

## 📝 Chi Tiết Indexes

### 1. Orders - User History

**Query Pattern:**
```typescript
// Lấy order history của user, sort theo mới nhất
db.collection("orders")
  .where("userId", "==", userId)
  .orderBy("createdAt", "desc")
  .limit(20)
```

**Index Required:**
- Collection: `orders`
- Fields:
  - `userId` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- Buyer xem lịch sử đơn hàng của mình
- Display trong "My Orders" screen

---

### 2. Orders - Restaurant Orders

**Query Pattern:**
```typescript
// Lấy orders của restaurant, sort theo mới nhất
db.collection("orders")
  .where("restaurantId", "==", restaurantId)
  .orderBy("createdAt", "desc")
  .limit(50)
```

**Index Required:**
- Collection: `orders`
- Fields:
  - `restaurantId` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- Seller xem danh sách đơn hàng của quán
- Display trong restaurant management dashboard

---

### 3. Orders - Shipper Deliveries

**Query Pattern:**
```typescript
// Lấy orders được gán cho shipper
db.collection("orders")
  .where("shipperId", "==", shipperId)
  .orderBy("createdAt", "desc")
  .limit(20)
```

**Index Required:**
- Collection: `orders`
- Fields:
  - `shipperId` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- Shipper xem lịch sử giao hàng
- Track earnings và deliveries

---

### 4. Orders - Filter by User + Status

**Query Pattern:**
```typescript
// Lấy orders của user với status cụ thể
db.collection("orders")
  .where("userId", "==", userId)
  .where("status", "==", "PENDING")
  .orderBy("createdAt", "desc")
```

**Index Required:**
- Collection: `orders`
- Fields:
  - `userId` (ASCENDING)
  - `status` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- Filter orders: "Đơn đang chờ", "Đơn đã giao"
- Status-specific order lists

---

### 5. Orders - Filter by Restaurant + Status

**Query Pattern:**
```typescript
// Lấy orders của restaurant với status cụ thể
db.collection("orders")
  .where("restaurantId", "==", restaurantId)
  .where("status", "==", "PENDING")
  .orderBy("createdAt", "desc")
```

**Index Required:**
- Collection: `orders`
- Fields:
  - `restaurantId` (ASCENDING)
  - `status` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- Seller filter: "Đơn chờ xác nhận", "Đơn đang chuẩn bị"
- Restaurant order management by status

---

### 6. Menu Items - Restaurant + Category

**Query Pattern:**
```typescript
// Lấy menu items theo restaurant và category
db.collection("menuItems")
  .where("restaurantId", "==", restaurantId)
  .where("category", "==", "Phở")
  .get()
```

**Index Required:**
- Collection: `menuItems`
- Fields:
  - `restaurantId` (ASCENDING)
  - `category` (ASCENDING)

**Use Case:**
- Browse menu by category (Phở, Cơm, Bún, etc.)
- Filter món ăn theo phân loại

---

### 7. Menu Items - Available Items

**Query Pattern:**
```typescript
// Lấy các món available theo restaurant và category
db.collection("menuItems")
  .where("restaurantId", "==", restaurantId)
  .where("available", "==", true)
  .where("category", "==", "Phở")
  .get()
```

**Index Required:**
- Collection: `menuItems`
- Fields:
  - `restaurantId` (ASCENDING)
  - `available` (ASCENDING)
  - `category` (ASCENDING)

**Use Case:**
- Chỉ hiển thị món đang available
- Hide sold-out items

---

### 8. Promotions - Find by Code

**Query Pattern:**
```typescript
// Tìm promotion theo code, kiểm tra còn hạn
db.collection("promotions")
  .where("code", "==", promotionCode)
  .where("isActive", "==", true)
  .where("expiresAt", ">", new Date())
  .limit(1)
```

**Index Required:**
- Collection: `promotions`
- Fields:
  - `code` (ASCENDING)
  - `isActive` (ASCENDING)
  - `expiresAt` (DESCENDING)

**Use Case:**
- Validate promotion code khi buyer apply
- Check expiration và active status

---

### 9. Promotions - Restaurant Promotions

**Query Pattern:**
```typescript
// Lấy promotions của restaurant còn hạn
db.collection("promotions")
  .where("restaurantId", "==", restaurantId)
  .where("isActive", "==", true)
  .orderBy("expiresAt", "desc")
```

**Index Required:**
- Collection: `promotions`
- Fields:
  - `restaurantId` (ASCENDING)
  - `isActive` (ASCENDING)
  - `expiresAt` (DESCENDING)

**Use Case:**
- Seller xem các promotions đang chạy
- Display active promotions cho buyers

---

### 10. Reviews - Restaurant Reviews

**Query Pattern:**
```typescript
// Lấy reviews của restaurant, sort mới nhất
db.collection("reviews")
  .where("restaurantId", "==", restaurantId)
  .orderBy("createdAt", "desc")
  .limit(20)
```

**Index Required:**
- Collection: `reviews`
- Fields:
  - `restaurantId` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- Hiển thị reviews trên restaurant page
- Sort theo mới nhất

---

### 11. Reviews - User Reviews

**Query Pattern:**
```typescript
// Lấy reviews của user
db.collection("reviews")
  .where("userId", "==", userId)
  .orderBy("createdAt", "desc")
```

**Index Required:**
- Collection: `reviews`
- Fields:
  - `userId` (ASCENDING)
  - `createdAt` (DESCENDING)

**Use Case:**
- User xem các reviews đã viết
- Review history

---

## 🔧 Index Configuration JSON

### File: `firestore.indexes.json`

```json
{
  "indexes": [
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "restaurantId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "shipperId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "restaurantId", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "menuItems",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "restaurantId", "order": "ASCENDING" },
        { "fieldPath": "category", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "menuItems",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "restaurantId", "order": "ASCENDING" },
        { "fieldPath": "available", "order": "ASCENDING" },
        { "fieldPath": "category", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "promotions",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "code", "order": "ASCENDING" },
        { "fieldPath": "isActive", "order": "ASCENDING" },
        { "fieldPath": "expiresAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "promotions",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "restaurantId", "order": "ASCENDING" },
        { "fieldPath": "isActive", "order": "ASCENDING" },
        { "fieldPath": "expiresAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "reviews",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "restaurantId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "reviews",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ],
  "fieldOverrides": []
}
```

### Location

File này phải nằm ở:
```
backend/firestore.indexes.json
```

Cùng thư mục với `firestore.rules`

---

## 🚀 Cách Tạo Indexes

### Method 1: Deploy Indexes từ File

```bash
# Từ thư mục backend/
firebase deploy --only firestore:indexes

# Output:
# ✔  firestore: deployed indexes in firestore.indexes.json successfully
```

**Lưu ý:**
- Indexes có thể mất vài phút để build
- Status: Building → Ready
- Check progress trong Firebase Console

### Method 2: Tạo từ Firebase Console

1. Truy cập Firebase Console
2. Vào Firestore Database → Indexes tab
3. Click "Create Index"
4. Chọn Collection
5. Add fields và order
6. Click "Create"

**URL Format:**
```
https://console.firebase.google.com/project/YOUR_PROJECT/firestore/indexes
```

### Method 3: Tạo từ Error Link

Khi gặp error FAILED_PRECONDITION:

```
The query requires an index. 
You can create it here: https://console.firebase.google.com/...
```

1. Click vào link trong error message
2. Firebase sẽ tự động điền index config
3. Click "Create Index"
4. Đợi index build xong

**⚠️ Nhược điểm:**
- Phải chờ error xảy ra mới biết cần index
- Không có trong code (khó maintain)

**✅ Best Practice:**
- Tạo index từ `firestore.indexes.json`
- Version control indexes
- Deploy indexes TRƯỚC khi deploy functions

### Method 4: Test trong Emulator

```bash
# Start emulator với indexes
firebase emulators:start --import=./emulator-data

# Emulator sẽ tự động create indexes khi gặp query cần index
# Export indexes sau khi test
firebase emulators:export ./emulator-data
```

**Emulator behavior:**
- Tự động create indexes khi cần
- Indexes được save trong export data
- Import data sẽ restore indexes

---

## 🔍 Troubleshooting

### Error: Index Already Exists

```
Error: Index already exists
```

**Giải pháp:**
- Bỏ qua error (index đã có rồi)
- Hoặc delete index cũ trước khi deploy

### Error: Index Building

```
Error: Index is still building
```

**Giải pháp:**
- Đợi vài phút (indexes lớn mất nhiều thời gian)
- Check status trong Firebase Console
- Không deploy functions cho đến khi indexes Ready

### Error: Index Not Found

```
FirebaseError: The query requires an index
```

**Giải pháp:**
1. Check query có đúng format không
2. Check index đã được deploy chưa
3. Check fields trong index match với query
4. Deploy indexes: `firebase deploy --only firestore:indexes`

### Error: Too Many Indexes

```
Error: Project has reached the maximum number of indexes
```

**Giải pháp:**
- Xóa unused indexes
- Firebase limit: 200 composite indexes per project
- Optimize queries để dùng ít indexes hơn

### Performance Issues

**Symptoms:**
- Queries chậm
- Indexes quá lớn
- Write operations chậm (do update indexes)

**Giải pháp:**
- Review indexes, xóa unused
- Optimize query patterns
- Consider data denormalization
- Use pagination với limit()

---

## ✅ Best Practices

### 1. Document All Queries

**Maintain query inventory:**

```typescript
// orders.repository.ts

/**
 * Get user orders
 * 
 * Required Index:
 * - userId (ASC) + createdAt (DESC)
 */
async getUserOrders(userId: string): Promise<Order[]> {
  return db.collection("orders")
    .where("userId", "==", userId)
    .orderBy("createdAt", "desc")
    .limit(20)
    .get();
}
```

### 2. Test Queries Trong Emulator

```bash
# Start emulator
firebase emulators:start

# Test all queries
npm run test:queries

# Export indexes
firebase emulators:export ./emulator-data
```

### 3. Deploy Indexes TRƯỚC Functions

**Deployment order:**

```bash
# 1. Deploy indexes first
firebase deploy --only firestore:indexes

# 2. Wait for indexes to build (check console)

# 3. Deploy functions
firebase deploy --only functions
```

### 4. Version Control Indexes

```bash
# Add to git
git add firestore.indexes.json
git commit -m "feat: add indexes for order queries"
```

### 5. Monitor Index Usage

**Firebase Console → Firestore → Usage tab:**
- Check which indexes được dùng nhiều
- Identify unused indexes
- Optimize expensive indexes

### 6. Avoid Over-Indexing

❌ **BAD - Too many indexes:**
```json
// Tạo index cho mọi combination
userId + status
userId + createdAt
userId + status + createdAt
userId + restaurantId
userId + restaurantId + status
// → 5+ indexes chỉ cho 1 collection
```

✅ **GOOD - Minimal indexes:**
```json
// Chỉ tạo indexes cho queries thực sự dùng
userId + createdAt
userId + status + createdAt
// → 2 indexes cover hầu hết use cases
```

### 7. Use Single-Field Indexes Wisely

**Firestore tự động create single-field indexes:**
- `userId` (ASC)
- `createdAt` (ASC)
- `createdAt` (DESC)

**Không cần tạo composite index nếu:**
- Query chỉ filter 1 field
- Query chỉ orderBy 1 field

```typescript
// Không cần index
db.collection("orders")
  .where("userId", "==", userId)
  .get()

// Không cần index
db.collection("orders")
  .orderBy("createdAt", "desc")
  .limit(10)

// CẦN index
db.collection("orders")
  .where("userId", "==", userId)
  .orderBy("createdAt", "desc")
```

### 8. Consider Query Alternatives

**Thay vì tạo nhiều indexes, consider:**

- **Client-side filtering**: Lấy data nhiều hơn, filter ở app
- **Denormalization**: Duplicate data để avoid complex queries
- **Pre-computed aggregations**: Store counts/totals trong separate docs
- **Pagination**: Use limit() để giảm data size

**Example - Avoid complex index:**

```typescript
// ❌ Cần index phức tạp
db.collection("orders")
  .where("userId", "==", userId)
  .where("status", "in", ["PENDING", "CONFIRMED"])
  .where("totalAmount", ">", 100000)
  .orderBy("createdAt", "desc")

// ✅ Lấy data ít hơn, filter ở client
const orders = await db.collection("orders")
  .where("userId", "==", userId)
  .where("status", "in", ["PENDING", "CONFIRMED"])
  .orderBy("createdAt", "desc")
  .limit(50)
  .get()

// Filter amount ở client side
const expensiveOrders = orders.docs
  .map(doc => doc.data())
  .filter(order => order.totalAmount > 100000)
```

---

## 📚 Tài Liệu Liên Quan

- [FIRESTORE_SCHEMA.md](./FIRESTORE_SCHEMA.md) - Database schema
- [API_REFERENCE.md](./API_REFERENCE.md) - API queries documentation
- [ROLES_AND_PERMISSIONS.md](./ROLES_AND_PERMISSIONS.md) - Security rules
- [Firebase Indexes Documentation](https://firebase.google.com/docs/firestore/query-data/indexing)
- [Index Best Practices](https://firebase.google.com/docs/firestore/query-data/index-overview)
- [Query Limitations](https://firebase.google.com/docs/firestore/query-data/queries#query_limitations)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
