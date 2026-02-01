import { Injectable, Inject, BadRequestException } from '@nestjs/common';
import { Firestore } from 'firebase-admin/firestore';
import { ShopAnalyticsEntity } from '../entities/shop-analytics.entity';

@Injectable()
export class AnalyticsService {
  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  /**
   * Get shop analytics for owner dashboard
   *
   * BEHAVIOR:
   * - When from/to are provided: analytics are limited to [from, to] range
   * - When from/to are NOT provided: analytics are calculated for all time
   *
   * TIMESTAMP RULES:
   * - Revenue/orderCount use deliveredAt (when order was delivered)
   * - Fallback: deliveredAt → updatedAt → createdAt if missing
   * - Only DELIVERED orders count toward revenue/orderCount
   * - ordersByStatus: counts all orders by createdAt (any status) within range if provided
   * - recentOrders: sorted by createdAt desc (shows order activity)
   */
  async getShopAnalytics(shopId: string, from?: string, to?: string): Promise<ShopAnalyticsEntity> {
    // 1. Parse and validate date parameters
    let fromDate: Date | null = null;
    let toDate: Date | null = null;

    if (from && to) {
      fromDate = new Date(from);
      toDate = new Date(to);

      // Validate dates
      if (isNaN(fromDate.getTime()) || isNaN(toDate.getTime())) {
        throw new BadRequestException('Invalid date format. Use YYYY-MM-DD');
      }

      // Set to end of day
      toDate.setHours(23, 59, 59, 999);

      if (toDate < fromDate) {
        throw new BadRequestException('to date must be after from date');
      }

      // Optional: max range validation (1 year)
      const maxRangeMs = 365 * 24 * 60 * 60 * 1000;
      if (toDate.getTime() - fromDate.getTime() > maxRangeMs) {
        throw new BadRequestException('Date range cannot exceed 1 year');
      }
    } else if (from || to) {
      throw new BadRequestException(
        'Both from and to parameters are required when filtering by date',
      );
    }

    // 2. Fetch orders by createdAt (for ordersByStatus and recentOrders)
    let ordersQuery = this.firestore.collection('orders').where('shopId', '==', shopId);

    if (fromDate) {
      ordersQuery = ordersQuery.where('createdAt', '>=', fromDate) as any;
    }
    if (toDate) {
      ordersQuery = ordersQuery.where('createdAt', '<=', toDate) as any;
    }

    const ordersSnapshot = await ordersQuery.get();

    // 3. Process orders
    const allOrders: any[] = [];
    const ordersByStatus: { [status: string]: number } = {};

    ordersSnapshot.docs.forEach((doc) => {
      const order: any = { id: doc.id, ...doc.data() };
      allOrders.push(order);

      // Count by status (based on createdAt filter)
      ordersByStatus[order.status] = (ordersByStatus[order.status] || 0) + 1;
    });

    // 4. Filter DELIVERED orders and extract delivery timestamp
    const deliveredOrders = allOrders
      .filter((o) => o.status === 'DELIVERED')
      .map((o) => {
        // Use deliveredAt, fallback to updatedAt, then createdAt
        let deliveryDate: Date | null = null;
        if (o.deliveredAt?.toDate) {
          deliveryDate = o.deliveredAt.toDate();
        } else if (o.updatedAt?.toDate) {
          deliveryDate = o.updatedAt.toDate();
        } else if (o.createdAt?.toDate) {
          deliveryDate = o.createdAt.toDate();
        }
        return { ...o, deliveryDate };
      })
      .filter((o) => o.deliveryDate !== null); // Exclude if no timestamp

    // 5. Calculate all-time revenue and order counts (within range if provided)
    const allTimeRevenue = deliveredOrders.reduce((sum, o) => sum + (o.total || 0), 0);
    const allTimeOrderCount = deliveredOrders.length;

    // 6. Calculate top products (from all delivered orders)
    const productSales = new Map<string, { name: string; count: number; revenue: number }>();
    deliveredOrders.forEach((order) => {
      if (order.items) {
        order.items.forEach((item: any) => {
          const existing = productSales.get(item.productId) || {
            name: item.productName || 'Unknown Product',
            count: 0,
            revenue: 0,
          };
          existing.count += item.quantity || 0;
          existing.revenue += (item.price || 0) * (item.quantity || 0);
          productSales.set(item.productId, existing);
        });
      }
    });

    const topProducts = Array.from(productSales.entries())
      .map(([id, data]) => ({
        id,
        name: data.name,
        soldCount: data.count,
        revenue: data.revenue,
      }))
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 5);

    // 7. Get recent 10 orders (sorted by createdAt desc)
    const recentOrders = allOrders
      .sort((a, b) => {
        const dateA = a.createdAt?.toDate?.().getTime() || 0;
        const dateB = b.createdAt?.toDate?.().getTime() || 0;
        return dateB - dateA;
      })
      .slice(0, 10)
      .map((o) => ({
        id: o.id,
        orderNumber: o.orderNumber,
        status: o.status,
        total: o.total || 0,
        createdAt: o.createdAt?.toDate?.().toISOString() || '',
      }));

    // 8. Return analytics
    return {
      allTime: {
        revenue: allTimeRevenue,
        orderCount: allTimeOrderCount,
        avgOrderValue: allTimeOrderCount > 0 ? allTimeRevenue / allTimeOrderCount : 0,
      },
      ordersByStatus,
      topProducts,
      recentOrders,
    };
  }
}
