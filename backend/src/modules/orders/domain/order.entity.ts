/**
 * Order Item (Value Object)
 */
export class OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;

  constructor(partial: Partial<OrderItem>) {
    Object.assign(this, partial);
    this.totalPrice = this.quantity * this.unitPrice;
  }
}

/**
 * Order Status Enum
 */
export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PREPARING = 'PREPARING',
  READY = 'READY',
  DELIVERING = 'DELIVERING',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

/**
 * Order Entity (Domain Model)
 * 
 * Represents a food order in the system.
 * This is a domain model, independent of any infrastructure concerns.
 */
export class Order {
  id: string;
  customerId: string;
  sellerId: string;
  items: OrderItem[];
  status: OrderStatus;
  totalAmount: number;
  deliveryAddress?: string;
  notes?: string;
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<Order>) {
    Object.assign(this, partial);
    this.calculateTotal();
  }

  /**
   * Calculate total amount from items
   */
  private calculateTotal(): void {
    this.totalAmount = this.items.reduce(
      (sum, item) => sum + item.totalPrice,
      0,
    );
  }

  /**
   * Change order status
   */
  changeStatus(newStatus: OrderStatus): void {
    this.status = newStatus;
    this.updatedAt = new Date();
  }

  /**
   * Check if order can be cancelled
   */
  canBeCancelled(): boolean {
    return [
      OrderStatus.PENDING,
      OrderStatus.CONFIRMED,
      OrderStatus.PREPARING,
    ].includes(this.status);
  }

  /**
   * Cancel the order
   */
  cancel(): void {
    if (!this.canBeCancelled()) {
      throw new Error(`Order cannot be cancelled in ${this.status} status`);
    }
    this.changeStatus(OrderStatus.CANCELLED);
  }
}
