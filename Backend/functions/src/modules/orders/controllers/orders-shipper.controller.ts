import {
  Controller,
  Get,
  Put,
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
  ApiOkResponse,
  ApiNotFoundResponse,
  ApiConflictResponse,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
  ApiQuery,
  ApiParam,
  ApiResponse,
} from '@nestjs/swagger';
import { OrdersService } from '../services';
import {
  OrderFilterDto,
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
 * Orders Shipper Controller
 *
 * Shipper order management endpoints
 * All endpoints require SHIPPER authentication
 *
 * Base URL: /api/orders
 *
 * ⚠️ IMPORTANT: Role-Based Access Control
 * - CUSTOMER or OWNER tokens calling these endpoints will receive 403 Forbidden
 * - Error message: "Access denied. Required roles: SHIPPER"
 * - All endpoints in this controller require SHIPPER role
 * - See OrdersController for CUSTOMER endpoints, OrdersOwnerController for OWNER endpoints
 *
 * Tasks: ORDER-013 to ORDER-016 (Phase 2)
 */
@ApiTags('Orders - Shipper')
@Controller('orders')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.SHIPPER)
export class OrdersShipperController {
  constructor(private readonly ordersService: OrdersService) {}

  /**
   * GET /api/orders/shipper
   * Get shipper's assigned orders with page-based pagination
   *
   * ⚠️ SHIPPER role required - CUSTOMER will receive 403 Forbidden
   * Endpoint: /api/orders/shipper (NOT /api/orders/shippers)
   *
   * ORDER-016 (Phase 2)
   */
  @Get('shipper')
  @ApiOperation({
    summary: 'Get shipper orders',
    description: 'Retrieve paginated list of orders assigned to shipper',
  })
  @ApiQuery({
    name: 'status',
    required: false,
    enum: ['READY', 'SHIPPING', 'DELIVERED'],
    description: 'Filter by order status',
    example: 'SHIPPING',
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
  @ApiForbiddenResponse({ description: 'User is not a SHIPPER (403 - required role missing)' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  @ApiResponse({
    status: 412,
    description: 'Firestore index required - Click the provided URL to create the index',
    schema: {
      example: {
        success: false,
        message: 'Query requires a Firestore index. Please create the index at: https://console.firebase.google.com/firestore/indexes',
        errorCode: 'ORDER_INDEX_REQUIRED',
        details: {
          firestoreMessage: 'The query requires an index. You can create it here: ...',
          indexUrl: 'https://console.firebase.google.com/firestore/indexes',
        },
        timestamp: '2026-01-18T10:30:00Z',
      },
    },
  })
  async getShipperOrders(
    @Req() req: any,
    @Query('status') status?: string,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ): Promise<PaginatedOrdersDto> {
    const filter: OrderFilterDto = { status, page, limit };
    return this.ordersService.getShipperOrders(
      req.user.uid,
      filter,
    );
  }

  /**
   * GET /api/orders/shipper/available
   * Get available (unassigned READY) orders for shipper
   *
   * NEW: SHIPPER Available Orders endpoint
   * Returns READY orders with shipperId=null that belong to shipper's shop
   */
  @Get('shipper/available')
  @ApiOperation({
    summary: 'Get available orders for shipper',
    description: 'Retrieve list of unassigned READY orders available for shipper pickup within their shop scope. Returns paginated results with same structure as assigned orders endpoint.',
  })
  @ApiQuery({
    name: 'page',
    required: false,
    type: Number,
    example: 1,
    description: 'Page number (default: 1)',
  })
  @ApiQuery({
    name: 'limit',
    required: false,
    type: Number,
    example: 10,
    description: 'Items per page (default: 10, max: 50)',
  })
  @ApiOkResponse({
    description: 'Available orders retrieved successfully',
    type: PaginatedOrdersResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Shipper not found' })
  @ApiForbiddenResponse({ description: 'User is not a SHIPPER (403 - required role missing)' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  @ApiResponse({
    status: 412,
    description: 'Firestore index required - Click the provided URL to create the index',
    schema: {
      example: {
        success: false,
        message: 'Query requires a Firestore index. Please create the index at: https://console.firebase.google.com/firestore/indexes',
        errorCode: 'ORDER_INDEX_REQUIRED',
        details: {
          firestoreMessage: 'The query requires an index. You can create it here: ...',
          indexUrl: 'https://console.firebase.google.com/firestore/indexes',
        },
        timestamp: '2026-01-18T10:30:00Z',
      },
    },
  })
  async getShipperOrdersAvailable(
    @Req() req: any,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ): Promise<PaginatedOrdersDto> {
    const filter: OrderFilterDto = { page, limit };
    return this.ordersService.getShipperOrdersAvailable(
      req.user.uid,
      filter,
    );
  }

  /**
   * GET /api/orders/shipper/:id
   * Get full order detail for shipper
   *
   * NEW: SHIPPER Order Detail endpoint
   */
  @Get('shipper/:id')
  @ApiOperation({
    summary: 'Get shipper order detail',
    description: 'Retrieve full order details for an order assigned to shipper or available for pickup (READY and unassigned)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_abc123def456',
  })
  @ApiOkResponse({
    description: 'Order detail retrieved successfully',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Order not assigned to you and not available for pickup' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getShipperOrderDetail(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.getShipperOrderDetail(req.user.uid, orderId);
  }

  /**
   * PUT /api/orders/:id/accept
   * Accept order for delivery (shipper)
   *
   * ORDER-013 (Phase 2)
   */
  @Put(':id/accept')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Accept order',
    description:
      'Accept an order for delivery (shipper only, must be in READY status, no shipper assigned)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order accepted successfully',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiConflictResponse({
    description:
      'Invalid state transition or order already assigned to another shipper',
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async acceptOrder(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.acceptOrder(req.user.uid, orderId);
  }

  /**
   * PUT /api/orders/:id/shipping
   * Mark order as shipping (shipper picked up order)
   *
   * ORDER-014 (Phase 2)
   */
  @Put(':id/shipping')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Mark order as shipping',
    description:
      'Mark order as shipping/picked up (shipper only). Note: Currently redundant with accept endpoint (both transition to SHIPPING). Kept for API completeness in case of future state split: READY→ASSIGNED→SHIPPING. Updates shippingAt timestamp if not already set.',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order marked as shipping successfully',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiConflictResponse({ description: 'Invalid state transition' })
  @ApiForbiddenResponse({ description: 'Shipper not assigned to this order' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async markShipping(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.markShipping(req.user.uid, orderId);
  }

  /**
   * PUT /api/orders/:id/delivered
   * Mark order as delivered
   *
   * ORDER-015 (Phase 2)
   */
  @Put(':id/delivered')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Mark order as delivered',
    description:
      'Mark order as delivered (shipper only, must be in SHIPPING status and assigned to this shipper). For COD orders, payment status automatically updates to PAID upon delivery.',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order marked as delivered successfully',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiConflictResponse({ description: 'Invalid state transition' })
  @ApiForbiddenResponse({ description: 'Shipper not assigned to this order' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async markDelivered(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<OrderEntity> {
    return this.ordersService.markDelivered(req.user.uid, orderId);
  }
}
