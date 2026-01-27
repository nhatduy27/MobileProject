import { PaymentEntity } from '../entities';

export const PAYMENTS_REPOSITORY_TOKEN = 'PAYMENTS_REPOSITORY';

export interface IPaymentsRepository {
  create(payment: Omit<PaymentEntity, 'id'>): Promise<PaymentEntity>;
  findById(id: string): Promise<PaymentEntity | null>;
  findByOrderId(orderId: string): Promise<PaymentEntity | null>;
  update(id: string, data: Partial<PaymentEntity>): Promise<void>;
}
