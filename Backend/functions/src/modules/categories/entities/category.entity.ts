import { BaseDocument } from '../../../core/firebase/firestore.service';

/**
 * Category Entity
 *
 * Represents a product category in Firestore.
 */
export interface Category extends BaseDocument {
  /** Category name */
  name: string;

  /** Category description */
  description: string;

  /** Icon/Image URL */
  iconUrl: string;

  /** Sort order for display */
  sortOrder: number;

  /** Whether category is active */
  isActive: boolean;
}
