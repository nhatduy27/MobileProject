# Shippers Module Guide - Backend (Extended)

> Module: Shipper Applications, Owner Review, Shipper Notifications
> Base Paths:
> - Public shipper apply: `/shipper-applications`
> - Owner manage shippers: `/owner/shippers`
> - Shipper notifications: `/shippers/notifications`
> Status: Implemented

---

## 1. Overview

Shippers module quan ly quy trinh apply lam shipper, owner duyet/tu choi, danh sach shipper thuoc shop, va thong bao online/offline cua shipper. Tai lieu nay da can chinh theo code backend hien tai (controllers + service + DTO).

### 1.1 Feature Matrix

| Feature | Endpoint | Description |
|---|---|---|
| Apply shipper | `POST /shipper-applications` | Shipper nop don (multipart + 3 anh) |
| My applications | `GET /shipper-applications/me` | Xem danh sach don cua minh |
| Cancel application | `DELETE /shipper-applications/{id}` | Huy don (chi PENDING) |
| Owner list applications | `GET /owner/shippers/applications` | Xem danh sach don (filter status) |
| Owner approve | `POST /owner/shippers/applications/{id}/approve` | Duyet don |
| Owner reject | `POST /owner/shippers/applications/{id}/reject` | Tu choi don |
| Owner list shippers | `GET /owner/shippers` | Danh sach shipper cua shop |
| Owner remove shipper | `DELETE /owner/shippers/{id}` | Xoa shipper khoi shop |
| Shipper go online | `POST /shippers/notifications/online` | Subscribe topic nhan ORDER_READY |
| Shipper go offline | `DELETE /shippers/notifications/online` | Unsubscribe topic |

### 1.2 Scope / Not In Scope

- Dang ky tai khoan SHIPPER nam trong Auth module.
- Doanh thu shipper nam trong Wallet/Revenue module.
- GPS/Trip la module khac (tham khao docs rieng neu can).

---

## 2. Data Models

### 2.1 Shipper Application Entity

```json
{
  "id": "app_abc123",
  "userId": "uid_123",
  "userName": "Nguyen Van A",
  "userPhone": "0901234567",
  "userAvatar": "https://...",
  "shopId": "shop_abc",
  "shopName": "Quan A Map",
  "vehicleType": "MOTORBIKE",
  "vehicleNumber": "59X1-12345",
  "idCardNumber": "079202012345",
  "idCardFrontUrl": "https://...",
  "idCardBackUrl": "https://...",
  "driverLicenseUrl": "https://...",
  "message": "Toi muon lam shipper...",
  "status": "PENDING",
  "reviewedBy": "owner_uid_123",
  "reviewedAt": "2026-01-13T10:00:00Z",
  "rejectReason": "Khong du dieu kien",
  "createdAt": "2026-01-13T10:00:00Z"
}
```

### 2.2 Shipper Entity

```json
{
  "id": "uid_123",
  "name": "Nguyen Van A",
  "phone": "0901234567",
  "avatar": "https://...",
  "shipperInfo": {
    "shopId": "shop_abc",
    "shopName": "Quan A Map",
    "vehicleType": "MOTORBIKE",
    "vehicleNumber": "59X1-12345",
    "status": "AVAILABLE",
    "rating": 4.8,
    "totalDeliveries": 150,
    "currentOrders": ["order_1", "order_2"],
    "joinedAt": "2026-01-13T10:00:00Z"
  }
}
```

### 2.3 Enums

**ApplicationStatus**
- PENDING
- APPROVED
- REJECTED

**VehicleType**
- MOTORBIKE
- CAR
- BICYCLE

**ShipperStatus**
- AVAILABLE
- BUSY
- OFFLINE

---

## 3. Authentication & Authorization

### 3.1 Auth required

Tat ca endpoints trong module deu can Firebase ID Token:

```http
Authorization: Bearer <firebase-id-token>
```

### 3.2 Role rules

- `POST /shipper-applications`, `GET /shipper-applications/me`, `DELETE /shipper-applications/{id}`:
  bat ky user da dang nhap (khong bat buoc role SHIPPER), mien khong da co shipperInfo.shopId.
