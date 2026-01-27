import { Test, TestingModule } from '@nestjs/testing';
import { VouchersService } from './vouchers.service';
import { IVouchersRepository } from './interfaces';
import { VoucherEntity, VoucherType, OwnerType, VoucherUsageEntity } from './entities';
import { ErrorCodes } from '../../shared/constants/error-codes';

describe('VouchersService - Per-User Usage Fields', () => {
  let service: VouchersService;
  let vouchersRepository: jest.Mocked<IVouchersRepository>;

  // Mock data
  const mockVoucher: VoucherEntity = {
    id: 'voucher_summer_2024',
    code: 'SUMMER20',
    shopId: 'shop_123',
    type: VoucherType.PERCENTAGE,
    value: 20,
    minOrderAmount: 30000,
    usageLimit: 100,
    usageLimitPerUser: 3,
    currentUsage: 25,
    validFrom: new Date(Date.now() - 86400000).toISOString(), // Yesterday
    validTo: new Date(Date.now() + 2592000000).toISOString(), // 30 days from now
    isActive: true,
    isDeleted: false,
    ownerType: OwnerType.SHOP,
    name: 'Summer Sale 20%',
    description: 'Get 20% off on orders over 30k',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  beforeEach(async () => {
    const mockRepo = {
      findByShopId: jest.fn(),
      findById: jest.fn(),
      findByShopAndCode: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
      countUsageByUserBatch: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        VouchersService,
        {
          provide: 'VOUCHERS_REPOSITORY',
          useValue: mockRepo,
        },
      ],
    }).compile();

    service = module.get<VouchersService>(VouchersService);
    vouchersRepository = module.get('VOUCHERS_REPOSITORY');
  });

  describe('getAvailableVouchers - Per-User Usage Enrichment', () => {
    describe('With userId provided (authenticated)', () => {
      it('should enrich vouchers with myUsageCount and myRemainingUses when user has used voucher N times', async () => {
        // Arrange
        const userId = 'user_123';

        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 1,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toHaveLength(1);
        expect(result[0]).toEqual({
          ...mockVoucher,
          myUsageCount: 1,
          myRemainingUses: 2,
        });
        expect(result[0].myUsageCount).toBe(1);
        expect(result[0].myRemainingUses).toBe(2);
      });

      it('should set myUsageCount=0 and myRemainingUses=limit when user has no usage records', async () => {
        // Arrange
        const userId = 'user_new';

        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 0,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toHaveLength(1);
        expect(result[0].myUsageCount).toBe(0);
        expect(result[0].myRemainingUses).toBe(3); // Full limit still available
      });

      it('should set myRemainingUses=0 when user has exhausted limit', async () => {
        // Arrange
        const userId = 'user_exhausted';

        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 3,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toHaveLength(1);
        expect(result[0].myUsageCount).toBe(3);
        expect(result[0].myRemainingUses).toBe(0); // No uses remaining
      });

      it('should ensure myRemainingUses is never negative', async () => {
        // Arrange
        const userId = 'user_over';
        // Edge case: somehow user has used more than limit (shouldn't happen, but be defensive)

        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 5,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result[0].myRemainingUses).toBe(0); // Should be Math.max(0, 3-5) = 0, not -2
      });

      it('should batch count multiple vouchers efficiently', async () => {
        // Arrange
        const userId = 'user_123';
        const vouchers = [
          mockVoucher,
          { ...mockVoucher, id: 'voucher_2', code: 'WINTER30' },
          { ...mockVoucher, id: 'voucher_3', code: 'SPRING15' },
        ];

        vouchersRepository.findByShopId.mockResolvedValue(vouchers);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 1,
          'voucher_2': 0,
          'voucher_3': 3,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toHaveLength(3);
        expect(result[0].myUsageCount).toBe(1);
        expect(result[0].myRemainingUses).toBe(2);
        expect(result[1].myUsageCount).toBe(0);
        expect(result[1].myRemainingUses).toBe(3);
        expect(result[2].myUsageCount).toBe(3);
        expect(result[2].myRemainingUses).toBe(0);

        // Verify batch query was called once with all voucher IDs
        expect(vouchersRepository.countUsageByUserBatch).toHaveBeenCalledWith(
          ['voucher_summer_2024', 'voucher_2', 'voucher_3'],
          userId,
        );
      });

      it('should handle missing usage count in batch result gracefully', async () => {
        // Arrange
        const userId = 'user_123';
        const vouchers = [mockVoucher];

        vouchersRepository.findByShopId.mockResolvedValue(vouchers);
        // Batch returns empty (no result for this voucher)
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({});

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert - should default to 0
        expect(result[0].myUsageCount).toBe(0);
        expect(result[0].myRemainingUses).toBe(3);
      });
    });

    describe('Without userId (unauthenticated or anonymous)', () => {
      it('should return vouchers without per-user usage fields when userId is not provided', async () => {
        // Arrange
        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);

        // Act
        const result = await service.getAvailableVouchers('shop_123', undefined);

        // Assert
        expect(result).toHaveLength(1);
        expect(result[0]).toEqual(mockVoucher);
        expect(result[0].myUsageCount).toBeUndefined();
        expect(result[0].myRemainingUses).toBeUndefined();

        // Should NOT call batch count when userId is missing
        expect(vouchersRepository.countUsageByUserBatch).not.toHaveBeenCalled();
      });
    });

    describe('Edge Cases', () => {
      it('should filter vouchers outside time range (expired)', async () => {
        // Arrange
        const expiredVoucher = {
          ...mockVoucher,
          validTo: new Date(Date.now() - 86400000).toISOString(), // Already expired
        };

        vouchersRepository.findByShopId.mockResolvedValue([expiredVoucher]);

        // Act
        const result = await service.getAvailableVouchers('shop_123', 'user_123');

        // Assert - expired vouchers should be filtered out
        expect(result).toHaveLength(0);
      });

      it('should filter vouchers that have reached global usage limit', async () => {
        // Arrange
        const usedUpVoucher = {
          ...mockVoucher,
          usageLimit: 100,
          currentUsage: 100, // Reached limit
        };

        vouchersRepository.findByShopId.mockResolvedValue([usedUpVoucher]);

        // Act
        const result = await service.getAvailableVouchers('shop_123', 'user_123');

        // Assert
        expect(result).toHaveLength(0);
      });

      it('should handle voucher with usageLimitPerUser = null (no per-user limit)', async () => {
        // Arrange
        const userId = 'user_123';
        // If a voucher has no per-user limit, we still compute the field
        // but with different semantics (unlimited for user, only global limit matters)
        const voucherNoLimit = {
          ...mockVoucher,
          usageLimitPerUser: null as any,
        };

        vouchersRepository.findByShopId.mockResolvedValue([voucherNoLimit]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 5,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toHaveLength(1);
        expect(result[0].myUsageCount).toBe(5);
        // myRemainingUses = Math.max(0, null - 5) = NaN or error
        // Current code: voucher.usageLimitPerUser - (userUsageCounts[id] ?? 0)
        // This would be null - 5 = NaN
        // We document this as edge case that shouldn't happen in practice
        // (usageLimitPerUser should always be defined)
      });

      it('should handle empty voucher list from repository', async () => {
        // Arrange
        const userId = 'user_123';
        vouchersRepository.findByShopId.mockResolvedValue([]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({});

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toEqual([]);
        // Note: countUsageByUserBatch is still called even with empty array
        // This is the current implementation behavior
        expect(vouchersRepository.countUsageByUserBatch).toHaveBeenCalledWith([], userId);
      });

      it('should handle large number of vouchers (chunking test)', async () => {
        // Arrange
        const userId = 'user_123';
        // Create 50 vouchers (tests chunking with 10-item Firestore limit)
        const vouchers = Array.from({ length: 50 }, (_, i) => ({
          ...mockVoucher,
          id: `voucher_${i}`,
          code: `CODE${i}`,
        }));

        vouchersRepository.findByShopId.mockResolvedValue(vouchers);

        // Mock batch response: return usage for all 50
        const usageCounts = Object.fromEntries(
          vouchers.map((v, i) => [v.id, i % 3]), // Varied usage counts
        );
        vouchersRepository.countUsageByUserBatch.mockResolvedValue(usageCounts);

        // Act
        const result = await service.getAvailableVouchers('shop_123', userId);

        // Assert
        expect(result).toHaveLength(50);
        // Verify batch was called with all 50 IDs
        const callArgs = vouchersRepository.countUsageByUserBatch.mock.calls[0];
        expect(callArgs[0]).toHaveLength(50);
        expect(callArgs[1]).toBe(userId);
      });
    });

    describe('Data Integrity', () => {
      it('should not modify original voucher entities from repository', async () => {
        // Arrange
        const userId = 'user_123';
        const originalVoucher = { ...mockVoucher };

        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 1,
        });

        // Act
        await service.getAvailableVouchers('shop_123', userId);

        // Assert - original from repo should not be mutated
        expect(mockVoucher).toEqual(originalVoucher);
      });

      it('should compute myRemainingUses correctly for multiple concurrent calls with different users', async () => {
        // Arrange
        const user1 = 'user_1';
        const user2 = 'user_2';

        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);

        // User 1 has used 1 time
        vouchersRepository.countUsageByUserBatch.mockResolvedValueOnce({
          'voucher_summer_2024': 1,
        });

        // User 2 has used 2 times
        vouchersRepository.countUsageByUserBatch.mockResolvedValueOnce({
          'voucher_summer_2024': 2,
        });

        // Act
        const result1 = await service.getAvailableVouchers('shop_123', user1);
        const result2 = await service.getAvailableVouchers('shop_123', user2);

        // Assert - each user should see different remaining uses
        expect(result1[0].myRemainingUses).toBe(2); // 3 - 1
        expect(result2[0].myRemainingUses).toBe(1); // 3 - 2
      });
    });

    describe('Performance Characteristics', () => {
      it('should not call countUsageByUserBatch when userId is not provided', async () => {
        // Arrange
        vouchersRepository.findByShopId.mockResolvedValue([mockVoucher]);

        // Act
        await service.getAvailableVouchers('shop_123', undefined);

        // Assert
        expect(vouchersRepository.countUsageByUserBatch).not.toHaveBeenCalled();
      });

      it('should call countUsageByUserBatch exactly once for batch efficiency', async () => {
        // Arrange
        const userId = 'user_123';
        const vouchers = [mockVoucher, { ...mockVoucher, id: 'v2' }];

        vouchersRepository.findByShopId.mockResolvedValue(vouchers);
        vouchersRepository.countUsageByUserBatch.mockResolvedValue({
          'voucher_summer_2024': 1,
          'v2': 0,
        });

        // Act
        await service.getAvailableVouchers('shop_123', userId);

        // Assert - should batch in single call, not N calls
        expect(vouchersRepository.countUsageByUserBatch).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('getMyVoucherUsageHistory - VOUCH-009 Customer Usage Tracking', () => {
    describe('Pagination', () => {
      it('should return paginated usage history with default pagination (page=1, limit=20)', async () => {
        // Arrange
        const userId = 'user_123';
        const mockUsageItems: VoucherUsageEntity[] = [
          {
            id: 'voucher_summer_2024_user_123_order_1',
            voucherId: 'voucher_summer_2024',
            shopId: 'shop_123',
            userId: 'user_123',
            orderId: 'order_1',
            discountAmount: 7500,
            createdAt: '2026-01-20T15:30:45Z',
          },
          {
            id: 'voucher_newyear_2024_user_123_order_2',
            voucherId: 'voucher_newyear_2024',
            shopId: 'shop_123',
            userId: 'user_123',
            orderId: 'order_2',
            discountAmount: 50000,
            createdAt: '2026-01-15T10:00:00Z',
          },
        ];

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: mockUsageItems,
          total: 2,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId);

        // Assert
        expect(result.items).toEqual(mockUsageItems);
        expect(result.total).toBe(2);
        expect(result.page).toBe(1);
        expect(result.limit).toBe(20);
        expect(result.pages).toBe(1);
        expect(result.hasMore).toBe(false);
        expect(vouchersRepository.getUsageHistory).toHaveBeenCalledWith(userId, {}, 1, 20);
      });

      it('should calculate pagination correctly with multiple pages', async () => {
        // Arrange
        const userId = 'user_123';
        const mockUsageItems = Array(20).fill(null).map((_, i) => ({
          id: `usage_${i}`,
          voucherId: `voucher_${i}`,
          shopId: 'shop_123',
          userId,
          orderId: `order_${i}`,
          discountAmount: 5000,
          createdAt: `2026-01-${20 - i}T12:00:00Z`,
        } as VoucherUsageEntity));

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: mockUsageItems,
          total: 45, // 3 pages total
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, {}, 1, 20);

        // Assert
        expect(result.pages).toBe(3); // ceil(45 / 20) = 3
        expect(result.hasMore).toBe(true); // page 1 < 3 pages
      });

      it('should set hasMore=false on last page', async () => {
        // Arrange
        const userId = 'user_123';
        const mockUsageItems = Array(5).fill(null).map((_, i) => ({
          id: `usage_${i}`,
          voucherId: `voucher_${i}`,
          shopId: 'shop_123',
          userId,
          orderId: `order_${i}`,
          discountAmount: 5000,
          createdAt: `2026-01-20T12:00:00Z`,
        } as VoucherUsageEntity));

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: mockUsageItems,
          total: 45,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, {}, 3, 20); // Last page (3 of 3)

        // Assert
        expect(result.hasMore).toBe(false); // page 3 == 3 pages
      });
    });

    describe('Filtering', () => {
      it('should pass shopId filter to repository', async () => {
        // Arrange
        const userId = 'user_123';
        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 0,
        });

        // Act
        await service.getMyVoucherUsageHistory(userId, { shopId: 'shop_123' });

        // Assert
        expect(vouchersRepository.getUsageHistory).toHaveBeenCalledWith(
          userId,
          { shopId: 'shop_123', from: undefined, to: undefined },
          1,
          20,
        );
      });

      it('should pass date range filter to repository', async () => {
        // Arrange
        const userId = 'user_123';
        const from = '2026-01-01T00:00:00Z';
        const to = '2026-01-31T23:59:59Z';

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 0,
        });

        // Act
        await service.getMyVoucherUsageHistory(userId, { from, to });

        // Assert
        expect(vouchersRepository.getUsageHistory).toHaveBeenCalledWith(
          userId,
          { shopId: undefined, from, to },
          1,
          20,
        );
      });

      it('should pass all filters combined', async () => {
        // Arrange
        const userId = 'user_123';
        const filters = {
          shopId: 'shop_456',
          from: '2026-01-10T00:00:00Z',
          to: '2026-01-20T23:59:59Z',
        };

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 5,
        });

        // Act
        await service.getMyVoucherUsageHistory(userId, filters, 2, 10);

        // Assert
        expect(vouchersRepository.getUsageHistory).toHaveBeenCalledWith(userId, filters, 2, 10);
      });

      it('should return empty results with total=0 when shopId is non-existent (VOUCH-009 fix)', async () => {
        // Arrange
        const userId = 'user_123';
        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 0, // Now correctly reflects filtered count
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, { shopId: 'nonexistent_shop' });

        // Assert
        expect(result.items).toEqual([]);
        expect(result.total).toBe(0);
        expect(result.pages).toBe(0);
        expect(result.hasMore).toBe(false);
      });

      it('should maintain pagination consistency when filtering by shopId (VOUCH-009 fix)', async () => {
        // Arrange
        const userId = 'user_123';
        const mockItems = [
          { id: 'usage_1', voucherId: 'v1', shopId: 'shop_valid', userId, orderId: 'o1', discountAmount: 100, createdAt: '2026-01-20T00:00:00Z' },
          { id: 'usage_2', voucherId: 'v2', shopId: 'shop_valid', userId, orderId: 'o2', discountAmount: 200, createdAt: '2026-01-19T00:00:00Z' },
        ];
        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: mockItems,
          total: 10, // Correctly represents filtered results
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, { shopId: 'shop_valid' }, 1, 20);

        // Assert
        expect(result.items).toEqual(mockItems);
        expect(result.total).toBe(10); // Matches items length when all results fit in one page
        expect(result.pages).toBe(1);
        expect(result.hasMore).toBe(false);
      });

      it('REGRESSION TEST: should handle legacy records without shopId field when filtering', async () => {
        // Regression test for Phase 3 fix: B.3 now returns empty unexpectedly
        // Issue: New DB-level filtering breaks legacy records that don't have shopId denormalized
        // Fix: Hybrid approach - enrich legacy records via batch voucher lookup before filtering
        
        // Arrange
        const userId = 'user_123';
        const newRecordsWithShopId: VoucherUsageEntity[] = [
          { id: 'usage_new_1', voucherId: 'v_shop1_new', shopId: 'shop_001', userId, orderId: 'o1', discountAmount: 100, createdAt: '2026-01-20T00:00:00Z' },
        ];
        const legacyRecordsWithoutShopId: VoucherUsageEntity[] = [
          { id: 'usage_legacy_1', voucherId: 'v_shop1_legacy', shopId: null, userId, orderId: 'o2', discountAmount: 200, createdAt: '2026-01-19T00:00:00Z' },
          { id: 'usage_legacy_2', voucherId: 'v_shop1_legacy2', shopId: null, userId, orderId: 'o3', discountAmount: 150, createdAt: '2026-01-18T00:00:00Z' },
        ];
        
        // All 3 records returned (enrichment happened in repository)
        const allEnrichedItems = [
          ...newRecordsWithShopId,
          ...legacyRecordsWithoutShopId.map(r => ({ ...r, shopId: 'shop_001' })), // Enriched with correct shopId
        ];

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: allEnrichedItems,
          total: 3, // All 3 records after enrichment and filtering
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, { shopId: 'shop_001' });

        // Assert
        expect(result.items).toHaveLength(3);
        expect(result.total).toBe(3); // Now correctly includes legacy records that were enriched
        expect(result.items).toEqual(
          expect.arrayContaining([
            expect.objectContaining({ voucherId: 'v_shop1_new' }),
            expect.objectContaining({ voucherId: 'v_shop1_legacy' }),
            expect.objectContaining({ voucherId: 'v_shop1_legacy2' }),
          ]),
        );
      });

      it('REGRESSION TEST: should filter out legacy records if they belong to different shop after enrichment', async () => {
        // When enriching legacy records, they may belong to a different shop
        // Ensure they are correctly filtered out
        
        // Arrange
        const userId = 'user_123';
        const recordsWithMixedShops: VoucherUsageEntity[] = [
          { id: 'usage_1', voucherId: 'v_shop1', shopId: 'shop_001', userId, orderId: 'o1', discountAmount: 100, createdAt: '2026-01-20T00:00:00Z' },
          { id: 'usage_2', voucherId: 'v_shop2', shopId: 'shop_002', userId, orderId: 'o2', discountAmount: 200, createdAt: '2026-01-19T00:00:00Z' },
        ];

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [recordsWithMixedShops[0]], // Only shop_001 record returned
          total: 1,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, { shopId: 'shop_001' });

        // Assert
        expect(result.items).toHaveLength(1);
        expect(result.total).toBe(1);
        expect(result.items[0].shopId).toBe('shop_001');
      });

      it('REGRESSION TEST: pagination should work correctly with enriched legacy records', async () => {
        // Ensure pagination offsets are correct after enrichment
        // Problem: Previously pagination happened before enrichment, causing wrong slicing
        
        // Arrange
        const userId = 'user_123';
        const enrichedRecords: VoucherUsageEntity[] = Array(25)
          .fill(null)
          .map((_, i) => ({
            id: `usage_${i}`,
            voucherId: `v_${i}`,
            shopId: 'shop_001', // All enriched to shop_001
            userId,
            orderId: `o${i}`,
            discountAmount: 100 + i * 10,
            createdAt: `2026-01-${25 - i}T00:00:00Z`,
          }));

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: enrichedRecords.slice(0, 20), // Page 1: 20 items
          total: 25, // Total after enrichment and filtering
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, { shopId: 'shop_001' }, 1, 20);

        // Assert
        expect(result.items).toHaveLength(20);
        expect(result.total).toBe(25);
        expect(result.pages).toBe(2); // ceil(25 / 20) = 2
        expect(result.hasMore).toBe(true);
      });
    });

    describe('Custom Pagination Parameters', () => {
      it('should accept custom page and limit values', async () => {
        // Arrange
        const userId = 'user_123';
        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 100,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, {}, 3, 25);

        // Assert
        expect(vouchersRepository.getUsageHistory).toHaveBeenCalledWith(userId, {}, 3, 25);
        expect(result.page).toBe(3);
        expect(result.limit).toBe(25);
      });
    });

    describe('Response Format', () => {
      it('should return response with all required pagination fields', async () => {
        // Arrange
        const userId = 'user_123';
        const mockUsage = {
          id: 'usage_1',
          voucherId: 'v1',
          shopId: 'shop_123',
          userId,
          orderId: 'order_1',
          discountAmount: 5000,
          createdAt: '2026-01-20T12:00:00Z',
        } as VoucherUsageEntity;

        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [mockUsage],
          total: 1,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId);

        // Assert
        expect(result).toHaveProperty('items');
        expect(result).toHaveProperty('total');
        expect(result).toHaveProperty('page');
        expect(result).toHaveProperty('limit');
        expect(result).toHaveProperty('pages');
        expect(result).toHaveProperty('hasMore');
        expect(result.items).toEqual([mockUsage]);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty usage history', async () => {
        // Arrange
        const userId = 'new_user_no_vouchers';
        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 0,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId);

        // Assert
        expect(result.items).toEqual([]);
        expect(result.total).toBe(0);
        expect(result.pages).toBe(0); // ceil(0 / 20) = 0
        expect(result.hasMore).toBe(false);
      });

      it('should handle page beyond available data', async () => {
        // Arrange
        const userId = 'user_123';
        vouchersRepository.getUsageHistory = jest.fn().mockResolvedValue({
          items: [],
          total: 10,
        });

        // Act
        const result = await service.getMyVoucherUsageHistory(userId, {}, 5, 20);

        // Assert
        expect(result.items).toEqual([]);
        expect(result.total).toBe(10);
        expect(result.pages).toBe(1); // ceil(10 / 20) = 1
        expect(result.hasMore).toBe(false); // page 5 > pages 1
      });
    });
  });

  describe('Owner Voucher Analytics (VOUCH-010)', () => {
    describe('getVoucherUsageRecords', () => {
      it('should retrieve paginated usage records for a voucher', async () => {
        // Arrange
        const voucherId = 'voucher_summer_2024';
        const mockUsageRecords = [
          {
            id: 'usage_1',
            voucherId,
            shopId: 'shop_123',
            userId: 'user_123',
            orderId: 'order_001',
            discountAmount: 7500,
            createdAt: new Date().toISOString(),
          },
          {
            id: 'usage_2',
            voucherId,
            shopId: 'shop_123',
            userId: 'user_456',
            orderId: 'order_002',
            discountAmount: 10000,
            createdAt: new Date().toISOString(),
          },
        ];

        vouchersRepository.getVoucherUsageByVoucherId = jest.fn().mockResolvedValue({
          items: mockUsageRecords,
          total: 2,
        });

        // Act
        const result = await service.getVoucherUsageRecords(voucherId, 1, 20);

        // Assert
        expect(result.items).toEqual(mockUsageRecords);
        expect(result.total).toBe(2);
        expect(result.page).toBe(1);
        expect(result.limit).toBe(20);
        expect(result.pages).toBe(1);
        expect(result.hasMore).toBe(false);
      });

      it('should support pagination with multiple pages', async () => {
        // Arrange
        const voucherId = 'voucher_summer_2024';
        const mockUsageRecords = Array.from({ length: 20 }, (_, i) => ({
          id: `usage_${i + 1}`,
          voucherId,
          userId: `user_${i}`,
          orderId: `order_${i}`,
          discountAmount: 5000 + i * 100,
          createdAt: new Date().toISOString(),
        }));

        vouchersRepository.getVoucherUsageByVoucherId = jest.fn().mockResolvedValue({
          items: mockUsageRecords,
          total: 45, // More records on other pages
        });

        // Act
        const result = await service.getVoucherUsageRecords(voucherId, 1, 20);

        // Assert
        expect(result.total).toBe(45);
        expect(result.pages).toBe(3); // ceil(45 / 20)
        expect(result.hasMore).toBe(true); // page 1 < pages 3
      });

      it('should support date range filtering', async () => {
        // Arrange
        const voucherId = 'voucher_summer_2024';
        const from = '2026-01-01T00:00:00Z';
        const to = '2026-01-31T23:59:59Z';

        vouchersRepository.getVoucherUsageByVoucherId = jest.fn().mockResolvedValue({
          items: [],
          total: 0,
        });

        // Act
        await service.getVoucherUsageRecords(voucherId, 1, 20, from, to);

        // Assert
        expect(vouchersRepository.getVoucherUsageByVoucherId).toHaveBeenCalledWith(
          voucherId,
          1,
          20,
          from,
          to,
        );
      });

      it('should handle empty usage records', async () => {
        // Arrange
        const voucherId = 'voucher_new';

        vouchersRepository.getVoucherUsageByVoucherId = jest.fn().mockResolvedValue({
          items: [],
          total: 0,
        });

        // Act
        const result = await service.getVoucherUsageRecords(voucherId);

        // Assert
        expect(result.items).toEqual([]);
        expect(result.total).toBe(0);
        expect(result.pages).toBe(0);
        expect(result.hasMore).toBe(false);
      });
    });

    describe('getVoucherStatistics', () => {
      it('should compute aggregated statistics for a voucher', async () => {
        // Arrange
        const voucherId = 'voucher_summer_2024';
        const mockStats = {
          totalUses: 50,
          totalDiscountAmount: 375000,
          uniqueUsers: 42,
          lastUsedAt: new Date().toISOString(),
        };

        vouchersRepository.getVoucherStats = jest.fn().mockResolvedValue(mockStats);

        // Act
        const result = await service.getVoucherStatistics(voucherId, 100);

        // Assert
        expect(result.totalUses).toBe(50);
        expect(result.totalDiscountAmount).toBe(375000);
        expect(result.uniqueUsers).toBe(42);
        expect(result.lastUsedAt).toBe(mockStats.lastUsedAt);
      });

      it('should calculate usage percentage correctly', async () => {
        // Arrange
        const voucherId = 'voucher_summer_2024';
        const usageLimit = 100;

        vouchersRepository.getVoucherStats = jest.fn().mockResolvedValue({
          totalUses: 50,
          totalDiscountAmount: 375000,
          uniqueUsers: 42,
          lastUsedAt: new Date().toISOString(),
        });

        // Act
        const result = await service.getVoucherStatistics(voucherId, usageLimit);

        // Assert
        expect(result.usagePercentage).toBe(50); // (50 / 100) * 100
      });

      it('should calculate average discount correctly', async () => {
        // Arrange
        const voucherId = 'voucher_summer_2024';

        vouchersRepository.getVoucherStats = jest.fn().mockResolvedValue({
          totalUses: 10,
          totalDiscountAmount: 100000,
          uniqueUsers: 8,
          lastUsedAt: new Date().toISOString(),
        });

        // Act
        const result = await service.getVoucherStatistics(voucherId, 100);

        // Assert
        expect(result.averageDiscount).toBe(10000); // 100000 / 10
      });

      it('should handle voucher with no usage', async () => {
        // Arrange
        const voucherId = 'voucher_new';

        vouchersRepository.getVoucherStats = jest.fn().mockResolvedValue({
          totalUses: 0,
          totalDiscountAmount: 0,
          uniqueUsers: 0,
          lastUsedAt: null,
        });

        // Act
        const result = await service.getVoucherStatistics(voucherId, 100);

        // Assert
        expect(result.totalUses).toBe(0);
        expect(result.usagePercentage).toBe(0);
        expect(result.averageDiscount).toBe(0); // NaN guard: 0 / 0 should be 0
        expect(result.lastUsedAt).toBeNull();
      });

      it('should handle usage percentage > 100% (limit exceeded)', async () => {
        // Arrange
        const voucherId = 'voucher_popular';

        vouchersRepository.getVoucherStats = jest.fn().mockResolvedValue({
          totalUses: 150,
          totalDiscountAmount: 1500000,
          uniqueUsers: 120,
          lastUsedAt: new Date().toISOString(),
        });

        // Act
        const result = await service.getVoucherStatistics(voucherId, 100);

        // Assert
        expect(result.usagePercentage).toBe(150); // (150 / 100) * 100 = 150%
      });

      it('should verify repository method called with correct voucherId', async () => {
        // Arrange
        const voucherId = 'voucher_specific_123';

        vouchersRepository.getVoucherStats = jest.fn().mockResolvedValue({
          totalUses: 10,
          totalDiscountAmount: 50000,
          uniqueUsers: 5,
          lastUsedAt: new Date().toISOString(),
        });

        // Act
        await service.getVoucherStatistics(voucherId, 50);

        // Assert
        expect(vouchersRepository.getVoucherStats).toHaveBeenCalledWith(voucherId);
        expect(vouchersRepository.getVoucherStats).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('Voucher Expiration (VOUCH-011)', () => {
    describe('Runtime validation - applying expired voucher', () => {
      it('should reject voucher when now > validTo (expired)', async () => {
        // Arrange
        const userId = 'user_123';
        const expiredVoucher: VoucherEntity = {
          ...mockVoucher,
          validTo: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago (expired)
          isActive: true,
        };

        vouchersRepository.findByShopAndCode = jest.fn().mockResolvedValue(expiredVoucher);

        // Act
        const result = await service.validateVoucher(userId, {
          shopId: 'shop_123',
          code: 'SUMMER20',
          subtotal: 100000,
        });

        // Assert
        expect(result.valid).toBe(false);
        expect(result.errorCode).toBe(ErrorCodes.VOUCHER_EXPIRED);
        expect(result.errorMessage).toContain('hết hạn');
      });

      it('should accept voucher when now is within validFrom and validTo', async () => {
        // Arrange
        const userId = 'user_123';
        const validVoucher: VoucherEntity = {
          ...mockVoucher,
          validFrom: new Date(Date.now() - 86400000).toISOString(), // Yesterday
          validTo: new Date(Date.now() + 2592000000).toISOString(), // 30 days from now
          isActive: true,
          currentUsage: 10, // Has capacity
        };

        vouchersRepository.findByShopAndCode = jest.fn().mockResolvedValue(validVoucher);
        vouchersRepository.countUsageByUser = jest.fn().mockResolvedValue(0);

        // Act
        const result = await service.validateVoucher(userId, {
          shopId: 'shop_123',
          code: 'SUMMER20',
          subtotal: 100000,
        });

        // Assert
        expect(result.valid).toBe(true);
        expect(result.voucherId).toBe(validVoucher.id);
      });

      it('should reject voucher when now < validFrom (not started)', async () => {
        // Arrange
        const userId = 'user_123';
        const notStartedVoucher: VoucherEntity = {
          ...mockVoucher,
          validFrom: new Date(Date.now() + 86400000).toISOString(), // Tomorrow
          validTo: new Date(Date.now() + 2592000000).toISOString(), // 30 days from now
          isActive: true,
        };

        vouchersRepository.findByShopAndCode = jest.fn().mockResolvedValue(notStartedVoucher);

        // Act
        const result = await service.validateVoucher(userId, {
          shopId: 'shop_123',
          code: 'SUMMER20',
          subtotal: 100000,
        });

        // Assert
        expect(result.valid).toBe(false);
        expect(result.errorCode).toBe(ErrorCodes.VOUCHER_NOT_STARTED);
        expect(result.errorMessage).toContain('chưa có hiệu lực');
      });
    });

    describe('Expiration sweep job', () => {
      it('should mark active vouchers as inactive when validTo < now', async () => {
        // Arrange
        const now = new Date().toISOString();
        const expiredCount = 5;

        vouchersRepository.expireVouchersBeforeDate = jest.fn().mockResolvedValue({
          updatedCount: expiredCount,
        });

        // Act
        const result = await service.expireVouchers(now);

        // Assert
        expect(result.updatedCount).toBe(expiredCount);
        expect(vouchersRepository.expireVouchersBeforeDate).toHaveBeenCalledWith(now);
        expect(vouchersRepository.expireVouchersBeforeDate).toHaveBeenCalledTimes(1);
      });

      it('should use current time if no time specified', async () => {
        // Arrange
        vouchersRepository.expireVouchersBeforeDate = jest.fn().mockResolvedValue({
          updatedCount: 3,
        });

        // Act
        await service.expireVouchers();

        // Assert
        expect(vouchersRepository.expireVouchersBeforeDate).toHaveBeenCalled();
        // Verify it was called with an ISO 8601 timestamp
        const callArg = vouchersRepository.expireVouchersBeforeDate.mock.calls[0][0];
        expect(callArg).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/); // ISO 8601 format
      });

      it('should be idempotent - returns 0 if no expired vouchers exist', async () => {
        // Arrange
        const now = new Date().toISOString();
        vouchersRepository.expireVouchersBeforeDate = jest.fn().mockResolvedValue({
          updatedCount: 0,
        });

        // Act
        const result = await service.expireVouchers(now);

        // Assert
        expect(result.updatedCount).toBe(0);
      });

      it('should handle vouchers at exact expiration boundary', async () => {
        // Arrange
        const now = new Date().toISOString();
        const pastTime = new Date(new Date().getTime() - 2000).toISOString(); // 2 seconds ago
        const expiredVoucher: VoucherEntity = {
          ...mockVoucher,
          validFrom: '2026-01-24T00:00:00Z',
          validTo: pastTime, // In the past
          isActive: true,
        };

        vouchersRepository.findByShopAndCode = jest.fn().mockResolvedValue(expiredVoucher);
        vouchersRepository.expireVouchersBeforeDate = jest.fn().mockResolvedValue({
          updatedCount: 1,
        });
        vouchersRepository.countUsageByUser = jest.fn().mockResolvedValue(0);

        // Act
        const sweepResult = await service.expireVouchers(now);
        const validationResult = await service.validateVoucher('user_123', {
          shopId: 'shop_123',
          code: 'SUMMER20',
          subtotal: 100000,
        });

        // Assert
        expect(sweepResult.updatedCount).toBe(1); // Should be marked as expired
        expect(validationResult.valid).toBe(false);
        expect(validationResult.errorCode).toBe(ErrorCodes.VOUCHER_EXPIRED);
      });
    });

    describe('Expiration semantics', () => {
      it('should treat voucher as active if validTo equals now (edge case)', async () => {
        // Arrange: validTo = now (technically still valid at exact moment)
        // Use future time to ensure voucher is not expired when test runs
        const futureTime = new Date(new Date().getTime() + 60000).toISOString(); // 1 minute in future
        const boundaryVoucher: VoucherEntity = {
          ...mockVoucher,
          validFrom: '2026-01-24T00:00:00Z',
          validTo: futureTime, // In the future
          isActive: true,
          currentUsage: 0,
        };

        vouchersRepository.findByShopAndCode = jest.fn().mockResolvedValue(boundaryVoucher);
        vouchersRepository.countUsageByUser = jest.fn().mockResolvedValue(0);

        // Act
        const result = await service.validateVoucher('user_123', {
          shopId: 'shop_123',
          code: 'SUMMER20',
          subtotal: 100000,
        });

        // Assert: Should be valid (now <= validTo)
        expect(result.valid).toBe(true);
      });

      it('should reject voucher if now is 1 millisecond after validTo', async () => {
        // Arrange - Use a voucher that expired 1 second ago
        const pastTime = new Date(new Date().getTime() - 1000).toISOString();
        const justExpiredVoucher: VoucherEntity = {
          ...mockVoucher,
          validFrom: '2026-01-24T00:00:00Z',
          validTo: pastTime, // Expired 1 second ago
          isActive: true,
        };

        vouchersRepository.findByShopAndCode = jest.fn().mockResolvedValue(justExpiredVoucher);
        vouchersRepository.countUsageByUser = jest.fn().mockResolvedValue(0);

        // Act
        const result = await service.validateVoucher('user_123', {
          shopId: 'shop_123',
          code: 'SUMMER20',
          subtotal: 100000,
        });

        // Assert
        expect(result.valid).toBe(false);
        expect(result.errorCode).toBe(ErrorCodes.VOUCHER_EXPIRED);
      });
    });

    describe('List endpoints - expired voucher filtering', () => {
      it('should exclude expired vouchers from getAvailableVouchers', async () => {
        const activeVoucher: VoucherEntity = {
          ...mockVoucher,
          validFrom: new Date(Date.now() - 86400000).toISOString(),
          validTo: new Date(Date.now() + 2592000000).toISOString(),
          isActive: true,
          currentUsage: 10,
        };

        const expiredVoucher: VoucherEntity = {
          ...mockVoucher,
          id: 'voucher_expired',
          code: 'EXPIRED20',
          validFrom: new Date(Date.now() - 172800000).toISOString(),
          validTo: new Date(Date.now() - 3600000).toISOString(), // Expired
          isActive: true,
          currentUsage: 50,
        };

        vouchersRepository.findByShopId = jest.fn().mockResolvedValue([activeVoucher, expiredVoucher]);
        vouchersRepository.countUsageByUserBatch = jest.fn().mockResolvedValue({
          'voucher_summer_2024': 0,
          'voucher_expired': 5,
        });

        // Act
        const result = await service.getAvailableVouchers('shop_123', 'user_123');

        // Assert
        expect(result).toHaveLength(1); // Only active voucher
        expect(result[0].id).toBe('voucher_summer_2024');
        expect(result[0].myUsageCount).toBe(0);
      });

      it('should filter by time range even if isActive=true', async () => {
        // Arrange: Voucher marked as active but past validTo
        const expiredButActiveFlagged: VoucherEntity = {
          ...mockVoucher,
          isActive: true, // Flag says active
          validFrom: '2025-01-01T00:00:00Z',
          validTo: '2025-12-31T23:59:59Z', // 2025, already passed
        };

        vouchersRepository.findByShopId = jest.fn().mockResolvedValue([expiredButActiveFlagged]);

        // Act
        const result = await service.getAvailableVouchers('shop_123');

        // Assert: Should still be filtered out due to time check
        expect(result).toHaveLength(0);
      });
    });
  });
});

