# Authentication APIs

> Nhóm API để đăng ký, đăng nhập và quản lý profile người dùng.

---

## signUp

**Mô tả:** Đăng ký tài khoản người dùng mới trong hệ thống.

**Loại:** Callable Function (`onCall`)

**Authentication:** ❌ Not Required (public API)

**Authorization:** Public - Không cần role

#### Input Schema

```typescript
interface SignUpRequest {
  email: string;              // Email đăng ký
  password: string;           // Mật khẩu (min 6 ký tự)
  displayName: string;        // Tên hiển thị
  phoneNumber?: string;       // Số điện thoại (optional)
  role?: "BUYER" | "SELLER" | "SHIPPER";  // Role mong muốn (default: BUYER)
}
```

#### Output Schema

```typescript
interface SignUpResponse {
  userId: string;             // UID của user mới tạo
  email: string;              // Email đã đăng ký
  displayName: string;        // Tên hiển thị
  role: string;               // Role được gán
  idToken: string;            // ID token để authentication
  message: string;            // Thông báo thành công
}
```

#### Logic Xử Lý
1. **Validate Input:**

- Email format hợp lệ

- Password >= 6 ký tự

- DisplayName không rỗng

- Role hợp lệ (nếu có)

2. **Create Auth Account:**

- Tạo user trong Firebase Authentication

- Set email và password

3. **Trigger onUserCreated:** 

- Auth trigger tự động chạy

- Tạo user profile trong Firestore

- Set custom claims cho role

4. **Generate ID Token:**

- Tạo custom token

- Trả về cho client để authentication
5. **Return Response:**

- userId, email, role, idToken

#### Quy Tắc Phân Quyền
- ✅ Public API - Không cần đăng nhập

- ✅ Role mặc định: BUYER

- ⚠️ Role SELLER và SHIPPER cần admin approval sau khi đăng ký

#### Request Example

```json
{
  "email": "buyer@example.com",
  "password": "password123",
  "displayName": "Nguyễn Văn A",
  "phoneNumber": "0901234567",
  "role": "BUYER"
}
```

#### Response Example

```json
{
  "userId": "user_abc123",
  "email": "buyer@example.com",
  "displayName": "Nguyễn Văn A",
  "role": "BUYER",
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6...",
  "message": "Đăng ký thành công"
}
```

## Errors
|Error Code	        | Condition	                | Message                                   |   
|-------------------|---------------------------|-------------------------------------------|
|invalid-argument	| Email rỗng hoặc invalid	| "Invalid email format"
|invalid-argument	| Password < 6 ký tự	    | "Password must be at least 6 characters"  |
|invalid-argument	| DisplayName rỗng	        | "Display name is required"                |
|already-exists	    | Email đã tồn tại	        | "Email already registered"                |
|invalid-argument	| Role không hợp lệ	        | "Invalid role. Must be BUYER, SELLER, or SHIPPER" |
|internal	        | Lỗi server	            | "Failed to create account"                |

## signIn
**Mô tả:** Đăng nhập vào hệ thống với email và password.

**Loại:** Callable Function (onCall)

**Authentication:** ❌ Not Required (public API)

**Authorization:** Public
#### Input Schema
```typescript
interface SignInRequest {
  email: string;              // Email đăng nhập
  password: string;           // Mật khẩu
}
```

#### Output Schema
```typescript
interface SignInResponse {
  userId: string;             // UID của user
  email: string;              // Email
  displayName: string;        // Tên hiển thị
  role: string;               // Role của user
  idToken: string;            // ID token
  refreshToken: string;       // Refresh token
  expiresIn: number;          // Token expiry (seconds)
}
```

#### Logic Xử Lý
1. **Gọi Firebase Auth signInWithEmailAndPassword**

2. **Lấy profile từ Firestore users/{userId}**

3. **Lấy role từ custom claims**

4. **Tạo tokens & kiểm tra isActive**
5. 
6. **Trả về thông tin user + tokens**

#### Errors
| Error Code          | Condition                | Message                           |
| ------------------- | ------------------------ | --------------------------------- |
| `invalid-argument`  | Email hoặc password rỗng | "Email and password are required" |
| `unauthenticated`   | Credentials sai          | "Invalid email or password"       |
| `permission-denied` | Account bị vô hiệu hóa   | "Account has been disabled"       |
| `internal`          | Lỗi server               | "Failed to sign in"               |

---

## getUserProfile

**Mô tả:** Lấy thông tin profile của user đang đăng nhập.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** All roles - Chỉ xem profile của chính mình

