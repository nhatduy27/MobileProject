# 📌 ISSUES_LIST.md — Danh sách việc cần làm cho Backend FoodApp

> File này dùng để **theo dõi các đầu việc chính** cho nhóm Backend.  
> Mỗi issue có: Mã, tiêu đề, mô tả ngắn, độ ưu tiên, trạng thái, gợi ý người phụ trách.

Bạn có thể dùng file này để:
- Làm checklist khi bắt đầu sprint
- Tạo issue tương ứng trên GitHub / Jira
- Assign cho từng thành viên trong nhóm

---

## 🔑 Quy ước

- **Priority (Độ ưu tiên)**  
  - 🔴 P0 – Bắt buộc phải làm sớm, ảnh hưởng core hệ thống  
  - 🟠 P1 – Quan trọng, nên làm trong đồ án  
  - 🟡 P2 – Tốt nếu có, có thể ghi vào “Future Work” nếu không kịp  

- **Status (Trạng thái)**  
  - TODO – Chưa bắt đầu  
  - DOING – Đang làm  
  - DONE – Hoàn thành  

---

## 1. Kiến trúc & Convention

### #ISSUE-001 — Chốt quy ước DTO (Domain vs HTTP)
- **Priority:** 🔴 P0  
- **Status:** TODO  
- **Mô tả:**
  - Hiện có các DTO vừa dùng cho HTTP (request/response), vừa dùng cho domain (input cho repository).
  - Cần chốt quy ước đặt tên rõ ràng để tránh nhầm lẫn, ví dụ:
    - HTTP: `RegisterDto`, `LoginDto`, `CreateOrderRequestDto`, `UpdateOrderStatusRequestDto`
    - Domain: `CreateAuthUserInput`, `CreateOrderInput`
  - Cập nhật lại code `auth` và `orders` nếu tên còn trùng/confusing.
- **Kết quả mong đợi:**
  - Tất cả DTO được phân loại rõ (request/response vs domain input).
  - `DEVELOPMENT_GUIDE.md` có đoạn mô tả quy ước này.
- **Gợi ý người phụ trách:** Backend lead.

---

### #ISSUE-002 — Chuẩn hoá cách xử lý lỗi từ Repository
- **Priority:** 🔴 P0  
- **Status:** TODO  
- **Mô tả:**
  - Chốt rule cho các hàm trong Repository Ports:
    - `findById` → `Promise<Entity | null>`
    - `update/delete` → throw error khi không tìm thấy (ví dụ `Error('ORDER_NOT_FOUND')`) hoặc dùng một error domain riêng.
  - Services sẽ:
    - Kiểm tra `null` / bắt lỗi domain → ném `NotFoundException` / `BadRequestException` tương ứng.
  - Áp dụng trước cho:
    - `AuthRepository`, `OrderRepository` và các adapter tương ứng.
- **Kết quả mong đợi:**
  - Không còn tình trạng nơi trả `null`, nơi throw error tuỳ tiện.
  - Exception trả ra API consistent, format theo `HttpExceptionFilter`.
- **Gợi ý người phụ trách:** Người đang nắm `orders` + `auth`.

---

## 2. Auth & Security

### #ISSUE-101 — Implement hashing mật khẩu trong AuthService
- **Priority:** 🔴 P0  
- **Status:** TODO  
- **Mô tả:**
  - Cài `bcrypt` hoặc `argon2`.
  - Trong `AuthService.register`:
    - Hash mật khẩu trước khi lưu (tạm thời lưu trong in-memory / Firebase sau này).
  - Trong `AuthService.login`:
    - So sánh mật khẩu raw với hash.
- **Kết quả mong đợi:**
  - Không lưu mật khẩu plaintext ở bất kỳ đâu.
- **Gợi ý người phụ trách:** Thành viên phụ trách Auth.

---

