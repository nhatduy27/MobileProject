# SELLER APIs

> Nhóm API cho chủ quán (SELLER): xem đơn, nhận / từ chối / cập nhật trạng thái chuẩn bị món.

---

## getRestaurantOrders


**Mô tả:** SELLER xem danh sách đơn hàng của restaurant mình. Có thể filter theo status.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SELLER only (chủ restaurant)

#### Input Schema

```typescript
interface GetOrdersRequest {
  restaurantId: string;
  status?: "PENDING" | "ACCEPTED" | "PREPARING" | "READY" | "ASSIGNED" | "CANCELLED";
  limit?: number;
  startAfter?: string;
}
```

#### Output Schema

```typescript
interface OrderList {
  orders: Order[];
  hasMore: boolean;
  nextCursor?: string;
}

interface Order {
  orderId: string;
  userId: string;
  buyerName: string;
  buyerPhone: string;
  items: OrderItem[];
  totalAmount: number;
  finalAmount: number;
  deliveryAddress: Address;
  status: string;
  paymentMethod: string;
  shipperId?: string;
  note?: string;
  createdAt: string;
  updatedAt?: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SELLER
2. **Verify Ownership**:
   - Get restaurant from Firestore
   - Check `restaurant.ownerId === auth.uid`
3. **Build Query**:
   - Where: `restaurantId = {restaurantId}`
   - Where: `status = {status}` (nếu có)
   - OrderBy: `createdAt DESC`
4. **Fetch Orders**: Get từ Firestore
5. **Populate Buyer Info**: Lấy buyer name, phone
6. **Return List**

#### Quy Tắc Phân Quyền

- ✅ **SELLER only**
- ✅ Chỉ xem orders của restaurant mình
- ❌ Không xem orders của restaurant khác

#### Request Example

```json
{
  "restaurantId": "rest_001",
  "status": "PENDING",
  "limit": 20
}
```

#### Response Example

```json
{
  "orders": [
    {
      "orderId": "order_20251207_001",
      "userId": "user_abc123",
      "buyerName": "Nguyễn Văn A",
      "buyerPhone": "0901234567",
      "items": [
        {
          "menuItemId": "item_phobotainha",
          "name": "Phở Bò Tái Nạm",
          "quantity": 2,
          "price": 60000,
          "specialInstructions": "Không hành"
        }
      ],
      "totalAmount": 125000,
      "finalAmount": 110000,
      "deliveryAddress": {
        "street": "123 Nguyễn Huệ",
        "district": "Quận 1",
        "city": "TP.HCM"
      },
      "status": "PENDING",
      "paymentMethod": "CASH",
      "note": "Gọi trước 5 phút",
      "createdAt": "2025-12-07T08:30:00Z"
    }
  ],
  "hasMore": false
}
```

#### Errors

| Error Code          | Condition                 | Message                                   |
| ------------------- | ------------------------- | ----------------------------------------- |
| `unauthenticated`   | Chưa đăng nhập            | "Unauthenticated"                         |
| `permission-denied` | Không phải SELLER         | "Only sellers can view restaurant orders" |
| `invalid-argument`  | restaurantId rỗng         | "Restaurant ID is required"               |
| `not-found`         | Restaurant không tồn tại  | "Restaurant not found"                    |
| `permission-denied` | Không phải chủ restaurant | "Unauthorized - Not your restaurant"      |
| `internal`          | Lỗi server                | "Failed to get orders"                    |


---

## acceptOrder


**Mô tả:** SELLER chấp nhận đơn hàng mới. Chuyển status từ PENDING → ACCEPTED.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SELLER only (chủ restaurant)

#### Input Schema

```typescript
interface AcceptOrderRequest {
  orderId: string;
  estimatedPreparationTime?: number;  // Phút (default: 15)
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "ACCEPTED";
  estimatedReadyTime: string;
  updatedAt: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SELLER
2. **Get Order**: Fetch từ Firestore
3. **Verify Restaurant Ownership**:
   - Check `order.restaurantId` thuộc seller
4. **Check Status**: Chỉ accept được nếu `status = "PENDING"`
5. **Update Order**:
   - Set `status = "ACCEPTED"`
   - Set `acceptedAt = timestamp`
   - Set `acceptedBy = userId`
   - Calculate `estimatedReadyTime`
6. **Send Notification**: Notify buyer
7. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SELLER only**
- ✅ Chỉ accept orders của restaurant mình
- ✅ Chỉ accept được khi `status = "PENDING"`

#### Request Example

```json
{
  "orderId": "order_20251207_001",
  "estimatedPreparationTime": 20
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "previousStatus": "PENDING",
  "newStatus": "ACCEPTED",
  "estimatedReadyTime": "2025-12-07T08:50:00Z",
  "updatedAt": "2025-12-07T08:30:00Z"
}
```

#### Errors

| Error Code            | Condition                      | Message                                      |
| --------------------- | ------------------------------ | -------------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập                 | "Unauthenticated"                            |
| `permission-denied`   | Không phải SELLER              | "Only sellers can accept orders"             |
| `invalid-argument`    | orderId rỗng                   | "Order ID is required"                       |
| `not-found`           | Order không tồn tại            | "Order not found"                            |
| `permission-denied`   | Không phải restaurant của mình | "Unauthorized - Not your restaurant's order" |
| `failed-precondition` | Status không phải PENDING      | "Cannot accept order with status: {status}"  |
| `internal`            | Lỗi server                     | "Failed to accept order"                     |


---

## rejectOrder


**Mô tả:** SELLER từ chối đơn hàng. Chuyển status từ PENDING → REJECTED.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SELLER only (chủ restaurant)

#### Input Schema

```typescript
interface RejectOrderRequest {
  orderId: string;
  reason: string;             // Bắt buộc phải có lý do
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "REJECTED";
  rejectionReason: string;
  rejectedAt: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SELLER
2. **Validate Input**: Reason không rỗng
3. **Get Order**: Fetch từ Firestore
4. **Verify Ownership**: Restaurant thuộc seller
5. **Check Status**: Chỉ reject được nếu `status = "PENDING"`
6. **Update Order**:
   - Set `status = "REJECTED"`
   - Set `rejectionReason = reason`
   - Set `rejectedAt = timestamp`
   - Set `rejectedBy = userId`
7. **Process Refund**: Nếu đã thanh toán
8. **Send Notification**: Notify buyer
9. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SELLER only**
- ✅ Chỉ reject orders của restaurant mình
- ✅ Chỉ reject được khi `status = "PENDING"`

#### Request Example

```json
{
  "orderId": "order_20251207_001",
  "reason": "Hết nguyên liệu"
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "previousStatus": "PENDING",
  "newStatus": "REJECTED",
  "rejectionReason": "Hết nguyên liệu",
  "rejectedAt": "2025-12-07T08:35:00Z"
}
```

#### Errors

| Error Code            | Condition                      | Message                                      |
| --------------------- | ------------------------------ | -------------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập                 | "Unauthenticated"                            |
| `permission-denied`   | Không phải SELLER              | "Only sellers can reject orders"             |
| `invalid-argument`    | orderId rỗng                   | "Order ID is required"                       |
| `invalid-argument`    | reason rỗng                    | "Rejection reason is required"               |
| `not-found`           | Order không tồn tại            | "Order not found"                            |
| `permission-denied`   | Không phải restaurant của mình | "Unauthorized - Not your restaurant's order" |
| `failed-precondition` | Status không phải PENDING      | "Cannot reject order with status: {status}"  |
| `internal`            | Lỗi server                     | "Failed to reject order"                     |


---

## updateOrderToPreparing


**Mô tả:** SELLER đánh dấu đơn hàng đang chuẩn bị. ACCEPTED → PREPARING.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SELLER only (chủ restaurant)

#### Input Schema

```typescript
interface UpdateOrderRequest {
  orderId: string;
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "PREPARING";
  updatedAt: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SELLER
2. **Get Order**: Fetch từ Firestore
3. **Verify Ownership**: Restaurant thuộc seller
4. **Check Status**: Chỉ update được nếu `status = "ACCEPTED"`
5. **Update Order**:
   - Set `status = "PREPARING"`
   - Set `preparingAt = timestamp`
6. **Send Notification**: Notify buyer (optional)
7. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SELLER only**
- ✅ Chỉ update orders của restaurant mình
- ✅ Flow: ACCEPTED → PREPARING

#### Request Example

```json
{
  "orderId": "order_20251207_001"
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "previousStatus": "ACCEPTED",
  "newStatus": "PREPARING",
  "updatedAt": "2025-12-07T08:35:00Z"
}
```

#### Errors

| Error Code            | Condition                      | Message                                |
| --------------------- | ------------------------------ | -------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập                 | "Unauthenticated"                      |
| `permission-denied`   | Không phải SELLER              | "Only sellers can update order status" |
| `invalid-argument`    | orderId rỗng                   | "Order ID is required"                 |
| `not-found`           | Order không tồn tại            | "Order not found"                      |
| `permission-denied`   | Không phải restaurant của mình | "Unauthorized"                         |
| `failed-precondition` | Status không phải ACCEPTED     | "Cannot update from status: {status}"  |
| `internal`            | Lỗi server                     | "Failed to update order"               |


---

## updateOrderToReady


**Mô tả:** SELLER đánh dấu đơn hàng đã chuẩn bị xong, sẵn sàng cho shipper lấy. PREPARING → READY.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SELLER only (chủ restaurant)

#### Input Schema

```typescript
interface UpdateOrderRequest {
  orderId: string;
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "READY";
  readyAt: string;
  updatedAt: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SELLER
2. **Get Order**: Fetch từ Firestore
3. **Verify Ownership**: Restaurant thuộc seller
4. **Check Status**: Chỉ update được nếu `status = "PREPARING"`
5. **Update Order**:
   - Set `status = "READY"`
   - Set `readyAt = timestamp`
6. **Notify Shippers**: Broadcast đơn hàng sẵn sàng cho shippers gần đó
7. **Send Notification**: Notify buyer
8. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SELLER only**
- ✅ Chỉ update orders của restaurant mình
- ✅ Flow: PREPARING → READY

#### Request Example

```json
{
  "orderId": "order_20251207_001"
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "previousStatus": "PREPARING",
  "newStatus": "READY",
  "readyAt": "2025-12-07T08:50:00Z",
  "updatedAt": "2025-12-07T08:50:00Z"
}
```

#### Errors

| Error Code            | Condition                      | Message                                |
| --------------------- | ------------------------------ | -------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập                 | "Unauthenticated"                      |
| `permission-denied`   | Không phải SELLER              | "Only sellers can update order status" |
| `invalid-argument`    | orderId rỗng                   | "Order ID is required"                 |
| `not-found`           | Order không tồn tại            | "Order not found"                      |
| `permission-denied`   | Không phải restaurant của mình | "Unauthorized"                         |
| `failed-precondition` | Status không phải PREPARING    | "Cannot update from status: {status}"  |
| `internal`            | Lỗi server                     | "Failed to update order"               |

