import { Order, OrderItem, OrderStatus } from './order.entity';

/**
 * Type for creating a new order (without generated fields and methods)
 */
export type CreateOrderDto = {
  customerId: string;
  sellerId: string;
  items: OrderItem[];
  status: OrderStatus;
  totalAmount: number;
  deliveryAddress?: string;
  notes?: string;
};

/**
 * Order Repository Port (Abstraction)
 * 
 * This abstract class defines the contract for order data access.
 * Implementations can use Firebase Firestore, PostgreSQL, MongoDB, etc.
 */
export abstract class OrderRepository {
  /**
   * Create a new order
   */
  abstract create(order: CreateOrderDto): Promise<Order>;

  /**
   * Find an order by ID
   */
  abstract findById(id: string): Promise<Order | null>;

  /**
   * Find all orders for a specific customer
   */
  abstract findByCustomer(customerId: string): Promise<Order[]>;

  /**
   * Find all orders for a specific seller
   */
  abstract findBySeller(sellerId: string): Promise<Order[]>;

  /**
   * Update an existing order
   */
  abstract update(id: string, order: Partial<Order>): Promise<Order>;

  /**
   * Delete an order by ID
   */
  abstract delete(id: string): Promise<void>;
}
