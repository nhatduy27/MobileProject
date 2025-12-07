# Thiết Kế Firestore Schema

## 📚 Tổng Quan Cấu Trúc Dữ Liệu

Firestore là NoSQL database của Firebase. Dữ liệu được tổ chức theo **collections** (bảng) và **documents** (bản ghi).

```
Firestore
├── users/                    ← Collection
│   ├── user_001/            ← Document
│   │   ├── email            ← Fields
│   │   ├── displayName
│   │   ├── role
│   │   └── ...
│   ├── user_002/
│   └── ...
├── restaurants/
├── menuItems/
├── orders/
└── promotions/
```

---

## 🔑 Collections & Schema

### 1. 📱 Collection: `users`

**Mục đích:** Lưu thông tin user của ứng dụng

**Document ID:** `uid` (từ Firebase Auth)

**Fields:**

| Field | Type | Bắt Buộc | Mô Tả |
|-------|------|---------|-------|
| `email` | `string` | ✅ | Email đăng nhập |
| `displayName` | `string` | ❌ | Tên hiển thị |
| `phoneNumber` | `string` | ❌ | Số điện thoại |
| `role` | `string` | ✅ | `BUYER` \| `SELLER` \| `SHIPPER` |
| `avatarUrl` | `string` | ❌ | URL ảnh đại diện |
| `createdAt` | `timestamp` | ✅ | Ngày tạo tài khoản |
| `updatedAt` | `timestamp` | ❌ | Lần cập nhật cuối |
| `isActive` | `boolean` | ✅ | Tài khoản có hoạt động |
| `isVerified` | `boolean` | ✅ | Email đã xác minh |
| `addresses` | `array` | ❌ | Danh sách địa chỉ giao hàng |

**Ví dụ Document:**

```json
// Document ID: "user_abc123xyz"
{
  "email": "nguyenvana@gmail.com",
  "displayName": "Nguyễn Văn A",
  "phoneNumber": "+84912345678",
  "role": "BUYER",
  "avatarUrl": "https://storage.googleapis.com/foodapp/avatars/user_abc123xyz.jpg",
  "createdAt": {
    "_seconds": 1701945000,
    "_nanoseconds": 0
  },
  "updatedAt": {
    "_seconds": 1733572500,
    "_nanoseconds": 0
  },
  "isActive": true,
  "isVerified": true,
  "addresses": [
    {
      "id": "addr_001",
      "label": "Nhà riêng",
      "recipientName": "Nguyễn Văn A",
      "recipientPhone": "+84912345678",
      "street": "123 Nguyễn Huệ",
      "ward": "Bến Nghé",
      "district": "Quận 1",
      "city": "TP. Hồ Chí Minh",
      "zipCode": "700000",
      "latitude": 10.7769,
      "longitude": 106.7009,
      "isDefault": true,
      "deliveryInstructions": "Gọi trước 5 phút"
    },
    {
      "id": "addr_002",
      "label": "Văn phòng",
      "recipientName": "Nguyễn Văn A",
      "recipientPhone": "+84912345678",
      "street": "45 Lê Lợi",
      "ward": "Bến Thành",
      "district": "Quận 1",
      "city": "TP. Hồ Chí Minh",
      "zipCode": "700000",
      "latitude": 10.7735,
      "longitude": 106.6980,
      "isDefault": false,
      "deliveryInstructions": "Tầng 5, phòng 502"
    }
  ],
  "preferences": {
    "language": "vi",
    "currency": "VND",
    "notificationsEnabled": true,
    "emailNotifications": true,
    "smsNotifications": false
  },
  "stats": {
    "totalOrders": 47,
    "completedOrders": 43,
    "cancelledOrders": 4,
    "totalSpent": 2350000,
    "favoriteRestaurants": ["rest_001", "rest_005", "rest_012"]
  }
}
```

**Indexes (Chỉ mục):**
```
- role (Ascending) - để filter buyers, sellers, shippers
- isActive (Ascending) - để lấy users đang hoạt động
- createdAt (Descending) - để sort by newest
```

---

### 2. 🍽️ Collection: `restaurants`

**Mục đích:** Lưu thông tin nhà hàng

**Document ID:** Custom ID (tự sinh)

**Fields:**

| Field | Type | Bắt Buộc | Mô Tả |
|-------|------|---------|-------|
| `ownerId` | `string` | ✅ | ID của seller (owner) |
| `name` | `string` | ✅ | Tên nhà hàng |
| `description` | `string` | ❌ | Mô tả chi tiết |
| `category` | `string` | ✅ | Loại nhà hàng (Việt, Âu, Á) |
| `phoneNumber` | `string` | ✅ | Số điện thoại liên hệ |
| `email` | `string` | ✅ | Email nhà hàng |
| `address` | `object` | ✅ | Địa chỉ |
| `averageRating` | `number` | ✅ | Đánh giá trung bình (0-5) |
| `totalReviews` | `number` | ✅ | Số lượng reviews |
| `logoUrl` | `string` | ❌ | URL logo |
| `bannerUrl` | `string` | ❌ | URL banner |
| `isOpen` | `boolean` | ✅ | Nhà hàng đang mở |
| `operatingHours` | `object` | ✅ | Giờ hoạt động |
| `deliveryFee` | `number` | ✅ | Phí giao hàng (VND) |
| `minOrderAmount` | `number` | ✅ | Đơn hàng tối thiểu (VND) |
| `orderCount` | `number` | ✅ | Tổng số đơn hàng |
| `createdAt` | `timestamp` | ✅ | Ngày tạo |
| `updatedAt` | `timestamp` | ❌ | Cập nhật cuối |

**Ví dụ Document:**

```json
// Document ID: "rest_pho_hanoi_001"
{
  "ownerId": "user_seller_xyz789",
  "name": "Nhà hàng Phở Hà Nội",
  "slug": "pho-ha-noi",
  "description": "Phở bò truyền thống Hà Nội. Nước dùng ninh từ xương 24 giờ, thịt bò Úc nhập khẩu tươi ngon. Phục vụ từ 1985.",
  "category": "Việt Nam",
  "subCategories": ["Phở", "Bún", "Cơm"],
  "phoneNumber": "+84234567890",
  "email": "info@pho-hanoi.com",
  "website": "https://phohanoi.vn",
  "address": {
    "street": "45 Lý Thái Tổ",
    "ward": "Cửa Nam",
    "district": "Hoàn Kiếm",
    "city": "Hà Nội",
    "zipCode": "100000",
    "fullAddress": "45 Lý Thái Tổ, Cửa Nam, Hoàn Kiếm, Hà Nội",
    "latitude": 21.0285,
    "longitude": 105.8542
  },
  "averageRating": 4.7,
  "totalReviews": 1247,
  "logoUrl": "https://storage.googleapis.com/foodapp/restaurants/rest_pho_hanoi_001/logo.png",
  "bannerUrl": "https://storage.googleapis.com/foodapp/restaurants/rest_pho_hanoi_001/banner.jpg",
  "imageGallery": [
    "https://storage.googleapis.com/foodapp/restaurants/rest_pho_hanoi_001/img1.jpg",
    "https://storage.googleapis.com/foodapp/restaurants/rest_pho_hanoi_001/img2.jpg",
    "https://storage.googleapis.com/foodapp/restaurants/rest_pho_hanoi_001/img3.jpg"
  ],
  "isOpen": true,
  "isAcceptingOrders": true,
  "isFeatured": true,
  "operatingHours": {
    "monday": { "open": "06:00", "close": "22:00", "isOpen": true },
    "tuesday": { "open": "06:00", "close": "22:00", "isOpen": true },
    "wednesday": { "open": "06:00", "close": "22:00", "isOpen": true },
    "thursday": { "open": "06:00", "close": "22:00", "isOpen": true },
    "friday": { "open": "06:00", "close": "22:00", "isOpen": true },
    "saturday": { "open": "06:00", "close": "23:00", "isOpen": true },
    "sunday": { "open": "06:00", "close": "23:00", "isOpen": true }
  },
  "deliveryFee": 15000,
  "freeDeliveryThreshold": 200000,
  "minOrderAmount": 50000,
  "maxOrderAmount": 5000000,
  "estimatedDeliveryTime": 30,
  "cuisineTypes": ["Vietnamese", "Noodles", "Soup"],
  "dietaryOptions": ["Halal", "Gluten-Free Available"],
  "paymentMethods": ["CASH", "CARD", "MOMO", "ZALOPAY"],
  "orderCount": 8547,
  "completedOrderCount": 8012,
  "cancelledOrderCount": 535,
  "tags": ["popular", "fast-delivery", "traditional", "authentic"],
  "certificates": ["Food Safety Certificate", "Halal Certified"],
  "createdAt": {
    "_seconds": 1654070400,
    "_nanoseconds": 0
  },
  "updatedAt": {
    "_seconds": 1733572800,
    "_nanoseconds": 0
  },
  "metadata": {
    "verificationStatus": "VERIFIED",
    "verifiedAt": "2024-06-20T10:00:00Z",
    "lastOrderAt": "2025-12-07T13:45:00Z",
    "popularDishes": ["Phở Bò Tái", "Phở Gà", "Bún Chả"]
  }
}
```

