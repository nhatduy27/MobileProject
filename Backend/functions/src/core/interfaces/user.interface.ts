/**
 * User Interface
 *
 * Represents authenticated user from Firebase Auth.
 */
export interface IUser {
  /** Firebase UID */
  uid: string;

  /** Email address */
  email?: string;

  /** Display name */
  displayName?: string;

  /** Phone number */
  phone?: string;

  /** User role */
  role: UserRole;

  /** Email verified status */
  emailVerified?: boolean;

  /** Photo URL */
  photoUrl?: string;
}

/**
 * User roles in the system
 */
export enum UserRole {
  CUSTOMER = 'customer',
  SELLER = 'seller',
  SHIPPER = 'shipper',
  ADMIN = 'admin',
}

/**
 * Request with authenticated user
 */
export interface AuthenticatedRequest extends Request {
  user: IUser;
}