- `/owner/shippers/**`: bat buoc role OWNER.
- `/shippers/notifications/**`: bat buoc role SHIPPER.

---

## 4. Business Rules (Theo code)

1) **User da co shipperInfo.shopId thi khong duoc apply lai**
- Tra ve `SHIPPER_001` (409).

2) **Khong apply trung shop khi da co PENDING**
- Tra ve `SHIPPER_005` (409).

3) **Tao application bat buoc 3 anh**
- idCardFront, idCardBack, driverLicense la bat buoc.
- Chi chap nhan JPG/JPEG/PNG, toi da 5MB/anh.

4) **Approve chi duoc khi PENDING**
- Neu status khac PENDING -> 409 "Da xu ly".

5) **Owner khong duoc tu duyet minh**
- Neu app.userId == ownerId -> BadRequest `SHIPPER_BUG`.

6) **Sau khi approve**
- Firestore update role = SHIPPER, shipperInfo, claimsSyncStatus = PENDING
- Set custom claims role SHIPPER
- Nếu sync claims fail: claimsSyncStatus = FAILED, user can re-login
- Tao vi shipper (wallet) bat dong bo
- Gui notification cho shipper

7) **Remove shipper**
- Clear shipperInfo, set role = CUSTOMER
- Update Firebase custom claims to CUSTOMER

---

## 5. API Endpoints (Shipper Applications)

### 5.1 Apply to be Shipper

```http
POST /shipper-applications
Authorization: Bearer <ID_TOKEN>
Content-Type: multipart/form-data

Fields:
- shopId (string, required)
- vehicleType (enum: MOTORBIKE/CAR/BICYCLE, required)
- vehicleNumber (string, required, max 20)
- idCardNumber (string, required, 12 digits)
- idCardFront (file, required)
- idCardBack (file, required)
- driverLicense (file, required)
- message (string, required, max 500)
```

**Success (201)**
```json
{
  "success": true,
  "data": {
    "id": "app_abc123",
    "status": "PENDING"
  }
}
```

**Error cases**
- 404: Shop not found
- 409: `SHIPPER_001` (da co shop)
- 409: `SHIPPER_005` (da co don PENDING cho shop nay)
- 400: Sai dinh dang anh / qua dung luong / upload fail

### 5.2 Get My Applications

```http
GET /shipper-applications/me
Authorization: Bearer <ID_TOKEN>
```

**Success (200)**
```json
{
  "success": true,
  "data": [
    {
      "id": "app_abc123",
      "shopName": "Quan A Map",
      "status": "PENDING",
      "createdAt": "2026-01-13T10:00:00Z"
    }
  ]
}
```

### 5.3 Cancel Application

```http
DELETE /shipper-applications/{id}
Authorization: Bearer <ID_TOKEN>
```

**Success (200)**
```json
{ "message": "Huy don thanh cong" }
```

**Rules**
- Chi cancel khi status = PENDING
- Chi user so huu don moi co quyen huy

---

## 6. API Endpoints (Owner - Shippers)

### 6.1 List Applications (Owner)

```http
GET /owner/shippers/applications?status=PENDING
Authorization: Bearer <ID_TOKEN>
```

**Query**
- `status` (optional): PENDING, APPROVED, REJECTED

### 6.2 Approve Application

```http
POST /owner/shippers/applications/{id}/approve
Authorization: Bearer <ID_TOKEN>
```

**Success (200)**
```json
{ "message": "Da duyet don xin lam shipper" }
```

**Error**
- 404: Don khong ton tai
- 403: Don khong thuoc shop cua owner
- 409: Don da xu ly
- 400: `SHIPPER_BUG` (owner tu duyet minh)

### 6.3 Reject Application

```http
POST /owner/shippers/applications/{id}/reject
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json

{
  "reason": "Khong du dieu kien"
}
```

**Success (200)**
```json
{ "message": "Da tu choi don xin lam shipper" }
```

### 6.4 List Shop Shippers

