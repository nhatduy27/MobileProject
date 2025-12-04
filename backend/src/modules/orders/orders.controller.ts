import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { OrdersService } from './orders.service';
import {
  CreateOrderDto,
  UpdateOrderStatusDto,
  OrderResponseDto,
} from './dto/order.dto';

/**
 * Orders Controller (Presentation Layer)
 * 
 * Handles HTTP requests for order operations.
 */
@Controller('orders')
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  /**
   * Create a new order
   * POST /orders
   */
  @Post()
  async createOrder(@Body() dto: CreateOrderDto): Promise<OrderResponseDto> {
    return this.ordersService.createOrder(dto);
  }

  /**
   * Get order by ID
   * GET /orders/:id
   */
  @Get(':id')
  async getOrderById(@Param('id') orderId: string): Promise<OrderResponseDto> {
    return this.ordersService.getOrderById(orderId);
  }

  /**
   * Get all orders for a customer
   * GET /orders/customer/:customerId
   */
  @Get('customer/:customerId')
  async getCustomerOrders(
    @Param('customerId') customerId: string,
  ): Promise<OrderResponseDto[]> {
    return this.ordersService.getCustomerOrders(customerId);
  }

  /**
   * Get all orders for a seller
   * GET /orders/seller/:sellerId
   */
  @Get('seller/:sellerId')
  async getSellerOrders(
    @Param('sellerId') sellerId: string,
  ): Promise<OrderResponseDto[]> {
    return this.ordersService.getSellerOrders(sellerId);
  }

  /**
   * Update order status
   * PATCH /orders/:id/status
   */
  @Patch(':id/status')
  async updateOrderStatus(
    @Param('id') orderId: string,
    @Body() dto: UpdateOrderStatusDto,
  ): Promise<OrderResponseDto> {
    return this.ordersService.updateOrderStatus(orderId, dto);
  }

  /**
   * Cancel an order
   * DELETE /orders/:id
   */
  @Delete(':id')
  @HttpCode(HttpStatus.OK)
  async cancelOrder(@Param('id') orderId: string): Promise<OrderResponseDto> {
    return this.ordersService.cancelOrder(orderId);
  }
}