**Subcollections:**

```
restaurants/{restaurantId}/
├── menuItems/              ← Danh sách món ăn
│   ├── menu_item_001/
│   │   ├── name
│   │   ├── price
│   │   ├── category
│   │   └── ...
│   └── ...
├── reviews/               ← Đánh giá & comments
│   ├── review_001/
│   │   ├── userId
│   │   ├── rating
│   │   ├── comment
│   │   └── ...
│   └── ...
└── promotions/            ← Khuyến mãi của nhà hàng
    ├── promo_001/
    └── ...
```

**Indexes:**
```
- ownerId (Ascending) - để lấy nhà hàng của seller
- isOpen (Ascending) - để filter nhà hàng mở
- category (Ascending) - để filter theo loại
- averageRating (Descending) - để sort by rating
```

---

### 3. 🍜 Collection: `restaurants/{restaurantId}/menuItems` (Subcollection)

**Mục đích:** Lưu danh sách món ăn của mỗi nhà hàng

**Document ID:** Custom ID (tự sinh)

**Fields:**

| Field | Type | Bắt Buộc | Mô Tả |
|-------|------|---------|-------|
| `name` | `string` | ✅ | Tên món ăn |
| `description` | `string` | ❌ | Mô tả chi tiết |
| `price` | `number` | ✅ | Giá tiền (VND) |
| `category` | `string` | ✅ | Loại món (Cơm, Phở, Khai Vị) |
| `imageUrl` | `string` | ❌ | URL ảnh |
| `isAvailable` | `boolean` | ✅ | Còn phục vụ |
| `preparationTime` | `number` | ✅ | Thời gian chuẩn bị (phút) |
| `rating` | `number` | ❌ | Đánh giá của khách (0-5) |
| `totalOrders` | `number` | ✅ | Số lần đã order |
| `createdAt` | `timestamp` | ✅ | Ngày tạo |

**Ví dụ Document:**

```json
// Document ID: "menu_pho_bo_tai_001"
// Path: restaurants/rest_pho_hanoi_001/menuItems/menu_pho_bo_tai_001
{
  "name": "Phở Bò Tái",
  "slug": "pho-bo-tai",
  "description": "Phở bò tái đặc biệt - nước dùng ninh từ xương bò 24 giờ, thịt bò Úc tươi thái mỏng. Ăn kèm rau thơm, hành lá, ngò gai.",
  "price": 55000,
  "originalPrice": 60000,
  "currency": "VND",
  "category": "Phở",
  "subCategory": "Phở Bò",
  "imageUrl": "https://storage.googleapis.com/foodapp/menu/rest_pho_hanoi_001/pho-bo-tai.jpg",
  "images": [
    "https://storage.googleapis.com/foodapp/menu/rest_pho_hanoi_001/pho-bo-tai-1.jpg",
    "https://storage.googleapis.com/foodapp/menu/rest_pho_hanoi_001/pho-bo-tai-2.jpg"
  ],
  "isAvailable": true,
  "isFeatured": true,
  "isPopular": true,
  "preparationTime": 12,
  "servingSize": "1 tô lớn (khoảng 500g)",
  "calories": 450,
  "rating": 4.8,
  "totalOrders": 3521,
  "totalReviews": 487,
  "ingredients": [
    "Thịt bò Úc",
    "Bánh phở",
    "Nước dùng xương bò",
    "Hành tây",
    "Gừng",
    "Gia vị"
  ],
  "allergens": ["Gluten"],
  "spicyLevel": 0,
  "dietaryTags": ["High Protein"],
  "options": [
    {
      "name": "Size",
      "required": false,
      "choices": [
        { "label": "Nhỏ", "priceModifier": -10000 },
        { "label": "Vừa", "priceModifier": 0 },
        { "label": "Lớn", "priceModifier": 10000 }
      ]
    },
    {
      "name": "Độ chín",
      "required": false,
      "choices": [
        { "label": "Tái", "priceModifier": 0 },
        { "label": "Chín", "priceModifier": 0 },
        { "label": "Nửa tái nửa chín", "priceModifier": 0 }
      ]
    },
    {
      "name": "Topping",
      "required": false,
      "multiple": true,
      "choices": [
        { "label": "Thêm thịt", "priceModifier": 15000 },
        { "label": "Thêm trứng", "priceModifier": 10000 },
        { "label": "Thêm rau", "priceModifier": 5000 }
      ]
    }
  ],
  "createdAt": {
    "_seconds": 1654070400,
    "_nanoseconds": 0
  },
  "updatedAt": {
    "_seconds": 1733572800,
    "_nanoseconds": 0
  },
  "metadata": {
    "lastOrderedAt": "2025-12-07T13:30:00Z",
    "viewCount": 15420,
    "favoriteCount": 892
  }
}
```

---

### 4. 📦 Collection: `orders`

**Mục đích:** Lưu thông tin đơn hàng

**Document ID:** Custom ID (tự sinh)

**Fields:**

