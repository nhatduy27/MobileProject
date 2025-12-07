# ADR-001: Tại Sao Chúng Ta Chọn Firebase Functions Thay Vì NestJS?

**Trạng thái:** Chấp nhận  
**Ngày quyết định:** Tháng 11, 2025  
**Người quyết định:** Backend Team  

---

## Bối Cảnh

Dự án mobile app (iOS/Android/Flutter) cần backend để xử lý:
- Đặt hàng (order processing)
- Quản lý nhà hàng (restaurant management)
- Xác thực người dùng (authentication)
- Thông báo real-time (notifications)
- Xử lý thanh toán (payments)

Team nhỏ (2-3 developers), không có DevOps engineer chuyên dụng. Cần giải pháp:
- Deploy nhanh
- Scale tự động
- Chi phí thấp khi bắt đầu
- Tích hợp Firebase Ecosystem (Auth, Firestore, Storage)

## Quyết Định

**Chúng tôi chọn Firebase Cloud Functions cho backend thay vì NestJS trên server riêng.**

## So Sánh Chi Tiết

| Tiêu Chí | Firebase Functions ⭐ | NestJS Server |
|----------|---------------------|---------------|
| **Startup time** | ~1 phút (first deploy) | ~30 phút (setup server) |
| **Scaling** | Tự động (serverless) | Manual (thêm instances) |
| **Chi phí ban đầu** | $0-1/tháng | $15-100/tháng (VPS/Cloud) |
| **Database** | Firestore tích hợp | Cần setup PostgreSQL/MongoDB |
| **Authentication** | Firebase Auth tích hợp | Phải implement JWT/OAuth |
| **DevOps** | Google quản lý | Team tự quản lý |
| **Real-time updates** | Firestore listeners | Websockets tự implement |
| **Event-driven** | Native support (triggers) | Custom implementation |
| **Monitoring** | Firebase Console có sẵn | Cần setup (Grafana, etc.) |
| **CI/CD** | Firebase CLI đơn giản | Cần setup Docker/K8s |
| **Learning curve** | Vừa phải | Cao (NestJS + Infrastructure) |

## Lợi Ích

### 1. **Không Cần DevOps**
```
Firebase Functions:
- Google quản lý infrastructure
- Auto-scaling theo traffic
- Không cần lo về server maintenance
```

### 2. **Pay-Per-Use Pricing**
```
Tháng 1: 1,000 requests    → $0.10
Tháng 2: 50,000 requests   → $3.50
Tháng 3: 200,000 requests  → $12.00

NestJS Server:
Tháng 1-3: $50/tháng (DigitalOcean Droplet)
          + $10/tháng (Database)
          = $60/tháng (dù chỉ có 1,000 requests)
```

### 3. **Tích Hợp Firebase Ecosystem**
```typescript
// Authentication - Tích hợp sẵn
export const placeOrder = onCall(async (request) => {
  const userId = request.auth?.uid;  // ✅ Sẵn có
  // ...
});

// Firestore - Triggers tự động
export const onOrderCreated = onDocumentCreated("orders/{orderId}", 
  async (event) => {
    // ✅ Tự động chạy khi order được tạo
  }
);

// Real-time updates - Client tự subscribe
const unsubscribe = onSnapshot(doc(db, "orders", orderId), (doc) => {
  console.log("Order updated:", doc.data());
});
```

### 4. **Event-Driven Architecture - Native**
```typescript
// Firebase có built-in triggers
onDocumentCreated()   // Firestore document created
onDocumentUpdated()   // Firestore document updated
onUserCreated()       // Auth user created
onSchedule()          // Cron jobs

// NestJS: phải tự implement event bus
```

### 5. **Deploy Đơn Giản**
```bash
# Firebase Functions
firebase deploy --only functions
# ✅ Done trong 1-2 phút

# NestJS Server
1. Build Docker image
2. Push to registry
3. Deploy to K8s/AWS/GCP
4. Setup load balancer
5. Configure environment
# ❌ 30-60 phút setup lần đầu
```

