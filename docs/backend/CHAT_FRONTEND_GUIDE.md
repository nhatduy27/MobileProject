# 💬 Chat Module - Frontend Integration Guide

> **Module:** chat  
> **Version:** 1.0  
> **Last Updated:** 2026-01-28  
> **Backend Status:** ✅ API Tested & Ready

---

## 📖 Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [API Endpoints](#2-api-endpoints)
3. [Data Models](#3-data-models)
4. [Real-time Flow với Firestore](#4-real-time-flow-với-firestore)
5. [UI Mockup & Flow Đề Xuất](#5-ui-mockup--flow-đề-xuất)
6. [Code Examples (Flutter)](#6-code-examples-flutter)
7. [Error Handling](#7-error-handling)
8. [Best Practices](#8-best-practices)

---

## 1. Tổng Quan

Chat Module cho phép **1-1 text chat** giữa các users trong app (Customer, Owner, Shipper).

### Key Features:

- ✅ Text messaging (max 1000 ký tự)
- ✅ Read receipts (trạng thái đã đọc)
- ✅ Real-time updates via Firestore listeners
- ✅ FCM Push notifications
- ✅ Pagination (cursor-based)

### Conversation ID Convention:

```
{minUid}__{maxUid}
```

Ví dụ: `abcd1234__xyz9876` (sorted alphabetically)

---

## 2. API Endpoints

**Base URL:** `http://localhost:3000/api` (dev) hoặc Cloud Functions URL (prod)

**Authentication:** Tất cả endpoints yêu cầu `Authorization: Bearer {firebaseIdToken}`

### 2.1. Create/Get Conversation

```http
POST /chat/conversations
Content-Type: application/json
Authorization: Bearer {token}

{
  "participantId": "userId_of_recipient"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "user123__user456",
    "participants": ["user123", "user456"],
    "lastMessage": "",
    "lastMessageAt": "2026-01-28T14:00:00Z",
    "lastSenderId": "",
    "createdAt": "2026-01-28T14:00:00Z",
    "updatedAt": "2026-01-28T14:00:00Z"
  }
}
```

**Lưu ý:** Idempotent - trả về conversation có sẵn nếu đã tồn tại.

---

### 2.2. List My Conversations

```http
GET /chat/conversations?limit=20&startAfter={conversationId}
Authorization: Bearer {token}
```

**Query Parameters:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `limit` | int | 20 | Số lượng (1-50) |
| `startAfter` | string | - | Cursor for pagination |

**Response:**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "user123__user456",
        "participants": ["user123", "user456"],
        "lastMessage": "Hello!",
        "lastMessageAt": "2026-01-28T14:05:00Z",
        "lastSenderId": "user456"
      }
    ],
    "hasMore": true,
    "nextCursor": "user123__user789"
  }
}
```

---

### 2.3. Get Single Conversation

```http
GET /chat/conversations/{conversationId}
Authorization: Bearer {token}
```

---

### 2.4. List Messages

```http
GET /chat/conversations/{conversationId}/messages?limit=20&startAfter={messageId}
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "msg_abc123",
        "senderId": "user456",
        "text": "Hello!",
        "status": "READ",
        "readAt": "2026-01-28T14:06:00Z",
        "createdAt": "2026-01-28T14:05:00Z"
      }
    ],
    "hasMore": false,
    "nextCursor": null
  }
}
```

---

### 2.5. Send Message

```http
POST /chat/messages
Content-Type: application/json
Authorization: Bearer {token}

{
  "conversationId": "user123__user456",
  "text": "Hello! How are you?"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "msg_xyz789",
    "senderId": "user123",
    "text": "Hello! How are you?",
    "status": "SENT",
    "createdAt": "2026-01-28T14:07:00Z"
  }
}
```

---

### 2.6. Mark Message as Read

```http
PUT /chat/messages/{messageId}/read
Content-Type: application/json
Authorization: Bearer {token}

