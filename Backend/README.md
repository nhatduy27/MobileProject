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

### Backend Environment Variables

Tạo file `Backend/.env` với nội dung sau:

```bash
# Firebase (Bắt buộc)
FIREBASE_PROJECT_ID=foodappproject-7c136
FIREBASE_REGION=asia-southeast1
FIREBASE_API_KEY=your-firebase-api-key

# Google Routes API (GPS Module - Server-side)
# Enable at: https://console.cloud.google.com/marketplace/product/google/routes.googleapis.com
GOOGLE_ROUTES_API_KEY=your-google-routes-api-key

# Optional Payment Providers
ZALOPAY_APP_ID=
MOMO_PARTNER_CODE=
SEPAY_API_KEY=

# Email Service
SENDGRID_API_KEY=your-sendgrid-key
```

> ⚠️ **Security:** Never commit real API keys to git. Use `.env` (already in .gitignore).

---

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

## � Authentication Flow

### Quan trọng: customToken ≠ ID Token!

```
1. Client call API Login/Register
   → Backend trả về customToken

2. Client sign in Firebase với customToken
   → Firebase.signInWithCustomToken(customToken)

3. Client lấy ID Token
   → user.getIdToken()

4. Client dùng ID Token để call protected APIs
   → Authorization: Bearer <ID_TOKEN>
```

**Xem hướng dẫn chi tiết:** [`docs/backend/AUTH_GUIDE.md`](../docs/backend/AUTH_GUIDE.md)

### Quick Test với Swagger

```bash
# 1. Lấy ID token để test protected APIs
cd Backend/functions
node get-id-token.js your-email@example.com

# 2. Copy ID token từ output (hoặc từ file id-token.txt)
# 3. Mở Swagger → Click "Authorize" → Paste: Bearer <token>
```

---

## 📚 API Documentation

Mở Swagger UI để xem tất cả endpoints:

- **Local**: http://localhost:3000/api/docs
- **Emulator**: http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api/docs

### Available Endpoints

**Public APIs** (không cần authentication):

- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/send-otp` - Gửi OTP verification
- `POST /api/auth/verify-otp` - Xác thực OTP
- `POST /api/auth/forgot-password` - Quên mật khẩu
- `POST /api/auth/reset-password` - Reset mật khẩu với OTP

**Protected APIs** (cần ID token):

- `PUT /api/auth/change-password` - Đổi mật khẩu
- `POST /api/auth/logout` - Đăng xuất
- `POST /api/auth/google` - Google Sign-In

---

## 🐛 Troubleshooting

### Android Maps API Key Setup

**For GPS/Map features (Client-side):**

1. **Get API Key:**
   - Go to: https://console.cloud.google.com/apis/credentials
   - Enable: Maps SDK for Android
   - Create Android API key

2. **Restrict Key (Security):**
   - Restrict by Android apps
   - Add package name: `com.yourapp.ktxdelivery`
   - Add SHA-1 certificate fingerprint

3. **Configure in Android Project:**

```kotlin
// Option 1: AndroidManifest.xml
<manifest>
  <application>
    <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="@string/google_maps_key" />
  </application>
</manifest>

// res/values/strings.xml
<string name="google_maps_key">YOUR_ANDROID_MAPS_API_KEY</string>
```

```gradle
// Option 2: Secrets Gradle Plugin (Recommended)
// local.properties
GOOGLE_MAPS_API_KEY=YOUR_ANDROID_MAPS_API_KEY
```

> ⚠️ **Never commit keys to git!** Use `local.properties` (in .gitignore) or environment variables.

---

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
