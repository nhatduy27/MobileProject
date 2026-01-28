# Hướng Dẫn API Modules Mới - KTX Delivery Backend

> **Tài liệu dành cho Frontend Team**  
> Phiên bản: 1.0.0  
> Cập nhật: 28/01/2026

---

## Mục Lục

1. [AI Chatbot Module](#1-ai-chatbot-module)
2. [Fuzzy Search Module](#2-fuzzy-search-module)
3. [Review System Module](#3-review-system-module)
4. [Menu Caching](#4-menu-caching-transparent)
5. [Environment Setup](#8-environment-setup-backend)

---

## 8. Environment Setup (Backend)

> **Dành cho Backend Team / DevOps**

### 8.1. File .env

Copy file `.env.example` thành `.env` và điền các giá trị:

```bash
cp .env.example .env
```

### 8.2. Lấy Gemini API Key (Cho AI Chatbot)

**Bước 1:** Truy cập Google AI Studio
```
https://aistudio.google.com/app/apikey
```

**Bước 2:** Đăng nhập bằng tài khoản Google

**Bước 3:** Click **"Create API key"**

**Bước 4:** Chọn project hoặc tạo mới

**Bước 5:** Copy API key và thêm vào `.env`:
```env
GEMINI_API_KEY=AIzaSy...your_key_here
```

**Lưu ý quan trọng:**
- Free tier: 10 RPM (requests per minute) cho model `gemini-2.5-flash-lite`
- Backend đã có rate limiting 3 req/min/user để tránh vượt quota
- Nếu cần production, đăng ký Google Cloud billing

### 8.3. Các biến môi trường cần thiết

| Biến | Bắt buộc | Mô tả |
|------|----------|-------|
| `FIREBASE_PROJECT_ID` | ✅ | ID dự án Firebase |
| `FIREBASE_API_KEY` | ✅ | Web API Key từ Firebase Console |
| `GOOGLE_APPLICATION_CREDENTIALS` | ✅ | Đường dẫn tới service account JSON |
| `GEMINI_API_KEY` | ✅ | API key cho AI Chatbot |
| `SENDGRID_API_KEY` | ⚠️ | Cho gửi OTP email |
| `SEPAY_SECRET_KEY` | ❌ | Optional - thanh toán online |

### 8.4. Khởi động Backend

```bash
cd Backend/functions
npm install
npm run start:dev
```

Backend sẽ chạy tại: `http://localhost:3000`
Swagger docs: `http://localhost:3000/api/docs`

---

## 1. AI Chatbot Module

Chatbot sử dụng Google Gemini AI để trả lời câu hỏi của khách hàng về dịch vụ KTX Delivery.

### 1.1. Gửi tin nhắn cho Chatbot

```
POST /api/chatbot/message
```

**Headers:**
```
Authorization: Bearer <firebase_id_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "message": "Làm sao để hủy đơn hàng?"
}
```

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "answer": "Để hủy đơn hàng, bạn vào chi tiết đơn hàng và nhấn nút \"Hủy đơn\". Lưu ý: Chỉ có thể hủy khi đơn còn ở trạng thái \"Chờ xác nhận\".",
    "confidence": "high"
  },
  "timestamp": "2026-01-28T03:30:53.784Z"
}
```

**Response bị rate limit (200):**
```json
{
  "success": true,
  "data": {
    "answer": "Bạn đang gửi tin nhắn quá nhanh. Vui lòng đợi 15 giây trước khi gửi tiếp.",
    "confidence": "low",
    "rateLimited": true,
    "waitTime": 15
  }
}
```

**Lưu ý quan trọng:**
- **Rate Limit**: Tối đa 3 tin nhắn/phút/user (20 giây giữa mỗi request)
- Khi `rateLimited: true`, hiển thị thông báo chờ và disable input trong `waitTime` giây
- `confidence` có thể là: `"high"`, `"medium"`, `"low"`

### 1.2. Lấy Quick Replies (Câu hỏi gợi ý)

```
POST /api/chatbot/quick-replies
```

**Headers:**
```
Content-Type: application/json
```

> Không cần Authorization - public endpoint

**Response (200):**
```json
{
  "success": true,
  "data": {
    "quickReplies": [
      "Làm sao để hủy đơn hàng?",
      "Thời gian giao hàng là bao lâu?",
      "Phí ship được tính như thế nào?",
      "Làm sao để theo dõi đơn hàng?",
      "Thanh toán online có an toàn không?",
      "Tôi muốn đăng ký làm shipper",
      "Cách sử dụng mã giảm giá?"
    ]
  }
}
```

### 1.3. Gợi ý UI/UX cho Chatbot

```
┌─────────────────────────────────────┐
│  🤖 Trợ lý ảo KTX Delivery          │
├─────────────────────────────────────┤
│                                     │
│  [Quick Reply 1] [Quick Reply 2]    │
│  [Quick Reply 3] [Quick Reply 4]    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 👤 Phí ship tính thế nào?   │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 🤖 Phí ship do quán thiết   │    │
│  │ lập, từ 3.000đ - 10.000đ... │    │
│  └─────────────────────────────┘    │
│                                     │
├─────────────────────────────────────┤
│ [Message input...        ] [Gửi]   │
└─────────────────────────────────────┘
```

---

## 2. Fuzzy Search Module

Tìm kiếm sản phẩm với khả năng:
- Chấp nhận lỗi chính tả
- Tự động normalize dấu tiếng Việt ("pho" → "Phở")
- Hỗ trợ filter theo shop, category, giá

### 2.1. Tìm kiếm sản phẩm

```
GET /api/search/products
```

**Query Parameters:**

| Param | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `q` | string | ✅ | Từ khóa tìm kiếm | `pho`, `com suon` |
| `limit` | number | ❌ | Số kết quả (default: 20, max: 50) | `10` |
| `shopId` | string | ❌ | Lọc theo shop | `shop123` |
| `categoryId` | string | ❌ | Lọc theo danh mục | `cat456` |
| `minPrice` | number | ❌ | Giá tối thiểu | `10000` |
| `maxPrice` | number | ❌ | Giá tối đa | `50000` |

**Ví dụ Request:**
```
GET /api/search/products?q=pho&limit=5&minPrice=20000
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": "prod123",
        "name": "Phở bò tái",
        "nameNormalized": "pho bo tai",
        "description": "Phở bò tái với nước dùng thơm ngon",
        "shopId": "shop456",
        "shopName": "Phở Thìn",
        "categoryId": "cat789",
        "categoryName": "Món nước",
        "price": 35000,
        "imageUrl": "https://...",
        "isAvailable": true,
        "rating": 4.5,
        "soldCount": 120
      }
    ],
    "total": 1
  },
  "timestamp": "2026-01-28T03:14:38.568Z"
}
```

**Lưu ý:**
- Tìm kiếm không phân biệt dấu: `com` = `cơm`
- Chấp nhận typo: `pho` tìm được `Phở`
- Kết quả sắp xếp theo độ relevance

### 2.2. Gợi ý UI cho Search

```
┌─────────────────────────────────────┐
│ 🔍 [Tìm món ăn...              ] X │
├─────────────────────────────────────┤
│ 📍 Gợi ý: com suon, pho, bun bo    │
├─────────────────────────────────────┤
│ ┌─────┐                             │
│ │ 🍜  │ Phở bò tái                  │
│ │     │ Phở Thìn · 35.000đ          │
│ └─────┘ ⭐ 4.5 (120 đã bán)         │
├─────────────────────────────────────┤
│ ┌─────┐                             │
│ │ 🍚  │ Cơm sườn nướng              │
│ │     │ Quán Ngon · 40.000đ         │
│ └─────┘ ⭐ 4.8 (85 đã bán)          │
└─────────────────────────────────────┘
```

---

## 3. Review System Module

Hệ thống đánh giá với các tính năng:
- Khách hàng đánh giá đơn hàng sau khi nhận
- Chủ quán phản hồi đánh giá
- Tự động cập nhật rating trung bình của shop

### 3.1. Tạo đánh giá (Customer)

```
POST /api/reviews
```

**Headers:**
```
Authorization: Bearer <firebase_id_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": "order123",
  "rating": 5,
  "comment": "Đồ ăn ngon, giao hàng nhanh!"
}
```

**Validation Rules:**
- `orderId`: Bắt buộc, phải là đơn hàng của user với status `DELIVERED`
- `rating`: Bắt buộc, từ 1-5 (số nguyên)
- `comment`: Tùy chọn, max 1000 ký tự

**Response thành công (201):**
```json
{
  "success": true,
  "data": {
    "id": "review123",
    "orderId": "order123",
    "customerId": "user456",
    "customerName": "Nguyễn Văn A",
    "shopId": "shop789",
    "shopName": "Quán Ngon",
    "rating": 5,
    "comment": "Đồ ăn ngon, giao hàng nhanh!",
    "createdAt": "2026-01-28T03:37:40.307Z",
    "updatedAt": "2026-01-28T03:37:40.307Z"
  }
}
```

**Các lỗi có thể xảy ra:**

| Error Code | Message | Nguyên nhân |
|------------|---------|-------------|
| `NOT_FOUND` | Đơn hàng không tồn tại | orderId không hợp lệ |
| `FORBIDDEN` | Đây không phải đơn hàng của bạn | User không sở hữu order |
| `BAD_REQUEST` | Chỉ có thể đánh giá đơn hàng đã giao thành công | Order chưa DELIVERED |
| `CONFLICT` | Bạn đã đánh giá đơn hàng này rồi | Review đã tồn tại |
| `VALIDATION_ERROR` | rating must be between 1 and 5 | Rating không hợp lệ |

### 3.2. Lấy reviews của tôi (Customer)

```
GET /api/reviews/my
```

**Headers:**
```
Authorization: Bearer <firebase_id_token>
```

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": "review123",
      "orderId": "order123",
      "shopId": "shop789",
      "shopName": "Quán Ngon",
      "rating": 5,
      "comment": "Đồ ăn ngon!",
      "ownerReply": "Cảm ơn bạn đã ủng hộ!",
      "ownerReplyAt": "2026-01-28T04:00:00.000Z",
      "createdAt": "2026-01-28T03:37:40.307Z"
    }
  ],
  "timestamp": "2026-01-28T03:38:01.227Z"
}
```