{
  "conversationId": "user123__user456"
}
```

**Lưu ý:** Chỉ **recipient** (người nhận) mới có thể mark as read.

---

## 3. Data Models

### Conversation

```typescript
interface Conversation {
  id: string; // "{minUid}__{maxUid}"
  participants: string[]; // [userId1, userId2]
  lastMessage: string; // Preview text (max 100 chars)
  lastMessageAt: Date; // Sorting key
  lastSenderId: string; // Who sent last
  createdAt: Date;
  updatedAt: Date;
}
```

### Message

```typescript
interface Message {
  id: string;
  senderId: string;
  text: string; // Max 1000 chars
  status: "PENDING" | "SENT" | "READ";
  readAt?: Date; // Only when READ
  createdAt: Date;
}
```

---

## 4. Real-time Flow với Firestore

> ⚠️ **QUAN TRỌNG:** Để có real-time updates, frontend PHẢI listen trực tiếp vào Firestore, KHÔNG dùng polling API.

### Firestore Paths:

```
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

### 4.1. Listen Conversations List

```dart
FirebaseFirestore.instance
  .collection('conversations')
  .where('participants', arrayContains: currentUserId)
  .orderBy('lastMessageAt', descending: true)
  .snapshots()
  .listen((snapshot) {
    // Update UI with real-time changes
  });
```

### 4.2. Listen Messages in Conversation

```dart
FirebaseFirestore.instance
  .collection('conversations')
  .doc(conversationId)
  .collection('messages')
  .orderBy('createdAt', descending: true)
  .limit(50)
  .snapshots()
  .listen((snapshot) {
    // Handle new messages, status changes
  });
```

### 4.3. Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     REAL-TIME CHAT FLOW                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐     POST /messages      ┌─────────────┐       │
│  │   User A    │ ──────────────────────► │   Backend   │       │
│  │  (Sender)   │                         │   (NestJS)  │       │
│  └─────────────┘                         └──────┬──────┘       │
│                                                 │               │
│                                    ┌────────────┼────────────┐  │
│                                    │            ▼            │  │
│                                    │   ┌──────────────┐      │  │
│                                    │   │   Firestore  │      │  │
│                                    │   │  (Real-time) │      │  │
│                                    │   └──────┬───────┘      │  │
│                                    │          │              │  │
│              ┌────────────────────────────────┼──────────────┤  │
│              │                                │              │  │
│              ▼                                ▼              │  │
│  ┌─────────────────────┐          ┌─────────────────────┐   │  │
│  │ Firestore Listener  │          │   FCM Notification  │   │  │
│  │   (onSnapshot)      │          └──────────┬──────────┘   │  │
│  └──────────┬──────────┘                     │              │  │
│             │                                │              │  │
│             ▼                                ▼              │  │
│  ┌─────────────────────────────────────────────────────┐    │  │
│  │                    User B (Receiver)                │    │  │
│  │  • In-app: instant update via listener              │    │  │
│  │  • Background: push notification                    │    │  │
│  └─────────────────────────────────────────────────────┘    │  │
│                                                             │  │
└─────────────────────────────────────────────────────────────┘  │
```

---

## 5. UI Mockup & Flow Đề Xuất

### 5.1. Conversations List Screen

```
┌─────────────────────────────────────┐
│  ←  Messages              [Search]  │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ 👤 Nguyễn Văn A                 │ │
│ │    Hello! Đơn hàng của bạn...   │ │
│ │                      2 phút trước│ │
│ │    ● (blue dot = unread)        │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 👤 Shop ABC                     │ │
│ │    Cảm ơn bạn đã đặt hàng!      │ │
│ │                         Hôm qua │ │
│ │    ✓✓ (double check = read)     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 👤 Shipper Minh                 │ │
│ │    Bạn: OK, cảm ơn bạn!         │ │
│ │                        2 ngày   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

**Features:**

