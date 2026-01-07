/**
 * Standard API Response
 */
export interface ApiResponse<T = unknown> {
  /** Success status */
  success: boolean;

  /** Response data */
  data?: T;

  /** Error message */
  message?: string;

  /** Error code */
  errorCode?: string;

  /** Timestamp */
  timestamp: string;
}

/**
 * Success Response
 */
export interface SuccessResponse<T> extends ApiResponse<T> {
  success: true;
  data: T;
}

/**
 * Error Response
 */
export interface ErrorResponse extends ApiResponse {
  success: false;
  message: string;
  errorCode: string;
  details?: Record<string, unknown>;
}
