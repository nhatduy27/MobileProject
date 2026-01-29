import {
  Controller,
  Get,
  Put,
  Body,
  Param,
  Query,
  UseGuards,
  Req,
} from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiQuery,
  ApiParam,
} from '@nestjs/swagger';
import { ShipperRemovalRequestsService } from '../services/shipper-removal-requests.service';
import { ProcessRemovalRequestDto, RemovalRequestFilterDto } from '../dto';
import {
  ShipperRemovalRequestEntity,
  RemovalRequestStatus,
} from '../entities/shipper-removal-request.entity';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Owner Removal Requests Controller
 *
 * Endpoints for shop owners to manage shipper removal requests
 */
@ApiTags('Owner - Shipper Removal Requests')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner')
export class OwnerRemovalRequestsController {
  constructor(
    private readonly removalRequestsService: ShipperRemovalRequestsService,
  ) {}

  /**
   * GET /owner/shops/:shopId/removal-requests
   * List removal requests for a shop
   */
  @Get('shops/:shopId/removal-requests')
  @ApiOperation({ summary: 'Danh sách yêu cầu rời shop từ shipper' })
  @ApiParam({
    name: 'shopId',
    description: 'Shop ID',
  })
  @ApiQuery({
    name: 'status',
    enum: RemovalRequestStatus,
    required: false,
    description: 'Filter by status (PENDING, APPROVED, REJECTED)',
  })
  @ApiResponse({
    status: 200,
    description: 'Danh sách yêu cầu',
    type: [ShipperRemovalRequestEntity],
    schema: {
      example: {
        success: true,
        data: [
          {
            id: 'srr_abc123',
            shipperId: 'uid_shipper_123',
            shipperName: 'Nguyễn Văn Shipper',
            shipperPhone: '0901234567',
            shopId: 'shop_abc456',
            shopName: 'Quán A Mập',
            ownerId: 'uid_owner_789',
            reason: 'Tôi bận việc riêng',
            status: 'PENDING',
            createdAt: '2026-01-28T10:00:00.000Z',
          },
        ],
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Bạn không phải chủ shop này',
  })
  async listShopRequests(
    @Req() req: Express.Request & { user: { uid: string } },
    @Param('shopId') shopId: string,
    @Query() filter: RemovalRequestFilterDto,
  ): Promise<ShipperRemovalRequestEntity[]> {
    return this.removalRequestsService.listShopRequests(
      req.user.uid,
      shopId,
      filter.status as RemovalRequestStatus | undefined,
    );
  }

  /**
   * PUT /owner/removal-requests/:requestId
   * Process (approve/reject) a removal request
   */
  @Put('removal-requests/:requestId')
  @ApiOperation({ summary: 'Xử lý yêu cầu rời shop (approve/reject)' })
  @ApiParam({
    name: 'requestId',
    description: 'Removal Request ID',
  })
  @ApiResponse({
    status: 200,
    description: 'Yêu cầu đã được xử lý',
    type: ShipperRemovalRequestEntity,
    schema: {
      example: {
        success: true,
        data: {
          id: 'srr_abc123',
          shipperId: 'uid_shipper_123',
          shipperName: 'Nguyễn Văn Shipper',
          shopId: 'shop_abc456',
          shopName: 'Quán A Mập',
          status: 'APPROVED',
          processedBy: 'uid_owner_789',
          processedAt: '2026-01-28T12:00:00.000Z',
          createdAt: '2026-01-28T10:00:00.000Z',
        },
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Bạn không có quyền xử lý yêu cầu này',
  })
  @ApiResponse({
    status: 404,
    description: 'Không tìm thấy yêu cầu',
  })
  @ApiResponse({
    status: 409,
    description: 'Yêu cầu đã được xử lý',
  })
  async processRequest(
    @Req() req: Express.Request & { user: { uid: string } },
    @Param('requestId') requestId: string,
    @Body() dto: ProcessRemovalRequestDto,
  ): Promise<ShipperRemovalRequestEntity> {
    return this.removalRequestsService.processRequest(
      req.user.uid,
      requestId,
      dto,
    );
  }
}
