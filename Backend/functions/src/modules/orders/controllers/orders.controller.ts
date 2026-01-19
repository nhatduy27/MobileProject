import {
  Controller,
  Get,
  Post,
  Put,
  Body,
  Param,
  Req,
  UseGuards,
  Query,
  HttpCode,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiBadRequestResponse,
  ApiNotFoundResponse,
  ApiConflictResponse,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
  ApiBody,
  ApiQuery,
  ApiParam,
} from '@nestjs/swagger';
import { OrdersService } from '../services';
import {
  CreateOrderDto,
  OrderFilterDto,
  CancelOrderDto,
  PaginatedOrdersDto,
  OrderResponseDto,
  PaginatedOrdersResponseDto,
} from '../dto';
import { OrderEntity } from '../entities';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Orders Controller - Customer Endpoints
 *
 * Customer-facing order endpoints
 * All endpoints require CUSTOMER authentication
 *
 * Base URL: /api/orders
 *
 * Tasks: ORDER-002 to ORDER-005, ORDER-010 (shop variant)
 */
@ApiTags('Orders')
@Controller('orders')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.CUSTOMER)
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  /**
   * POST /api/orders
   * Create a new order from cart
   *
   * ORDER-002 (MVP)
   * CRITICAL: Uses Firestore transaction for atomic cart clearing
   */
  @Post()
  @HttpCode(201)
  @ApiOperation({
    summary: 'Create a new order',
    description:
      'Creates an order from cart and atomically clears the cart group for the specified shop',
  })
  @ApiBody({
    type: CreateOrderDto,
    examples: {
      codMinimal: {
        summary: 'Example A: COD Order with KTX address',
        description: 'Minimal COD order with deliveryAddress snapshot',
        value: {
          shopId: 'shop_ktx_001',
          deliveryAddress: {
            label: 'KTX B5',
            fullAddress: 'KTX Khu B - Tòa B5',
            building: 'B5',
            room: '101',
            note: 'Gọi trước 5 phút',
          },
          paymentMethod: 'COD',
        },
      },
      onlineWithVoucher: {
        summary: 'Example B: Online Payment with voucher',
        description: 'ZALOPAY payment with deliveryAddress snapshot and voucher code',
        value: {
          shopId: 'shop_ktx_002',
          deliveryAddress: {
            label: 'Phòng ký túc xá',
            fullAddress: 'KTX Khu A - Tòa A2',
            building: 'A2',
            room: '205',
            note: 'Để ở cổng tòa nhà',
          },
          paymentMethod: 'ZALOPAY',
          voucherCode: 'FREESHIP10',
        },
      },
      oneTimeAddress: {
        summary: 'Example C: One-time delivery address',
        description: 'Order with custom one-time address (e.g., guest user or different location)',
        value: {
          shopId: 'shop_ktx_003',
          deliveryAddress: {
            label: 'Nhà bạn',
            fullAddress: 'Số 123 Lý Thường Kiệt',
            note: 'Gọi trước khi đến',
          },
          paymentMethod: 'COD',
        },
      },
    },
  })
  @ApiCreatedResponse({
    description: 'Order created successfully',
    type: OrderResponseDto,
  })
  @ApiBadRequestResponse({ description: 'Invalid input data' })
  @ApiNotFoundResponse({ description: 'Cart or shop not found' })
  @ApiConflictResponse({ description: 'Shop closed or product unavailable' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async createOrder(
    @Req() req: any,
    @Body() dto: CreateOrderDto,
  ): Promise<OrderEntity> {
    return this.ordersService.createOrder(req.user.uid, dto);
  }

  /**
   * GET /api/orders
   * List customer's orders with page-based pagination
   *
   * ORDER-003 (MVP)
   */
  @Get()
  @ApiOperation({
    summary: 'Get my orders',
    description: 'Retrieve paginated list of orders for authenticated customer',
  })
  @ApiQuery({
    name: 'status',
    required: false,
    enum: ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING', 'DELIVERED', 'CANCELLED'],
    description: 'Filter by order status',
    example: 'PENDING',
  })
  @ApiQuery({
    name: 'page',
    required: false,
    type: Number,
    description: 'Page number (1-indexed, default: 1)',
    example: 1,
  })
  @ApiQuery({
    name: 'limit',
    required: false,
    type: Number,
    description: 'Number of results per page (default: 10)',
    example: 10,
  })
  @ApiOkResponse({
    description: 'Orders retrieved successfully',
    type: PaginatedOrdersResponseDto,
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getMyOrders(
    @Req() req: any,
    @Query('status') status?: string,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ): Promise<PaginatedOrdersDto> {
    const filter: OrderFilterDto = { status, page, limit };
    return this.ordersService.getMyOrders(req.user.uid, filter);
  }

  /**
   * GET /api/orders/:id
   * Get order detail
   *
   * ORDER-004 (MVP)
   */
  @Get(':id')
  @ApiOperation({
    summary: 'Get order detail',
    description: 'Retrieve full details of a specific order',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order details retrieved',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized to view this order' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getOrderDetail(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.getOrderDetail(
      req.user.uid,
      orderId,
    );
  }

  /**
   * PUT /api/orders/:id/cancel
   * Cancel order (customer)
   *
   * ORDER-005 (MVP)
   */
  @Put(':id/cancel')
  @ApiOperation({
    summary: 'Cancel order',
    description:
      'Cancel an order (only if in PENDING, CONFIRMED, or PREPARING status)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_abc123def456',
  })
  @ApiBody({
    type: CancelOrderDto,
    examples: {
      withReason: {
        summary: 'Cancel with reason',
        value: {
          reason: 'Changed my mind',
        },
      },
      withoutReason: {
        summary: 'Cancel without reason',
        value: {},
      },
    },
  })
  @ApiOkResponse({
    description: 'Order cancelled successfully',
    type: OrderResponseDto,
    schema: {
      example: {
        success: true,
        data: {
          id: 'order_abc123def456',
          orderNumber: 'ORD-1705591320000-A2B3C4',
          customerId: 'user_cust_001',
          shopId: 'shop_123',
          shopName: 'Cơm Tấm Sườn',
          shipperId: null,
          items: [
            {
              productId: 'prod_123',
              productName: 'Cơm sườn bì chả',
              quantity: 2,
              price: 25000,
              subtotal: 50000,
            },
          ],
          subtotal: 50000,
          shipFee: 15000,
          discount: 0,
          total: 65000,
          status: 'CANCELLED',
          paymentStatus: 'UNPAID',
          paymentMethod: 'COD',
          deliveryAddress: {
            label: 'KTX B5',
            fullAddress: 'KTX Khu B - Tòa B5',
            building: 'B5',
            room: '101',
            note: 'Gọi trước 5 phút',
          },
          deliveryNote: 'Call before delivery',
          cancelReason: 'Changed my mind',
          cancelledBy: 'CUSTOMER',
          cancelledAt: '2026-01-18T15:12:20.059Z',
          createdAt: '2026-01-18T14:00:00.000Z',
          updatedAt: '2026-01-18T15:12:20.059Z',
          confirmedAt: null,
          preparingAt: null,
          readyAt: null,
          shippingAt: null,
          deliveredAt: null,
          reviewId: null,
          reviewedAt: null,
          paidOut: false,
          paidOutAt: null,
        },
        timestamp: '2026-01-18T15:12:21.000Z',
      },
    },
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized' })
  @ApiConflictResponse({ description: 'Cannot cancel order in current status' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async cancelOrder(
    @Req() req: any,
    @Param('id') orderId: string,
    @Body() dto: CancelOrderDto,
  ): Promise<OrderEntity> {
    return this.ordersService.cancelOrder(
      req.user.uid,
      orderId,
      dto.reason,
    );
  }
}
