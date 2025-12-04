import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import { OrderRepository } from './domain/order.repository';
import { Order, OrderItem, OrderStatus } from './domain/order.entity';
import { CreateOrderDto, UpdateOrderStatusDto, OrderResponseDto } from './dto/order.dto';
import { CachePort } from '../../shared/cache/cache.port';
import { NotificationPort } from '../../shared/notifications/notification.port';
import { EventBusPort } from '../../shared/events/event-bus.port';

/**
 * Orders Service (Application Layer)
 * 
 * Contains the business logic for order operations.
 * Demonstrates integration with shared services (cache, notifications, events).
 */
@Injectable()
export class OrdersService {
  private readonly logger = new Logger(OrdersService.name);

  constructor(
    private readonly orderRepository: OrderRepository,
    private readonly cache: CachePort,
    private readonly notification: NotificationPort,
    private readonly eventBus: EventBusPort,
  ) {}

  /**
   * Create a new order
   */
  async createOrder(dto: CreateOrderDto): Promise<OrderResponseDto> {
    this.logger.log(`Creating order for customer: ${dto.customerId}`);

    // Convert DTOs to domain entities
    const items = dto.items.map(
      (item) =>
        new OrderItem({
          productId: item.productId,
          productName: item.productName,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          totalPrice: 0, // Will be calculated in constructor
        }),
    );

    // Create order entity
    const order = await this.orderRepository.create({
      customerId: dto.customerId,
      sellerId: dto.sellerId,
      items,
      status: OrderStatus.PENDING,
      totalAmount: 0, // Will be calculated in constructor
      deliveryAddress: dto.deliveryAddress,
      notes: dto.notes,
    });

    // Invalidate customer's order cache
    await this.cache.del(`orders:customer:${order.customerId}`);

    // Publish order created event
    await this.eventBus.publish('order.created', {
      orderId: order.id,
      customerId: order.customerId,
      sellerId: order.sellerId,
      totalAmount: order.totalAmount,
      timestamp: new Date().toISOString(),
    });

    // Send notification to customer
    await this.notification.sendToUser(order.customerId, {
      title: 'Order Placed Successfully',
      body: `Your order #${order.id} has been placed. Total: $${order.totalAmount}`,
    });

    // Send notification to seller
    await this.notification.sendToUser(order.sellerId, {
      title: 'New Order Received',
      body: `You have a new order #${order.id} for $${order.totalAmount}`,
    });

    return this.mapToResponseDto(order);
  }

  /**
   * Get order by ID
   */
  async getOrderById(orderId: string): Promise<OrderResponseDto> {
    this.logger.log(`Getting order by ID: ${orderId}`);

    // Try to get from cache first
    const cached = await this.cache.get<OrderResponseDto>(`order:${orderId}`);
    if (cached) {
      this.logger.log(`Order ${orderId} found in cache`);
      return cached;
    }

    const order = await this.orderRepository.findById(orderId);
    if (!order) {
      throw new NotFoundException(`Order not found: ${orderId}`);
    }

    const response = this.mapToResponseDto(order);

    // Cache the result for 5 minutes
    await this.cache.set(`order:${orderId}`, response, 300);

    return response;
  }

  /**
   * Get all orders for a customer
   */
  async getCustomerOrders(customerId: string): Promise<OrderResponseDto[]> {
    this.logger.log(`Getting orders for customer: ${customerId}`);

    // Try to get from cache first
    const cacheKey = `orders:customer:${customerId}`;
    const cached = await this.cache.get<OrderResponseDto[]>(cacheKey);
    if (cached) {
      this.logger.log(`Customer orders found in cache`);
      return cached;
    }

    const orders = await this.orderRepository.findByCustomer(customerId);
    const response = orders.map((order) => this.mapToResponseDto(order));

    // Cache the result for 2 minutes
    await this.cache.set(cacheKey, response, 120);

    return response;
  }

  /**
   * Get all orders for a seller
   */
  async getSellerOrders(sellerId: string): Promise<OrderResponseDto[]> {
    this.logger.log(`Getting orders for seller: ${sellerId}`);

    const orders = await this.orderRepository.findBySeller(sellerId);
    return orders.map((order) => this.mapToResponseDto(order));
  }

  /**
   * Update order status
   */
  async updateOrderStatus(
    orderId: string,
    dto: UpdateOrderStatusDto,
  ): Promise<OrderResponseDto> {
    this.logger.log(`Updating order ${orderId} status to: ${dto.status}`);

    const order = await this.orderRepository.findById(orderId);
    if (!order) {
      throw new NotFoundException(`Order not found: ${orderId}`);
    }

    // Update order status
    order.changeStatus(dto.status);
    const updatedOrder = await this.orderRepository.update(orderId, {
      status: order.status,
      updatedAt: order.updatedAt,
    });

    // Invalidate caches
    await this.cache.del(`order:${orderId}`);
    await this.cache.del(`orders:customer:${order.customerId}`);

    // Publish status change event
    await this.eventBus.publish('order.status.changed', {
      orderId: order.id,
      newStatus: dto.status,
      customerId: order.customerId,
      sellerId: order.sellerId,
      timestamp: new Date().toISOString(),
    });

    // Send notification to customer
    await this.notification.sendToUser(order.customerId, {
      title: 'Order Status Updated',
      body: `Your order #${order.id} is now ${dto.status}`,
    });

    return this.mapToResponseDto(updatedOrder);
  }

  /**
   * Cancel an order
   */
  async cancelOrder(orderId: string): Promise<OrderResponseDto> {
    this.logger.log(`Cancelling order: ${orderId}`);

    const order = await this.orderRepository.findById(orderId);
    if (!order) {
      throw new NotFoundException(`Order not found: ${orderId}`);
    }

    // Cancel the order (domain logic)
    order.cancel();

    const updatedOrder = await this.orderRepository.update(orderId, {
      status: order.status,
      updatedAt: order.updatedAt,
    });

    // Invalidate caches
    await this.cache.del(`order:${orderId}`);
    await this.cache.del(`orders:customer:${order.customerId}`);

    // Publish cancellation event
    await this.eventBus.publish('order.cancelled', {
      orderId: order.id,
      customerId: order.customerId,
      sellerId: order.sellerId,
      timestamp: new Date().toISOString(),
    });

    // Send notifications
    await this.notification.sendToUser(order.customerId, {
      title: 'Order Cancelled',
      body: `Your order #${order.id} has been cancelled`,
    });

    await this.notification.sendToUser(order.sellerId, {
      title: 'Order Cancelled',
      body: `Order #${order.id} has been cancelled by the customer`,
    });

    return this.mapToResponseDto(updatedOrder);
  }

  /**
   * Map Order entity to response DTO
   */
  private mapToResponseDto(order: Order): OrderResponseDto {
    return {
      id: order.id,
      customerId: order.customerId,
      sellerId: order.sellerId,
      items: order.items.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        totalPrice: item.totalPrice,
      })),
      status: order.status,
      totalAmount: order.totalAmount,
      deliveryAddress: order.deliveryAddress,
      notes: order.notes,
      createdAt: order.createdAt,
      updatedAt: order.updatedAt,
    };
  }
}
