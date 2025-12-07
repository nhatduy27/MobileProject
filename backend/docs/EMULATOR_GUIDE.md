# Hướng Dẫn Sử Dụng Firebase Emulator

> **Tài liệu này hướng dẫn cách sử dụng Firebase Emulator Suite để test và develop backend locally.**

---

## 📖 Mục Lục

- [Full Emulator Suite](#-full-emulator-suite)
- [Functions Emulator](#-functions-emulator-only)
- [Connect Android App to Emulator](#-connect-android-app-to-emulator)
- [Testing với cURL và Postman](#-testing-với-curl-và-postman)
- [Emulator Tips](#-emulator-tips)

---

## 🔥 Full Emulator Suite

### Khởi động Full Emulator (Firestore + Functions + Auth)

```bash
# Bắt đầu tất cả emulators
firebase emulators:start

# Bắt đầu với seed data
firebase emulators:start --import=./seed-data

# Xóa data sau khi tắt
firebase emulators:start --clear-on-exit
```

### Truy cập Emulator UI

**URL:** http://localhost:4000

**Các tab có sẵn:**
- **Authentication** - Quản lý test users
- **Firestore** - Xem và edit documents
- **Functions** - View logs và test functions
- **Logs** - Unified logs từ tất cả emulators

### Các Ports Mặc Định

| Service | Port | URL |
|---------|------|-----|
| Emulator UI | 4000 | http://localhost:4000 |
| Authentication | 9099 | - |
| Firestore | 8080 | - |
| Functions | 5001 | http://localhost:5001 |
| Storage | 9199 | - |

### Cấu Hình Emulator

**File: `firebase.json`**

```json
{
  "emulators": {
    "auth": {
      "port": 9099
    },
    "firestore": {
      "port": 8080
    },
    "functions": {
      "port": 5001
    },
    "ui": {
      "enabled": true,
      "port": 4000
    },
    "singleProjectMode": true
  }
}
```

### Import/Export Emulator Data

**Export data:**

```bash
# Export tất cả emulator data
firebase emulators:export ./emulator-data

# Export khi tắt emulator (auto)
firebase emulators:start --export-on-exit=./emulator-data
```

**Import data:**

```bash
# Import data khi start
firebase emulators:start --import=./emulator-data

# Import và export on exit
firebase emulators:start --import=./emulator-data --export-on-exit
```

**Use case:** Tạo seed data cho testing

```bash
# 1. Start emulator
firebase emulators:start

# 2. Manually tạo test data qua UI hoặc scripts
# 3. Export data (Ctrl+C và chọn export)

# 4. Sau này start với data đó
firebase emulators:start --import=./seed-data
```

---

## 🔧 Functions Emulator Only

### Chỉ chạy Functions Emulator

```bash
# Hot reload mode (auto-rebuild khi code thay đổi)
npm run dev

# Build + emulator (no watch)
npm run serve
```

**Output ví dụ:**

```
⚡  functions: Loaded functions definitions from source.
⚡  functions[us-central1-placeOrder]: http function initialized.
⚡  functions[us-central1-updateOrderStatus]: http function initialized.

┌─────────────────────────────────────────────────────────────┐
│ ✔  All emulators ready! It is now safe to connect your app. │
│ i  View Emulator UI at http://localhost:4000                │
└─────────────────────────────────────────────────────────────┘

┌───────────┬──────────────┬─────────────────────────────────┐
│ Emulator  │ Host:Port    │ View in Emulator UI             │
├───────────┼──────────────┼─────────────────────────────────┤
│ Functions │ localhost:5001 │ http://localhost:4000/functions │
└───────────┴──────────────┴─────────────────────────────────┘
```

### Benefits của Hot Reload Mode

✅ **Tự động compile TypeScript** - Không cần chạy `npm run build` manually  
✅ **Tự động reload functions** - Changes được phản ánh ngay lập tức  
✅ **Fast feedback loop** - Test nhanh hơn  
✅ **Live logs** - Xem logs real-time trong terminal

---

## 📱 Connect Android App to Emulator

### Android Configuration

**⚠️ Android Emulator dùng IP `10.0.2.2` thay vì `localhost`**

**File: `app/src/main/java/.../MainActivity.kt`**

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Connect to emulators (development only)
        if (BuildConfig.DEBUG) {
            // Auth Emulator
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            
            // Firestore Emulator
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
            
            // Functions Emulator
            FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5001)
        }
        
        setContentView(R.layout.activity_main)
    }
}
```

**⚠️ Lưu ý quan trọng:**

```
📱 Android Emulator: Dùng IP 10.0.2.2 thay vì localhost
💻 Physical Device: Dùng IP máy host (e.g., 192.168.1.10)
```

### Physical Device Configuration

**Cách tìm IP máy host:**

```bash
# Windows
ipconfig

# macOS/Linux
ifconfig | grep "inet "

# Tìm IP local network (ví dụ: 192.168.1.10)
```

**Update Android code:**

```kotlin
if (BuildConfig.DEBUG) {
    val hostIP = "192.168.1.10" // Thay bằng IP máy bạn
    
    FirebaseAuth.getInstance().useEmulator(hostIP, 9099)
    FirebaseFirestore.getInstance().useEmulator(hostIP, 8080)
    FirebaseFunctions.getInstance().useEmulator(hostIP, 5001)
}
```

### Test Connection từ Android

```kotlin
// Test Auth
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword("buyer@test.com", "password123")
    .addOnSuccessListener { result ->
        Log.d("Emulator", "✅ Auth connected: ${result.user?.uid}")
    }
    .addOnFailureListener { error ->
        Log.e("Emulator", "❌ Auth error: ${error.message}")
    }

// Test Firestore
FirebaseFirestore.getInstance()
    .collection("test")
    .document("ping")
    .set(mapOf("timestamp" to System.currentTimeMillis()))
    .addOnSuccessListener {
        Log.d("Emulator", "✅ Firestore connected")
    }
    .addOnFailureListener { error ->
        Log.e("Emulator", "❌ Firestore error: ${error.message}")
    }

// Test Functions
FirebaseFunctions.getInstance()
    .getHttpsCallable("getUserProfile")
    .call()
    .addOnSuccessListener { result ->
        Log.d("Emulator", "✅ Functions connected: $result")
    }
    .addOnFailureListener { error ->
        Log.e("Emulator", "❌ Functions error: ${error.message}")
    }
```

---

## 🧪 Testing với cURL và Postman

### Test với cURL

```bash
# Test callable function
curl -X POST http://localhost:5001/PROJECT_ID/us-central1/placeOrder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ID_TOKEN" \
  -d '{
    "data": {
      "restaurantId": "rest_001",
      "items": [{"menuItemId": "item_1", "quantity": 2}]
    }
  }'
```

**Lấy ID Token:**

1. Truy cập http://localhost:4000/auth
2. Sign in với test user
3. Copy ID Token từ UI

### Test với Postman

**Setup:**

1. Tạo new request
2. Method: POST
3. URL: `http://localhost:5001/PROJECT_ID/us-central1/placeOrder`
4. Headers:
   - `Content-Type: application/json`
   - `Authorization: Bearer YOUR_ID_TOKEN`
5. Body (raw JSON):

```json
{
  "data": {
    "restaurantId": "rest_001",
    "items": [
      {"menuItemId": "item_1", "quantity": 2}
    ]
  }
}
```

### Test với Firebase Functions Shell

```bash
# Start interactive shell
npm run shell

# Test function
> placeOrder({
    restaurantId: 'rest_001',
    items: [{menuItemId: 'item_1', quantity: 1}]
  })

# Output sẽ hiển thị ngay
```

---

## ⚙️ Emulator Tips

### Tip 1: Clear Emulator Data

```bash
# Start với data mới (xóa hết data cũ)
firebase emulators:start --clear-on-exit

# Hoặc xóa manually
rm -rf .firebase/emulator-data
```

### Tip 2: Debug Emulator Issues

```bash
# Check emulator logs
npm run dev

# Check specific function logs
npm run dev 2>&1 | grep "placeOrder"

# Check port conflicts
lsof -i :5001  # macOS/Linux
netstat -ano | findstr :5001  # Windows
```

### Tip 3: Reset Auth State

```bash
# Stop emulator (Ctrl+C)
# Delete auth data
rm -rf .firebase/emulator-data/auth_export

# Restart emulator
firebase emulators:start
```

### Tip 4: Monitor Real-time Logs

```bash
# Terminal 1: Start emulator
npm run dev

# Terminal 2: Watch specific logs
tail -f functions.log | grep "ERROR"

# Terminal 3: Test functions
curl http://localhost:5001/...
```

### Tip 5: Create Seed Data Script

**File: `scripts/seed-data.ts`**

```typescript
import * as admin from "firebase-admin";

admin.initializeApp();

async function seedData() {
  const db = admin.firestore();
  
  // Seed restaurants
  await db.collection("restaurants").doc("rest_001").set({
    name: "Test Restaurant",
    isActive: true,
    rating: 4.5,
  });
  
  // Seed menu items
  await db.collection("restaurants").doc("rest_001")
    .collection("menuItems").doc("item_1").set({
      name: "Phở Bò",
      price: 50000,
      available: true,
    });
  
  console.log("✅ Seed data completed");
  process.exit(0);
}

seedData();
```

**Chạy script:**

```bash
# With emulator running
npm run seed:data
```

---

## 🔍 Troubleshooting

### Issue: Emulator không start

**Check Java:**

```bash
java -version
# Should show Java 11 or higher
```

**Check ports:**

```bash
# macOS/Linux
lsof -i :4000
lsof -i :5001
lsof -i :8080
lsof -i :9099

# Windows
netstat -ano | findstr :5001
```

**Kill processes:**

```bash
# macOS/Linux
kill -9 <PID>

# Windows
taskkill /PID <PID> /F
```

### Issue: Functions không reload

**Solution:**

```bash
# Stop emulator (Ctrl+C)
# Clear build cache
rm -rf lib/

# Rebuild
npm run build

# Restart emulator
npm run dev
```

### Issue: Android không connect được emulator

**Checklist:**

- ✅ Đảm bảo dùng IP `10.0.2.2` (KHÔNG phải localhost)
- ✅ Emulator đang chạy (`firebase emulators:start`)
- ✅ Firewall không block ports
- ✅ BuildConfig.DEBUG = true

### Issue: Physical device không connect được

**Solution:**

1. Tìm IP máy host:
```bash
ipconfig  # Windows
ifconfig  # macOS/Linux
```

2. Update code với IP đó:
```kotlin
FirebaseAuth.getInstance().useEmulator("192.168.1.10", 9099)
```

3. Đảm bảo firewall KHÔNG block ports 4000, 5001, 8080, 9099

---

## 📚 Tài Liệu Liên Quan

- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Hướng dẫn phát triển chính
- [DEBUGGING.md](./DEBUGGING.md) - Hướng dẫn debugging chi tiết
- [Firebase Emulator Suite Documentation](https://firebase.google.com/docs/emulator-suite)
- [Connect Android App to Emulator](https://firebase.google.com/docs/emulator-suite/connect_and_prototype)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