- Avatar của người chat cùng
- Last message preview (truncate 50 chars)
- Timestamp (relative: "2 phút", "Hôm qua", etc.)
- Unread indicator (blue dot hoặc counter)
- Double-check cho messages đã đọc

---

### 5.2. Chat Detail Screen

```
┌─────────────────────────────────────┐
│  ←  Nguyễn Văn A            [···]  │
├─────────────────────────────────────┤
│                                     │
│      ┌─────────────────────┐        │
│      │ Xin chào! Tôi muốn  │        │
│      │ hỏi về đơn hàng     │        │
│      │       10:30 AM  ✓✓  │  ←YOU  │
│      └─────────────────────┘        │
│                                     │
│  ┌─────────────────────┐            │
│  │ Dạ, đơn hàng của   │             │
│  │ anh/chị đang được  │             │
│  │ chuẩn bị ạ!        │             │
│  │ 10:32 AM           │  ←THEM      │
│  └─────────────────────┘            │
│                                     │
│      ┌─────────────────────┐        │
│      │ OK cảm ơn bạn!      │        │
│      │       10:33 AM  ✓   │  ←YOU  │
│      └─────────────────────┘        │
│                                     │
├─────────────────────────────────────┤
│ [  Type a message...        ] [📤] │
└─────────────────────────────────────┘
```

**Features:**

- Messages grouped by sender (left/right alignment)
- Timestamp for each message
- Read status: ✓ (sent), ✓✓ (read)
- Auto-scroll to bottom on new message
- Input field với send button

---

### 5.3. User Flow

```
┌────────────────────────────────────────────────────────────────────┐
│                         CHAT USER FLOW                             │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌───────────┐    ┌─────────────────┐    ┌───────────────────┐    │
│  │  Profile  │───►│  Chat Button    │───►│ POST /conversations│   │
│  │  Screen   │    │  "Nhắn tin"     │    │ (get or create)   │    │
│  └───────────┘    └─────────────────┘    └─────────┬─────────┘    │
│                                                    │              │
│                                                    ▼              │
│  ┌───────────┐    ┌─────────────────┐    ┌───────────────────┐    │
│  │  Order    │───►│  Chat with      │───►│  Chat Detail      │    │
│  │  Detail   │    │  Shipper/Owner  │    │  Screen           │    │
│  └───────────┘    └─────────────────┘    └─────────┬─────────┘    │
│                                                    │              │
│                                          ┌────────┴────────┐      │
│                                          ▼                 ▼      │
│                                  ┌─────────────┐   ┌─────────────┐│
│                                  │ Send Message│   │Listen       ││
│                                  │ POST /msg   │   │Firestore    ││
│                                  └─────────────┘   └─────────────┘│
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

---

## 6. Code Examples (Flutter)

### 6.1. ChatService

```dart
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:http/http.dart' as http;

class ChatService {
  final String baseUrl;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  ChatService({required this.baseUrl});

  // Get or create conversation
  Future<Conversation> getOrCreateConversation(
    String token,
    String participantId
  ) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/conversations'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'participantId': participantId}),
    );

    final data = jsonDecode(response.body);
    return Conversation.fromJson(data['data']);
  }

  // Send message
  Future<Message> sendMessage(
    String token,
    String conversationId,
    String text
  ) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/messages'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'conversationId': conversationId,
        'text': text,
      }),
    );

    final data = jsonDecode(response.body);
    return Message.fromJson(data['data']);
  }

  // Mark as read
  Future<void> markAsRead(
    String token,
    String conversationId,
    String messageId
  ) async {
    await http.put(
      Uri.parse('$baseUrl/chat/messages/$messageId/read'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'conversationId': conversationId}),
    );
  }

  // Real-time: Listen to conversations
  Stream<List<Conversation>> listenConversations(String userId) {
    return _firestore
        .collection('conversations')
        .where('participants', arrayContains: userId)
        .orderBy('lastMessageAt', descending: true)
        .snapshots()
        .map((snapshot) => snapshot.docs
            .map((doc) => Conversation.fromFirestore(doc))
            .toList());
  }

  // Real-time: Listen to messages
  Stream<List<Message>> listenMessages(String conversationId) {
    return _firestore
        .collection('conversations')
        .doc(conversationId)
        .collection('messages')
        .orderBy('createdAt', descending: true)
        .limit(100)
        .snapshots()
        .map((snapshot) => snapshot.docs
            .map((doc) => Message.fromFirestore(doc))
            .toList());
  }
}
```

---

### 6.2. Chat Detail Screen (Simplified)

```dart
class ChatDetailScreen extends StatefulWidget {
  final String conversationId;
  final String currentUserId;

