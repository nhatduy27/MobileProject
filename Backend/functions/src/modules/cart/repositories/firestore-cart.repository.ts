import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { ICartRepository } from '../interfaces';
import { CartEntity } from '../entities';

@Injectable()
export class FirestoreCartRepository implements ICartRepository {
  private readonly collection = 'carts';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async findByCustomerId(customerId: string): Promise<CartEntity | null> {
    const doc = await this.firestore
      .collection(this.collection)
      .doc(customerId)
      .get();

    if (!doc.exists) {
      return null;
    }

    return this.mapToEntity(doc);
  }

  async create(cart: CartEntity): Promise<CartEntity> {
    await this.firestore
      .collection(this.collection)
      .doc(cart.customerId)
      .set({
        items: cart.items,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      });

    return cart;
  }

  async update(cart: CartEntity): Promise<void> {
    await this.firestore
      .collection(this.collection)
      .doc(cart.customerId)
      .update({
        items: cart.items,
        updatedAt: FieldValue.serverTimestamp(),
      });
  }

  async delete(customerId: string): Promise<void> {
    await this.firestore.collection(this.collection).doc(customerId).delete();
  }

  private mapToEntity(
    doc: FirebaseFirestore.DocumentSnapshot,
  ): CartEntity | null {
    if (!doc.exists) {
      return null;
    }

    const data = doc.data();
    if (!data) {
      return null;
    }

    return {
      customerId: doc.id,
      items: (data.items || []).map((item: any) => ({
        productId: item.productId,
        shopId: item.shopId,
        productName: item.productName,
        productImage: item.productImage,
        quantity: item.quantity,
        priceAtAdd: item.priceAtAdd,
        addedAt: item.addedAt,
        updatedAt: item.updatedAt,
      })),
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  }
}
