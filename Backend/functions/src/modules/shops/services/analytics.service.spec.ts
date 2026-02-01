import { Test, TestingModule } from '@nestjs/testing';
import { BadRequestException } from '@nestjs/common';
import { AnalyticsService } from './analytics.service';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let mockFirestore: any;

  beforeEach(async () => {
    mockFirestore = {
      collection: jest.fn().mockReturnThis(),
      where: jest.fn().mockReturnThis(),
      get: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AnalyticsService,
        {
          provide: 'FIRESTORE',
          useValue: mockFirestore,
        },
      ],
    }).compile();

    service = module.get<AnalyticsService>(AnalyticsService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('getShopAnalytics - Date Parameter Validation', () => {
    it('should throw error if to < from', async () => {
      mockFirestore.get.mockResolvedValue({ docs: [] });

      await expect(
        service.getShopAnalytics('shop_1', '2026-01-31', '2026-01-01'),
      ).rejects.toThrow(BadRequestException);
    });

    it('should throw error if date range exceeds 1 year', async () => {
      mockFirestore.get.mockResolvedValue({ docs: [] });

      await expect(
        service.getShopAnalytics('shop_1', '2024-01-01', '2026-01-01'),
      ).rejects.toThrow('Date range cannot exceed 1 year');
    });

    it('should throw error if only from is provided without to', async () => {
      mockFirestore.get.mockResolvedValue({ docs: [] });

      await expect(service.getShopAnalytics('shop_1', '2026-01-01')).rejects.toThrow(
        'Both from and to parameters are required',
      );
    });

    it('should throw error if only to is provided without from', async () => {
      mockFirestore.get.mockResolvedValue({ docs: [] });

      await expect(service.getShopAnalytics('shop_1', undefined, '2026-01-31')).rejects.toThrow(
        'Both from and to parameters are required',
      );
    });

    it('should throw error for invalid date format', async () => {
      mockFirestore.get.mockResolvedValue({ docs: [] });

      await expect(
        service.getShopAnalytics('shop_1', 'invalid-date', '2026-01-31'),
      ).rejects.toThrow('Invalid date format');
    });
  });

  describe('getShopAnalytics - Revenue Calculation', () => {
    it('should only count DELIVERED orders for revenue', async () => {
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-31T10:00:00Z', '2026-01-31T12:00:00Z'),
        createMockOrder('order_2', 'PENDING', 30000, '2026-01-31T11:00:00Z', null),
        createMockOrder('order_3', 'CANCELLED', 20000, '2026-01-31T09:00:00Z', null),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      expect(result.allTime.orderCount).toBe(1);
      expect(result.allTime.revenue).toBe(50000);
      expect(result.ordersByStatus.DELIVERED).toBe(1);
      expect(result.ordersByStatus.PENDING).toBe(1);
      expect(result.ordersByStatus.CANCELLED).toBe(1);
    });

    it('should use deliveredAt fallback chain for revenue calculations', async () => {
      const orders = [
        {
          id: 'order_1',
          data: () => ({
            orderNumber: 'ORD-order_1',
            status: 'DELIVERED',
            total: 50000,
            createdAt: createTimestamp('2026-01-10T10:00:00Z'),
            updatedAt: createTimestamp('2026-01-31T12:00:00Z'),
            // deliveredAt missing
            items: [],
          }),
        },
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      expect(result.allTime.orderCount).toBe(1);
      expect(result.allTime.revenue).toBe(50000);
    });
  });

  describe('getShopAnalytics - Top Products and Recent Orders', () => {
    it('should calculate top products from DELIVERED orders only', async () => {
      const orders = [
        createMockOrderWithItems('order_1', 'DELIVERED', 100000, '2026-01-15T10:00:00Z', '2026-01-15T12:00:00Z', [
          { productId: 'prod_1', productName: 'Com suon', quantity: 2, price: 40000 },
          { productId: 'prod_2', productName: 'Pho bo', quantity: 1, price: 50000 },
        ]),
        createMockOrderWithItems('order_2', 'PENDING', 60000, '2026-01-16T10:00:00Z', null, [
          { productId: 'prod_1', productName: 'Com suon', quantity: 3, price: 40000 },
        ]),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      const comSuon = result.topProducts.find((p) => p.id === 'prod_1');
      expect(comSuon?.soldCount).toBe(2);
    });

    it('should sort recentOrders by createdAt desc', async () => {
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-10T10:00:00Z', '2026-01-10T12:00:00Z'),
        createMockOrder('order_2', 'PENDING', 30000, '2026-01-20T10:00:00Z', null),
        createMockOrder('order_3', 'DELIVERED', 40000, '2026-01-15T10:00:00Z', '2026-01-15T12:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      expect(result.recentOrders[0].id).toBe('order_2');
      expect(result.recentOrders[1].id).toBe('order_3');
      expect(result.recentOrders[2].id).toBe('order_1');
    });
  });
});

// Helper functions
function createMockOrder(
  id: string,
  status: string,
  total: number,
  createdAt: string,
  deliveredAt: string | null,
) {
  return {
    id,
    data: () => ({
      orderNumber: `ORD-${id}`,
      status,
      total,
      createdAt: createTimestamp(createdAt),
      updatedAt: createTimestamp(createdAt),
      deliveredAt: deliveredAt ? createTimestamp(deliveredAt) : null,
      items: [],
    }),
  };
}

function createMockOrderWithItems(
  id: string,
  status: string,
  total: number,
  createdAt: string,
  deliveredAt: string | null,
  items: any[],
) {
  return {
    id,
    data: () => ({
      orderNumber: `ORD-${id}`,
      status,
      total,
      createdAt: createTimestamp(createdAt),
      updatedAt: createTimestamp(createdAt),
      deliveredAt: deliveredAt ? createTimestamp(deliveredAt) : null,
      items,
    }),
  };
}

function createTimestamp(isoString: string): any {
  const date = new Date(isoString);
  return {
    toDate: () => date,
    seconds: Math.floor(date.getTime() / 1000),
    nanoseconds: 0,
  };
}
