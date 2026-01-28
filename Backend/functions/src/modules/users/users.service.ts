import { Injectable, Inject, NotFoundException, ForbiddenException, InternalServerErrorException } from '@nestjs/common';
import { FieldValue } from 'firebase-admin/firestore';
import { IUsersRepository, USERS_REPOSITORY } from './interfaces/users-repository.interface';
import {
  IAddressesRepository,
  ADDRESSES_REPOSITORY,
} from './interfaces/addresses-repository.interface';
import { UserEntity, UserSettings, AddressEntity } from './entities';
import { UpdateProfileDto, CreateAddressDto, UpdateAddressDto, UserSettingsDto } from './dto';
import { StorageService } from '../../shared/services';
import { FirebaseService } from '../../core/firebase/firebase.service';

const DEFAULT_SETTINGS: UserSettings = {
  notifications: {
    orderUpdates: true,
    promotions: true,
    email: true,
    push: true,
  },
  language: 'vi',
  currency: 'VND',
};

@Injectable()
export class UsersService {
  constructor(
    @Inject(USERS_REPOSITORY)
    private readonly usersRepository: IUsersRepository,
    @Inject(ADDRESSES_REPOSITORY)
    private readonly addressesRepository: IAddressesRepository,
    private readonly storageService: StorageService,
    private readonly firebaseService: FirebaseService,
  ) {}

  // ==================== Profile ====================

  async getProfile(userId: string): Promise<UserEntity> {
    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return user;
  }

  async getProfileWithAddresses(
    userId: string,
  ): Promise<UserEntity & { addresses: AddressEntity[] }> {
    const user = await this.getProfile(userId);
    const addresses = await this.addressesRepository.findByUserId(userId);

    return {
      ...user,
      addresses,
    };
  }

  async updateProfile(userId: string, dto: UpdateProfileDto): Promise<UserEntity> {
    await this.getProfile(userId); // Verify user exists

    // Only update allowed fields
    // NOTE: avatarUrl is managed exclusively through POST /me/avatar and DELETE /me/avatar
    // Ignore any avatarUrl in DTO to prevent users from injecting arbitrary URLs
    const updateData: Partial<UserEntity> = {};
    if (dto.displayName !== undefined) updateData.displayName = dto.displayName;
    if (dto.phone !== undefined) updateData.phone = dto.phone;
    // Intentionally NOT including dto.avatarUrl - use dedicated upload/delete endpoints

    return this.usersRepository.updateProfile(userId, updateData);
  }

  async updateAvatarUrl(userId: string, avatarUrl: string): Promise<void> {
    await this.getProfile(userId); // Verify user exists
    await this.usersRepository.updateAvatarUrl(userId, avatarUrl);
  }

  async uploadAvatar(userId: string, buffer: Buffer, mimetype: string): Promise<string> {
    const user = await this.getProfile(userId);

    // Delete old avatar if exists
    if (user.avatarUrl) {
      await this.storageService.deleteAvatar(user.avatarUrl);
    }

    // Upload new avatar
    const avatarUrl = await this.storageService.uploadAvatar(userId, buffer, mimetype);

    // Update user profile
    await this.updateAvatarUrl(userId, avatarUrl);

    return avatarUrl;
  }

  /**
   * Delete current user's avatar
   * 
   * Idempotent: if user has no avatar, still returns successfully
   * If Firebase Storage deletion fails, still clears DB entry to prevent dead links
   */
  async deleteAvatar(userId: string): Promise<void> {
    const user = await this.getProfile(userId); // Verify user exists

    // Attempt to delete from Firebase Storage if avatar exists
    if (user.avatarUrl) {
      try {
        await this.storageService.deleteAvatar(user.avatarUrl);
      } catch (error) {
        // Log but don't throw: storage file may already be gone
        // We still want to clear the DB entry to prevent dead links
        console.warn(`Failed to delete avatar from storage for user ${userId}:`, error);
      }
    }

    // Clear avatar URL in database
    await this.usersRepository.clearAvatarUrl(userId);
  }

  // ==================== Settings ====================

  async getSettings(userId: string): Promise<UserSettings> {
    const settings = await this.usersRepository.getSettings(userId);
    return settings || DEFAULT_SETTINGS;
  }

  async updateSettings(userId: string, dto: UserSettingsDto): Promise<UserSettings> {
    const currentSettings = await this.getSettings(userId);

    const newSettings: UserSettings = {
      notifications: dto.notifications || currentSettings.notifications,
      language: dto.language || currentSettings.language,
      currency: dto.currency || currentSettings.currency,
    };

    return this.usersRepository.updateSettings(userId, newSettings);
  }

  // ==================== FCM Token ====================

