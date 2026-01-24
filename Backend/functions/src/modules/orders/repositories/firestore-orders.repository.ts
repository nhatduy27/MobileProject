import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue, Timestamp } from 'firebase-admin/firestore';
import { IOrdersRepository } from '../interfaces';
import { OrderEntity, OrderStatus, PaymentStatus } from '../entities';
import { FirestoreErrorHandler } from '../../../core/filters/firestore-error.handler';
import { removeUndefinedDeep } from '../utils/address.normalizer';

@Injectable()
export class FirestoreOrdersRepository implements IOrdersRepository {
  private readonly ordersCollection = 'orders';
  private readonly cartsCollection = 'carts';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async findById(id: string): Promise<OrderEntity | null> {
    const doc = await this.firestore
      .collection(this.ordersCollection)
      .doc(id)
      .get();

    if (!doc.exists) {
      return null;
    }

    return this.mapToEntity(doc);
  }

  async findByCustomerId(
    customerId: string,
    options?: { limit?: number; startAfter?: any }
  ): Promise<OrderEntity[]> {
    const limit = options?.limit || 10;
    let query = this.firestore
      .collection(this.ordersCollection)
      .where('customerId', '==', customerId)
      .orderBy('createdAt', 'desc')
      .limit(limit);

    if (options?.startAfter) {
      query = query.startAfter(options.startAfter);
    }

    const snapshot = await query.get();
    return snapshot.docs.map((doc) => this.mapToEntity(doc)!);
  }

  async findByShopId(
    shopId: string,
    options?: { limit?: number; startAfter?: any }
  ): Promise<OrderEntity[]> {
    const limit = options?.limit || 10;
    let query = this.firestore
      .collection(this.ordersCollection)
      .where('shopId', '==', shopId)
      .orderBy('createdAt', 'desc')
      .limit(limit);

    if (options?.startAfter) {
      query = query.startAfter(options.startAfter);
    }

    const snapshot = await query.get();
    return snapshot.docs.map((doc) => this.mapToEntity(doc)!);
  }

  async findMany(query: any): Promise<OrderEntity[]> {
    try {
      const snapshot = await query.get();
      return snapshot.docs.map((doc: any) => this.mapToEntity(doc)!);
    } catch (error) {
      FirestoreErrorHandler.handle(error);
    }
  }

  async create(order: OrderEntity): Promise<OrderEntity> {
    const docRef = this.firestore.collection(this.ordersCollection).doc();

    const orderData = {
      ...order,
      id: docRef.id,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    };

    await docRef.set(orderData);

    return {
      ...order,
      id: docRef.id,
    };
  }

  async update(id: string, updates: Partial<OrderEntity>): Promise<void> {
    const updateData: any = {
      ...updates,
      updatedAt: FieldValue.serverTimestamp(),
    };

    await this.firestore
      .collection(this.ordersCollection)
      .doc(id)
      .update(updateData);
  }

  query() {
    return this.firestore.collection(this.ordersCollection);
  }

  async count(where?: Partial<Record<keyof OrderEntity, any>>): Promise<number> {
    try {
      let query: any = this.firestore.collection(this.ordersCollection);

      if (where) {
        // Build query with where clauses
        // IMPORTANT: Include null values (e.g., shipperId === null means "unassigned")
        // Previously skipped null values, causing COUNT to return different results than FETCH
        const entries = Object.entries(where);
        for (const [key, value] of entries) {
          if (value !== undefined) {  // Allow null, only skip undefined
            query = query.where(key, '==', value);
          }
        }
      }

      const snapshot = await query.count().get();
      return snapshot.data().count;
    } catch (error) {
      FirestoreErrorHandler.handle(error);
    }
  }

  /**
   * Creates an order and atomically clears the cart group in a transaction
   * CRITICAL: Ensure all validation is done in service layer BEFORE calling this
   * 
   * @param additionalTransactionOps Optional callback for additional transaction operations (e.g., apply voucher)
   */
  async createOrderAndClearCartGroup(
    customerId: string,
    shopId: string,
    orderData: OrderEntity,
    additionalTransactionOps?: () => Promise<void>,
  ): Promise<OrderEntity> {
    // TRANSACTION LAYER (atomic writes only)
    // Cart existence and shop-group validation happens in service layer BEFORE this is called.
    // This transaction simply performs atomic: create order + update/delete cart group.
    //
    // CRITICAL: Firestore requires ALL reads BEFORE any writes in a transaction.

    return await this.firestore.runTransaction(async (transaction) => {
      // ====================================================================
      // PHASE A: READS ONLY (must happen first)
      // ====================================================================

      // 1. Read cart document first (before any writes)
      const cartRef = this.firestore
        .collection(this.cartsCollection)
        .doc(customerId);
      const cartSnap = await transaction.get(cartRef);

      // ====================================================================
      // PHASE B: WRITES ONLY (all reads completed)
      // ====================================================================

      // 2. Create order document
      const orderRef = this.firestore.collection(this.ordersCollection).doc();

      // Remove undefined values before saving to Firestore
      // This prevents "Cannot use 'undefined' as a Firestore value" errors
      // (e.g., when using new KTX format without legacy street/ward/district/city fields)
      const cleanedOrderData = removeUndefinedDeep(orderData);

      const newOrder = {
        ...cleanedOrderData,
        id: orderRef.id,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      };

      transaction.set(orderRef, newOrder);

      // 3. Execute additional transaction operations (e.g., apply voucher atomically)
      if (additionalTransactionOps) {
        await additionalTransactionOps();
      }

      // 4. Update cart: Remove items for this shop only
      // Cart MUST exist at this point (validated in service layer)
      if (cartSnap.exists) {
        const cart = cartSnap.data();
        if (cart) {
          const remainingItems = cart.items.filter(
            (item: any) => item.shopId !== shopId
          );

          if (remainingItems.length === 0) {
            // Cart now empty after removing this shop group - delete document
            transaction.delete(cartRef);
          } else {
            // Cart has items from other shops - update with remaining items
            transaction.update(cartRef, {
              items: remainingItems,
              updatedAt: FieldValue.serverTimestamp(),
            });
          }
        }
      }

      return {
        ...orderData,
        id: orderRef.id,
      };
    });
  }

