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
   * - When from/to are provided:
   *   - "today" bucket = [startOfDay(to), endOfDay(to)]
   *   - "thisWeek" bucket = [to - 6 days, endOfDay(to)]
   *   - "thisMonth" bucket = [startOfMonth(to), endOfDay(to)]
   *   - All buckets clamped within [from, to] range
   * - When from/to are NOT provided:
   *   - Buckets calculated relative to server's current time
   *
   * TIMESTAMP RULES:
   * - Revenue/orderCount use deliveredAt (when order was delivered)
   * - Fallback: deliveredAt → updatedAt → createdAt if missing
   * - Only DELIVERED orders count toward revenue/orderCount
   * - ordersByStatus: counts all orders by createdAt (any status)
   * - recentOrders: sorted by createdAt desc (shows order activity)
   * - pendingOrders: counts orders with status in [PENDING, CONFIRMED, PREPARING, READY, SHIPPING]
   *   created within "today" bucket
   */
  async getShopAnalytics(shopId: string, from?: string, to?: string): Promise<ShopAnalyticsEntity> {
    // 1. Parse and validate date parameters
    let fromDate: Date | null = null;
    let toDate: Date | null = null;
    let anchorDate: Date;

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

      anchorDate = toDate;
    } else if (from || to) {
      throw new BadRequestException('Both from and to parameters are required when filtering by date');
    } else {
      // No date params: use server's current time
      anchorDate = new Date();
    }

    // 2. Calculate time bucket boundaries
    const todayStart = new Date(
      anchorDate.getFullYear(),
      anchorDate.getMonth(),
      anchorDate.getDate(),
      0,
      0,
      0,
      0,
    );
    const todayEnd = new Date(
      anchorDate.getFullYear(),
      anchorDate.getMonth(),
      anchorDate.getDate(),
      23,
      59,
      59,
      999,
    );

    // thisWeek = 7 days ending on anchor date
    const weekStart = new Date(todayStart.getTime() - 6 * 24 * 60 * 60 * 1000);
    const weekEnd = todayEnd;

    // thisMonth = from 1st of anchor's month to anchor date
    const monthStart = new Date(anchorDate.getFullYear(), anchorDate.getMonth(), 1, 0, 0, 0, 0);
    const monthEnd = todayEnd;

    // Clamp buckets to [from, to] if provided
    const clampedTodayStart = fromDate && todayStart < fromDate ? fromDate : todayStart;
    const clampedTodayEnd = toDate && todayEnd > toDate ? toDate : todayEnd;

    const clampedWeekStart = fromDate && weekStart < fromDate ? fromDate : weekStart;
    const clampedWeekEnd = toDate && weekEnd > toDate ? toDate : weekEnd;

    const clampedMonthStart = fromDate && monthStart < fromDate ? fromDate : monthStart;
    const clampedMonthEnd = toDate && monthEnd > toDate ? toDate : monthEnd;

    // 3. Fetch orders by createdAt (for ordersByStatus and recentOrders)
    let ordersQuery = this.firestore.collection('orders').where('shopId', '==', shopId);

    if (fromDate) {
      ordersQuery = ordersQuery.where('createdAt', '>=', fromDate) as any;
    }
    if (toDate) {
      ordersQuery = ordersQuery.where('createdAt', '<=', toDate) as any;
    }

    const ordersSnapshot = await ordersQuery.get();

    // 4. Process orders
    const allOrders: any[] = [];
    const ordersByStatus: { [status: string]: number } = {};

    ordersSnapshot.docs.forEach((doc) => {
      const order: any = { id: doc.id, ...doc.data() };
      allOrders.push(order);

      // Count by status (based on createdAt filter)
      ordersByStatus[order.status] = (ordersByStatus[order.status] || 0) + 1;
    });

    // 5. Filter DELIVERED orders and extract delivery timestamp
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

    // 6. Group delivered orders by time buckets
    const todayDelivered = deliveredOrders.filter(
      (o) => o.deliveryDate >= clampedTodayStart && o.deliveryDate <= clampedTodayEnd,
    );
    const weekDelivered = deliveredOrders.filter(
      (o) => o.deliveryDate >= clampedWeekStart && o.deliveryDate <= clampedWeekEnd,
    );
    const monthDelivered = deliveredOrders.filter(
      (o) => o.deliveryDate >= clampedMonthStart && o.deliveryDate <= clampedMonthEnd,
    );

    // 7. Calculate revenue and order counts
    const todayRevenue = todayDelivered.reduce((sum, o) => sum + (o.total || 0), 0);
    const todayOrderCount = todayDelivered.length;

    const weekRevenue = weekDelivered.reduce((sum, o) => sum + (o.total || 0), 0);
    const weekOrderCount = weekDelivered.length;

    const monthRevenue = monthDelivered.reduce((sum, o) => sum + (o.total || 0), 0);
    const monthOrderCount = monthDelivered.length;

    // 8. Calculate pending orders (based on createdAt within today bucket)
    const pendingStatuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING'];
    const todayPendingOrders = allOrders.filter((o) => {
      const orderDate = o.createdAt?.toDate();
      return (
        orderDate &&
        orderDate >= clampedTodayStart &&
        orderDate <= clampedTodayEnd &&
        pendingStatuses.includes(o.status)
      );
    }).length;

    // 9. Calculate top products (from all delivered orders)
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

    // 10. Get recent 10 orders (sorted by createdAt desc)
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

    // 11. Return analytics
    return {
      today: {
        revenue: todayRevenue,
        orderCount: todayOrderCount,
        avgOrderValue: todayOrderCount > 0 ? todayRevenue / todayOrderCount : 0,
        pendingOrders: todayPendingOrders,
      },
      thisWeek: {
        revenue: weekRevenue,
        orderCount: weekOrderCount,
        avgOrderValue: weekOrderCount > 0 ? weekRevenue / weekOrderCount : 0,
      },
      thisMonth: {
        revenue: monthRevenue,
        orderCount: monthOrderCount,
      },
      ordersByStatus,
      topProducts,
      recentOrders,
    };
  }
}
