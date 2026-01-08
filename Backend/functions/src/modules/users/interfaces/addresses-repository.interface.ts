import { AddressEntity } from '../entities';

/**
 * Addresses Repository Interface
 *
 * Abstraction for address data access (SOLID - Dependency Inversion)
 */
export interface IAddressesRepository {
  /**
   * Create new address
   */
  create(userId: string, data: Partial<AddressEntity>): Promise<AddressEntity>;

  /**
   * Find all addresses for user
   */
  findByUserId(userId: string): Promise<AddressEntity[]>;

  /**
   * Find address by ID
   */
  findById(addressId: string): Promise<AddressEntity | null>;

  /**
   * Update address
   */
  update(addressId: string, data: Partial<AddressEntity>): Promise<AddressEntity>;

  /**
   * Delete address
   */
  delete(addressId: string): Promise<void>;

  /**
   * Set address as default (and unset other defaults)
   */
  setDefault(userId: string, addressId: string): Promise<void>;

  /**
   * Get default address for user
   */
  getDefault(userId: string): Promise<AddressEntity | null>;
}

export const ADDRESSES_REPOSITORY = 'IAddressesRepository';
