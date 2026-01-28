import {
  isValidSingleWrap,
  isDoubleWrapped,
  getUnwrappedData,
  isDataNotWrapped,
} from './response-validation.utils';

describe('Response Validation Utils', () => {
  describe('isValidSingleWrap', () => {
    it('should validate correct single-wrapped response', () => {
      const response = {
        success: true,
        data: { id: 'test_123', name: 'Test Item' },
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(true);
    });

    it('should reject double-wrapped response', () => {
      const response = {
        success: true,
        data: {
          success: true,
          data: { id: 'test_123' },
          timestamp: new Date().toISOString(),
        },
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(false);
    });

    it('should reject response without success field', () => {
      const response = {
        data: { id: 'test_123' },
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(false);
    });

    it('should reject response without data field', () => {
      const response = {
        success: true,
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(false);
    });

    it('should reject response without timestamp field', () => {
      const response = {
        success: true,
        data: { id: 'test_123' },
      };
      expect(isValidSingleWrap(response)).toBe(false);
    });

    it('should reject response with non-boolean success', () => {
      const response = {
        success: 'true',
        data: { id: 'test_123' },
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(false);
    });

    it('should reject response with non-string timestamp', () => {
      const response = {
        success: true,
        data: { id: 'test_123' },
        timestamp: 123456,
      };
      expect(isValidSingleWrap(response)).toBe(false);
    });

    it('should reject null response', () => {
      expect(isValidSingleWrap(null)).toBe(false);
    });

    it('should reject undefined response', () => {
      expect(isValidSingleWrap(undefined)).toBe(false);
    });

    it('should accept error response (success: false)', () => {
      const response = {
        success: false,
        data: {
          message: 'Not found',
          errorCode: 'NOT_FOUND',
        },
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(true);
    });

    it('should accept response with array data', () => {
      const response = {
        success: true,
        data: [{ id: '1' }, { id: '2' }],
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(true);
    });

    it('should accept response with string data', () => {
      const response = {
        success: true,
        data: 'success message',
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(true);
    });

    it('should accept response with null data', () => {
      const response = {
        success: true,
        data: null,
        timestamp: new Date().toISOString(),
      };
      expect(isValidSingleWrap(response)).toBe(true);
    });
  });

  describe('isDoubleWrapped', () => {
    it('should detect double-wrapped response', () => {
      const response = {
        success: true,
        data: {
          success: true,
          data: { id: 'test_123' },
          timestamp: new Date().toISOString(),
        },
        timestamp: new Date().toISOString(),
      };
      expect(isDoubleWrapped(response)).toBe(true);
    });

    it('should return false for single-wrapped response', () => {
      const response = {
        success: true,
        data: { id: 'test_123' },
        timestamp: new Date().toISOString(),
      };
      expect(isDoubleWrapped(response)).toBe(false);
    });

    it('should return false for response without nested structure', () => {
      const response = {
        success: true,
        data: [{ id: '1' }, { id: '2' }],
        timestamp: new Date().toISOString(),
      };
      expect(isDoubleWrapped(response)).toBe(false);
    });

    it('should return false for null response', () => {
      expect(isDoubleWrapped(null)).toBe(false);
    });

    it('should detect double-wrapped with error in nested data', () => {
      const response = {
        success: false,
        data: {
          success: false,
          data: { message: 'Not found' },
          timestamp: new Date().toISOString(),
        },
        timestamp: new Date().toISOString(),
      };
      expect(isDoubleWrapped(response)).toBe(true);
    });
  });

  describe('getUnwrappedData', () => {
    it('should return data from single-wrapped response', () => {
      const data = { id: 'test_123', name: 'Test' };
      const response = {
        success: true,
        data,
        timestamp: new Date().toISOString(),
      };
      expect(getUnwrappedData(response)).toEqual(data);
    });

    it('should unwrap double-wrapped response once', () => {
      const actualData = { id: 'test_123' };
      const response = {
        success: true,
        data: {
          success: true,
          data: actualData,
          timestamp: new Date().toISOString(),
        },
        timestamp: new Date().toISOString(),
      };
      expect(getUnwrappedData(response)).toEqual(actualData);
    });

    it('should return null data as-is', () => {
      const response = {
        success: true,
        data: null,
        timestamp: new Date().toISOString(),
      };
      expect(getUnwrappedData(response)).toBeNull();
    });

    it('should return response as-is if not wrapped', () => {
      const data = { id: 'test_123' };
      expect(getUnwrappedData(data)).toEqual(data);
    });

    it('should return null for null input', () => {
      expect(getUnwrappedData(null)).toBeNull();
    });
  });

  describe('isDataNotWrapped', () => {
    it('should accept simple object data', () => {
      const data = { id: 'test_123', name: 'Test Item' };
      expect(isDataNotWrapped(data)).toBe(true);
    });

    it('should accept array data', () => {
      const data = [{ id: '1' }, { id: '2' }];
      expect(isDataNotWrapped(data)).toBe(true);
    });

    it('should accept string data', () => {
      expect(isDataNotWrapped('success message')).toBe(true);
    });

    it('should accept null data', () => {
      expect(isDataNotWrapped(null)).toBe(true);
    });

    it('should reject wrapped data (has success/data/timestamp)', () => {
      const wrappedData = {
        success: true,
        data: { id: 'test_123' },
        timestamp: new Date().toISOString(),
      };
      expect(isDataNotWrapped(wrappedData)).toBe(false);
    });

    it('should accept object with success but no data/timestamp', () => {
      const data = { success: true, message: 'all good' };
      expect(isDataNotWrapped(data)).toBe(true);
    });

    it('should accept object with data and timestamp but no success', () => {
      const data = { data: 'some value', timestamp: '2026-01-18T10:00:00Z' };
      expect(isDataNotWrapped(data)).toBe(true);
    });

    it('should return true for non-object types', () => {
      expect(isDataNotWrapped('string')).toBe(true);
      expect(isDataNotWrapped(123)).toBe(true);
      expect(isDataNotWrapped(true)).toBe(true);
      expect(isDataNotWrapped(undefined)).toBe(true);
    });
  });

  describe('Integration: Single vs Double Wrap Detection', () => {
    it('should detect single-wrapped success response from orders.controller', () => {
      const response = {
        success: true,
        data: {
          id: 'order_123',
          customerId: 'user_abc',
          status: 'PENDING',
          totalPrice: 150000,
          createdAt: '2026-01-18T10:00:00Z',
        },
        timestamp: '2026-01-18T10:05:00Z',
      };

      expect(isValidSingleWrap(response)).toBe(true);
      expect(isDoubleWrapped(response)).toBe(false);
      expect(isDataNotWrapped(response.data)).toBe(true);
    });

    it('should detect double-wrapped response (before fix)', () => {
      const response = {
        success: true,
        data: {
          success: true,
          data: {
            id: 'order_123',
            customerId: 'user_abc',
            status: 'PENDING',
            totalPrice: 150000,
            createdAt: '2026-01-18T10:00:00Z',
          },
          timestamp: '2026-01-18T10:05:00Z',
        },
        timestamp: '2026-01-18T10:05:00Z',
      };

      expect(isValidSingleWrap(response)).toBe(false);
      expect(isDoubleWrapped(response)).toBe(true);
      expect(getUnwrappedData(response)).toEqual(response.data.data);
    });

    it('should detect single-wrapped paginated response from cart.controller', () => {
      const response = {
        success: true,
        data: {
          groups: [
            {
              shopId: 'shop_123',
              items: [{ productId: 'prod_1', quantity: 2, price: 35000 }],
            },
          ],
          total: 1,
          page: 1,
          limit: 10,
        },
        timestamp: '2026-01-18T10:05:00Z',
      };

      expect(isValidSingleWrap(response)).toBe(true);
      expect(isDoubleWrapped(response)).toBe(false);
    });

    it('should detect single-wrapped error response', () => {
      const response = {
        success: false,
        data: {
          message: 'Firestore error: FAILED_PRECONDITION',
          errorCode: 'FIRESTORE_ERROR',
          details: {
            firestoreMessage: 'The query requires an index',
            indexUrl: 'https://console.firebase.google.com/firestore/indexes',
          },
        },
        timestamp: '2026-01-18T10:05:00Z',
      };

      expect(isValidSingleWrap(response)).toBe(true);
      expect(isDoubleWrapped(response)).toBe(false);
    });
  });
});