| Field | Type | Bắt Buộc | Mô Tả |
|-------|------|---------|-------|
| `userId` | `string` | ✅ | ID buyer |
| `restaurantId` | `string` | ✅ | ID nhà hàng |
| `shipperId` | `string` | ❌ | ID shipper (khi đang giao) |
| `items` | `array` | ✅ | Danh sách items |
| `status` | `string` | ✅ | `PENDING`, `CONFIRMED`, `PREPARING`, `DELIVERING`, `COMPLETED`, `CANCELLED` |
| `subtotal` | `number` | ✅ | Tổng tiền hàng (VND) |
| `deliveryFee` | `number` | ✅ | Phí giao (VND) |
| `discountAmount` | `number` | ✅ | Số tiền giảm (VND) |
| `totalAmount` | `number` | ✅ | Tổng cộng (VND) |
| `promotionCode` | `string` | ❌ | Mã khuyến mãi nếu có |
| `deliveryAddress` | `object` | ✅ | Địa chỉ giao hàng |
| `notes` | `string` | ❌ | Ghi chú đặc biệt |
| `paymentMethod` | `string` | ✅ | `CASH`, `CARD`, `WALLET` |
| `paymentStatus` | `string` | ✅ | `PENDING`, `COMPLETED`, `FAILED` |
| `estimatedDeliveryTime` | `timestamp` | ❌ | Dự kiến giao |
| `actualDeliveryTime` | `timestamp` | ❌ | Thực tế giao |
| `rating` | `number` | ❌ | Đánh giá từ buyer (1-5) |
| `review` | `string` | ❌ | Comment từ buyer |
| `createdAt` | `timestamp` | ✅ | Ngày tạo |
| `updatedAt` | `timestamp` | ❌ | Cập nhật cuối |

**Ví dụ Document:**

```json
// Document ID: "order_20251207_abc123"
{
  "orderNumber": "ORD-20251207-8547",
  "userId": "user_abc123xyz",
  "userName": "Nguyễn Văn A",
  "userPhone": "+84912345678",
  "restaurantId": "rest_pho_hanoi_001",
  "restaurantName": "Nhà hàng Phở Hà Nội",
  "restaurantPhone": "+84234567890",
  "restaurantAddress": "45 Lý Thái Tổ, Cửa Nam, Hoàn Kiếm, Hà Nội",
  "shipperId": "user_shipper_xyz456",
  "shipperName": "Trần Văn B",
  "shipperPhone": "+84987654321",
  "items": [
    {
      "menuItemId": "menu_pho_bo_tai_001",
      "name": "Phở Bò Tái",
      "quantity": 2,
      "unitPrice": 55000,
      "options": [
        {
          "name": "Size",
          "choice": "Lớn",
          "priceModifier": 10000
        },
        {
          "name": "Topping",
          "choices": ["Thêm thịt"],
          "priceModifier": 15000
        }
      ],
      "itemTotal": 160000,
      "notes": "Ít hành"
    },
    {
      "menuItemId": "menu_tra_da_002",
      "name": "Trà đá",
      "quantity": 2,
      "unitPrice": 5000,
      "options": [],
      "itemTotal": 10000,
      "notes": ""
    }
  ],
  "status": "DELIVERING",
  "statusHistory": [
    {
      "status": "PENDING",
      "timestamp": "2025-12-07T14:00:00Z",
      "note": "Đơn hàng đã được tạo"
    },
    {
      "status": "CONFIRMED",
      "timestamp": "2025-12-07T14:02:30Z",
      "note": "Nhà hàng đã xác nhận",
      "actor": "rest_pho_hanoi_001"
    },
    {
      "status": "PREPARING",
      "timestamp": "2025-12-07T14:03:00Z",
      "note": "Đang chuẩn bị món"
    },
    {
      "status": "DELIVERING",
      "timestamp": "2025-12-07T14:15:00Z",
      "note": "Shipper đã nhận và đang giao hàng",
      "actor": "user_shipper_xyz456"
    }
  ],
  "subtotal": 170000,
  "deliveryFee": 15000,
  "serviceFee": 5000,
  "discountAmount": 17000,
  "taxAmount": 0,
  "totalAmount": 173000,
  "promotionCode": "WELCOME10",
  "promotionDetails": {
    "code": "WELCOME10",
    "description": "Giảm 10% đơn đầu tiên",
    "discountType": "PERCENT",
    "discountValue": 10,
    "discountAmount": 17000
  },
  "deliveryAddress": {
    "id": "addr_001",
    "recipientName": "Nguyễn Văn A",
    "recipientPhone": "+84912345678",
    "street": "123 Nguyễn Huệ",
    "ward": "Bến Nghé",
    "district": "Quận 1",
    "city": "TP. Hồ Chí Minh",
    "zipCode": "700000",
    "fullAddress": "123 Nguyễn Huệ, Bến Nghé, Quận 1, TP. Hồ Chí Minh",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "deliveryInstructions": "Gọi trước 5 phút"
  },
  "notes": "Không hành, thêm chanh",
  "specialInstructions": "Giao trước 15:00 nếu được",
  "paymentMethod": "CASH",
  "paymentStatus": "PENDING",
  "estimatedPreparationTime": 15,
  "estimatedDeliveryTime": {
    "_seconds": 1733575500,
    "_nanoseconds": 0
  },
  "actualDeliveryTime": null,
  "confirmedAt": {
    "_seconds": 1733572950,
    "_nanoseconds": 0
  },
  "rating": null,
  "review": null,
  "reviewedAt": null,
  "createdAt": {
    "_seconds": 1733572800,
    "_nanoseconds": 0
  },
  "updatedAt": {
    "_seconds": 1733573700,
    "_nanoseconds": 0
  },
  "metadata": {
    "source": "mobile_app",
    "appVersion": "2.5.0",
    "platform": "android",
    "deviceId": "device_xyz789",
    "cancellationReason": null,
    "refundAmount": null,
    "refundStatus": null
  }
}
```

**Subcollections:**

```
orders/{orderId}/
└── timeline/              ← Lịch sử cập nhật trạng thái
    ├── event_001/
    │   ├── status
    │   ├── timestamp
    │   └── note
    └── ...
```

**Indexes:**
```
- userId, createdAt (Descending) - để lấy orders của user
- restaurantId, status - để lấy orders của restaurant theo status
- status, createdAt (Descending) - để filter orders theo status
```

---

### 5. 🎁 Collection: `promotions`

**Mục đích:** Lưu thông tin khuyến mãi

**Document ID:** Custom ID hoặc promo code

**Fields:**

| Field | Type | Bắt Buộc | Mô Tả |
|-------|------|---------|-------|
| `code` | `string` | ✅ | Mã khuyến mãi (ví dụ: `WELCOME10`) |
| `description` | `string` | ❌ | Mô tả |
| `type` | `string` | ✅ | `PERCENT` (giảm %) hoặc `FIXED` (giảm tiền) |
| `discountValue` | `number` | ✅ | Giá trị giảm (% hoặc VND) |
| `maxDiscount` | `number` | ❌ | Giảm tối đa (VND) |
| `minOrderAmount` | `number` | ✅ | Đơn hàng tối thiểu (VND) |
| `usageLimit` | `number` | ❌ | Tổng số lần dùng |
| `usageCount` | `number` | ✅ | Đã dùng bao nhiêu lần |
| `usageLimitPerUser` | `number` | ❌ | Mỗi user dùng tối đa bao nhiêu lần |
| `restaurantId` | `string` | ❌ | ID nhà hàng (nếu chỉ cho 1 nhà hàng) |
| `isActive` | `boolean` | ✅ | Khuyến mãi có hoạt động |
| `startDate` | `timestamp` | ✅ | Ngày bắt đầu |
| `endDate` | `timestamp` | ✅ | Ngày kết thúc |
| `createdAt` | `timestamp` | ✅ | Ngày tạo |
| `updatedAt` | `timestamp` | ❌ | Cập nhật cuối |

**Ví dụ Document 1: Promotion theo % (Tất cả nhà hàng)**

