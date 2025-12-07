/**
 * Auth Triggers
 *
 * Firebase Authentication event triggers
 */

import * as functionsV1 from "firebase-functions/v1";
import {UserRecord} from "firebase-admin/auth";
import {userRepository} from "../repositories/user.repository";
import {logError} from "../utils/error.utils";

/**
 * On User Created Trigger
 *
 * Được trigger khi user mới đăng ký qua Firebase Authentication
 * Tự động tạo user profile document trong Firestore
 *
 * Note: Sử dụng v1 API vì v2 identity triggers có thể chưa ổn định
 */
export const onUserCreated = functionsV1.auth
  .user()
  .onCreate(async (user: UserRecord) => {
    try {
      console.log(`New user created: ${user.uid} (${user.email})`);

      // TODO: Add additional logic
      //  - Send welcome email
      //  - Add to mailing list
      //  - Grant default permissions
      //  - Create related documents (cart, preferences, etc.)

      // Create user profile in Firestore
      await userRepository.createUserProfileFromAuth(user);

      console.log(`User profile created successfully for: ${user.uid}`);
    } catch (error) {
      // Log error nhưng không throw để không block user creation
      logError("onUserCreated", error);

      // TODO: Implement retry mechanism hoặc queue for failed profile creations
    }
  });

/**
 * TODO: Add more auth triggers
 *
 * - onUserDeleted: Clean up user data khi user bị xóa
 * - beforeUserCreated: Validate/block user creation based on rules
 * - beforeUserSignedIn: Additional authentication checks
 */
