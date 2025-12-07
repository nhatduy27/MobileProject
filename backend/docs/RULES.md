# Firestore Security Rules

## 🔐 Tổng Quan

Firestore Security Rules kiểm soát:
- **Ai được đọc (read)** dữ liệu nào
- **Ai được ghi (write)** dữ liệu nào
- **Validate dữ liệu** trước khi lưu

**Nguyên tắc: "Deny by Default"** - Nếu không có rule cho phép, toàn bộ requests bị từ chối.

---

## 🛡️ General Security Principles

### 1. Deny by Default (Mặc Định Từ Chối)

**Nguyên tắc:** Tất cả requests đều bị từ chối trừ khi có rule cho phép rõ ràng.

```firestore
// ✅ CORRECT - Explicit allow
match /users/{userId} {
  allow read: if request.auth.uid == userId;
}

// ❌ WRONG - Implicit (không có rule = deny)
match /users/{userId} {
  // Không có rule nào = tất cả requests bị deny
}

// ⚠️ DANGEROUS - Allow all (tránh!)
match /users/{userId} {
  allow read, write: if true;  // Bất kỳ ai cũng truy cập được!
}
```

**Best practice:**
- Luôn sử dụng catch-all rule ở cuối để deny mọi thứ không match:
```firestore
match /{document=**} {
  allow read, write: if false;
}
```

---

### 2. Allow Minimal Reads/Writes (Quyền Tối Thiểu)

**Nguyên tắc:** Chỉ cho phép đúng quyền cần thiết, không hơn không kém.

```firestore
// ✅ GOOD - Specific permissions
match /orders/{orderId} {
  // Buyer chỉ đọc được order của mình
  allow read: if request.auth.uid == resource.data.userId;
  
  // Buyer chỉ được hủy order PENDING
  allow update: if request.auth.uid == resource.data.userId
    && resource.data.status == 'PENDING'
    && request.resource.data.status == 'CANCELLED';
}

// ❌ BAD - Too permissive
match /orders/{orderId} {
  // Buyer có thể làm gì cũng được với order
  allow read, write: if request.auth.uid == resource.data.userId;
}
```

**Phân tách quyền:**
- `read` = `get` + `list`
- `write` = `create` + `update` + `delete`

```firestore
// ✅ Specific operations
allow get: if condition1;     // Chỉ read 1 document
allow list: if condition2;    // Query nhiều documents
allow create: if condition3;  // Tạo mới
allow update: if condition4;  // Cập nhật
allow delete: if condition5;  // Xóa
```

---

### 3. Validate Data Before Write (Kiểm Tra Dữ Liệu)

**Nguyên tắc:** Luôn validate dữ liệu trước khi cho phép ghi vào database.

```firestore
// ✅ GOOD - Validate all fields
match /restaurants/{restaurantId} {
  allow create: if request.auth != null
    // Check required fields exist
    && request.resource.data.keys().hasAll(['name', 'ownerId', 'phoneNumber', 'email'])
    // Check ownerId matches authenticated user
    && request.resource.data.ownerId == request.auth.uid
    // Check name is not empty
    && request.resource.data.name.size() > 0
    // Check email format
    && request.resource.data.email.matches('.*@.*\\..*')
    // Check timestamp is server time
    && request.resource.data.createdAt == request.time;
}

// ❌ BAD - No validation
match /restaurants/{restaurantId} {
  allow create: if request.auth != null;
  // Client có thể gửi bất kỳ data gì!
}
```

**Validation checklist:**
- ✅ Required fields tồn tại: `request.resource.data.keys().hasAll([...])`
- ✅ Field types đúng: `request.resource.data.age is int`
- ✅ Values trong phạm vi hợp lệ: `request.resource.data.rating >= 1 && request.resource.data.rating <= 5`
- ✅ String không rỗng: `request.resource.data.name.size() > 0`
- ✅ Timestamps từ server: `request.resource.data.createdAt == request.time`
- ✅ Không cho phép update protected fields: `!(request.resource.data.keys().hasAny(['ownerId']))`

---

### 4. Never Trust Client Input (Không Tin Client)

**Nguyên tắc:** Luôn giả định client có thể gửi data độc hại hoặc không hợp lệ.

```firestore
// ✅ GOOD - Server-side validation
match /orders/{orderId} {
  allow create: if request.auth != null
    // Validate userId matches auth
    && request.resource.data.userId == request.auth.uid
    // Force initial status (client không thể set khác)
    && request.resource.data.status == 'PENDING'
    // Force payment status
    && request.resource.data.paymentStatus == 'PENDING'
    // Force server timestamp
    && request.resource.data.createdAt == request.time
    // ShipperId must be null initially
    && request.resource.data.shipperId == null
    // Validate items structure
    && request.resource.data.items.size() > 0
    && request.resource.data.items.all(item, 
        item.quantity > 0 && item.unitPrice > 0
      );
}

// ❌ BAD - Trust client
match /orders/{orderId} {
  allow create: if request.auth != null;
  // Client có thể:
  // - Set status = 'COMPLETED' (bypass workflow)
  // - Set userId = người khác
  // - Set paymentStatus = 'COMPLETED' (không trả tiền)
  // - Set createdAt = thời gian bất kỳ
}
```

