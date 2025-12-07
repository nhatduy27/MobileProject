# QUICKSTART.md

## 1. Giới thiệu dự án
- Mobile Food Delivery là hệ thống giao đồ ăn gồm backend (Firebase Functions) và ứng dụng Android (Kotlin).
- Dự án giúp kết nối người mua, quán ăn, shipper qua nền tảng di động.

## 2. Yêu cầu cài đặt
- Node.js (>= 16)
- Firebase CLI (`npm install -g firebase-tools`)
- Android Studio
- Java/Kotlin environment (JDK >= 11)

## 3. Hướng dẫn clone project
```bash
# Clone repository
$ git clone <repo-url>
$ cd MobileProject
```

## 4. Setup backend
- Đăng nhập Firebase:
  ```bash
  firebase login
  ```
- Chọn project Firebase:
  ```bash
  firebase use --add
  ```
- Chạy emulator:
  ```bash
  firebase emulators:start
  ```
- Chạy functions:
  ```bash
  cd backend/functions
  npm install
  npm run serve   # hoặc npm run build
  ```

## 5. Setup Android
- Mở folder `FoodApp/app/` bằng Android Studio.
- Sync Gradle (bấm "Sync Now" nếu được yêu cầu).
- Chạy app trên thiết bị hoặc emulator.

## 6. Kiểm tra API bằng Firebase Emulator UI
- Truy cập [http://localhost:4000](http://localhost:4000) để kiểm tra API, Firestore, Auth, Functions.

## 7. Tài liệu quan trọng
- [ARCHITECTURE.md](../docs/ARCHITECTURE.md) – Kiến trúc hệ thống
- [API_REFERENCE_MVP.md](../docs/API_REFERENCE_MVP.md) – API contract
- [FIRESTORE_SCHEMA.md](../docs/FIRESTORE_SCHEMA.md) – Thiết kế dữ liệu

## 8. Ghi chú cho người mới
- Luôn kiểm tra README và các file hướng dẫn trước khi hỏi.
- Nếu gặp lỗi, xem file COMMON_ERRORS.md hoặc DEBUGGING.md.
- Đảm bảo đã cài đủ các yêu cầu môi trường.

---

## Tiếp theo nên đọc gì?
- ARCHITECTURE.md để hiểu tổng quan hệ thống.
- API_REFERENCE_MVP.md để biết các API chính.
- FIRESTORE_SCHEMA.md để nắm dữ liệu backend.

Chúc bạn khởi động dự án thành công!