### 3.3. Lấy reviews của shop (Public)

```
GET /api/reviews/shop/:shopId
```

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `limit` | number | ❌ | Số reviews (default: 10, max: 50) |
| `lastId` | string | ❌ | ID review cuối để phân trang |

**Ví dụ:**
```
GET /api/reviews/shop/shop789?limit=10
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "reviews": [
      {
        "id": "review123",
        "orderId": "order123",
        "customerId": "user456",
        "customerName": "Nguyễn Văn A",
        "rating": 5,
        "comment": "Đồ ăn ngon!",
        "ownerReply": "Cảm ơn bạn!",
        "ownerReplyAt": "2026-01-28T04:00:00.000Z",
        "createdAt": "2026-01-28T03:37:40.307Z"
      }
    ],
    "total": 1,
    "avgRating": 5
  }
}
```

### 3.4. Lấy review theo orderId (Customer)

```
GET /api/reviews/order/:orderId
```

**Headers:**
```
Authorization: Bearer <firebase_id_token>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "review123",
    "rating": 5,
    "comment": "Tuyệt vời!",
    "createdAt": "2026-01-28T03:37:40.307Z"
  }
}
```

**Response nếu chưa review (404):**
```json
{
  "success": false,
  "message": "Chưa có đánh giá cho đơn hàng này",
  "errorCode": "NOT_FOUND"
}
```

