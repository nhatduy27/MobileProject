import { Timestamp } from 'firebase-admin/firestore';
import { CartItem } from './cart-item.entity';

export class CartEntity {
  customerId: string; // Document ID
  items: CartItem[];
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