```http
GET /owner/shippers
Authorization: Bearer <ID_TOKEN>
```

**Response Example**
```json
{
  "success": true,
  "data": [
    {
      "id": "uid_123",
      "name": "Nguyen Van A",
      "phone": "0901234567",
      "avatar": "https://...",
      "shipperInfo": {
        "shopId": "shop_abc",
        "shopName": "Quan A Map",
        "vehicleType": "MOTORBIKE",
        "vehicleNumber": "59X1-12345",
        "status": "AVAILABLE",
        "rating": 4.8,
        "totalDeliveries": 150,
        "currentOrders": ["order_1"],
        "joinedAt": "2026-01-01T00:00:00Z"
      }
    }
  ]
}
```

### 6.5 Remove Shipper

```http
DELETE /owner/shippers/{id}
Authorization: Bearer <ID_TOKEN>
```

**Success (200)**
```json
{ "message": "Da xoa shipper khoi shop" }
```

**Notes**
- Co the that bai neu shipper khong thuoc shop owner.
- Khi remove: role ve CUSTOMER, xoa shipperInfo, sync claims.

---

## 7. API Endpoints (Shipper Notifications)

### 7.1 Go Online

```http
POST /shippers/notifications/online
Authorization: Bearer <ID_TOKEN>
```

**Response**
```json
{
  "subscribedCount": 1,
  "topic": "shop_abc123_shippers_active"
}
```

### 7.2 Go Offline

```http
DELETE /shippers/notifications/online
Authorization: Bearer <ID_TOKEN>
```

**Response**
```json
{
  "unsubscribedCount": 1,
  "topic": "shop_abc123_shippers_active"
}
```

**Error**
- 400: Shipper profile not found or not assigned to a shop

---

## 8. Error Codes Summary

| Code | Status | Description |
|---|---|---|
| SHIPPER_001 | 409 | User da la shipper cua mot shop |
| SHIPPER_005 | 409 | Da nop don PENDING cho shop nay |
| SHIPPER_BUG | 400 | Owner tu duyet minh (sanity check) |

Ngoai ra co cac loi chung:
- 404: Application not found / Shop not found
- 403: Khong co quyen tren don/shipper
- 400: Validate input, upload anh fail

---

## 9. Validation & Constraints

### 9.1 ApplyShipperDto
- shopId: required
- vehicleType: MOTORBIKE/CAR/BICYCLE
- vehicleNumber: max 20 chars
- idCardNumber: 12 digits
- message: required, max 500 chars

### 9.2 Image rules
- JPG/JPEG/PNG
- Max 5MB per file

---

## 10. Flows

### 10.1 Shipper Apply Flow
1. User dang nhap
2. POST /shipper-applications (multipart)
3. Backend:
   - Check shipperInfo.shopId
   - Check shop exists
   - Check pending app
   - Validate image types + size
   - Upload 3 images
   - Create application (PENDING)
   - Send notification to shop owner

### 10.2 Owner Approve Flow
1. Owner GET /owner/shippers/applications
2. Owner POST approve
3. Backend:
   - Validate ownership + PENDING
   - Transaction: update application + update user role/shipperInfo
   - Sync Firebase claims (role SHIPPER)
   - Init shipper wallet (best effort)
   - Send notification to shipper

### 10.3 Owner Reject Flow
1. Owner POST reject + reason
2. Backend update status REJECTED + save reason
3. Notify shipper

### 10.4 Shipper Online Flow
1. Shipper POST /shippers/notifications/online
2. Backend subscribe topic `shop_{shopId}_shippers_active`
3. When offline, call DELETE to unsubscribe

---

## 11. Testing Checklist

- [ ] Apply shipper with full docs (3 images)
- [ ] Apply with wrong image type/size -> 400
- [ ] Apply when already shipper -> SHIPPER_001
- [ ] Apply duplicate PENDING -> SHIPPER_005
- [ ] Get my applications
- [ ] Cancel PENDING application
- [ ] Cancel when status != PENDING -> 409
- [ ] Owner list applications (filter status)
- [ ] Owner approve application
- [ ] Owner reject application (with reason)
- [ ] Owner remove shipper
- [ ] Shipper go online/offline
- [ ] Claims sync fail -> verify claimsSyncStatus = FAILED

