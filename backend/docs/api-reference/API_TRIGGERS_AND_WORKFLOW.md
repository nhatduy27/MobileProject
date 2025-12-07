# Backend Triggers & Order Workflow

---

## Backend Triggers

### onUserCreated

**Loại:** Auth Trigger (Firebase Authentication)

**Event:** User account được tạo (sign up)

**Trigger Condition:** Khi có user mới đăng ký thông qua Firebase Authentication

#### Mô Tả

Trigger này tự động chạy khi có user account mới được tạo trong Firebase Authentication. Nhiệm vụ chính là tạo user profile document trong Firestore và set custom claims cho user.

#### Logic Xử Lý

1. **Detect User Creation**:
   - Trigger được kích hoạt khi user sign up
   - Nhận được user object từ Firebase Auth

2. **Extract User Data**:
   - userId (UID)
   - email
   - displayName (nếu có)
   - photoURL (nếu có)
   - phoneNumber (nếu có)

3. **Determine User Role**:
   - Mặc định: role = "BUYER"
   - Nếu có metadata từ client: dùng role từ metadata
   - Validate role thuộc ["BUYER", "SELLER", "SHIPPER", "ADMIN"]

4. **Create User Profile**:
   - Tạo document trong collection `users` với ID = userId
   - Fields:
     - `email`: Email của user
     - `displayName`: Tên hiển thị
     - `role`: Role của user
     - `isActive`: true
     - `isVerified`: false (cần verify sau)
     - `phoneNumber`: Số điện thoại (nếu có)
     - `photoURL`: Avatar URL (nếu có)
     - `createdAt`: Server timestamp
     - `updatedAt`: Server timestamp

5. **Set Custom Claims**:
   - Gọi `admin.auth().setCustomUserClaims(userId, { role })`
   - Claims này sẽ có trong ID token của user
   - Dùng để authorization trong các API calls

6. **Send Welcome Notification**:
   - Gọi notification service
   - Gửi welcome email/notification cho user mới

7. **Initialize User Stats**:
   - Tạo document trong collection `userStats`
   - Fields: orderCount = 0, totalSpent = 0, etc.

8. **Log Event**:
   - Log thông tin user creation
   - Track analytics event

#### Tác Động Hệ Thống

- ✅ **User Profile**: Tạo profile đầy đủ trong Firestore
- ✅ **Authorization**: Set custom claims cho role-based access
- ✅ **Notification**: Gửi welcome message
- ✅ **Stats**: Initialize statistics tracking
- ✅ **Audit Trail**: Log user creation event

#### Error Handling

- Nếu tạo profile thất bại: Log error, retry logic
- Nếu set claims thất bại: Log error, user vẫn tồn tại nhưng chưa có role
- Không throw error để không block user creation flow

#### Example Log Output

```
[onUserCreated] User created: test_buyer_123
[onUserCreated] Creating user profile in Firestore
[onUserCreated] Setting custom claims: { role: 'BUYER' }
[onUserCreated] Sending welcome notification
[onUserCreated] User profile created successfully
```

---

### onOrderCreated

**Loại:** Firestore Trigger (Document Created)

**Event:** Order document được tạo trong collection `orders`

**Trigger Condition:** Khi có document mới trong `orders/{orderId}`

#### Mô Tả

Trigger này tự động chạy khi có đơn hàng mới được tạo trong Firestore. Nhiệm vụ chính là gửi notifications cho các bên liên quan và cập nhật statistics.

#### Logic Xử Lý

1. **Detect Order Creation**:
   - Trigger được kích hoạt khi document mới trong collection `orders`
   - Nhận được order data và orderId

2. **Extract Order Information**:
   - `orderId`: ID của order
   - `userId`: ID người đặt hàng
   - `restaurantId`: ID nhà hàng
   - `items`: Danh sách món ăn
   - `totalAmount`: Tổng tiền
   - `status`: Trạng thái (PENDING)

3. **Get Related Data**:
   - Lấy user document từ `users/{userId}`
   - Lấy restaurant document từ `restaurants/{restaurantId}`
   - Lấy thông tin món ăn từ menu items

