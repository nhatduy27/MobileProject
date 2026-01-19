import { ErrorCodeNormalizer } from './error-normalizer';

describe('ErrorCodeNormalizer', () => {
  describe('normalize()', () => {
    /**
     * Test 1: error.code as string
     * Expected: uppercase + replace hyphens with underscores
     */
    it('should normalize string error codes to UPPERCASE_WITH_UNDERSCORES', () => {
      expect(ErrorCodeNormalizer.normalize('invalid-argument')).toBe('INVALID_ARGUMENT');
      expect(ErrorCodeNormalizer.normalize('permission-denied')).toBe('PERMISSION_DENIED');
      expect(ErrorCodeNormalizer.normalize('not-found')).toBe('NOT_FOUND');
      expect(ErrorCodeNormalizer.normalize('already-exists')).toBe('ALREADY_EXISTS');
      expect(ErrorCodeNormalizer.normalize('failed-precondition')).toBe('FAILED_PRECONDITION');
      expect(ErrorCodeNormalizer.normalize('ALREADY_NORMALIZED')).toBe('ALREADY_NORMALIZED');
    });

    /**
     * Test 2: error.code as number
     * Expected: HTTP_<num> for status codes (100-599), CODE_<num> for others
     */
    it('should handle number error codes with HTTP_ or CODE_ prefix', () => {
      // HTTP status codes
      expect(ErrorCodeNormalizer.normalize(404)).toBe('HTTP_404');
      expect(ErrorCodeNormalizer.normalize(403)).toBe('HTTP_403');
      expect(ErrorCodeNormalizer.normalize(500)).toBe('HTTP_500');
      expect(ErrorCodeNormalizer.normalize(200)).toBe('HTTP_200');
      expect(ErrorCodeNormalizer.normalize(599)).toBe('HTTP_599');

      // Non-HTTP codes
      expect(ErrorCodeNormalizer.normalize(1)).toBe('CODE_1');
      expect(ErrorCodeNormalizer.normalize(9999)).toBe('CODE_9999');
    });

    /**
     * Test 3: error.code is missing/undefined
     * Expected: UNKNOWN_ERROR (safe fallback)
     */
    it('should return UNKNOWN_ERROR for undefined/null codes', () => {
      expect(ErrorCodeNormalizer.normalize(undefined)).toBe('UNKNOWN_ERROR');
      expect(ErrorCodeNormalizer.normalize(null)).toBe('UNKNOWN_ERROR');
    });

    /**
     * Test 4: error.code as object
     * Expected: UNKNOWN_ERROR (safe fallback, cannot be stringified directly)
     */
    it('should return UNKNOWN_ERROR for object error codes', () => {
      expect(ErrorCodeNormalizer.normalize({})).toBe('UNKNOWN_ERROR');
      expect(ErrorCodeNormalizer.normalize({ code: 'nested' })).toBe('UNKNOWN_ERROR');
      expect(ErrorCodeNormalizer.normalize(['array'])).toBe('UNKNOWN_ERROR');
    });

    /**
     * Test 5: error.code as other types
     * Expected: UNKNOWN_ERROR (safe fallback)
     */
    it('should return UNKNOWN_ERROR for other types (boolean, symbol, function)', () => {
      expect(ErrorCodeNormalizer.normalize(true)).toBe('UNKNOWN_ERROR');
      expect(ErrorCodeNormalizer.normalize(false)).toBe('UNKNOWN_ERROR');
      expect(ErrorCodeNormalizer.normalize(Symbol('test'))).toBe('UNKNOWN_ERROR');
      expect(ErrorCodeNormalizer.normalize(() => {})).toBe('UNKNOWN_ERROR');
    });

    /**
     * Test 6: Never throws an error (defensive)
     * Even with malicious or unexpected input, should return safe string
     */
    it('should never throw an error during normalization (defensive)', () => {
      const maliciousInputs = [
        { toString: () => { throw new Error('Malicious!'); } },
        Object.create(null),
        new Proxy({}, { get: () => { throw new Error('Trap!'); } }),
      ];

      for (const input of maliciousInputs) {
        // Should not throw, should return safe fallback
        expect(() => ErrorCodeNormalizer.normalize(input)).not.toThrow();
        const result = ErrorCodeNormalizer.normalize(input);
        expect(typeof result).toBe('string');
        expect(result).toBe('UNKNOWN_ERROR');
      }
    });
  });

  describe('getMessage()', () => {
    it('should extract message from Error objects', () => {
      const error = new Error('Test error message');
      expect(ErrorCodeNormalizer.getMessage(error)).toBe('Test error message');
    });

    it('should extract message from error-like objects', () => {
      const errorLike = {
        message: 'Custom message',
        code: 'CUSTOM_CODE',
      };
      expect(ErrorCodeNormalizer.getMessage(errorLike)).toBe('Custom message');
    });

    it('should return default message when no message found', () => {
      expect(ErrorCodeNormalizer.getMessage({})).toBe('Operation failed');
      expect(ErrorCodeNormalizer.getMessage(null)).toBe('Operation failed');
      expect(ErrorCodeNormalizer.getMessage(undefined)).toBe('Operation failed');
    });

    it('should use custom default message when provided', () => {
      expect(ErrorCodeNormalizer.getMessage(null, 'Custom default')).toBe('Custom default');
      expect(ErrorCodeNormalizer.getMessage({}, 'DB error')).toBe('DB error');
    });

    it('should never throw an error', () => {
      const maliciousInput = {
        get message() {
          throw new Error('Trap!');
        },
      };

      expect(() => ErrorCodeNormalizer.getMessage(maliciousInput)).not.toThrow();
      expect(ErrorCodeNormalizer.getMessage(maliciousInput)).toBe('Operation failed');
    });
  });

  describe('getSafeDetails()', () => {
    it('should extract safe details from Error objects', () => {
      const error = new Error('Test message');
      (error as any).code = 'TEST_CODE';

      const details = ErrorCodeNormalizer.getSafeDetails(error);
      expect(details.type).toBe('Error');
      expect(details.hasCode).toBe(true);
      expect(details.codeType).toBe('string');
      expect(details.message).toBe('Test message');
    });

    it('should handle Firestore-like error objects', () => {
      const firestoreError = {
        code: 'permission-denied',
        message: 'Permission denied',
      };

      const details = ErrorCodeNormalizer.getSafeDetails(firestoreError);
      expect(details.type).toBe('Object');
      expect(details.hasCode).toBe(true);
      expect(details.codeType).toBe('string');
      expect(details.message).toBe('Permission denied');
    });

    it('should handle errors with numeric codes', () => {
      const errorWithNumCode = {
        code: 404,
        message: 'Not found',
      };

      const details = ErrorCodeNormalizer.getSafeDetails(errorWithNumCode);
      expect(details.codeType).toBe('number');
    });

    it('should handle errors with missing code', () => {
      const errorWithoutCode = {
        message: 'Some error',
      };

      const details = ErrorCodeNormalizer.getSafeDetails(errorWithoutCode);
      expect(details.hasCode).toBe(false);
      expect(details.codeType).toBe('n/a');
    });

    it('should never throw an error', () => {
      const maliciousInput = {
        get type() {
          throw new Error('Trap!');
        },
      };

      expect(() => ErrorCodeNormalizer.getSafeDetails(maliciousInput)).not.toThrow();
      const details = ErrorCodeNormalizer.getSafeDetails(maliciousInput);
      expect(typeof details).toBe('object');
      // Constructor name might be "Object" or "unknown" depending on the input
      expect(['unknown', 'Object']).toContain(details.type);
    });
  });

  /**
   * Integration-like test: Simulate the root cause bug
   * error.code is a number or object, .toUpperCase() would crash
   */
  describe('Integration: Fix for .toUpperCase() crash', () => {
    it('should safely handle error.code that is a number (original bug)', () => {
      // Original code: error.code.toUpperCase() would throw "is not a function"
      const firebaseError = {
        code: 403, // ← Number, not string
        message: 'Permission denied by Firestore security rules',
      };

      // Should not throw
      expect(() => {
        ErrorCodeNormalizer.normalize(firebaseError.code);
      }).not.toThrow();

      // Should return safe string
      const normalized = ErrorCodeNormalizer.normalize(firebaseError.code);
      expect(normalized).toBe('HTTP_403');
    });

    it('should safely handle error.code that is an object (original bug)', () => {
      // Original code: error.code.toUpperCase() would throw "is not a function"
      const malformedError = {
        code: { _type: 'custom-error' }, // ← Object, not string
        message: 'Some error',
      };

      // Should not throw
      expect(() => {
        ErrorCodeNormalizer.normalize(malformedError.code);
      }).not.toThrow();

      // Should return safe string
      const normalized = ErrorCodeNormalizer.normalize(malformedError.code);
      expect(normalized).toBe('UNKNOWN_ERROR');
    });

    it('should safely handle error.code that is undefined (original bug)', () => {
      // Original code: error.code.toUpperCase() would throw "Cannot read property 'toUpperCase' of undefined"
      const brokenError = {
        code: undefined, // ← Missing code
        message: 'Unknown error',
      };

      // Should not throw
      expect(() => {
        ErrorCodeNormalizer.normalize(brokenError.code);
      }).not.toThrow();

      // Should return safe string
      const normalized = ErrorCodeNormalizer.normalize(brokenError.code);
      expect(normalized).toBe('UNKNOWN_ERROR');
    });
  });
});
