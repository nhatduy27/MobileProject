# Roles và Permissions - Hệ Thống Food Delivery

> **Tài liệu này định nghĩa các roles trong hệ thống và permissions tương ứng của từng role.**

---

## 📖 Mục Lục

- [Tổng Quan Roles](#-tổng-quan-roles)
- [BUYER - Người Mua](#-buyer---người-mua)
- [SELLER - Chủ Quán](#-seller---chủ-quán)
- [SHIPPER - Người Giao Hàng](#-shipper---người-giao-hàng)
- [Permission Matrix](#-permission-matrix)
- [Backend-Level Permissions](#-backend-level-permissions)
- [Frontend vs Backend Authorization](#-frontend-vs-backend-authorization)
- [Security Best Practices](#-security-best-practices)

---

## 🎭 Tổng Quan Roles

Hệ thống có **3 roles chính**:

| Role | Mô Tả | Custom Claim |
|------|-------|--------------|
| **BUYER** | Người dùng đặt món ăn | `{ role: "BUYER" }` |
| **SELLER** | Chủ quán, quản lý menu và đơn hàng | `{ role: "SELLER" }` |
| **SHIPPER** | Người giao hàng, cập nhật trạng thái giao hàng | `{ role: "SHIPPER" }` |

### Role Assignment

- **Default Role**: Khi user mới đăng ký, mặc định được gán role `BUYER`
- **Role Change**: Chỉ ADMIN có thể thay đổi role của user
- **Custom Claims**: Role được lưu trong Firebase Auth custom claims
- **Firestore Profile**: Role cũng được lưu trong document `users/{userId}`

### Role Verification

```
User Sign Up
    ↓
Auth Trigger: onUserCreated
    ↓
Set Custom Claim: { role: "BUYER" }
    ↓
Create User Profile in Firestore
    ↓
User can access APIs with role-based permissions
```

---

## 👤 BUYER - Người Mua

### Mô Tả
Người dùng cuối sử dụng ứng dụng để đặt món ăn từ các nhà hàng.

### Quyền Xem Dữ Liệu (Read Permissions)

✅ **Được phép xem:**

- **Restaurants**:
  - Tất cả restaurants đang active (`isActive: true`)
  - Thông tin cơ bản: tên, địa chỉ, rating, giờ mở cửa
  - Menu items và giá
  - Reviews và ratings

- **Orders**:
  - **CHỈ** các đơn hàng của chính mình (`order.userId === auth.uid`)
  - Tất cả thông tin của đơn hàng: items, status, amount, delivery address
  - Order history

- **Promotions**:
  - Tất cả promotions đang active và chưa hết hạn
  - Điều kiện áp dụng và discount value

- **User Profile**:
  - Thông tin profile của chính mình
  - Delivery addresses đã lưu
  - Payment methods đã lưu

❌ **KHÔNG được phép xem:**

- Đơn hàng của người dùng khác
- Thông tin tài chính của restaurant (revenue, commission)
- Thông tin cá nhân của shipper (chỉ xem tên và số điện thoại khi đơn đang giao)
- Restaurant's private data (cost, supplier info)
- Admin data và logs

### Quyền Ghi Dữ Liệu (Write Permissions)

✅ **Được phép tạo/cập nhật:**

- **Orders**:
  - **Tạo đơn hàng mới** (placeOrder API)
  - **Hủy đơn hàng** của chính mình nếu status = PENDING
  - **Áp dụng promotion** cho đơn hàng của mình

- **User Profile**:
  - Cập nhật thông tin cá nhân (tên, số điện thoại, avatar)
  - Thêm/xóa/sửa delivery addresses
  - Thêm/xóa payment methods

- **Reviews**:
  - Viết review cho restaurant sau khi order COMPLETED
  - Viết review cho shipper sau khi order DELIVERED
  - Cập nhật/xóa review của chính mình

- **Favorites**:
  - Thêm/xóa restaurants vào danh sách yêu thích

❌ **KHÔNG được phép:**

- Tạo/sửa/xóa restaurant data
- Tạo/sửa menu items
- Cập nhật order status (chỉ cancel được nếu PENDING)
- Xác nhận đơn hàng
- Thay đổi giá hoặc discount không hợp lệ
- Gán shipper cho đơn hàng
- Truy cập admin functions

### Hành Động Cụ Thể

1. **Đặt món ăn**
   - Browse restaurants và menu
   - Thêm items vào cart
   - Áp dụng promotion code
   - Chọn địa chỉ giao hàng
   - Chọn phương thức thanh toán
   - Place order

2. **Quản lý đơn hàng**
   - Xem danh sách orders (chỉ của mình)
   - Xem chi tiết từng order
   - Hủy order (nếu PENDING)
   - Track order status real-time
   - Liên hệ shipper khi đang giao

3. **Tương tác với restaurants**
   - Xem thông tin restaurant
   - Xem menu và giá
   - Xem reviews
   - Viết review sau khi order hoàn thành
   - Lưu restaurants yêu thích

4. **Quản lý profile**
   - Cập nhật thông tin cá nhân
   - Quản lý địa chỉ giao hàng
   - Quản lý payment methods
   - Xem order history
   - Xem promotions available

---

## 🏪 SELLER - Chủ Quán

### Mô Tả
Chủ nhà hàng, quản lý menu, nhận và xử lý đơn hàng từ khách hàng.

### Quyền Xem Dữ Liệu (Read Permissions)

✅ **Được phép xem:**

- **Restaurant Data**:
  - **CHỈ** restaurant của chính mình (`restaurant.ownerId === auth.uid`)
  - Tất cả thông tin restaurant: menu, settings, reviews
  - Restaurant statistics: orders, revenue, ratings

- **Orders**:
  - **CHỈ** orders của restaurant mình (`order.restaurantId === myRestaurantId`)
  - Tất cả thông tin order: items, buyer info, delivery address, status
  - Order history và analytics

- **Menu Items**:
  - Tất cả menu items của restaurant mình
  - Item statistics: số lượng bán, revenue

- **Reviews**:
  - Tất cả reviews về restaurant mình
  - Rating statistics

- **User Profile**:
  - Thông tin cơ bản của buyer (tên, số điện thoại) khi có order
  - **KHÔNG** xem được toàn bộ profile hoặc orders của buyer

❌ **KHÔNG được phép xem:**

- Restaurant data của người khác
- Orders của restaurants khác
- Thông tin chi tiết của buyers (trừ khi có order)
- Thông tin chi tiết của shippers
- Admin data và logs
- Promotions của platform (chỉ xem được promotions mình tạo)

### Quyền Ghi Dữ Liệu (Write Permissions)

✅ **Được phép tạo/cập nhật:**

- **Restaurant**:
  - Cập nhật thông tin restaurant (tên, địa chỉ, giờ mở cửa)
  - Cập nhật avatar/photos
  - Bật/tắt `isAcceptingOrders` (tạm ngưng nhận đơn)
  - Cập nhật delivery settings

- **Menu Items**:
  - Tạo menu items mới
  - Cập nhật thông tin items (tên, giá, mô tả)
  - Cập nhật availability (`available: true/false`)
  - Xóa menu items
  - Tạo categories

- **Orders**:
  - **Xác nhận đơn hàng** (PENDING → CONFIRMED)
  - **Từ chối đơn hàng** (PENDING → REJECTED) với lý do
  - **Đánh dấu đơn đã chuẩn bị xong** (CONFIRMED → READY_FOR_PICKUP)
  - **KHÔNG** được thay đổi order items hoặc amount sau khi confirmed

- **Promotions**:
  - Tạo promotions cho restaurant mình
  - Cập nhật/vô hiệu hóa promotions

❌ **KHÔNG được phép:**

- Tạo/sửa restaurant khác
- Thay đổi giá order sau khi order đã được tạo
- Cập nhật delivery status (việc của shipper)
- Hủy order đã được shipper nhận
- Xóa reviews (chỉ reply được)
- Truy cập admin functions

### Hành Động Cụ Thể

1. **Quản lý restaurant**
   - Cập nhật thông tin quán
   - Upload ảnh quán
   - Set giờ mở cửa
   - Bật/tắt nhận đơn
   - Xem statistics và revenue

2. **Quản lý menu**
   - Thêm/sửa/xóa món ăn
   - Set giá và mô tả
   - Upload ảnh món ăn
   - Bật/tắt availability món ăn
   - Tạo categories

3. **Xử lý đơn hàng**
   - Nhận notification đơn mới
   - Xem chi tiết đơn hàng
   - Xác nhận đơn (CONFIRM)
   - Từ chối đơn với lý do (REJECT)
   - Đánh dấu đã chuẩn bị xong (READY_FOR_PICKUP)
   - Liên hệ buyer nếu cần

4. **Quản lý promotions**
   - Tạo promotion codes
   - Set điều kiện và discount
   - Theo dõi promotion usage

5. **Tương tác với customers**
   - Xem reviews
   - Reply reviews
   - Xem ratings

---

## 🛵 SHIPPER - Người Giao Hàng

### Mô Tả
Người giao hàng, nhận đơn hàng đã sẵn sàng và giao đến địa chỉ khách hàng.

### Quyền Xem Dữ Liệu (Read Permissions)

✅ **Được phép xem:**

- **Orders**:
  - **CHỈ** orders được gán cho mình (`order.shipperId === auth.uid`)
  - Orders với status READY_FOR_PICKUP (để nhận đơn)
  - Thông tin cần thiết: items, delivery address, buyer contact, restaurant address
  - Delivery instructions và notes

- **Restaurants**:
  - Thông tin cơ bản của restaurant (tên, địa chỉ, số điện thoại)
  - **CHỈ** khi có order cần lấy từ restaurant đó

- **Buyers**:
  - Thông tin liên hệ cơ bản (tên, số điện thoại)
  - **CHỈ** khi đang giao order cho buyer đó

- **Shipper Profile**:
  - Thông tin profile của chính mình
  - Delivery history
  - Earnings và statistics

❌ **KHÔNG được phép xem:**

- Orders của shippers khác
- Orders chưa được gán shipper
- Thông tin chi tiết của restaurants (menu, revenue)
- Thông tin chi tiết của buyers (order history, payment info)
- Admin data và logs

### Quyền Ghi Dữ Liệu (Write Permissions)

✅ **Được phép tạo/cập nhật:**

- **Orders**:
  - **Nhận đơn hàng** (READY_FOR_PICKUP → DELIVERING)
    - Hệ thống set `shipperId = auth.uid`
  - **Cập nhật location** real-time khi đang giao
  - **Xác nhận đã lấy hàng** (picked up from restaurant)
  - **Xác nhận đã giao hàng** (DELIVERING → DELIVERED)
  - **Báo cáo vấn đề** (nếu không giao được)

- **Shipper Profile**:
  - Cập nhật thông tin cá nhân
  - Cập nhật vehicle info
  - Bật/tắt availability (online/offline)
  - Cập nhật location

❌ **KHÔNG được phép:**

- Thay đổi order items hoặc amount
- Hủy order (phải báo admin/support)
- Từ chối order sau khi đã nhận (trừ trường hợp đặc biệt)
- Cập nhật restaurant data
- Xem/sửa payment info
- Truy cập admin functions

### Hành Động Cụ Thể

1. **Nhận đơn hàng**
   - Xem danh sách orders READY_FOR_PICKUP gần vị trí
   - Chọn và nhận order
   - Xem thông tin pickup (restaurant address)
   - Xem thông tin delivery (buyer address)

2. **Giao hàng**
   - Navigate đến restaurant để lấy hàng
   - Xác nhận đã lấy hàng
   - Update location real-time
   - Navigate đến địa chỉ buyer
   - Liên hệ buyer nếu cần
   - Xác nhận đã giao hàng thành công

3. **Quản lý profile**
   - Cập nhật thông tin cá nhân
   - Cập nhật vehicle info (xe máy, ô tô)
   - Set status online/offline
   - Xem delivery history
   - Xem earnings

4. **Xử lý vấn đề**
   - Báo cáo vấn đề (buyer không nghe máy, địa chỉ sai)
   - Contact support
   - Upload ảnh proof of delivery

---

## 📊 Permission Matrix

### Quản Lý Orders

| Hành động | Buyer | Seller | Shipper |
|-----------|:-----:|:------:|:-------:|
| Tạo đơn hàng mới | ✅ | ❌ | ❌ |
| Xem đơn hàng của mình | ✅ | ✅ | ✅ |
| Xem tất cả đơn hàng | ❌ | ❌ | ❌ |
| Hủy đơn (PENDING) | ✅ (chủ đơn) | ❌ | ❌ |
| Xác nhận đơn (CONFIRM) | ❌ | ✅ | ❌ |
| Từ chối đơn (REJECT) | ❌ | ✅ | ❌ |
| Đánh dấu sẵn sàng (READY) | ❌ | ✅ | ❌ |
| Nhận giao hàng (DELIVERING) | ❌ | ❌ | ✅ |
| Xác nhận đã giao (DELIVERED) | ❌ | ❌ | ✅ |
| Áp dụng promotion | ✅ | ❌ | ❌ |
| Thay đổi items/amount | ❌ | ❌ | ❌ |

### Quản Lý Restaurants

| Hành động | Buyer | Seller | Shipper |
|-----------|:-----:|:------:|:-------:|
| Xem thông tin restaurant | ✅ | ✅ | ✅ (khi có đơn) |
| Xem menu và giá | ✅ | ✅ | ❌ |
| Tạo/sửa restaurant | ❌ | ✅ (của mình) | ❌ |
| Tạo/sửa/xóa menu items | ❌ | ✅ | ❌ |
| Bật/tắt nhận đơn | ❌ | ✅ | ❌ |
| Xem restaurant revenue | ❌ | ✅ (của mình) | ❌ |

### Quản Lý Users

| Hành động | Buyer | Seller | Shipper |
|-----------|:-----:|:------:|:-------:|
| Xem profile của mình | ✅ | ✅ | ✅ |
| Sửa profile của mình | ✅ | ✅ | ✅ |
| Xem profile người khác | ❌ | ❌ | ❌ |
| Thay đổi role | ❌ | ❌ | ❌ |

### Reviews và Ratings

| Hành động | Buyer | Seller | Shipper |
|-----------|:-----:|:------:|:-------:|
| Viết review cho restaurant | ✅ (sau order) | ❌ | ❌ |
| Viết review cho shipper | ✅ (sau order) | ❌ | ❌ |
| Xem reviews | ✅ | ✅ | ✅ |
| Reply reviews | ❌ | ✅ (của mình) | ❌ |
| Xóa reviews | ✅ (của mình) | ❌ | ❌ |

### Promotions

| Hành động | Buyer | Seller | Shipper |
|-----------|:-----:|:------:|:-------:|
| Xem promotions | ✅ | ✅ | ❌ |
| Áp dụng promotion | ✅ | ❌ | ❌ |
| Tạo promotion | ❌ | ✅ (cho quán) | ❌ |
| Sửa/xóa promotion | ❌ | ✅ (của mình) | ❌ |

---

## 🔒 Backend-Level Permissions

### Firestore Collections Access

#### Collection: `users`

| Role | Read | Write | Conditions |
|------|------|-------|------------|
| BUYER | ✅ Own document | ✅ Own document | `userId === auth.uid` |
| SELLER | ✅ Own document | ✅ Own document | `userId === auth.uid` |
| SHIPPER | ✅ Own document | ✅ Own document | `userId === auth.uid` |

**Rules:**
- User chỉ được đọc/ghi document của chính mình
- Không được thay đổi field `role` (chỉ admin)
- Không được thay đổi field `isVerified` (chỉ backend trigger)

#### Collection: `restaurants`

| Role | Read | Write | Conditions |
|------|------|-------|------------|
| BUYER | ✅ All active | ❌ | `isActive === true` |
| SELLER | ✅ Own restaurant | ✅ Own restaurant | `ownerId === auth.uid` |
| SHIPPER | ✅ When has order | ❌ | Has order from this restaurant |

**Rules:**
- BUYER xem được tất cả restaurants active
- SELLER chỉ được sửa restaurant của mình
- SELLER không được thay đổi `ownerId`
- SHIPPER chỉ xem thông tin cơ bản khi có đơn

#### Collection: `orders`

| Role | Read | Write (Create) | Write (Update) | Conditions |
|------|------|----------------|----------------|------------|
| BUYER | ✅ Own orders | ✅ Create new | ✅ Cancel (PENDING only) | `userId === auth.uid` |
| SELLER | ✅ Restaurant orders | ❌ | ✅ Status update | `restaurantId === myRestaurantId` |
| SHIPPER | ✅ Assigned orders | ❌ | ✅ Delivery status | `shipperId === auth.uid` |

**Rules - BUYER:**
- Chỉ đọc orders của mình (`order.userId === auth.uid`)
- Tạo order mới với `userId = auth.uid`
- Hủy order nếu status = PENDING
- **KHÔNG** được thay đổi `totalAmount`, `restaurantId`, `items` sau khi tạo

**Rules - SELLER:**
- Chỉ đọc orders của restaurant mình (`order.restaurantId === myRestaurantId`)
- Cập nhật status: PENDING → CONFIRMED hoặc REJECTED
- Cập nhật status: CONFIRMED → READY_FOR_PICKUP
- **KHÔNG** được thay đổi `userId`, `items`, `totalAmount`
- **KHÔNG** được cập nhật status khi shipper đã nhận

**Rules - SHIPPER:**
- Chỉ đọc orders được gán cho mình (`order.shipperId === auth.uid`)
- Xem orders READY_FOR_PICKUP để nhận
- Cập nhật status: READY_FOR_PICKUP → DELIVERING (khi nhận đơn)
- Cập nhật status: DELIVERING → DELIVERED (khi giao xong)
- Cập nhật `location` real-time
- **KHÔNG** được thay đổi `items`, `totalAmount`, `userId`, `restaurantId`

#### Collection: `menuItems`

| Role | Read | Write | Conditions |
|------|------|-------|------------|
| BUYER | ✅ All available | ❌ | `available === true` |
| SELLER | ✅ Own items | ✅ Own items | `restaurantId === myRestaurantId` |
| SHIPPER | ❌ | ❌ | - |

**Rules:**
- BUYER xem menu items available của tất cả restaurants
- SELLER tạo/sửa/xóa items của restaurant mình
- SELLER không được thay đổi `restaurantId`

#### Collection: `promotions`

| Role | Read | Write | Conditions |
|------|------|-------|------------|
| BUYER | ✅ Active promotions | ❌ | `isActive === true` |
| SELLER | ✅ Own promotions | ✅ Own promotions | `restaurantId === myRestaurantId` |
| SHIPPER | ❌ | ❌ | - |

**Rules:**
- BUYER xem tất cả promotions active và chưa hết hạn
- SELLER tạo/sửa promotions cho restaurant mình
- SELLER không được thay đổi `usageCount` (do backend trigger update)

#### Collection: `reviews`

| Role | Read | Write (Create) | Write (Update/Delete) | Conditions |
|------|------|----------------|----------------------|------------|
| BUYER | ✅ All | ✅ After order completed | ✅ Own reviews | `userId === auth.uid` |
| SELLER | ✅ Restaurant reviews | ❌ | ✅ Reply only | `restaurantId === myRestaurantId` |
| SHIPPER | ✅ Own reviews | ❌ | ❌ | - |

**Rules:**
- BUYER viết review sau khi order COMPLETED
- BUYER chỉ sửa/xóa reviews của mình
- SELLER xem và reply reviews về restaurant mình
- SELLER không xóa reviews

---

## 🛡️ Frontend vs Backend Authorization

### Phân Quyền FE (Client-Side)

**Mục đích:** UX và giảm unnecessary API calls

**Cách hoạt động:**
- Check role từ Firebase Auth ID token
- Hiển thị/ẩn UI elements dựa trên role
- Prevent user từ việc gọi APIs không có quyền
- Hiển thị error messages thân thiện

**Ví dụ:**
```
// Check role từ ID token
if (currentUser.role === "BUYER") {
  showOrderButton();
} else {
  hideOrderButton();
}

if (currentUser.role === "SELLER") {
  showManageMenuButton();
}
```

**⚠️ LƯU Ý QUAN TRỌNG:**
- FE authorization chỉ để cải thiện UX
- **KHÔNG BAO GIỜ** tin tưởng FE hoàn toàn
- User có thể bypass FE bằng cách modify code
- User có thể gọi API trực tiếp với cURL/Postman

### Phân Quyền Backend (Server-Side)

**Mục đích:** Security và data integrity

**Cách hoạt động:**
- Verify ID token trong mỗi API call
- Check custom claims (role) từ token
- Validate user có quyền thực hiện action
- Reject unauthorized requests

**Ví dụ Logic:**
```
Function: placeOrder
1. Verify ID token → get userId
2. Check role from token claims
3. IF role !== "BUYER":
     throw "permission-denied: Only buyers can place orders"
4. IF user.isVerified !== true:
     throw "permission-denied: Account not verified"
5. Proceed with order creation
```

**Kiểm tra bắt buộc ở Backend:**

1. **Authentication Check**
   - Mọi API phải verify ID token
   - Reject nếu token invalid/expired

2. **Role Check**
   - Check role từ custom claims
   - Reject nếu role không phù hợp với API

3. **Ownership Check**
   - BUYER: order.userId === auth.uid
   - SELLER: order.restaurantId === myRestaurantId
   - SHIPPER: order.shipperId === auth.uid

4. **Status Check**
   - Chỉ cho phép state transitions hợp lệ
   - VD: Không cho BUYER update order status
   - VD: Không cho SELLER update status khi shipper đã nhận

5. **Data Integrity Check**
   - Không cho thay đổi amount/items sau khi order created
   - Validate promotion còn hạn và usage limit
   - Validate menu items available và giá đúng

### Firestore Security Rules

**Layer thứ 3 của security:**

```
Firestore Rules chạy TRƯỚC khi data được đọc/ghi
    ↓
Reject nếu không có quyền
    ↓
Cloud Function trigger chạy SAU khi data đã được ghi
```

**Vai trò:**
- Protect data at database level
- Prevent unauthorized reads/writes
- Backup cho backend authorization
- Validate data schema

**Best Practice:**
- Rules phải match với backend logic
- Không cho phép client writes quan trọng (amount, userId)
- Chỉ cho update fields được phép theo role

---

## 🚨 Security Best Practices

### 1. Never Trust Frontend

❌ **SAI:**
```
// Frontend set totalAmount
const order = {
  items: items,
  totalAmount: calculateTotalOnFrontend(items)  // ❌ CÓ THỂ BỊ CHEAT
};
functions.httpsCallable('placeOrder')(order);
```

✅ **ĐÚNG:**
```
// Backend calculate totalAmount
const order = {
  items: items
  // Không gửi totalAmount từ FE
};
// Backend sẽ tính lại totalAmount từ database prices
functions.httpsCallable('placeOrder')(order);
```

### 2. Validate Ownership

**Mọi read/write operation phải check ownership:**

- BUYER chỉ xem orders của mình
- SELLER chỉ sửa restaurant của mình
- SHIPPER chỉ update orders được gán cho mình

**Backend Logic:**
```
1. Get order from database
2. IF order.userId !== auth.uid:
     throw "permission-denied"
3. Proceed
```

### 3. Validate State Transitions

**Order status flow phải được enforce:**

```
PENDING → CONFIRMED (Seller only)
CONFIRMED → READY_FOR_PICKUP (Seller only)
READY_FOR_PICKUP → DELIVERING (Shipper only)
DELIVERING → DELIVERED (Shipper only)

PENDING → CANCELLED (Buyer only, before confirmed)
```

**Backend check:**
```
IF current status = DELIVERING:
  AND auth.role !== "SHIPPER":
    throw "permission-denied"
```

### 4. Immutable Fields

**Một số fields không được thay đổi sau khi tạo:**

- `order.userId` - Không đổi chủ đơn
- `order.restaurantId` - Không đổi quán
- `order.items` - Không đổi món (sau khi confirmed)
- `order.totalAmount` - Không đổi giá (sau khi confirmed)
- `user.role` - Chỉ admin đổi được

**Firestore Rules enforce:**
```
// Không cho update userId
request.resource.data.userId == resource.data.userId
```

### 5. Rate Limiting

**Prevent spam và abuse:**

- Limit số orders per user per hour
- Limit số promotion applications
- Limit API calls per second

### 6. Audit Logging

**Log tất cả critical operations:**

- Order creation/cancellation
- Status changes
- Role changes
- Promotion usage

**Format log:**
```
{
  action: "ORDER_CANCELLED",
  userId: "user_123",
  orderId: "order_456",
  previousStatus: "PENDING",
  reason: "Changed my mind",
  timestamp: "2025-12-07T10:00:00Z"
}
```

### 7. Data Validation

**Validate tất cả input từ client:**

- Required fields không empty
- Numbers trong range hợp lệ
- Enum values hợp lệ
- IDs tồn tại trong database

---

## 📚 Tài Liệu Liên Quan

- [API_REFERENCE.md](./API_REFERENCE.md) - API documentation
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Error handling patterns
- [FIRESTORE_SCHEMA.md](./FIRESTORE_SCHEMA.md) - Database schema
- [RULES.md](./RULES.md) - Firestore Security Rules chi tiết
- [Firebase Auth Custom Claims](https://firebase.google.com/docs/auth/admin/custom-claims)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