### 3.5. Chủ quán phản hồi review (Owner)

```
POST /api/owner/reviews/:reviewId/reply
```

**Headers:**
```
Authorization: Bearer <firebase_id_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "ownerReply": "Cảm ơn bạn đã ủng hộ quán! Hẹn gặp lại!"
}
```

**Validation:**
- `ownerReply`: Bắt buộc, max 500 ký tự
- User phải là owner của shop liên quan đến review

**Response thành công (200):**
```json
{
  "success": true,
  "data": {
    "id": "review123",
    "ownerReply": "Cảm ơn bạn đã ủng hộ quán! Hẹn gặp lại!",
    "ownerReplyAt": "2026-01-28T04:00:00.000Z"
  }
}
```

### 3.6. Gợi ý UI cho Reviews

**Màn hình chi tiết đơn hàng (đã giao):**
```
┌─────────────────────────────────────┐
│ ĐƠN HÀNG #KTX-ABC123                │
│ Trạng thái: ✅ Đã giao              │
├─────────────────────────────────────┤
│ ...thông tin đơn hàng...            │
├─────────────────────────────────────┤
│ ⭐ ĐÁNH GIÁ ĐƠN HÀNG                │
│                                     │
│ Bạn thấy đơn hàng thế nào?          │
│ ☆ ☆ ☆ ☆ ☆                          │
│                                     │
│ [Nhập nhận xét...                 ] │
│                                     │
│ [    GỬI ĐÁNH GIÁ    ]              │
└─────────────────────────────────────┘
```