### #ISSUE-102 — Tích hợp JWT cơ bản cho đăng nhập
- **Priority:** 🔴 P0  
- **Status:** TODO  
- **Mô tả:**
  - Cài `@nestjs/jwt`.
  - Thêm `JwtModule` trong `AuthModule`.
  - Trong `AuthService.login`:
    - Sau khi verify mật khẩu, sinh JWT với payload: `sub`, `email`, `roles`.
  - Cập nhật `API_CONTRACT.md`:
    - Mô tả accessToken là JWT thực.
- **Kết quả mong đợi:**
  - Login trả về JWT hợp lệ, có thể decode bằng jwt.io.
- **Gợi ý người phụ trách:** Cùng người làm ISSUE-101 hoặc chia đôi.

---

### #ISSUE-103 — Implement AuthGuard dựa trên JWT
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - Thay stub `AuthGuard` hiện tại bằng guard thật:
    - Đọc `Authorization: Bearer <token>`
    - Verify JWT
    - Gắn `request.user` với payload.
  - Bảo vệ các route cần auth (ví dụ: orders cho customer/seller).
- **Kết quả mong đợi:**
  - Các endpoint nhạy cảm không truy cập được nếu không có JWT hợp lệ.
- **Gợi ý người phụ trách:** Người hiểu Nest Guard/Interceptor.

---

## 3. Firebase Integration

### #ISSUE-201 — Thiết kế cấu trúc collection Firestore cho Orders
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - Đề xuất schema Firestore cho:
    - `orders` collection
    - Có thể thêm `orderItems` embedded/subcollection nếu cần.
  - Mapping giữa domain `Order` và Firestore document.
  - Cập nhật vào `ENVIRONMENT_SETUP.md` hoặc tạo `FIRESTORE_SCHEMA.md`.
- **Kết quả mong đợi:**
  - Có tài liệu rõ ràng cho schema Firestore, dễ implement adapter.
- **Gợi ý người phụ trách:** Người sẽ làm Firestore adapter.

---

### #ISSUE-202 — Implement Firestore cho FirebaseOrderRepository (create + findById)
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - Dùng Firebase Admin SDK để implement:
    - `create(order: Order): Promise<Order>`
    - `findById(id: string): Promise<Order | null>`
  - Đảm bảo mapping đúng với domain `Order`.
  - Giữ nguyên contract `OrderRepository`.
- **Kết quả mong đợi:**
  - `OrdersService.createOrder` và `getOrderById` hoạt động với Firestore thật.
- **Gợi ý người phụ trách:** Backend dev đã làm ISSUE-201.

---

### #ISSUE-203 — Tích hợp Firebase Auth/Firebase Admin (tuỳ scope)
- **Priority:** 🟡 P2  
- **Status:** TODO  
- **Mô tả (tuỳ scope đồ án):**
  - Xác định rõ:
    - Dùng Firebase Auth làm nguồn user, hay tự lưu user bên backend?
  - Nếu dùng Firebase Auth:
    - Add verify token trong backend.
    - Cập nhật `AuthRepository` để sync user với backend nếu cần.
- **Kết quả mong đợi:**
  - Kiểu tích hợp Firebase Auth được mô tả rõ, implementation có/không tuỳ thời gian.
- **Gợi ý người phụ trách:** Người phụ trách security.

---

## 4. Orders & Domain Logic

