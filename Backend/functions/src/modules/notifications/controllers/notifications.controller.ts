import {
  Controller,
  Post,
  Get,
  Put,
  Delete,
  Body,
  Query,
  Param,
  UseGuards,
  HttpCode,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
  ApiParam,
} from '@nestjs/swagger';
import { NotificationsService } from '../services';
import { RegisterDeviceTokenDto, NotificationQueryDto, UpdatePreferencesDto } from '../dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { PaginatedResponse, MarkAllAsReadResponse } from '../utils';
import { DeviceTokenEntity, NotificationEntity, NotificationPreferencesEntity } from '../entities';

/**
 * Notifications Controller
 * Endpoints for managing device tokens and viewing notifications
 *
 * Base URL: /notifications
 *
 * Tasks: NOTIF-003, NOTIF-004, NOTIF-005, NOTIF-006
 *
 * IMPORTANT: Controllers return RAW payloads only.
 * The global TransformInterceptor wraps all responses with {success, data, timestamp}.
 * DO NOT manually wrap responses - this prevents double-wrapping.
 */
@ApiTags('Notifications')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard)
@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  // ==================== Device Token Management ====================

  /**
   * POST /notifications/tokens
   * Register a device token for push notifications
   *
   * NOTIF-003: Register Device Token
   *
   * Rules:
   * - Upsert by token (update lastUsedAt if exists)
   * - One user may have multiple tokens
   * - Transfer ownership if token belongs to another user
   */
  @Post('tokens')
  @HttpCode(201)
  @ApiOperation({
    summary: 'Register device token',
    description: 'Register or update a device token for push notifications (upsert by token)',
  })
  @ApiResponse({
    status: 201,
    description: 'Device token registered/updated successfully',
    schema: {
      type: 'object',
      properties: {
        id: { type: 'string', example: 'device_1' },
        userId: { type: 'string', example: 'user_1' },
        token: { type: 'string', example: 'cRLstRFyj7k:APA91bHi8L...' },
        platform: { type: 'string', example: 'android', enum: ['android', 'ios'] },
        deviceInfo: {
          type: 'object',
          properties: {
            model: { type: 'string' },
            osVersion: { type: 'string' },
          },
        },
        createdAt: { type: 'string', example: '2024-01-08T10:00:00Z' },
        lastUsedAt: { type: 'string', example: '2024-01-08T10:00:00Z' },
      },
    },
  })
  async registerDeviceToken(
    @CurrentUser() user: any,
    @Body() dto: RegisterDeviceTokenDto,
  ): Promise<DeviceTokenEntity> {
    return await this.notificationsService.registerDeviceToken(user.uid, dto);
  }

  /**
   * DELETE /notifications/tokens/:token
   * Unregister a device token
   *
   * NOTIF-004: Unregister Device Token
   *
   * Rules:
   * - User can only remove their own tokens
   * - Idempotent (no error if token doesn't exist)
   */
  @Delete('tokens/:token')
  @HttpCode(204)
  @ApiOperation({
    summary: 'Unregister device token',
    description: 'Remove a device token (idempotent - no error if not found)',
  })
  @ApiParam({
    name: 'token',
    description: 'FCM device token to unregister',
    example: 'cRLstRFyj7k:APA91bHi8L...',
  })
  @ApiResponse({
    status: 204,
    description: 'Device token unregistered successfully (or did not exist)',
  })
  async unregisterDeviceToken(
    @CurrentUser() user: any,
    @Param('token') token: string,
  ): Promise<void> {
    await this.notificationsService.unregisterDeviceToken(user.uid, token);
  }

  // ==================== Notification History ====================

  /**
   * GET /notifications?read=false&page=1&limit=20
   * Get user's notifications with pagination and optional read status filter
   *
   * NOTIF-005: Get My Notifications
   *
   * Query Parameters:
   * - read (boolean, optional): Filter by read status
   *   * true: Return only read notifications
   *   * false: Return only unread notifications
   *   * omitted: Return all notifications
   * - page (number, optional): Page number (1-based, default 1)
   * - limit (number, optional): Items per page (default 20, max 100)
   *
   * Response includes:
   * - items: array of notifications (filtered by read param if provided)
   * - total: total count of notifications (respecting read filter)
   * - page: current page (1-based)
   * - limit: items per page
   * - unreadCount: count of ALL unread notifications (ignores read/page filter)
   */
  @Get()
  @ApiOperation({
    summary: 'Get my notifications',
    description: 'Retrieve paginated notifications with optional read status filter. Pass read=true for read notifications, read=false for unread, or omit the parameter for all notifications.',
  })
  @ApiQuery({
    name: 'read',
    required: false,
    type: Boolean,
    description: 'Filter by read status: true=read only, false=unread only, omitted=all notifications. Note: Accepts boolean values "true" or "false" from URL query string.',
  })
  @ApiQuery({
    name: 'page',
    required: false,
    type: Number,
    description: 'Page number (1-based, default 1)',
    example: 1,
  })
  @ApiQuery({
    name: 'limit',
    required: false,
    type: Number,
    description: 'Items per page (default 20, max 100)',
    example: 20,
  })
  @ApiResponse({
    status: 200,
    description: 'Notifications retrieved successfully',
    schema: {
      type: 'object',
      properties: {
        items: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              id: { type: 'string', example: 'notif_1' },
              userId: { type: 'string', example: 'user_1' },
              title: { type: 'string', example: 'Đơn hàng đã được xác nhận' },
              body: { type: 'string', example: 'Đơn hàng #ORD-001 đang được chuẩn bị' },
              imageUrl: { type: 'string', example: 'https://...' },
              type: {
                type: 'string',
                example: 'ORDER_CONFIRMED',
              },
              data: { type: 'object', example: { orderId: 'order_1' } },
              read: { type: 'boolean', example: false },
              readAt: { type: 'string', example: '2024-01-08T10:05:00Z' },
              orderId: { type: 'string', example: 'order_1' },
              shopId: { type: 'string', example: 'shop_1' },
              createdAt: { type: 'string', example: '2024-01-08T10:00:00Z' },
            },
          },
        },
        total: {
          type: 'number',
          example: 50,
          description: 'Total notifications (respecting read filter)',
        },
        page: { type: 'number', example: 1 },
        limit: { type: 'number', example: 20 },
        unreadCount: {
          type: 'number',
          example: 15,
          description: 'Total unread notifications (ignores read filter)',
        },
      },
    },
  })
  async getMyNotifications(
    @CurrentUser() user: any,
    @Query() query: NotificationQueryDto,
  ): Promise<PaginatedResponse<NotificationEntity>> {
    if (process.env.DEBUG_NOTIF_QUERY === '1') {
      console.log('[NotificationsController getMyNotifications]');
      console.log('  query.read:', query.read, 'typeof:', typeof query.read);
      console.log('  Full query object:', JSON.stringify(query));
    }
    return await this.notificationsService.getMyNotifications(
      user.uid,
      query.read,
      query.page || 1,
      query.limit || 20,
    );
  }

  /**
   * PUT /notifications/:id/read
   * Mark a single notification as read
   *
   * NOTIF-006: Mark Notification as Read
   *
   * Rules:
   * - User can only update their own notifications
   * - Sets read = true and readAt = now
   */
  @Put(':id/read')
  @ApiOperation({
    summary: 'Mark notification as read',
    description: 'Mark a single notification as read by setting read=true and readAt=now',
  })
  @ApiParam({
    name: 'id',
    description: 'Notification ID',
    example: 'notif_1',
  })
  @ApiResponse({
    status: 200,
    description: 'Notification marked as read',
    schema: {
      type: 'object',
      properties: {
        id: { type: 'string', example: 'notif_1' },
        userId: { type: 'string', example: 'user_1' },
        title: { type: 'string' },
        body: { type: 'string' },
        type: { type: 'string' },
        read: { type: 'boolean', example: true },
        readAt: { type: 'string', example: '2024-01-08T10:05:00Z' },
        createdAt: { type: 'string' },
      },
    },
  })
  async markAsRead(
    @CurrentUser() user: any,
    @Param('id') notificationId: string,
  ): Promise<NotificationEntity> {
    return await this.notificationsService.markAsRead(user.uid, notificationId);
  }

  /**
   * PUT /notifications/read-all
   * Mark all unread notifications as read
   *
   * Optional enhancement (not in core 6 tasks)
   *
   * Response:
   * - updated: number of notifications marked as read
   */
  @Put('read-all')
  @ApiOperation({
    summary: 'Mark all notifications as read',
    description: 'Mark all unread notifications for the user as read',
  })
  @ApiResponse({
    status: 200,
    description: 'All unread notifications marked as read',
    schema: {
      type: 'object',
      properties: {
        updated: {
          type: 'number',
          example: 15,
          description: 'Number of notifications marked as read',
        },
      },
    },
  })
  async markAllAsRead(@CurrentUser() user: any): Promise<MarkAllAsReadResponse> {
    const updated = await this.notificationsService.markAllAsRead(user.uid);
    return { updated };
  }

  // ==================== Notification Preferences (NOTIF-013) ====================

  /**
   * GET /notifications/preferences
   * Get current user's notification preferences
   *
   * NOTIF-013: Notification Preferences
   *
   * Returns defaults if not set:
   * - transactional: true (always enabled)
   * - informational: true
   * - marketing: true
   */
  @Get('preferences')
  @ApiOperation({
    summary: 'Get notification preferences',
    description: 'Get current user notification preferences. Returns defaults if not set.',
  })
  @ApiResponse({
    status: 200,
    description: 'User notification preferences',
    schema: {
      type: 'object',
      properties: {
        userId: { type: 'string', example: 'user_1' },
        transactional: {
          type: 'boolean',
          example: true,
          description: 'Order/payment notifications (always enabled, cannot be disabled)',
        },
        informational: {
          type: 'boolean',
          example: true,
          description: 'Shop updates, daily summaries',
        },
        marketing: {
          type: 'boolean',
          example: false,
          description: 'Promotions, vouchers',
        },
        updatedAt: { type: 'string', example: '2024-01-08T10:00:00Z' },
      },
    },
  })
  async getPreferences(@CurrentUser() user: any): Promise<NotificationPreferencesEntity> {
    return await this.notificationsService.getPreferences(user.uid);
  }

  /**
   * PUT /notifications/preferences
   * Update notification preferences
   *
   * NOTIF-013: Notification Preferences
   *
   * Rules:
   * - transactional is always enabled and cannot be modified
   * - Can only update informational and marketing preferences
   */
  @Put('preferences')
  @ApiOperation({
    summary: 'Update notification preferences',
    description:
      'Update notification preferences. Note: transactional notifications cannot be disabled. Only informational and marketing fields can be modified.',
  })
  @ApiResponse({
    status: 200,
    description: 'Updated notification preferences',
    schema: {
      type: 'object',
      properties: {
        userId: { type: 'string', example: 'user_1' },
        transactional: { type: 'boolean', example: true, description: 'Always true' },
        informational: { type: 'boolean', example: true },
        marketing: { type: 'boolean', example: false },
        updatedAt: { type: 'string', example: '2024-01-08T10:05:00Z' },
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Validation error - transactional field cannot be modified',
    schema: {
      type: 'object',
      properties: {
        success: { type: 'boolean', example: false },
        message: { type: 'string', example: 'property transactional should not exist' },
        errorCode: { type: 'string', example: 'VALIDATION_ERROR' },
        timestamp: { type: 'string', example: '2026-01-25T04:58:27.127Z' },
      },
    },
  })
  async updatePreferences(
    @CurrentUser() user: any,
    @Body() dto: UpdatePreferencesDto,
  ): Promise<NotificationPreferencesEntity> {
    return await this.notificationsService.updatePreferences(user.uid, dto);
  }
}
