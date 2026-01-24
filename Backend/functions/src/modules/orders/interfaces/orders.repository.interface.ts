import { OrderEntity } from '../entities';

export interface IOrdersRepository {
  findById(id: string): Promise<OrderEntity | null>;
  findByCustomerId(
    customerId: string,
    options?: { limit?: number; startAfter?: any }
  ): Promise<OrderEntity[]>;
  findByShopId(
    shopId: string,
    options?: { limit?: number; startAfter?: any }
  ): Promise<OrderEntity[]>;
  findMany(query: any): Promise<OrderEntity[]>;
  count(where?: Partial<Record<keyof OrderEntity, any>>): Promise<number>;
  create(order: OrderEntity): Promise<OrderEntity>;
  update(id: string, updates: Partial<OrderEntity>): Promise<void>;
  query(): any;
  createOrderAndClearCartGroup(
    customerId: string,
    shopId: string,
    orderData: OrderEntity,
    additionalTransactionOps?: () => Promise<void>,
  ): Promise<OrderEntity>;
  acceptOrderAtomically(
    orderId: string,
    shipperId: string
  ): Promise<OrderEntity>;
}

export const ORDERS_REPOSITORY = 'ORDERS_REPOSITORY';
