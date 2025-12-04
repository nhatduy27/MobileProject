# 🧾 BACKEND_CHEATSHEET.md — Tóm tắt nhanh cho Backend FoodApp

> File này dành cho bạn nào muốn nắm nhanh backend trong **3 phút**.  
> Không thay thế tài liệu chính — chỉ là tờ “phao” để tra cứu.

---

# ⚙️ 1. Tech Stack chính

- **NestJS**
- **TypeScript**
- **Firebase (stub) → sẽ tích hợp thật**
- **Clean-lite: Ports & Adapters + Layered Architecture**
- **Dependency Injection**
- (Tùy chọn sau này) Redis / BullMQ / Firestore

---

# 📂 2. Cấu trúc thư mục ngắn gọn

```
src/
  modules/
    <feature>/
      domain/          # Entity + RepositoryPort
      infra/           # Adapter (Firebase/Mock)
      dto/
      <feature>.service.ts
      <feature>.controller.ts
      <feature>.module.ts

  shared/
    cache/             # CachePort + InMemoryAdapter
    events/            # EventBusPort + InMemoryAdapter
    notifications/     # NotificationPort + FCMAdapter (stub)
```

---

# 🧱 3. Luồng xử lý chuẩn

```
Client → Controller → Service → RepositoryPort → RepositoryAdapter → Firebase/DB
```

---

# ❗ Quy tắc vàng (Quan trọng)

### ✔ Controller:
- Không chứa nghiệp vụ  
- Không gọi Firebase trực tiếp  
- Chỉ gọi service  

### ✔ Service:
- Xử lý logic  
- Gọi repository thông qua **Port**  
- Không gọi Adapter trực tiếp  

### ✔ Repository:
- interface (Port) → trong domain  
- implementation (Adapter) → trong infra  
- Adapter mới được phép gọi Firebase/SDK  

---

# 🔥 4. Cách tạo module mới (tóm tắt)

```
1. Tạo folder src/modules/<name>
2. Tạo domain/<name>.entity.ts
3. Tạo domain/<name>.repository.ts (Port)
4. Tạo infra/firebase-<name>.repository.ts (Adapter)
5. Tạo dto/
6. Tạo service
7. Tạo controller
8. Bind Port ↔ Adapter trong module.ts
```

Xem chi tiết hơn ở:  
`docs/HOW_TO_ADD_A_NEW_MODULE.md`

---

# 💻 5. Các lệnh thường dùng

```bash
npm run start:dev     # chạy ở chế độ watch
npm run start         # chạy production mode local
```

---

# 📬 6. API chính bạn cần nhớ

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Orders
- `POST /api/orders`
- `GET /api/orders/:id`
- `PATCH /api/orders/:id/status`

Xem chi tiết hơn:  
`docs/API_CONTRACT.md`

---

# 🧪 7. Testing (cơ bản)
- Mock Port để test Service  
- Không test Adapter cùng lúc  

---

# 🧯 8. Khi bị lỗi, kiểm tra:

1. File `.env`
2. Port/Adapter chưa bind trong module?
3. Import module thiếu trong `app.module.ts`?
4. Firebase credentials chưa cấu hình?

---

# 🎯 9. Bạn chỉ cần nhớ:

- “Service gọi Port, Adapter gọi Firebase/Infra.”
- “Không business trong controller.”
- “Không Firebase trong service.”

Chúc bạn code backend thật sung! 🚀
