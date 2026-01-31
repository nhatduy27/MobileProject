import { Injectable, Inject } from '@nestjs/common';
import Fuse, { IFuseOptions } from 'fuse.js';
import { Firestore } from 'firebase-admin/firestore';
import { globalCache, CACHE_TTL } from '../../../shared/utils';

/**
 * Product Search Item for fuzzy search index
 */
export interface ProductSearchItem {
  id: string;
  name: string;
  nameNormalized: string;
  description: string;
  shopId: string;
  shopName: string;
  categoryId: string;
  categoryName: string;
  price: number;
  imageUrls: string[];
  isAvailable: boolean;
  rating: number;
  soldCount: number;
}

/**
 * Search result with score
 */
interface SearchResult {
  item: ProductSearchItem;
  score?: number;
}

/**
 * Product Search Service
 *
 * Provides fuzzy search capability for products using Fuse.js.
 * Features:
 * - Vietnamese diacritics support (normalizes to ASCII for matching)
 * - Typo tolerance with configurable threshold
 * - In-memory caching for search index
 * - Score-based relevance ranking
 */
@Injectable()
export class ProductSearchService {
  private readonly CACHE_KEY = 'products:search:index';

  private readonly fuseOptions: IFuseOptions<ProductSearchItem> = {
    keys: [
      { name: 'name', weight: 0.4 },
      { name: 'nameNormalized', weight: 0.4 },
      { name: 'description', weight: 0.1 },
      { name: 'categoryName', weight: 0.1 },
    ],
    threshold: 0.4, // 0 = exact, 1 = match anything
    includeScore: true,
    minMatchCharLength: 2,
    ignoreLocation: true,
  };

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  /**
   * Search products with fuzzy matching
   *
   * @param query Search query string
   * @param options Search options
   * @returns Array of matching products
   */
  async searchProducts(
    query: string,
    options: {
      limit?: number;
      shopId?: string;
      categoryId?: string;
      minPrice?: number;
      maxPrice?: number;
    } = {},
  ): Promise<{ products: ProductSearchItem[]; total: number }> {
    const { limit = 20, shopId, categoryId, minPrice, maxPrice } = options;

    if (!query || query.trim().length < 2) {
      return { products: [], total: 0 };
    }

    // Get search index
    const index = await this.getSearchIndex();

    // Create Fuse instance
    const fuse = new Fuse(index, this.fuseOptions);

    // Normalize query for better matching
    const normalizedQuery = this.normalizeVietnamese(query);

    // Search with both original and normalized query
    let results: SearchResult[] = fuse.search(query);

    // If query was normalized, also search with normalized version
    if (normalizedQuery !== query.toLowerCase()) {
      const normalizedResults = fuse.search(normalizedQuery);
      // Merge results, preferring original query matches
      results = this.mergeResults(results, normalizedResults);
    }

    // Apply filters
    let filteredResults = results;

    if (shopId) {
      filteredResults = filteredResults.filter((r) => r.item.shopId === shopId);
    }

    if (categoryId) {
      filteredResults = filteredResults.filter((r) => r.item.categoryId === categoryId);
    }

    if (minPrice !== undefined) {
      filteredResults = filteredResults.filter((r) => r.item.price >= minPrice);
    }

    if (maxPrice !== undefined) {
      filteredResults = filteredResults.filter((r) => r.item.price <= maxPrice);
    }

    // Only return available products
    filteredResults = filteredResults.filter((r) => r.item.isAvailable);

    const total = filteredResults.length;
    const products = filteredResults.slice(0, limit).map((r) => r.item);

    return { products, total };
  }

  /**
   * Get search index from cache or build from Firestore
   */
  private async getSearchIndex(): Promise<ProductSearchItem[]> {
    // Check cache first
    const cached = globalCache.get<ProductSearchItem[]>(this.CACHE_KEY);
    if (cached) {
      return cached;
    }

    console.log('📦 Building search index from Firestore...');

    // Build index from Firestore
    const snapshot = await this.firestore
      .collection('products')
      .where('isDeleted', '==', false)
      .where('isAvailable', '==', true)
      .get();

    const index: ProductSearchItem[] = snapshot.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        name: data.name || '',
        nameNormalized: this.normalizeVietnamese(data.name || ''),
        description: data.description || '',
        shopId: data.shopId,
        shopName: data.shopName || '',
        categoryId: data.categoryId || '',
        categoryName: data.categoryName || '',
        price: data.price || 0,
        imageUrls: data.imageUrls ?? [],
        isAvailable: data.isAvailable ?? true,
        rating: data.rating || 0,
        soldCount: data.soldCount || 0,
      };
    });

    console.log(`📦 Search index built: ${index.length} products`);

    // Cache the index
    globalCache.set(this.CACHE_KEY, index, CACHE_TTL.SEARCH_INDEX);

    return index;
  }

  /**
   * Normalize Vietnamese text by removing diacritics
   * E.g., "Phở bò" -> "pho bo"
   */
  private normalizeVietnamese(text: string): string {
    return text
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .toLowerCase()
      .trim();
  }

  /**
   * Merge two result sets, removing duplicates and preferring lower scores
   */
  private mergeResults(primary: SearchResult[], secondary: SearchResult[]): SearchResult[] {
    const seenIds = new Set<string>();
    const merged: SearchResult[] = [];

    // Add primary results first
    for (const result of primary) {
      if (!seenIds.has(result.item.id)) {
        seenIds.add(result.item.id);
        merged.push(result);
      }
    }

    // Add secondary results that weren't in primary
    for (const result of secondary) {
      if (!seenIds.has(result.item.id)) {
        seenIds.add(result.item.id);
        merged.push(result);
      }
    }

    // Sort by score (lower is better)
    return merged.sort((a, b) => (a.score ?? 1) - (b.score ?? 1));
  }

  /**
   * Invalidate the search index cache
   * Called when products are created/updated/deleted
   */
  invalidateIndex(): void {
    globalCache.invalidate(this.CACHE_KEY);
  }
}
