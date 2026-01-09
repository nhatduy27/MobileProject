/**
 * Shop Analytics Entity
 * For owner dashboard
 */
export class ShopAnalyticsEntity {
  // Revenue
  totalRevenue: number;
  todayRevenue: number;
  weekRevenue: number;
  monthRevenue: number;

  // Orders
  totalOrders: number;
  pendingOrders: number;
  completedOrders: number;
  cancelledOrders: number;

  // Top products
  topProducts: Array<{
    productId: string;
    productName: string;
    soldCount: number;
    revenue: number;
  }>;

  // Rating
  averageRating: number;
  totalRatings: number;
  recentReviews: Array<{
    userId: string;
    userName: string;
    rating: number;
    comment: string;
    createdAt: Date;
  }>;
}