### 6. **Monitoring & Logging**
```
Firebase Console:
- Logs tự động (Cloud Logging)
- Metrics (invocations, errors, latency)
- Alerts qua email/SMS
- ✅ Không cần setup gì

NestJS:
- Phải setup Grafana/Prometheus
- Custom logging (Winston, etc.)
- Alerting system riêng
```

## Trade-offs (Nhược Điểm)

### 1. **Cold Start Latency**
```
Firebase Functions:
- Cold start: ~1-3 giây (first request)
- Warm: <100ms
- ❌ Không phù hợp real-time gaming

Mobile App:
- Acceptable (user không nhận ra 1-2s delay)
- ✅ OK cho đặt hàng, thanh toán
```

### 2. **Vendor Lock-in**
```
Firebase:
- Code phụ thuộc Firebase SDK
- Khó migrate sang AWS/Azure
- ❌ Locked vào Google Cloud

Mitigation:
- Business logic trong services (không phụ thuộc Firebase)
- Chỉ triggers & repositories dùng Firebase SDK
- ✅ Có thể migrate nếu cần (nhưng tốn công)
```

### 3. **Timeout 60 Giây**
```typescript
// ❌ BAD - Long-running task
export const generateReport = onCall(async (request) => {
  // Process 100,000 orders → 120 giây
  // ❌ Firebase timeout sau 60s
});

// ✅ GOOD - Queue-based
export const generateReport = onCall(async (request) => {
  // Tạo task trong queue
  await queueReport(request.data);
  return { taskId: "task_123", status: "PROCESSING" };
});

// Worker function xử lý task
export const processReportQueue = onTaskDispatched(async (task) => {
  // Process trong background, không timeout
});
```

### 4. **Khó Test Locally**
```bash
# Firebase Emulator có giới hạn:
- Không emulate tất cả Firebase features
- Emulator chậm hơn production
- Auth triggers đôi khi không fire

# Solution:
- Unit tests cho services (mock repositories)
- Integration tests với emulator
- Staging environment trên Firebase
```

### 5. **Debugging Khó Hơn**
```
NestJS:
- Attach debugger trực tiếp
- Console.log ngay lập tức
- ✅ Easy debugging

Firebase Functions:
- Logs delay 5-10 giây (Cloud Logging)
- Không attach debugger trực tiếp
- ❌ Phải rely on logs + emulator
```

## Khi Nào NÊN Dùng NestJS?

1. **Latency cực thấp (<10ms response time)**
   - Gaming backends
   - Trading platforms
   - Video streaming APIs

2. **Long-running tasks (>60 seconds)**
   - Video encoding
   - Batch processing
   - Data migrations

3. **Custom infrastructure requirements**
   - Special networking (VPN, private subnets)
   - Custom OS-level dependencies
   - Hardware acceleration (GPU)

4. **Multi-cloud strategy**
   - Deploy trên AWS + GCP + Azure
   - Avoid vendor lock-in
   - Control hoàn toàn infrastructure

## Kết Luận

**Firebase Functions là lựa chọn đúng đắn cho dự án này vì:**

✅ Team nhỏ, không có DevOps  
✅ Mobile app backend (không yêu cầu latency cực thấp)  
✅ Tích hợp Firebase (Auth, Firestore, Storage, Hosting)  
✅ Pay-per-use phù hợp khi bắt đầu  
✅ Event-driven architecture native support  
✅ Deploy & scale tự động  

**Trade-offs acceptable:**
- Cold start 1-3s → OK cho mobile app
- Vendor lock-in → Có thể migrate nếu cần (services độc lập)
- Timeout 60s → Dùng queue cho long tasks
- Local testing khó → Unit tests + emulator + staging

**Quyết định này có thể review lại khi:**
- Traffic lớn (>10M requests/tháng) → Chi phí cao hơn NestJS
- Cần latency <100ms → Consider dedicated servers
- Cần features không support trên Firebase

---

**Tham khảo:**
- [Firebase Functions Pricing](https://firebase.google.com/pricing)
- [NestJS Documentation](https://docs.nestjs.com/)
- [Serverless vs Traditional Servers](https://www.cloudflare.com/learning/serverless/why-use-serverless/)

**Cập nhật lần cuối:** 7 Tháng 12, 2025