```json
// Document ID: "promo_welcome10"
{
  "code": "WELCOME10",
  "title": "Chào mừng khách hàng mới",
  "description": "Giảm 10% cho đơn hàng đầu tiên. Áp dụng cho tất cả nhà hàng. Đơn tối thiểu 100,000đ",
  "type": "PERCENT",
  "discountValue": 10,
  "maxDiscount": 50000,
  "minOrderAmount": 100000,
  "usageLimit": 5000,
  "usageCount": 2847,
  "usageLimitPerUser": 1,
  "restaurantId": null,
  "restaurantIds": [],
  "applicableCategories": [],
  "excludedCategories": [],
  "userEligibility": {
    "newUsersOnly": true,
    "minOrderHistory": 0,
    "specificUserIds": [],
    "excludedUserIds": []
  },
  "isActive": true,
  "isPaused": false,
  "priority": 10,
  "displayOnHome": true,
  "bannerUrl": "https://storage.googleapis.com/foodapp/promotions/welcome10-banner.jpg",
  "terms": [
    "Chỉ áp dụng cho khách hàng mới",
    "Mỗi khách hàng chỉ được sử dụng 1 lần",
    "Không áp dụng đồng thời với khuyến mãi khác",
    "Áp dụng cho tất cả phương thức thanh toán"
  ],
  "startDate": {
    "_seconds": 1701388800,
    "_nanoseconds": 0
  },
  "endDate": {
    "_seconds": 1735689599,
    "_nanoseconds": 0
  },
  "createdAt": {
    "_seconds": 1700524800,
    "_nanoseconds": 0
  },
  "updatedAt": {
    "_seconds": 1733572800,
    "_nanoseconds": 0
  },
  "createdBy": "admin_user_001",
  "metadata": {
    "campaignName": "New User Acquisition Q4 2025",
    "budgetAllocated": 250000000,
    "budgetSpent": 142350000,
    "conversionRate": 0.34,
    "averageOrderValue": 187500
  }
}
```

**Ví dụ Document 2: Promotion giảm tiền cố định (Nhà hàng cụ thể)**

```json
// Document ID: "promo_pho_hanoi_50k"
{
  "code": "PHOHANOI50K",
  "title": "Giảm 50k tại Phở Hà Nội",
  "description": "Giảm ngay 50,000đ cho đơn từ 200,000đ tại Nhà hàng Phở Hà Nội. Số lượng có hạn!",
  "type": "FIXED",
  "discountValue": 50000,
  "maxDiscount": 50000,
  "minOrderAmount": 200000,
  "usageLimit": 500,
  "usageCount": 387,
  "usageLimitPerUser": 3,
  "restaurantId": "rest_pho_hanoi_001",
  "restaurantIds": ["rest_pho_hanoi_001"],
  "applicableCategories": ["Phở", "Bún"],
  "excludedCategories": [],
  "userEligibility": {
    "newUsersOnly": false,
    "minOrderHistory": 0,
    "specificUserIds": [],
    "excludedUserIds": []
  },
  "isActive": true,
  "isPaused": false,
  "priority": 5,
  "displayOnHome": false,
  "bannerUrl": "https://storage.googleapis.com/foodapp/promotions/pho-hanoi-50k-banner.jpg",
  "terms": [
    "Chỉ áp dụng tại Nhà hàng Phở Hà Nội",
    "Mỗi khách hàng được sử dụng tối đa 3 lần",
    "Áp dụng cho món Phở và Bún",
    "Không áp dụng cho đơn hàng đã có khuyến mãi khác"
  ],
  "startDate": {
    "_seconds": 1733443200,
    "_nanoseconds": 0
  },
  "endDate": {
    "_seconds": 1734048000,
    "_nanoseconds": 0
  },
  "createdAt": {
    "_seconds": 1733356800,
    "_nanoseconds": 0
  },
  "updatedAt": {
    "_seconds": 1733572800,
    "_nanoseconds": 0
  },
  "createdBy": "user_seller_xyz789",
  "metadata": {
    "campaignName": "Phở Hà Nội Weekly Special",
    "budgetAllocated": 25000000,
    "budgetSpent": 19350000,
    "conversionRate": 0.42,
    "averageOrderValue": 245000
  }
}
```

**Indexes:**
```
- code (Ascending) - để lookup nhanh bằng code
- isActive, endDate (Descending) - để lấy promotions còn hoạt động
```

---

## 🔍 Required Composite Indexes

Firestore tự động tạo **single-field indexes**, nhưng với **composite indexes** (query nhiều fields), bạn phải tạo thủ công.

### Cách Tạo Composite Index

**Option 1: Tự động (Khuyên dùng)**
- Chạy query lần đầu
- Firebase sẽ báo lỗi và cung cấp link tạo index
- Click link → Index được tạo tự động

**Option 2: Thủ công**
```bash
# Firebase Console → Firestore Database → Indexes → Create Index
```

**Option 3: firebase.indexes.json**
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
    }
  ]
}
```

---

### Danh Sách Required Indexes

#### 1. Collection: `orders`

| Fields | Order | Use Case | Query Example |
|--------|-------|----------|---------------|
| `userId` + `createdAt` | ASC + DESC | Lấy orders của user, sort by newest | Lịch sử đơn hàng |
| `userId` + `status` | ASC + ASC | Filter orders của user theo status | Đơn hàng đang giao |
| `restaurantId` + `createdAt` | ASC + DESC | Lấy orders của nhà hàng, sort by newest | Dashboard nhà hàng |
| `restaurantId` + `status` | ASC + ASC | Filter orders của nhà hàng theo status | Đơn chờ xác nhận |
| `restaurantId` + `status` + `createdAt` | ASC + ASC + DESC | Orders của nhà hàng, filter status, sort by time | Đơn đang giao mới nhất |
| `shipperId` + `status` | ASC + ASC | Lấy orders của shipper theo status | Đơn đang giao |
| `status` + `createdAt` | ASC + DESC | Tất cả orders theo status, sort by time | Admin dashboard |

**Query Example:**
```typescript
// Query: Lấy đơn hàng của user, sort by newest
db.collection("orders")
  .where("userId", "==", "user_abc123xyz")
  .orderBy("createdAt", "desc")
  .limit(20);

// Query: Đơn PENDING của nhà hàng
db.collection("orders")
  .where("restaurantId", "==", "rest_pho_hanoi_001")
  .where("status", "==", "PENDING")
  .orderBy("createdAt", "asc");
```

---

#### 2. Collection: `restaurants/{restaurantId}/menuItems`

| Fields | Order | Use Case | Query Example |
|--------|-------|----------|---------------|
| `category` + `isAvailable` | ASC + ASC | Lấy món available theo category | Menu theo danh mục |
| `category` + `price` | ASC + ASC | Sort món theo giá trong category | Phở từ rẻ → đắt |
| `isAvailable` + `rating` | ASC + DESC | Món available, sort by rating | Món phổ biến |
| `isAvailable` + `totalOrders` | ASC + DESC | Món available, sort by popularity | Best sellers |

**Query Example:**
```typescript
// Query: Món Phở available, sort by rating
db.collection("restaurants")
  .doc("rest_pho_hanoi_001")
  .collection("menuItems")
  .where("category", "==", "Phở")
  .where("isAvailable", "==", true)
  .orderBy("rating", "desc");
