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
import { ListBuyersQueryDto, PaginatedBuyerListDto, BuyerDetailDto, BuyerSortBy } from '../dto';

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
    summary: "List buyers for owner's shop",
    description: `
**Chức năng:** Xem danh sách khách hàng của shop

**Query params:**
- \`page\` (int, default=1): Trang hiện tại
- \`limit\` (int, default=20, max=50): Số items/trang
- \`tier\` (enum): ALL | VIP | NORMAL | NEW
- \`search\` (string): Tìm kiếm prefix match theo tên hoặc SĐT
- \`sort\` (enum): createdAt | totalSpent (default: createdAt)

**Business rules:**
- Chỉ xem buyers của shop mình
- Tier tự động tính: NEW (<500k), NORMAL (500k-2M), VIP (>=2M)
- Search: prefix match displayName + phone (case-insensitive)

**Use case:**
- Owner xem danh sách khách hàng
- Filter theo tier (VIP, thường xuyên, mới)
- Tìm kiếm khách theo tên/SĐT
- Sắp xếp theo thời gian hoặc chi tiêu
    `,
  })
  @ApiResponse({
    status: 200,
    description: 'Danh sách buyers',
    type: PaginatedBuyerListDto,
  })
  @ApiResponse({ status: 401, description: 'Unauthorized - Token không hợp lệ' })
  @ApiResponse({ status: 403, description: 'Forbidden - Không phải OWNER role' })
  @ApiResponse({
    status: 400,
    description: 'Bad Request - Invalid query params',
    schema: {
      example: {
        success: false,
        error: {
          code: 'BUYER_002',
          message: 'Invalid tier parameter',
        },
        timestamp: '2026-01-28T10:30:00Z',
      },
    },
  })
  @ApiResponse({ status: 500, description: 'Internal server error' })
  async listBuyers(
    @CurrentUser() user: { uid: string; role: UserRole },
    @Query(
      new ValidationPipe({
        transform: true,
        transformOptions: {
          enableImplicitConversion: true,
        },
      }),
    )
    query: ListBuyersQueryDto,
  ): Promise<PaginatedBuyerListDto> {
    // Ensure defaults are set (ValidationPipe might not apply class defaults)
    const safeQuery: ListBuyersQueryDto = {
      page: query.page ?? 1,
      limit: query.limit ?? 20,
      tier: query.tier ?? 'ALL',
      sort: query.sort ?? BuyerSortBy.CREATED_AT,
      search: query.search,
    };
    return this.buyersService.listBuyers(user.uid, safeQuery);
  }

  /**
   * GET /owner/buyers/:customerId
   * Get buyer detail with recent orders
   *
   * BUYER-005
   */
  @Get(':customerId')
  @HttpCode(HttpStatus.OK)
  @ApiParam({ name: 'customerId', description: 'Customer ID' })
  @ApiOperation({
    summary: 'Get buyer detail',
    description: `
**Chức năng:** Xem chi tiết khách hàng + lịch sử đơn hàng gần đây

**Response includes:**
- Thông tin khách: tên, SĐT, email, ảnh
- Tier hiện tại
- Thống kê: tổng đơn, tổng chi tiêu, đơn TB
- Ngày tham gia, đơn đầu tiên, đơn gần nhất
- Danh sách 5 đơn DELIVERED gần nhất

**Business rules:**
- Chỉ xem buyers của shop mình
- 404 nếu customerId không tồn tại trong shop
- Recent orders: max 5, DELIVERED only, sắp xếp DESC by createdAt

**Use case:**
- Owner xem chi tiết khách hàng
- Kiểm tra lịch sử mua hàng
- Xem tier và stats để quyết định voucher/ưu đãi
    `,
  })
  @ApiResponse({
    status: 200,
    description: 'Chi tiết buyer',
    type: BuyerDetailDto,
  })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  @ApiResponse({ status: 403, description: 'Forbidden - Not OWNER' })
  @ApiResponse({
    status: 404,
    description: 'Customer not found',
    schema: {
      example: {
        success: false,
        error: {
          code: 'BUYER_003',
          message: 'Customer not found in this shop',
        },
        timestamp: '2026-01-28T10:30:00Z',
      },
    },
  })
  @ApiResponse({ status: 500, description: 'Internal server error' })
  async getBuyerDetail(
    @CurrentUser() user: { uid: string; role: UserRole },
    @Param('customerId') customerId: string,
  ): Promise<BuyerDetailDto> {
    return this.buyersService.getBuyerDetail(user.uid, customerId);
  }
}
