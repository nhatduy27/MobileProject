import {
  Controller,
  Get,
  Param,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
  ValidationPipe,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiParam } from '@nestjs/swagger';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';
import { BuyersService } from '../services/buyers.service';
import { ListBuyersQueryDto, PaginatedBuyerListDto, BuyerDetailDto } from '../dto';

/**
 * Owner Buyers Controller
 *
 * Handles buyer management for owners (READ-ONLY in MVP)
 * All endpoints require OWNER role
 *
 * Base URL: /owner/buyers
 *
 * Tasks: BUYER-004, BUYER-005
 */
@ApiTags('Owner - Buyer Management')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/buyers')
export class BuyersOwnerController {
  constructor(private readonly buyersService: BuyersService) {}

  /**
   * GET /owner/buyers
   * List buyers with filters and pagination
   *
   * BUYER-004
   */
  @Get()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'List buyers',
    description: 'Get paginated list of shop buyers with tier filter and search',
  })
  @ApiResponse({
    status: 200,
    description: 'Buyer list retrieved successfully',
    type: PaginatedBuyerListDto,
    schema: {
      example: {
        success: true,
        data: {
          items: [
            {
              customerId: 'cust_123',
              displayName: 'Nguyễn Văn A',
              phone: '0912345678',
              avatar: 'https://...',
              tier: 'VIP',
              totalOrders: 25,
              totalSpent: 2500000,
              avgOrderValue: 100000,
              joinedDate: '2024-06-15T10:00:00Z',
              lastOrderDate: '2025-01-20T14:30:00Z',
            },
          ],
          pagination: {
            page: 1,
            limit: 20,
            total: 150,
            totalPages: 8,
          },
        },
        timestamp: '2026-01-27T10:30:00Z',
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid query parameters',
    schema: {
      example: {
        success: false,
        message: 'BUYER_002: Invalid tier parameter',
      },
    },
  })
  @ApiResponse({
    status: 401,
    description: 'Unauthorized - No token provided',
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Not OWNER role',
  })
  async listBuyers(
    @CurrentUser('uid') ownerId: string,
    @Query(new ValidationPipe({ transform: true })) query: ListBuyersQueryDto,
  ): Promise<PaginatedBuyerListDto> {
    return this.buyersService.listBuyers(ownerId, query);
  }

  /**
   * GET /owner/buyers/:customerId
   * Get buyer detail with recent orders
   *
   * BUYER-005
   */
  @Get(':customerId')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Get buyer detail',
    description: 'Get detailed buyer information with stats and recent orders (max 5)',
  })
  @ApiParam({
    name: 'customerId',
    description: 'Customer ID',
    example: 'cust_123',
  })
  @ApiResponse({
    status: 200,
    description: 'Buyer detail retrieved successfully',
    type: BuyerDetailDto,
    schema: {
      example: {
        success: true,
        data: {
          customerId: 'cust_123',
          displayName: 'Nguyễn Văn A',
          phone: '0912345678',
          avatar: 'https://...',
          email: 'a@example.com',
          tier: 'VIP',
          totalOrders: 25,
          totalSpent: 2500000,
          avgOrderValue: 100000,
          joinedDate: '2024-06-15T10:00:00Z',
          firstOrderDate: '2024-06-15T10:00:00Z',
          lastOrderDate: '2025-01-20T14:30:00Z',
          recentOrders: [
            {
              orderId: 'order_456',
              orderNumber: 'ORD-0001',
              total: 125000,
              status: 'DELIVERED',
              createdAt: '2025-01-20T14:30:00Z',
            },
          ],
        },
        timestamp: '2026-01-27T10:30:00Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Customer not in owner shop',
    schema: {
      example: {
        success: false,
        message: 'BUYER_010: Bạn không có quyền xem khách hàng này',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Customer not found',
    schema: {
      example: {
        success: false,
        message: 'BUYER_003: Không tìm thấy khách hàng',
      },
    },
  })
  async getBuyerDetail(
    @CurrentUser('uid') ownerId: string,
    @Param('customerId') customerId: string,
  ): Promise<BuyerDetailDto> {
    return this.buyersService.getBuyerDetail(ownerId, customerId);
  }
}