```

---

#### 3. Collection: `restaurants`

| Fields | Order | Use Case | Query Example |
|--------|-------|----------|---------------|
| `isOpen` + `category` | ASC + ASC | Nhà hàng mở theo loại | Nhà hàng Việt đang mở |
| `isOpen` + `averageRating` | ASC + DESC | Nhà hàng mở, sort by rating | Top rated restaurants |
| `category` + `averageRating` | ASC + DESC | Nhà hàng theo category, sort by rating | Nhà hàng Việt rating cao |
| `isAcceptingOrders` + `deliveryFee` | ASC + ASC | Nhà hàng nhận order, sort by phí giao | Free shipping restaurants |

**Query Example:**
```typescript
// Query: Nhà hàng Việt đang mở, rating cao
db.collection("restaurants")
  .where("category", "==", "Việt Nam")
  .where("isOpen", "==", true)
  .orderBy("averageRating", "desc")
  .limit(10);
```

---

#### 4. Collection: `users`

| Fields | Order | Use Case | Query Example |
|--------|-------|----------|---------------|
| `role` + `isActive` | ASC + ASC | Users active theo role | Danh sách sellers |
| `role` + `createdAt` | ASC + DESC | Users theo role, sort by newest | Shippers mới |
| `isVerified` + `createdAt` | ASC + DESC | Users verified, sort by time | Admin dashboard |

**Query Example:**
```typescript
// Query: Sellers active
db.collection("users")
  .where("role", "==", "SELLER")
  .where("isActive", "==", true)
  .orderBy("createdAt", "desc");
```

---

#### 5. Collection: `promotions`

| Fields | Order | Use Case | Query Example |
|--------|-------|----------|---------------|
| `isActive` + `endDate` | ASC + DESC | Promotions active, sort by end date | Khuyến mãi sắp hết hạn |
| `restaurantId` + `isActive` | ASC + ASC | Promotions của nhà hàng | Khuyến mãi nhà hàng |
| `isActive` + `startDate` | ASC + ASC | Promotions sắp diễn ra | Upcoming promotions |

**Query Example:**
```typescript
// Query: Promotions active của nhà hàng
db.collection("promotions")
  .where("restaurantId", "==", "rest_pho_hanoi_001")
  .where("isActive", "==", true)
  .orderBy("endDate", "desc");
```

---

### Index Configuration File

**File: `firestore.indexes.json`**

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
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "menuItems",
      "queryScope": "COLLECTION_GROUP",
      "fields": [
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "isAvailable", "order": "ASCENDING" },
        { "fieldPath": "rating", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "restaurants",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "isOpen", "order": "ASCENDING" },
        { "fieldPath": "averageRating", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "promotions",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "isActive", "order": "ASCENDING" },
        { "fieldPath": "endDate", "order": "DESCENDING" }
      ]
    }
  ],
  "fieldOverrides": []
}
```

**Deploy indexes:**
```bash
firebase deploy --only firestore:indexes
```

---

## 🔄 Relationships (Quan Hệ Dữ Liệu)

### Sơ Đồ Quan Hệ Tổng Thể

```
┌─────────────────────────────────────────────────────────────────┐
│                            USERS                                │
│  - BUYER (đặt hàng)                                            │
│  - SELLER (quản lý nhà hàng)                                   │
│  - SHIPPER (giao hàng)                                         │
└────────┬──────────────────┬──────────────────┬─────────────────┘
         │                  │                  │
         │ (1:N)           │ (1:N)           │ (1:N)
         ▼                  ▼                  ▼
    ┌─────────┐       ┌──────────┐      ┌─────────┐
    │ ORDERS  │       │RESTAURANTS│      │ ORDERS  │
    │(as buyer)│      │ (as owner)│      │(as shipper)
    └────┬────┘       └─────┬────┘       └─────────┘
         │                  │
         │ (N:1)           │ (1:N)
         │                  ▼
         │            ┌──────────────┐
         └───────────►│  MENU ITEMS  │
                      │(subcollection)│
                      └──────────────┘
                      
         ┌─────────────────┐
         │   PROMOTIONS    │
         │  (global/resto) │
         └─────────────────┘
```

---

### Chi Tiết Relationships

#### 1. **users ↔ restaurants** (1:N - One to Many)

**Relationship:** Một SELLER có thể sở hữu nhiều nhà hàng.

**Implementation:**
```typescript
// User document
{
  uid: "user_seller_xyz789",
  role: "SELLER"
}

// Restaurant document
{
  restaurantId: "rest_pho_hanoi_001",
  ownerId: "user_seller_xyz789"  // ← Foreign key reference
}
```

**Query: Lấy tất cả nhà hàng của seller**
```typescript
db.collection("restaurants")
  .where("ownerId", "==", "user_seller_xyz789")
  .get();
```

**Type:** Reference (denormalized with name for display)

---

#### 2. **restaurants ↔ menuItems** (1:N - Subcollection)

**Relationship:** Một nhà hàng có nhiều món ăn.

**Implementation:** Subcollection (nested structure)
```typescript
// Path: restaurants/{restaurantId}/menuItems/{menuItemId}
db.collection("restaurants")
  .doc("rest_pho_hanoi_001")
  .collection("menuItems")
  .doc("menu_pho_bo_tai_001");
```

**Benefits của Subcollection:**
- ✅ Tự động cascade khi xóa restaurant (security rules)
- ✅ Không tính vào document size limit (1MB)
- ✅ Query độc lập
- ✅ Security rules riêng biệt

**Query: Lấy tất cả món của nhà hàng**
```typescript
db.collection("restaurants")
  .doc("rest_pho_hanoi_001")
  .collection("menuItems")
  .where("isAvailable", "==", true)
  .get();
```

**Type:** Subcollection (strong containment)

---

#### 3. **users (BUYER) ↔ orders** (1:N - One to Many)

**Relationship:** Một buyer có nhiều orders.

**Implementation:**
```typescript
// Order document
{
  orderId: "order_20251207_abc123",
  userId: "user_abc123xyz",        // ← Foreign key
  userName: "Nguyễn Văn A",        // ← Denormalized for display
  userPhone: "+84912345678"        // ← Denormalized
}
```

**Query: Lấy orders của user**
```typescript
db.collection("orders")
  .where("userId", "==", "user_abc123xyz")
  .orderBy("createdAt", "desc")
  .get();
```

**Type:** Reference + Denormalization

**Denormalization rationale:**
- Tránh phải query `users` collection mỗi lần hiển thị order
- User info (name, phone) ít thay đổi

---

#### 4. **restaurants ↔ orders** (1:N - One to Many)

**Relationship:** Một nhà hàng có nhiều orders.

**Implementation:**
```typescript
// Order document
{
  orderId: "order_20251207_abc123",
  restaurantId: "rest_pho_hanoi_001",     // ← Foreign key
  restaurantName: "Nhà hàng Phở Hà Nội",  // ← Denormalized
  restaurantPhone: "+84234567890",        // ← Denormalized
  restaurantAddress: "45 Lý Thái Tổ..."  // ← Denormalized
}
```

**Query: Lấy orders của nhà hàng**
```typescript
db.collection("orders")
  .where("restaurantId", "==", "rest_pho_hanoi_001")
  .where("status", "==", "PENDING")
  .orderBy("createdAt", "asc")
  .get();
```

**Type:** Reference + Denormalization

---

#### 5. **orders ↔ menuItems** (N:M - Many to Many)

**Relationship:** Một order có nhiều món, một món có thể nằm trong nhiều orders.

**Implementation:** Embedded array (denormalized)
```typescript
// Order document
{
  orderId: "order_20251207_abc123",
  items: [
    {
      menuItemId: "menu_pho_bo_tai_001",  // ← Reference
      name: "Phở Bò Tái",                 // ← Denormalized snapshot
      quantity: 2,
      unitPrice: 55000,                   // ← Snapshot at order time
      itemTotal: 110000
    }
  ]
}
```

