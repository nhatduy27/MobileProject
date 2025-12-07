# SHIPPER APIs

> Nhóm API cho shipper: xem đơn sẵn sàng, nhận đơn, cập nhật trạng thái giao hàng.

---

## getAvailableOrders


**Mô tả:** SHIPPER xem danh sách đơn hàng sẵn sàng để nhận giao (status = READY).

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SHIPPER only

#### Input Schema

```typescript
interface GetOrdersRequest {
  location?: {                // Vị trí hiện tại của shipper
    lat: number;
    lng: number;
  };
  radius?: number;            // Bán kính tìm kiếm (km, default: 5)
  limit?: number;             // Default: 20
}
```

#### Output Schema

```typescript
interface OrderList {
  orders: AvailableOrder[];
  hasMore: boolean;
}

interface AvailableOrder {
  orderId: string;
  restaurantId: string;
  restaurantName: string;
  restaurantAddress: string;
  deliveryAddress: Address;
  totalAmount: number;
  deliveryFee: number;
  distance?: number;          // Khoảng cách từ restaurant đến delivery (km)
  estimatedDeliveryTime: number;
  readyAt: string;
  pickupByTime: string;       // Cần lấy hàng trước giờ này
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SHIPPER
2. **Build Query**:
   - Collection: `orders`
   - Where: `status = "READY"`
   - Where: `shipperId` is null (chưa được gán)
   - OrderBy: `readyAt ASC` (ưu tiên đơn cũ)
3. **Filter by Location** (nếu có):
   - Calculate distance từ shipper location
   - Filter orders trong radius
4. **Fetch Orders**: Get từ Firestore
5. **Populate Data**: Restaurant info, delivery address
6. **Return List**: Orders sẵn sàng

#### Quy Tắc Phân Quyền

- ✅ **SHIPPER only**
- ✅ Chỉ xem orders chưa được gán shipper
- ✅ Có thể filter theo location

#### Request Example

```json
{
  "location": {
    "lat": 10.7769,
    "lng": 106.7009
  },
  "radius": 5,
  "limit": 10
}
```

#### Response Example

```json
{
  "orders": [
    {
      "orderId": "order_20251207_001",
      "restaurantId": "rest_001",
      "restaurantName": "Phở Hà Nội",
      "restaurantAddress": "123 Nguyễn Huệ, Q1",
      "deliveryAddress": {
        "street": "456 Lê Lợi",
        "district": "Quận 1",
        "city": "TP.HCM"
      },
      "totalAmount": 110000,
      "deliveryFee": 15000,
      "distance": 2.5,
      "estimatedDeliveryTime": 20,
      "readyAt": "2025-12-07T08:50:00Z",
      "pickupByTime": "2025-12-07T09:00:00Z"
    }
  ],
  "hasMore": false
}
```

#### Errors

| Error Code          | Condition           | Message                                   |
| ------------------- | ------------------- | ----------------------------------------- |
| `unauthenticated`   | Chưa đăng nhập      | "Unauthenticated"                         |
| `permission-denied` | Không phải SHIPPER  | "Only shippers can view available orders" |
| `invalid-argument`  | Location format sai | "Invalid location format"                 |
| `invalid-argument`  | Radius > 50km       | "Radius cannot exceed 50km"               |
| `internal`          | Lỗi server          | "Failed to get available orders"          |


---

## acceptDelivery


**Mô tả:** SHIPPER nhận đơn giao hàng. READY → ASSIGNED. Order được gán `shipperId`.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SHIPPER only

#### Input Schema

```typescript
interface AcceptDeliveryRequest {
  orderId: string;
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "ASSIGNED";
  shipperId: string;
  shipperName: string;
  assignedAt: string;
  restaurantAddress: string;
  deliveryAddress: Address;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SHIPPER
2. **Get Order**: Fetch từ Firestore
3. **Check Status**: 
   - Order phải `status = "READY"`
   - Order phải chưa có `shipperId` (chưa ai nhận)
4. **Assign Shipper**:
   - Set `shipperId = auth.uid`
   - Set `status = "ASSIGNED"`
   - Set `assignedAt = timestamp`
5. **Send Notifications**:
   - Notify buyer (có shipper rồi)
   - Notify restaurant (shipper đang đến lấy)
6. **Return Response**: Order info với pickup address

#### Quy Tắc Phân Quyền

- ✅ **SHIPPER only**
- ✅ Order phải `status = "READY"`
- ✅ Order chưa được gán shipper khác
- ❌ Không nhận order đã có shipper

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
  "previousStatus": "READY",
  "newStatus": "ASSIGNED",
  "shipperId": "shipper_001",
  "shipperName": "Nguyễn Văn Shipper",
  "assignedAt": "2025-12-07T08:55:00Z",
  "restaurantAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
  "deliveryAddress": {
    "street": "456 Lê Lợi",
    "district": "Quận 1",
    "city": "TP.HCM"
  }
}
```

#### Errors

| Error Code            | Condition               | Message                                              |
| --------------------- | ----------------------- | ---------------------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập          | "Unauthenticated"                                    |
| `permission-denied`   | Không phải SHIPPER      | "Only shippers can accept deliveries"                |
| `invalid-argument`    | orderId rỗng            | "Order ID is required"                               |
| `not-found`           | Order không tồn tại     | "Order not found"                                    |
| `failed-precondition` | Status không phải READY | "Order is not ready for pickup"                      |
| `failed-precondition` | Đã có shipper           | "Order has already been assigned to another shipper" |
| `internal`            | Lỗi server              | "Failed to accept delivery"                          |


---

## updateOrderToPickedUp

**Mô tả:** SHIPPER xác nhận đã lấy hàng từ restaurant. ASSIGNED → PICKED_UP.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SHIPPER only (shipper được gán)

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
  newStatus: "PICKED_UP";
  pickedUpAt: string;
  estimatedArrivalTime: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SHIPPER
2. **Get Order**: Fetch từ Firestore
3. **Check Ownership**: `order.shipperId === auth.uid`
4. **Check Status**: Chỉ update được nếu `status = "ASSIGNED"`
5. **Update Order**:
   - Set `status = "PICKED_UP"`
   - Set `pickedUpAt = timestamp`
   - Calculate `estimatedArrivalTime`
6. **Send Notification**: Notify buyer (shipper đã lấy hàng)
7. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SHIPPER only**
- ✅ Chỉ shipper được gán mới update được
- ✅ Flow: ASSIGNED → PICKED_UP

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
  "previousStatus": "ASSIGNED",
  "newStatus": "PICKED_UP",
  "pickedUpAt": "2025-12-07T09:00:00Z",
  "estimatedArrivalTime": "2025-12-07T09:20:00Z"
}
```