  async updateFcmToken(userId: string, token: string, deviceId: string): Promise<void> {
    await this.getProfile(userId); // Verify user exists
    await this.usersRepository.addFcmToken(userId, token, deviceId);
  }

  async removeFcmToken(userId: string, token: string): Promise<void> {
    await this.usersRepository.removeFcmToken(userId, token);
  }

  // ==================== Addresses ====================

  async getAddresses(userId: string): Promise<AddressEntity[]> {
    return this.addressesRepository.findByUserId(userId);
  }

  async createAddress(userId: string, dto: CreateAddressDto): Promise<AddressEntity> {
    // If this is set as default, handle it
    if (dto.isDefault) {
      const addresses = await this.addressesRepository.findByUserId(userId);
      // If first address, set as default automatically
      if (addresses.length === 0) {
        dto.isDefault = true;
      }
    }

    const address = await this.addressesRepository.create(userId, dto);

    // If set as default, unset others
    if (dto.isDefault && address.id) {
      await this.addressesRepository.setDefault(userId, address.id);
    }

    return address;
  }

  async updateAddress(
    userId: string,
    addressId: string,
    dto: UpdateAddressDto,
  ): Promise<AddressEntity> {
    const address = await this.addressesRepository.findById(addressId);
    if (!address) {
      throw new NotFoundException('Address not found');
    }
    if (address.userId !== userId) {
      throw new ForbiddenException('You can only update your own addresses');
    }

    const updated = await this.addressesRepository.update(addressId, dto);

    // If set as default, unset others
    if (dto.isDefault) {
      await this.addressesRepository.setDefault(userId, addressId);
    }

    return updated;
  }

  async deleteAddress(userId: string, addressId: string): Promise<void> {
    const address = await this.addressesRepository.findById(addressId);
    if (!address) {
      throw new NotFoundException('Address not found');
    }
    if (address.userId !== userId) {
      throw new ForbiddenException('You can only delete your own addresses');
    }

    await this.addressesRepository.delete(addressId);
  }

  async setDefaultAddress(userId: string, addressId: string): Promise<void> {
    const address = await this.addressesRepository.findById(addressId);
    if (!address) {
      throw new NotFoundException('Address not found');
    }
    if (address.userId !== userId) {
      throw new ForbiddenException('You can only set default for your own addresses');
    }

    await this.addressesRepository.setDefault(userId, addressId);
  }

  // ==================== Delete Account ====================

  async deleteAccount(userId: string): Promise<void> {
    const user = await this.getProfile(userId);

    // CRITICAL: Delete from Firebase Auth FIRST (fail-fast) before marking Firestore as DELETED.
    // If Auth deletion fails, we abort and don't mark Firestore DELETED to avoid
    // inconsistent state (Auth has user but Firestore says DELETED).
    // This also prevents re-registration blocks caused by leftover Auth users.
    try {
      await this.firebaseService.auth.deleteUser(userId);
    } catch (error: any) {
      console.error('Failed to delete user from Firebase Auth', {
        userId,
        operation: 'deleteAccount',
        step: 'auth.deleteUser',
        errorCode: error.code,
        errorMessage: error.message,
      });
      throw new InternalServerErrorException('Account deletion incomplete - Auth cleanup failed');
    }

    // Auth deletion succeeded. Now mark as DELETED in Firestore (soft-delete).
    // At this point, user no longer exists in Firebase Auth.
    await this.usersRepository.softDelete(userId);

    // Cleanup related Firestore collections (addresses)
    const addresses = await this.addressesRepository.findByUserId(userId);
    for (const address of addresses) {
      await this.addressesRepository.delete(address.id);
    }

    // Cleanup storage (avatar) - non-blocking, best effort
    if (user.avatarUrl) {
      try {
        await this.storageService.deleteAvatar(user.avatarUrl);
      } catch (error: any) {
        console.warn(`Failed to delete avatar from storage for user ${userId}:`, error);
        // Record orphaned file for later cleanup instead of failing the deletion
        await this.recordOrphanFile(userId, 'AVATAR', user.avatarUrl, error);
        // Do not throw - storage failure should not block account deletion
      }
    }
  }

  /**
   * Record an orphaned file (failed storage deletion) for later cleanup
   * Helps track failed deletions without blocking the primary operation
   */
  private async recordOrphanFile(
    userId: string,
    fileType: string,
    filePath: string,
    error: any,
  ): Promise<void> {
    try {
      await this.firebaseService.firestore.collection('orphanFiles').add({
        userId,
        fileType,
        filePath,
        errorMessage: error?.message || String(error) || 'Unknown error',
        createdAt: FieldValue.serverTimestamp(),
      });
    } catch (recordError) {
      // Even recording failure should not block account deletion
      console.error(
        `Failed to record orphan file for user ${userId}:`,
        recordError,
      );
    }
  }
}