**Why denormalized (snapshot)?**
- ✅ Menu item price có thể thay đổi sau khi order
- ✅ Menu item có thể bị xóa
- ✅ Order phải giữ nguyên thông tin tại thời điểm đặt hàng

**Query:** Không query trực tiếp N:M, dùng `array-contains`
```typescript
// Tìm orders có món cụ thể (không hiệu quả lắm)
db.collection("orders")
  .where("items", "array-contains", {
    menuItemId: "menu_pho_bo_tai_001"
  });
```

**Type:** Embedded (denormalized snapshot)

---

#### 6. **users (SHIPPER) ↔ orders** (1:N - One to Many)

**Relationship:** Một shipper giao nhiều orders.

**Implementation:**
```typescript
// Order document
{
  orderId: "order_20251207_abc123",
  shipperId: "user_shipper_xyz456",   // ← Foreign key (nullable)
  shipperName: "Trần Văn B",          // ← Denormalized
  shipperPhone: "+84987654321"        // ← Denormalized
}
```

**Query: Lấy orders của shipper**
```typescript
db.collection("orders")
  .where("shipperId", "==", "user_shipper_xyz456")
  .where("status", "==", "DELIVERING")
  .get();
```

**Type:** Reference + Denormalization (nullable)

---

#### 7. **promotions ↔ restaurants** (N:1 hoặc Global)

**Relationship:** Một promotion có thể áp dụng cho:
- Tất cả nhà hàng (global): `restaurantId = null`
- Một nhà hàng cụ thể: `restaurantId = "rest_xxx"`
- Nhiều nhà hàng: `restaurantIds = ["rest_1", "rest_2"]`

**Implementation:**
```typescript
// Global promotion
{
  promotionId: "promo_welcome10",
  code: "WELCOME10",
  restaurantId: null,           // ← Global
  restaurantIds: []
}

// Restaurant-specific promotion
{
  promotionId: "promo_pho_hanoi_50k",
  code: "PHOHANOI50K",
  restaurantId: "rest_pho_hanoi_001",  // ← Specific
  restaurantIds: ["rest_pho_hanoi_001"]
}
```

**Query: Lấy promotions của nhà hàng**
```typescript
// Global + Restaurant-specific
db.collection("promotions")
  .where("isActive", "==", true)
  .where("restaurantId", "in", [null, "rest_pho_hanoi_001"])
  .get();
```

**Type:** Reference (nullable / array)

---

#### 8. **orders ↔ promotions** (N:1 - Many to One)

**Relationship:** Một order có thể áp dụng một promotion.

**Implementation:** Embedded (denormalized snapshot)
```typescript
// Order document
{
  orderId: "order_20251207_abc123",
  promotionCode: "WELCOME10",     // ← Reference
  promotionDetails: {             // ← Denormalized snapshot
    code: "WELCOME10",
    description: "Giảm 10% đơn đầu tiên",
    discountType: "PERCENT",
    discountValue: 10,
    discountAmount: 17000
  }
}
```

**Why denormalized?**
- ✅ Promotion có thể bị xóa/thay đổi
- ✅ Order phải lưu giữ thông tin khuyến mãi tại thời điểm đặt

**Type:** Embedded (denormalized snapshot)

---

### Summary Table

| From | To | Type | Implementation | Reason |
|------|-----|------|----------------|---------|
| User (SELLER) | Restaurants | 1:N | Reference (`ownerId`) | Owner có thể có nhiều nhà hàng |
| Restaurant | MenuItems | 1:N | **Subcollection** | Tách biệt, không tính vào 1MB limit |
| User (BUYER) | Orders | 1:N | Reference + Denorm | Lịch sử đơn hàng |
| Restaurant | Orders | 1:N | Reference + Denorm | Orders của nhà hàng |
| Order | MenuItems | N:M | **Embedded snapshot** | Giữ giá tại thời điểm order |
| User (SHIPPER) | Orders | 1:N | Reference (nullable) | Shipper được assign |
| Promotion | Restaurant | N:1 | Reference (nullable) | Global hoặc specific |
| Order | Promotion | N:1 | **Embedded snapshot** | Giữ thông tin khuyến mãi |

**Key Principles:**
- ✅ **Subcollection:** Khi data lớn, cần query độc lập
- ✅ **Reference:** Khi data thay đổi thường xuyên, cần consistency
- ✅ **Denormalization:** Khi cần performance, ít thay đổi
- ✅ **Embedded snapshot:** Khi cần lưu trữ historical data (prices, promotions)

---

## ✅ Data Integrity Rules

### 1. Order Status State Machine

**Order status phải tuân theo state machine (không được nhảy status tùy ý):**

```
┌──────────┐
│ PENDING  │ (Đơn mới tạo)
└────┬─────┘
     │
     ├─→ CONFIRMED (Seller xác nhận)
     │        │
     │        ├─→ PREPARING (Đang chuẩn bị)
     │        │        │
     │        │        ├─→ DELIVERING (Shipper đang giao)
     │        │        │        │
     │        │        │        ├─→ COMPLETED (Hoàn thành)
     │        │        │        │
     │        │        │        └─→ CANCELLED (Hủy trong quá trình giao)
     │        │        │
     │        │        └─→ CANCELLED (Hủy khi chuẩn bị)
     │        │
     │        └─→ CANCELLED (Hủy sau khi xác nhận)
     │
     └─→ CANCELLED (Hủy ngay khi tạo)
```

**Valid Transitions:**

| From | To | Actor | Condition |
|------|-----|-------|-----------|
| `PENDING` | `CONFIRMED` | SELLER | Trong 5 phút |
| `PENDING` | `CANCELLED` | BUYER / SELLER | Trước khi xác nhận |
| `CONFIRMED` | `PREPARING` | SELLER | - |
| `CONFIRMED` | `CANCELLED` | SELLER | Lý do hợp lệ |
| `PREPARING` | `DELIVERING` | SELLER (assign shipper) | Shipper available |
| `PREPARING` | `CANCELLED` | SELLER | Lý do hợp lệ |
| `DELIVERING` | `COMPLETED` | SHIPPER | Đã giao hàng |
| `DELIVERING` | `CANCELLED` | SHIPPER / BUYER | Không giao được |
| `COMPLETED` | *(final)* | - | Không thể thay đổi |
| `CANCELLED` | *(final)* | - | Không thể thay đổi |

**Implementation - Validation Function:**

```typescript
// order.service.ts
function validateStatusTransition(
  currentStatus: OrderStatus,
  newStatus: OrderStatus,
  actor: UserRole
): boolean {
  const validTransitions: Record<OrderStatus, OrderStatus[]> = {
    PENDING: ["CONFIRMED", "CANCELLED"],
    CONFIRMED: ["PREPARING", "CANCELLED"],
    PREPARING: ["DELIVERING", "CANCELLED"],
    DELIVERING: ["COMPLETED", "CANCELLED"],
    COMPLETED: [],  // Final state
    CANCELLED: []   // Final state
  };

  // Check if transition is valid
  if (!validTransitions[currentStatus].includes(newStatus)) {
    throw new Error(
      `Invalid status transition: ${currentStatus} → ${newStatus}`
    );
  }

  // Check actor permissions
  if (newStatus === "CONFIRMED" && actor !== "SELLER") {
    throw new Error("Only SELLER can confirm orders");
  }

  if (newStatus === "COMPLETED" && actor !== "SHIPPER") {
    throw new Error("Only SHIPPER can complete orders");
  }

  return true;
}
```

