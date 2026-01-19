import { HttpException, HttpStatus } from '@nestjs/common';
import { FirestoreErrorHandler } from './firestore-error.handler';

describe('FirestoreErrorHandler', () => {
  describe('handle - FAILED_PRECONDITION with missing index', () => {
    it('should convert FAILED_PRECONDITION (missing index) to HTTP 412', () => {
      const error = {
        code: 'FAILED_PRECONDITION',
        message:
          'The query requires an index. You can create it here: https://console.firebase.google.com/firestore/indexes?create_composite=ClRQCgoJdXNlcnNfcmVhZBIIChAKBnVzZXJJZBACEA0KCG9yZGVyQnkQAw==',
      };

      expect(() => FirestoreErrorHandler.handle(error)).toThrow(HttpException);

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception).toBeInstanceOf(HttpException);
        expect(exception.getStatus()).toBe(HttpStatus.PRECONDITION_FAILED); // 412
        
        const response = exception.getResponse() as Record<string, unknown>;
        expect(response.errorCode).toBe('FIRESTORE_INDEX_REQUIRED');
        expect((response.message as string)).toContain('Query requires a Firestore index');
        const details = response.details as Record<string, unknown>;
        expect((details.indexUrl as string)).toContain('console.firebase.google.com');
      }
    });

    it('should extract Firebase Console URL from error message', () => {
      const error = {
        code: 'FAILED_PRECONDITION',
        message:
          'The query requires an index. You can create it here: https://console.firebase.google.com/firestore/indexes?create_composite=ClRQCgoJdXNlcnNfcmVhZBIIChAKBnVzZXJJZBACEA0KCG9yZGVyQnkQAw==',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        const response = exception.getResponse() as Record<string, unknown>;
        const details = response.details as Record<string, unknown>;
        expect((details.indexUrl as string)).toBe(
          'https://console.firebase.google.com/firestore/indexes?create_composite=ClRQCgoJdXNlcnNfcmVhZBIIChAKBnVzZXJJZBACEA0KCG9yZGVyQnkQAw=='
        );
      }
    });

    it('should include developer-friendly message with index URL', () => {
      const error = {
        code: 'FAILED_PRECONDITION',
        message:
          'The query requires an index. You can create it here: https://console.firebase.google.com/firestore/indexes',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        const response = exception.getResponse() as Record<string, unknown>;
        expect((response.message as string)).toContain('index');
        expect((response.message as string)).toContain('https://console.firebase.google.com/firestore/indexes');
        const details = response.details as Record<string, unknown>;
        expect((details.firestoreMessage as string)).toBe(error.message);
      }
    });

    it('should return 412, not 500 for missing index', () => {
      const error = {
        code: 'FAILED_PRECONDITION',
        message: 'The query requires an index. You can create it here: https://console.firebase.google.com/firestore/indexes',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        const status = exception.getStatus();
        expect(status).toBe(412);
        expect(status).not.toBe(500);
      }
    });

    it('should include timestamp in error response', () => {
      const error = {
        code: 'FAILED_PRECONDITION',
        message: 'The query requires an index',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        const response = exception.getResponse() as Record<string, unknown>;
        expect(response.timestamp).toBeDefined();
        expect(new Date(response.timestamp as string)).toBeInstanceOf(Date);
      }
    });

    it('should handle index building (503) vs index missing (412)', () => {
      // Test: Index is building
      const buildingError = {
        code: 'FAILED_PRECONDITION',
        message: 'The query requires an index. That index is currently building and cannot be used yet.',
      };

      try {
        FirestoreErrorHandler.handle(buildingError);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.SERVICE_UNAVAILABLE); // 503
        const response = exception.getResponse() as Record<string, unknown>;
        expect(response.errorCode).toBe('FIRESTORE_INDEX_BUILDING');
        expect((response.message as string)).toContain('index is building');
      }

      // Test: Index is missing
      const missingError = {
        code: 'FAILED_PRECONDITION',
        message: 'The query requires an index. You can create it here: https://console.firebase.google.com/firestore/indexes',
      };

      try {
        FirestoreErrorHandler.handle(missingError);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.PRECONDITION_FAILED); // 412
        const response = exception.getResponse() as Record<string, unknown>;
        expect(response.errorCode).toBe('FIRESTORE_INDEX_REQUIRED');
      }
    });
  });

  describe('handle - other Firestore errors', () => {
    it('should map INVALID_ARGUMENT to HTTP 400', () => {
      const error = {
        code: 'INVALID_ARGUMENT',
        message: 'Invalid query parameter',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.BAD_REQUEST);
        const response = exception.getResponse() as Record<string, unknown>;
        expect((response.errorCode as string)).toContain('INVALID_ARGUMENT');
      }
    });

    it('should map PERMISSION_DENIED to HTTP 403', () => {
      const error = {
        code: 'PERMISSION_DENIED',
        message: 'Permission denied',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.FORBIDDEN);
        const response = exception.getResponse() as Record<string, unknown>;
        expect((response.errorCode as string)).toContain('PERMISSION_DENIED');
      }
    });

    it('should map NOT_FOUND to HTTP 404', () => {
      const error = {
        code: 'NOT_FOUND',
        message: 'Document not found',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.NOT_FOUND);
      }
    });

    it('should map RESOURCE_EXHAUSTED to HTTP 429', () => {
      const error = {
        code: 'RESOURCE_EXHAUSTED',
        message: 'Too many requests',
      };

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.TOO_MANY_REQUESTS);
      }
    });
  });

  describe('handle - non-Firestore errors', () => {
    it('should re-throw non-Firestore errors when they are HttpExceptions', () => {
      const httpError = new HttpException('Not Found', HttpStatus.NOT_FOUND);

      expect(() => FirestoreErrorHandler.handle(httpError)).toThrow(httpError);
    });

    it('should wrap non-Firestore errors in generic 500 HttpException', () => {
      const error = new Error('Generic error');

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.INTERNAL_SERVER_ERROR);
        const response = exception.getResponse();
        expect(typeof response).toBe('object');
        if (typeof response === 'object') {
          expect((response as any).success).toBe(false);
          expect((response as any).errorCode).toBe('INTERNAL_ERROR');
        }
      }
    });

    it('should wrap errors without code property in generic 500 HttpException', () => {
      const error = new Error('Error without code');

      try {
        FirestoreErrorHandler.handle(error);
      } catch (thrown: unknown) {
        const exception = thrown as HttpException;
        expect(exception.getStatus()).toBe(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    });
  });
});