---

## 12. Testing with cURL

```bash
# Apply shipper
curl -X POST http://localhost:3000/shipper-applications \
  -H "Authorization: Bearer <token>" \
  -F "shopId=shop_abc" \
  -F "vehicleType=MOTORBIKE" \
  -F "vehicleNumber=59X1-12345" \
  -F "idCardNumber=079202012345" \
  -F "message=Toi muon lam shipper" \
  -F "idCardFront=@cccd_front.jpg" \
  -F "idCardBack=@cccd_back.jpg" \
  -F "driverLicense=@license.jpg"

# My applications
curl -X GET http://localhost:3000/shipper-applications/me \
  -H "Authorization: Bearer <token>"

# Cancel application
curl -X DELETE http://localhost:3000/shipper-applications/app_abc123 \
  -H "Authorization: Bearer <token>"

# Owner list applications
curl -X GET "http://localhost:3000/owner/shippers/applications?status=PENDING" \
  -H "Authorization: Bearer <owner_token>"

# Owner approve
curl -X POST http://localhost:3000/owner/shippers/applications/app_abc123/approve \
  -H "Authorization: Bearer <owner_token>"

# Owner reject
curl -X POST http://localhost:3000/owner/shippers/applications/app_abc123/reject \
  -H "Authorization: Bearer <owner_token>" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Khong du dieu kien"}'

# Owner list shippers
curl -X GET http://localhost:3000/owner/shippers \
  -H "Authorization: Bearer <owner_token>"

# Owner remove shipper
curl -X DELETE http://localhost:3000/owner/shippers/uid_123 \
  -H "Authorization: Bearer <owner_token>"

# Shipper go online
curl -X POST http://localhost:3000/shippers/notifications/online \
  -H "Authorization: Bearer <shipper_token>"

# Shipper go offline
curl -X DELETE http://localhost:3000/shippers/notifications/online \
  -H "Authorization: Bearer <shipper_token>"
```

---

## 13. UI/UX Notes (Shipper App)

### 13.1 Status Mapping

| Condition | UI State |
|---|---|
| role != SHIPPER | Show registration / upgrade flow |
| role == SHIPPER, shipperInfo.shopId == null | Prompt apply to shop |
| application PENDING | Waiting approval screen |
| application REJECTED | Show reason + re-apply CTA |
| shipperInfo.shopId != null | Shipper home |

### 13.2 Online/Offline

- Khi user bam "Online", call `POST /shippers/notifications/online`.
- Khi user tat, call `DELETE /shippers/notifications/online`.
- Hien thi thong bao ORDER_READY neu da subscribe.

---

## 14. FAQ

**Q: Co the apply nhieu shop cung luc khong?**  
A: Khong. Chi 1 don PENDING cho 1 shop, va neu da la shipper thi khong duoc apply.

**Q: Sau khi duoc duyet can lam gi?**  
A: Re-login de cap nhat Firebase custom claims (role SHIPPER) neu can.

**Q: Neu claims sync fail thi sao?**  
A: Backend set claimsSyncStatus = FAILED, user can re-login hoac admin retry.

**Q: Owner co the tu duyet minh lam shipper khong?**  
A: Khong. Co check `SHIPPER_BUG`.

---

## 15. Related Files

- `D:\MobileProject\Backend\functions\src\modules\shippers\shipper-applications.controller.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\owner-shippers.controller.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\shipper-notifications.controller.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\shippers.service.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\entities\shipper-application.entity.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\entities\shipper.entity.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\dto\apply-shipper.dto.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\dto\reject-application.dto.ts`

---

## 16. Troubleshooting

1. Backend logs: terminal dang chay `npm start`
2. Firebase console: Firestore + Auth
3. Swagger docs: http://localhost:3000/api/docs
4. Claims mismatch: user re-login hoac admin sync claims
5. Upload errors: check image type/size