**Màn hình reviews của shop:**
```
┌─────────────────────────────────────┐
│ ⭐ 4.8 (156 đánh giá)               │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ 👤 Nguyễn Văn A                 │ │
│ │ ⭐⭐⭐⭐⭐ · 2 ngày trước        │ │
│ │ "Đồ ăn ngon, giao nhanh!"       │ │
│ │                                 │ │
│ │ 💬 Chủ quán: Cảm ơn bạn!        │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 👤 Trần Thị B                   │ │
│ │ ⭐⭐⭐⭐☆ · 5 ngày trước         │ │
│ │ "Món ăn OK, ship hơi lâu"       │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

---

## 4. Menu Caching (Transparent)

> **Ghi chú cho Frontend**: Đây là tính năng backend, **không cần thay đổi gì ở frontend**.

Menu caching tự động hoạt động:
- Cache menu của shop trong 2 phút
- Auto-invalidate khi có thay đổi (thêm/sửa/xóa sản phẩm)
- Giảm thời gian load menu từ ~500ms xuống ~50ms

---

## 5. Error Handling Chung

Tất cả API trả về format thống nhất:

**Thành công:**
```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-01-28T03:00:00.000Z"
}
```

**Lỗi:**
```json
{
  "success": false,
  "message": "Mô tả lỗi tiếng Việt",
  "errorCode": "ERROR_CODE",
  "timestamp": "2026-01-28T03:00:00.000Z"
}
```

**Các error code phổ biến:**

| Code | HTTP Status | Mô tả |
|------|-------------|-------|
| `UNAUTHORIZED` | 401 | Chưa đăng nhập hoặc token hết hạn |
| `FORBIDDEN` | 403 | Không có quyền truy cập |
| `NOT_FOUND` | 404 | Không tìm thấy resource |
| `BAD_REQUEST` | 400 | Request không hợp lệ |
| `VALIDATION_ERROR` | 400 | Dữ liệu không đúng format |
| `CONFLICT` | 409 | Xung đột (đã tồn tại, etc.) |

---

## 6. Testing với Swagger

Backend có Swagger UI để test API:

```
http://localhost:3000/api/docs
```

**Bước authorize:**
1. Chạy script lấy token:
   ```bash
   node scripts/get-id-token.js testcustomer999@test.com
   ```
2. Copy token từ output
3. Vào Swagger → Click "Authorize" → Paste token

---

## 7. Liên hệ

Nếu có thắc mắc về API, liên hệ Backend Team qua:
- **Slack**: #backend-support
- **Email**: backend-team@ktxdelivery.com