4. **Send Notifications**:
   
   **a) Notification cho Buyer:**
   - Tiêu đề: "Đơn hàng đã được đặt"
   - Nội dung: "Đơn hàng #{orderId} đang chờ xử lý"
   - Action: Link đến order detail

   **b) Notification cho Restaurant (Seller):**
   - Tiêu đề: "Đơn hàng mới"
   - Nội dung: "Bạn có đơn hàng mới #{orderId} từ {buyerName}"
   - Action: Link đến order management
   - Priority: HIGH (cần xử lý ngay)

   **c) Push Notification:**
   - Gửi FCM notification đến device của user
   - Gửi FCM notification đến restaurant's devices

5. **Update Restaurant Stats**:
   - Cập nhật document `restaurantStats/{restaurantId}`
   - Tăng `pendingOrderCount`
   - Tăng `totalOrderCount`
   - Update `lastOrderAt` timestamp

6. **Update User Stats**:
   - Cập nhật document `userStats/{userId}`
   - Tăng `orderCount`
   - Cập nhật `lastOrderAt` timestamp

7. **Create Activity Log**:
   - Tạo document trong collection `activityLogs`
   - Log order creation event với timestamp

8. **Trigger Analytics**:
   - Track event "order_created"
   - Gửi metrics: order value, items count, restaurant

9. **Check Promotions**:
   - Nếu order có promotionId, update promotion usage stats
   - Tăng promotion.usageCount

#### Tác Động Hệ Thống

- 📧 **Notifications**: Buyer và Seller nhận thông báo real-time
- 📊 **Statistics**: Restaurant và User stats được cập nhật
- 📝 **Activity Logs**: Order event được ghi nhận
- 📈 **Analytics**: Tracking metrics cho business intelligence
- 🎁 **Promotions**: Usage tracking cho campaigns

#### Error Handling

- Nếu gửi notification thất bại: Log error, không block flow
- Nếu update stats thất bại: Retry với exponential backoff
- Nếu critical operation fail: Log error và alert admin

#### Example Log Output

```
[onOrderCreated] Processing order: order_20251207_001
[onOrderCreated] Order amount: 125000 VND
[onOrderCreated] Sending notification to buyer: user_123
[onOrderCreated] Sending notification to restaurant: rest_001
[onOrderCreated] Updating restaurant stats
[onOrderCreated] Updating user stats
[onOrderCreated] Order creation processing completed
```

#### Performance Considerations

- Notifications được gửi async (không block)
- Stats updates được batch khi có thể
- Sử dụng Firestore transactions cho critical updates
- Retry logic cho failed operations

---

### onOrderStatusChanged

**Loại:** Firestore Trigger (Document Updated)

**Event:** Order document được cập nhật trong collection `orders`

**Trigger Condition:** Khi field `status` trong `orders/{orderId}` thay đổi

#### Mô Tả

Trigger này tự động chạy khi trạng thái đơn hàng thay đổi. Nhiệm vụ chính là gửi notifications cho các bên liên quan, cập nhật statistics, và xử lý business logic phụ thuộc vào status transition.

#### Logic Xử Lý

1. **Detect Status Change**:
   - Trigger được kích hoạt khi document trong `orders` update
   - So sánh `before.data().status` vs `after.data().status`
   - Chỉ xử lý nếu status thực sự thay đổi

2. **Extract Order Data**:
   - `orderId`: ID của order
   - `previousStatus`: Status cũ
   - `newStatus`: Status mới
   - `userId`: Buyer ID
   - `restaurantId`: Restaurant ID
   - `shipperId`: Shipper ID (nếu có)

3. **Status Transition Logic**:

   **a) PENDING → ACCEPTED:**
   - Notify buyer: "Đơn hàng đã được chấp nhận"
   - Update restaurant stats: decrease pendingOrderCount

   **b) PENDING → REJECTED:**
   - Notify buyer: "Đơn hàng bị từ chối: {reason}"
   - Process refund if payment completed
   - Update restaurant stats

   **c) PENDING → CANCELLED:**
   - Notify restaurant: "Đơn hàng bị hủy bởi khách"
   - Process refund if needed

   **d) ACCEPTED → PREPARING:**
   - Notify buyer: "Quán đang chuẩn bị món"
   - Update estimated ready time

   **e) PREPARING → READY:**
   - Notify buyer: "Món đã sẵn sàng, đang tìm shipper"
   - Broadcast to nearby shippers (push notification)
   - Create notification for shipper matching

   **f) READY → ASSIGNED:**
   - Notify buyer: "Đã có shipper nhận đơn: {shipperName}"
   - Notify restaurant: "Shipper {shipperName} đang đến lấy hàng"
   - Notify shipper: "Bạn đã nhận đơn #{orderId}"

   **g) ASSIGNED → PICKED_UP:**
   - Notify buyer: "Shipper đã lấy hàng, đang trên đường giao"
   - Notify restaurant: "Đơn hàng đã được lấy"

   **h) PICKED_UP → DELIVERING:**
   - Notify buyer: "Đơn hàng đang được giao đến bạn"
   - Send live tracking link

   **i) DELIVERING → COMPLETED:**
   - Notify buyer: "Đơn hàng đã giao thành công"
   - Notify restaurant: "Đơn hàng #{orderId} hoàn thành"
   - Update payment status to COMPLETED (if COD)
   - Update all related stats:
     - Restaurant: completedOrderCount, totalRevenue
     - Shipper: completedDeliveryCount, totalEarnings
     - User: completedOrderCount, totalSpent
   - Trigger review request notification

