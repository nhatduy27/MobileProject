# Hướng dẫn Cài đặt Môi trường

## 📋 Tổng quan

Tài liệu này hướng dẫn chi tiết cách cài đặt môi trường phát triển backend NestJS cho dự án FoodApp, từ việc cài đặt dependencies đến cấu hình Firebase và chạy ứng dụng.

## ✅ Yêu cầu Trước khi Bắt đầu

### 1. Node.js và npm

**Phiên bản yêu cầu:**
- Node.js: >= 18.x.x (khuyến nghị 18.x LTS hoặc 20.x LTS)
- npm: >= 9.x.x

**Kiểm tra phiên bản:**

```bash
node --version
# v18.19.0 hoặc cao hơn

npm --version
# 9.x.x hoặc cao hơn
```

**Cài đặt Node.js:**

**Windows:**
1. Tải Node.js từ: https://nodejs.org/
2. Chọn phiên bản LTS (Long Term Support)
3. Chạy installer và làm theo hướng dẫn
4. Restart terminal sau khi cài đặt

**macOS:**
```bash
# Sử dụng Homebrew
brew install node@18

# Hoặc tải từ nodejs.org
```

**Linux (Ubuntu/Debian):**
```bash
# Cài đặt Node.js 18.x
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Verify installation
node --version
npm --version
```

### 2. Nest CLI (Optional nhưng khuyến nghị)

**Cài đặt global:**

```bash
npm install -g @nestjs/cli

# Verify installation
nest --version
# 10.x.x
```

**Nest CLI giúp:**
- Generate modules, controllers, services nhanh
- Chạy development server với hot-reload
- Build production-ready code

### 3. Git

**Kiểm tra:**

```bash
git --version
# git version 2.x.x
```

**Cài đặt nếu chưa có:**
- Windows: https://git-scm.com/download/win
- macOS: `brew install git`
- Linux: `sudo apt-get install git`

### 4. Code Editor (khuyến nghị VS Code)

**Visual Studio Code:**
- Tải từ: https://code.visualstudio.com/
- Extensions khuyến nghị:
  - ESLint
  - Prettier - Code formatter
  - Thunder Client (test API)
  - GitLens

## 🚀 Cài đặt Backend

### Bước 1: Clone Repository

```bash
# Clone repository
git clone <repository-url>
cd MobileProject/backend
```

### Bước 2: Cài đặt Dependencies

```bash
# Cài đặt tất cả dependencies
npm install

# Hoặc dùng yarn
# yarn install
```

**Dependencies chính được cài:**
- `@nestjs/common`, `@nestjs/core`, `@nestjs/platform-express`: NestJS framework
- `class-validator`, `class-transformer`: DTO validation
- `rxjs`: Reactive programming
- `reflect-metadata`: Metadata reflection

**Dev Dependencies:**
- TypeScript compiler
- ESLint
- Jest (testing framework)
- Prettier (code formatting)

**Thời gian cài đặt:** ~2-5 phút tùy tốc độ mạng.

### Bước 3: Tạo File Cấu hình `.env`

Tạo file `.env` trong thư mục `backend/`:

```bash
# Trong backend/
touch .env
```

**Nội dung file `.env`:**

```env
# ================================
# SERVER CONFIGURATION
# ================================
PORT=3000
NODE_ENV=development

# ================================
# FIREBASE CONFIGURATION (TODO)
# ================================
# Firebase Admin SDK Service Account
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYour-Private-Key\n-----END PRIVATE KEY-----\n"
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project-id.iam.gserviceaccount.com

# Firebase Web App Config
FIREBASE_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXX
FIREBASE_AUTH_DOMAIN=your-project-id.firebaseapp.com
FIREBASE_DATABASE_URL=https://your-project-id.firebaseio.com
FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com

# ================================
# JWT CONFIGURATION (TODO)
# ================================
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRES_IN=7d

# ================================
# REDIS CONFIGURATION (TODO - cho production)
# ================================
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# ================================
# CORS CONFIGURATION
# ================================
CORS_ORIGIN=http://localhost:3000,http://localhost:8080

# ================================
# LOGGING
# ================================
LOG_LEVEL=debug
```

**⚠️ Lưu ý:**
- File `.env` đã được thêm vào `.gitignore` - **không commit lên Git**
- Copy file này thành `.env.example` để share template cho team
- Thay thế các giá trị placeholder bằng giá trị thật khi deploy production

### Bước 4: Verify Installation

**Kiểm tra TypeScript compilation:**

```bash
npm run build
```

**Output mong đợi:**
```
✔ Successfully compiled TypeScript files
```

**Kiểm tra linting:**

```bash
npm run lint
```

**Output mong đợi:**
```
✨  Done in X.XXs
```

## 🔥 Cấu hình Firebase (TODO)