#### Errors

| Error Code            | Condition                   | Message                                    |
| --------------------- | --------------------------- | ------------------------------------------ |
| `unauthenticated`     | Chưa đăng nhập              | "Unauthenticated"                          |
| `permission-denied`   | Không phải SHIPPER          | "Only shippers can update delivery status" |
| `invalid-argument`    | orderId rỗng                | "Order ID is required"                     |
| `not-found`           | Order không tồn tại         | "Order not found"                          |
| `permission-denied`   | Không phải shipper được gán | "Unauthorized - Not assigned to you"       |
| `failed-precondition` | Status không phải ASSIGNED  | "Cannot update from status: {status}"      |
| `internal`            | Lỗi server                  | "Failed to update order"                   |


---

## updateOrderToDelivering


**Mô tả:** SHIPPER đánh dấu đang giao hàng. PICKED_UP → DELIVERING.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SHIPPER only (shipper được gán)

#### Input Schema

```typescript
interface UpdateOrderRequest {
  orderId: string;
  currentLocation?: {
    lat: number;
    lng: number;
  };
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "DELIVERING";
  deliveringAt: string;
  estimatedArrivalTime: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SHIPPER
2. **Get Order**: Fetch từ Firestore
3. **Check Ownership**: `order.shipperId === auth.uid`
4. **Check Status**: Chỉ update được nếu `status = "PICKED_UP"`
5. **Update Order**:
   - Set `status = "DELIVERING"`
   - Set `deliveringAt = timestamp`
   - Set `currentLocation` (nếu có)
6. **Send Notification**: Notify buyer (shipper đang trên đường)
7. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SHIPPER only**
- ✅ Chỉ shipper được gán mới update được
- ✅ Flow: PICKED_UP → DELIVERING

#### Request Example

```json
{
  "orderId": "order_20251207_001",
  "currentLocation": {
    "lat": 10.7750,
    "lng": 106.7020
  }
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "previousStatus": "PICKED_UP",
  "newStatus": "DELIVERING",
  "deliveringAt": "2025-12-07T09:05:00Z",
  "estimatedArrivalTime": "2025-12-07T09:20:00Z"
}
```

#### Errors

| Error Code            | Condition                   | Message                                    |
| --------------------- | --------------------------- | ------------------------------------------ |
| `unauthenticated`     | Chưa đăng nhập              | "Unauthenticated"                          |
| `permission-denied`   | Không phải SHIPPER          | "Only shippers can update delivery status" |
| `invalid-argument`    | orderId rỗng                | "Order ID is required"                     |
| `not-found`           | Order không tồn tại         | "Order not found"                          |
| `permission-denied`   | Không phải shipper được gán | "Unauthorized - Not assigned to you"       |
| `failed-precondition` | Status không phải PICKED_UP | "Cannot update from status: {status}"      |
| `internal`            | Lỗi server                  | "Failed to update order"                   |


---

## completeOrder


**Mô tả:** SHIPPER xác nhận đã giao hàng thành công. DELIVERING → COMPLETED.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** SHIPPER only (shipper được gán)

#### Input Schema

```typescript
interface CompleteOrderRequest {
  orderId: string;
  proofOfDelivery?: string;   // URL ảnh xác nhận giao hàng
  notes?: string;              // Ghi chú
}
```

#### Output Schema

```typescript
interface OrderResponse {
  orderId: string;
  previousStatus: string;
  newStatus: "COMPLETED";
  completedAt: string;
  deliveryTime: number;        // Tổng thời gian giao (minutes)
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = SHIPPER
2. **Get Order**: Fetch từ Firestore
3. **Check Ownership**: `order.shipperId === auth.uid`
4. **Check Status**: Chỉ complete được nếu `status = "DELIVERING"`
5. **Update Order**:
   - Set `status = "COMPLETED"`
   - Set `completedAt = timestamp`
   - Set `proofOfDelivery` (nếu có)
   - Calculate `deliveryTime`
6. **Update Payment**: 
   - Nếu COD, set `paymentStatus = "COMPLETED"`
7. **Update Stats**:
   - Restaurant stats (completed orders)
   - Shipper stats (completed deliveries)
8. **Send Notifications**:
   - Notify buyer (đơn đã giao)
   - Notify restaurant (đơn hoàn thành)
9. **Return Response**

#### Quy Tắc Phân Quyền

- ✅ **SHIPPER only**
- ✅ Chỉ shipper được gán mới complete được
- ✅ Flow: DELIVERING → COMPLETED

#### Request Example

```json
{
  "orderId": "order_20251207_001",
  "proofOfDelivery": "https://storage.../proof.jpg",
  "notes": "Đã giao cho người nhà"
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "previousStatus": "DELIVERING",
  "newStatus": "COMPLETED",
  "completedAt": "2025-12-07T09:20:00Z",
  "deliveryTime": 25
}
```

#### Errors

| Error Code            | Condition                    | Message                                 |
| --------------------- | ---------------------------- | --------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập               | "Unauthenticated"                       |
| `permission-denied`   | Không phải SHIPPER           | "Only shippers can complete deliveries" |
| `invalid-argument`    | orderId rỗng                 | "Order ID is required"                  |
| `not-found`           | Order không tồn tại          | "Order not found"                       |
| `permission-denied`   | Không phải shipper được gán  | "Unauthorized - Not assigned to you"    |
| `failed-precondition` | Status không phải DELIVERING | "Cannot complete from status: {status}" |
| `internal`            | Lỗi server                   | "Failed to complete order"              |

