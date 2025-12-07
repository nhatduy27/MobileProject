/**
 * Restaurant Models
 *
 * Định nghĩa types và interfaces cho Restaurant domain
 */

export interface Restaurant {
  id: string;
  name: string;
  address: string;
  phoneNumber?: string;
  imageUrl?: string;
  rating?: number;
  isOpen?: boolean;
  createdAt?: string;
  // Có thể thêm: cuisine, openingHours, deliveryFee, etc.
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description?: string;
  price: number;
  isAvailable: boolean;
  imageUrl?: string;
  category?: string;
  // Có thể thêm: ingredients, allergens, etc.
}