4. **Send Notifications**:
   - Determine target users (buyer, seller, shipper)
   - Prepare notification content based on status
   - Send FCM push notifications
   - Create in-app notification records

5. **Update Statistics**:
   - Update `orderStats` collection
   - Update `restaurantStats`
   - Update `shipperStats` (if applicable)
   - Update `userStats`

6. **Log Activity**:
   - Create activity log entry
   - Track status transition timing
   - Calculate SLA metrics

7. **Business Rules Enforcement**:
   - Validate status transitions are allowed
   - Check time constraints (e.g., READY orders not picked up after 30 mins)
   - Trigger alerts for anomalies

#### Status Transition Matrix

| From Status | To Status  | Triggered By | Notifications              |
| ----------- | ---------- | ------------ | -------------------------- |
| PENDING     | ACCEPTED   | SELLER       | Buyer                      |
| PENDING     | REJECTED   | SELLER       | Buyer                      |
| PENDING     | CANCELLED  | BUYER        | Restaurant                 |
| ACCEPTED    | PREPARING  | SELLER       | Buyer                      |
| PREPARING   | READY      | SELLER       | Buyer, Shippers            |
| READY       | ASSIGNED   | SHIPPER      | Buyer, Restaurant, Shipper |
| ASSIGNED    | PICKED_UP  | SHIPPER      | Buyer, Restaurant          |
| PICKED_UP   | DELIVERING | SHIPPER      | Buyer                      |
| DELIVERING  | COMPLETED  | SHIPPER      | Buyer, Restaurant          |

#### Tác Động Hệ Thống

- 📧 **Real-time Notifications**: Tất cả stakeholders được thông báo ngay lập tức
- 📊 **Statistics Updates**: Stats được cập nhật theo thời gian thực
- 💳 **Payment Processing**: Auto-complete payment khi order COMPLETED
- 📝 **Activity Tracking**: Mọi thay đổi được log đầy đủ
- 🔔 **Push Notifications**: FCM notifications gửi đến mobile devices
- 📈 **Analytics Events**: Track conversion funnel và SLA metrics

#### Error Handling

- Nếu notification thất bại: Retry 3 lần, sau đó log error
- Nếu stats update thất bại: Queue for retry
- Nếu payment processing fail: Alert admin, manual intervention needed
- Invalid status transition: Log error, prevent update

#### Example Log Output

```
[onOrderStatusChanged] Order: order_20251207_001
[onOrderStatusChanged] Status changed: PREPARING → READY
[onOrderStatusChanged] Sending notification to buyer: user_123
[onOrderStatusChanged] Broadcasting to 12 nearby shippers
[onOrderStatusChanged] Updating restaurant stats
[onOrderStatusChanged] Status change processing completed in 450ms
```

#### Performance Considerations

- Batch notifications when possible
- Use Firestore transactions for stats updates
- Cache frequently accessed data (user profiles, restaurant info)
- Async processing for non-critical operations
- Rate limiting for external API calls (FCM)


---

## Order Status Workflow


### Status Flow Diagram

