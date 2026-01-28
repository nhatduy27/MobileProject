import {
  validatePaginationMeta,
  validatePaginatedResponse,
  expectPaginationStructure,
  PaginationMeta,
} from './pagination-validation.utils';

describe('Pagination Validation Utilities', () => {
  describe('validatePaginationMeta', () => {
    it('should pass with valid pagination meta', () => {
      const meta: PaginationMeta = { page: 1, limit: 10, total: 100, totalPages: 10 };
      expect(() => validatePaginationMeta(meta)).not.toThrow();
    });

    it('should pass with valid values (page 2)', () => {
      const meta: PaginationMeta = { page: 2, limit: 10, total: 100, totalPages: 10 };
      expect(() => validatePaginationMeta(meta)).not.toThrow();
    });

    it('should pass with empty results', () => {
      const meta: PaginationMeta = { page: 1, limit: 10, total: 0, totalPages: 0 };
      expect(() => validatePaginationMeta(meta)).not.toThrow();
    });

    it('should fail if hasNext is present (CRITICAL - rejects old format)', () => {
      const meta = { page: 1, limit: 10, total: 100, totalPages: 10, hasNext: true };
      expect(() => validatePaginationMeta(meta)).toThrow(/unexpected fields.*hasNext/);
    });

    it('should fail if hasPrev is present (CRITICAL - rejects old format)', () => {
      const meta = { page: 1, limit: 10, total: 100, totalPages: 10, hasPrev: false };
      expect(() => validatePaginationMeta(meta)).toThrow(/unexpected fields.*hasPrev/);
    });

    it('should fail if both hasNext and hasPrev are present', () => {
      const meta = {
        page: 1,
        limit: 10,
        total: 100,
        totalPages: 10,
        hasNext: true,
        hasPrev: false,
      };
      expect(() => validatePaginationMeta(meta)).toThrow(/unexpected fields/);
    });

    it('should fail if page is missing', () => {
      const meta = { limit: 10, total: 100, totalPages: 10 } as any;
      expect(() => validatePaginationMeta(meta)).toThrow(/Required pagination field missing.*page/);
    });

    it('should fail if limit is missing', () => {
      const meta = { page: 1, total: 100, totalPages: 10 } as any;
      expect(() => validatePaginationMeta(meta)).toThrow(
        /Required pagination field missing.*limit/,
      );
    });

    it('should fail if total is missing', () => {
      const meta = { page: 1, limit: 10, totalPages: 10 } as any;
      expect(() => validatePaginationMeta(meta)).toThrow(
        /Required pagination field missing.*total/,
      );
    });

    it('should fail if totalPages is missing', () => {
      const meta = { page: 1, limit: 10, total: 100 } as any;
      expect(() => validatePaginationMeta(meta)).toThrow(
        /Required pagination field missing.*totalPages/,
      );
    });

    it('should fail if page is not a number', () => {
      const meta = { page: '1', limit: 10, total: 100, totalPages: 10 } as any;
      expect(() => validatePaginationMeta(meta)).toThrow(/Invalid page/);
    });

    it('should fail if page is 0', () => {
      const meta = { page: 0, limit: 10, total: 100, totalPages: 10 };
      expect(() => validatePaginationMeta(meta)).toThrow(/Invalid page/);
    });

    it('should fail if limit is not a number', () => {
      const meta = { page: 1, limit: '10', total: 100, totalPages: 10 } as any;
      expect(() => validatePaginationMeta(meta)).toThrow(/Invalid limit/);
    });

    it('should fail if limit is 0', () => {
      const meta = { page: 1, limit: 0, total: 100, totalPages: 10 };
      expect(() => validatePaginationMeta(meta)).toThrow(/Invalid limit/);
    });

    it('should fail if total is negative', () => {
      const meta = { page: 1, limit: 10, total: -1, totalPages: 10 };
      expect(() => validatePaginationMeta(meta)).toThrow(/Invalid total/);
    });

    it('should fail if totalPages is negative', () => {
      const meta = { page: 1, limit: 10, total: 100, totalPages: -1 };
      expect(() => validatePaginationMeta(meta)).toThrow(/Invalid totalPages/);
    });

    it('should fail if totalPages does not match calculated value', () => {
      const meta = { page: 1, limit: 10, total: 100, totalPages: 9 };
      expect(() => validatePaginationMeta(meta)).toThrow(/totalPages mismatch/);
    });

    it('should fail if page exceeds totalPages', () => {
      const meta = { page: 11, limit: 10, total: 100, totalPages: 10 };
      expect(() => validatePaginationMeta(meta)).toThrow(/cannot exceed totalPages/);
    });
  });

  describe('validatePaginatedResponse', () => {
    it('should pass with valid response structure', () => {
      const response = {
        success: true,
        data: {
          orders: [
            { id: '1', name: 'Order 1' },
            { id: '2', name: 'Order 2' },
          ],
          page: 1,
          limit: 10,
          total: 42,
          totalPages: 5,
        },
        timestamp: '2024-01-18T10:00:00Z',
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).not.toThrow();
    });

    it('should pass with custom data field name', () => {
      const response = {
        success: true,
        result: {
          items: [{ id: '1' }],
          page: 1,
          limit: 10,
          total: 1,
          totalPages: 1,
        },
      };
      expect(() => validatePaginatedResponse(response, 'result', 'items')).not.toThrow();
    });

    it('should fail with hasNext in response', () => {
      const response = {
        success: true,
        data: {
          orders: [],
          page: 1,
          limit: 10,
          total: 0,
          totalPages: 0,
          hasNext: false,
        },
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).toThrow(
        /unexpected fields.*hasNext/,
      );
    });

    it('should fail with hasPrev in response', () => {
      const response = {
        success: true,
        data: {
          orders: [],
          page: 1,
          limit: 10,
          total: 0,
          totalPages: 0,
          hasPrev: false,
        },
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).toThrow(
        /unexpected fields.*hasPrev/,
      );
    });

    it('should fail if success is false', () => {
      const response = {
        success: false,
        data: { orders: [], page: 1, limit: 10, total: 0, totalPages: 0 },
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).toThrow(
        /success must be true/,
      );
    });

    it('should fail if data field is missing', () => {
      const response = {
        success: true,
        orders: [],
        page: 1,
        limit: 10,
        total: 0,
        totalPages: 0,
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).toThrow(
        /data is missing/,
      );
    });

    it('should fail if items field is not an array', () => {
      const response = {
        success: true,
        data: {
          orders: 'not an array',
          page: 1,
          limit: 10,
          total: 0,
          totalPages: 0,
        },
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).toThrow(
        /must be an array/,
      );
    });

    it('should fail if item count exceeds limit', () => {
      const response = {
        success: true,
        data: {
          orders: [{ id: '1' }, { id: '2' }],
          page: 1,
          limit: 1,
          total: 2,
          totalPages: 2,
        },
      };
      expect(() => validatePaginatedResponse(response, 'data', 'orders')).toThrow(
        /Item count.*exceeds limit/,
      );
    });
  });

  describe('expectPaginationStructure', () => {
    it('should pass with correct structure', () => {
      const data = {
        orders: [{ id: '1' }],
        page: 1,
        limit: 10,
        total: 42,
        totalPages: 5,
      };
      expect(() => expectPaginationStructure(data, 'orders')).not.toThrow();
    });

    it('should pass with expected values', () => {
      const data = {
        orders: [{ id: '1' }],
        page: 1,
        limit: 10,
        total: 42,
        totalPages: 5,
      };
      expect(() =>
        expectPaginationStructure(data, 'orders', { page: 1, limit: 10, total: 42 }),
      ).not.toThrow();
    });

    it('should fail if expected values do not match', () => {
      const data = {
        orders: [{ id: '1' }],
        page: 1,
        limit: 10,
        total: 42,
        totalPages: 5,
      };
      expect(() => expectPaginationStructure(data, 'orders', { total: 100 })).toThrow(
        /total mismatch/,
      );
    });

    it('should fail if items field is missing', () => {
      const data = {
        page: 1,
        limit: 10,
        total: 42,
        totalPages: 5,
      };
      expect(() => expectPaginationStructure(data, 'orders')).toThrow(/not found/);
    });

    it('should reject hasNext in structure', () => {
      const data = {
        orders: [{ id: '1' }],
        page: 1,
        limit: 10,
        total: 42,
        totalPages: 5,
        hasNext: true,
      };
      expect(() => expectPaginationStructure(data, 'orders')).toThrow(/unexpected fields.*hasNext/);
    });
  });
});