  @override
  _ChatDetailScreenState createState() => _ChatDetailScreenState();
}

class _ChatDetailScreenState extends State<ChatDetailScreen> {
  final TextEditingController _controller = TextEditingController();
  final ChatService _chatService = ChatService(baseUrl: 'http://...');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Chat')),
      body: Column(
        children: [
          // Messages list with real-time listener
          Expanded(
            child: StreamBuilder<List<Message>>(
              stream: _chatService.listenMessages(widget.conversationId),
              builder: (context, snapshot) {
                if (!snapshot.hasData) {
                  return Center(child: CircularProgressIndicator());
                }

                final messages = snapshot.data!.reversed.toList();
                return ListView.builder(
                  itemCount: messages.length,
                  itemBuilder: (context, index) {
                    final msg = messages[index];
                    final isMe = msg.senderId == widget.currentUserId;

                    return MessageBubble(
                      message: msg,
                      isMe: isMe,
                    );
                  },
                );
              },
            ),
          ),

          // Input field
          Padding(
            padding: EdgeInsets.all(8),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    decoration: InputDecoration(
                      hintText: 'Type a message...',
                    ),
                  ),
                ),
                IconButton(
                  icon: Icon(Icons.send),
                  onPressed: () => _sendMessage(),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _sendMessage() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;

    _controller.clear();

    await _chatService.sendMessage(
      token,
      widget.conversationId,
      text,
    );
    // UI auto-updates via Firestore listener
  }
}
```

---

## 7. Error Handling

| HTTP Code | Error                                      | Handling                      |
| --------- | ------------------------------------------ | ----------------------------- |
| 400       | `Cannot create conversation with yourself` | Validate trước khi gọi API    |
| 400       | `Cannot mark your own message as read`     | Chỉ mark tin nhắn người khác  |
| 401       | Unauthorized                               | Redirect to login             |
| 403       | `Not a participant`                        | User không trong conversation |
| 404       | Conversation/Message not found             | Show error toast              |

---

## 8. Best Practices

### ✅ DO:

- Dùng Firestore listeners cho real-time updates
- Cache conversations list locally
- Show optimistic UI khi gửi tin nhắn
- Debounce typing indicators (if implemented)
- Mark messages as read khi user scrolls to them

### ❌ DON'T:

- Polling API để check tin nhắn mới
- Reload toàn bộ messages khi có update
- Store sensitive data trong tin nhắn
- Gửi tin nhắn rỗng hoặc quá 1000 ký tự

---

## 9. Test Accounts

| Role     | Email                    | UID                            |
| -------- | ------------------------ | ------------------------------ |
| Customer | testcustomer999@test.com | `ujQm1FYhRpPLtdfKxTMPW2M1Nrl1` |
| Shipper  | testshipper888@test.com  | `9LwPoJGDByPb7Rm93ik8gBLmMfi2` |
| Owner    | testowner777@test.com    | (get via get-id-token.js)      |

**Lấy token để test:**

```bash
cd Backend/functions
node get-id-token.js testcustomer999@test.com
```

---

**Questions?** Contact: Backend team (Hòa)
