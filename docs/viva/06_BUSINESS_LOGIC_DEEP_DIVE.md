# 06. Business Logic Deep Dive

> **Mục đích**: Giải thích chi tiết các tính năng phức tạp: Notifications, GPS, Chat, Chatbot.  
> **Dành cho**: Bảo vệ đồ án – trả lời câu hỏi về logic nghiệp vụ.

---

## 1. NOTIFICATIONS (Thông báo)

### 1.1 Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────┐
│                     NOTIFICATION FLOW                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐ │
│   │  Mobile  │───▶│ Backend  │───▶│   FCM    │───▶│  Mobile  │ │
│   │ Register │    │  Send    │    │  Server  │    │ Receive  │ │
│   │  Token   │    │ Notif    │    │          │    │  Notif   │ │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 FCM Token Lifecycle

**a) Đăng ký token khi đăng nhập:**

```kotlin
// LoginViewModel.kt - lines 190-210
private suspend fun registerFCMToken(userId: String) {
    try {
        val fcmToken = FirebaseMessaging.getInstance().token.await()
        userApiClient.registerDeviceTokenForUser(fcmToken)
        Log.d("FCM", "Token registered successfully")
    } catch (e: Exception) {
        Log.e("FCM", "Failed to register FCM token: ${e.message}")
    }
}
```

**b) Thu hồi token khi logout:**

```kotlin
// SettingViewModel.kt - lines 85-95
suspend fun logout() {
    revokeFCMToken() // Xóa token khỏi server
    authManager.signOut()
}
```

| Sự kiện | Hành động | File |
|---------|-----------|------|
| Login thành công | Gọi `registerDeviceTokenForUser()` | LoginViewModel.kt |
| Sign up thành công | Gọi `registerDeviceTokenForUser()` | SignUpViewModel.kt |
| Logout | Gọi `revokeFCMToken()` | SettingViewModel.kt |

### 1.3 Backend Notification Service

**Quy trình gửi notification:**

```typescript
// notifications.service.ts - lines 40-115
async send(options: SendNotificationOptions): Promise<NotificationEntity> {
  // 1. Kiểm tra user preferences (NOTIF-013)
  if (category && category !== NotificationCategory.TRANSACTIONAL) {
    const preferences = await this.getPreferences(userId);
    if (!this.checkPreferences(preferences, category)) {
      // Vẫn lưu history nhưng đánh dấu FAILED
      return saveNotification({ deliveryStatus: 'FAILED' });
    }
  }

  // 2. Lấy device tokens của user
  const deviceTokens = await this.deviceTokensRepository.findByUserId(userId);
  
  // 3. Gửi qua FCM (best-effort)
  if (tokens.length > 0) {
    await this.fcmService.sendToTokens(tokens, { title, body, data });
  }

  // 4. LUÔN lưu notification history (NOTIF-014)
  return saveNotification({ deliveryStatus: 'SENT' });
}
```

### 1.4 Notification Categories

| Category | Ví dụ | Có thể tắt? |
|----------|-------|-------------|
| `TRANSACTIONAL` | Order confirmed, Payment received | ❌ Không |
| `PROMOTIONAL` | Voucher mới, Flash sale | ✅ Có |
| `SOCIAL` | New message, Follow request | ✅ Có |

### 1.5 Pitfalls & Gotchas

| Vấn đề | Giải pháp |
|--------|-----------|
| Token expire | FCM tự refresh, app cần re-register |
| User chưa cấp quyền | Check permission trước khi register |
| Multiple devices | Backend lưu nhiều tokens per user |
| Notification không nhận | Check: (1) Token valid, (2) Preferences enabled, (3) Network |

---

## 2. GPS / LOCATION TRACKING

### 2.1 Kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    GPS TRACKING FLOW                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│   │LocationHelper│───▶│ GpsViewModel │───▶│   Backend    │     │
│   │(Flow<Location>)    │(State Holder)│    │ updateGps() │     │
│   └──────────────┘    └──────────────┘    └──────────────┘     │
│          ▲                    │                                  │
│          │                    ▼                                  │
│   ┌──────────────┐    ┌──────────────┐                         │
│   │FusedLocation │    │   UI/Map     │                         │
│   │ProviderClient│    │  Display     │                         │
│   └──────────────┘    └──────────────┘                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 LocationHelper Implementation