### #ISSUE-301 — Chuẩn hoá luồng trạng thái Order
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - Xác định các trạng thái hợp lệ:
    - `PENDING`, `CONFIRMED`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED`, …
  - Viết rõ rule chuyển trạng thái:
    - Ví dụ: Không thể chuyển từ `CANCELLED` → trạng thái khác.
  - Đảm bảo domain `Order` (domain/order.entity.ts) enforce được rule này.
- **Kết quả mong đợi:**
  - Logic trạng thái đơn hàng không bị “bay lung tung”.
- **Gợi ý người phụ trách:** Dev phụ trách Orders.

---

### #ISSUE-302 — Sử dụng CachePort & EventBusPort trong OrdersService (hoàn chỉnh)
- **Priority:** 🟡 P2  
- **Status:** TODO  
- **Mô tả:**
  - Rà lại `OrdersService`:
    - Xác định chỗ nên cache (vd: list orders by customer).
    - Chỗ nên publish event (vd: `order.created`, `order.statusChanged`).
  - Dùng `CachePort` + `EventBusPort` thay vì log stub.
- **Kết quả mong đợi:**
  - Có luồng cache + event cơ bản, chuẩn bị cho Redis/MQ sau này.
- **Gợi ý người phụ trách:** Ai quen caching / event-driven.

---

## 5. Docs & Developer Experience

### #ISSUE-401 — Cập nhật ARCHITECTURE.md theo code hiện tại
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - So sánh kiến trúc mô tả trong `ARCHITECTURE.md` với code thực tế.
  - Cập nhật lại:
    - Module thực sự có (auth, orders, shared…)
    - Pattern đang dùng (Monolithic + Layered + Clean-lite)
    - Stub Firestore/Firebase hiện tại.
- **Kết quả mong đợi:**
  - ARCHITECTURE.md đồng bộ với code, không phải “design trên giấy”.
- **Gợi ý người phụ trách:** Backend lead.

---

### #ISSUE-402 — Cập nhật API_CONTRACT.md sau khi finalize Auth & Orders
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - Sau khi implement JWT + cập nhật Orders, revise lại:
    - Request/Response của Auth / Orders.
    - Thêm ví dụ JWT thật (hide secret).
    - Ghi rõ require Authorization header cho các endpoint.
- **Kết quả mong đợi:**
  - API_CONTRACT.md phản ánh đúng behavior hiện tại → FE/Tester dùng chuẩn.
- **Gợi ý người phụ trách:** Người làm Auth/Orders.

---

### #ISSUE-403 — Thêm .env.example & hướng dẫn sử dụng
- **Priority:** 🟠 P1  
- **Status:** TODO  
- **Mô tả:**
  - Tạo file `.env.example` chứa:
    - PORT
    - NODE_ENV
    - FIREBASE_PROJECT_ID
    - FIREBASE_CLIENT_EMAIL
    - FIREBASE_PRIVATE_KEY (ghi chú: phải wrap đúng)
    - JWT_SECRET, JWT_EXPIRES_IN
  - Cập nhật `ENVIRONMENT_SETUP.md` để nhắc copy `.env.example` → `.env`.
- **Kết quả mong đợi:**
  - Onboard môi trường mới dễ hơn, tránh commit lộ `.env`.
- **Gợi ý người phụ trách:** Dev quan tâm DevOps / DX.

---

## 6. Nice-to-have / Future Work

### #ISSUE-501 — Tích hợp Redis cho CachePort
- **Priority:** 🟡 P2  
- **Status:** TODO  
- **Mô tả:**
  - Implement `RedisCacheAdapter` cho `CachePort`.
  - Cấu hình Redis (local hoặc cloud).
  - Bind trong `SharedModule` (optionally qua env).
- **Kết quả mong đợi:**
  - Có thể ghi mục này vào phần “Future Improvement” trong báo cáo.
- **Gợi ý người phụ trách:** Dev có hứng thú DevOps / infra.

---

### #ISSUE-502 — Sử dụng message queue (BullMQ hoặc tương tự) cho EventBusPort
- **Priority:** 🟡 P2  
- **Status:** TODO  
- **Mô tả:**
  - Implement adapter thực cho `EventBusPort` dùng Bull/BullMQ.
  - Dùng queue cho các tác vụ:
    - Gửi thông báo
    - Ghi log audit
- **Kết quả mong đợi:**
  - Hệ thống thể hiện rõ định hướng event-driven.
- **Gợi ý người phụ trách:** Thành viên yêu thích kiến trúc hệ thống.

---

> 💡 Gợi ý: Bạn có thể copy từng issue này lên GitHub Issues hoặc Notion, rồi assign cho từng thành viên.  
> File này nên được cập nhật sau mỗi buổi họp/sprint để phản ánh đúng tiến độ.
