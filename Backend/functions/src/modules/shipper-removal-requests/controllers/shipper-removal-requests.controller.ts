import {
  Controller,
  Post,
  Get,
  Body,
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
} from '@nestjs/swagger';
import { ShipperRemovalRequestsService } from '../services/shipper-removal-requests.service';
import { CreateRemovalRequestDto, RemovalRequestFilterDto } from '../dto';
import {
  ShipperRemovalRequestEntity,
  RemovalRequestStatus,
} from '../entities/shipper-removal-request.entity';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Shipper Removal Requests Controller (for Shippers)
 *
 * Endpoints for shippers to manage their removal requests
 */
@ApiTags('Shipper - Removal Requests')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.SHIPPER)
@Controller('shippers/removal-requests')
export class ShipperRemovalRequestsController {
  constructor(
    private readonly removalRequestsService: ShipperRemovalRequestsService,
  ) {}

  /**
   * POST /shippers/removal-requests
   * Create a removal request to leave a shop
   */
  @Post()
  @ApiOperation({ summary: 'Tạo yêu cầu rời khỏi shop' })
  @ApiResponse({
    status: 201,
    description: 'Yêu cầu được tạo thành công',
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
          ownerId: 'uid_owner_789',
          reason: 'Tôi bận việc riêng',
          status: 'PENDING',
          createdAt: '2026-01-28T10:00:00.000Z',
        },
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Shipper không thuộc shop này',
  })
  @ApiResponse({
    status: 409,
    description: 'Shipper có đơn hàng chưa hoàn thành',
  })
  async createRemovalRequest(
    @Req() req: Express.Request & { user: { uid: string } },
    @Body() dto: CreateRemovalRequestDto,
  ): Promise<ShipperRemovalRequestEntity> {
    return this.removalRequestsService.createRemovalRequest(req.user.uid, dto);
  }

  /**
   * GET /shippers/removal-requests
   * List shipper's removal requests
   */
  @Get()
  @ApiOperation({ summary: 'Danh sách yêu cầu rời shop của tôi' })
  @ApiQuery({
    name: 'status',
    enum: RemovalRequestStatus,
    required: false,
    description: 'Filter by status',
  })
  @ApiResponse({
    status: 200,
    description: 'Danh sách yêu cầu',
    type: [ShipperRemovalRequestEntity],
  })
  async listMyRequests(
    @Req() req: Express.Request & { user: { uid: string } },
    @Query() filter: RemovalRequestFilterDto,
  ): Promise<ShipperRemovalRequestEntity[]> {
    return this.removalRequestsService.listMyRequests(
      req.user.uid,
      filter.status as RemovalRequestStatus | undefined,
    );
  }
}
