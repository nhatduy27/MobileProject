import { Test, TestingModule } from '@nestjs/testing';
import { BadRequestException } from '@nestjs/common';
import { AnalyticsService } from './analytics.service';

/**
 * Analytics Service Tests
 *
 * Tests the fixed dashboard analytics behavior:
 * 1. Time buckets respect from/to query parameters
 * 2. Revenue uses deliveredAt instead of createdAt
 * 3. Consistent metrics across ordersByStatus and time buckets
 */

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let mockFirestore: any;

  beforeEach(async () => {
    // Mock Firestore
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

  describe('getShopAnalytics - Time Bucket Calculation', () => {
    it('should calculate today bucket based on "to" parameter', async () => {
      // Create orders: one on 2026-01-15, one on 2026-01-31
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-15T10:00:00Z', '2026-01-15T12:00:00Z'),
        createMockOrder('order_2', 'DELIVERED', 30000, '2026-01-31T10:00:00Z', '2026-01-31T14:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // "today" should only include order delivered on 2026-01-31
      expect(result.today.orderCount).toBe(1);
      expect(result.today.revenue).toBe(30000);
    });

    it('should calculate thisWeek bucket as 7 days ending on "to" date', async () => {
      // Create orders: one delivered on Jan 20, one on Jan 27 (within 7 days of Jan 31)
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-20T10:00:00Z', '2026-01-20T12:00:00Z'),
        createMockOrder('order_2', 'DELIVERED', 30000, '2026-01-27T10:00:00Z', '2026-01-27T14:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // thisWeek = [Jan 25 - Jan 31], should only include order_2
      expect(result.thisWeek.orderCount).toBe(1);
      expect(result.thisWeek.revenue).toBe(30000);
    });

    it('should calculate thisMonth bucket from 1st of month to "to" date', async () => {
      // Create orders throughout January
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-05T10:00:00Z', '2026-01-05T12:00:00Z'),
        createMockOrder('order_2', 'DELIVERED', 30000, '2026-01-15T10:00:00Z', '2026-01-15T14:00:00Z'),
        createMockOrder('order_3', 'DELIVERED', 20000, '2026-01-25T10:00:00Z', '2026-01-25T16:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // thisMonth should include all January orders
      expect(result.thisMonth.orderCount).toBe(3);
      expect(result.thisMonth.revenue).toBe(100000);
    });
  });

  describe('getShopAnalytics - deliveredAt vs createdAt', () => {
    it('should use deliveredAt for revenue calculations, not createdAt', async () => {
      // Order created on Jan 10 but delivered on Jan 31
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-10T10:00:00Z', '2026-01-31T12:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // Should be in "today" (Jan 31) based on deliveredAt, not createdAt (Jan 10)
      expect(result.today.orderCount).toBe(1);
      expect(result.today.revenue).toBe(50000);
    });

    it('should fallback to updatedAt if deliveredAt is missing', async () => {
      const orders = [
        {
          id: 'order_1',
          data: () => ({
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

      // Should use updatedAt (Jan 31) as fallback
      expect(result.today.orderCount).toBe(1);
      expect(result.today.revenue).toBe(50000);
    });

    it('should fallback to createdAt if both deliveredAt and updatedAt are missing', async () => {
      const orders = [
        {
          id: 'order_1',
          data: () => ({
            status: 'DELIVERED',
            total: 50000,
            createdAt: createTimestamp('2026-01-31T10:00:00Z'),
            // updatedAt missing
            // deliveredAt missing
            items: [],
          }),
        },
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // Should use createdAt (Jan 31) as last fallback
      expect(result.today.orderCount).toBe(1);
      expect(result.today.revenue).toBe(50000);
    });
  });

  describe('getShopAnalytics - Status Filtering', () => {
    it('should only count DELIVERED orders for revenue', async () => {
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-31T10:00:00Z', '2026-01-31T12:00:00Z'),
        createMockOrder('order_2', 'PENDING', 30000, '2026-01-31T11:00:00Z', null),
        createMockOrder('order_3', 'CANCELLED', 20000, '2026-01-31T09:00:00Z', null),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // Only DELIVERED order should count toward revenue
      expect(result.today.orderCount).toBe(1);
      expect(result.today.revenue).toBe(50000);

      // But ordersByStatus should count all
      expect(result.ordersByStatus.DELIVERED).toBe(1);
      expect(result.ordersByStatus.PENDING).toBe(1);
      expect(result.ordersByStatus.CANCELLED).toBe(1);
    });

    it('should count pending orders correctly', async () => {
      const orders = [
        createMockOrder('order_1', 'PENDING', 30000, '2026-01-31T10:00:00Z', null),
        createMockOrder('order_2', 'CONFIRMED', 40000, '2026-01-31T11:00:00Z', null),
        createMockOrder('order_3', 'PREPARING', 50000, '2026-01-31T09:00:00Z', null),
        createMockOrder('order_4', 'DELIVERED', 60000, '2026-01-31T08:00:00Z', '2026-01-31T12:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // pendingOrders = orders with status in [PENDING, CONFIRMED, PREPARING, READY, SHIPPING]
      // created within "today" bucket
      expect(result.today.pendingOrders).toBe(3);
    });
  });

  describe('getShopAnalytics - Consistency Check', () => {
    it('should maintain consistency between ordersByStatus and time buckets', async () => {
      // Reproduce the original bug scenario
      const orders = [
        // 9 DELIVERED orders from earlier in the month
        ...Array.from({ length: 9 }, (_, i) =>
          createMockOrder(
            `order_${i + 1}`,
            'DELIVERED',
            50000,
            `2026-01-${10 + i}T10:00:00Z`,
            `2026-01-${10 + i}T14:00:00Z`,
          ),
        ),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // With the fix, thisMonth should match ordersByStatus count
      expect(result.ordersByStatus.DELIVERED).toBe(9);
      expect(result.thisMonth.orderCount).toBe(9);
      expect(result.thisMonth.revenue).toBe(450000);

      // today should be 0 (no orders delivered on Jan 31)
      expect(result.today.orderCount).toBe(0);
      expect(result.today.revenue).toBe(0);
    });
  });

  describe('getShopAnalytics - Without Date Parameters', () => {
    it('should use server current time when no date params provided', async () => {
      // Mock orders from various dates
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-15T10:00:00Z', '2026-01-15T12:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      // Should not throw error
      const result = await service.getShopAnalytics('shop_1');

      expect(result).toBeDefined();
      expect(result.today).toBeDefined();
      expect(result.thisWeek).toBeDefined();
      expect(result.thisMonth).toBeDefined();
    });
  });

  describe('getShopAnalytics - Top Products', () => {
    it('should calculate top products from DELIVERED orders only', async () => {
      const orders = [
        createMockOrderWithItems('order_1', 'DELIVERED', 100000, '2026-01-15T10:00:00Z', '2026-01-15T12:00:00Z', [
          { productId: 'prod_1', productName: 'Cơm sườn', quantity: 2, price: 40000 },
          { productId: 'prod_2', productName: 'Phở bò', quantity: 1, price: 50000 },
        ]),
        createMockOrderWithItems('order_2', 'PENDING', 60000, '2026-01-16T10:00:00Z', null, [
          { productId: 'prod_1', productName: 'Cơm sườn', quantity: 3, price: 40000 },
        ]),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // Should only count items from DELIVERED orders
      expect(result.topProducts.length).toBeGreaterThan(0);
      const comSuon = result.topProducts.find((p) => p.id === 'prod_1');
      expect(comSuon?.soldCount).toBe(2); // Only from order_1, not order_2
    });
  });

  describe('getShopAnalytics - Recent Orders', () => {
    it('should sort recentOrders by createdAt desc', async () => {
      const orders = [
        createMockOrder('order_1', 'DELIVERED', 50000, '2026-01-10T10:00:00Z', '2026-01-10T12:00:00Z'),
        createMockOrder('order_2', 'PENDING', 30000, '2026-01-20T10:00:00Z', null),
        createMockOrder('order_3', 'DELIVERED', 40000, '2026-01-15T10:00:00Z', '2026-01-15T12:00:00Z'),
      ];

      mockFirestore.get.mockResolvedValue({ docs: orders });

      const result = await service.getShopAnalytics('shop_1', '2026-01-01', '2026-01-31');

      // Should be sorted by createdAt desc: order_2 (Jan 20), order_3 (Jan 15), order_1 (Jan 10)
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