**Firestore Security Rule:**
```javascript
match /orders/{orderId} {
  allow update: if request.resource.data.status == resource.data.status
    || (resource.data.status == "PENDING" && request.resource.data.status == "CONFIRMED")
    || (resource.data.status == "CONFIRMED" && request.resource.data.status == "PREPARING")
    || (resource.data.status == "PREPARING" && request.resource.data.status == "DELIVERING")
    || (resource.data.status == "DELIVERING" && request.resource.data.status == "COMPLETED");
}
```

---

### 2. totalAmount Validation

**Rule:** `totalAmount` phải khớp với tổng của `subtotal + deliveryFee + serviceFee - discountAmount + taxAmount`

**Formula:**
```
totalAmount = subtotal + deliveryFee + serviceFee - discountAmount + taxAmount
```

**Validation Function:**

```typescript
// order.service.ts
function validateOrderAmounts(order: Order): void {
  // 1. Validate subtotal matches items
  const calculatedSubtotal = order.items.reduce(
    (sum, item) => sum + item.itemTotal,
    0
  );

  if (calculatedSubtotal !== order.subtotal) {
    throw new Error(
      `Subtotal mismatch: calculated ${calculatedSubtotal}, got ${order.subtotal}`
    );
  }

  // 2. Validate totalAmount formula
  const calculatedTotal =
    order.subtotal +
    (order.deliveryFee || 0) +
    (order.serviceFee || 0) +
    (order.taxAmount || 0) -
    (order.discountAmount || 0);

  if (Math.abs(calculatedTotal - order.totalAmount) > 1) {
    // Allow 1đ rounding error
    throw new Error(
      `Total amount mismatch: calculated ${calculatedTotal}, got ${order.totalAmount}`
    );
  }

  // 3. Validate item totals
  order.items.forEach((item) => {
    const itemOptionsTotal = item.options?.reduce(
      (sum, opt) => sum + (opt.priceModifier || 0),
      0
    ) || 0;

    const calculatedItemTotal =
      (item.unitPrice + itemOptionsTotal) * item.quantity;

    if (calculatedItemTotal !== item.itemTotal) {
      throw new Error(
        `Item ${item.menuItemId} total mismatch: ` +
        `calculated ${calculatedItemTotal}, got ${item.itemTotal}`
      );
    }
  });
}
```

**When to validate:**
- ✅ Khi tạo order mới (placeOrder)
- ✅ Khi apply promotion
- ✅ Khi update order amounts (admin)

---

### 3. User Role Constraints

**Rule:** Mỗi role chỉ được phép thực hiện các hành động nhất định.

**Role Permissions:**

| Action | BUYER | SELLER | SHIPPER | ADMIN |
|--------|-------|--------|---------|-------|
| Tạo order | ✅ | ❌ | ❌ | ✅ |
| Xác nhận order | ❌ | ✅ (own restaurant) | ❌ | ✅ |
| Assign shipper | ❌ | ✅ (own restaurant) | ❌ | ✅ |
| Update delivering status | ❌ | ❌ | ✅ (assigned) | ✅ |
| Complete order | ❌ | ❌ | ✅ (assigned) | ✅ |
| Cancel order (PENDING) | ✅ (own) | ✅ (restaurant) | ❌ | ✅ |
| Cancel order (CONFIRMED+) | ❌ | ✅ (restaurant) | ❌ | ✅ |
| Tạo restaurant | ❌ | ✅ | ❌ | ✅ |
| Tạo menu item | ❌ | ✅ (own restaurant) | ❌ | ✅ |
| Tạo promotion (global) | ❌ | ❌ | ❌ | ✅ |
| Tạo promotion (restaurant) | ❌ | ✅ (own restaurant) | ❌ | ✅ |

**Implementation - Middleware:**

```typescript
// middleware/role.middleware.ts
function requireRole(...allowedRoles: UserRole[]) {
  return (context: CallableRequestContext) => {
    const userRole = context.auth?.token?.role;

    if (!userRole || !allowedRoles.includes(userRole)) {
      throw new HttpsError(
        "permission-denied",
        `This action requires one of roles: ${allowedRoles.join(", ")}`
      );
    }
  };
}

// Usage:
export const confirmOrder = onCall(async (request) => {
  requireRole("SELLER", "ADMIN")(request);
  // ... business logic
});
```

**Firestore Security Rules:**
```javascript
match /orders/{orderId} {
  // BUYER can only read their own orders
  allow read: if request.auth.token.role == "BUYER"
    && resource.data.userId == request.auth.uid;

  // SELLER can read orders for their restaurants
  allow read: if request.auth.token.role == "SELLER"
    && resource.data.restaurantId in get(/databases/$(database)/documents/restaurants)
      .where("ownerId", "==", request.auth.uid);

  // SHIPPER can read assigned orders
  allow read: if request.auth.token.role == "SHIPPER"
    && resource.data.shipperId == request.auth.uid;
}

match /restaurants/{restaurantId} {
  // Only SELLER can create restaurants
  allow create: if request.auth.token.role == "SELLER"
    && request.resource.data.ownerId == request.auth.uid;

  // Only owner can update
  allow update: if request.auth.token.role == "SELLER"
    && resource.data.ownerId == request.auth.uid;
}
```

---

### 4. Promotion Validity Constraints

**Rule:** Promotion chỉ valid khi thỏa mãn tất cả các điều kiện.

**Validation Checklist:**

```typescript
// promotion.service.ts
async function validatePromotion(
  promotionCode: string,
  order: Partial<Order>,
  userId: string
): Promise<Promotion> {
  // 1. Fetch promotion
  const promotion = await promotionRepository.getByCode(promotionCode);
  if (!promotion) {
    throw new Error(`Promotion code ${promotionCode} not found`);
  }

  // 2. Check active status
  if (!promotion.isActive || promotion.isPaused) {
    throw new Error("Promotion is not active");
  }

  // 3. Check date range
  const now = new Date();
  const startDate = new Date(promotion.startDate);
  const endDate = new Date(promotion.endDate);

  if (now < startDate || now > endDate) {
    throw new Error("Promotion is not valid at this time");
  }

  // 4. Check usage limit (global)
  if (
    promotion.usageLimit &&
    promotion.usageCount >= promotion.usageLimit
  ) {
    throw new Error("Promotion usage limit reached");
  }

  // 5. Check usage limit per user
  if (promotion.usageLimitPerUser) {
    const userUsageCount = await promotionRepository.getUserUsageCount(
      promotionCode,
      userId
    );

    if (userUsageCount >= promotion.usageLimitPerUser) {
      throw new Error("You have reached usage limit for this promotion");
    }
  }

  // 6. Check min order amount
  if (order.subtotal < promotion.minOrderAmount) {
    throw new Error(
      `Minimum order amount is ${promotion.minOrderAmount}đ`
    );
  }

  // 7. Check restaurant eligibility
  if (
    promotion.restaurantId &&
    promotion.restaurantId !== order.restaurantId
  ) {
    throw new Error("Promotion is not valid for this restaurant");
  }

  if (
    promotion.restaurantIds?.length &&
    !promotion.restaurantIds.includes(order.restaurantId)
  ) {
    throw new Error("Promotion is not valid for this restaurant");
  }

  // 8. Check user eligibility
  if (promotion.userEligibility.newUsersOnly) {
    const userOrderCount = await orderRepository.getUserOrderCount(userId);
    if (userOrderCount > 0) {
      throw new Error("Promotion is for new users only");
    }
  }

  if (promotion.userEligibility.minOrderHistory) {
    const userOrderCount = await orderRepository.getUserOrderCount(userId);
    if (userOrderCount < promotion.userEligibility.minOrderHistory) {
      throw new Error(
        `You need at least ${promotion.userEligibility.minOrderHistory} orders`
      );
    }
  }

  // 9. Check excluded users
  if (promotion.userEligibility.excludedUserIds?.includes(userId)) {
    throw new Error("You are not eligible for this promotion");
  }

  return promotion;
}
```

