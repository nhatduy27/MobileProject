import { Injectable, Inject, NotFoundException, ForbiddenException } from '@nestjs/common';
import { IBuyersRepository } from '../repositories/buyers-repository.interface';
import { OrdersService } from '../../orders/services/orders.service';
import { ShopsService } from '../../shops/services/shops.service';
import {
  BuyerListItemDto,
  BuyerDetailDto,
  PaginatedBuyerListDto,
  RecentOrderDto,
  ListBuyersQueryDto,
} from '../dto';
import { BuyerSortBy } from '../dto';
import { OrderStatus } from '../../orders/entities/order.entity';
import { OrderListItemDto } from '../../orders/dto/order-list-item.dto';

@Injectable()
export class BuyersService {
  constructor(
    @Inject('IBuyersRepository')
    private readonly buyersRepository: IBuyersRepository,
    private readonly ordersService: OrdersService,
    private readonly shopsService: ShopsService,
  ) {}

  /**
   * List buyers for owner's shop
   */
  async listBuyers(ownerId: string, queryDto: ListBuyersQueryDto): Promise<PaginatedBuyerListDto> {
    // Get owner's shop
    const shop = await this.shopsService.getMyShop(ownerId);

    // Build filters
    const filters = {
      page: queryDto.page || 1,
      limit: queryDto.limit || 20,
      tier: queryDto.tier || 'ALL',
      search: queryDto.search,
      sort: queryDto.sort || BuyerSortBy.CREATED_AT,
    };

    // Query repository
    const { buyers, total } = await this.buyersRepository.listByShop(shop.id, filters);

    // Map to DTOs
    const items = buyers.map(
      (buyer) =>
        new BuyerListItemDto({
          customerId: buyer.customerId,
          displayName: buyer.displayName,
          phone: buyer.phone,
          avatar: buyer.avatar,
          tier: buyer.tier,
          totalOrders: buyer.totalOrders,
          totalSpent: buyer.totalSpent,
          avgOrderValue: buyer.avgOrderValue,
          joinedDate: buyer.joinedDate?.toISOString(),
          lastOrderDate: buyer.lastOrderDate?.toISOString(),
        }),
    );

    // Pagination metadata
    const totalPages = Math.ceil(total / filters.limit);

    return new PaginatedBuyerListDto({
      items,
      pagination: {
        page: filters.page,
        limit: filters.limit,
        total,
        totalPages,
      },
    });
  }

  /**
   * Get buyer detail with recent orders
   */
  async getBuyerDetail(ownerId: string, customerId: string): Promise<BuyerDetailDto> {
    // Get owner's shop
    const shop = await this.shopsService.getMyShop(ownerId);

    // Find buyer in shop
    const buyer = await this.buyersRepository.findById(shop.id, customerId);
    if (!buyer) {
      throw new NotFoundException('BUYER_003: Không tìm thấy khách hàng');
    }

    // Verify buyer belongs to this shop (additional check)
    if (buyer.shopId !== shop.id) {
      throw new ForbiddenException('BUYER_010: Bạn không có quyền xem khách hàng này');
    }

    // Fetch recent orders (max 5, DELIVERED only, sorted by date DESC)
    const recentOrders = await this.getRecentOrders(shop.id, customerId);

    // Map to DTO
    return new BuyerDetailDto({
      customerId: buyer.customerId,
      displayName: buyer.displayName,
      phone: buyer.phone,
      avatar: buyer.avatar,
      email: buyer.email,
      tier: buyer.tier,
      totalOrders: buyer.totalOrders,
      totalSpent: buyer.totalSpent,
      avgOrderValue: buyer.avgOrderValue,
      joinedDate: buyer.joinedDate?.toISOString(),
      firstOrderDate: buyer.firstOrderDate?.toISOString(),
      lastOrderDate: buyer.lastOrderDate?.toISOString(),
      recentOrders,
    });
  }

  /**
   * Get recent orders for a buyer (max 5)
   * Private helper method
   */
  private async getRecentOrders(shopId: string, customerId: string): Promise<RecentOrderDto[]> {
    try {
      // Get customer's orders for this shop (filter by shopId + customerId)
      const ordersResult = await this.ordersService.getMyOrders(customerId, {
        page: 1,
        limit: 5,
        status: OrderStatus.DELIVERED, // Only DELIVERED orders
      });

      // Filter by shopId (since getMyOrders returns all shops)
      const shopOrders = ordersResult.orders.filter(
        (order: OrderListItemDto) => order.shopId === shopId,
      );

      // Map to RecentOrderDto
      return shopOrders.slice(0, 5).map(
        (order: OrderListItemDto) =>
          new RecentOrderDto({
            orderId: order.id || '',
            orderNumber: order.orderNumber,
            total: order.total,
            status: order.status,
            createdAt:
              typeof order.createdAt === 'string'
                ? order.createdAt
                : order.createdAt?.toDate?.()?.toISOString() || '',
          }),
      );
    } catch (error) {
      // If orders query fails, return empty array (non-blocking)
      return [];
    }
  }
}