**Common attacks to prevent:**
- ❌ Client set `userId` = người khác → Đọc data của người khác
- ❌ Client set `role` = 'ADMIN' → Privilege escalation
- ❌ Client set `isVerified` = true → Bypass verification
- ❌ Client set `totalAmount` = 1 → Bypass payment
- ❌ Client set `status` = 'COMPLETED' → Bypass workflow
- ❌ Client set `createdAt` = quá khứ → Manipulate timestamps

**Defense:**
```firestore
// Force values from server context
request.resource.data.userId == request.auth.uid
request.resource.data.createdAt == request.time

// Check against existing data (for updates)
request.resource.data.role == resource.data.role  // No role changes

// Validate against whitelist
request.resource.data.status in ['PENDING', 'CONFIRMED', 'CANCELLED']
```

---

### Summary Table

| Principle | Rule | Example |
|-----------|------|---------|
| **Deny by Default** | Không có rule = deny | Luôn có catch-all `if false` |
| **Minimal Permissions** | Chỉ cho quyền cần thiết | `allow get` thay vì `allow read` |
| **Validate Data** | Check trước khi write | `request.resource.data.keys().hasAll([...])` |
| **Never Trust Client** | Force server values | `request.resource.data.createdAt == request.time` |

---

## 👥 Roles trong Ứng Dụng

| Role | Mô Tả | Quyền |
|------|-------|-------|
| **BUYER** | Khách hàng | Đặt hàng, đánh giá, xem lịch sử |
| **SELLER** | Chủ nhà hàng | Quản lý menu, xác nhận đơn, xem doanh thu |
| **SHIPPER** | Người giao hàng | Nhận đơn, cập nhật vị trí, hoàn thành giao |
| **ADMIN** | Quản trị viên | Quản lý toàn bộ hệ thống |

---

## 📋 Rules Structure

