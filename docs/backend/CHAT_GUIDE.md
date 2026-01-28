# 💬 Chat Module - Frontend Integration Guide

> **Module:** chat  
> **Version:** 1.0  
> **Last Updated:** 2026-01-29  
> **Backend Status:** ✅ API Tested & Ready

---

## 📖 Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Real-time Strategy](#2-real-time-strategy-quan-trọng)
3. [API Endpoints Reference](#3-api-endpoints-reference)
4. [Data Models](#4-data-models)
5. [UI Mockup & User Flow](#5-ui-mockup--user-flow)
6. [Implementation Flow](#6-implementation-flow-chi-tiết)
7. [Error Handling](#7-error-handling)
8. [Performance & Best Practices](#8-performance--best-practices)

---

## 1. Tổng Quan

Chat Module cho phép **1-1 text chat realtime** giữa các user roles (Customer ↔ Owner ↔ Shipper).

### ✨ Key Features

- ✅ Text messaging (max 1000 chars)
- ✅ Read receipts (SENT ↔ READ status)
- ✅ **Real-time updates** via Firestore listeners
- ✅ FCM push notifications (khi app background)
- ✅ Cursor-based pagination
- ✅ Deterministic conversation IDs (tránh duplicates)

### 🔑 Conversation ID Format

```
{minUserId}__{maxUserId}
```

**Example:**

- User A: `xyz9876`
- User B: `abcd1234`
- → Conversation ID: `abcd1234__xyz9876` (sorted alphabetically)

**Tại sao deterministic?**  
→ Đảm bảo chỉ có **1 conversation duy nhất** giữa 2 users, dù ai tạo trước.

---

## 2. Real-time Strategy (QUAN TRỌNG)

> ⚠️ **Không dùng polling!** Sử dụng Firestore Realtime Listeners để có UX tốt nhất.

### 2.1. Hybrid Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                   CHAT REAL-TIME ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐         ┌─────────────────┐                   │
│  │  REST API       │         │  Firestore      │                   │
│  │  (HTTP)         │         │  (WebSocket)    │                   │
│  └────────┬────────┘         └────────┬────────┘                   │
│           │                           │                            │
│  ┌────────▼───────────────────────────▼───────────┐                │
│  │        KHI NÀO DÙNG API vs FIRESTORE?          │                │
│  ├────────────────────────────────────────────────┤                │
│  │                                                │                │
│  │  📡 REST API (1-time operations):             │                │
│  │    ├─ POST /conversations (create/get)        │                │
│  │    ├─ POST /messages (send message)           │                │
│  │    └─ PUT /messages/:id/read (mark read)      │                │
│  │                                                │                │
│  │  🔥 Firestore Listeners (continuous):         │                │
│  │    ├─ /conversations (danh sách trò chuyện)   │                │
│  │    └─ /conversations/:id/messages (tin nhắn)  │                │
│  │                                                │                │
│  └────────────────────────────────────────────────┘                │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2. Write vs Read Strategy

| Operation              | Method             | Purpose                          | Realtime?     |
| ---------------------- | ------------------ | -------------------------------- | ------------- |
| **Tạo conversation**   | REST API           | Create/get conversation ID       | ❌ 1-time     |
| **Gửi message**        | REST API           | Write to Firestore + trigger FCM | ❌ 1-time     |
| **Mark as read**       | REST API           | Update status                    | ❌ 1-time     |
| **Xem danh sách chat** | Firestore Listener | Realtime conversations list      | ✅ Continuous |
| **Xem tin nhắn**       | Firestore Listener | Realtime messages stream         | ✅ Continuous |

### 2.3. Flow Chi Tiết: Send Message

```
┌──────────────────────────────────────────────────────────────────┐
│                    SEND MESSAGE FLOW                             │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  USER A (Sender)                                                 │
│  ─────────────                                                   │
│                                                                  │
│  [1] User nhấn Send                                              │
│       │                                                          │
│       ├─► [2] Optimistic UI: thêm message vào list ngay          │
│       │        (status: PENDING, placeholder ID)                 │
│       │                                                          │
│       └─► [3] Call REST API: POST /chat/messages                │
│                │                                                 │
│                └─► [4] Backend writes to Firestore               │
│                         (Firestore Transaction)                  │
│                         ├─ Create message doc                    │
│                         └─ Update conversation.lastMessage       │
│                                                                  │
│  ─────────────────────────────────────────────────────────────   │
│                                                                  │
│  FIRESTORE (Real-time Database)                                 │
│  ────────────────────────────                                   │
│                                                                  │
│  [5] Firestore emits event: new message added                   │
│       │                                                          │
│       ├─► USER A (Listener)                                      │
│       │    └─ Replace PENDING message with real data            │
│       │       (real ID, timestamp, status: SENT)                │
│       │                                                          │
│       └─► USER B (Listener)                                      │
│            └─ New message appears instantly                      │
│               (no API call needed!)                              │
│                                                                  │
│  ─────────────────────────────────────────────────────────────   │
│                                                                  │
│  USER B (Recipient - App in background)                         │
│  ───────────────────────────────────────                        │
│                                                                  │
│  [6] FCM Push Notification                                      │
│       └─► "Nguyễn Văn A: Hello!"                                │
│            └─ Tap notification → open chat                      │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### 2.4. Tại Sao Hybrid Approach?

| Aspect            | Pure API Polling      | Pure Firestore SDK     | **Hybrid (Recommended)** |
| ----------------- | --------------------- | ---------------------- | ------------------------ |
| Realtime          | ❌ Delay 3-5s         | ✅ Instant (<100ms)    | ✅ Instant               |
| Write security    | ✅ Backend validation | ⚠️ Client rules only   | ✅ Backend validation    |
| Business logic    | ✅ Centralized        | ❌ Client-side         | ✅ Centralized           |
| Network cost      | ❌ High (polling)     | ✅ Low (WebSocket)     | ✅ Low                   |
| FCM notifications | ✅ Server-side        | ❌ Need Cloud Function | ✅ Server-side           |

**Kết luận:**  
→ **Write qua REST API** (đảm bảo validation, FCM)  
→ **Read qua Firestore Listeners** (đảm bảo realtime)

---

## 3. API Endpoints Reference

---

## 3. API Endpoints Reference

**Base URL:** `https://your-cloud-function-url/api` (prod) hoặc `http://localhost:3000/api` (dev)

**Authentication:** Tất cả endpoints cần `Authorization: Bearer {firebaseIdToken}`

> 💡 **Lưu ý:** API chỉ dùng cho **WRITE operations**. Read operations dùng Firestore Listeners.

### 3.1. POST /chat/conversations

**Mục đích:** Tạo hoặc lấy conversation ID để bắt đầu chat.

**Khi nào dùng:**

- User nhấn nút "Nhắn tin" trên profile/shop/order
- Trước khi navigate vào Chat Detail Screen

```http
POST /chat/conversations
Content-Type: application/json
Authorization: Bearer {token}

{
  "participantId": "userId_of_recipient"
}
```

**Response 201:**

```json
{
  "success": true,
  "data": {
    "id": "userA__userB",
    "participants": ["userA", "userB"],
    "lastMessage": "",
    "lastMessageAt": "2026-01-29T00:00:00Z",
    "lastSenderId": "",
    "createdAt": "2026-01-29T00:00:00Z",
    "updatedAt": "2026-01-29T00:00:00Z"
  }
}
```

**Error 400:** `Cannot create conversation with yourself`

---

### 3.2. POST /chat/messages

**Mục đích:** Gửi tin nhắn.

**Khi nào dùng:**

- User nhấn Send button trong chat

```http
POST /chat/messages
Content-Type: application/json
Authorization: Bearer {token}

{
  "conversationId": "userA__userB",
  "text": "Hello! Max 1000 chars"
}
```

**Response 201:**

```json
{
  "success": true,
  "data": {
    "id": "msg_xyz123",
    "senderId": "currentUserId",
    "text": "Hello! Max 1000 chars",
    "status": "SENT",
    "createdAt": "2026-01-29T00:05:00Z"
  }
}
```

**Backend sẽ tự động:**

1. ✅ Validate text length
2. ✅ Check user là participant
3. ✅ Update `conversation.lastMessage`
4. ✅ Gửi FCM push notification cho recipient (async)

**Error 400:** Text > 1000 chars  
**Error 403:** User không phải participant  
**Error 404:** Conversation không tồn tại

---

### 3.3. PUT /chat/messages/:messageId/read

**Mục đích:** Đánh dấu tin nhắn đã đọc.

**Khi nào dùng:**

- Khi user scroll đến message trong viewport
- Hoặc khi user mở Chat Detail Screen (mark tất cả unread)

```http
PUT /chat/messages/msg_xyz123/read
Content-Type: application/json
Authorization: Bearer {token}

{
  "conversationId": "userA__userB"
}
```

**Response 200:**

```json
{
  "success": true,
  "data": {
    "id": "msg_xyz123",
    "senderId": "otherUserId",
    "text": "Hello!",
    "status": "READ",
    "readAt": "2026-01-29T00:10:00Z",
    "createdAt": "2026-01-29T00:05:00Z"
  }
}
```

**Error 400:** Cannot mark own message as read  
**Error 403:** User không phải participant

---

### 3.4. GET /chat/conversations (Optional)

> ⚠️ **Không khuyến nghị dùng endpoint này!** Dùng Firestore Listener thay vì.

**Khi nào dùng:**

- Chỉ khi cần load initial data 1 lần
- Sau đó **PHẢI** switch sang Firestore Listener

```http
GET /chat/conversations?limit=20&startAfter={conversationId}
Authorization: Bearer {token}
```

**Better approach:** Xem section 6.1 để dùng Firestore Listener.

---

### 3.5. GET /chat/conversations/:id/messages (Optional)

> ⚠️ **Không khuyến nghị!** Dùng Firestore Listener.

```http
GET /chat/conversations/userA__userB/messages?limit=50&startAfter={messageId}
Authorization: Bearer {token}
```

**Better approach:** Xem section 6.2.

---

## 4. Data Models

### 4.1. Conversation (Firestore Path: `conversations/{id}`)

```typescript
interface Conversation {
  id: string; // "{minUid}__{maxUid}"
  participants: string[]; // [userId1, userId2] sorted
  lastMessage: string; // Truncated preview (max 100 chars)
  lastMessageAt: Date; // Sort key cho conversation list
  lastSenderId: string; // Để hiển thị "You: " prefix
  createdAt: Date;
  updatedAt: Date;
}
```

**Ví dụ:**

```json
{
  "id": "alice123__bob456",
  "participants": ["alice123", "bob456"],
  "lastMessage": "Cảm ơn bạn đã đặt hàng!",
  "lastMessageAt": "2026-01-29T10:30:00Z",
  "lastSenderId": "bob456",
  "createdAt": "2026-01-28T14:00:00Z",
  "updatedAt": "2026-01-29T10:30:00Z"
}
```

---

### 4.2. Message (Firestore Path: `conversations/{conversationId}/messages/{id}`)

```typescript
interface Message {
  id: string;
  senderId: string;
  text: string; // Max 1000 chars
  status: "PENDING" | "SENT" | "READ";
  readAt?: Date; // Chỉ có khi status = "READ"
  createdAt: Date;
}
```

**Status meanings:**

- `PENDING`: UI optimistic, chưa lưu Firestore (client-only)
- `SENT`: Đã lưu Firestore, chưa đọc
- `READ`: Recipient đã mark as read

**Ví dụ:**

```json
{
  "id": "msg_xyz789",
  "senderId": "alice123",
  "text": "Xin chào! Đơn hàng của tôi đến khi nào?",
  "status": "READ",
  "readAt": "2026-01-29T10:31:00Z",
  "createdAt": "2026-01-29T10:30:00Z"
}
```

---

## 5. UI Mockup & User Flow

### 5.1. Conversations List Screen

```
╔═══════════════════════════════════════╗
║  ←  Tin nhắn              🔍  [⋮]    ║
╠═══════════════════════════════════════╣
║                                       ║
║ ┌───────────────────────────────────┐ ║
║ │ 👤 Nguyễn Văn A         • 2 phút │ ║ ← Unread (blue dot)
║ │    Hello! Đơn hàng của bạn...    │ ║
║ │    📍 CUSTOMER                    │ ║ ← Badge: role
║ └───────────────────────────────────┘ ║
║                                       ║
║ ┌───────────────────────────────────┐ ║
║ │ 🏪 Shop ABC Food        ✓✓ Hôm qua║ ║ ← Read (double check)
║ │    Bạn: Cảm ơn shop!              │ ║ ← "Bạn:" prefix
║ │    📍 OWNER                        │ ║
║ └───────────────────────────────────┘ ║
║                                       ║
║ ┌───────────────────────────────────┐ ║
║ │ 🛵 Shipper Minh         ✓ 2 ngày │ ║ ← Sent, not read
║ │    Bạn: Đã nhận hàng rồi ạ       │ ║
║ │    📍 SHIPPER                     │ ║
║ └───────────────────────────────────┘ ║
║                                       ║
║ [Load more conversations...]          ║
║                                       ║
╚═══════════════════════════════════════╝

🔥 FIRESTORE LISTENER ACTIVE:
   └─ conversations
      .where('participants', arrayContains: currentUserId)
      .orderBy('lastMessageAt', 'desc')
      .limit(50)
      .snapshots()
```

**UI Features:**

| Element      | Data Source                  | Update Trigger     |
| ------------ | ---------------------------- | ------------------ |
| Avatar       | User profile (cache)         | Manual fetch 1 lần |
| Name         | User profile (cache)         | Manual fetch 1 lần |
| Last message | `conversation.lastMessage`   | Firestore realtime |
| Timestamp    | `conversation.lastMessageAt` | Firestore realtime |
| Unread badge | Count unread messages        | Firestore query    |
| Read status  | Latest message status        | Firestore realtime |

---

### 5.2. Chat Detail Screen

```
╔═══════════════════════════════════════╗
║  ← Nguyễn Văn A              [⋮]     ║
║     🟢 Online • CUSTOMER              ║ ← Optional: online status
╠═══════════════════════════════════════╣
║                                       ║
║           ┌─────────────────────┐     ║
║           │ Xin chào! Tôi muốn  │     ║
║           │ hỏi về đơn hàng     │     ║
║           │   10:30 AM  ✓✓      │ ←ME ║
║           └─────────────────────┘     ║
║                                       ║
║  ┌─────────────────────┐              ║
║  │ Dạ, đơn hàng của    │              ║
║  │ anh/chị đang được   │              ║
║  │ chuẩn bị ạ!         │              ║
║  │ 10:32 AM            │ ←THEM        ║
║  └─────────────────────┘              ║
║                                       ║
║           ┌─────────────────────┐     ║
║           │ OK cảm ơn bạn!      │     ║
║           │   10:33 AM  ✓       │ ←ME ║
║           └─────────────────────┘     ║
║                                       ║
║                   ⬇ [Load older]      ║ ← Pagination
║                                       ║
╠═══════════════════════════════════════╣
║ [📎] Type a message...          [📤] ║
╚═══════════════════════════════════════╝

🔥 FIRESTORE LISTENER ACTIVE:
   └─ conversations/{conversationId}/messages
      .orderBy('createdAt', 'desc')
      .limit(50)
      .snapshots()
```

**Message Bubble Layout:**

```
┌─────────────────────────────────────────────────┐
│  SENDER (Them - Left aligned)                   │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────────────┐                   │
│  │ Message text here        │                   │
│  │ Can be multi-line...     │                   │
│  │ 10:32 AM                 │                   │
│  └──────────────────────────┘                   │
│  👤 Name (optional)                              │
│                                                 │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│  RECEIVER (Me - Right aligned)                  │
├─────────────────────────────────────────────────┤
│                                                 │
│                   ┌──────────────────────────┐  │
│                   │ My message text          │  │
│                   │ 10:33 AM  ✓✓             │  │
│                   └──────────────────────────┘  │
│                                                 │
│  Color coding:                                  │
│  • Background: Blue (me) vs Gray (them)         │
│  • Status icons:                                │
│    - ⏱ PENDING (clock)                          │
│    - ✓ SENT (single check, gray)                │
│    - ✓✓ READ (double check, blue)               │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

### 5.3. Complete User Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                     USER JOURNEY: CHAT                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ENTRY POINTS                                                    │
│  ────────────                                                    │
│                                                                  │
│  [Shop Detail]                [Order Detail]      [Profile]     │
│       │                              │                 │         │
│       ├─ "Nhắn tin Shop"             │                 │         │
│       │                              ├─ "Chat Shipper" │         │
│       │                              │                 │         │
│       └──────────────────────────────┴─────────────────┘         │
│                                │                                 │
│                                ▼                                 │
│                ┌───────────────────────────┐                     │
│                │ POST /chat/conversations  │                     │
│                │ {participantId: "xyz"}    │                     │
│                └───────────┬───────────────┘                     │
│                            │                                     │
│                            ▼                                     │
│                ┌───────────────────────────┐                     │
│                │  Get conversationId       │                     │
│                │  "alice__bob"             │                     │
│                └───────────┬───────────────┘                     │
│                            │                                     │
│                            ▼                                     │
│                ┌───────────────────────────┐                     │
│                │  Navigate to:             │                     │
│                │  ChatDetailScreen         │                     │
│                │  (conversationId)         │                     │
│                └───────────┬───────────────┘                     │
│                            │                                     │
│    ┌───────────────────────┴────────────────────┐               │
│    │                                            │               │
│    ▼                                            ▼               │
│  ┌─────────────────────┐          ┌──────────────────────┐     │
│  │ START LISTENER:     │          │ LOAD USER PROFILE    │     │
│  │ messages.snapshots()│          │ (Avatar, Name)       │     │
│  └─────────┬───────────┘          └──────────────────────┘     │
│            │                                                    │
│            ├─► onSnapshot: display messages                    │
│            │   └─ Auto scroll to bottom                        │
│            │                                                    │
│            ├─► onSnapshot: new message from other user         │
│            │   ├─ Update UI instantly                          │
│            │   └─ Auto mark as read (if visible)               │
│            │       └─ PUT /messages/:id/read                   │
│            │                                                    │
│            └─► onSnapshot: status change (SENT → READ)         │
│                └─ Update checkmarks                            │
│                                                                  │
│  USER SENDS MESSAGE                                             │
│  ──────────────────                                             │
│                                                                  │
│  [1] User types & clicks Send                                   │
│       │                                                          │
│       ├─► [2] Optimistic UI                                     │
│       │       └─ Add message to list (status: PENDING)          │
│       │          (temp ID, current timestamp)                   │
│       │                                                          │
│       └─► [3] POST /chat/messages                               │
│                  │                                               │
│                  └─► [4] Backend writes to Firestore            │
│                           └─ Listener auto-updates UI           │
│                              (replace PENDING → SENT)           │
│                                                                  │
│  RECIPIENT RECEIVES                                             │
│  ─────────────────                                              │
│                                                                  │
│  • App foreground: Listener updates instantly                   │
│  • App background: FCM push notification                        │
│       └─ Tap notification → Navigate to ChatDetailScreen        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 6. Implementation Flow Chi Tiết

---

## 6. Implementation Flow Chi Tiết

### 6.1. Conversations List Screen

```
┌──────────────────────────────────────────────────────────────────┐
│              CONVERSATIONS LIST IMPLEMENTATION                   │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ON SCREEN LOAD (initState / onMount)                           │
│  ─────────────────────────────────────────                       │
│                                                                  │
│  [1] Initialize Firestore Listener                              │
│      └─ Collection: conversations                               │
│         Filter: participants array-contains currentUserId       │
│         OrderBy: lastMessageAt DESC                             │
│         Limit: 50                                               │
│                                                                  │
│  [2] Listen to stream:                                          │
│      ├─ onData (snapshot.docs):                                 │
│      │   ├─ FOR EACH conversation doc:                          │
│      │   │   ├─ Extract data                                    │
│      │   │   ├─ Get other participant ID                        │
│      │   │   ├─ Fetch user profile (avatar, name)              │
│      │   │   │   └─ Use cache if available                      │
│      │   │   └─ Calculate unread count (optional)               │
│      │   │       └─ Query: messages where                       │
│      │   │             senderId != currentUserId                │
│      │   │             status != READ                           │
│      │   │                                                      │
│      │   └─ Update UI state                                     │
│      │                                                          │
│      ├─ onError: show error toast                              │
│      └─ Auto-cleanup listener when screen disposed              │
│                                                                  │
│  ON USER TAP CONVERSATION                                        │
│  ───────────────────────────                                     │
│                                                                  │
│  [1] Get conversationId from tapped item                        │
│  [2] Navigate to ChatDetailScreen                               │
│      └─ Pass params: {conversationId, otherUserId}              │
│                                                                  │
│  REALTIME UPDATES                                               │
│  ────────────────                                               │
│                                                                  │
│  • New message arrives:                                         │
│    └─ Listener emits new snapshot                               │
│       ├─ Conversation moves to top (lastMessageAt updated)      │
│       ├─ Last message text updates                              │
│       └─ Unread count increments (if needed)                    │
│                                                                  │
│  • User sends message from another screen:                      │
│    └─ Same auto-update via listener                             │
│                                                                  │
│  • Message marked as read:                                      │
│    └─ Unread count decrements                                   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**Firestore Query:**

```javascript
Firestore.collection("conversations")
  .where("participants", "array-contains", currentUserId)
  .orderBy("lastMessageAt", "desc")
  .limit(50)
  .onSnapshot((snapshot) => {
    // Handle updates
  });
```

**Unread Count Query (per conversation):**

```javascript
Firestore.collection("conversations")
  .doc(conversationId)
  .collection("messages")
  .where("senderId", "!=", currentUserId)
  .where("status", "==", "SENT")
  .get()
  .then((snapshot) => {
    const unreadCount = snapshot.size;
  });
```

---

### 6.2. Chat Detail Screen

```
┌──────────────────────────────────────────────────────────────────┐
│                CHAT DETAIL IMPLEMENTATION                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ON SCREEN LOAD (initState / onMount)                           │
│  ─────────────────────────────────────────                       │
│                                                                  │
│  [1] Fetch other user profile (if not cached)                   │
│      └─ GET /api/users/{userId} hoặc từ cache                   │
│                                                                  │
│  [2] Initialize Firestore Messages Listener                     │
│      └─ Collection: conversations/{conversationId}/messages     │
│         OrderBy: createdAt DESC                                 │
│         Limit: 50                                               │
│                                                                  │
│  [3] Listen to stream:                                          │
│      ├─ onData (snapshot.docs):                                 │
│      │   ├─ Reverse list (oldest first for UI)                  │
│      │   ├─ Detect new messages:                                │
│      │   │   └─ If senderId != currentUserId:                   │
│      │   │       └─ Auto mark as read (debounced)               │
│      │   │          └─ PUT /messages/{id}/read                  │
│      │   │                                                      │
│      │   ├─ Update UI state                                     │
│      │   └─ Auto-scroll to bottom (if user at bottom)           │
│      │                                                          │
│      └─ Auto-cleanup listener when screen disposed              │
│                                                                  │
│  ON USER SENDS MESSAGE                                           │
│  ────────────────────────                                        │
│                                                                  │
│  [1] Validate input:                                            │
│      ├─ Not empty                                               │
│      ├─ Max 1000 chars                                          │
│      └─ Trim whitespace                                         │
│                                                                  │
│  [2] Optimistic UI Update:                                      │
│      ├─ Generate temp ID (e.g., "temp_${timestamp}")            │
│      ├─ Create local message object:                            │
│      │   {                                                      │
│      │     id: tempId,                                          │
│      │     senderId: currentUserId,                             │
│      │     text: inputText,                                     │
│      │     status: "PENDING",                                   │
│      │     createdAt: now                                       │
│      │   }                                                      │
│      ├─ Add to messages list (local state)                      │
│      ├─ Clear input field                                       │
│      └─ Scroll to bottom                                        │
│                                                                  │
│  [3] Call API:                                                  │
│      └─ POST /chat/messages                                     │
│          {conversationId, text}                                 │
│                                                                  │
│  [4] Handle API Response:                                       │
│      ├─ SUCCESS:                                                │
│      │   └─ Firestore listener auto-replaces PENDING            │
│      │      with real message (real ID, status: SENT)           │
│      │                                                          │
│      └─ ERROR:                                                  │
│          ├─ Remove PENDING message from UI                      │
│          ├─ Show error toast                                    │
│          └─ Restore input field text                            │
│                                                                  │
│  LOAD OLDER MESSAGES (Pagination)                               │
│  ────────────────────────────────                               │
│                                                                  │
│  [1] User scrolls to top & triggers load more                   │
│  [2] Get oldest message's createdAt                             │
│  [3] Query:                                                     │
│      └─ messages                                                │
│         .orderBy('createdAt', 'desc')                           │
│         .startAfter(oldestDoc)                                  │
│         .limit(50)                                              │
│  [4] Prepend to existing messages list                          │
│  [5] Maintain scroll position                                   │
│                                                                  │
│  AUTO MARK AS READ                                              │
│  ──────────────────                                             │
│                                                                  │
│  Strategy: Debounce 500ms after scroll stops                    │
│                                                                  │
│  [1] On new message received (listener):                        │
│      └─ If senderId != currentUserId:                           │
│         └─ Check if message in viewport                         │
│            └─ If YES: mark as read after 500ms delay            │
│                                                                  │
│  [2] Implementation:                                            │
│      ├─ Keep track of unread messages in viewport               │
│      ├─ Use IntersectionObserver / ScrollController             │
│      ├─ Debounce marks (group multiple)                         │
│      └─ Batch mark multiple messages (loop)                     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**Firestore Query:**

```javascript
Firestore.collection("conversations")
  .doc(conversationId)
  .collection("messages")
  .orderBy("createdAt", "desc")
  .limit(50)
  .onSnapshot((snapshot) => {
    snapshot.docChanges().forEach((change) => {
      if (change.type === "added") {
        // New message
      }
      if (change.type === "modified") {
        // Message updated (e.g., status SENT → READ)
      }
    });
  });
```

---

### 6.3. Optimistic UI Pattern

```
┌──────────────────────────────────────────────────────────────────┐
│                    OPTIMISTIC UI FLOW                            │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  TIMELINE                                                        │
│  ────────                                                        │
│                                                                  │
│  t=0ms    User clicks Send                                      │
│            │                                                     │
│            ├─► UI: Add message with status="PENDING"            │
│            │        └─ Show clock icon ⏱                         │
│            │                                                     │
│            └─► API: POST /messages (async)                      │
│                                                                  │
│  t=50ms   Message appears in chat (instantly!)                  │
│            └─ User sees immediate feedback                      │
│                                                                  │
│  t=200ms  API response received                                 │
│            └─ Backend wrote to Firestore                        │
│                                                                  │
│  t=250ms  Firestore listener emits new doc                      │
│            │                                                     │
│            └─► UI: Replace PENDING message                      │
│                 ├─ Update ID (temp → real)                      │
│                 ├─ Update status (PENDING → SENT)               │
│                 └─ Update icon (⏱ → ✓)                          │
│                                                                  │
│  ERROR CASE                                                      │
│  ──────────                                                      │
│                                                                  │
│  t=0ms    User clicks Send                                      │
│            └─► UI: Add PENDING message                          │
│                                                                  │
│  t=5000ms API timeout/error                                     │
│            │                                                     │
│            └─► UI: Remove PENDING message                       │
│                 ├─ Show error toast                             │
│                 ├─ Restore input text                           │
│                 └─ Optional: [Retry] button                     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 7. Error Handling

---

## 7. Error Handling

### 7.1. API Errors

| HTTP Code | Error Message                              | Scenario                       | User-facing Action               |
| --------- | ------------------------------------------ | ------------------------------ | -------------------------------- |
| **400**   | `Cannot create conversation with yourself` | User tries chat với chính mình | Prevent button, not possible     |
| **400**   | `Cannot mark your own message as read`     | Logic error                    | Fix code logic                   |
| **400**   | Text > 1000 chars                          | Validation failed              | Show "Message too long" toast    |
| **401**   | Unauthorized                               | Token expired/invalid          | Redirect to login                |
| **403**   | `Not a participant`                        | User không trong conversation  | Show error, go back              |
| **404**   | Conversation not found                     | Invalid conversationId         | Show error, go back              |
| **500**   | Internal server error                      | Backend issue                  | Show "Try again" toast + [Retry] |

### 7.2. Firestore Listener Errors

```
┌──────────────────────────────────────────────────────────────────┐
│                   LISTENER ERROR HANDLING                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  onError(error) {                                               │
│    ├─ PERMISSION_DENIED                                         │
│    │   └─ User không có quyền read Firestore                    │
│    │      ├─ Check Firestore Rules                              │
│    │      └─ Re-authenticate user                               │
│    │                                                             │
│    ├─ UNAVAILABLE                                               │
│    │   └─ Network offline hoặc Firestore down                   │
│    │      ├─ Show offline banner                                │
│    │      └─ Auto-retry khi network back                        │
│    │                                                             │
│    └─ FAILED_PRECONDITION                                       │
│        └─ Missing Firestore index                               │
│           └─ Deploy index (check Firebase Console logs)         │
│  }                                                              │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**Error UI Patterns:**

```
┌─────────────────────────────────────┐
│  NETWORK OFFLINE                    │
├─────────────────────────────────────┤
│  ⚠️ You're offline                  │
│  Messages will send when reconnected│
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  SEND MESSAGE FAILED                │
├─────────────────────────────────────┤
│  ❌ Failed to send message          │
│  [Retry]    [Dismiss]               │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  PERMISSION ERROR                   │
├─────────────────────────────────────┤
│  🔒 You don't have access           │
│  [Go Back]                          │
└─────────────────────────────────────┘
```

---

## 8. Performance & Best Practices

### 8.1. DO's ✅

```
┌──────────────────────────────────────────────────────────────────┐
│                         BEST PRACTICES                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  🔥 FIRESTORE LISTENERS                                          │
│  ────────────────────                                            │
│                                                                  │
│  ✅ Attach listeners only when screen is active                  │
│     └─ onMount/initState: start listening                       │
│     └─ onUnmount/dispose: stop listening                        │
│                                                                  │
│  ✅ Use .limit() to control number of docs                       │
│     └─ Conversations: limit(50)                                 │
│     └─ Messages: limit(100)                                     │
│                                                                  │
│  ✅ Handle listener errors gracefully                            │
│     └─ onError callback with user-friendly messages             │
│                                                                  │
│  ───────────────────────────────────────────────────────────────  │
│                                                                  │
│  💬 MESSAGE SENDING                                              │
│  ────────────────                                                │
│                                                                  │
│  ✅ Always use optimistic UI                                     │
│     └─ Show message instantly, replace when confirmed           │
│                                                                  │
│  ✅ Validate before sending                                      │
│     ├─ Not empty (after trim)                                   │
│     ├─ Max 1000 chars                                           │
│     └─ Disable send button during API call                      │
│                                                                  │
│  ✅ Handle API errors                                            │
│     ├─ Remove optimistic message on error                       │
│     ├─ Show error toast                                         │
│     └─ Optional: restore input text for retry                   │
│                                                                  │
│  ───────────────────────────────────────────────────────────────  │
│                                                                  │
│  📱 UI/UX                                                        │
│  ──────                                                          │
│                                                                  │
│  ✅ Auto-scroll to bottom when new message (if user at bottom)   │
│     └─ Don't scroll if user is reading old messages             │
│                                                                  │
│  ✅ Cache user profiles (avatar, name)                           │
│     └─ Avoid re-fetching every time                             │
│                                                                  │
│  ✅ Debounce mark-as-read operations                             │
│     └─ Group multiple marks, wait 500ms after scroll stops      │
│                                                                  │
│  ✅ Show typing indicator (optional)                             │
│     └─ Update Firestore field: conversation.{userId}IsTyping    │
│        (debounced, auto-clear after 3s)                         │
│                                                                  │
│  ✅ Relative timestamps                                          │
│     └─ "2 phút trước", "Hôm qua", not ISO strings               │
│                                                                  │
│  ───────────────────────────────────────────────────────────────  │
│                                                                  │
│  🔐 SECURITY                                                     │
│  ─────────                                                       │
│                                                                  │
│  ✅ Never bypass backend API for writes                          │
│     └─ Always POST /messages, don't write Firestore directly    │
│                                                                  │
│  ✅ Validate conversationId format                               │
│     └─ Ensure "{minUid}__{maxUid}" pattern                      │
│                                                                  │
│  ✅ Never store sensitive data in messages                       │
│     └─ Passwords, tokens, payment info = NO                     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### 8.2. DON'Ts ❌

```
┌──────────────────────────────────────────────────────────────────┐
│                         ANTI-PATTERNS                            │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ❌ DON'T poll API for new messages                              │
│     └─ Use Firestore listeners instead                          │
│                                                                  │
│  ❌ DON'T reload entire message list on update                   │
│     └─ Firestore listeners give incremental updates             │
│                                                                  │
│  ❌ DON'T write messages directly to Firestore from client       │
│     └─ Always use REST API (validation, FCM, business logic)    │
│                                                                  │
│  ❌ DON'T fetch all messages at once                             │
│     └─ Use pagination (.limit() + .startAfter())                │
│                                                                  │
│  ❌ DON'T keep listeners active when screen not visible          │
│     └─ Memory leak + unnecessary Firestore reads                │
│                                                                  │
│  ❌ DON'T mark as read immediately on receive                    │
│     └─ Only mark when user actually sees the message            │
│                                                                  │
│  ❌ DON'T send empty or whitespace-only messages                 │
│     └─ Validate .trim() before sending                          │
│                                                                  │
│  ❌ DON'T ignore errors silently                                 │
│     └─ Always show user-friendly error messages                 │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### 8.3. Performance Metrics

| Metric                       | Target  | How to Achieve                   |
| ---------------------------- | ------- | -------------------------------- |
| **Time to first message**    | < 500ms | Firestore listener + index       |
| **New message latency**      | < 100ms | WebSocket connection (Firestore) |
| **Send message feedback**    | Instant | Optimistic UI                    |
| **Scroll performance**       | 60 FPS  | Virtualized list, image caching  |
| **Firestore reads/user/day** | < 100   | Limit queries, cache profiles    |

---

## 9. Testing Checklist

### 9.1. Functional Tests

- [ ] Create conversation → cùng 2 users → same conversationId
- [ ] Send message → appears instantly (optimistic)
- [ ] Send message → appears on recipient (realtime)
- [ ] Mark as read → checkmarks update (both users)
- [ ] Pagination → load older messages
- [ ] Network offline → show offline banner
- [ ] Network back → messages send automatically

### 9.2. Edge Cases

- [ ] Empty message → send button disabled
- [ ] Message > 1000 chars → show error
- [ ] Chat với chính mình → prevented
- [ ] Conversation không tồn tại → error handling
- [ ] User không phải participant → error 403
- [ ] Firestore permission denied → auth error
- [ ] API timeout → show retry button
- [ ] Multiple tabs/devices → state syncs correctly

### 9.3. Performance Tests

- [ ] Scroll 100+ messages → smooth 60 FPS
- [ ] Attach listener → < 500ms first render
- [ ] Send message → optimistic UI < 50ms
- [ ] Receive message → update < 100ms
- [ ] Memory usage stable (no leaks)

---

## 10. Quick Reference

### Firestore Paths

```
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

### API Endpoints (Write Only)

```
POST   /chat/conversations        → Create/get conversation
POST   /chat/messages              → Send message
PUT    /chat/messages/:id/read     → Mark as read
```

### Firestore Queries

```javascript
// List conversations
conversations
  .where("participants", "array-contains", userId)
  .orderBy("lastMessageAt", "desc")
  .limit(50);

// List messages
conversations / { id } / messages.orderBy("createdAt", "desc").limit(100);

// Unread count
messages.where("senderId", "!=", userId).where("status", "==", "SENT");
```

---

## 11. FAQ

**Q: Tại sao không dùng API để get messages?**  
A: API poll → delay 3-5s. Firestore Listener → instant (<100ms).

**Q: Có cần load all conversations một lần?**  
A: Không. Dùng Firestore listener với `.limit(50)`. Pagination nếu cần.

**Q: Làm sao biết user đang online?**  
A: Optional: dùng Firestore Presence với database rules (nâng cao).

**Q: Có thể gửi image/video không?**  
A: Spec có design nhưng chưa implement. Phase sau.

**Q: Xóa tin nhắn được không?**  
A: Chưa có API. Có thể thêm soft-delete field sau.

---

**Questions?** → Contact Backend Team (Hòa)