Backend hiện tại sử dụng **stub implementations** cho Firebase. Để tích hợp Firebase thật, làm theo các bước sau:

### Bước 1: Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** (Thêm dự án)
3. Nhập tên project: `foodapp-mobile` (hoặc tên khác)
4. Bỏ chọn Google Analytics nếu không cần
5. Click **"Create project"**

### Bước 2: Enable Authentication

1. Trong Firebase Console, vào **Authentication**
2. Click **"Get started"**
3. Enable **Email/Password** provider:
   - Click vào "Email/Password"
   - Toggle **"Enable"**
   - Click **"Save"**

### Bước 3: Create Firestore Database

1. Vào **Firestore Database**
2. Click **"Create database"**
3. Chọn **"Start in test mode"** (cho development)
4. Chọn location: `asia-southeast1` (Singapore)
5. Click **"Enable"**

**Firestore Collections cần tạo:**
- `users`: Lưu thông tin người dùng
- `orders`: Lưu đơn hàng
- `products`: Lưu sản phẩm (khi implement ProductsModule)

### Bước 4: Generate Service Account Key

1. Vào **Project Settings** (⚙️ icon)
2. Chọn tab **"Service accounts"**
3. Click **"Generate new private key"**
4. Click **"Generate key"** - file JSON sẽ được download

**File JSON có dạng:**

```json
{
  "type": "service_account",
  "project_id": "foodapp-mobile",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@foodapp-mobile.iam.gserviceaccount.com",
  "client_id": "1234567890",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  ...
}
```

### Bước 5: Lưu Service Account Key

**Option 1: Environment Variables (khuyến nghị cho production)**

Thêm vào `.env`:

```env
FIREBASE_PROJECT_ID=foodapp-mobile
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYour-Key-Here\n-----END PRIVATE KEY-----\n"
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@foodapp-mobile.iam.gserviceaccount.com
```

**Option 2: File JSON (dễ hơn cho development)**

1. Copy file JSON vào `backend/secrets/`
2. Đổi tên thành `firebase-service-account.json`
3. Update `.gitignore` để ignore folder này:

```gitignore
# .gitignore
secrets/
*.json
```

4. Update `src/config/firebase.config.ts`:

```typescript
import * as admin from 'firebase-admin';
import * as path from 'path';

export function initializeFirebase(): void {
  if (admin.apps.length === 0) {
    const serviceAccountPath = path.join(
      __dirname,
      '../../secrets/firebase-service-account.json'
    );

    admin.initializeApp({
      credential: admin.credential.cert(serviceAccountPath),
      databaseURL: process.env.FIREBASE_DATABASE_URL,
    });

    console.log('✅ Firebase Admin SDK initialized');
  }
}
```

### Bước 6: Cài đặt Firebase Admin SDK

```bash
npm install firebase-admin
```

### Bước 7: Update Repository Adapters

Thay thế stub code trong các repository adapters:

**Ví dụ: `src/modules/auth/infra/firebase-auth.repository.ts`**

```typescript
import * as admin from 'firebase-admin';

@Injectable()
export class FirebaseAuthRepository extends AuthRepository {
  async create(userData: CreateAuthUserDto): Promise<AuthUser> {
    // Tạo user trong Firebase Authentication
    const userRecord = await admin.auth().createUser({
      email: userData.email,
      password: userData.password,
      displayName: userData.displayName,
    });

    // Lưu user data vào Firestore
    await admin.firestore().collection('users').doc(userRecord.uid).set({
      email: userData.email,
      displayName: userData.displayName,
      role: userData.role,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Return user entity
    return new AuthUser({
      id: userRecord.uid,
      email: userData.email,
      displayName: userData.displayName,
      role: userData.role,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  }

  // Implement các methods khác tương tự...
}
```

### Bước 8: Setup Firebase Cloud Messaging (Optional)

Để gửi push notifications:

1. Vào **Cloud Messaging** trong Firebase Console
2. Copy **Server key** vào `.env`:

```env
FCM_SERVER_KEY=AAAA...your-server-key
```

3. Update `src/shared/notifications/fcm-notification.adapter.ts` với logic gửi FCM

## 🏃 Chạy Backend

### Development Mode (với hot-reload)

```bash
npm run start:dev
```

**Output mong đợi:**

```
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [NestFactory] Starting Nest application...
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [InstanceLoader] SharedModule dependencies initialized +50ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [InstanceLoader] AuthModule dependencies initialized +10ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [InstanceLoader] OrdersModule dependencies initialized +5ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [RoutesResolver] AppController {/api}: +15ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [RouterExplorer] Mapped {/api/auth/register, POST} route +5ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [RouterExplorer] Mapped {/api/auth/login, POST} route +1ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [RouterExplorer] Mapped {/api/orders, POST} route +1ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [RouterExplorer] Mapped {/api/orders/:id, GET} route +1ms
[Nest] 12345  - 12/04/2024, 10:30:00 AM     LOG [NestApplication] Nest application successfully started +5ms
✅ Application is running on: http://localhost:3000/api
```