Firestore Rules file cấu trúc cơ bản:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Rule 1
    match /users/{userId} {
      allow read, write: if condition;
    }
    
    // Rule 2
    match /restaurants/{restaurantId} {
      allow read: if condition;
      allow write: if condition;
    }
  }
}
```

**Phương pháp kiểm tra:**
- `read` = `get` + `list`
- `write` = `create` + `update` + `delete`

---

## 🎯 Chi Tiết Rules

### 1️⃣ Collection: `users`

#### Quyền Read

```firestore
match /users/{userId} {
  // Bản thân user có thể xem profile của mình
  allow get: if request.auth.uid == userId;
  
  // Bất kỳ ai đã đăng nhập có thể xem displayName, avatarUrl (công khai)
  // Nhưng không được xem email, phoneNumber, addresses
  allow list: if request.auth != null;
}
```

**Logic:**
- ✅ User 123 xem profile của user 123 → **ALLOW**
- ✅ User 456 xem displayName của user 123 → **ALLOW** (công khai)
- ❌ User 456 xem email của user 123 → **DENY** (riêng tư)

#### Quyền Write

```firestore
match /users/{userId} {
  // Tạo user: chỉ khi tạo lần đầu (Auth trigger)
  allow create: if request.auth != null 
    && request.auth.uid == userId
    && request.resource.data.role in ['BUYER', 'SELLER', 'SHIPPER'];
  
  // Cập nhật user: bản thân có thể cập nhật
  allow update: if request.auth.uid == userId
    && (request.resource.data.role == resource.data.role)  // Không đổi role
    && !(request.resource.data.keys().hasAny(['isVerified', 'isActive']))  // Không update special fields
    && request.resource.data.updatedAt == request.time;  // Validate timestamp
  
  // Xóa user: riêng tư (chỉ chính user đó)
  allow delete: if request.auth.uid == userId;
}
```

**Chi tiết:**
- ✅ User tạo profile của chính mình với role BUYER → **ALLOW**
- ❌ User tạo profile với role ADMIN → **DENY** (không được phép)
- ✅ User cập nhật displayName của chính mình → **ALLOW**
- ❌ User cập nhật isVerified → **DENY** (chỉ admin/backend)
- ❌ User thay đổi role từ BUYER sang SELLER → **DENY**

---

### 2️⃣ Collection: `restaurants`

#### Quyền Read

```firestore
match /restaurants/{restaurantId} {
  // Bất kỳ ai (kể cả chưa đăng nhập) có thể xem thông tin nhà hàng công khai
  allow get: if resource.data.isOpen == true;
  
  // List tất cả nhà hàng (có lọc)
  allow list: if true;  // Bất kỳ ai
}
```

**Logic:**
- ✅ Bất kỳ user xem thông tin nhà hàng mở → **ALLOW**
- ❌ Xem nhà hàng chưa công khai → **DENY**

#### Quyền Write

```firestore
match /restaurants/{restaurantId} {
  // Tạo nhà hàng: phải là SELLER
  allow create: if request.auth != null
    && userHasRole(request.auth.uid, 'SELLER')
    && request.resource.data.ownerId == request.auth.uid;
  
  // Cập nhật: phải là owner hoặc admin
  allow update: if request.auth != null
    && (
      resource.data.ownerId == request.auth.uid ||
      userHasRole(request.auth.uid, 'ADMIN')
    )
    && !(request.resource.data.keys().hasAny(['ownerId', 'createdAt']))  // Không đổi owner
    && request.resource.data.updatedAt == request.time;
  
  // Xóa: chỉ owner
  allow delete: if request.auth != null
    && resource.data.ownerId == request.auth.uid;
}
```

**Helper Function:**

```firestore
// Kiểm tra user có role gì
function userHasRole(userId, role) {
  return get(/databases/$(database)/documents/users/$(userId)).data.role == role;
}
```

---

### 3️⃣ Subcollection: `restaurants/{restaurantId}/menuItems`

#### Quyền Read

```firestore
match /restaurants/{restaurantId}/menuItems/{itemId} {
  // Bất kỳ ai có thể xem menu items của nhà hàng
  allow get, list: if true;
}
```

#### Quyền Write

```firestore
match /restaurants/{restaurantId}/menuItems/{itemId} {
  // Tạo/cập nhật/xóa: chỉ owner nhà hàng
  allow create, update, delete: if request.auth != null
    && get(/databases/$(database)/documents/restaurants/$(restaurantId)).data.ownerId == request.auth.uid;
}
```

---

### 4️⃣ Collection: `orders`

**Đây là phần quan trọng nhất!**

#### Quyền Read

**⚠️ LƯU Ý QUAN TRỌNG:** Firestore Security Rules **KHÔNG hỗ trợ** complex queries như `.where()`, `.query()`. Rules chỉ có thể:
- Đọc 1 document cụ thể: `get(/databases/$(database)/documents/path)`
- Check conditions trên document đó

**❌ KHÔNG THỂ:** Query list các restaurants của seller trong rules  
**✅ GIẢI PHÁP:** Client phải query và backend/Cloud Functions validate ownership

```firestore
match /orders/{orderId} {
  // BUYER: xem đơn hàng của chính mình
  allow get: if request.auth != null
    && request.auth.uid == resource.data.userId;
  
  // SELLER: xem đơn hàng của nhà hàng mình
  // Check nếu user là owner của restaurant trong order
  allow get: if request.auth != null
    && isSellerOfRestaurant(request.auth.uid, resource.data.restaurantId);
  
  // SHIPPER: xem đơn hàng được assign cho mình
  allow get: if request.auth != null
    && request.auth.uid == resource.data.shipperId;
  
  // ADMIN: xem tất cả
  allow get: if request.auth != null
    && userHasRole(request.auth.uid, 'ADMIN');
  
  // List orders: Cho phép authenticated users query
  // ⚠️ Rules KHÔNG thể filter query results
  // Client phải query: .where('userId', '==', currentUserId)
  // Backend Cloud Functions sẽ validate ownership
  allow list: if request.auth != null;
}
```

**Helper Function - Kiểm tra seller ownership:**

```firestore
// ✅ CORRECT - Direct document read
function isSellerOfRestaurant(userId, restaurantId) {
  return exists(/databases/$(database)/documents/restaurants/$(restaurantId))
    && get(/databases/$(database)/documents/restaurants/$(restaurantId)).data.ownerId == userId;
}

// ❌ WRONG - Firestore Rules không hỗ trợ queries
// function userRestaurants(userId) {
//   return firestore.query(
//     collection('restaurants'),
//     where('ownerId', '==', userId)
//   );  // ← SYNTAX ERROR!
// }
```

**Pattern để list orders theo role:**

```typescript
// CLIENT-SIDE (Flutter/React/etc.)

// BUYER: Query orders của mình
const buyerOrders = await db.collection('orders')
  .where('userId', '==', currentUserId)  // Client filter
  .get();
// Security rules sẽ check: allow list if request.auth != null

// SELLER: Query orders của nhà hàng
const sellerOrders = await db.collection('orders')
  .where('restaurantId', '==', myRestaurantId)  // Client filter
  .get();
// Security rules CHỈ cho phép list (không filter được)
// Backend Cloud Functions validate ownership sau

// SHIPPER: Query orders được assign
const shipperOrders = await db.collection('orders')
  .where('shipperId', '==', currentUserId)  // Client filter
  .get();
