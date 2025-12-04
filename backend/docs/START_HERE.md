# 🚀 START_HERE.md — Hướng Dẫn Bắt Đầu Nhanh Cho Backend FoodApp

Chào mừng bạn đến với backend của dự án **FoodApp**!  
Đây là tài liệu giúp bạn nắm bắt trong **5–10 phút** tất cả những gì cần biết để bắt đầu code mà **không phải đọc 4000 dòng tài liệu**.

---

# 🧭 1. Backend này sử dụng kiến trúc gì?

Backend được xây dựng theo:

- **Monolithic Architecture**  
- **Layered Architecture (Controller → Service → Repo)**  
- **Clean-lite + Ports & Adapters + Dependency Inversion**

Mục tiêu:
- Code rõ ràng, dễ mở rộng  
- Teammate dễ hiểu  
- Có thể chuyển từ Firebase → Postgres, in-memory → Redis, Event stub → MQ mà không phải rewrite service

---

# 📌 2. Tôi cần đọc gì trước? (Ưu tiên theo mức độ)

## ⭐ BẮT BUỘC (đọc trước khi code — ~15 phút)
### 1. `QUICKSTART.md`
Cách chạy backend, cách cấu hình môi trường.

### 2. `DEVELOPMENT_GUIDE.md`
Chỉ đọc **3 phần đầu**:
- Tổng quan kiến trúc
- Cách tổ chức controller/service/domain/infra
- Quy tắc code & import

👉 Bạn chỉ cần đọc mục 1–3, các mục sau là optional.

---

## ⭐⭐ KHI BẮT ĐẦU LÀM TASK (đọc theo nhu cầu)
### 3. `HOW_TO_ADD_A_NEW_MODULE.md`
Đọc khi bạn tạo module mới (products, shops, reviews…).  
Có hướng dẫn step-by-step.

### 4. `API_CONTRACT.md`
Đọc khi làm việc với API cho mobile app hoặc debug FE/BE.

---

## ⭐⭐⭐ TÀI LIỆU THAM KHẢO (không cần đọc hết)
### 5. `REPOSITORY_GUIDE.md`
Dành cho bạn nào muốn hiểu sâu hơn Ports/Adapters & DIP.

### 6. `ARCHITECTURE.md`
Đọc khi cần hiểu sâu về triết lý Clean-lite.

### 7. `ENVIRONMENT_SETUP.md`
Chỉ cần khi setup máy mới hoặc cấu hình Firebase/Redis/MQ.

---

# 🧩 3. Tôi phải hiểu những thứ nào trước khi code?

Chỉ cần nắm 3 ý:

### ✔ 1. Controller **không chứa logic**
Nó chỉ:
- nhận request  
- validate  
- gọi service  

### ✔ 2. Service **chỉ xử lý nghiệp vụ**  
Không gọi Firebase/prisma/SDK trực tiếp → chỉ gọi **Ports** (abstraction).

### ✔ 3. Hạ tầng (Firebase/Redis/MQ) nằm trong **Adapter**
Có thể thay thế bất cứ lúc nào mà không sửa Service.

---

# 📂 4. Cấu trúc thư mục cực ngắn

```
src/
  modules/
    orders/
      domain/          # Entity + Repository Port
      infra/           # Firebase Repo Adapter
      dto/
      orders.service.ts
      orders.controller.ts
      orders.module.ts
  shared/
    cache/             # CachePort + Adapter
    events/            # EventBusPort + Adapter
    notifications/     # NotificationPort + Adapter
```

---

# 🧪 5. Luồng làm việc cơ bản của 1 backend developer

## Khi làm nhiệm vụ mới:
1️⃣ Xem API cần gì → mở `API_CONTRACT.md`  
2️⃣ Nếu là module mới → xem `HOW_TO_ADD_A_NEW_MODULE.md`  
3️⃣ Tạo entity → tạo Port → tạo Adapter stub  
4️⃣ Viết service (dùng Port, không đụng Firebase)  
5️⃣ Viết controller  
6️⃣ Test bằng Postman/cURL  

---

# 👥 6. Luồng teamwork (quan trọng)

### Khi backend thay đổi API → cập nhật `API_CONTRACT.md`  
### Khi thêm module → cập nhật `HOW_TO_ADD_A_NEW_MODULE.md`  
### Khi setup Firebase/Redis → cập nhật `ENVIRONMENT_SETUP.md`

---

# ❤️ 7. Đọc gì nếu chỉ có 5 phút?

- `QUICKSTART.md`  
- Mục 1–3 của `DEVELOPMENT_GUIDE.md`  
- Cuộn qua `API_CONTRACT.md` để biết BE đang có gì  

---

# 🎯 8. Kết luận: Đừng cố đọc tất cả ngay!  
Hãy:
- đọc tài liệu đúng thời điểm  
- bắt đầu code sớm  
- khi cần thì quay lại docs để tra cứu  

Backend này được thiết kế để **teammate học nhanh – code nhanh – ít sai**.

Chào mừng đến với team 🍀