  /**
   * Atomically accept order and update shipper status
   * CRITICAL: Firestore transaction prevents race condition where two shippers accept same order
   * 
   * Transaction flow:
   * 1. READ: Get current order state (verify shipperId is still null)
   * 2. READ: Get shipper state (verify status still AVAILABLE)
   * 3. WRITE: Update order with shipperId, status=SHIPPING, timestamps
   * 4. WRITE: Update shipper with status=BUSY
   */
  async acceptOrderAtomically(
    orderId: string,
    shipperId: string,
  ): Promise<any> {
    return await this.firestore.runTransaction(async (transaction) => {
      // ====================================================================
      // PHASE A: READS ONLY (must happen first)
      // ====================================================================

      // 1. Read order to verify state hasn't changed
      const orderRef = this.firestore.collection(this.ordersCollection).doc(orderId);
      const orderSnap = await transaction.get(orderRef);

      if (!orderSnap.exists) {
        throw new Error(`Order ${orderId} not found`);
      }

      const order = orderSnap.data();

      if (!order) {
        throw new Error(`Order ${orderId} data not found`);
      }

      // 2. Verify order is still READY and unassigned (prevents race condition)
      if (order.status !== 'READY') {
        throw new Error(`Order is no longer READY (current: ${order.status})`);
      }

      if (order.shipperId) {
        throw new Error(`Order already accepted by another shipper: ${order.shipperId}`);
      }

      // 3. Read shipper to verify availability
      // FIX: Read from 'users' collection (not 'shippers'), shipper profile stored in users/{uid}.shipperInfo
      const shipperRef = this.firestore.collection('users').doc(shipperId);
      const shipperSnap = await transaction.get(shipperRef);

      if (!shipperSnap.exists) {
        throw new Error(`Shipper ${shipperId} not found`);
      }

      const shipperData = shipperSnap.data();

      // 4. Verify shipper is still AVAILABLE
      if (shipperData?.shipperInfo?.status !== 'AVAILABLE') {
        throw new Error(`Shipper is no longer AVAILABLE (current: ${shipperData?.shipperInfo?.status})`);
      }

      // ====================================================================
      // PHASE B: WRITES ONLY (all reads completed)
      // ====================================================================

      // 5. Update order: set shipperId only
      // OPTION-1-FIX: Do NOT change status (still READY)
      // Status will be changed later when shipper marks as shipping
      const now = Timestamp.now();
      transaction.update(orderRef, {
        shipperId,
        // FIX: Status stays READY (do not transition to SHIPPING here)
        updatedAt: now,
      });

      // 6. Do NOT update shipper status to BUSY here
      // Shipper should mark as shipping first, THEN set status to BUSY
      // This allows shipper to view order after accept but before shipping

      // 7. Return updated order data
      return {
        ...order,
        id: orderId,
        shipperId,
        // FIX: Status stays READY (not SHIPPING)
        status: order.status, // Keep original status (READY)
        updatedAt: now,
      };
    });
  }

  private mapToEntity(
    doc: FirebaseFirestore.DocumentSnapshot
  ): OrderEntity | null {
    if (!doc.exists) {
      return null;
    }

    const data = doc.data();
    if (!data) {
      return null;
    }

    return {
      id: doc.id,
      orderNumber: data.orderNumber,
      customerId: data.customerId,
      customerSnapshot: data.customerSnapshot, // Read customer snapshot for OWNER list
      shopId: data.shopId,
      shopName: data.shopName,
      shipperId: data.shipperId,
      shipperSnapshot: data.shipperSnapshot, // Read shipper snapshot for OWNER list
      items: data.items || [],
      subtotal: data.subtotal,
      shipFee: data.shipFee,
      discount: data.discount,
      voucherCode: data.voucherCode ?? null,
      voucherId: data.voucherId ?? null,
      total: data.total,
      status: data.status as OrderStatus,
      paymentStatus: data.paymentStatus as PaymentStatus,
      paymentMethod: data.paymentMethod,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
      confirmedAt: data.confirmedAt,
      preparingAt: data.preparingAt,
      readyAt: data.readyAt,
      shippingAt: data.shippingAt,
      deliveredAt: data.deliveredAt,
      cancelledAt: data.cancelledAt,
      deliveryAddress: data.deliveryAddress,
      deliveryNote: data.deliveryNote,
      cancelReason: data.cancelReason,
      cancelledBy: data.cancelledBy,
      reviewId: data.reviewId,
      reviewedAt: data.reviewedAt,
      paidOut: data.paidOut,
      paidOutAt: data.paidOutAt,
    };
  }
}
