import { Injectable, Inject } from '@nestjs/common';
import { Firestore } from 'firebase-admin/firestore';
import { ShopAnalyticsEntity } from '../entities/shop-analytics.entity';

@Injectable()
export class AnalyticsService {
  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  /**
   * Get shop analytics for owner dashboard
   */
  async getShopAnalytics(shopId: string, from?: string, to?: string): Promise<ShopAnalyticsEntity> {
    // Get orders for this shop
    let ordersQuery = this.firestore.collection('orders').where('shopId', '==', shopId);

    // Apply date range filter if provided
    if (from) {
      const fromDate = new Date(from);
      ordersQuery = ordersQuery.where('createdAt', '>=', fromDate) as any;
    }
    if (to) {
      const toDate = new Date(to);
      toDate.setHours(23, 59, 59, 999); // End of day
      ordersQuery = ordersQuery.where('createdAt', '<=', toDate) as any;
    }

    const ordersSnapshot = await ordersQuery.get();

    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const weekStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);

    // Initialize counters
    const todayOrders: any[] = [];
    const weekOrders: any[] = [];
    const monthOrders: any[] = [];
    const allOrders: any[] = [];
    const ordersByStatus: { [status: string]: number } = {};
    const productSales = new Map<string, { name: string; count: number; revenue: number }>();

    ordersSnapshot.docs.forEach((doc) => {
      const order: any = { id: doc.id, ...doc.data() };
      const orderDate = order.createdAt?.toDate();

      allOrders.push(order);

      // Count by status
      ordersByStatus[order.status] = (ordersByStatus[order.status] || 0) + 1;

      // Group by time period
      if (orderDate >= todayStart) todayOrders.push(order);
      if (orderDate >= weekStart) weekOrders.push(order);
      if (orderDate >= monthStart) monthOrders.push(order);

      // Track product sales (only completed orders)
      if (order.status === 'COMPLETED' && order.items) {
        order.items.forEach((item: any) => {
          const existing = productSales.get(item.productId) || {
            name: item.productName,
            count: 0,
            revenue: 0,
          };
          existing.count += item.quantity;
          existing.revenue += item.price * item.quantity;
          productSales.set(item.productId, existing);
        });
      }
    });

    // Aggregate today stats
    const todayRevenue = todayOrders
      .filter((o) => o.status === 'COMPLETED')
      .reduce((sum, o) => sum + (o.total || 0), 0);
    const todayOrderCount = todayOrders.filter((o) => o.status === 'COMPLETED').length;
    const todayPendingOrders = todayOrders.filter(
      (o) => o.status === 'PENDING' || o.status === 'PREPARING',
    ).length;

    // Aggregate week stats
    const weekRevenue = weekOrders
      .filter((o) => o.status === 'COMPLETED')
      .reduce((sum, o) => sum + (o.total || 0), 0);
    const weekOrderCount = weekOrders.filter((o) => o.status === 'COMPLETED').length;

    // Aggregate month stats
    const monthRevenue = monthOrders
      .filter((o) => o.status === 'COMPLETED')
      .reduce((sum, o) => sum + (o.total || 0), 0);
    const monthOrderCount = monthOrders.filter((o) => o.status === 'COMPLETED').length;

    // Get top 5 products
    const topProducts = Array.from(productSales.entries())
      .map(([id, data]) => ({
        id,
        name: data.name,
        soldCount: data.count,
        revenue: data.revenue,
      }))
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 5);

    // Get recent 10 orders
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
