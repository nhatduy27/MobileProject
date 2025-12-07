/**
 * User Models
 *
 * Định nghĩa types và interfaces cho User domain
 */

export type UserRole = "BUYER" | "SELLER" | "SHIPPER";

export interface User {
  id: string;
  email: string;
  displayName?: string;
  role: UserRole;
  phoneNumber?: string;
  createdAt?: string;
  updatedAt?: string;
  // Có thể thêm: avatarUrl, addresses, preferences, etc.
}

/**
 * User-related request/response types
 */

export interface UserProfile {
  id: string;
  email: string;
  displayName?: string;
  role: UserRole;
  phoneNumber?: string;
}
