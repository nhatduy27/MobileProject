import { CartEntity } from '../entities';

export interface ICartRepository {
  findByCustomerId(customerId: string): Promise<CartEntity | null>;
  create(cart: CartEntity): Promise<CartEntity>;
  update(cart: CartEntity): Promise<void>;
  delete(customerId: string): Promise<void>;
}

export const CART_REPOSITORY = 'CART_REPOSITORY';
