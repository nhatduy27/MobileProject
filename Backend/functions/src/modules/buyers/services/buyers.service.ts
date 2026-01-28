import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { Firestore } from '@google-cloud/firestore';
import { IBuyersRepository } from '../repositories/buyers-repository.interface';
import { ShopsService } from '../../shops/services/shops.service';
import {
  ListBuyersQueryDto,
  PaginatedBuyerListDto,
  BuyerListItemDto,
  BuyerDetailDto,
  RecentOrderDto,
  BuyerSortBy,
} from '../dto';

@Injectable()
export class BuyersService {
  constructor(
    @Inject('FIRESTORE') private readonly firestore: Firestore,
    @Inject('IBuyersRepository')
    private readonly buyersRepo: IBuyersRepository,
    private readonly shopsService: ShopsService,
  ) {}

  /**
   * List buyers for owner's shop with filters
   */
  async listBuyers(ownerId: string, query: ListBuyersQueryDto): Promise<PaginatedBuyerListDto> {
    // Get owner's shop
    const shop = await this.shopsService.getMyShop(ownerId);
    const shopId = shop.id!;

    console.log('[BuyersService] listBuyers - shopId:', shopId);
    console.log('[BuyersService] listBuyers - query:', query);

    // Build filters with explicit defaults (avoid passing undefined)
    const filters: any = {
      page: query.page ?? 1,
      limit: query.limit ?? 20,
      tier: query.tier ?? 'ALL',
      sort: query.sort ?? BuyerSortBy.CREATED_AT,
    };

    // Only include search if provided
    if (query.search && query.search.trim()) {
      filters.search = query.search.trim();
    }

    console.log('[BuyersService] listBuyers - filters:', filters);

    // Query buyers
    const { buyers, total } = await this.buyersRepo.listByShop(shopId, filters);

    // Map to DTOs
    const items: BuyerListItemDto[] = buyers.map((buyer) => ({
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
    }));

    return {
      items,
      pagination: {
        page: query.page || 1,
        limit: query.limit || 20,
        total,
        totalPages: Math.ceil(total / (query.limit || 20)),
      },
    };
  }

  /**
   * Get buyer detail with recent orders
   */
  async getBuyerDetail(ownerId: string, customerId: string): Promise<BuyerDetailDto> {
    // Get owner's shop
    const shop = await this.shopsService.getMyShop(ownerId);
    const shopId = shop.id!;

    // Get buyer
    const buyer = await this.buyersRepo.findById(shopId, customerId);
    if (!buyer) {
      throw new NotFoundException({
        code: 'BUYER_003',
        message: 'Customer not found in this shop',
        statusCode: 404,
      });
    }

    // Get recent orders (max 5, DELIVERED only)
    const recentOrders = await this.getRecentOrders(shopId, customerId, 5);

    return {
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
    };
  }

  /**
   * Helper: Get recent DELIVERED orders for a customer
   */
  private async getRecentOrders(
    shopId: string,
    customerId: string,
    limit: number,
  ): Promise<RecentOrderDto[]> {
    if (!shopId || !customerId) return [];
    // Query orders directly from Firestore
    const ordersSnapshot = await this.firestore
      .collection('orders')
      .where('shopId', '==', shopId)
      .where('customerId', '==', customerId)
      .where('status', '==', 'DELIVERED')
      .orderBy('createdAt', 'desc')
      .limit(limit)
      .get();

    return ordersSnapshot.docs.map((doc) => {
      const data = doc.data();
      return {
        orderId: doc.id,
        orderNumber: data.orderNumber,
        total: data.total,
        status: data.status,
        createdAt: data.createdAt?.toDate().toISOString() || new Date().toISOString(),
      };
    });
  }
}