```kotlin
// LocationHelper.kt - Complete implementation
class LocationHelper(private val context: Context) {
    private val fusedLocationClient = 
        LocationServices.getFusedLocationProviderClient(context)

    // CONFIG: Update mỗi 5 giây, tối thiểu 5 mét
    fun getLocationUpdates(intervalMillis: Long = 5000L): Flow<Location> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis
        )
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        // Yêu cầu ACCESS_FINE_LOCATION hoặc ACCESS_COARSE_LOCATION
        fusedLocationClient.requestLocationUpdates(
            locationRequest, callback, Looper.getMainLooper()
        )

        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }
}
```

### 2.3 GpsViewModel - Trip Lifecycle

```kotlin
// GpsViewModel.kt - Key states and methods
data class GpsUiState(
    val isLoading: Boolean = false,
    val currentTrip: Trip? = null,
    val acceptedOrders: List<Order> = emptyList(),
    val error: String? = null
)

// Trip lifecycle
const val MAX_ORDERS_PER_TRIP = 15

suspend fun createTrip() {
    // 1. Validate: phải có orders đã accept
    if (acceptedOrders.isEmpty()) {
        _uiState.update { it.copy(error = "No orders to create trip") }
        return
    }
    
    // 2. Tạo trip từ accepted orders
    val orderIds = acceptedOrders.map { it.id }
    val trip = gpsApiClient.createTrip(orderIds)
    
    // 3. Update state
    _uiState.update { it.copy(currentTrip = trip) }
}

suspend fun startTrip() {
    // Chuyển trip sang trạng thái DELIVERING
    // Bắt đầu location tracking
    startLocationTracking()
}

suspend fun finishTrip() {
    // Dừng tracking, đánh dấu hoàn thành
    stopLocationTracking()
    gpsApiClient.finishTrip(tripId)
}
```

### 2.4 Location Tracking Flow

```kotlin
// GpsViewModel.kt - Location tracking
private var locationJob: Job? = null

fun startLocationTracking() {
    locationJob = viewModelScope.launch {
        locationHelper.getLocationUpdates()
            .distinctUntilChanged()
            .collect { location ->
                // 1. Update local state
                _currentLocationState.value = location
                
                // 2. Gửi lên server (throttled)
                gpsApiClient.updateLocation(
                    tripId = currentTrip?.id,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
    }
}

fun stopLocationTracking() {
    locationJob?.cancel()
    locationJob = null
}
```

### 2.5 Permissions Required

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

### 2.6 Pitfalls & Gotchas

| Vấn đề | Nguyên nhân | Giải pháp |
|--------|-------------|-----------|
| GPS drain battery | Update quá nhanh | Set interval 5s, distance 5m |
| Tracking dừng khi app ở background | Android kill background service | Dùng Foreground Service |
| Location không chính xác | Indoor, network mode | Dùng PRIORITY_HIGH_ACCURACY |
| Memory leak | Không cancel Flow | awaitClose() trong callbackFlow |

---

## 3. CHAT (Nhắn tin)

### 3.1 Kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                      CHAT ARCHITECTURE                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────────┐                      ┌──────────────┐        │
│   │   Customer   │◀────────────────────▶│    Owner     │        │
│   └──────────────┘                      └──────────────┘        │
│          │                                      │                │
│          ▼                                      ▼                │
│   ┌──────────────────────────────────────────────────┐          │
│   │               ChatViewModel                       │          │
│   │  - messages: LiveData<List<Message>>             │          │
│   │  - sendMessage() → optimistic update             │          │
│   └──────────────────────────────────────────────────┘          │
│          │                                      │                │
│          ▼                                      ▼                │
│   ┌──────────────────────────────────────────────────┐          │
│   │               Backend (NestJS)                    │          │
│   │  - Firestore: conversations, messages            │          │
│   │  - FCM: push notification                        │          │
│   └──────────────────────────────────────────────────┘          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 ChatViewModel - Optimistic UI Pattern

