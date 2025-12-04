# API Contract Documentation

## 📋 Tổng quan

Tài liệu này mô tả chi tiết các REST API endpoints của backend FoodApp, bao gồm request/response formats, status codes, và ví dụ sử dụng.

**Base URL:** `http://localhost:3000/api`

**Content-Type:** `application/json`

## 📑 Mục lục

- [API Contract Documentation](#api-contract-documentation)
  - [📋 Tổng quan](#-tổng-quan)
  - [📑 Mục lục](#-mục-lục)
  - [Auth Module](#auth-module)
    - [POST /auth/register](#post-authregister)
    - [POST /auth/login](#post-authlogin)
    - [GET /auth/users/:id](#get-authusersid)
  - [Orders Module](#orders-module)
    - [POST /orders](#post-orders)
    - [GET /orders/:id](#get-ordersid)
    - [GET /orders/customer/:customerId](#get-orderscustomercustomerid)
    - [GET /orders/seller/:sellerId](#get-orderssellersellerid)
    - [PATCH /orders/:id/status](#patch-ordersidstatus)
    - [DELETE /orders/:id](#delete-ordersid)
  - [🔐 Authentication (TODO)](#-authentication-todo)
  - [📊 Error Response Format](#-error-response-format)
  - [🧪 Testing với Postman](#-testing-với-postman)
    - [Import Collection](#import-collection)
    - [Test Workflow](#test-workflow)
  - [📚 Tài liệu Liên quan](#-tài-liệu-liên-quan)
  - [🔄 Version History](#-version-history)

---

## Auth Module

Module xác thực người dùng (Authentication).

### POST /auth/register

**Mô tả:** Đăng ký tài khoản người dùng mới.

**Endpoint:** `POST /api/auth/register`

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePassword123",
  "displayName": "Nguyen Van A",
  "role": "CUSTOMER"
}
```

**Request Body Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | ✅ | Email của người dùng |
| password | string | ✅ | Mật khẩu (tối thiểu 6 ký tự) |
| displayName | string | ✅ | Tên hiển thị |
| role | string | ✅ | Vai trò: `CUSTOMER`, `SELLER`, `ADMIN` |

**Response 201 Created:**

```json
{
  "user": {
    "id": "user_123456",
    "email": "user@example.com",
    "displayName": "Nguyen Van A",
    "role": "CUSTOMER",
    "createdAt": "2024-12-04T10:30:00.000Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response Schema:**

| Field | Type | Description |
|-------|------|-------------|
| user.id | string | ID người dùng |
| user.email | string | Email |
| user.displayName | string | Tên hiển thị |
| user.role | string | Vai trò |
| user.createdAt | string (ISO 8601) | Thời gian tạo |
| token | string | JWT token (stub - TODO) |

**Status Codes:**

| Code | Description |
|------|-------------|
| 201 | Đăng ký thành công |
| 400 | Dữ liệu không hợp lệ (validation error) |
| 409 | Email đã tồn tại |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@foodapp.com",
    "password": "password123",
    "displayName": "Khách Hàng A",
    "role": "CUSTOMER"
  }'
```

---

### POST /auth/login

**Mô tả:** Đăng nhập vào hệ thống.

**Endpoint:** `POST /api/auth/login`

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePassword123"
}
```

**Request Body Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | ✅ | Email đã đăng ký |
| password | string | ✅ | Mật khẩu |

**Response 200 OK:**

```json
{
  "user": {
    "id": "user_123456",
    "email": "user@example.com",
    "displayName": "Nguyen Van A",
    "role": "CUSTOMER",
    "createdAt": "2024-12-04T10:30:00.000Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Đăng nhập thành công |
| 400 | Dữ liệu không hợp lệ |
| 401 | Email hoặc mật khẩu sai |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@foodapp.com",
    "password": "password123"
  }'
```

---

### GET /auth/users/:id

**Mô tả:** Lấy thông tin người dùng theo ID.

**Endpoint:** `GET /api/auth/users/:id`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| id | string | ID người dùng |

**Response 200 OK:**

```json
{
  "id": "user_123456",
  "email": "user@example.com",
  "displayName": "Nguyen Van A",
  "role": "CUSTOMER",
  "createdAt": "2024-12-04T10:30:00.000Z"
}
```

**Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Lấy thông tin thành công |
| 404 | Không tìm thấy người dùng |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl http://localhost:3000/api/auth/users/user_123456
```

---

## Orders Module

Module quản lý đơn hàng.

### POST /orders

**Mô tả:** Tạo đơn hàng mới.

**Endpoint:** `POST /api/orders`

**Request Body:**

```json
{
  "customerId": "user_123",
  "sellerId": "seller_456",
  "items": [
    {
      "productId": "product_1",
      "productName": "Phở Bò",
      "quantity": 2,
      "unitPrice": 50000
    },
    {
      "productId": "product_2",
      "productName": "Trà Đá",
      "quantity": 1,
      "unitPrice": 5000
    }
  ]
}
```

**Request Body Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| customerId | string | ✅ | ID khách hàng |
| sellerId | string | ✅ | ID người bán |
| items | array | ✅ | Danh sách sản phẩm trong đơn hàng |
| items[].productId | string | ✅ | ID sản phẩm |
| items[].productName | string | ✅ | Tên sản phẩm |
| items[].quantity | number | ✅ | Số lượng (> 0) |
| items[].unitPrice | number | ✅ | Đơn giá (>= 0) |

**Response 201 Created:**

```json
{
  "id": "order_789",
  "customerId": "user_123",
  "sellerId": "seller_456",
  "items": [
    {
      "productId": "product_1",
      "productName": "Phở Bò",
      "quantity": 2,
      "unitPrice": 50000,
      "totalPrice": 100000
    },
    {
      "productId": "product_2",
      "productName": "Trà Đá",
      "quantity": 1,
      "unitPrice": 5000,
      "totalPrice": 5000
    }
  ],
  "status": "PENDING",
  "totalAmount": 105000,
  "createdAt": "2024-12-04T11:00:00.000Z",
  "updatedAt": "2024-12-04T11:00:00.000Z"
}
```

**Response Schema:**

| Field | Type | Description |
|-------|------|-------------|
| id | string | ID đơn hàng |
| customerId | string | ID khách hàng |
| sellerId | string | ID người bán |
| items | array | Danh sách sản phẩm (có thêm totalPrice) |
| status | string | Trạng thái: `PENDING`, `CONFIRMED`, `PREPARING`, `DELIVERING`, `COMPLETED`, `CANCELLED` |
| totalAmount | number | Tổng tiền đơn hàng (tự động tính) |
| createdAt | string (ISO 8601) | Thời gian tạo |
| updatedAt | string (ISO 8601) | Thời gian cập nhật |

**Status Codes:**

| Code | Description |
|------|-------------|
| 201 | Tạo đơn hàng thành công |
| 400 | Dữ liệu không hợp lệ |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl -X POST http://localhost:3000/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "user_123",
    "sellerId": "seller_456",
    "items": [
      {
        "productId": "product_1",
        "productName": "Phở Bò",
        "quantity": 2,
        "unitPrice": 50000
      }
    ]
  }'
```

---

### GET /orders/:id

**Mô tả:** Lấy thông tin đơn hàng theo ID.

**Endpoint:** `GET /api/orders/:id`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| id | string | ID đơn hàng |

**Response 200 OK:**

```json
{
  "id": "order_789",
  "customerId": "user_123",
  "sellerId": "seller_456",
  "items": [
    {
      "productId": "product_1",
      "productName": "Phở Bò",
      "quantity": 2,
      "unitPrice": 50000,
      "totalPrice": 100000
    }
  ],
  "status": "CONFIRMED",
  "totalAmount": 100000,
  "createdAt": "2024-12-04T11:00:00.000Z",
  "updatedAt": "2024-12-04T11:15:00.000Z"
}
```

**Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Lấy đơn hàng thành công |
| 404 | Không tìm thấy đơn hàng |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl http://localhost:3000/api/orders/order_789
```

---

### GET /orders/customer/:customerId

**Mô tả:** Lấy tất cả đơn hàng của khách hàng.

**Endpoint:** `GET /api/orders/customer/:customerId`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| customerId | string | ID khách hàng |

**Response 200 OK:**

```json
[
  {
    "id": "order_789",
    "customerId": "user_123",
    "sellerId": "seller_456",
    "items": [...],
    "status": "COMPLETED",
    "totalAmount": 100000,
    "createdAt": "2024-12-04T11:00:00.000Z",
    "updatedAt": "2024-12-04T12:00:00.000Z"
  },
  {
    "id": "order_790",
    "customerId": "user_123",
    "sellerId": "seller_789",
    "items": [...],
    "status": "PENDING",
    "totalAmount": 50000,
    "createdAt": "2024-12-04T13:00:00.000Z",
    "updatedAt": "2024-12-04T13:00:00.000Z"
  }
]
```

**Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Lấy danh sách thành công (có thể là mảng rỗng) |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl http://localhost:3000/api/orders/customer/user_123
```

---

### GET /orders/seller/:sellerId

**Mô tả:** Lấy tất cả đơn hàng của người bán.

**Endpoint:** `GET /api/orders/seller/:sellerId`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| sellerId | string | ID người bán |

**Response 200 OK:**

```json
[
  {
    "id": "order_789",
    "customerId": "user_123",
    "sellerId": "seller_456",
    "items": [...],
    "status": "PREPARING",
    "totalAmount": 100000,
    "createdAt": "2024-12-04T11:00:00.000Z",
    "updatedAt": "2024-12-04T11:30:00.000Z"
  },
  {
    "id": "order_791",
    "customerId": "user_456",
    "sellerId": "seller_456",
    "items": [...],
    "status": "CONFIRMED",
    "totalAmount": 75000,
    "createdAt": "2024-12-04T12:00:00.000Z",
    "updatedAt": "2024-12-04T12:15:00.000Z"
  }
]
```

**Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Lấy danh sách thành công |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl http://localhost:3000/api/orders/seller/seller_456
```

---

### PATCH /orders/:id/status

**Mô tả:** Cập nhật trạng thái đơn hàng.

**Endpoint:** `PATCH /api/orders/:id/status`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| id | string | ID đơn hàng |

**Request Body:**

```json
{
  "status": "CONFIRMED"
}
```

**Request Body Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | string | ✅ | Trạng thái mới: `PENDING`, `CONFIRMED`, `PREPARING`, `DELIVERING`, `COMPLETED`, `CANCELLED` |

**Response 200 OK:**

```json
{
  "id": "order_789",
  "customerId": "user_123",
  "sellerId": "seller_456",
  "items": [...],
  "status": "CONFIRMED",
  "totalAmount": 100000,
  "createdAt": "2024-12-04T11:00:00.000Z",
  "updatedAt": "2024-12-04T11:45:00.000Z"
}
```

**Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Cập nhật thành công |
| 400 | Trạng thái không hợp lệ |
| 404 | Không tìm thấy đơn hàng |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl -X PATCH http://localhost:3000/api/orders/order_789/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

---

### DELETE /orders/:id

**Mô tả:** Xóa đơn hàng (chỉ dùng cho mục đích admin/testing).

**Endpoint:** `DELETE /api/orders/:id`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| id | string | ID đơn hàng |

**Response 204 No Content:**

_Không có response body_

**Status Codes:**

| Code | Description |
|------|-------------|
| 204 | Xóa thành công |
| 404 | Không tìm thấy đơn hàng |
| 500 | Lỗi server |

**Curl Example:**

```bash
curl -X DELETE http://localhost:3000/api/orders/order_789
```

---

## 🔐 Authentication (TODO)

**Hiện tại:** Các endpoints chưa yêu cầu authentication.

**Tương lai:** Sẽ implement JWT authentication với:

**Header required:**
```
Authorization: Bearer <jwt-token>
```

**Protected endpoints:**
- Tất cả endpoints trừ `/auth/register` và `/auth/login`

**Example với authentication:**

```bash
# 1. Đăng nhập
TOKEN=$(curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}' \
  | jq -r '.token')

# 2. Sử dụng token trong requests
curl http://localhost:3000/api/orders/order_123 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Error Response Format

Tất cả errors trả về format chuẩn:

```json
{
  "statusCode": 404,
  "message": "Không tìm thấy đơn hàng: order_999",
  "error": "Not Found",
  "timestamp": "2024-12-04T14:30:00.000Z",
  "path": "/api/orders/order_999"
}
```

**Error Schema:**

| Field | Type | Description |
|-------|------|-------------|
| statusCode | number | HTTP status code |
| message | string | Mô tả lỗi bằng tiếng Việt |
| error | string | Tên lỗi HTTP |
| timestamp | string | Thời gian xảy ra lỗi |
| path | string | Endpoint path |

**Common Errors:**

| Status Code | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Dữ liệu request không hợp lệ |
| 401 | Unauthorized | Chưa đăng nhập hoặc token không hợp lệ |
| 403 | Forbidden | Không có quyền truy cập |
| 404 | Not Found | Không tìm thấy resource |
| 409 | Conflict | Xung đột dữ liệu (ví dụ: email đã tồn tại) |
| 500 | Internal Server Error | Lỗi server |

---

## 🧪 Testing với Postman

### Import Collection

Tạo file `FoodApp.postman_collection.json`:

```json
{
  "info": {
    "name": "FoodApp Backend API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"test@foodapp.com\",\n  \"password\": \"password123\",\n  \"displayName\": \"Test User\",\n  \"role\": \"CUSTOMER\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{baseUrl}}/auth/register",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "register"]
            }
          }
        },
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"test@foodapp.com\",\n  \"password\": \"password123\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{baseUrl}}/auth/login",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "login"]
            }
          }
        }
      ]
    },
    {
      "name": "Orders",
      "item": [
        {
          "name": "Create Order",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"customerId\": \"user_123\",\n  \"sellerId\": \"seller_456\",\n  \"items\": [\n    {\n      \"productId\": \"product_1\",\n      \"productName\": \"Phở Bò\",\n      \"quantity\": 2,\n      \"unitPrice\": 50000\n    }\n  ]\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{baseUrl}}/orders",
              "host": ["{{baseUrl}}"],
              "path": ["orders"]
            }
          }
        },
        {
          "name": "Get Order by ID",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{baseUrl}}/orders/order_1",
              "host": ["{{baseUrl}}"],
              "path": ["orders", "order_1"]
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:3000/api",
      "type": "string"
    }
  ]
}
```

### Test Workflow

```bash
# 1. Đăng ký user
# POST /auth/register

# 2. Đăng nhập
# POST /auth/login

# 3. Tạo đơn hàng
# POST /orders

# 4. Lấy đơn hàng
# GET /orders/:id

# 5. Cập nhật trạng thái
# PATCH /orders/:id/status

# 6. Lấy danh sách đơn hàng
# GET /orders/customer/:customerId
```

---

## 📚 Tài liệu Liên quan

- [QUICKSTART.md](./QUICKSTART.md) - Hướng dẫn chạy backend nhanh
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Quy ước lập trình
- [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md) - Cấu hình môi trường

---

## 🔄 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-12-04 | Initial API documentation |

**Maintainer:** Backend Team FoodApp
