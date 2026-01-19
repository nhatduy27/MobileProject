import { HttpException, HttpStatus, Logger } from '@nestjs/common';
import { ErrorCodeNormalizer } from '../utils/error-normalizer';

/**
 * Firestore Error Handler
 * Maps Firestore error codes to appropriate HTTP responses
 * Handles special cases like missing indexes
 * 
 * ⚠️ DEFENSIVE ERROR HANDLING
 * All operations wrapped to prevent crashes in error handling itself.
 * Uses ErrorCodeNormalizer to safely convert error.code to strings.
 */
export class FirestoreErrorHandler {
  private static readonly logger = new Logger(FirestoreErrorHandler.name);

  /**
   * Handle Firestore errors and convert to appropriate HTTP exceptions
   * Special handling for FAILED_PRECONDITION (missing indexes)
   * 
   * Defensive: Never throws an error while handling an error.
   * All code is wrapped with try-catch to ensure safe fallback behavior.
   */
  static handle(error: any): never {
    try {
      // Check if it's a Firestore error
      if (error && error.code) {
        // Safely normalize the error code
        const code = ErrorCodeNormalizer.normalize(error.code, 'FirestoreErrorHandler');

        // Log raw error details for debugging
        const errorDetails = ErrorCodeNormalizer.getSafeDetails(error);
        this.logger.debug(
          `Firestore error - Type: ${errorDetails.type}, Code: ${code}, Message: ${error.message}`
        );

        // Handle FAILED_PRECONDITION for missing indexes
        if (code === 'FAILED_PRECONDITION') {
          if (error.message && typeof error.message === 'string' && error.message.includes('requires an index')) {
            const indexUrl = this.extractIndexUrl(error.message) || 
              'https://console.firebase.google.com/firestore/indexes';
            
            // Check if index is currently building vs completely missing
            const isBuilding = error.message.includes('currently building') || 
                              error.message.includes('cannot be used yet');
            
            if (isBuilding) {
              // Index exists but is still building
              this.logger.warn(
                `Firestore index is building: ${error.message}. Index URL: ${indexUrl}`
              );

              throw new HttpException(
                {
                  success: false,
                  message: 'Firestore index is building. Please retry in a few minutes or enable fallback mode for development.',
                  errorCode: 'FIRESTORE_INDEX_BUILDING',
                  details: {
                    firestoreMessage: error.message,
                    indexUrl,
                    suggestion: 'Set ENABLE_FIRESTORE_PAGINATION_FALLBACK=true in .env for development',
                  },
                  timestamp: new Date().toISOString(),
                },
                HttpStatus.SERVICE_UNAVAILABLE // 503
              );
            } else {
              // Index is completely missing
              this.logger.warn(
                `Firestore missing index: ${error.message}. Index URL: ${indexUrl}`
              );

              throw new HttpException(
                {
                  success: false,
                  message: `Query requires a Firestore index. Please create the index at: ${indexUrl}`,
                  errorCode: 'FIRESTORE_INDEX_REQUIRED',
                  details: {
                    firestoreMessage: error.message,
                    indexUrl,
                  },
                  timestamp: new Date().toISOString(),
                },
                HttpStatus.PRECONDITION_FAILED // 412
              );
            }
          }
        }

        // Handle other known Firestore errors
        const statusMap: Record<string, number> = {
          'INVALID_ARGUMENT': HttpStatus.BAD_REQUEST,
          'NOT_FOUND': HttpStatus.NOT_FOUND,
          'ALREADY_EXISTS': HttpStatus.CONFLICT,
          'PERMISSION_DENIED': HttpStatus.FORBIDDEN,
          'RESOURCE_EXHAUSTED': HttpStatus.TOO_MANY_REQUESTS,
          'DEADLINE_EXCEEDED': HttpStatus.REQUEST_TIMEOUT,
          'HTTP_403': HttpStatus.FORBIDDEN,
          'HTTP_404': HttpStatus.NOT_FOUND,
          'HTTP_400': HttpStatus.BAD_REQUEST,
          'HTTP_500': HttpStatus.INTERNAL_SERVER_ERROR,
        };

        const status = statusMap[code] || HttpStatus.INTERNAL_SERVER_ERROR;
        const message = ErrorCodeNormalizer.getMessage(error, 'Database operation failed');
        
        this.logger.error(
          `Firestore error [${code}]: ${message}`
        );

        throw new HttpException(
          {
            success: false,
            message,
            errorCode: `FIRESTORE_${code}`,
            timestamp: new Date().toISOString(),
          },
          status
        );
      }

      // If not a Firestore error, re-throw (let parent error handler deal with it)
      throw error;
    } catch (err) {
      // If error is already an HttpException, re-throw it
      if (err instanceof HttpException) {
        throw err;
      }

      // Otherwise, log and throw a generic 500 error
      this.logger.error(
        `Unexpected error while handling Firestore error: ${err instanceof Error ? err.message : String(err)}`
      );

      throw new HttpException(
        {
          success: false,
          message: 'Database operation failed',
          errorCode: 'INTERNAL_ERROR',
          timestamp: new Date().toISOString(),
        },
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  /**
   * Extract Firebase Console index URL from error message
   * Firebase provides a clickable link in the error message
   */
  private static extractIndexUrl(message: string): string | null {
    try {
      // Firebase error messages contain URLs in various formats
      // Example: "The query requires an index. You can create it here: https://console.firebase.google.com/..."
      const urlMatch = message.match(/(https:\/\/console\.firebase\.google\.com\/[^\s)]+)/);
      return urlMatch ? urlMatch[1] : null;
    } catch {
      // Safe fallback if regex fails
      return null;
    }
  }
}

