/**
 * User Repository
 *
 * Data access layer cho Users collection
 */

import {getFirestore} from "firebase-admin/firestore";
import {User, UserRole} from "../models/user.model";
import {UserRecord} from "firebase-admin/auth";

/**
 * Firestore data access cho Users collection.
 */
export class UserRepository {
  private db = getFirestore();
  private collection = "users";

  /**
   * Tạo user profile từ Firebase Auth user record
   * Được gọi từ auth trigger khi user mới đăng ký
   * @param {UserRecord} user Firebase Auth user record
   */
  async createUserProfileFromAuth(user: UserRecord): Promise<void> {
    // TODO: Determine default role (BUYER by default?)
    // TODO: Extract additional info from user metadata/claims

    const userProfile: Partial<User> = {
      id: user.uid,
      email: user.email || "",
      displayName: user.displayName || undefined,
      role: "BUYER" as UserRole, // Default role
      phoneNumber: user.phoneNumber || undefined,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    await this.db
      .collection(this.collection)
      .doc(user.uid)
      .set(userProfile);

    // TODO: Send welcome email/notification
    console.log(`User profile created for: ${user.uid}`);
  }

  /**
   * Lấy user theo ID
   * @param {string} userId User ID
   */
  async getUserById(userId: string): Promise<User | null> {
    const doc = await this.db.collection(this.collection).doc(userId).get();

    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as User;
  }

  /**
   * Update user profile
   * @param {string} userId User ID
   * @param {Partial<User>} data Data cần cập nhật
   */
  async updateUser(userId: string, data: Partial<User>): Promise<void> {
    await this.db
      .collection(this.collection)
      .doc(userId)
      .update({
        ...data,
        updatedAt: new Date().toISOString(),
      });
  }

  /**
   * Check if user has specific role
   * @param {string} userId User ID
   * @param {UserRole} role Role cần kiểm tra
   */
  async hasRole(userId: string, role: UserRole): Promise<boolean> {
    const user = await this.getUserById(userId);
    return user?.role === role;
  }
}

// Singleton instance
export const userRepository = new UserRepository();
