import {
  Controller,
  Get,
  Put,
  Param,
  Req,
  UseGuards,
  Query,
  HttpCode,
  Body,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiOkResponse,
  ApiNotFoundResponse,
  ApiConflictResponse,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
  ApiQuery,
  ApiParam,
  ApiBody,
} from '@nestjs/swagger';
import { OrdersService } from '../services';
import {
  OrderFilterDto,
  PaginatedOrdersDto,
  OrderResponseDto,
  PaginatedOrdersResponseDto,
  CancelOrderDto,
  OwnerOrderDetailDto,
} from '../dto';
import { OrderEntity } from '../entities';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Orders Owner Controller
 *
 * Shop owner order management endpoints
 * All endpoints require OWNER/SHOP_OWNER authentication
 *
 * Base URL: /api/orders
 *
 * ⚠️ IMPORTANT: Role-Based Access Control
 * - CUSTOMER or SHIPPER tokens calling these endpoints will receive 403 Forbidden
 * - Error message: "Access denied. Required roles: OWNER"
 * - All endpoints in this controller require OWNER role
 * - See OrdersController for CUSTOMER endpoints, OrdersShipperController for SHIPPER endpoints
 *
 * Tasks: ORDER-006 to ORDER-008, ORDER-010
 */
@ApiTags('Orders - Owner')
@Controller('orders')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
export class OrdersOwnerController {
  constructor(private readonly ordersService: OrdersService) {}

  /**
   * GET /api/orders/shop
   * Get shop's orders with page-based pagination
   *
   * ⚠️ OWNER role required - CUSTOMER will receive 403 Forbidden
   * Endpoint: /api/orders/shop (NOT /api/orders/owner)
   *
   * ORDER-010 (MVP)
   */
  @Get('shop')
  @ApiOperation({
    summary: 'Get shop orders',
    description: 'Retrieve paginated list of orders for owner shop',
  })
  @ApiQuery({
    name: 'status',
    required: false,
    enum: ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING', 'DELIVERED', 'CANCELLED'],
    description: 'Filter by order status',
    example: 'CONFIRMED',
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
    description: 'Number of results per page (default: 20)',
    example: 20,
  })
  @ApiOkResponse({
    description: 'Orders retrieved successfully',
    type: PaginatedOrdersResponseDto,
  })
  @ApiForbiddenResponse({ description: 'User is not an OWNER (403 - required role missing)' })
  @ApiNotFoundResponse({ description: 'Shop not found' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getShopOrders(
    @Req() req: any,
    @Query('status') status?: string,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ): Promise<PaginatedOrdersDto> {
    const filter: OrderFilterDto = { status, page, limit };
    return this.ordersService.getShopOrders(req.user.uid, filter);
  }

  /**
   * GET /api/orders/shop/:id
   * Get full order detail for shop owner
   *
   * NEW: OWNER Order Detail endpoint
   */
  @Get('shop/:id')
  @ApiOperation({
    summary: 'Get shop order detail',
    description: 'Retrieve full order details for an order that belongs to owner\'s shop',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_abc123def456',
  })
  @ApiOkResponse({
    description: 'Order detail retrieved successfully',
    type: OwnerOrderDetailDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found or shop not found' })
  @ApiForbiddenResponse({ description: 'Order does not belong to your shop' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getShopOrderDetail(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OwnerOrderDetailDto> {
    return this.ordersService.getShopOrderDetail(req.user.uid, orderId);
  }

  /**
   * PUT /api/orders/:id/confirm
   * Confirm order (owner)
   *
   * ORDER-006 (MVP)
   */
  @Put(':id/confirm')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Confirm order',
    description:
      'Confirm an order (owner only, must be in PENDING status)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order confirmed successfully',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized' })
  @ApiConflictResponse({ description: 'Cannot confirm - payment or status issue' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async confirmOrder(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.confirmOrder(
      req.user.uid,
      orderId,
    );
  }

  /**
   * PUT /api/orders/:id/preparing
   * Mark order as preparing (owner)
   *
   * ORDER-007 (MVP)
   */
  @Put(':id/preparing')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Mark order as preparing',
    description:
      'Mark order as being prepared (owner only, must be in CONFIRMED status)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order marked as preparing',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized' })
  @ApiConflictResponse({ description: 'Cannot transition to PREPARING status' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async markPreparing(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.markPreparing(
      req.user.uid,
      orderId,
    );
  }

  /**
   * PUT /api/orders/:id/ready
   * Mark order as ready (owner)
   *
   * ORDER-008 (MVP)
   */
  @Put(':id/ready')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Mark order as ready',
    description:
      'Mark order as ready for pickup (owner only, must be in PREPARING status)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order marked as ready',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized' })
  @ApiConflictResponse({ description: 'Cannot transition to READY status' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async markReady(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.markReady(req.user.uid, orderId);
  }

  /**
   * PUT /api/orders/:id/owner-cancel
   * Cancel order (owner)
   *
   * ORDER-009 (MVP)
   */
  @Put(':id/owner-cancel')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Cancel order (owner)',
    description:
      'Cancel an order (owner only, must be in CONFIRMED or PREPARING status)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiBody({
    type: CancelOrderDto,
    examples: {
      withReason: {
        summary: 'Cancel with reason',
        value: {
          reason: 'Out of ingredients',
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
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized to cancel this order' })
  @ApiConflictResponse({
    description: 'Cannot cancel order in current status (must be CONFIRMED or PREPARING)',
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async ownerCancelOrder(
    @Req() req: any,
    @Param('id') orderId: string,
    @Body() dto: CancelOrderDto,
  ): Promise<OrderEntity> {
    return this.ordersService.ownerCancelOrder(
      req.user.uid,
      orderId,
      dto.reason,
    );
  }
}
