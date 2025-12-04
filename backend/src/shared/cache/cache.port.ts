/**
 * Cache Port (Abstraction)
 * 
 * This abstract class defines the contract for cache operations.
 * Implementations can be in-memory, Redis, Memcached, etc.
 */
export abstract class CachePort {
  /**
   * Get a cached value by key
   */
  abstract get<T>(key: string): Promise<T | null>;

  /**
   * Set a value in cache with optional TTL
   * @param key - Cache key
   * @param value - Value to cache
   * @param ttlSeconds - Time to live in seconds (optional)
   */
  abstract set<T>(key: string, value: T, ttlSeconds?: number): Promise<void>;

  /**
   * Delete a cached value by key
   */
  abstract del(key: string): Promise<void>;
}