**Calculate Discount:**

```typescript
function calculateDiscount(
  promotion: Promotion,
  subtotal: number
): number {
  let discountAmount = 0;

  if (promotion.type === "PERCENT") {
    discountAmount = subtotal * (promotion.discountValue / 100);

    // Apply max discount cap
    if (promotion.maxDiscount && discountAmount > promotion.maxDiscount) {
      discountAmount = promotion.maxDiscount;
    }
  } else if (promotion.type === "FIXED") {
    discountAmount = promotion.discountValue;

    // Discount cannot exceed subtotal
    if (discountAmount > subtotal) {
      discountAmount = subtotal;
    }
  }

  return Math.floor(discountAmount); // Round down to integer
}
```

**When to validate:**
- ✅ Khi user apply promotion code (before order)
- ✅ Khi tạo order (placeOrder)
- ❌ Không validate lại sau khi order created (snapshot)

---

### Summary: Data Integrity Enforcement

| Rule | Enforcement Point | Implementation |
|------|------------------|----------------|
| Order status transitions | Service layer + Security rules | State machine validation |
| totalAmount calculation | Service layer | Formula validation |
| User role permissions | Middleware + Security rules | Role-based access control |
| Promotion validity | Service layer | Multi-condition validation |
| MenuItem availability | Service layer | Check `isAvailable` |
| Restaurant isOpen | Service layer | Check `isOpen` + `operatingHours` |
| Unique promotion codes | Firestore rules | Document ID = code |
| Non-negative amounts | Service layer | `amount >= 0` |
| Required fields | TypeScript + Firestore rules | Type definitions |

**Best Practices:**
- ✅ Validate ở service layer (business logic)
- ✅ Enforce ở security rules (database level)
- ✅ Use TypeScript types (compile time)
- ✅ Log validation failures (debugging)
- ✅ Return clear error messages (UX)

---

## ⏰ Best Practices cho Firestore

### 1. Timestamps

**Luôn dùng server timestamp:**

```typescript
// ✅ GOOD - Server timestamp (tránh clock skew)
createdAt: admin.firestore.FieldValue.serverTimestamp()

// ❌ BAD - Client timestamp
createdAt: new Date().toISOString()
```

### 2. Denormalization (Lặp lại dữ liệu)

**Firestore cho phép lặp lại dữ liệu để tránh reads quá nhiều:**

```json
// ❌ BAD - Phải read lại restaurant để lấy tên
{
  "orderId": "order_001",
  "restaurantId": "rest_001"
}

// ✅ GOOD - Lưu thêm restaurantName (denormalization)
{
  "orderId": "order_001",
  "restaurantId": "rest_001",
  "restaurantName": "Phở Hà Nội",
  "restaurantLogoUrl": "https://..."
}
```

### 3. Document Size

- Mỗi document tối đa **1 MB**
- Nếu lớn hơn, tách thành subcollections

**❌ BAD - Document quá lớn:**
```json
{
  "restaurantId": "rest_001",
  "allMenuItems": [
    { "name": "...", "price": ... },
    // 50,000 items!
  ]
}
```

**✅ GOOD - Dùng subcollection:**
```
restaurants/{restaurantId}/menuItems/
  ├── item_001/
  ├── item_002/
  └── ... (50,000 items)
```

### 4. Indexing (Chỉ Mục)

**Firestore tự động tạo indexes cho single field.**

**Phải tạo composite index khi:**
- Query nhiều fields với operators khác nhau
- Sort kết hợp với filter

**Ví dụ - Tạo composite index:**

```typescript
// Query này cần composite index
db.collection("orders")
  .where("restaurantId", "==", "rest_001")
  .where("status", "==", "PENDING")
  .orderBy("createdAt", "desc")
  .limit(10)
```

**Firestore sẽ gợi ý tạo index tự động!**

### 5. Subcollections vs Maps

**Dùng Subcollection khi:**
- Dữ liệu có thể lớn (tránh 1MB limit)
- Cần query riêng biệt
- Muốn security rules riêng

```typescript
// ✅ Dùng subcollection cho menu items
restaurants/{id}/menuItems/
  
// ✅ Dùng map field cho địa chỉ (nhỏ, không query)
users.addresses = [
  { street: "...", ward: "..." }
]
```

### 6. Array Fields

**Giới hạn array trong 1 document:**
- Tối đa 20,000 elements
- Nhưng thực tế nên < 100

**❌ BAD:**
```json
{
  "items": [
    { "id": "item_1", "name": "..." },
    { "id": "item_2", "name": "..." },
    // ... 1 triệu items
  ]
}
```

**✅ GOOD:**
```
orders/{orderId}/items/
  ├── item_1/
  ├── item_2/
  └── ...
```

### 7. References vs Denormalization

**Dùng references khi:**
- Data thay đổi thường xuyên
- Không cần real-time updates

```typescript
// ❌ Reference only (phải query 2 lần)
{ restaurantId: "rest_001" }

// ✅ Mixed (reference + denormalize thông tin cơ bản)
{
  restaurantId: "rest_001",
  restaurantName: "Phở Hà Nội",
  restaurantRating: 4.5
}
```

---

## 📊 Data Types

| TypeScript | Firestore | Ví dụ |
|-----------|-----------|--------|
| `string` | String | "Phở Bò" |
| `number` | Number | 45000 |
| `boolean` | Boolean | true |
| `Date` | Timestamp | `serverTimestamp()` |
| `object` | Map | `{ street: "...", city: "..." }` |
| `array` | Array | `[1, 2, 3]` |
| `null` | Null | null |

---

## 🔐 Security Considerations

### Document Structure for Access Control

Organize documents để dễ implement security rules:

```firestore
// ✅ GOOD - Dễ check ownership
orders/{orderId}
├── userId      ← Check rules: request.auth.uid == userId
├── restaurantId
└── ...

// ❌ BAD - Khó check
orders/{orderId}
├── data: { userId, restaurantId, ... }
```

---

## 📈 Typical Queries

```typescript
// Get user's recent orders
db.collection("orders")
  .where("userId", "==", userId)
  .orderBy("createdAt", "desc")
  .limit(20)

// Get open restaurants by category
db.collection("restaurants")
  .where("isOpen", "==", true)
  .where("category", "==", "Việt")
  .limit(50)

// Get pending orders for restaurant
db.collection("orders")
  .where("restaurantId", "==", restaurantId)
  .where("status", "==", "PENDING")
  .orderBy("createdAt", "asc")

// Get available menu items
db.collection("restaurants")
  .doc(restaurantId)
  .collection("menuItems")
  .where("isAvailable", "==", true)
  .orderBy("rating", "desc")
```

---

## 💾 Backup Strategy

**Firestore data được backup tự động, nhưng nên:**

1. **Enable automated backups** trong Firebase Console
2. **Export data định kỳ** cho business critical data
3. **Maintain audit logs** cho mọi thay đổi

```bash
# Export data (gcloud command)
gcloud firestore export gs://bucket-name/backup-$(date +%Y%m%d)
```

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
