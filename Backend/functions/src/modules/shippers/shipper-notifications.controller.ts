import {
  Controller,
  Post,
  Delete,
  UseGuards,
  HttpCode,
  HttpStatus,
  BadRequestException,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
} from '@nestjs/swagger';
import { AuthGuard } from '../../core/guards/auth.guard';
import { RolesGuard } from '../../core/guards/roles.guard';
import { Roles } from '../../core/decorators/roles.decorator';
import { UserRole } from '../users/entities/user.entity';
import { CurrentUser } from '../../core/decorators/current-user.decorator';
import { NotificationsService } from '../notifications/services/notifications.service';
import { ShippersService } from './shippers.service';

/**
 * Shipper-facing controller for notification topic management
 * Shippers subscribe to per-shop topic (shop_${shopId}_shippers_active) to receive ORDER_READY broadcasts
 */
@ApiTags('Shipper Notifications')
@ApiBearerAuth('firebase-auth')
@Controller('shippers/notifications')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.SHIPPER)
export class ShipperNotificationsController {
  constructor(
    private readonly notificationsService: NotificationsService,
    private readonly shippersService: ShippersService,
  ) {}

  /**
   * Shipper goes online - subscribe to per-shop topic
   * When online, shipper will receive ORDER_READY broadcasts from their shop
   */
  @Post('online')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Shipper goes online',
    description: 'Subscribe shipper to their shop topic to receive ORDER_READY broadcasts',
  })
  @ApiResponse({
    status: 200,
    description: 'Successfully subscribed to topic',
    schema: {
      type: 'object',
      properties: {
        subscribedCount: {
          type: 'number',
          example: 1,
        },
        topic: {
          type: 'string',
          example: 'shop_abc123_shippers_active',
        },
      },
    },
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  @ApiForbiddenResponse({ description: 'User is not a SHIPPER (403 - required role missing)' })
  async goOnline(
    @CurrentUser('uid') shipperId: string,
  ): Promise<{ subscribedCount: number; topic: string }> {
    const shipper = await this.shippersService.findById(shipperId);
    if (!shipper || !shipper.shipperInfo?.shopId) {
      throw new BadRequestException('Shipper profile not found or not assigned to a shop');
    }

    const topic = `shop_${shipper.shipperInfo.shopId}_shippers_active`;
    const result = await this.notificationsService.subscribeToTopic({
      topic,
      userIds: [shipperId],
    });

    return {
      subscribedCount: result.subscribedCount,
      topic,
    };
  }

  /**
   * Shipper goes offline - unsubscribe from per-shop topic
   * When offline, shipper will NOT receive ORDER_READY broadcasts
   */
  @Delete('online')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Shipper goes offline',
    description: 'Unsubscribe shipper from their shop topic',
  })
  @ApiResponse({
    status: 200,
    description: 'Successfully unsubscribed from topic',
    schema: {
      type: 'object',
      properties: {
        unsubscribedCount: {
          type: 'number',
          example: 1,
        },
        topic: {
          type: 'string',
          example: 'shop_abc123_shippers_active',
        },
      },
    },
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  @ApiForbiddenResponse({ description: 'User is not a SHIPPER (403 - required role missing)' })
  async goOffline(
    @CurrentUser('uid') shipperId: string,
  ): Promise<{ unsubscribedCount: number; topic: string }> {
    const shipper = await this.shippersService.findById(shipperId);
    if (!shipper || !shipper.shipperInfo?.shopId) {
      throw new BadRequestException('Shipper profile not found or not assigned to a shop');
    }

    const topic = `shop_${shipper.shipperInfo.shopId}_shippers_active`;
    const result = await this.notificationsService.unsubscribeFromTopic({
      topic,
      userIds: [shipperId],
    });

    return {
      unsubscribedCount: result.unsubscribedCount,
      topic,
    };
  }
}
