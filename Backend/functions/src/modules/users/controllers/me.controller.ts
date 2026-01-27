import {
  Controller,
  Get,
  Put,
  Post,
  Delete,
  Body,
  Param,
  UseGuards,
  HttpCode,
  HttpStatus,
  UseInterceptors,
  UploadedFile,
  BadRequestException,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiConsumes } from '@nestjs/swagger';
import { FileInterceptor } from '@nestjs/platform-express';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UsersService } from '../users.service';
import {
  UpdateProfileDto,
  CreateAddressDto,
  UpdateAddressDto,
  UserSettingsDto,
  UpdateFcmTokenDto,
} from '../dto';

/**
 * Me Controller
 *
 * Handles current user profile and related endpoints
 * All endpoints require authentication
 *
 * Base URL: /me
 *
 * Tasks: USER-002 to USER-008
 */
@ApiTags('Me (Current User)')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard)
@Controller('me')
export class MeController {
  constructor(private readonly usersService: UsersService) {}

  // ==================== Profile ====================

  /**
   * GET /me
   * Get current user profile with role-specific fields
   *
   * USER-002
   */
  @Get()
  @ApiOperation({
    summary: 'Get my profile',
    description: 'Returns current user profile with addresses and role-specific fields',
  })
  @ApiResponse({
    status: 200,
    description: 'User profile',
    schema: {
      example: {
        success: true,
        data: {
          id: 'uid_xxx',
          email: 'user@example.com',
          displayName: 'Nguyễn Văn A',
          phone: '0901234567',
          avatarUrl: 'https://...',
          role: 'CUSTOMER',
          status: 'ACTIVE',
          createdAt: '2026-01-05T10:00:00Z',
          addresses: [
            {
              id: 'addr_1',
              label: 'Nhà',
              fullAddress: 'Tòa A, Phòng 101',
              isDefault: true,
            },
          ],
        },
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async getProfile(@CurrentUser() user: any) {
    return this.usersService.getProfileWithAddresses(user.uid);
  }

  /**
   * PUT /me
   * Update current user profile
   *
   * USER-003
   */
  @Put()
  @ApiOperation({
    summary: 'Update my profile',
    description: 'Update display name, phone, or avatar URL. Cannot change email, role, or status.',
  })
  @ApiResponse({ status: 200, description: 'Profile updated' })
  @ApiResponse({ status: 400, description: 'Invalid data' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async updateProfile(@CurrentUser() user: any, @Body() dto: UpdateProfileDto) {
    return this.usersService.updateProfile(user.uid, dto);
  }

  /**
   * POST /me/avatar
   * Upload avatar image
   *
   * USER-004
   */
  @Post('avatar')
  @HttpCode(HttpStatus.OK)
  @UseInterceptors(FileInterceptor('avatar'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Upload avatar',
    description: 'Upload avatar image. Accepts JPEG/PNG. Max 5MB.',
  })
  @ApiResponse({ status: 200, description: 'Avatar uploaded successfully' })
  @ApiResponse({ status: 400, description: 'Invalid file' })
  async uploadAvatar(@CurrentUser() user: any, @UploadedFile() file: Express.Multer.File) {
    if (!file) {
      throw new BadRequestException('No file uploaded');
    }

    // Validate file type

    // Validate file size (5MB max)
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      throw new BadRequestException('File size must not exceed 5MB');
    }

    const avatarUrl = await this.usersService.uploadAvatar(user.uid, file.buffer, file.mimetype);

    return { avatarUrl };
  }

  // ==================== Settings ====================

  /**
   * GET /me/settings
   * Get user settings
   *
   * USER-007
   */
  @Get('settings')
  @ApiOperation({ summary: 'Get my settings' })
  @ApiResponse({ status: 200, description: 'User settings' })
  async getSettings(@CurrentUser() user: any) {
    return this.usersService.getSettings(user.uid);
  }

  /**
   * PUT /me/settings
   * Update user settings
   *
   * USER-007
   */
  @Put('settings')
  @ApiOperation({ summary: 'Update my settings' })
  @ApiResponse({ status: 200, description: 'Settings updated' })
  async updateSettings(@CurrentUser() user: any, @Body() dto: UserSettingsDto) {
    return this.usersService.updateSettings(user.uid, dto);
  }

  // ==================== FCM Token ====================

  /**
   * PUT /me/fcm-token
   * Update FCM token for push notifications
   *
   * USER-008
   */
  @Put('fcm-token')
  @ApiOperation({
    summary: 'Update FCM token',
    description: 'Add or update FCM token for push notifications. Max 5 tokens per user.',
  })
  @ApiResponse({ status: 200, description: 'FCM token updated' })
  async updateFcmToken(@CurrentUser() user: any, @Body() dto: UpdateFcmTokenDto) {
    await this.usersService.updateFcmToken(user.uid, dto.fcmToken, dto.deviceId);
    return { message: 'FCM token updated successfully' };
  }

  // ==================== Addresses ====================

  /**
   * GET /me/addresses
   * List all addresses
   *
   * USER-005
   */
  @Get('addresses')
  @ApiOperation({ summary: 'List my addresses' })
  @ApiResponse({ status: 200, description: 'List of addresses' })
  async getAddresses(@CurrentUser() user: any) {
    return this.usersService.getAddresses(user.uid);
  }

  /**
   * POST /me/addresses
   * Add new address
   *
   * USER-005
   */
  @Post('addresses')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Add new address' })
  @ApiResponse({ status: 201, description: 'Address created' })
  @ApiResponse({ status: 400, description: 'Invalid data' })
  async createAddress(@CurrentUser() user: any, @Body() dto: CreateAddressDto) {
    return this.usersService.createAddress(user.uid, dto);
  }

  /**
   * PUT /me/addresses/:id
   * Update address
   *
   * USER-005
   */
  @Put('addresses/:id')
  @ApiOperation({ summary: 'Update address' })
  @ApiResponse({ status: 200, description: 'Address updated' })
  @ApiResponse({ status: 404, description: 'Address not found' })
  async updateAddress(
    @CurrentUser() user: any,
    @Param('id') addressId: string,
    @Body() dto: UpdateAddressDto,
  ) {
    return this.usersService.updateAddress(user.uid, addressId, dto);
  }

  /**
   * DELETE /me/addresses/:id
   * Delete address
   *
   * USER-005
   */
  @Delete('addresses/:id')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Delete address' })
  @ApiResponse({ status: 200, description: 'Address deleted' })
  @ApiResponse({ status: 404, description: 'Address not found' })
  async deleteAddress(@CurrentUser() user: any, @Param('id') addressId: string) {
    await this.usersService.deleteAddress(user.uid, addressId);
    return { message: 'Address deleted successfully' };
  }

  /**
   * PUT /me/addresses/:id/default
   * Set address as default
   *
   * USER-005
   */
  @Put('addresses/:id/default')
  @ApiOperation({ summary: 'Set address as default' })
  @ApiResponse({ status: 200, description: 'Default address set' })
  @ApiResponse({ status: 404, description: 'Address not found' })
  async setDefaultAddress(@CurrentUser() user: any, @Param('id') addressId: string) {
    await this.usersService.setDefaultAddress(user.uid, addressId);
    return { message: 'Default address set successfully' };
  }

  // ==================== Delete Account ====================

  /**
   * DELETE /me
   * Delete user account permanently
   *
   * USER-009
   */
  @Delete()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Delete my account',
    description: 'Permanently delete user account, addresses, and all associated data.',
  })
  @ApiResponse({ status: 200, description: 'Account deleted successfully' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async deleteAccount(@CurrentUser() user: any) {
    await this.usersService.deleteAccount(user.uid);
    return { message: 'Account deleted successfully' };
  }
}
