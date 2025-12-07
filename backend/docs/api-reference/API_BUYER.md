# BUYER APIs

> Nhóm API cho khách đặt món (BUYER): xem quán, xem menu, đặt đơn, hủy đơn, lịch sử đơn, áp mã giảm giá.

---

## getRestaurants


**Mô tả:** Lấy danh sách các nhà hàng đang hoạt động trong hệ thống.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** BUYER only

#### Input Schema

```typescript
interface GetRestaurantsRequest {
  city?: string;              // Filter theo thành phố
  category?: string;          // Filter theo loại món (Phở, Cơm, Bún...)
  minRating?: number;         // Filter rating tối thiểu
  isOpen?: boolean;           // Filter quán đang mở cửa
  limit?: number;             // Số lượng kết quả (default: 20)
  startAfter?: string;        // Pagination cursor
}
```

#### Output Schema

```typescript
interface RestaurantList {
  restaurants: Restaurant[];
  hasMore: boolean;           // Còn data để load thêm
  nextCursor?: string;        // Cursor cho page tiếp theo
}

interface Restaurant {
  restaurantId: string;
  name: string;
  address: string;
  city: string;
  category: string[];
  rating: number;
  totalReviews: number;
  isActive: boolean;
  isAcceptingOrders: boolean;
  openingHours: string;
  photoURL: string;
  deliveryFee: number;
  minOrderAmount: number;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = BUYER
2. **Build Query**:
   - Base: `restaurants` collection where `isActive = true`
   - Apply filters (city, category, rating)
   - OrderBy rating DESC
   - Limit results
3. **Execute Query**: Get restaurants từ Firestore
4. **Return List**: Trả về danh sách và pagination info

#### Quy Tắc Phân Quyền

- ✅ **BUYER only**
- ❌ SELLER và SHIPPER không cần API này

#### Request Example

```json
{
  "city": "TP.HCM",
  "category": "Phở",
  "minRating": 4.0,
  "isOpen": true,
  "limit": 10
}
```

#### Response Example

```json
{
  "restaurants": [
    {
      "restaurantId": "rest_001",
      "name": "Phở Hà Nội",
      "address": "123 Nguyễn Huệ, Q1",
      "city": "TP.HCM",
      "category": ["Phở", "Bún"],
      "rating": 4.5,
      "totalReviews": 120,
      "isActive": true,
      "isAcceptingOrders": true,
      "openingHours": "07:00 - 22:00",
      "photoURL": "https://...",
      "deliveryFee": 15000,
      "minOrderAmount": 50000
    }
  ],
  "hasMore": true,
  "nextCursor": "rest_001"
}
```

#### Errors

| Error Code          | Condition        | Message                              |
| ------------------- | ---------------- | ------------------------------------ |
| `unauthenticated`   | Chưa đăng nhập   | "Unauthenticated"                    |
| `permission-denied` | Không phải BUYER | "Only buyers can browse restaurants" |
| `invalid-argument`  | Limit > 100      | "Limit cannot exceed 100"            |
| `internal`          | Lỗi server       | "Failed to get restaurants"          |


---

## getRestaurantMenu


**Mô tả:** Xem menu (danh sách món ăn) của một nhà hàng cụ thể.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** BUYER only

#### Input Schema

```typescript
interface GetMenuRequest {
  restaurantId: string;       // ID nhà hàng
  category?: string;          // Filter theo category
  availableOnly?: boolean;    // Chỉ lấy món available (default: true)
}
```

#### Output Schema

```typescript
interface MenuItemList {
  restaurantId: string;
  restaurantName: string;
  menuItems: MenuItem[];
}

