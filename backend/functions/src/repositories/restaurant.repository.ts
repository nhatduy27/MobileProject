/**
 * Restaurant Repository
 *
 * Data access layer cho Restaurants và MenuItems collections
 */

import {getFirestore} from "firebase-admin/firestore";
import {Restaurant, MenuItem} from "../models/restaurant.model";

/**
 * Firestore data access cho Restaurants và MenuItems collections.
 */
export class RestaurantRepository {
  private db = getFirestore();
  private collection = "restaurants";
  private menuItemsSubcollection = "menuItems";

  /**
   * Lấy restaurant theo ID
   * @param {string} id Restaurant ID
   */
  async getRestaurantById(id: string): Promise<Restaurant | null> {
    const doc = await this.db.collection(this.collection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as Restaurant;
  }

  /**
   * Lấy tất cả menu items của restaurant
   * @param {string} restaurantId Restaurant ID
   */
  async getMenuItemsByRestaurant(restaurantId: string): Promise<MenuItem[]> {
    // TODO: Add caching
    // TODO: Filter by isAvailable?
    // TODO: Add pagination

    const snapshot = await this.db
      .collection(this.collection)
      .doc(restaurantId)
      .collection(this.menuItemsSubcollection)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      restaurantId,
      ...doc.data(),
    } as MenuItem));
  }

  /**
   * Lấy menu item cụ thể
   * @param {string} restaurantId Restaurant ID
   * @param {string} menuItemId Menu item ID
   */
  async getMenuItemById(
    restaurantId: string,
    menuItemId: string
  ): Promise<MenuItem | null> {
    const doc = await this.db
      .collection(this.collection)
      .doc(restaurantId)
      .collection(this.menuItemsSubcollection)
      .doc(menuItemId)
      .get();

    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      restaurantId,
      ...doc.data(),
    } as MenuItem;
  }

  /**
   * Lấy danh sách restaurants
   * TODO: Add filtering by location, cuisine, rating, etc.
   * @param {number} [limit=20] Số lượng tối đa
   */
  async getRestaurants(limit = 20): Promise<Restaurant[]> {
    const snapshot = await this.db
      .collection(this.collection)
      .where("isOpen", "==", true)
      .limit(limit)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    } as Restaurant));
  }

  /**
   * Check if restaurant is open
   * @param {string} restaurantId Restaurant ID
   */
  async isRestaurantOpen(restaurantId: string): Promise<boolean> {
    const restaurant = await this.getRestaurantById(restaurantId);
    return restaurant?.isOpen ?? false;
  }
}

// Singleton instance
export const restaurantRepository = new RestaurantRepository();
