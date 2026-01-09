import { Injectable, Inject } from '@nestjs/common';
import { Firestore } from 'firebase-admin/firestore';
import { ShopAnalyticsEntity } from '../entities/shop-analytics.entity';

@Injectable()
export class AnalyticsService {
  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  /**
   * Get shop analytics for owner dashboard
   */
  async getShopAnalytics(shopId: string): Promise<ShopAnalyticsEntity> {
    // Get orders for this shop
    const ordersSnapshot = await this.firestore
      .collection('orders')
      .where('shopId', '==', shopId)
      .get();

    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const weekStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);

    let totalRevenue = 0;
    let todayRevenue = 0;
    let weekRevenue = 0;
    let monthRevenue = 0;
    let totalOrders = 0;
    let pendingOrders = 0;
    let completedOrders = 0;
    let cancelledOrders = 0;

    const productSales = new Map<string, { name: string; count: number; revenue: number }>();

    ordersSnapshot.docs.forEach((doc) => {
      const order = doc.data();
      const orderDate = order.createdAt?.toDate();

      totalOrders++;

      // Count by status
      if (order.status === 'PENDING' || order.status === 'PREPARING') pendingOrders++;
      if (order.status === 'COMPLETED') completedOrders++;
      if (order.status === 'CANCELLED') cancelledOrders++;

      // Calculate revenue (only completed orders)
      if (order.status === 'COMPLETED') {
        const amount = order.totalAmount || 0;
        totalRevenue += amount;

        if (orderDate >= todayStart) todayRevenue += amount;
        if (orderDate >= weekStart) weekRevenue += amount;
        if (orderDate >= monthStart) monthRevenue += amount;

        // Track product sales
        if (order.items) {
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
      }
    });

    // Get top 5 products
    const topProducts = Array.from(productSales.entries())
      .map(([productId, data]) => ({
        productId,
        productName: data.name,
        soldCount: data.count,
        revenue: data.revenue,
      }))
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 5);

    // Get recent reviews
    const reviewsSnapshot = await this.firestore
      .collection('reviews')
      .where('shopId', '==', shopId)
      .orderBy('createdAt', 'desc')
      .limit(5)
      .get();

    const recentReviews = reviewsSnapshot.docs.map((doc) => {
      const review = doc.data();
      return {
        userId: review.userId,
        userName: review.userName,
        rating: review.rating,
        comment: review.comment,
        createdAt: review.createdAt?.toDate(),
      };
    });

    // Get shop rating
    const shopDoc = await this.firestore.collection('shops').doc(shopId).get();
    const shopData = shopDoc.data();

    return {
      totalRevenue,
      todayRevenue,
      weekRevenue,
      monthRevenue,
      totalOrders,
      pendingOrders,
      completedOrders,
      cancelledOrders,
      topProducts,
      averageRating: shopData?.rating || 0,
      totalRatings: shopData?.totalRatings || 0,
      recentReviews,
    };
  }
}
