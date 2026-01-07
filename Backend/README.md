# KTX Delivery - Backend

Backend API cho ứng dụng giao hàng KTX, sử dụng NestJS + Firebase Functions.

## 📋 Yêu cầu

- **Node.js**: v22+
- **Java JDK**: v21+ (chỉ cần khi chạy Firebase Emulators)
- **Firebase CLI**: `npm install -g firebase-tools`

## 🚀 Cài đặt

```bash
cd Backend/functions
npm install
```

## ⚡ Chạy Backend

### Cách 1: Quick Start (Khuyến nghị khi dev)

```bash
cd Backend/functions
npm run build
npm start
```

**Kết quả:**

- API: `http://localhost:3000/api`
- Swagger: `http://localhost:3000/api/docs`

> ⚠️ **Lưu ý**: Cách này kết nối thẳng Firebase Production. Dùng cẩn thận!

---

### Cách 2: Firebase Emulators (An toàn cho test)

```bash
cd Backend
firebase emulators:start
```

**Kết quả:**

- API: `http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api`
- Swagger: `http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api/docs`
- Emulator UI: `http://127.0.0.1:4000`

**Các Emulators:**
| Service | Port |
|---------|------|
| Auth | 9099 |
| Functions | 5001 |
| Firestore | 8080 |
| Storage | 9199 |
| UI | 4000 |

> ✅ Data chỉ lưu local, tắt emulator là mất. An toàn để test!

---

## 📝 Scripts

| Command               | Mô tả                              |
| --------------------- | ---------------------------------- |
| `npm start`           | Chạy NestJS standalone (port 3000) |
| `npm run build`       | Build TypeScript → JavaScript      |
| `npm run build:watch` | Build + watch thay đổi             |
| `npm run emulators`   | Chạy Firebase Emulators            |
| `npm run deploy`      | Deploy lên Firebase Production     |

---

## 🔧 Cấu hình cho Frontend

### Kết nối API

```kotlin
// Android - Retrofit
val BASE_URL = "http://10.0.2.2:3000/api/"  // Emulator Android
// hoặc
val BASE_URL = "http://localhost:3000/api/" // Web/iOS Simulator
```

### Kết nối Firebase Emulators (Android)

```kotlin
// Trong Application class
if (BuildConfig.DEBUG) {
    Firebase.auth.useEmulator("10.0.2.2", 9099)
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.storage.useEmulator("10.0.2.2", 9199)
}
```

---

## 📚 API Documentation

Mở Swagger UI để xem tất cả endpoints:

- **Local**: http://localhost:3000/api/docs
- **Emulator**: http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api/docs

---

## 🐛 Troubleshooting

### Lỗi Java version

```
Error: firebase-tools no longer supports Java version before 21
```

**Fix:** Cài Java 21+ và set JAVA_HOME:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
```

### Lỗi port đang dùng

```bash
# Windows - Kill process trên port 3000
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### Build lỗi TypeScript

```bash
cd functions
rm -rf lib node_modules
npm install
npm run build
```

## 📞 Hỗ trợ

Có vấn đề? Liên hệ backend team hoặc tạo issue trên GitHub.
