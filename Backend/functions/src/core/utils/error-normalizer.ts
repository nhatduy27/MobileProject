import { Logger } from '@nestjs/common';

/**
 * Error Code Normalizer
 * 
 * Safely converts error.code from various types (string, number, object, undefined)
 * to a safe, normalized string format suitable for API error responses.
 * 
 * This prevents crashes in error handlers when error.code has unexpected types.
 */
export class ErrorCodeNormalizer {
  private static readonly logger = new Logger(ErrorCodeNormalizer.name);

  /**
   * Normalize error code to safe string format
   * 
   * Handles:
   * - String: uppercase + replace hyphens with underscores
   * - Number: prefix with "HTTP_" or "CODE_"
   * - Object: returns "UNKNOWN_ERROR"
   * - Undefined/null: returns "UNKNOWN_ERROR"
   * - Any other type: returns "UNKNOWN_ERROR"
   * 
   * NEVER throws an error
   * 
   * @param errorCode - Raw error.code from Error object
   * @param context - Optional context for logging (e.g. "FirestoreErrorHandler")
   * @returns Safe, normalized error code string
   */
  static normalize(errorCode: unknown, context: string = 'ErrorHandler'): string {
    try {
      // Case 1: String - uppercase and replace hyphens with underscores
      if (typeof errorCode === 'string') {
        const normalized = errorCode.toUpperCase().replace(/-/g, '_');
        return normalized;
      }

      // Case 2: Number - prefix with HTTP_ or CODE_
      if (typeof errorCode === 'number') {
        // If it's a likely HTTP status code (100-599), prefix with HTTP_
        if (errorCode >= 100 && errorCode <= 599) {
          return `HTTP_${errorCode}`;
        }
        // Otherwise use CODE_ prefix
        return `CODE_${errorCode}`;
      }

      // Case 3: Object (including null) - unsafe, cannot use directly
      if (typeof errorCode === 'object') {
        // Attempt to get a string representation for logging (non-sensitive info)
        const objDesc = errorCode === null 
          ? 'null' 
          : (errorCode as any).code 
            ? `object:${String((errorCode as any).code)}`
            : 'object';
        
        this.logger.warn(
          `[${context}] error.code is an ${objDesc}, falling back to UNKNOWN_ERROR`
        );
        return 'UNKNOWN_ERROR';
      }

      // Case 4: Any other type (symbol, function, etc.) - unknown
      this.logger.warn(
        `[${context}] error.code is type ${typeof errorCode}, falling back to UNKNOWN_ERROR`
      );
      return 'UNKNOWN_ERROR';
    } catch (err: unknown) {
      // Defensive: even if something goes wrong in normalization, return safe default
      this.logger.error(
        `[${context}] Error during code normalization: ${err instanceof Error ? err.message : String(err)}`
      );
      return 'UNKNOWN_ERROR';
    }
  }

  /**
   * Extract safe error message from error object
   * 
   * Safely gets error message without throwing,
   * falls back to generic message if error.message is unavailable
   * 
   * @param error - Error object
   * @param defaultMessage - Message to use if error.message is not available
   * @returns Safe error message string
   */
  static getMessage(
    error: unknown,
    defaultMessage: string = 'Operation failed'
  ): string {
    try {
      if (error instanceof Error) {
        return error.message;
      }

      if (typeof error === 'object' && error !== null) {
        const msg = (error as any).message;
        if (typeof msg === 'string') {
          return msg;
        }
      }

      return defaultMessage;
    } catch {
      return defaultMessage;
    }
  }

  /**
   * Get safe error details for logging
   * 
   * Extracts non-sensitive error info for debugging without leaking secrets
   * 
   * @param error - Error object
   * @returns Object with safe error info
   */
  static getSafeDetails(error: unknown): {
    type: string;
    hasCode: boolean;
    codeType: string;
    message?: string;
  } {
    try {
      if (error instanceof Error) {
        return {
          type: error.constructor.name,
          hasCode: 'code' in error,
          codeType: error && 'code' in error ? typeof (error as any).code : 'n/a',
          message: error.message,
        };
      }

      if (typeof error === 'object' && error !== null) {
        return {
          type: error.constructor?.name || 'Object',
          hasCode: 'code' in error,
          codeType: 'code' in error ? typeof (error as any).code : 'n/a',
          message: (error as any).message,
        };
      }

      return {
        type: typeof error,
        hasCode: false,
        codeType: 'n/a',
      };
    } catch {
      return {
        type: 'unknown',
        hasCode: false,
        codeType: 'n/a',
      };
    }
  }
}