**Server đang chạy tại:** http://localhost:3000/api

### Production Mode

```bash
# Build production code
npm run build

# Run production build
npm run start:prod
```

### Debug Mode (VS Code)

Tạo file `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "node",
      "request": "launch",
      "name": "Debug NestJS",
      "runtimeArgs": [
        "--nolazy",
        "-r",
        "ts-node/register"
      ],
      "args": [
        "${workspaceFolder}/src/main.ts"
      ],
      "cwd": "${workspaceFolder}",
      "internalConsoleOptions": "openOnSessionStart",
      "skipFiles": [
        "<node_internals>/**",
        "node_modules/**"
      ],
      "env": {
        "NODE_ENV": "development"
      }
    }
  ]
}
```

Press **F5** để start debugging.

## ✅ Verify Backend hoạt động

### Test với curl

```bash
# Test health check
curl http://localhost:3000/api

# Test register
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "displayName": "Test User",
    "role": "CUSTOMER"
  }'

# Test login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Test với Postman / Thunder Client

Import collection từ [API_CONTRACT.md](./API_CONTRACT.md)

## 🧪 Chạy Tests

```bash
# Unit tests
npm run test

# E2E tests
npm run test:e2e

# Test coverage
npm run test:cov
```

## 🔧 Troubleshooting

### 1. Port 3000 đã được sử dụng

**Error:**
```
Error: listen EADDRINUSE: address already in use :::3000
```

**Giải pháp:**

```bash
# Tìm process đang dùng port 3000
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# macOS/Linux
lsof -ti:3000
kill -9 <PID>

# Hoặc đổi port trong .env
PORT=3001
```

### 2. Module not found

**Error:**
```
Error: Cannot find module 'class-validator'
```

**Giải pháp:**

```bash
# Xóa node_modules và reinstall
rm -rf node_modules package-lock.json
npm install
```

### 3. TypeScript compilation errors

**Error:**
```
error TS2304: Cannot find name 'xyz'
```

**Giải pháp:**

```bash
# Clean build và rebuild
rm -rf dist
npm run build
```

### 4. Firebase initialization error

**Error:**
```
Error: Failed to parse private key
```

**Giải pháp:**
- Kiểm tra `FIREBASE_PRIVATE_KEY` trong `.env` có đúng format không
- Phải wrap private key trong quotes: `"-----BEGIN PRIVATE KEY-----\n..."`
- Đảm bảo có `\n` giữa các dòng

### 5. CORS Error từ Frontend

**Error:**
```
Access to fetch at 'http://localhost:3000/api' from origin 'http://localhost:8080' 
has been blocked by CORS policy
```

**Giải pháp:**

Thêm origin vào `.env`:

```env
CORS_ORIGIN=http://localhost:8080,http://localhost:3000
```

## 📦 Package Scripts

| Script | Mô tả |
|--------|-------|
| `npm run start` | Chạy production build |
| `npm run start:dev` | Chạy development với hot-reload |
| `npm run start:debug` | Chạy debug mode |
| `npm run start:prod` | Chạy production mode |
| `npm run build` | Build production code |
| `npm run lint` | Chạy ESLint |
| `npm run format` | Format code với Prettier |
| `npm run test` | Chạy unit tests |
| `npm run test:watch` | Chạy tests ở watch mode |
| `npm run test:cov` | Chạy tests với coverage report |
| `npm run test:e2e` | Chạy end-to-end tests |

## 🌐 Deploy lên Production

### Heroku

```bash
# Install Heroku CLI
# https://devcenter.heroku.com/articles/heroku-cli

# Login
heroku login

# Create app
heroku create foodapp-backend

# Set environment variables
heroku config:set NODE_ENV=production
heroku config:set PORT=3000
heroku config:set FIREBASE_PROJECT_ID=your-project-id
# ... set tất cả env vars

# Deploy
git push heroku main

# View logs
heroku logs --tail
```

### Docker (Optional)

Tạo `Dockerfile`:

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

EXPOSE 3000

CMD ["node", "dist/main"]
```

Build và run:

```bash
docker build -t foodapp-backend .
docker run -p 3000:3000 --env-file .env foodapp-backend
```

## 📚 Tài liệu Liên quan

- [QUICKSTART.md](./QUICKSTART.md) - Hướng dẫn nhanh chạy backend
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc
- [API_CONTRACT.md](./API_CONTRACT.md) - API documentation
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Quy ước lập trình

## 💬 Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra [Troubleshooting](#-troubleshooting)
2. Tìm kiếm issues trên GitHub repository
3. Hỏi team lead hoặc senior developers
4. Tạo issue mới với đầy đủ thông tin (error logs, steps to reproduce)

---

**Happy Coding! 🚀**
