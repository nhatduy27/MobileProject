/**
 * User Entity - Firestore Document Structure
 *
 * Matches frontend User model exactly.
 * Collection: users/{userId}
 */
export interface UserEntity {
  /** Firebase UID (same as document ID) */
  id: string;

  /** User's full name */
  fullName: string;

  /** Email address */
  email: string;

  /** Email/Phone verification status */
  isVerify: boolean;

  /** Phone number (Vietnamese format) */
  phone: string;

  /** User role: user (customer), seller, delivery (shipper) */
  role: 'user' | 'seller' | 'delivery';

  /** Avatar image URL */
  imageAvatar: string;

  /** Timestamp when user was created */
  createdAt: number;

  /** Timestamp when user was last updated */
  updatedAt: number;
}

/**
 * Create default UserEntity for new registration
 */
export function createDefaultUserEntity(
  id: string,
  email: string,
  fullName: string,
): UserEntity {
  const now = Date.now();
  return {
    id,
    fullName,
    email,
    isVerify: false,
    phone: '',
    role: 'user', // Default role, will be updated in Role Selection
    imageAvatar: '',
    createdAt: now,
    updatedAt: now,
  };
}

/**
 * Create UserEntity for Google Sign-In
 * isVerify = true because Google already verified email
 */
export function createGoogleUserEntity(
  id: string,
  email: string,
  displayName: string,
  photoUrl?: string,
): UserEntity {
  const now = Date.now();
  return {
    id,
    fullName: displayName || 'Google User',
    email: email || '',
    isVerify: true, // Google email is already verified
    phone: '',
    role: 'user',
    imageAvatar: photoUrl || '',
    createdAt: now,
    updatedAt: now,
  };
}
