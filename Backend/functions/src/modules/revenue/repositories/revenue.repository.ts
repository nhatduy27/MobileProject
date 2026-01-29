import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from '@google-cloud/firestore';
import { OrderEntity, OrderStatus } from '../../orders/entities/order.entity';
import { TimeSlotData, TopProduct } from '../entities/revenue-analytics.entity';
import { formatCurrency, calculatePercentage } from '../utils/format.utils';

/**
 * Time slot definitions
 */
export enum TimeSlot {
  MORNING = 'MORNING', // 06:00-10:59
  NOON = 'NOON', // 11:00-16:59
  EVENING = 'EVENING', // 17:00-21:59
}

/**
 * Product aggregation for ranking
 */
interface ProductAgg {
  productName: string;
  quantity: number;
  totalRevenue: number;
}

@Injectable()
export class RevenueRepository {
  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  /**
   * Get DELIVERED orders for a shop within date range
   * (Final state: order has been successfully delivered to customer)
   */
  async getCompletedOrdersByPeriod(
    shopId: string,
    startDate: Date,
    endDate: Date,
  ): Promise<OrderEntity[]> {
    const startTimestamp = Timestamp.fromDate(startDate);
    const endTimestamp = Timestamp.fromDate(endDate);

    const snapshot = await this.firestore
      .collection('orders')
      .where('shopId', '==', shopId)
      .where('status', '==', OrderStatus.DELIVERED) // Using DELIVERED as spec says COMPLETED
      .where('createdAt', '>=', startTimestamp)
      .where('createdAt', '<=', endTimestamp)
      .get();

    return snapshot.docs.map((doc) => this.mapToEntity(doc.id, doc.data()));
  }

  /**
   * Aggregate orders by time slot
   * Returns exactly 3 slots with emoji, title, stats
   */
  aggregateOrdersByTimeSlot(orders: OrderEntity[], totalRevenue: number): TimeSlotData[] {
    const slotAgg = {
      [TimeSlot.MORNING]: { count: 0, revenue: 0 },
      [TimeSlot.NOON]: { count: 0, revenue: 0 },
      [TimeSlot.EVENING]: { count: 0, revenue: 0 },
    };

    // Aggregate orders by time slot
    orders.forEach((order) => {
      const slot = this.getTimeSlot(order.createdAt);
      if (slot) {
        slotAgg[slot].count++;
        slotAgg[slot].revenue += order.total || 0;
      }
    });

    // Format to response structure
    return [
      {
        emoji: '🌅',
        title: 'Sáng (6:00 - 10:59)',
        ordersCount: slotAgg[TimeSlot.MORNING].count,
        percentage: calculatePercentage(slotAgg[TimeSlot.MORNING].revenue, totalRevenue),
        amount: formatCurrency(slotAgg[TimeSlot.MORNING].revenue),
      },
      {
        emoji: '☀️',
        title: 'Trưa (11:00 - 16:59)',
        ordersCount: slotAgg[TimeSlot.NOON].count,
        percentage: calculatePercentage(slotAgg[TimeSlot.NOON].revenue, totalRevenue),
        amount: formatCurrency(slotAgg[TimeSlot.NOON].revenue),
      },
      {
        emoji: '🌙',
        title: 'Tối (17:00 - 21:59)',
        ordersCount: slotAgg[TimeSlot.EVENING].count,
        percentage: calculatePercentage(slotAgg[TimeSlot.EVENING].revenue, totalRevenue),
        amount: formatCurrency(slotAgg[TimeSlot.EVENING].revenue),
      },
    ];
  }

  /**
   * Get top N products by revenue
   * Returns products sorted by totalRevenue DESC
   */
  getRankingByProduct(orders: OrderEntity[], limit: number = 3): TopProduct[] {
    const productMap = new Map<string, ProductAgg>();

    // Aggregate products from all orders
    orders.forEach((order) => {
      order.items?.forEach((item) => {
        const existing = productMap.get(item.productName) || {
          productName: item.productName,
          quantity: 0,
          totalRevenue: 0,
        };

        existing.quantity += item.quantity;
        existing.totalRevenue += item.price * item.quantity;

        productMap.set(item.productName, existing);
      });
    });

    // Sort by totalRevenue DESC, then by quantity DESC (tie-breaker)
    const sorted = Array.from(productMap.values()).sort((a, b) => {
      if (b.totalRevenue !== a.totalRevenue) {
        return b.totalRevenue - a.totalRevenue;
      }
      return b.quantity - a.quantity;
    });

    // Take top N and add rank emoji
    const rankEmojis = ['🥇', '🥈', '🥉'];
    return sorted.slice(0, limit).map((product, index) => ({
      rank: rankEmojis[index] || '',
      name: product.productName,
      quantity: product.quantity,
      unitPrice: Math.round(product.totalRevenue / product.quantity),
      totalRevenue: formatCurrency(product.totalRevenue),
    }));
  }

  /**
   * Determine time slot from order timestamp
   */
  private getTimeSlot(timestamp?: Timestamp): TimeSlot | null {
    if (!timestamp) return null;

    const date = timestamp.toDate();
    const hour = date.getHours();

    if (hour >= 6 && hour <= 10) return TimeSlot.MORNING;
    if (hour >= 11 && hour <= 16) return TimeSlot.NOON;
    if (hour >= 17 && hour <= 21) return TimeSlot.EVENING;

    return null; // Late night (22:00-05:59) not counted
  }

  /**
   * Map Firestore doc to OrderEntity
   */
  private mapToEntity(id: string, data: FirebaseFirestore.DocumentData): OrderEntity {
    return {
      id,
      orderNumber: data.orderNumber,
      customerId: data.customerId,
      shopId: data.shopId,
      shopName: data.shopName,
      shipperId: data.shipperId || null,
      items: data.items || [],
      subtotal: data.subtotal || 0,
      shipFee: data.shipFee || 0,
      discount: data.discount || 0,
      total: data.total || 0,
      status: data.status,
      paymentStatus: data.paymentStatus,
      paymentMethod: data.paymentMethod,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
      deliveryAddress: data.deliveryAddress,
    };
  }
}
