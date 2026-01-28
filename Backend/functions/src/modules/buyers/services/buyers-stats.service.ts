import { Injectable, Inject, Logger } from '@nestjs/common';
import { Firestore } from '@google-cloud/firestore';
import { IBuyersRepository } from '../repositories/buyers-repository.interface';
import { calculateBuyerTier } from '../entities/shop-buyer.entity';
import { OrderEntity } from '../../orders/entities';

/**
 * Buyer Stats Sync Service
 * MVP: Synchronous update called from Orders service
 * Phase 2: Replace with Cloud Function trigger
 */
@Injectable()
export class BuyersStatsService {
  private readonly logger = new Logger(BuyersStatsService.name);

  constructor(
    @Inject('FIRESTORE') private readonly firestore: Firestore,
    @Inject('IBuyersRepository')
    private readonly buyersRepo: IBuyersRepository,
  ) {}

  /**
   * Update buyer stats when order is delivered
   * Called synchronously from OrdersService.markDelivered()
   *
   * @param order - The delivered order
   */
  async updateBuyerStatsOnDelivery(order: OrderEntity): Promise<void> {
    try {
      const { customerId, shopId } = order;

      if (!customerId || !shopId) {
        this.logger.warn(`Missing customerId or shopId for order ${order.id}`);
        return;
      }

      // Get all DELIVERED orders for this customer in this shop
      const ordersSnapshot = await this.firestore
        .collection('orders')
        .where('shopId', '==', shopId)
        .where('customerId', '==', customerId)
        .where('status', '==', 'DELIVERED')
        .orderBy('createdAt', 'desc')
        .get();

      if (ordersSnapshot.empty) {
        this.logger.warn(`No DELIVERED orders found for customer ${customerId} in shop ${shopId}`);
        return;
      }

      // Calculate stats from all delivered orders
      const orders = ordersSnapshot.docs.map((doc) => doc.data());
      const totalOrders = orders.length;
      const totalSpent = orders.reduce((sum, o) => sum + (o.total || 0), 0);
      const avgOrderValue = totalSpent / totalOrders;

      // Sort by createdAt to get first and last
      const sortedOrders = [...orders].sort((a, b) => {
        const aTime = a.createdAt?.toMillis() || 0;
        const bTime = b.createdAt?.toMillis() || 0;
        return aTime - bTime;
      });

      const firstOrderDate = sortedOrders[0].createdAt?.toDate();
      const lastOrderDate = sortedOrders[sortedOrders.length - 1].createdAt?.toDate();

      // Get customer info from order snapshot
      const displayName =
        order.customerSnapshot?.displayName ||
        order.customer?.displayName ||
        `Customer ${customerId.slice(0, 8)}`;
      const phone = order.customerSnapshot?.phone || order.customer?.phone;

      // Create or update buyer
      await this.buyersRepo.createOrUpdate(shopId, {
        customerId,
        shopId,
        displayName,
        phone,
        totalOrders,
        totalSpent,
        avgOrderValue,
        firstOrderDate,
        lastOrderDate,
        joinedDate: firstOrderDate, // joinedDate = firstOrderDate
      });

      const tier = calculateBuyerTier(totalSpent);
      this.logger.log(
        `Updated buyer stats for ${customerId} in shop ${shopId}: ` +
          `${totalOrders} orders, ${totalSpent}đ total, tier=${tier}`,
      );
    } catch (error) {
      // Non-blocking: log error but don't throw
      // Order delivery should succeed even if buyer stats fail
      this.logger.error(`Failed to update buyer stats for order ${order.id}:`, error);
    }
  }
}