```
                    BUYER Actions              SELLER Actions            SHIPPER Actions
                          |                          |                          |
                          v                          v                          v
                    ┌─────────┐                                                  
                    │ PENDING │ ◄─── Order Created (placeOrder API)             
                    └────┬────┘                                                  
                         │                                                       
         ┌───────────────┼───────────────┐                                      
         │               │               │                                      
         v               v               v                                      
    CANCELLED       ACCEPTED         REJECTED                                   
    (Buyer)         (Seller)         (Seller)                                   
                         │                                                       
                         v                                                       
                   ┌──────────┐                                                 
                   │PREPARING │ ◄─── Seller preparing food                      
                   └────┬─────┘                                                 
                        │                                                        
                        v                                                        
                   ┌────────┐                                                   
                   │ READY  │ ◄─── Food ready, waiting for shipper              
                   └────┬───┘                                                   
                        │                                                        
                        v                                                        
                   ┌─────────┐                                                  
                   │ASSIGNED │ ◄─── Shipper accepted delivery                   
                   └────┬────┘                                                  
                        │                                                        
                        v                                                        
                  ┌──────────┐                                                  
                  │PICKED_UP │ ◄─── Shipper picked up from restaurant           
                  └────┬─────┘                                                  
                       │                                                         
                       v                                                         
                 ┌────────────┐                                                 
                 │ DELIVERING │ ◄─── Shipper on the way to customer             
                 └─────┬──────┘                                                 
                       │                                                         
                       v                                                         
                  ┌──────────┐                                                  
                  │COMPLETED │ ◄─── Successfully delivered                      
                  └──────────┘                                                  
```

### Status Definitions

| Status         | Mô Tả                               | Ai Tạo  | Ai Có Thể Xem | Thời Gian Trung Bình |
| -------------- | ----------------------------------- | ------- | ------------- | -------------------- |
| **PENDING**    | Đơn hàng mới tạo, chờ quán xác nhận | BUYER   | All           | 0-5 mins             |
| **ACCEPTED**   | Quán đã chấp nhận, sẽ bắt đầu làm   | SELLER  | All           | 5-10 mins            |
| **PREPARING**  | Quán đang chuẩn bị món              | SELLER  | All           | 10-30 mins           |
| **READY**      | Món đã sẵn sàng, chờ shipper lấy    | SELLER  | All           | 0-10 mins            |
| **ASSIGNED**   | Đã có shipper nhận đơn              | SHIPPER | All           | 0-5 mins             |
| **PICKED_UP**  | Shipper đã lấy hàng từ quán         | SHIPPER | All           | 5-10 mins            |
| **DELIVERING** | Shipper đang giao hàng đến khách    | SHIPPER | All           | 10-30 mins           |
| **COMPLETED**  | Giao hàng thành công                | SHIPPER | All           | -                    |
| **CANCELLED**  | Khách hủy đơn                       | BUYER   | All           | -                    |
| **REJECTED**   | Quán từ chối đơn                    | SELLER  | All           | -                    |

### Allowed Transitions

#### BUYER Can Change:
- `PENDING` → `CANCELLED` (chỉ khi chưa được accept)

#### SELLER Can Change:
- `PENDING` → `ACCEPTED` (chấp nhận đơn)
- `PENDING` → `REJECTED` (từ chối đơn)
- `ACCEPTED` → `PREPARING` (bắt đầu làm)
- `PREPARING` → `READY` (món đã xong)

#### SHIPPER Can Change:
- `READY` → `ASSIGNED` (nhận đơn giao)
- `ASSIGNED` → `PICKED_UP` (đã lấy hàng)
- `PICKED_UP` → `DELIVERING` (đang giao)
- `DELIVERING` → `COMPLETED` (giao xong)

### Terminal States

Các status không thể chuyển sang status khác:
- ✅ **COMPLETED** - Đơn hàng hoàn thành thành công
- ❌ **CANCELLED** - Đơn hàng đã bị hủy bởi buyer
- ❌ **REJECTED** - Đơn hàng bị từ chối bởi seller

### Business Rules

1. **Time Constraints:**
   - PENDING orders not accepted within 10 minutes → Auto-cancel
   - READY orders not picked up within 30 minutes → Alert restaurant
   - DELIVERING orders taking > 60 minutes → Alert support team

2. **Validation Rules:**
   - Cannot skip status (must follow flow)
   - Cannot change terminal states
   - BUYER can only cancel PENDING orders
   - SHIPPER cannot change status of orders not assigned to them

3. **Payment Rules:**
   - Payment captured when status → ACCEPTED
   - Refund triggered when status → CANCELLED or REJECTED
   - Payment completed when status → COMPLETED (for COD)

4. **Notification Rules:**
   - All status changes trigger notifications
   - Critical statuses (READY, ASSIGNED) trigger push notifications
   - Buyers receive email for terminal states

