/**
 * Chat Conversation Entity
 *
 * Represents a 1-1 conversation between two users stored in Firestore.
 * Path: conversations/{conversationId}
 *
 * ID Convention: "{minUid}__{maxUid}" (deterministic, sorted)
 */

export interface ConversationEntity {
  id: string; // Deterministic: "{minUid}__{maxUid}"
  participants: string[]; // [userId1, userId2] sorted
  lastMessage: string; // Preview text or "[Media]"
  lastMessageAt: Date; // For sorting conversations list
  lastSenderId: string; // Who sent last message
  createdAt: Date;
  updatedAt: Date;
}

/**
 * Extended Conversation with participant details
 * Used in list conversations response for better UX
 */
export interface ConversationWithParticipantInfo extends ConversationEntity {
  otherParticipant: {
    id: string;
    displayName: string;
    avatarUrl?: string;
    role: string; // CUSTOMER | OWNER | SHIPPER
    shopName?: string; // For OWNER role - tên shop
    shopId?: string; // For OWNER/SHIPPER role
  };
}

export interface CreateConversationData {
  id: string;
  participants: string[];
}

export interface UpdateLastMessageData {
  lastMessage: string;
  lastSenderId: string;
  lastMessageAt: Date;
}