#### Input Schema

```typescript
interface GetProfileRequest {
  // No input - uses auth.uid from context
}
```

#### Output Schema

```typescript
interface UserProfile {
  userId: string;
  email: string;
  displayName: string;
  phoneNumber?: string;
  photoURL?: string;
  role: "BUYER" | "SELLER" | "SHIPPER";
  isActive: boolean;
  isVerified: boolean;
  createdAt: string;
  updatedAt?: string;
  
  // BUYER specific
  deliveryAddresses?: Address[];
  favoriteRestaurants?: string[];
  
  // SELLER specific
  restaurantId?: string;
  
  // SHIPPER specific
  vehicleInfo?: VehicleInfo;
  isOnline?: boolean;
}
```

#### Logic Xử Lý

1. **Get User ID**: Lấy `auth.uid` từ context
2. **Fetch Profile**: Query Firestore `users/{userId}`
3. **Check Existence**: Throw error nếu không tồn tại
4. **Return Data**: Trả về full profile

#### Quy Tắc Phân Quyền

- ✅ **All Roles** - BUYER, SELLER, SHIPPER
- ✅ User chỉ xem profile của chính mình
- ❌ Không xem profile của người khác

#### Request Example

```json
{}
```

#### Response Example

```json
{
  "userId": "user_abc123",
  "email": "buyer@example.com",
  "displayName": "Nguyễn Văn A",
  "phoneNumber": "0901234567",
  "photoURL": "https://...",
  "role": "BUYER",
  "isActive": true,
  "isVerified": true,
  "createdAt": "2025-12-07T08:00:00Z",
  "deliveryAddresses": [
    {
      "street": "123 Nguyễn Huệ",
      "district": "Quận 1",
      "city": "TP.HCM"
    }
  ],
  "favoriteRestaurants": ["rest_001", "rest_002"]
}
```

#### Errors

| Error Code        | Condition             | Message                  |
| ----------------- | --------------------- | ------------------------ |
| `unauthenticated` | Chưa đăng nhập        | "Unauthenticated"        |
| `not-found`       | Profile không tồn tại | "User profile not found" |
| `internal`        | Lỗi server            | "Failed to get profile"  |

---


## updateUserProfile

**Mô tả:** Cập nhật thông tin profile của user.

**Loại:** Callable Function (`onCall`)

**Authentication:** ✅ Required

**Authorization:** All roles - Chỉ update profile của chính mình

#### Input Schema

```typescript
interface UpdateProfileRequest {
  displayName?: string;
  phoneNumber?: string;
  photoURL?: string;
  deliveryAddresses?: Address[];  // BUYER only
  vehicleInfo?: VehicleInfo;      // SHIPPER only
}
```

#### Output Schema

```typescript
interface UserProfile {
  // Same as getUserProfile
}
```

#### Logic Xử Lý

1. **Validate Auth**: Check user đã đăng nhập
2. **Validate Input**: 
   - DisplayName không rỗng (nếu update)
   - Phone format hợp lệ
3. **Update Profile**: Update Firestore `users/{userId}`
4. **Set Timestamp**: `updatedAt = serverTimestamp()`
5. **Return Updated Profile**

#### Quy Tắc Phân Quyền

- ✅ **All Roles**
- ✅ Chỉ update profile của chính mình
- ❌ Không update `role`, `isActive`, `isVerified` (chỉ admin)

#### Request Example

```json
{
  "displayName": "Nguyễn Văn B",
  "phoneNumber": "0907654321",
  "photoURL": "https://example.com/avatar.jpg"
}
```

#### Response Example

```json
{
  "userId": "user_abc123",
  "email": "buyer@example.com",
  "displayName": "Nguyễn Văn B",
  "phoneNumber": "0907654321",
  "photoURL": "https://example.com/avatar.jpg",
  "role": "BUYER",
  "updatedAt": "2025-12-07T09:00:00Z"
}
```

#### Errors

| Error Code         | Condition        | Message                        |
| ------------------ | ---------------- | ------------------------------ |
| `unauthenticated`  | Chưa đăng nhập   | "Unauthenticated"              |
| `invalid-argument` | DisplayName rỗng | "Display name cannot be empty" |
| `invalid-argument` | Phone format sai | "Invalid phone number format"  |
| `internal`         | Lỗi server       | "Failed to update profile"     |

---

👉 Với hai phần `getUserProfile` và `updateUserProfile`, bạn chỉ cần kéo trọn block tương ứng từ file gốc v