```

**Backend Validation (Cloud Functions):**

```typescript
// Cloud Function callable: getMyOrders
export const getMyOrders = onCall(async (request) => {
  const { auth } = request;
  const userId = auth?.uid;
  const userRole = auth?.token?.role;

  if (!userId) throw new Error('Unauthenticated');

  let ordersQuery;

  if (userRole === 'BUYER') {
    // Buyer: chỉ lấy orders của mình
    ordersQuery = db.collection('orders')
      .where('userId', '==', userId);
  } else if (userRole === 'SELLER') {
    // Seller: lấy orders của các restaurants mình sở hữu
    const myRestaurants = await db.collection('restaurants')
      .where('ownerId', '==', userId)
      .get();
    const restaurantIds = myRestaurants.docs.map(doc => doc.id);
    
    ordersQuery = db.collection('orders')
      .where('restaurantId', 'in', restaurantIds);
  } else if (userRole === 'SHIPPER') {
    // Shipper: lấy orders được assign
    ordersQuery = db.collection('orders')
      .where('shipperId', '==', userId);
  } else {
    throw new Error('Invalid role');
  }

  const snapshot = await ordersQuery.get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
});
```

#### Quyền Write

```firestore
match /orders/{orderId} {
  // TẠO đơn hàng: chỉ BUYER có thể tạo
  allow create: if request.auth != null
    && userHasRole(request.auth.uid, 'BUYER')
    && request.resource.data.userId == request.auth.uid
    && request.resource.data.status == 'PENDING'
    && request.resource.data.createdAt == request.time
    && request.resource.data.paymentStatus == 'PENDING'
    && request.resource.data.shipperId == null
    && validateOrderItems(request.resource.data.items)
    && validateDeliveryAddress(request.resource.data.deliveryAddress);
  
  // CẬP NHẬT đơn hàng: tùy theo role
  allow update: if request.auth != null && (
    // BUYER: chỉ được hủy order (PENDING → CANCELLED)
    (
      userHasRole(request.auth.uid, 'BUYER')
      && request.auth.uid == resource.data.userId
      && resource.data.status == 'PENDING'
      && request.resource.data.status == 'CANCELLED'
      && onlyFieldChanges(['status', 'updatedAt'])
    ) ||
    // SELLER: cập nhật status (confirm, preparing, etc.)
    // ✅ Sử dụng isSellerOfRestaurant thay vì userRestaurants
    (
      userHasRole(request.auth.uid, 'SELLER')
      && isSellerOfRestaurant(request.auth.uid, resource.data.restaurantId)
      && request.resource.data.status in ['CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY']
      && resource.data.status in ['PENDING', 'CONFIRMED', 'PREPARING']
      && onlyFieldChanges(['status', 'updatedAt'])
    ) ||
    // SHIPPER: cập nhật vị trí & status giao hàng
    (
      userHasRole(request.auth.uid, 'SHIPPER')
      && request.auth.uid == resource.data.shipperId
      && request.resource.data.status in ['DELIVERING', 'COMPLETED']
      && resource.data.status in ['CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'DELIVERING']
      && onlyFieldChanges(['status', 'updatedAt'])
    ) ||
    // ADMIN: full control
    (
      userHasRole(request.auth.uid, 'ADMIN')
    )
  ) && request.resource.data.updatedAt == request.time;
  
  // XÓA đơn hàng: không cho phép xóa (chỉ set status)
  allow delete: if false;  // No deletion allowed
}
```

**Helper Functions:**

```firestore
// Validate items structure
function validateOrderItems(items) {
  return items.size() > 0
    && items.all(item, 
        item.keys().hasAll(['menuItemId', 'quantity', 'unitPrice']) 
        && item.quantity > 0 
        && item.unitPrice > 0
      );
}

// Validate delivery address
function validateDeliveryAddress(address) {
  return address.keys().hasAll(['street', 'ward', 'district', 'city'])
    && address.street.size() > 0
    && address.city.size() > 0;
}

// Check only specific fields changed
function onlyFieldChanges(allowedFields) {
  return request.resource.data.diff(resource.data).affectedKeys()
    .hasOnly(allowedFields);
}
```

---

### 5️⃣ Collection: `promotions`

#### Quyền Read

```firestore
match /promotions/{promoId} {
  // Bất kỳ ai có thể xem promotion (công khai)
  allow get: if resource.data.isActive == true
    && resource.data.endDate >= request.time;
  
  allow list: if true;  // Xem danh sách tất cả
}
```

#### Quyền Write

```firestore
match /promotions/{promoId} {
  // Tạo/cập nhật: chỉ ADMIN hoặc SELLER (promotion của nhà hàng)
  allow create, update, delete: if request.auth != null
    && (
      userHasRole(request.auth.uid, 'ADMIN') ||
      (
        userHasRole(request.auth.uid, 'SELLER')
        // ✅ Check nếu promotion là của restaurant mình sở hữu
        && request.resource.data.restaurantId != null
        && isSellerOfRestaurant(request.auth.uid, request.resource.data.restaurantId)
      )
    );
}
```

---

## ✅ ❌ Examples: Allowed and Rejected Operations

### Collection: `users`

#### ✅ Allowed Operations

```typescript
// 1. User tự đọc profile của mình
await db.collection('users').doc('user_123').get();
// Auth: uid = 'user_123'
// Result: ALLOW ✓

// 2. User tạo profile lần đầu
await db.collection('users').doc('user_123').set({
  email: 'user@example.com',
  role: 'BUYER',
  isActive: true,
  isVerified: false,
  createdAt: serverTimestamp()
});
// Auth: uid = 'user_123'
// Result: ALLOW ✓

// 3. User cập nhật displayName của mình
await db.collection('users').doc('user_123').update({
  displayName: 'New Name',
  updatedAt: serverTimestamp()
});
// Auth: uid = 'user_123'
// Result: ALLOW ✓
```

#### ❌ Rejected Operations

```typescript
// 1. User đọc profile người khác
await db.collection('users').doc('user_456').get();
// Auth: uid = 'user_123'
// Result: DENY ✗ (không phải profile của mình)

// 2. User tạo profile với role ADMIN
await db.collection('users').doc('user_123').set({
  email: 'user@example.com',
  role: 'ADMIN',  // ← Không hợp lệ
  createdAt: serverTimestamp()
});
// Result: DENY ✗ (role phải là BUYER/SELLER/SHIPPER)

// 3. User tự set isVerified = true
await db.collection('users').doc('user_123').update({
  isVerified: true  // ← Protected field
});
// Result: DENY ✗ (chỉ admin/backend có thể update)

// 4. User thay đổi role
await db.collection('users').doc('user_123').update({
  role: 'SELLER'  // ← Không được đổi role
});
// Result: DENY ✗ (role không thể thay đổi)
```

---

### Collection: `restaurants`

#### ✅ Allowed Operations

```typescript
// 1. Bất kỳ ai đọc thông tin nhà hàng
await db.collection('restaurants').doc('rest_001').get();
// Auth: không cần (public)
// Result: ALLOW ✓

// 2. SELLER tạo nhà hàng mới
await db.collection('restaurants').add({
  name: 'Phở Hà Nội',
  ownerId: 'seller_123',  // Must match auth.uid
  phoneNumber: '+84123456789',
  email: 'info@pho.com',
  address: { street: '123 ABC', city: 'Hà Nội' },
  category: 'Việt',
  createdAt: serverTimestamp()
});
// Auth: uid = 'seller_123', role = 'SELLER'
// Result: ALLOW ✓

// 3. Owner cập nhật thông tin nhà hàng
await db.collection('restaurants').doc('rest_001').update({
  name: 'Phở Hà Nội Mới',
  updatedAt: serverTimestamp()
});
// Auth: uid = 'seller_123' (owner)
// Result: ALLOW ✓
```

#### ❌ Rejected Operations

```typescript
// 1. BUYER tạo nhà hàng
await db.collection('restaurants').add({
  name: 'Nhà hàng',
  ownerId: 'buyer_456',
  ...
});
// Auth: uid = 'buyer_456', role = 'BUYER'
// Result: DENY ✗ (chỉ SELLER mới tạo được)

// 2. SELLER tạo nhà hàng cho người khác
await db.collection('restaurants').add({
  name: 'Nhà hàng',
  ownerId: 'seller_789',  // ← Khác auth.uid
  ...
});
// Auth: uid = 'seller_123'
// Result: DENY ✗ (ownerId phải match auth.uid)

// 3. Người khác update nhà hàng
await db.collection('restaurants').doc('rest_001').update({
  name: 'Hack'
});
// Auth: uid = 'seller_456' (không phải owner)
// Result: DENY ✗ (chỉ owner mới update được)

// 4. Owner thay đổi ownerId
await db.collection('restaurants').doc('rest_001').update({
  ownerId: 'seller_789'  // ← Protected field
});
// Result: DENY ✗ (không được đổi owner)
```

---

### Collection: `orders`

#### ✅ Allowed Operations

```typescript
// 1. BUYER tạo order mới
await db.collection('orders').add({
  userId: 'buyer_123',  // Must match auth.uid
  restaurantId: 'rest_001',
  items: [
    { menuItemId: 'item_1', quantity: 2, unitPrice: 50000, itemTotal: 100000 }
  ],
  status: 'PENDING',  // Must be PENDING
  subtotal: 100000,
  totalAmount: 115000,
  deliveryAddress: {
    street: '123 ABC',
    ward: 'Ward 1',
    district: 'District 1',
    city: 'TP.HCM'
  },
  paymentStatus: 'PENDING',
  shipperId: null,  // Must be null initially
  createdAt: serverTimestamp()
});
// Auth: uid = 'buyer_123', role = 'BUYER'
// Result: ALLOW ✓

// 2. BUYER đọc order của mình
await db.collection('orders').doc('order_001').get();
// Auth: uid = 'buyer_123' (owner of order)
// Result: ALLOW ✓

// 3. BUYER hủy order PENDING
await db.collection('orders').doc('order_001').update({
  status: 'CANCELLED',
  updatedAt: serverTimestamp()
});
// Auth: uid = 'buyer_123', role = 'BUYER'
// Current status: 'PENDING'
// Result: ALLOW ✓

// 4. SELLER xác nhận order
await db.collection('orders').doc('order_001').update({
  status: 'CONFIRMED',
  updatedAt: serverTimestamp()
});
// Auth: uid = 'seller_123', role = 'SELLER'
// Restaurant 'rest_001' ownerId = 'seller_123'
// Current status: 'PENDING'
// Result: ALLOW ✓

// 5. SHIPPER cập nhật delivery status
await db.collection('orders').doc('order_001').update({
  status: 'DELIVERING',
  updatedAt: serverTimestamp()
});
// Auth: uid = 'shipper_456', role = 'SHIPPER'
// Order shipperId = 'shipper_456'
// Current status: 'READY_FOR_DELIVERY'
// Result: ALLOW ✓
```

#### ❌ Rejected Operations

```typescript
// 1. BUYER tạo order với status COMPLETED
await db.collection('orders').add({
  userId: 'buyer_123',
  status: 'COMPLETED',  // ← Phải là PENDING
  ...
});
// Result: DENY ✗ (status phải là PENDING)

// 2. BUYER tạo order cho người khác
await db.collection('orders').add({
  userId: 'buyer_456',  // ← Khác auth.uid
  ...
});
// Auth: uid = 'buyer_123'
// Result: DENY ✗ (userId phải match auth.uid)

// 3. BUYER đọc order của người khác
await db.collection('orders').doc('order_002').get();
// Auth: uid = 'buyer_123'
// Order userId = 'buyer_456'
// Result: DENY ✗ (không phải order của mình)

// 4. BUYER hủy order đã CONFIRMED
await db.collection('orders').doc('order_001').update({
  status: 'CANCELLED'
});
// Auth: uid = 'buyer_123', role = 'BUYER'
// Current status: 'CONFIRMED'  // ← Quá muộn
// Result: DENY ✗ (chỉ hủy được khi PENDING)

// 5. BUYER update nhiều fields cùng lúc
await db.collection('orders').doc('order_001').update({
  status: 'CANCELLED',
  totalAmount: 1,  // ← Không được đổi totalAmount
  updatedAt: serverTimestamp()
});
// Result: DENY ✗ (chỉ được đổi status và updatedAt)

// 6. SELLER xác nhận order của nhà hàng khác
await db.collection('orders').doc('order_001').update({
  status: 'CONFIRMED'
});
// Auth: uid = 'seller_456', role = 'SELLER'
// Restaurant 'rest_001' ownerId = 'seller_123' (không phải seller_456)
// Result: DENY ✗ (không phải owner của restaurant)

// 7. SHIPPER update order chưa được assign
await db.collection('orders').doc('order_001').update({
  status: 'DELIVERING'
});
// Auth: uid = 'shipper_456', role = 'SHIPPER'
// Order shipperId = null  // ← Chưa assign
// Result: DENY ✗ (phải được assign trước)

// 8. Xóa order
await db.collection('orders').doc('order_001').delete();
// Result: DENY ✗ (không được xóa orders)
```

---

### Collection: `menuItems` (Subcollection)

#### ✅ Allowed Operations

```typescript
// 1. Bất kỳ ai đọc menu items
await db.collection('restaurants').doc('rest_001')
  .collection('menuItems').doc('item_001').get();
// Result: ALLOW ✓

// 2. Owner tạo menu item
await db.collection('restaurants').doc('rest_001')
  .collection('menuItems').add({
    name: 'Phở Bò',
    price: 50000,
    isAvailable: true,
    createdAt: serverTimestamp()
  });
// Auth: uid = 'seller_123' (owner of rest_001)
// Result: ALLOW ✓

// 3. Owner cập nhật menu item
await db.collection('restaurants').doc('rest_001')
  .collection('menuItems').doc('item_001').update({
    price: 55000
  });
// Auth: uid = 'seller_123' (owner)
// Result: ALLOW ✓
```

#### ❌ Rejected Operations

```typescript
// 1. BUYER tạo menu item
await db.collection('restaurants').doc('rest_001')
  .collection('menuItems').add({
    name: 'Hack',
    price: 1
  });
// Auth: uid = 'buyer_456', role = 'BUYER'
// Result: DENY ✗ (chỉ owner mới tạo được)

// 2. SELLER khác update menu item
await db.collection('restaurants').doc('rest_001')
  .collection('menuItems').doc('item_001').update({
    price: 1
  });
// Auth: uid = 'seller_789' (không phải owner của rest_001)
// Result: DENY ✗
```

---

### Collection: `promotions`

#### ✅ Allowed Operations

```typescript
// 1. Bất kỳ ai đọc promotions
await db.collection('promotions').doc('promo_001').get();
// Result: ALLOW ✓

// 2. ADMIN tạo global promotion
await db.collection('promotions').add({
  code: 'WELCOME10',
  discountValue: 10,
  type: 'PERCENT',
  restaurantId: null,  // Global
  isActive: true,
  ...
});
// Auth: uid = 'admin_001', role = 'ADMIN'
// Result: ALLOW ✓

// 3. SELLER tạo promotion cho nhà hàng mình
await db.collection('promotions').add({
  code: 'PHO50K',
  discountValue: 50000,
  type: 'FIXED',
  restaurantId: 'rest_001',  // Restaurant mình sở hữu
  isActive: true,
  ...
});
// Auth: uid = 'seller_123' (owner of rest_001)
// Result: ALLOW ✓
```

#### ❌ Rejected Operations

```typescript
// 1. BUYER tạo promotion
await db.collection('promotions').add({
  code: 'HACK',
  ...
});
// Auth: uid = 'buyer_456', role = 'BUYER'
// Result: DENY ✗ (chỉ ADMIN/SELLER mới tạo được)

// 2. SELLER tạo promotion cho nhà hàng khác
await db.collection('promotions').add({
  code: 'HACK',
  restaurantId: 'rest_002',  // Không phải nhà hàng mình
  ...
});
// Auth: uid = 'seller_123' (owner of rest_001, không phải rest_002)
// Result: DENY ✗
```

---

## 📋 Complete Rules File

Tệp hoàn chỉnh để copy & paste:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // ============ HELPER FUNCTIONS ============
    
    function userHasRole(userId, role) {
      return exists(/databases/$(database)/documents/users/$(userId))
        && get(/databases/$(database)/documents/users/$(userId)).data.role == role;
    }
    
    // ✅ CORRECT - Check if user is owner of specific restaurant
    function isSellerOfRestaurant(userId, restaurantId) {
      return exists(/databases/$(database)/documents/restaurants/$(restaurantId))
        && get(/databases/$(database)/documents/restaurants/$(restaurantId)).data.ownerId == userId;
    }
    
    function validateOrderItems(items) {
      return items.size() > 0
        && items.all(item, 
            item.keys().hasAll(['menuItemId', 'quantity', 'unitPrice']) 
            && item.quantity > 0 
            && item.unitPrice > 0
          );
    }
    
    function validateDeliveryAddress(address) {
      return address != null
        && address.keys().hasAll(['street', 'ward', 'district', 'city'])
        && address.street.size() > 0
        && address.city.size() > 0;
    }
    
    function onlyFieldChanges(allowedFields) {
      return request.resource.data.diff(resource.data).affectedKeys()
        .hasOnly(allowedFields);
    }
    
    // ============ COLLECTIONS ============
    
    // USERS Collection
    match /users/{userId} {
      allow get: if request.auth.uid == userId;
      
      allow list: if request.auth != null;
      
      allow create: if request.auth != null 
        && request.auth.uid == userId
        && request.resource.data.role in ['BUYER', 'SELLER', 'SHIPPER']
        && request.resource.data.keys().hasAll(['email', 'role', 'isActive', 'isVerified'])
        && request.resource.data.createdAt == request.time;
      
      allow update: if request.auth.uid == userId
        && request.resource.data.role == resource.data.role
        && !(request.resource.data.keys().hasAny(['isVerified', 'isActive']))
        && request.resource.data.updatedAt == request.time;
      
      allow delete: if request.auth.uid == userId;
    }
    
    // RESTAURANTS Collection
    match /restaurants/{restaurantId} {
      allow get, list: if true;
      
      allow create: if request.auth != null
        && userHasRole(request.auth.uid, 'SELLER')
        && request.resource.data.ownerId == request.auth.uid
        && request.resource.data.keys().hasAll(['name', 'ownerId', 'phoneNumber', 'email', 'address', 'category'])
        && request.resource.data.createdAt == request.time;
      
      allow update: if request.auth != null
        && (
          resource.data.ownerId == request.auth.uid ||
          userHasRole(request.auth.uid, 'ADMIN')
        )
        && !(request.resource.data.keys().hasAny(['ownerId', 'createdAt']))
        && request.resource.data.updatedAt == request.time;
      
      allow delete: if request.auth != null
        && resource.data.ownerId == request.auth.uid;
      
      // MENU ITEMS Subcollection
      match /menuItems/{itemId} {
        allow get, list: if true;
        
        allow create, update, delete: if request.auth != null
          && resource.ref.parent.parent.get().data.ownerId == request.auth.uid;
      }
      
      // REVIEWS Subcollection
      match /reviews/{reviewId} {
        allow get, list: if true;
        
        allow create: if request.auth != null
          && request.resource.data.userId == request.auth.uid
          && request.resource.data.rating >= 1 && request.resource.data.rating <= 5
          && request.resource.data.createdAt == request.time;
        
        allow update, delete: if request.auth != null
          && request.auth.uid == resource.data.userId;
      }
    }
    
    // ORDERS Collection
    match /orders/{orderId} {
      // ✅ CORRECTED: Replaced userRestaurants() with isSellerOfRestaurant()
      allow get: if request.auth != null
        && (
          request.auth.uid == resource.data.userId ||
          isSellerOfRestaurant(request.auth.uid, resource.data.restaurantId) ||
          request.auth.uid == resource.data.shipperId ||
          userHasRole(request.auth.uid, 'ADMIN')
        );
      
      allow list: if request.auth != null;
      
      allow create: if request.auth != null
        && userHasRole(request.auth.uid, 'BUYER')
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.status == 'PENDING'
        && request.resource.data.createdAt == request.time
        && validateOrderItems(request.resource.data.items)
        && validateDeliveryAddress(request.resource.data.deliveryAddress)
        && request.resource.data.totalAmount > 0;
      
      allow update: if request.auth != null && (
        // BUYER: cancel order
        (
          userHasRole(request.auth.uid, 'BUYER')
          && request.auth.uid == resource.data.userId
          && resource.data.status == 'PENDING'
          && request.resource.data.status == 'CANCELLED'
          && onlyFieldChanges(['status', 'updatedAt'])
        ) ||
        // SELLER: update status
        // ✅ CORRECTED: Replaced userRestaurants() with isSellerOfRestaurant()
        (
          userHasRole(request.auth.uid, 'SELLER')
          && isSellerOfRestaurant(request.auth.uid, resource.data.restaurantId)
          && request.resource.data.status in ['CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY']
          && onlyFieldChanges(['status', 'updatedAt'])
        ) ||
        // SHIPPER: update delivery status
        (
          userHasRole(request.auth.uid, 'SHIPPER')
          && request.auth.uid == resource.data.shipperId
          && request.resource.data.status in ['DELIVERING', 'COMPLETED']
          && onlyFieldChanges(['status', 'updatedAt'])
        ) ||
        // ADMIN: full control
        (
          userHasRole(request.auth.uid, 'ADMIN')
        )
      ) && request.resource.data.updatedAt == request.time;
      
      allow delete: if false;
      
      // TIMELINE Subcollection
      match /timeline/{eventId} {
        // ✅ CORRECTED: Replaced userRestaurants() with isSellerOfRestaurant()
        allow get, list: if request.auth != null
          && (
            request.auth.uid == get(/databases/$(database)/documents/orders/$(orderId)).data.userId ||
            isSellerOfRestaurant(request.auth.uid, get(/databases/$(database)/documents/orders/$(orderId)).data.restaurantId) ||
            request.auth.uid == get(/databases/$(database)/documents/orders/$(orderId)).data.shipperId ||
            userHasRole(request.auth.uid, 'ADMIN')
          );
        
        allow create: if request.auth != null
          && userHasRole(request.auth.uid, 'ADMIN');
        
        allow delete: if false;
      }
    }
    
    // PROMOTIONS Collection
    match /promotions/{promoId} {
      allow get, list: if true;
      
      // ✅ CORRECTED: Replaced userRestaurants() with isSellerOfRestaurant()
      allow create, update: if request.auth != null
        && (
          userHasRole(request.auth.uid, 'ADMIN') ||
          (
            userHasRole(request.auth.uid, 'SELLER')
            && request.resource.data.restaurantId != null
            && isSellerOfRestaurant(request.auth.uid, request.resource.data.restaurantId)
          )
        );
      
      allow delete: if request.auth != null
        && userHasRole(request.auth.uid, 'ADMIN');
    }
    
    // Catch-all: Deny everything else
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

---

## 🧪 Testing Rules

### Cách Test trong Emulator

```bash
# 1. Start emulator
firebase emulators:start

# 2. Vào Firebase Console
# http://localhost:4000

# 3. Chọn Firestore → Rules
# 4. Test rules ở sidebar "Rules Playground"
```

### Test Cases

```firestore
// Test: BUYER có thể xem order của mình không?
// Expected: ALLOW
rules_playground({
  rules_version: 2,
  service cloud.firestore {
    match /databases/{database}/documents {
      match /orders/{orderId} {
        allow get: if request.auth.uid == resource.data.userId;
      }
    }
  },
  request: {
    auth: { uid: 'buyer_001' },
    method: 'get',
    path: '/databases/(default)/documents/orders/order_123'
  },
  resource: {
    data: { userId: 'buyer_001' }
  }
});  // ALLOW ✓
```

---

## 📊 Security Rule Best Practices

### ✅ DO

```firestore
// ✅ Check auth first
allow read: if request.auth != null;

// ✅ Specific conditions
allow write: if request.auth.uid == resource.data.userId;

// ✅ Validate data
allow create: if request.resource.data.status in ['PENDING'];

// ✅ Use helper functions
allow read: if userHasRole(request.auth.uid, 'ADMIN');
```

### ❌ DON'T

```firestore
// ❌ Allow public access
allow read, write: if true;

// ❌ Generic condition
allow write: if request.auth != null;

// ❌ Unvalidated writes
allow create: if true;

// ❌ Complex logic in rules (move to Cloud Functions)
allow write: if request.resource.data.customCalculation() > 100;
```

---

## 🔄 Deployment

### 1. Edit rules locally

```bash
# File: backend/firestore.rules
rules_version = '2';
service cloud.firestore {
  // ... rules
}
```

### 2. Deploy to Firebase

```bash
firebase deploy --only firestore:rules
```

### 3. Verify

```bash
# Xem rules hiện tại
firebase rules:list
```

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