interface MenuItem {
  menuItemId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  photoURL: string;
  available: boolean;
  preparationTime: number;    // Phút
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = BUYER
2. **Validate Restaurant**:
   - Check restaurant tồn tại
   - Check `isActive = true`
3. **Query Menu Items**:
   - Collection: `menuItems`
   - Where: `restaurantId = {restaurantId}`
   - Filter: `available = true` (nếu availableOnly)
   - Filter: category (nếu có)
4. **Return Menu**: Trả về danh sách món

#### Quy Tắc Phân Quyền

- ✅ **BUYER only**
- ✅ Xem menu của mọi restaurant active

#### Request Example

```json
{
  "restaurantId": "rest_001",
  "category": "Phở",
  "availableOnly": true
}
```

#### Response Example

```json
{
  "restaurantId": "rest_001",
  "restaurantName": "Phở Hà Nội",
  "menuItems": [
    {
      "menuItemId": "item_phobotainha",
      "name": "Phở Bò Tái Nạm",
      "description": "Phở bò với tái và nạm",
      "price": 60000,
      "category": "Phở",
      "photoURL": "https://...",
      "available": true,
      "preparationTime": 15
    }
  ]
}
```

#### Errors

| Error Code            | Condition                | Message                       |
| --------------------- | ------------------------ | ----------------------------- |
| `unauthenticated`     | Chưa đăng nhập           | "Unauthenticated"             |
| `permission-denied`   | Không phải BUYER         | "Only buyers can view menus"  |
| `invalid-argument`    | restaurantId rỗng        | "Restaurant ID is required"   |
| `not-found`           | Restaurant không tồn tại | "Restaurant not found"        |
| `failed-precondition` | Restaurant không active  | "Restaurant is not available" |
| `internal`            | Lỗi server               | "Failed to get menu"          |


---

## placeOrder


**Mô tả:** Tạo đơn hàng mới. BUYER chọn món, nhập địa chỉ giao hàng, và tạo order.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** BUYER only (và `isVerified = true`)

#### Input Schema

```typescript
interface PlaceOrderRequest {
  restaurantId: string;
  items: OrderItem[];
  deliveryAddress: Address;
  paymentMethod: "CASH" | "CARD" | "WALLET";
  note?: string;
  promotionCode?: string;
}

interface OrderItem {
  menuItemId: string;
  quantity: number;
  specialInstructions?: string;
}

interface Address {
  street: string;
  district: string;
  city: string;
  coordinates?: { lat: number; lng: number };
}
```

#### Output Schema

```typescript
interface PlaceOrderResponse {
  orderId: string;
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  deliveryFee: number;
  estimatedDeliveryTime: number;  // minutes
  status: "PENDING";
  createdAt: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = BUYER và `isVerified = true`
2. **Validate Input**: Items không rỗng, address đầy đủ
3. **Check Restaurant**: Active và accepting orders
4. **Validate Menu Items**: Tồn tại, available, lấy giá từ DB
5. **Calculate Amounts**:
   - Subtotal từ items
   - Apply promotion (nếu có)
   - Add delivery fee
   - Calculate final amount
6. **Create Order**:
   - Set `status = "PENDING"`
   - Set `paymentStatus = "PENDING"`
   - Set `userId = auth.uid`
   - Save to Firestore
7. **Trigger onOrderCreated**: Auto-trigger sẽ gửi notifications
8. **Return Response**: orderId và amounts

#### Quy Tắc Phân Quyền

- ✅ **BUYER only**
- ✅ Account phải `isVerified = true`
- ✅ Order được tạo với `userId = auth.uid`

#### Request Example

```json
{
  "restaurantId": "rest_001",
  "items": [
    {
      "menuItemId": "item_phobotainha",
      "quantity": 2,
      "specialInstructions": "Không hành"
    }
  ],
  "deliveryAddress": {
    "street": "123 Nguyễn Huệ",
    "district": "Quận 1",
    "city": "TP.HCM",
    "coordinates": { "lat": 10.7769, "lng": 106.7009 }
  },
  "paymentMethod": "CASH",
  "note": "Gọi trước 5 phút",
  "promotionCode": "FREESHIP50"
}
```

#### Response Example

```json
{
  "orderId": "order_20251207_001",
  "totalAmount": 125000,
  "discountAmount": 15000,
  "finalAmount": 110000,
  "deliveryFee": 15000,
  "estimatedDeliveryTime": 30,
  "status": "PENDING",
  "createdAt": "2025-12-07T08:30:00.000Z"
}
```

#### Errors

| Error Code            | Condition                 | Message                                 |
| --------------------- | ------------------------- | --------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập            | "Unauthenticated"                       |
| `permission-denied`   | Không phải BUYER          | "Only buyers can place orders"          |
| `permission-denied`   | Chưa verify               | "Only verified buyers can place orders" |
| `invalid-argument`    | restaurantId rỗng         | "Restaurant ID is required"             |
| `invalid-argument`    | items rỗng                | "Order items are required"              |
| `invalid-argument`    | deliveryAddress thiếu     | "Delivery address is required"          |
| `not-found`           | Restaurant không tồn tại  | "Restaurant not found"                  |
| `failed-precondition` | Restaurant không active   | "Restaurant is not accepting orders"    |
| `not-found`           | Menu item không tồn tại   | "Menu item not found"                   |
| `failed-precondition` | Menu item không available | "Menu item is not available"            |
| `invalid-argument`    | Promotion code sai        | "Invalid promotion code"                |
| `internal`            | Lỗi server                | "Failed to place order"                 |

---

## cancelOrder|


**Mô tả:** Hủy đơn hàng. BUYER chỉ có thể hủy đơn đang ở trạng thái PENDING.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** BUYER only (chủ đơn hàng)

#### Input Schema

```typescript
interface CancelOrderRequest {
  orderId: string;
  reason?: string;
}
```

#### Output Schema

```typescript
interface CancelOrderResponse {
  success: boolean;
  orderId: string;
  previousStatus: string;
  newStatus: "CANCELLED";
  cancelledAt: string;
  refundAmount?: number;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = BUYER
2. **Get Order**: Fetch từ Firestore
3. **Check Ownership**: `order.userId === auth.uid`
4. **Check Status**: Chỉ cancel được nếu `status = "PENDING"`
5. **Update Order**:
   - Set `status = "CANCELLED"`
   - Set `cancelledAt = timestamp`
   - Set `cancelReason = reason`
   - Set `cancelledBy = userId`
6. **Process Refund**: Nếu đã thanh toán, xử lý refund
7. **Send Notifications**: Notify restaurant và shipper (nếu đã gán)
8. **Return Response**: Confirmation

#### Quy Tắc Phân Quyền

- ✅ **BUYER only**
- ✅ Chỉ hủy đơn của chính mình
- ✅ Chỉ hủy được khi `status = "PENDING"`
- ❌ Không hủy được khi seller đã accept hoặc shipper đã nhận

#### Request Example

```json
{
  "orderId": "order_20251207_001",
  "reason": "Đặt nhầm món"
}
```

#### Response Example

```json
{
  "success": true,
  "orderId": "order_20251207_001",
  "previousStatus": "PENDING",
  "newStatus": "CANCELLED",
  "cancelledAt": "2025-12-07T08:45:00.000Z",
  "refundAmount": 0
}
```

#### Errors

| Error Code            | Condition                 | Message                                     |
| --------------------- | ------------------------- | ------------------------------------------- |
| `unauthenticated`     | Chưa đăng nhập            | "Unauthenticated"                           |
| `permission-denied`   | Không phải BUYER          | "Only buyers can cancel orders"             |
| `invalid-argument`    | orderId rỗng              | "Order ID is required"                      |
| `not-found`           | Order không tồn tại       | "Order not found"                           |
| `permission-denied`   | Không phải chủ đơn        | "Unauthorized - Not your order"             |
| `failed-precondition` | Status không phải PENDING | "Cannot cancel order with status: {status}" |
| `failed-precondition` | Đã có shipper             | "Cannot cancel - Order is being delivered"  |
| `internal`            | Lỗi server                | "Failed to cancel order"                    |


---

## getMyOrders


**Mô tả:** Lấy lịch sử đơn hàng của BUYER. Có thể filter theo status.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** BUYER only

#### Input Schema

```typescript
interface GetOrdersRequest {
  status?: "PENDING" | "ACCEPTED" | "PREPARING" | "READY" | "ASSIGNED" | "PICKED_UP" | "DELIVERING" | "COMPLETED" | "CANCELLED";
  limit?: number;               // Default: 20
  startAfter?: string;          // Pagination cursor (orderId)
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
  restaurantId: string;
  restaurantName: string;
  items: OrderItem[];
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  deliveryFee: number;
  deliveryAddress: Address;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  shipperId?: string;
  shipperName?: string;
  estimatedDeliveryTime?: number;
  createdAt: string;
  updatedAt?: string;
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check role = BUYER
2. **Build Query**:
   - Collection: `orders`
   - Where: `userId = auth.uid`
   - Where: `status = {status}` (nếu có filter)
   - OrderBy: `createdAt DESC`
   - Limit: default 20
3. **Fetch Orders**: Get từ Firestore
4. **Populate Data**: Lấy restaurant name, shipper name
5. **Return List**: Orders với pagination

#### Quy Tắc Phân Quyền

- ✅ **BUYER only**
- ✅ Chỉ xem orders của chính mình

#### Request Example

```json
{
  "status": "COMPLETED",
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
      "items": [
        {
          "menuItemId": "item_phobotainha",
          "name": "Phở Bò Tái Nạm",
          "quantity": 2,
          "price": 60000
        }
      ],
      "totalAmount": 125000,
      "discountAmount": 15000,
      "finalAmount": 110000,
      "deliveryFee": 15000,
      "deliveryAddress": {
        "street": "123 Nguyễn Huệ",
        "district": "Quận 1",
        "city": "TP.HCM"
      },
      "status": "COMPLETED",
      "paymentStatus": "COMPLETED",
      "paymentMethod": "CASH",
      "shipperId": "shipper_001",
      "shipperName": "Nguyễn Văn Shipper",
      "createdAt": "2025-12-07T08:30:00Z",
      "updatedAt": "2025-12-07T09:15:00Z"
    }
  ],
  "hasMore": false
}
```

#### Errors

| Error Code          | Condition           | Message                              |
| ------------------- | ------------------- | ------------------------------------ |
| `unauthenticated`   | Chưa đăng nhập      | "Unauthenticated"                    |
| `permission-denied` | Không phải BUYER    | "Only buyers can view order history" |
| `invalid-argument`  | Status không hợp lệ | "Invalid order status"               |
| `invalid-argument`  | Limit > 100         | "Limit cannot exceed 100"            |
| `internal`          | Lỗi server          | "Failed to get orders"               |

---

### applyPromotion

**Mô tả:** Áp dụng mã khuyến mãi (promotion code) và tính toán số tiền giảm giá trước khi đặt hàng.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** BUYER only

#### Input Schema

```typescript
interface ApplyPromotionRequest {
  orderId: string;               // ID đơn hàng
  promotionCode: string;         // Mã khuyến mãi
}
```

#### Output Schema

```typescript
interface ApplyPromotionResponse {
  success: boolean;              // true nếu áp dụng thành công
  promotionId: string;           // ID của promotion
  discountType: "PERCENTAGE" | "FIXED_AMOUNT" | "FREE_DELIVERY";
  discountValue: number;         // Giá trị giảm (% hoặc VND)
  discountAmount: number;        // Số tiền giảm thực tế (VND)
  originalAmount: number;        // Tổng tiền gốc (VND)
  finalAmount: number;           // Tổng tiền sau giảm (VND)
  expiresAt: string;             // Thời hạn promotion (ISO timestamp)
}
```

#### Request Example

```json
{
  "orderId": "order_20251207_001",
  "promotionCode": "FREESHIP50"
}
```

#### Response Example

```json
{
  "success": true,
  "promotionId": "promo_freeship50",
  "discountType": "FREE_DELIVERY",
  "discountValue": 100,
  "discountAmount": 15000,
  "originalAmount": 125000,
  "finalAmount": 110000,
  "expiresAt": "2025-12-31T23:59:59.000Z"
}
```

#### Logic Tóm Tắt

1. **Validate Authentication**: Kiểm tra user đã đăng nhập
2. **Validate Authorization**: Kiểm tra user có role BUYER
3. **Validate Input**: 
   - Kiểm tra orderId không rỗng
   - Kiểm tra promotionCode không rỗng
4. **Get Order**: Lấy order từ Firestore
5. **Check Order Ownership**: User phải là chủ order
6. **Check Order Status**: Order phải ở trạng thái PENDING
7. **Get Promotion**:
   - Query Firestore collection `promotions` với field `code`
   - Kiểm tra promotion có tồn tại
8. **Validate Promotion**:
   - Kiểm tra isActive = true
   - Kiểm tra chưa hết hạn (expiresAt > now)
   - Kiểm tra số lượng còn lại (usageCount < maxUsage)
   - Kiểm tra điều kiện minimum order (minOrderAmount)
   - Kiểm tra user chưa sử dụng promotion này (nếu maxUsagePerUser)
9. **Calculate Discount**:
   - **PERCENTAGE**: discountAmount = orderAmount * (discountValue / 100)
   - **FIXED_AMOUNT**: discountAmount = discountValue
   - **FREE_DELIVERY**: discountAmount = deliveryFee
   - Đảm bảo không vượt quá maxDiscountAmount (nếu có)
10. **Update Order**:
    - Set promotionId
    - Set discountAmount
    - Tính lại finalAmount
11. **Update Promotion**: Tăng usageCount
12. **Return Response**: Trả về thông tin discount

#### Errors

| Error Code            | Condition                          | Message                                                 |
| --------------------- | ---------------------------------- | ------------------------------------------------------- |
| `unauthenticated`     | User chưa đăng nhập                | "Unauthenticated"                                       |
| `permission-denied`   | User không phải BUYER              | "Only buyers can apply promotions"                      |
| `invalid-argument`    | orderId hoặc promotionCode rỗng    | "orderId and promotionCode are required"                |
| `not-found`           | Order không tồn tại                | "Order not found"                                       |
| `permission-denied`   | User không phải chủ order          | "Unauthorized - Not your order"                         |
| `failed-precondition` | Order không ở trạng thái PENDING   | "Cannot apply promotion to order with status: {status}" |
| `not-found`           | Promotion code không tồn tại       | "Invalid promotion code"                                |
| `failed-precondition` | Promotion không active             | "Promotion is not active"                               |
| `failed-precondition` | Promotion đã hết hạn               | "Promotion has expired"                                 |
| `failed-precondition` | Promotion hết lượt sử dụng         | "Promotion usage limit exceeded"                        |
| `failed-precondition` | Order không đủ điều kiện tối thiểu | "Order amount must be at least {minAmount} VND"         |
| `failed-precondition` | User đã sử dụng promotion          | "You have already used this promotion"                  |
| `internal`            | Lỗi server                         | "Failed to apply promotion: {error}"                    |

---