import { Injectable } from '@nestjs/common';
import { CachePort } from './cache.port';

interface CacheEntry<T> {
  value: T;
  expiresAt?: number;
}

/**
 * In-Memory Cache Adapter
 * 
 * Simple in-memory cache implementation using a Map.
 * This is a stub implementation - replace with Redis or other cache in production.
 */
@Injectable()
export class InMemoryCacheAdapter extends CachePort {
  private cache = new Map<string, CacheEntry<any>>();

  async get<T>(key: string): Promise<T | null> {
    const entry = this.cache.get(key);
    
    if (!entry) {
      return null;
    }

    // Check if expired
    if (entry.expiresAt && Date.now() > entry.expiresAt) {
      this.cache.delete(key);
      return null;
    }

    return entry.value as T;
  }

  async set<T>(key: string, value: T, ttlSeconds?: number): Promise<void> {
    const entry: CacheEntry<T> = {
      value,
      expiresAt: ttlSeconds ? Date.now() + ttlSeconds * 1000 : undefined,
    };
    
    this.cache.set(key, entry);
  }

  async del(key: string): Promise<void> {
    this.cache.delete(key);
  }
}
