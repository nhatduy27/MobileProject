import { Injectable } from '@nestjs/common';
import { FirestoreService, BaseDocument } from '../../core/firebase/firestore.service';
import { Category } from './entities/category.entity';

const COLLECTION_NAME = 'categories';

/**
 * Categories Repository
 *
 * Repository pattern implementation for Categories collection.
 * Encapsulates all Firestore operations.
 */
@Injectable()
export class CategoriesRepository {
  constructor(private readonly firestoreService: FirestoreService) {}

  async findAll(): Promise<Category[]> {
    const result = await this.firestoreService.findAll<Category>(COLLECTION_NAME, {
      orderBy: 'sortOrder',
      orderDirection: 'asc',
      limit: 100,
    });
    return result.data;
  }

  async findActive(): Promise<Category[]> {
    return this.firestoreService.findByField<Category>(
      COLLECTION_NAME,
      'isActive',
      true,
    );
  }

  async findById(id: string): Promise<Category | null> {
    return this.firestoreService.findById<Category>(COLLECTION_NAME, id);
  }

  async create(data: Omit<Category, keyof BaseDocument>): Promise<Category> {
    return this.firestoreService.create<Category>(COLLECTION_NAME, data);
  }

  async update(
    id: string,
    data: Partial<Omit<Category, keyof BaseDocument>>,
  ): Promise<Category | null> {
    return this.firestoreService.update<Category>(COLLECTION_NAME, id, data);
  }

  async delete(id: string): Promise<boolean> {
    return this.firestoreService.delete(COLLECTION_NAME, id);
  }

  async exists(id: string): Promise<boolean> {
    return this.firestoreService.exists(COLLECTION_NAME, id);
  }
}
