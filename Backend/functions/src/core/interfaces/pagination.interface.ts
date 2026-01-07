/**
 * Pagination Interface
 */
export interface PaginationOptions {
  /** Page number (1-based) */
  page?: number;

  /** Items per page */
  limit?: number;

  /** Field to sort by */
  sortBy?: string;

  /** Sort direction */
  sortOrder?: 'asc' | 'desc';

  /** Cursor for cursor-based pagination */
  cursor?: string;
}

/**
 * Paginated Result
 */
export interface PaginatedResult<T> {
  /** Data items */
  data: T[];

  /** Pagination metadata */
  meta: {
    /** Total items count */
    total: number;

    /** Current page */
    page: number;

    /** Items per page */
    limit: number;

    /** Total pages */
    totalPages: number;

    /** Has next page */
    hasNext: boolean;

    /** Has previous page */
    hasPrev: boolean;

    /** Cursor for next page (cursor-based) */
    nextCursor?: string;
  };
}

/**
 * Cursor-based Pagination Result (Firestore optimized)
 */
export interface CursorPaginatedResult<T> {
  /** Data items */
  data: T[];

  /** Has more items */
  hasMore: boolean;

  /** Last document ID for next query */
  lastId?: string;
}
