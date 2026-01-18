import { Timestamp } from 'firebase-admin/firestore';

export class CartItem {
  productId: string;
  shopId: string;
  productName: string; // Snapshot at add time
  productImage: string; // Snapshot at add time
  quantity: number;
  priceAtAdd: number; // Snapshot at add time
  addedAt: Timestamp;
  updatedAt: Timestamp;
}