```kotlin
// ChatViewModel.kt - Optimistic update pattern
sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Success(val data: Any) : ChatState()
    data class Error(val message: String) : ChatState()
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false
)

fun sendMessage(text: String) {
    viewModelScope.launch {
        // 1. TẠO TEMP MESSAGE (Optimistic)
        val tempMessage = Message(
            id = "temp_${System.currentTimeMillis()}",
            text = text,
            senderId = currentUserId,
            status = "SENDING",  // UI hiển thị "đang gửi..."
            createdAt = Date()
        )
        
        // 2. THÊM VÀO UI NGAY (user thấy tin nhắn liền)
        _messages.update { current -> current + tempMessage }
        
        try {
            // 3. GỬI LÊN SERVER
            val realMessage = chatApiClient.sendMessage(conversationId, text)
            
            // 4. THAY THẾ TEMP BẰNG REAL MESSAGE
            _messages.update { current ->
                current.map { 
                    if (it.id == tempMessage.id) realMessage 
                    else it 
                }
            }
        } catch (e: Exception) {
            // 5. ĐÁNH DẤU FAILED nếu lỗi
            _messages.update { current ->
                current.map {
                    if (it.id == tempMessage.id) 
                        it.copy(status = "FAILED")
                    else it
                }
            }
        }
    }
}
```

### 3.3 Backend Chat Service

```typescript
// chat.service.ts - Message flow
async sendMessage(userId: string, conversationId: string, text: string) {
  // 1. Validate participant
  if (!this.isParticipant(conversationId, userId)) {
    throw new ForbiddenException('Not a participant');
  }

  // 2. Transaction: Create message + Update conversation
  await this.firestore.runTransaction(async (transaction) => {
    // Create message
    transaction.set(messageRef, {
      senderId: userId,
      text,
      status: MessageStatus.SENT,
      createdAt: now,
    });

    // Update conversation's lastMessage
    transaction.update(conversationRef, {
      lastMessage: text.substring(0, 100),
      lastSenderId: userId,
      lastMessageAt: now,
    });
  });

  // 3. Send FCM notification (async, non-blocking)
  this.sendNotificationToRecipient(recipientId, userId, text, conversationId)
    .catch(err => this.logger.error(`FCM failed: ${err.message}`));

  return message;
}
```

### 3.4 Message Status Flow

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ SENDING │───▶│  SENT   │───▶│DELIVERED│───▶│  READ   │
└─────────┘    └─────────┘    └─────────┘    └─────────┘
      │
      ▼
┌─────────┐
│ FAILED  │ (có thể retry)
└─────────┘
```

### 3.5 Limitations (Chưa implement)

| Feature | Status | Note |
|---------|--------|------|
| Text messages | ✅ Implemented | Full support |
| Image/Media | ❌ Not implemented | Text only |
| Voice message | ❌ Not implemented | |
| Read receipts | ✅ Implemented | markAsRead() |
| Typing indicator | ❌ Not implemented | |
| Message reactions | ❌ Not implemented | |

---

## 4. CHATBOT (AI Assistant)

### 4.1 Kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    CHATBOT ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│   │   Mobile     │───▶│   Backend    │───▶│  Gemini AI   │     │
│   │ ChatBotScreen│    │ChatbotService│    │(gemini-2.5)  │     │
│   └──────────────┘    └──────────────┘    └──────────────┘     │
│                              │                    │              │
│                              ▼                    │              │
│                       ┌──────────────┐           │              │
│                       │  Firestore   │◀──────────┘              │
│                       │  FAQs Cache  │                          │
│                       └──────────────┘                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Backend Implementation

```typescript
// chatbot.service.ts - Key implementation
@Injectable()
export class ChatbotService implements OnModuleInit {
  private genai: GoogleGenAI | null = null;

  async onModuleInit() {
    const apiKey = this.configService.get('GEMINI_API_KEY');
    if (apiKey) {
      this.genai = new GoogleGenAI({ apiKey });
    }
  }

  async chat(userId: string, message: string): Promise<ChatResponse> {
    // 1. Rate limiting check (10 requests/minute/user)
    const waitTime = this.checkRateLimit(userId);
    if (waitTime > 0) {
      return {
        answer: `Vui lòng đợi ${waitTime} giây`,
        rateLimited: true
      };
    }

    // 2. Get FAQ context from Firestore (cached)
    const faqContext = await this.getFaqContext();

    // 3. Build system prompt (Vietnamese support)
    const systemPrompt = this.buildSystemPrompt(faqContext);

    // 4. Generate response với Gemini
    const result = await this.genai.models.generateContent({
      model: 'gemini-2.5-flash-lite',
      contents: [{
        role: 'user',
        parts: [{ text: `${systemPrompt}\n\nKhách hàng hỏi: ${message}` }]
      }]
    });

    return { answer: result.text, confidence: 'high' };
  }
}
```

### 4.3 Rate Limiting Strategy

```typescript
// Rate limit config
const RATE_LIMIT_WINDOW_MS = 60 * 1000;  // 1 phút
const MAX_REQUESTS_PER_WINDOW = 10;       // 10 requests
const MIN_INTERVAL_MS = 6000;             // 6 giây giữa 2 requests

