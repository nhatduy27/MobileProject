import { Injectable, Inject, NotFoundException, ForbiddenException } from '@nestjs/common';
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
    const updateData: Partial<UserEntity> = {};
    if (dto.displayName !== undefined) updateData.displayName = dto.displayName;
    if (dto.phone !== undefined) updateData.phone = dto.phone;
    if (dto.avatarUrl !== undefined) updateData.avatarUrl = dto.avatarUrl;

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

    // Delete avatar from storage
    if (user.avatarUrl) {
      await this.storageService.deleteAvatar(user.avatarUrl);
    }

    // Delete all addresses
    const addresses = await this.addressesRepository.findByUserId(userId);
    for (const address of addresses) {
      await this.addressesRepository.delete(address.id);
    }

    // Soft delete user in Firestore
    await this.usersRepository.softDelete(userId);

    // Delete user from Firebase Auth
    try {
      await this.firebaseService.auth.deleteUser(userId);
    } catch (error) {
      // If auth deletion fails, log but don't throw (user data already deleted)
      console.error('Failed to delete user from Firebase Auth:', error);
    }
  }
}
