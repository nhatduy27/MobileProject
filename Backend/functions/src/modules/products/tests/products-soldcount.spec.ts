import { Test, TestingModule } from '@nestjs/testing';
import { ProductsService } from '../services/products.service';
import { IProductsRepository } from '../interfaces';
import { ShopsService } from '../../shops/services/shops.service';
import { StorageService } from '../../../shared/services/storage.service';
import { CategoriesService } from '../../categories/categories.service';

describe('ProductsService - soldCount Operations', () => {
  let service: ProductsService;
  let repository: jest.Mocked<IProductsRepository>;

  beforeEach(async () => {
    const mockRepository = {
      incrementSoldCount: jest.fn(),
      decrementSoldCount: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ProductsService,
        {
          provide: 'PRODUCTS_REPOSITORY',
          useValue: mockRepository,
        },
        {
          provide: ShopsService,
          useValue: {},
        },
        {
          provide: StorageService,
          useValue: {},
        },
        {
          provide: CategoriesService,
          useValue: {},
        },
      ],
    }).compile();

    service = module.get<ProductsService>(ProductsService);
    repository = module.get('PRODUCTS_REPOSITORY');
  });

  describe('incrementSoldCount', () => {
    it('should call repository incrementSoldCount with correct items', async () => {
      // Arrange
      const items = [
        { productId: 'prod_1', quantity: 2 },
        { productId: 'prod_2', quantity: 5 },
      ];

      // Act
      await service.incrementSoldCount(items);

      // Assert
      expect(repository.incrementSoldCount).toHaveBeenCalledWith(items);
      expect(repository.incrementSoldCount).toHaveBeenCalledTimes(1);
    });

    it('should handle single item', async () => {
      // Arrange
      const items = [{ productId: 'prod_1', quantity: 1 }];

      // Act
      await service.incrementSoldCount(items);

      // Assert
      expect(repository.incrementSoldCount).toHaveBeenCalledWith(items);
    });

    it('should handle empty items array', async () => {
      // Arrange
      const items: Array<{ productId: string; quantity: number }> = [];

      // Act
      await service.incrementSoldCount(items);

      // Assert
      expect(repository.incrementSoldCount).toHaveBeenCalledWith(items);
    });
  });

  describe('decrementSoldCount', () => {
    it('should call repository decrementSoldCount with correct items', async () => {
      // Arrange
      const items = [
        { productId: 'prod_1', quantity: 2 },
        { productId: 'prod_2', quantity: 3 },
      ];

      // Act
      await service.decrementSoldCount(items);

      // Assert
      expect(repository.decrementSoldCount).toHaveBeenCalledWith(items);
      expect(repository.decrementSoldCount).toHaveBeenCalledTimes(1);
    });
  });
});
