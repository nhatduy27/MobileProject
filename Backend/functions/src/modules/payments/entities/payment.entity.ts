import { Timestamp } from 'firebase-admin/firestore';

export enum PaymentMethod {
  COD = 'COD',
  SEPAY = 'SEPAY',
}

export enum PaymentStatus {
  UNPAID = 'UNPAID',
  PROCESSING = 'PROCESSING',
  PAID = 'PAID',
  REFUNDED = 'REFUNDED',
}

export interface PaymentProviderData {
  transactionId?: string;
  paymentUrl?: string;
  qrCode?: string;
  [key: string]: any;
}

export class PaymentEntity {
  id?: string;
  orderId: string;
  orderNumber: string;
  customerId: string;
  shopId: string;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  providerData?: PaymentProviderData;
  paidAt?: Timestamp;
  refundedAt?: Timestamp;
  refundReason?: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