private checkRateLimit(userId: string): number {
  const lastTime = this.userLastRequestTime.get(userId) || 0;
  const timeSince = Date.now() - lastTime;
  
  if (timeSince < MIN_INTERVAL_MS) {
    return Math.ceil((MIN_INTERVAL_MS - timeSince) / 1000);
  }
  return 0;  // OK to proceed
}
```

### 4.4 FAQ Context System

```typescript
// Lấy FAQ từ Firestore để cung cấp context cho AI
private async getFaqContext(): Promise<string> {
  // Check cache first (CACHE_TTL = 5 minutes)
  const cached = globalCache.get<string>(this.FAQ_CACHE_KEY);
  if (cached) return cached;

  // Fetch từ Firestore
  const snapshot = await this.firestore
    .collection('faqs')
    .limit(100)
    .get();

  const faqs = snapshot.docs.map(doc => ({
    question: doc.data().question,
    answer: doc.data().answer,
    category: doc.data().category
  }));

  // Format as context
  const context = faqs
    .map(faq => `Q: ${faq.question}\nA: ${faq.answer}`)
    .join('\n\n');

  globalCache.set(this.FAQ_CACHE_KEY, context, CACHE_TTL);
  return context;
}
```

### 4.5 Mobile Integration

```kotlin
// Screen route
sealed class Screen(val route: String) {
    object ChatBot : Screen("chat_bot")
}

// NavGraph.kt - ChatBot screen navigation
composable(Screen.ChatBot.route) {
    ChatBotScreen(navController = navController)
}
```

### 4.6 Pitfalls & Gotchas

| Vấn đề | Nguyên nhân | Giải pháp |
|--------|-------------|-----------|
| AI trả lời sai | Không có FAQ context | Duy trì FAQ collection đầy đủ |
| Rate limit hit | User spam | Hiển thị "vui lòng đợi X giây" |
| API key invalid | Env không set | Check `GEMINI_API_KEY` |
| Response chậm | Model lớn | Dùng `gemini-2.5-flash-lite` (fast) |

---

## 5. SUMMARY TABLE

| Feature | Mobile Component | Backend Service | Database |
|---------|------------------|-----------------|----------|
| **Notifications** | FirebaseMessaging SDK | notifications.service.ts | device_tokens, notifications |
| **GPS** | LocationHelper + GpsViewModel | gps.service.ts | trips, locations |
| **Chat** | ChatViewModel (optimistic UI) | chat.service.ts | conversations, messages |
| **Chatbot** | ChatBotScreen | chatbot.service.ts | faqs (cache) |

---

## 6. CÂU HỎI THƯỜNG GẶP

**Q: FCM token được lưu ở đâu?**  
A: Collection `device_tokens` trên backend Firestore, được lập chỉ mục theo userId. Mỗi người dùng có thể có nhiều tokens (nhiều thiết bị).

**Q: GPS tracking hoạt động khi ứng dụng ở nền không?**  
A: Cần Foreground Service để theo dõi liên tục. Hiện tại, theo dõi sẽ dừng khi ứng dụng bị dừng.

**Q: Tại sao chat sử dụng optimistic update?**  
A: Để UX mượt mà - người dùng thấy tin nhắn ngay lập tức thay vì chờ phản hồi từ máy chủ. Nếu thất bại sẽ đánh dấu "FAILED".

**Q: Chatbot có học từ người dùng không?**  
A: Không. Mỗi yêu cầu độc lập. Ngữ cảnh chỉ từ collection FAQ, không lưu lịch sử hội thoại.

**Q: Giới hạn tốc độ ở đâu?**  
A: In-memory Map trên backend cho mỗi người dùng. 10 yêu cầu/phút, tối thiểu 6 giây giữa 2 yêu cầu.
