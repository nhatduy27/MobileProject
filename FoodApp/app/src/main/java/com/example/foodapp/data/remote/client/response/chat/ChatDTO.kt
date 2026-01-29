package com.example.foodapp.data.remote.client.response.chat

import com.google.gson.annotations.SerializedName

/**
 * Request DTO cho API tạo/lấy conversation
 * Format request:
 * POST /chat/conversations
 * Headers:
 * - Authorization: Bearer {token}
 * - Content-Type: application/json
 * Body:
 * - participantId: ID của người nhận tin nhắn
 */
data class CreateConversationRequest(
    @SerializedName("participantId")
    val participantId: String
)

/**
 * Model cho thông tin người tham gia khác
 * Format:
 * {
 *   "id": "nzIfau9GtqIPyWkmLyku",
 *   "displayName": "Unknown User",
 *   "role": "CUSTOMER"
 * }
 */
data class ParticipantInfo(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("displayName")
    val displayName: String = "",

    @SerializedName("role")
    val role: String = ""
)

/**
 * Response model cho thông tin conversation
 * Format response:
 * {
 *   "id": "userA_userB",
 *   "participants": ["userA", "userB"],
 *   "lastMessage": "",
 *   "lastMessageAt": "2026-01-29T00:00:00Z",
 *   "lastSenderId": "",
 *   "createdAt": "2026-01-29T00:00:00Z",
 *   "updatedAt": "2026-01-29T00:00:00Z",
 *   "otherParticipant": {
 *     "id": "userB",
 *     "displayName": "Unknown User",
 *     "role": "CUSTOMER"
 *   }
 * }
 */
data class ConversationApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("participants")
    val participants: List<String> = emptyList(),

    @SerializedName("lastMessage")
    val lastMessage: String = "",

    @SerializedName("lastMessageAt")
    val lastMessageAt: String = "",

    @SerializedName("lastSenderId")
    val lastSenderId: String = "",

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("otherParticipant")
    val otherParticipant: ParticipantInfo? = null
)

/**
 * Response model wrapper cho API tạo conversation
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "id": "userA_userB",
 *     "participants": ["userA", "userB"],
 *     "lastMessage": "",
 *     "lastMessageAt": "2026-01-29T00:00:00Z",
 *     "lastSenderId": "",
 *     "createdAt": "2026-01-29T00:00:00Z",
 *     "updatedAt": "2026-01-29T00:00:00Z"
 *   }
 * }
 */
data class ConversationResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ConversationApiModel? = null
)


/**
 * Query parameters cho API lấy danh sách conversations
 * Format request:
 * GET /chat/conversations?cursor=abc&limit=20
 * Headers:
 * - Authorization: Bearer {token}
 */
data class ListConversationsQueryRequest(
    @SerializedName("cursor")
    val cursor: String? = null,

    @SerializedName("limit")
    val limit: Int = 20 // Default value
)

/**
 * Response model cho danh sách conversations có phân trang
 * Format response:
 * {
 *   "items": [
 *     {
 *       "id": "user123__user456",
 *       "participants": ["user123", "user456"],
 *       "lastMessage": "Hello!",
 *       "lastMessageAt": "2026-01-28T10:05:00Z",
 *       "lastSenderId": "user456"
 *     }
 *   ],
 *   "hasMore": true,
 *   "nextCursor": "user123__user789"
 * }
 */
data class ListConversationsApiModel(
    @SerializedName("items")
    val items: List<ConversationApiModel> = emptyList(),

    @SerializedName("hasMore")
    val hasMore: Boolean = false,

    @SerializedName("nextCursor")
    val nextCursor: String? = null
)

/**
 * Response wrapper cho API lấy danh sách conversations
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "items": [...],
 *     "hasMore": true,
 *     "nextCursor": "user123__user789"
 *   }
 * }
 */
data class ListConversationsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ListConversationsApiModel? = null
)



/**
 * Query parameters cho API lấy danh sách messages trong conversation
 * Format request:
 * GET /chat/conversations/userA__userB/messages?limit=50&startAfter=msg_abc123
 * Headers:
 * - Authorization: Bearer {token}
 */
data class ListMessagesQueryRequest(
    @SerializedName("limit")
    val limit: Int = 50, // Default value

    @SerializedName("startAfter")
    val startAfter: String? = null
)

/**
 * Model cho từng message trong conversation
 * Format:
 * {
 *   "id": "msg_abc123",
 *   "senderId": "user456",
 *   "text": "Hello!",
 *   "status": "READ",
 *   "readAt": "2026-01-28T10:06:00Z",
 *   "createdAt": "2026-01-28T10:05:00Z"
 * }
 */
data class MessageApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("senderId")
    val senderId: String = "",

    @SerializedName("text")
    val text: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("readAt")
    val readAt: String = "",

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Response model cho danh sách messages có phân trang
 * Format response:
 * {
 *   "items": [
 *     {
 *       "id": "msg_abc123",
 *       "senderId": "user456",
 *       "text": "Hello!",
 *       "status": "READ",
 *       "readAt": "2026-01-28T10:06:00Z",
 *       "createdAt": "2026-01-28T10:05:00Z"
 *     }
 *   ],
 *   "hasMore": true,
 *   "nextCursor": "msg_ghi789"
 * }
 */
data class ListMessagesApiModel(
    @SerializedName("items")
    val items: List<MessageApiModel> = emptyList(),

    @SerializedName("hasMore")
    val hasMore: Boolean = false,

    @SerializedName("nextCursor")
    val nextCursor: String? = null
)

/**
 * Response wrapper cho API lấy danh sách messages
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "items": [...],
 *     "hasMore": true,
 *     "nextCursor": "msg_ghi789"
 *   }
 * }
 */
data class ListMessagesResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ListMessagesApiModel? = null
)


/**
 * Request DTO cho API gửi tin nhắn
 * Format request:
 * POST /chat/messages
 * Headers:
 * - Authorization: Bearer {token}
 * - Content-Type: application/json
 * Body:
 * - conversationId: ID của conversation
 * - text: Nội dung tin nhắn (tối đa 1000 ký tự)
 */
data class SendMessageRequest(
    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("text")
    val text: String
)

/**
 * Response wrapper cho API gửi tin nhắn
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "id": "msg_xyz123",
 *     "senderId": "currentUserId",
 *     "text": "Hello! Max 1000 chars",
 *     "status": "SENT",
 *     "createdAt": "2026-01-29T00:05:00Z"
 *   }
 * }
 */
data class SendMessageResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: MessageApiModel? = null
)